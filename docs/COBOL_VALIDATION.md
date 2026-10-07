# VALIDATION module

Purpose: basic signed decimal input validation. Inputs: basic salary, aggregate earnings, aggregate deductions as signed fixed decimal. Output: `Y` only if all three are non-negative. Copybooks/configuration: none. Formula: reject if any value is below zero. Flow: set valid then test fields. Errors: caller maps `N` to a user-safe message. Example: salary -1.00 produces `N`. Compile with engine scripts; add cases to COBOL tests. Java invokes only through coordinator. Common error: validating after arithmetic. Future: field-specific errors, bounds, and frequency validation.
