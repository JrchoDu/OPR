# OpenPayroll PH

OpenPayroll PH is an open-source desktop payroll project for the Philippines. It uses Java 21 and JavaFX for the desktop application, SQLite by default or optional MySQL for storage, and GNU COBOL for payroll calculation routines.

> **Payroll scope:** Calculations are estimates for supported private-sector cases using the configured PH-2026.1 rule tables. The current input model treats entered earnings as taxable. Exemptions, partial months, loans, annualization, approval/finalization, and remittance reporting are not fully modeled. Review [the rule source review](docs/GOVERNMENT_SOURCE_REVIEW_2026.md) before using results for payroll disbursement or filing.

## What it does

- Create, view, update, and archive employee records.
- Create draft payroll periods and calculate supported payroll estimates.
- Save successful calculations with timestamps in Payroll History.
- Store data locally in SQLite, or connect to a MySQL server.
- Validate configured statutory rule tables before calculation.
- Use Java for the desktop interface, database access, and validation, with COBOL modules for payroll arithmetic.

The application does not yet provide authentication or roles, employee-specific statutory profiles, payslips, statutory reports, payroll approval/finalization, or every special tax and contribution case. It is early-stage software and is not a substitute for review by a qualified payroll professional.

## Requirements

- Java 21 or later
- Maven 3.9 or later
- GNU COBOL (`cobc`) and its runtime

See [dependency setup](docs/DEPENDENCIES.md) for installation details and supported environment setup.

## Build and run

From the repository root on Windows:

```powershell
scripts\build-all.bat
scripts\run.bat
```

On Linux or macOS:

```sh
./scripts/build-all.sh
./scripts/run.sh
```

To run the COBOL checks, use `scripts/test-cobol.bat` on Windows or `./scripts/test-cobol.sh` on Linux/macOS. For Java unit and integration tests, see the Testing section in [CONTRIBUTING.md](CONTRIBUTING.md) and the Maven configuration in `desktop/pom.xml`.

## Database setup

SQLite is the default. On first launch, the application creates `data/openpayroll.db` and its tables. Database files are local application data and are excluded from Git.

To use MySQL, create an empty database and an application user on your MySQL server. phpMyAdmin can administer that server, but the desktop app connects to MySQL directly. Set these variables in the same terminal used to launch the application:

```powershell
$env:OPENPAYROLL_DB_URL = 'jdbc:mysql://127.0.0.1:3306/payroll'
$env:OPENPAYROLL_DB_USER = 'your-MySQL-username'
$env:OPENPAYROLL_DB_PASSWORD = 'your-MySQL-password'
scripts\run.bat
```

Change the host, port, database name, and credentials to match your server. Do not commit real credentials. The application creates its tables in the selected database, but switching from SQLite does not migrate existing records automatically. See [database options](docs/DATABASE_OPTIONS.md) for details and backup guidance.

## Payroll rules and calculation

The `config/ph/2026` directory contains versioned SSS, PhilHealth, Pag-IBIG, and BIR rule tables with source notes. The Java application validates the rule data and passes the validated inputs to the COBOL calculation modules. Review [government source review](docs/GOVERNMENT_SOURCE_REVIEW_2026.md) for sources, assumptions, and unsupported cases.

## Documentation

- [Project status and limitations](docs/PROJECT_STATUS.md)
- [Database options and MySQL configuration](docs/DATABASE_OPTIONS.md)
- [Dependencies and installation](docs/DEPENDENCIES.md)
- [Architecture](docs/COBOL_ARCHITECTURE.md)
- [Payroll rule sources and assumptions](docs/GOVERNMENT_SOURCE_REVIEW_2026.md)
- [Contributing](CONTRIBUTING.md)
- [Security reporting](SECURITY.md)

## Repository layout

- `desktop/` — JavaFX app and Maven project
- `cobol/` — payroll calculation modules and COBOL checks
- `config/ph/` — versioned Philippine payroll rule tables
- `database/` — database schema reference
- `docs/` — setup, architecture, and project documentation
- `samples/` — sample employee data, not imported automatically
- `scripts/` — build, run, and check scripts

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
