      *> PROGRAM: GROSSPAY.CBL | OpenPayroll PH
      *> PURPOSE: Sum earnings using fixed-decimal arithmetic.
       >>SOURCE FORMAT FREE
identification division.
program-id. GROSSPAY.
*> PURPOSE: Sum basic salary and configured earnings using fixed decimal.
*> INPUT: Basic, overtime, taxable/non-taxable allowances, bonus, commission.
*> OUTPUT: Gross pay. Caller determines taxable portion from verified rules.
data division.
linkage section.
01 basic-amount pic 9(9)v99.
01 overtime-amount pic 9(9)v99.
01 taxable-allowance pic 9(9)v99.
01 other-allowance pic 9(9)v99.
01 bonus-amount pic 9(9)v99.
01 commission-amount pic 9(9)v99.
01 gross-amount pic 9(9)v99.
procedure division using basic-amount overtime-amount taxable-allowance other-allowance bonus-amount commission-amount gross-amount.
    compute gross-amount rounded = basic-amount + overtime-amount + taxable-allowance + other-allowance + bonus-amount + commission-amount
    goback.
end program GROSSPAY.
