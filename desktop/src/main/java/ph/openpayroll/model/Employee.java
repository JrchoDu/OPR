package ph.openpayroll.model;

import java.math.BigDecimal;

/** Employee details used by the local payroll workspace. */
public record Employee(
        String id,
        String firstName,
        String lastName,
        String position,
        BigDecimal salary,
        String frequency,
        boolean active) {

    public String fullName() {
        return (firstName + " " + lastName).trim();
    }
}
