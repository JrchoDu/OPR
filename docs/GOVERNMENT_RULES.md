# Government rule versioning

The active sample bundle is `PH-2026.1` under `config/ph/2026`. The cited rates and effective dates are summarized in [GOVERNMENT_SOURCE_REVIEW_2026.md](GOVERNMENT_SOURCE_REVIEW_2026.md). The Java loader validates the exact supported rule rows before passing them to COBOL; a missing or modified row blocks calculation.

Each payroll calculation must remain tied to its rule version. When an agency publishes a new schedule, create a new effective-dated bundle, update its sources and checksum/review notes, and add new boundary fixtures. Do not overwrite historical tables. The current implementation supports ordinary private-sector employees on monthly and semi-monthly payroll only. See source-review limits before using this as a final payroll record.
