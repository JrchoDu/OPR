@echo off
cd /d %~dp0..
call scripts\configure-tools.bat
mvn -Dmaven.repo.local="%USERPROFILE%\.m2\repository" -f desktop\pom.xml javafx:run
exit /b %ERRORLEVEL%
