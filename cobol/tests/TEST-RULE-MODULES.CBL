       >>SOURCE FORMAT FREE
identification division.
program-id. TEST-RULE-MODULES.
*> TEST-ONLY synthetic rates below validate arithmetic, not government policy.
data division.
working-storage section.
01 verified pic x value "Y".
01 base-amount pic 9(9)v99 value 5000.00.
01 employee-amount pic 9(9)v99 value zero.
01 employer-amount pic 9(9)v99 value zero.
01 floor-amount pic 9(9)v99 value 1000.00.
01 ceiling-amount pic 9(9)v99 value 10000.00.
01 rate-value pic 9v999999 value 0.040000.
01 employee-percent pic 9v999999 value 0.500000.
01 employer-percent pic 9v999999 value 0.500000.
01 capped-base pic 9(9)v99 value 4000.00.
01 employee-rate-low pic 9v999999 value 0.010000.
01 employee-rate-high pic 9v999999 value 0.020000.
01 employee-threshold pic 9(9)v99 value 1500.00.
01 employer-rate pic 9v999999 value 0.020000.
01 salary-base pic 9(9)v99 value 50000.00.
01 bracket-count pic 999 value 1.
01 bracket-table.
   05 bracket-row occurs 100 times.
      10 bracket-lower pic 9(9)v99.
      10 bracket-upper pic 9(9)v99.
      10 bracket-employee pic 9(7)v99.
      10 bracket-employer pic 9(7)v99.
01 taxable pic 9(9)v99 value 20000.00.
01 tax pic 9(9)v99 value zero.
01 tax-count pic 999 value 2.
01 tax-table.
   05 tax-row occurs 100 times.
      10 tax-min pic 9(9)v99.
      10 tax-max pic 9(9)v99.
      10 tax-base pic 9(9)v99.
      10 tax-rate pic 9v999999.
      10 tax-excess pic 9(9)v99.
procedure division.
    move 0 to bracket-lower(1)
    move 999999999.99 to bracket-upper(1)
    move 100.00 to bracket-employee(1)
    move 200.00 to bracket-employer(1)
    call "SSSCALC" using salary-base employee-amount employer-amount verified bracket-count bracket-table
    if employee-amount not = 100.00 or employer-amount not = 200.00 display "FAIL SSS" stop run returning 1 end-if
    call "PHILHEALTH" using base-amount employee-amount employer-amount verified floor-amount ceiling-amount rate-value employee-percent employer-percent
    if employee-amount not = 100.00 or employer-amount not = 100.00 display "FAIL PHILHEALTH" stop run returning 1 end-if
    call "PAGIBIG" using base-amount employee-amount employer-amount verified capped-base employee-rate-low employee-rate-high employee-threshold employer-rate
    if employee-amount not = 80.00 or employer-amount not = 80.00 display "FAIL PAGIBIG" stop run returning 1 end-if
    move 0 to tax-min(1) move 9999.99 to tax-max(1) move 0 to tax-base(1) move 0.050000 to tax-rate(1) move 0 to tax-excess(1)
    move 10000.00 to tax-min(2) move 999999999.99 to tax-max(2) move 500.00 to tax-base(2) move 0.100000 to tax-rate(2) move 10000.00 to tax-excess(2)
    call "TAXCALC" using taxable tax verified tax-count tax-table
    if tax not = 1500.00 display "FAIL TAX" stop run returning 1 end-if
    display "PASS SYNTHETIC RULE MODULE ARITHMETIC"
    stop run.
end program TEST-RULE-MODULES.
