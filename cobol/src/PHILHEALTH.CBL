      *> PROGRAM: PHILHEALTH.CBL | OpenPayroll PH
      *> PURPOSE: Bound contribution base and calculate configured shares.
       >>SOURCE FORMAT FREE
identification division.
program-id. PHILHEALTH.
*> PURPOSE: PhilHealth bounded-base contribution interface.
*> INPUT: Contribution base and verification flag.
*> OUTPUT: Employee and employer shares, returned separately.
data division.
working-storage section.
01 bounded-base pic 9(9)v99.
01 total-contribution pic 9(9)v99.
linkage section.
01 contribution-base pic 9(9)v99.
01 employee-share pic 9(9)v99.
01 employer-share pic 9(9)v99.
01 table-verified pic x.
01 minimum-base pic 9(9)v99.
01 maximum-base pic 9(9)v99.
01 contribution-rate pic 9v999999.
01 employee-percent pic 9v999999.
01 employer-percent pic 9v999999.
procedure division using contribution-base employee-share employer-share table-verified minimum-base maximum-base contribution-rate employee-percent employer-percent.
    move zero to employee-share employer-share
    if table-verified not = "Y" or maximum-base < minimum-base
        move "N" to table-verified
        goback
    end-if
    move contribution-base to bounded-base
    if bounded-base < minimum-base move minimum-base to bounded-base end-if
    if bounded-base > maximum-base move maximum-base to bounded-base end-if
    compute total-contribution rounded = bounded-base * contribution-rate
    compute employee-share rounded = total-contribution * employee-percent
    compute employer-share rounded = total-contribution * employer-percent
    goback.
end program PHILHEALTH.
