      *> PROGRAM: PAGIBIG.CBL | OpenPayroll PH
      *> PURPOSE: Cap contribution base and calculate separate shares.
       >>SOURCE FORMAT FREE
identification division.
program-id. PAGIBIG.
*> PURPOSE: Pag-IBIG contribution interface with separately returned shares.
*> INPUT: Contribution base and verification flag.
*> OUTPUT: Employee and employer contribution amounts.
data division.
working-storage section.
01 bounded-base pic 9(9)v99.
01 selected-rate pic 9v999999.
linkage section.
01 contribution-base pic 9(9)v99.
01 employee-share pic 9(9)v99.
01 employer-share pic 9(9)v99.
01 table-verified pic x.
01 maximum-base pic 9(9)v99.
01 employee-rate-low pic 9v999999.
01 employee-rate-high pic 9v999999.
01 employee-threshold pic 9(9)v99.
01 employer-rate pic 9v999999.
procedure division using contribution-base employee-share employer-share table-verified maximum-base employee-rate-low employee-rate-high employee-threshold employer-rate.
    move zero to employee-share employer-share
    if table-verified not = "Y" or maximum-base <= 0
        move "N" to table-verified
        goback
    end-if
    move contribution-base to bounded-base
    if bounded-base > maximum-base move maximum-base to bounded-base end-if
    if contribution-base <= employee-threshold
        move employee-rate-low to selected-rate
    else
        move employee-rate-high to selected-rate
    end-if
    compute employee-share rounded = bounded-base * selected-rate
    compute employer-share rounded = bounded-base * employer-rate
    goback.
end program PAGIBIG.
