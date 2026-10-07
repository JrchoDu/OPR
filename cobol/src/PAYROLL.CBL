      *> PROGRAM: PAYROLL.CBL | OpenPayroll PH
      *> PURPOSE: Load validated rule values and coordinate one payroll period.
       >>SOURCE FORMAT FREE
identification division.
program-id. PAYROLL.
environment division.
input-output section.
file-control.
    select in-file assign to dynamic in-path organization line sequential
      file status is in-status.
    select out-file assign to dynamic out-path organization line sequential
      file status is out-status.
data division.
file section.
fd in-file.
01 in-record pic x(512).
fd out-file.
01 out-record pic x(512).
working-storage section.
01 in-path pic x(512).
01 out-path pic x(512).
01 in-status pic xx.
01 out-status pic xx.
01 field-key pic x(64).
01 field-value pic x(448).
01 token-1 pic x(32).
01 token-2 pic x(32).
01 token-3 pic x(32).
01 token-4 pic x(32).
01 token-5 pic x(32).
01 token-6 pic x(32).
01 rules-verified pic x(3) value spaces.
01 pay-frequency pic x(20) value spaces.
01 input-sss-count pic 999 value zero.
01 input-tax-count pic 999 value zero.
01 sss-count pic 999 value zero.
01 tax-count pic 999 value zero.
01 row-index pic 999 value zero.
01 parse-error pic x value "N".
01 error-code pic x(40) value spaces.
01 error-message pic x(160) value spaces.
01 period-factor pic 9 value 1.
01 basic-salary pic 9(9)v99 value zero.
01 overtime-pay pic 9(9)v99 value zero.
01 taxable-allowance pic 9(9)v99 value zero.
01 other-deduction pic 9(9)v99 value zero.
01 gross-pay pic 9(9)v99 value zero.
01 monthly-sss-base pic 9(9)v99 value zero.
01 employee-sss-monthly pic 9(9)v99 value zero.
01 employer-sss-monthly pic 9(9)v99 value zero.
01 employee-sss pic 9(9)v99 value zero.
01 employer-sss pic 9(9)v99 value zero.
01 monthly-phic-base pic 9(9)v99 value zero.
01 employee-phic-monthly pic 9(9)v99 value zero.
01 employer-phic-monthly pic 9(9)v99 value zero.
01 employee-phic pic 9(9)v99 value zero.
01 employer-phic pic 9(9)v99 value zero.
01 monthly-hdmf-base pic 9(9)v99 value zero.
01 employee-hdmf-monthly pic 9(9)v99 value zero.
01 employer-hdmf-monthly pic 9(9)v99 value zero.
01 employee-hdmf pic 9(9)v99 value zero.
01 employer-hdmf pic 9(9)v99 value zero.
01 taxable-compensation pic 9(9)v99 value zero.
01 withholding-tax pic 9(9)v99 value zero.
01 zero-amount pic 9(9)v99 value zero.
01 zero-amount-two pic 9(9)v99 value zero.
01 zero-amount-three pic 9(9)v99 value zero.
01 zero-amount-four pic 9(9)v99 value zero.
01 zero-amount-five pic 9(9)v99 value zero.
01 zero-amount-six pic 9(9)v99 value zero.
01 other-amount pic 9(9)v99 value zero.
01 total-deductions pic 9(9)v99 value zero.
01 net-pay pic 9(9)v99 value zero.
01 employer-cost pic 9(9)v99 value zero.
01 table-verified pic x value "N".
01 sss-table.
   05 sss-row occurs 100 times.
      10 sss-lower pic 9(9)v99.
      10 sss-upper pic 9(9)v99.
      10 sss-employee pic 9(7)v99.
      10 sss-employer pic 9(7)v99.
01 phic-rate pic 9v999999 value zero.
01 phic-minimum pic 9(9)v99 value zero.
01 phic-maximum pic 9(9)v99 value zero.
01 phic-employee-percent pic 9v999999 value zero.
01 phic-employer-percent pic 9v999999 value zero.
01 hdmf-rate-low pic 9v999999 value zero.
01 hdmf-rate-high pic 9v999999 value zero.
01 hdmf-threshold pic 9(9)v99 value zero.
01 hdmf-employer-rate pic 9v999999 value zero.
01 hdmf-maximum pic 9(9)v99 value zero.
01 tax-table.
   05 tax-row occurs 100 times.
      10 tax-minimum pic 9(9)v99.
      10 tax-maximum pic 9(9)v99.
      10 tax-base pic 9(9)v99.
      10 tax-rate pic 9v999999.
      10 tax-excess pic 9(9)v99.
