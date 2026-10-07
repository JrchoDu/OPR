      *> PROGRAM: TAXCALC.CBL | OpenPayroll PH
      *> PURPOSE: Select a verified BIR bracket and calculate tax.
       >>SOURCE FORMAT FREE
identification division.
program-id. TAXCALC.
*> PURPOSE: BIR withholding bracket calculation interface.
*> INPUT: Taxable compensation, frequency and verified brackets.
*> OUTPUT: Withholding tax. No bracket values are embedded.
data division.
working-storage section.
01 ix pic 999.
linkage section.
01 taxable-amount pic 9(9)v99.
01 withholding-tax pic 9(9)v99.
01 table-verified pic x.
01 bracket-count pic 999.
01 bracket-table.
   05 bracket-row occurs 100 times.
      10 bracket-minimum pic 9(9)v99.
      10 bracket-maximum pic 9(9)v99.
      10 bracket-base-tax pic 9(9)v99.
      10 bracket-rate pic 9v999999.
      10 bracket-excess-over pic 9(9)v99.
procedure division using taxable-amount withholding-tax table-verified bracket-count bracket-table.
    move zero to withholding-tax
    if table-verified not = "Y" or bracket-count < 1 or bracket-count > 100
        move "N" to table-verified
        goback
    end-if
    perform varying ix from 1 by 1 until ix > bracket-count
        if taxable-amount >= bracket-minimum(ix) and taxable-amount <= bracket-maximum(ix)
            compute withholding-tax rounded = bracket-base-tax(ix) +
                ((taxable-amount - bracket-excess-over(ix)) * bracket-rate(ix))
            goback
        end-if
    end-perform
    move "N" to table-verified
    goback.
end program TAXCALC.
