package ph.openpayroll.data;

import ph.openpayroll.model.PayrollCalculation;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Stores and reads timestamped payroll calculation snapshots. */
public final class PayrollHistoryRepository {
    private final Database database;

    public PayrollHistoryRepository(Database database) {
        this.database = database;
    }

    public void save(PayrollCalculation calculation) throws SQLException {
        String sql = """
                INSERT INTO payroll_calculations (
                    calculated_at, employee_id, employee_name, `position`, pay_frequency,
                    period_start, period_end, basic_salary, overtime, allowance,
                    other_deductions, gross_pay, taxable_compensation, withholding_tax,
                    employee_sss, employee_philhealth, employee_pagibig,
                    total_deductions, net_pay, employer_cost
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, calculation.calculatedAt());
            statement.setString(2, calculation.employeeId());
            statement.setString(3, calculation.employeeName());
            statement.setString(4, calculation.position());
            statement.setString(5, calculation.frequency());
            statement.setString(6, calculation.periodStart());
            statement.setString(7, calculation.periodEnd());
            statement.setString(8, calculation.basicSalary().toPlainString());
            statement.setString(9, calculation.overtime().toPlainString());
            statement.setString(10, calculation.allowance().toPlainString());
            statement.setString(11, calculation.otherDeductions().toPlainString());
            statement.setString(12, calculation.grossPay().toPlainString());
            statement.setString(13, calculation.taxableCompensation().toPlainString());
            statement.setString(14, calculation.withholdingTax().toPlainString());
            statement.setString(15, calculation.employeeSss().toPlainString());
            statement.setString(16, calculation.employeePhilhealth().toPlainString());
            statement.setString(17, calculation.employeePagibig().toPlainString());
            statement.setString(18, calculation.totalDeductions().toPlainString());
            statement.setString(19, calculation.netPay().toPlainString());
            statement.setString(20, calculation.employerCost().toPlainString());
            statement.executeUpdate();
        }
    }

    public List<PayrollCalculation> listRecent() throws SQLException {
        String sql = """
                SELECT *
                FROM payroll_calculations
                ORDER BY calculated_at DESC, id DESC
                """;
        List<PayrollCalculation> calculations = new ArrayList<>();

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                calculations.add(new PayrollCalculation(
                        result.getLong("id"),
                        result.getString("calculated_at"),
                        result.getString("employee_id"),
                        result.getString("employee_name"),
                        result.getString("position"),
                        result.getString("pay_frequency"),
                        result.getString("period_start"),
                        result.getString("period_end"),
                        decimal(result, "basic_salary"),
                        decimal(result, "overtime"),
                        decimal(result, "allowance"),
                        decimal(result, "other_deductions"),
                        decimal(result, "gross_pay"),
                        decimal(result, "taxable_compensation"),
                        decimal(result, "withholding_tax"),
                        decimal(result, "employee_sss"),
                        decimal(result, "employee_philhealth"),
                        decimal(result, "employee_pagibig"),
                        decimal(result, "total_deductions"),
                        decimal(result, "net_pay"),
                        decimal(result, "employer_cost")));
            }
        }

        return calculations;
    }

    private BigDecimal decimal(ResultSet result, String column) throws SQLException {
        return new BigDecimal(result.getString(column));
    }
}