01 valid-flag pic x.
01 gross-format pic z(8)9.99.
01 employee-sss-format pic z(8)9.99.
01 employer-sss-format pic z(8)9.99.
01 employee-phic-format pic z(8)9.99.
01 employer-phic-format pic z(8)9.99.
01 employee-hdmf-format pic z(8)9.99.
01 employer-hdmf-format pic z(8)9.99.
01 taxable-format pic z(8)9.99.
01 tax-format pic z(8)9.99.
01 deductions-format pic z(8)9.99.
01 net-format pic z(8)9.99.
01 employer-cost-format pic z(8)9.99.
linkage section.
01 unused-linkage pic x.
procedure division.
main.
    accept in-path from argument-value
    accept out-path from argument-value
    open input in-file
    if in-status not = "00"
        move "INPUT_ERROR" to error-code
        move "Payroll request file could not be opened." to error-message
        perform write-error
    end-if
    perform until in-status = "10"
        read in-file
            at end continue
            not at end perform parse-input-line
        end-read
        if parse-error = "Y"
            close in-file
            move "INVALID_REQUEST" to error-code
            move "Payroll request contains invalid or incomplete data." to error-message
            perform write-error
        end-if
    end-perform
    close in-file
    if rules-verified not = "YES" or sss-count < 1 or sss-count > 100 or
       tax-count < 1 or tax-count > 100 or input-sss-count not = sss-count or
       input-tax-count not = tax-count
        move "RULES_UNVERIFIED" to error-code
        move "Government rule bundle is missing, incomplete, or unverified." to error-message
        perform write-error
    end-if
    if pay-frequency = "MONTHLY"
        move 1 to period-factor
    else
        if pay-frequency = "SEMI_MONTHLY"
            move 2 to period-factor
        else
            move "INVALID_FREQUENCY" to error-code
            move "Only MONTHLY and SEMI_MONTHLY pay frequencies are supported." to error-message
            perform write-error
        end-if
    end-if
    call "GROSSPAY" using basic-salary overtime-pay taxable-allowance zero-amount
        zero-amount-two zero-amount-three gross-pay
    move "Y" to table-verified
    compute monthly-sss-base = gross-pay * period-factor
    call "SSSCALC" using monthly-sss-base employee-sss-monthly employer-sss-monthly
        table-verified sss-count sss-table
    if table-verified not = "Y"
        move "SSS_BRACKET_MISSING" to error-code
        move "No SSS salary-credit bracket covers this monthly compensation." to error-message
        perform write-error
    end-if
    compute employee-sss rounded = employee-sss-monthly / period-factor
    compute employer-sss rounded = employer-sss-monthly / period-factor

    move "Y" to table-verified
    compute monthly-phic-base = basic-salary * period-factor
    call "PHILHEALTH" using monthly-phic-base employee-phic-monthly employer-phic-monthly
        table-verified phic-minimum phic-maximum phic-rate phic-employee-percent phic-employer-percent
    if table-verified not = "Y"
        move "PHIC_RULE_INVALID" to error-code
        move "PhilHealth contribution rule is invalid." to error-message
        perform write-error
    end-if
    compute employee-phic rounded = employee-phic-monthly / period-factor
    compute employer-phic rounded = employer-phic-monthly / period-factor

    move "Y" to table-verified
    compute monthly-hdmf-base = gross-pay * period-factor
    call "PAGIBIG" using monthly-hdmf-base employee-hdmf-monthly employer-hdmf-monthly
        table-verified hdmf-maximum hdmf-rate-low hdmf-rate-high hdmf-threshold hdmf-employer-rate
    if table-verified not = "Y"
        move "PAGIBIG_RULE_INVALID" to error-code
        move "Pag-IBIG contribution rule is invalid." to error-message
        perform write-error
    end-if
    compute employee-hdmf rounded = employee-hdmf-monthly / period-factor
    compute employer-hdmf rounded = employer-hdmf-monthly / period-factor

    if gross-pay < employee-sss + employee-phic + employee-hdmf
        move zero to taxable-compensation
    else
        compute taxable-compensation = gross-pay - employee-sss - employee-phic - employee-hdmf
    end-if
    move "Y" to table-verified
    call "TAXCALC" using taxable-compensation withholding-tax table-verified tax-count tax-table
    if table-verified not = "Y"
        move "BIR_BRACKET_MISSING" to error-code
        move "No BIR withholding bracket covers taxable compensation." to error-message
        perform write-error
    end-if
    move other-deduction to other-amount
    call "DEDUCTIONS" using employee-sss employee-phic employee-hdmf withholding-tax
        zero-amount-four zero-amount-five other-amount total-deductions
    if total-deductions > gross-pay
        move "DEDUCTIONS_EXCEED_GROSS" to error-code
        move "Employee deductions exceed gross pay." to error-message
        perform write-error
    end-if
    call "NETPAY" using gross-pay total-deductions net-pay
    call "EMPLOYERCOST" using gross-pay employer-sss employer-phic employer-hdmf employer-cost
    perform write-success
    stop run.

