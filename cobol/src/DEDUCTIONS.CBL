      *> PROGRAM: DEDUCTIONS.CBL | OpenPayroll PH
      *> PURPOSE: Sum employee-side deductions only.
       >>SOURCE FORMAT FREE
identification division.
program-id. DEDUCTIONS.
*> PURPOSE: Total employee-side mandatory and other deductions.
*> INPUT: Employee contributions, tax, loans, advances and other deductions.
*> OUTPUT: Total employee deductions; employer shares are not accepted.
data division.
linkage section.
01 employee-sss pic 9(9)v99.
01 employee-phic pic 9(9)v99.
01 employee-hdmf pic 9(9)v99.
01 withholding-tax pic 9(9)v99.
01 loan-amount pic 9(9)v99.
01 cash-advance pic 9(9)v99.
01 other-amount pic 9(9)v99.
01 total-deductions pic 9(9)v99.
procedure division using employee-sss employee-phic employee-hdmf withholding-tax loan-amount cash-advance other-amount total-deductions.
    compute total-deductions rounded = employee-sss + employee-phic + employee-hdmf + withholding-tax + loan-amount + cash-advance + other-amount
    goback.
end program DEDUCTIONS.
