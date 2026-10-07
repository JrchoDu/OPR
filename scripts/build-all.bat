@echo off
cd /d %~dp0..
call scripts\build-cobol.bat || exit /b 1
call scripts\build-java.bat || exit /b 1
call scripts\test-all.bat
exit /b %ERRORLEVEL%