parse-input-line.
    move spaces to field-key field-value
    unstring function trim(in-record) delimited by "=" into field-key field-value end-unstring
    evaluate function trim(field-key)
        when "BASIC_SALARY" compute basic-salary = function numval(field-value)
        when "OVERTIME" compute overtime-pay = function numval(field-value)
        when "TAXABLE_ALLOWANCE" compute taxable-allowance = function numval(field-value)
        when "OTHER_DEDUCTION" compute other-deduction = function numval(field-value)
        when "PAY_FREQUENCY" move function trim(field-value) to pay-frequency
        when "RULES_VERIFIED" move function trim(field-value) to rules-verified
        when "SSS_COUNT" compute input-sss-count = function numval(field-value)
        when "SSS_ROW" perform parse-sss-row
        when "PHIC" perform parse-phic
        when "PAGIBIG" perform parse-pagibig
        when "TAX_COUNT" compute input-tax-count = function numval(field-value)
        when "TAX_ROW" perform parse-tax-row
        when other move "Y" to parse-error
    end-evaluate.

parse-sss-row.
    if sss-count >= 100
        move "Y" to parse-error
        exit paragraph
    end-if
    add 1 to sss-count
    move spaces to token-1 token-2 token-3 token-4
    unstring function trim(field-value) delimited by "," into token-1 token-2 token-3 token-4 end-unstring
    compute sss-lower(sss-count) = function numval(token-1)
    compute sss-upper(sss-count) = function numval(token-2)
    compute sss-employee(sss-count) = function numval(token-3)
    compute sss-employer(sss-count) = function numval(token-4).

parse-phic.
    move spaces to token-1 token-2 token-3 token-4 token-5
    unstring function trim(field-value) delimited by "," into token-1 token-2 token-3 token-4 token-5 end-unstring
    compute phic-rate = function numval(token-1)
    compute phic-minimum = function numval(token-2)
    compute phic-maximum = function numval(token-3)
    compute phic-employee-percent = function numval(token-4)
    compute phic-employer-percent = function numval(token-5).

parse-pagibig.
    move spaces to token-1 token-2 token-3 token-4 token-5
    unstring function trim(field-value) delimited by "," into token-1 token-2 token-3 token-4 token-5 end-unstring
    compute hdmf-rate-low = function numval(token-1)
    compute hdmf-rate-high = function numval(token-2)
    compute hdmf-threshold = function numval(token-3)
    compute hdmf-employer-rate = function numval(token-4)
    compute hdmf-maximum = function numval(token-5).

parse-tax-row.
    if tax-count >= 100
        move "Y" to parse-error
        exit paragraph
    end-if
    add 1 to tax-count
    move spaces to token-1 token-2 token-3 token-4 token-5
    unstring function trim(field-value) delimited by "," into token-1 token-2 token-3 token-4 token-5 end-unstring
    compute tax-minimum(tax-count) = function numval(token-1)
    compute tax-maximum(tax-count) = function numval(token-2)
    compute tax-base(tax-count) = function numval(token-3)
    compute tax-rate(tax-count) = function numval(token-4)
    compute tax-excess(tax-count) = function numval(token-5).

write-error.
    open output out-file
    if out-status = "00"
        move "STATUS=ERROR" to out-record
        write out-record
        move spaces to out-record
        string "ERROR_CODE=" function trim(error-code) into out-record end-string
        write out-record
        move spaces to out-record
        string "ERROR_MESSAGE=" function trim(error-message) into out-record end-string
        write out-record
        close out-file
    end-if
    stop run.

write-success.
    move gross-pay to gross-format
    move employee-sss to employee-sss-format
    move employer-sss to employer-sss-format
    move employee-phic to employee-phic-format
    move employer-phic to employer-phic-format
    move employee-hdmf to employee-hdmf-format
    move employer-hdmf to employer-hdmf-format
    move taxable-compensation to taxable-format
    move withholding-tax to tax-format
    move total-deductions to deductions-format
    move net-pay to net-format
    move employer-cost to employer-cost-format
    open output out-file
    if out-status = "00"
        move "STATUS=OK" to out-record write out-record
        move spaces to out-record
        string "GROSS_PAY=" function trim(gross-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "EMPLOYEE_SSS=" function trim(employee-sss-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "EMPLOYER_SSS=" function trim(employer-sss-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "EMPLOYEE_PHIC=" function trim(employee-phic-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "EMPLOYER_PHIC=" function trim(employer-phic-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "EMPLOYEE_HDMF=" function trim(employee-hdmf-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "EMPLOYER_HDMF=" function trim(employer-hdmf-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "TAXABLE_COMPENSATION=" function trim(taxable-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "WITHHOLDING_TAX=" function trim(tax-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "TOTAL_DEDUCTIONS=" function trim(deductions-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "NET_PAY=" function trim(net-format) into out-record end-string
        write out-record
        move spaces to out-record
        string "EMPLOYER_COST=" function trim(employer-cost-format) into out-record end-string
        write out-record
        close out-file
    end-if.
end program PAYROLL.
