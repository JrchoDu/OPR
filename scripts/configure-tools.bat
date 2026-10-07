@echo off
rem Configure the tool locations installed for this OpenPayroll PH workstation.
if not defined OPENPAYROLL_JDK_HOME if exist "%LOCALAPPDATA%\OpenPayrollPH\jdk-21.0.12.1\jdk-21.0.12.1+1\bin\java.exe" set "OPENPAYROLL_JDK_HOME=%LOCALAPPDATA%\OpenPayrollPH\jdk-21.0.12.1\jdk-21.0.12.1+1"
if not defined OPENPAYROLL_MAVEN_HOME if exist "%LOCALAPPDATA%\OpenPayrollPH\apache-maven-3.10.0\apache-maven-3.10.0\bin\mvn.cmd" set "OPENPAYROLL_MAVEN_HOME=%LOCALAPPDATA%\OpenPayrollPH\apache-maven-3.10.0\apache-maven-3.10.0"
if not defined JAVA_HOME if defined OPENPAYROLL_JDK_HOME set "JAVA_HOME=%OPENPAYROLL_JDK_HOME%"
if not defined MAVEN_HOME if defined OPENPAYROLL_MAVEN_HOME set "MAVEN_HOME=%OPENPAYROLL_MAVEN_HOME%"
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
if defined MAVEN_HOME set "PATH=%MAVEN_HOME%\bin;%PATH%"
if not defined OPENPAYROLL_MSYS2_UCRT64 if exist "C:\msys64\ucrt64\bin\cobenv.cmd" set "OPENPAYROLL_MSYS2_UCRT64=C:\msys64\ucrt64"
if defined OPENPAYROLL_MSYS2_UCRT64 if exist "%OPENPAYROLL_MSYS2_UCRT64%\bin\cobenv.cmd" call "%OPENPAYROLL_MSYS2_UCRT64%\bin\cobenv.cmd" --setenv
