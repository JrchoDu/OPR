@echo off
cd /d %~dp0..
call scripts\configure-tools.bat
mvn -Dmaven.repo.local="%USERPROFILE%\.m2\repository" -f desktop\pom.xml package
exit /b %ERRORLEVEL%
