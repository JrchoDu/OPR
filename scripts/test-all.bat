@echo off
cd /d %~dp0..
call scripts\configure-tools.bat
call scripts\test-cobol.bat || exit /b 1
mvn -Dmaven.repo.local="%USERPROFILE%\.m2\repository" -f desktop\pom.xml test
exit /b %ERRORLEVEL%
