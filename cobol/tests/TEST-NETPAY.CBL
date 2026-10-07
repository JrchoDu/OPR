      *> PROGRAM: TEST-NETPAY.CBL | OpenPayroll PH
      *> PURPOSE: Verify fixed-decimal NETPAY calculation.
       >>SOURCE FORMAT FREE
identification division.
program-id. TEST-NETPAY.
*> PURPOSE: Smoke test fixed-decimal NETPAY module behavior.
*> INPUT: Gross 55000.00, employee deductions 1250.50.
*> EXPECTED: Net pay 53749.50; employer contributions are not supplied.
data division.
working-storage section.
01 gross-amount pic 9(9)v99 value 55000.00.
01 deductions pic 9(9)v99 value 1250.50.
01 result-amount pic 9(9)v99 value zero.
procedure division.
    call "NETPAY" using gross-amount deductions result-amount
    if result-amount not = 53749.50
        display "FAIL NETPAY"
        stop run returning 1
    end-if
    display "PASS NETPAY"
    stop run.
end program TEST-NETPAY.
