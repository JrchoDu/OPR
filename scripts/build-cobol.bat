@echo off
setlocal
cd /d %~dp0..
call scripts\configure-tools.bat
where cobc >nul 2>nul || (echo GnuCOBOL cobc is required on PATH.& exit /b 1)
if not exist build mkdir build
cobc -x -free -Wall -o build\openpayroll-engine cobol\src\PAYROLL.CBL cobol\src\GROSSPAY.CBL cobol\src\SSSCALC.CBL cobol\src\PHILHEALTH.CBL cobol\src\PAGIBIG.CBL cobol\src\TAXCALC.CBL cobol\src\DEDUCTIONS.CBL cobol\src\NETPAY.CBL cobol\src\EMPLOYERCOST.CBL cobol\src\VALIDATION.CBL
exit /b %ERRORLEVEL%
