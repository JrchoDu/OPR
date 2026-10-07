# Java to COBOL integration

`PayrollService` validates monetary input and frequency, asks `GovernmentRuleBundle` to verify the source CSV tables, writes the earnings and table values to an OS temporary request file, and invokes the COBOL executable with request/result paths. It applies a timeout, checks the process exit code, requires `STATUS=OK` plus all twelve monetary result fields, then removes temporary files. Amounts and government identifiers are not command-line arguments; no employee identity or government identifier is sent to COBOL.

The COBOL coordinator reads the request and calls the payroll modules. Java handles data loading and validation; COBOL owns the arithmetic. If tables are changed or incomplete, the Java loader refuses to start the engine.
