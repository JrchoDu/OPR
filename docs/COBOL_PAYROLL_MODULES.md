# COBOL payroll modules

`PAYROLL.CBL` parses the Java-validated request, loads SSS and BIR arrays plus PhilHealth and Pag-IBIG values, invokes the calculation modules, and writes either a complete success breakdown or an error status. It refuses an unverified/incomplete rule bundle, missing bracket, unsupported frequency, or deductions larger than gross pay.

| Module | Calculation |
| --- | --- |
| `GROSSPAY.CBL` | Sum basic pay, overtime, and taxable allowance. |
| `SSSCALC.CBL` | Select the employee's SSS compensation bracket and return employee/employer contribution amounts (employer amount includes EC). |
| `PHILHEALTH.CBL` | Bound the basic salary contribution base and calculate the 5% premium split. |
| `PAGIBIG.CBL` | Cap the monthly compensation base and apply the employee rate threshold and employer rate. |
| `TAXCALC.CBL` | Select the monthly or semi-monthly Annex E bracket and calculate tax on the excess. |
| `DEDUCTIONS.CBL` | Sum employee statutory shares, tax, and post-tax other deduction. |
| `NETPAY.CBL` | Subtract employee deductions from gross pay. |
| `EMPLOYERCOST.CBL` | Add employer statutory shares to gross pay. |
| `VALIDATION.CBL` | Validate signed inputs for caller use. |

Government amounts are loaded from `config/ph/2026`; no statutory rates are hard-coded in these arithmetic modules. Synthetic tests use test-only values and do not stand in for government examples. See `GOVERNMENT_SOURCE_REVIEW_2026.md` for input assumptions and exclusions.
