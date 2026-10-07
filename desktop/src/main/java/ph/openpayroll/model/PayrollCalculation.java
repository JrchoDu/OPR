package ph.openpayroll.model;

import java.math.BigDecimal;

/** A payroll calculation snapshot retained for the employee's history. */
public record PayrollCalculation(
        long id,
        String calculatedAt,
        String employeeId,
        String employeeName,
        String position,
        String frequency,
        String periodStart,
        String periodEnd,
        BigDecimal basicSalary,
        BigDecimal overtime,
        BigDecimal allowance,
        BigDecimal otherDeductions,
        BigDecimal grossPay,
        BigDecimal taxableCompensation,
        BigDecimal withholdingTax,
        BigDecimal employeeSss,
        BigDecimal employeePhilhealth,
        BigDecimal employeePagibig,
        BigDecimal totalDeductions,
        BigDecimal netPay,
        BigDecimal employerCost) {
}
