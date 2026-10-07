      *> PROGRAM: SSSCALC.CBL | OpenPayroll PH
      *> PURPOSE: Select verified SSS salary-credit bracket shares.
       >>SOURCE FORMAT FREE
identification division.
program-id. SSSCALC.
*> ============================================================
*> PURPOSE: SSS salary-credit bracket calculation interface.
*> INPUT: Salary base and table verification flag.
*> OUTPUT: Employee share and employer share.
*> CONFIG: Effective-dated official SSS bracket table is required.
*> ============================================================
data division.
working-storage section.
01 ix pic 999.
linkage section.
01 salary-base pic 9(9)v99.
01 employee-share pic 9(9)v99.
01 employer-share pic 9(9)v99.
01 table-verified pic x.
01 bracket-count pic 999.
01 bracket-table.
   05 bracket-row occurs 100 times.
      10 bracket-lower pic 9(9)v99.
      10 bracket-upper pic 9(9)v99.
      10 bracket-employee pic 9(7)v99.
      10 bracket-employer pic 9(7)v99.
procedure division using salary-base employee-share employer-share table-verified bracket-count bracket-table.
    move zero to employee-share employer-share
    if table-verified not = "Y" or bracket-count < 1 or bracket-count > 100
        move "N" to table-verified
        goback
    end-if
    perform varying ix from 1 by 1 until ix > bracket-count
        if salary-base >= bracket-lower(ix) and salary-base <= bracket-upper(ix)
            move bracket-employee(ix) to employee-share
            move bracket-employer(ix) to employer-share
            goback
        end-if
    end-perform
    move "N" to table-verified
    goback.
end program SSSCALC.
