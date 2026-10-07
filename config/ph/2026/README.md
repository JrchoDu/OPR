# Philippine payroll rule bundle PH-2026.1

Scope: ordinary private-sector employer/employee SSS members, monthly or semi-monthly pay. Sourced from the official agency materials and government reports listed in `docs/GOVERNMENT_SOURCE_REVIEW_2026.md`, reviewed 2026-10-07.

All CSV rows are marked `YES` and are checked by `GovernmentRuleBundle` before the request is passed to COBOL. The loader validates every SSS row, the exact PhilHealth/Pag-IBIG schedule, and complete monthly/semi-monthly BIR brackets. Missing or changed rows block calculation.

Effective schedules represented: SSS Circular 2024-006 from 2025-01-01 (latest business employee schedule located); PhilHealth 2026 5% schedule as announced by PhilHealth and reported by PIA; Pag-IBIG Circular 460 from 2024-02-01; BIR RR 11-2018 Annex E from 2023-01-01 onwards.

This is a payroll estimate for a narrow set of ordinary earnings. All input earnings are treated as taxable; no absences, partial-month adjustments, MWE exemptions, 13th-month/de-minimis calculations, non-taxable benefits, loans, annualization, payroll persistence, or remittance reports are implemented. See the source review before relying on the output for final disbursement or filing.
