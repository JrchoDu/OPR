# Contribution tables

`config/ph/2026` contains a sourced rule bundle for ordinary private-sector employees paid monthly or semi-monthly. Java validates every row and the bundle's fixed rates and boundaries before passing the rules to COBOL. COBOL applies the contribution and tax tables and returns a detailed breakdown.

The bundle source review and assumptions are in [GOVERNMENT_SOURCE_REVIEW_2026.md](GOVERNMENT_SOURCE_REVIEW_2026.md). The machine-readable files are:

- `sss.csv`: 61 SSS compensation brackets, employee 5% and employer 10% shares, plus employer EC contribution.
- `philhealth.csv`: 5% rate, ₱10,000 floor, ₱100,000 ceiling, and equal employee/employer split.
- `pagibig.csv`: employee 1%/2% threshold schedule, 2% employer rate, and ₱10,000 cap.
- `bir-monthly.csv` and `bir-semi-monthly.csv`: BIR Annex E bracket tables.

`verified=YES` means the row matches the cited government schedule and the loader's source-specific validation. It is not a government endorsement or independent accountant/legal certification. Review source updates before using a different payroll year or employee class.

The calculation assumes every supplied earnings item (basic pay, overtime, taxable allowance) is taxable compensation. The UI's other-deduction field is a post-tax deduction. Monthly statutory contributions are divided equally across the two semi-monthly paychecks. The current input model does not support absences, partial months, minimum-wage-earner exemptions, 13th-month/de-minimis treatment, non-taxable benefits, loans, annualized year-end adjustments, or employee-specific statutory exemptions. See [PROJECT_STATUS.md](PROJECT_STATUS.md); do not use this estimate for final payroll filing without reviewing those cases.
