# COBOL data types and money

`PIC 9(9)V99` stores a fixed decimal with two fractional digits. `V` is implied, not a stored character. `COMP` uses binary representation; `COMP-3` uses packed decimal. Do not use binary floating point for payroll. Parse decimal-point input deliberately. `ROUNDED` applies COBOL decimal rounding at a `COMPUTE`; the final policy and rounding stage must match official payroll rules and be tested.
