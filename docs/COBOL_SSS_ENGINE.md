# SSS engine

`SSSCALC.CBL` selects an inclusive compensation bracket from the SSS table loaded into COBOL by `PAYROLL.CBL`. The source bundle has 61 cents-based employee brackets derived from SSS Circular 2024-006. Employee shares use the 5% rate; employer shares use 10% plus the employer-only EC amount. MPF is included in total SSS shares above the ₱20,000 MSC while the official salary-credit brackets cap at ₱35,000. The loader checks every bracket and amount before invoking COBOL. Source and scope: `GOVERNMENT_SOURCE_REVIEW_2026.md`.
