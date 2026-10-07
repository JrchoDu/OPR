package ph.openpayroll.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/** Opens and initializes the local SQLite or configured MySQL database. */
public final class Database {
    private static final String MYSQL_PREFIX = "jdbc:mysql://";

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final Path sqlitePath;

    /** Uses the MySQL environment settings when present; otherwise uses the SQLite file. */
    public Database(Path fallbackPath) {
        String configuredUrl = System.getenv("OPENPAYROLL_DB_URL");
        if (configuredUrl == null || configuredUrl.isBlank()) {
            sqlitePath = fallbackPath.toAbsolutePath().normalize();
            jdbcUrl = "jdbc:sqlite:" + sqlitePath;
            username = null;
            password = null;
            return;
        }

        if (!configuredUrl.startsWith(MYSQL_PREFIX)) {
            throw new IllegalArgumentException(
                    "OPENPAYROLL_DB_URL must start with jdbc:mysql://, or be unset to use SQLite");
        }

        sqlitePath = null;
        jdbcUrl = configuredUrl;
        username = System.getenv("OPENPAYROLL_DB_USER");
        password = System.getenv("OPENPAYROLL_DB_PASSWORD");
    }

    public boolean isSqlite() {
        return sqlitePath != null;
    }

    public Path path() {
        if (!isSqlite()) {
            throw new IllegalStateException("A MySQL database does not have a local SQLite file path");
        }
        return sqlitePath;
    }

    public Connection connect() throws SQLException {
        if (isSqlite()) {
            return DriverManager.getConnection(jdbcUrl);
        }

        Properties credentials = new Properties();
        if (username != null && !username.isBlank()) {
            credentials.setProperty("user", username);
        }
        if (password != null) {
            credentials.setProperty("password", password);
        }
        return DriverManager.getConnection(jdbcUrl, credentials);
    }

