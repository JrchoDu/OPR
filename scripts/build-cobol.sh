#!/usr/bin/env sh
set -eu
command -v cobc >/dev/null || { echo "GnuCOBOL (cobc) is required" >&2; exit 1; }
mkdir -p build
cobc -x -free -Wall -o build/openpayroll-engine cobol/src/PAYROLL.CBL cobol/src/GROSSPAY.CBL cobol/src/SSSCALC.CBL cobol/src/PHILHEALTH.CBL cobol/src/PAGIBIG.CBL cobol/src/TAXCALC.CBL cobol/src/DEDUCTIONS.CBL cobol/src/NETPAY.CBL cobol/src/EMPLOYERCOST.CBL cobol/src/VALIDATION.CBL
