      *> PROGRAM: NETPAY.CBL | OpenPayroll PH
      *> PURPOSE: Calculate net pay excluding employer contributions.
       >>SOURCE FORMAT FREE
identification division.
program-id. NETPAY.
*> PURPOSE: Calculate employee take-home pay without employer shares.
*> INPUT: Gross pay and employee deductions only.
*> OUTPUT: Net pay rounded to two decimal places.
data division.
linkage section.
01 gross-amount pic 9(9)v99.
01 employee-deductions pic 9(9)v99.
01 take-home pic 9(9)v99.
procedure division using gross-amount employee-deductions take-home.
    compute take-home rounded = gross-amount - employee-deductions
    goback.
end program NETPAY.
