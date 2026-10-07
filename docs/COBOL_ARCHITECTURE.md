# COBOL architecture

`PAYROLL.CBL` is the executable coordinator and structured file boundary. Java reads and validates the source CSV tables, then passes values as request fields. The coordinator loads the values into typed tables, calls the SSS, PhilHealth, Pag-IBIG and BIR modules, then calls gross-pay, deductions, net-pay and employer-cost routines. Rates live in the versioned CSV bundle, not in the COBOL calculation modules. The supported scope and exclusions are in `docs/GOVERNMENT_SOURCE_REVIEW_2026.md`.
