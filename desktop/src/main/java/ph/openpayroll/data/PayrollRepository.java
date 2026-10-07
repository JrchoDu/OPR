package ph.openpayroll.data;

import ph.openpayroll.model.Employee;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provides persistence operations for employees, payroll periods, and settings. */
public final class PayrollRepository {
    private final Database database;

    public PayrollRepository(Database database) {
        this.database = database;
    }

    public void saveEmployee(Employee employee) throws SQLException {
        if (employee.id().isBlank()
                || employee.firstName().isBlank()
                || employee.lastName().isBlank()
                || employee.salary().signum() < 0) {
            throw new IllegalArgumentException("Invalid employee");
        }

        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            String before = findEmployeeAuditValue(connection, employee.id());
            upsertEmployee(connection, employee);
            writeEmployeeAudit(connection, employee, before);
            connection.commit();
        }
    }

    private String findEmployeeAuditValue(Connection connection, String employeeId) throws SQLException {
        String sql = "SELECT basic_salary, active FROM employees WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employeeId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return "salary=" + result.getString("basic_salary")
                        + ",active=" + result.getInt("active");
            }
        }
    }

    private void upsertEmployee(Connection connection, Employee employee) throws SQLException {
        String sql = database.isSqlite()
                ? """
                  INSERT INTO employees (
                      id, first_name, last_name, `position`, basic_salary,
                      pay_frequency, active, created_at
                  ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                  ON CONFLICT(id) DO UPDATE SET
                      first_name = excluded.first_name,
                      last_name = excluded.last_name,
                      `position` = excluded.`position`,
                      basic_salary = excluded.basic_salary,
                      pay_frequency = excluded.pay_frequency,
                      active = excluded.active
                  """
                : """
                  INSERT INTO employees (
                      id, first_name, last_name, `position`, basic_salary,
                      pay_frequency, active, created_at
                  ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                  ON DUPLICATE KEY UPDATE
                      first_name = VALUES(first_name),
                      last_name = VALUES(last_name),
                      `position` = VALUES(`position`),
                      basic_salary = VALUES(basic_salary),
                      pay_frequency = VALUES(pay_frequency),
                      active = VALUES(active)
                  """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.id());
            statement.setString(2, employee.firstName());
            statement.setString(3, employee.lastName());
            statement.setString(4, employee.position());
            statement.setString(5, employee.salary().toPlainString());
            statement.setString(6, employee.frequency());
            statement.setInt(7, employee.active() ? 1 : 0);
            statement.setString(8, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    private void writeEmployeeAudit(Connection connection, Employee employee, String before)
            throws SQLException {
        String sql = """
                INSERT INTO audit_logs (
                    actor, action, entity, entity_id, timestamp, before_value, after_value
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "LOCAL_OPERATOR");
            statement.setString(2, before == null ? "EMPLOYEE_CREATED" : "EMPLOYEE_UPDATED");
            statement.setString(3, "employees");
            statement.setString(4, employee.id());
            statement.setString(5, Instant.now().toString());
            statement.setString(6, before);
            statement.setString(7, "salary=" + employee.salary().toPlainString()
                    + ",active=" + (employee.active() ? 1 : 0));
            statement.executeUpdate();
        }
    }

    public List<Employee> listEmployees() throws SQLException {
        String sql = "SELECT * FROM employees ORDER BY last_name, first_name";
        List<Employee> employees = new ArrayList<>();

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                employees.add(new Employee(
                        result.getString("id"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getString("position"),
                        new BigDecimal(result.getString("basic_salary")),
                        result.getString("pay_frequency"),
                        result.getInt("active") != 0));
            }
        }

        return employees;
    }

    public void deleteEmployee(String employeeId) throws SQLException {
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);

            if (hasPayrollRecords(connection, employeeId)) {
                throw new IllegalStateException(
                        "This employee has payroll records and cannot be deleted. Edit the employee "
                                + "and clear Active to archive the record instead.");
            }

            String before = findEmployeeAuditValue(connection, employeeId);
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM employees WHERE id = ?")) {
                statement.setString(1, employeeId);
                if (statement.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Employee not found: " + employeeId);
                }
            }

            writeEmployeeDeleteAudit(connection, employeeId, before);
            connection.commit();
        }
    }

    private boolean hasPayrollRecords(Connection connection, String employeeId) throws SQLException {
        for (String table : List.of("payroll_entries", "payroll_calculations")) {
            String sql = "SELECT count(*) FROM " + table + " WHERE employee_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, employeeId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next() && result.getLong(1) > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void writeEmployeeDeleteAudit(Connection connection, String employeeId, String before)
            throws SQLException {
        String sql = """
                INSERT INTO audit_logs (
                    actor, action, entity, entity_id, timestamp, before_value, after_value
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "LOCAL_OPERATOR");
            statement.setString(2, "EMPLOYEE_DELETED");
            statement.setString(3, "employees");
            statement.setString(4, employeeId);
            statement.setString(5, Instant.now().toString());
            statement.setString(6, before);
            statement.setString(7, null);
            statement.executeUpdate();
        }
    }

    public long countEmployees() throws SQLException {
        String sql = "SELECT count(*) FROM employees WHERE active = 1";
        try (Connection connection = database.connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getLong(1) : 0;
        }
    }

    public long countPayrollRuns() throws SQLException {
        String sql = "SELECT count(*) FROM payroll_runs";
        try (Connection connection = database.connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getLong(1) : 0;
        }
    }

    public void createDraftRun(String start, String end, String frequency, String ruleVersion)
            throws SQLException {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("Payroll period start must be before its end");
        }

        String sql = """
                INSERT INTO payroll_runs (
                    period_start, period_end, pay_frequency, status, rule_version, created_at
                ) VALUES (?, ?, ?, 'DRAFT', ?, ?)
                """;
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, start);
            statement.setString(2, end);
            statement.setString(3, frequency);
            statement.setString(4, ruleVersion);
            statement.setString(5, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    public Map<String, String> settings() throws SQLException {
        Map<String, String> values = new HashMap<>();
        String sql = "SELECT `key` AS setting_key, value FROM application_settings";

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                values.put(result.getString("setting_key"), result.getString("value"));
            }
        }

        return values;
    }

    public void saveSettings(Map<String, String> values) throws SQLException {
        String sql = database.isSqlite()
                ? """
                  INSERT INTO application_settings (`key`, value) VALUES (?, ?)
                  ON CONFLICT(`key`) DO UPDATE SET value = excluded.value
                  """
                : """
                  INSERT INTO application_settings (`key`, value) VALUES (?, ?)
                  ON DUPLICATE KEY UPDATE value = VALUES(value)
                  """;

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (var entry : values.entrySet()) {
                statement.setString(1, entry.getKey());
                statement.setString(2, entry.getValue());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
