# Troubleshooting

- `cobc` missing: install GnuCOBOL and place compiler/runtime on PATH.
- Engine missing: run the COBOL build script before opening Payroll.
- `RULES_UNVERIFIED` or `Unverified or malformed row`: check that every CSV row in `config/ph/2026` is intact and marked `YES`; do not bypass loader validation.
- `SSS brackets`, `BIR brackets`, or agency rule invalid: restore the sourced CSV table from version control and check that you are using the matching frequency table.
- Payroll calculation returns `STATUS=ERROR`: read the accompanying `ERROR_CODE` and `ERROR_MESSAGE`; add a regression case before changing a rule or calculation routine.
- JavaFX launch error: use Java 21 and run with `scripts/run.*`/Maven plugin.
- SQLite error: ensure `data/` is writable; inspect `logs/application.log`.
