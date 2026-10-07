      *> PROGRAM: EMPLOYERCOST.CBL | OpenPayroll PH
      *> PURPOSE: Add employer contribution shares to gross pay.
       >>SOURCE FORMAT FREE
identification division.
program-id. EMPLOYERCOST.
*> PURPOSE: Calculate employer total cost without reducing employee net pay.
*> INPUT: Gross pay and employer SSS, PhilHealth and Pag-IBIG portions.
*> OUTPUT: Employer payroll cost.
data division.
linkage section.
01 gross-amount pic 9(9)v99.
01 employer-sss pic 9(9)v99.
01 employer-phic pic 9(9)v99.
01 employer-hdmf pic 9(9)v99.
01 total-employer-cost pic 9(9)v99.
procedure division using gross-amount employer-sss employer-phic employer-hdmf total-employer-cost.
    compute total-employer-cost rounded = gross-amount + employer-sss + employer-phic + employer-hdmf
    goback.
end program EMPLOYERCOST.
