@echo off
cd /d %~dp0..
call scripts\configure-tools.bat
if not exist build mkdir build
cobc -x -free -Wall -o build\test-netpay cobol\tests\TEST-NETPAY.CBL cobol\src\NETPAY.CBL || exit /b 1
build\test-netpay.exe
if errorlevel 1 exit /b %ERRORLEVEL%
cobc -x -free -Wall -o build\test-rule-modules cobol\tests\TEST-RULE-MODULES.CBL cobol\src\SSSCALC.CBL cobol\src\PHILHEALTH.CBL cobol\src\PAGIBIG.CBL cobol\src\TAXCALC.CBL || exit /b 1
build\test-rule-modules.exe
exit /b %ERRORLEVEL%
