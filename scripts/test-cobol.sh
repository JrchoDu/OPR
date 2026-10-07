#!/usr/bin/env sh
set -eu
mkdir -p build
cobc -x -free -Wall -o build/test-netpay cobol/tests/TEST-NETPAY.CBL cobol/src/NETPAY.CBL
./build/test-netpay
cobc -x -free -Wall -o build/test-rule-modules cobol/tests/TEST-RULE-MODULES.CBL cobol/src/SSSCALC.CBL cobol/src/PHILHEALTH.CBL cobol/src/PAGIBIG.CBL cobol/src/TAXCALC.CBL
./build/test-rule-modules