    public void initialize() throws Exception {
        if (isSqlite()) {
            Files.createDirectories(sqlitePath.getParent());
        }

        List<String> schema = isSqlite() ? sqliteSchema() : mysqlSchema();
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            if (isSqlite()) {
                statement.execute("PRAGMA foreign_keys=ON");
            }
            for (String sql : schema) {
                statement.execute(sql);
            }
        }
    }

    private List<String> sqliteSchema() {
        return List.of(
                """
                CREATE TABLE IF NOT EXISTS employees (
                    id TEXT PRIMARY KEY,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    position TEXT NOT NULL DEFAULT '',
                    basic_salary TEXT NOT NULL,
                    pay_frequency TEXT NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS payroll_runs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    period_start TEXT NOT NULL,
                    period_end TEXT NOT NULL,
                    pay_frequency TEXT NOT NULL,
                    status TEXT NOT NULL,
                    rule_version TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS payroll_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    payroll_run_id INTEGER NOT NULL REFERENCES payroll_runs(id),
                    employee_id TEXT NOT NULL REFERENCES employees(id),
                    input_payload TEXT NOT NULL,
                    result_payload TEXT NOT NULL
                )
                """,
                payrollCalculationsSchema(),
                """
                CREATE TABLE IF NOT EXISTS application_settings (
                    `key` TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    actor TEXT NOT NULL,
                    action TEXT NOT NULL,
                    entity TEXT NOT NULL,
                    entity_id TEXT NOT NULL,
                    `timestamp` TEXT NOT NULL,
                    before_value TEXT,
                    after_value TEXT
                )
                """);
    }

    private List<String> mysqlSchema() {
        return List.of(
                """
                CREATE TABLE IF NOT EXISTS employees (
                    id VARCHAR(100) PRIMARY KEY,
                    first_name VARCHAR(255) NOT NULL,
                    last_name VARCHAR(255) NOT NULL,
                    `position` VARCHAR(255) NOT NULL DEFAULT '',
                    basic_salary DECIMAL(12, 2) NOT NULL,
                    pay_frequency VARCHAR(20) NOT NULL,
                    active TINYINT NOT NULL DEFAULT 1,
                    created_at VARCHAR(40) NOT NULL
                ) ENGINE=InnoDB
                """,
                """
                CREATE TABLE IF NOT EXISTS payroll_runs (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    period_start VARCHAR(10) NOT NULL,
                    period_end VARCHAR(10) NOT NULL,
                    pay_frequency VARCHAR(20) NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    rule_version VARCHAR(40) NOT NULL,
                    created_at VARCHAR(40) NOT NULL
                ) ENGINE=InnoDB
                """,
                """
                CREATE TABLE IF NOT EXISTS payroll_entries (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    payroll_run_id BIGINT NOT NULL,
                    employee_id VARCHAR(100) NOT NULL,
                    input_payload LONGTEXT NOT NULL,
                    result_payload LONGTEXT NOT NULL,
                    CONSTRAINT fk_payroll_entries_run
                        FOREIGN KEY (payroll_run_id) REFERENCES payroll_runs(id),
                    CONSTRAINT fk_payroll_entries_employee
                        FOREIGN KEY (employee_id) REFERENCES employees(id)
                ) ENGINE=InnoDB
                """,
                payrollCalculationsSchema(),
                """
                CREATE TABLE IF NOT EXISTS application_settings (
                    `key` VARCHAR(255) PRIMARY KEY,
                    value TEXT NOT NULL
                ) ENGINE=InnoDB
                """,
                """
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    actor VARCHAR(255) NOT NULL,
                    action VARCHAR(100) NOT NULL,
                    entity VARCHAR(100) NOT NULL,
                    entity_id VARCHAR(255) NOT NULL,
                    `timestamp` VARCHAR(40) NOT NULL,
                    before_value LONGTEXT,
                    after_value LONGTEXT
                ) ENGINE=InnoDB
                """);
    }

    private String payrollCalculationsSchema() {
        if (isSqlite()) {
            return """
                    CREATE TABLE IF NOT EXISTS payroll_calculations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        calculated_at TEXT NOT NULL,
                        employee_id TEXT NOT NULL,
                        employee_name TEXT NOT NULL,
                        position TEXT NOT NULL,
                        pay_frequency TEXT NOT NULL,
                        period_start TEXT NOT NULL,
                        period_end TEXT NOT NULL,
                        basic_salary TEXT NOT NULL,
                        overtime TEXT NOT NULL,
                        allowance TEXT NOT NULL,
                        other_deductions TEXT NOT NULL,
                        gross_pay TEXT NOT NULL,
                        taxable_compensation TEXT NOT NULL,
                        withholding_tax TEXT NOT NULL,
                        employee_sss TEXT NOT NULL,
                        employee_philhealth TEXT NOT NULL,
                        employee_pagibig TEXT NOT NULL,
                        total_deductions TEXT NOT NULL,
                        net_pay TEXT NOT NULL,
                        employer_cost TEXT NOT NULL
                    )
                    """;
        }

        return """
                CREATE TABLE IF NOT EXISTS payroll_calculations (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    calculated_at VARCHAR(40) NOT NULL,
                    employee_id VARCHAR(100) NOT NULL,
                    employee_name VARCHAR(511) NOT NULL,
                    `position` VARCHAR(255) NOT NULL,
                    pay_frequency VARCHAR(20) NOT NULL,
                    period_start VARCHAR(10) NOT NULL,
                    period_end VARCHAR(10) NOT NULL,
                    basic_salary DECIMAL(12, 2) NOT NULL,
                    overtime DECIMAL(12, 2) NOT NULL,
                    allowance DECIMAL(12, 2) NOT NULL,
                    other_deductions DECIMAL(12, 2) NOT NULL,
                    gross_pay DECIMAL(12, 2) NOT NULL,
                    taxable_compensation DECIMAL(12, 2) NOT NULL,
                    withholding_tax DECIMAL(12, 2) NOT NULL,
                    employee_sss DECIMAL(12, 2) NOT NULL,
                    employee_philhealth DECIMAL(12, 2) NOT NULL,
                    employee_pagibig DECIMAL(12, 2) NOT NULL,
                    total_deductions DECIMAL(12, 2) NOT NULL,
                    net_pay DECIMAL(12, 2) NOT NULL,
                    employer_cost DECIMAL(12, 2) NOT NULL
                ) ENGINE=InnoDB
                """;
    }
}
