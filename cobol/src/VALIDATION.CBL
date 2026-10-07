      *> PROGRAM: VALIDATION.CBL | OpenPayroll PH
      *> PURPOSE: Reject negative payroll input amounts.
       >>SOURCE FORMAT FREE
identification division.
program-id. VALIDATION.
*> PURPOSE: Reject invalid negative payroll input fields before calculation.
*> INPUT: Basic salary and variable earnings/deductions.
*> OUTPUT: Y when valid, N when any input is negative.
data division.
linkage section.
01 basic-amount pic s9(9)v99.
01 earnings-amount pic s9(9)v99.
01 deductions-amount pic s9(9)v99.
01 valid-flag pic x.
procedure division using basic-amount earnings-amount deductions-amount valid-flag.
    move "Y" to valid-flag
    if basic-amount < 0 or earnings-amount < 0 or deductions-amount < 0
        move "N" to valid-flag
    end-if
    goback.
end program VALIDATION.
