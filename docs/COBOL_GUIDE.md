# COBOL guide (project-specific)

COBOL is a compiled language used here for fixed-decimal financial calculations. Source files use free format (`>>SOURCE FORMAT FREE`).

- `IDENTIFICATION DIVISION` names a program.
- `ENVIRONMENT DIVISION` describes external files and devices.
- `DATA DIVISION` defines file records and working/linkage data.
- `PROCEDURE DIVISION` contains executable paragraphs.
- `WORKING-STORAGE` persists during one program call; `FILE SECTION` describes records; `LINKAGE` describes arguments received from `CALL ... USING`.
- `PIC 9(9)V99` means nine integer digits and two decimal digits; `V` is an implied decimal point. `S` adds a sign. `COMP` is native binary storage; `COMP-3` is packed decimal. Do not use binary floating point for money.
- `PERFORM` runs a paragraph; `CALL` invokes a separately compiled module; `IF` and `EVALUATE` branch; `COMPUTE` evaluates decimal expressions; `MOVE` transfers values; `COPY` includes a copybook. An 88-level condition gives a field named true/false values.

`NETPAY.CBL` shows `COMPUTE take-home = gross-amount - employee-deductions ROUNDED`. The employer contribution fields are deliberately absent from its input interface. The rounding policy must be reconciled with current official rules before full calculations; this initial example rounds to cents.

Compile with the scripts or `cobc -x -free -I cobol/copybooks -o build/openpayroll-engine cobol/src/PAYROLL.CBL cobol/src/SSSCALC.CBL cobol/src/PHILHEALTH.CBL cobol/src/PAGIBIG.CBL cobol/src/TAXCALC.CBL cobol/src/NETPAY.CBL`.
