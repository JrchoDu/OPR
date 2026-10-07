# Payroll flow

1. Java validates the supplied pay-period earnings and loads the selected frequency's versioned agency tables.
2. Java rejects missing, unverified, changed, incomplete, or out-of-range government data.
3. Java writes the earnings and validated rule rows into a temporary key/value request file.
4. COBOL calculates gross pay, SSS, PhilHealth, Pag-IBIG, BIR withholding, total employee deductions, net pay, and employer cost using fixed-decimal fields.
5. COBOL returns a complete result file; Java accepts it only when status is `OK` and every expected monetary field is present.

Employee contribution amounts reduce net pay; employer shares are included only in employer cost. Monthly contributions are split equally across two semi-monthly checks. Supported scope and limitations are documented in `GOVERNMENT_SOURCE_REVIEW_2026.md`.
