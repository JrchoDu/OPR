# OpenPayroll PH dependencies

This project uses native Windows desktop tooling. Versions below are the versions installed and build-verified in the primary Windows environment on 2026-10-07. Installed tool archives live in the current user's Local AppData and are not part of the Git repository.

## Required developer tools

| Tool | Installed version | Purpose | Install location |
| --- | --- | --- | --- |
| Microsoft Build of OpenJDK | 21.0.12.1 LTS | Compile/run Java 21 app | `%LOCALAPPDATA%\OpenPayrollPH\jdk-21.0.12.1\jdk-21.0.12.1+1` |
| Apache Maven | 3.10.0 | Java dependency resolution, compile, tests, packaging | `%LOCALAPPDATA%\OpenPayrollPH\apache-maven-3.10.0\apache-maven-3.10.0` |
| MSYS2 UCRT64 toolchain | GCC 16.2.0, GnuCOBOL 3.2.0 | Compile and run COBOL engine | `C:\msys64\ucrt64` |
| Git | existing | Source control | Installed separately by the developer environment |

The repository's Windows scripts load the default install locations from `scripts/configure-tools.bat`, so they still work if the current IDE terminal has an old or restricted `Path`. To use the commands directly in a terminal, add the JDK/Maven `bin` paths and the MSYS2 UCRT64 `bin` path to user `Path`, and set `JAVA_HOME`/`MAVEN_HOME` to the directories above. Open a new terminal after changing user environment variables.

The dependency setup process attempted to persist `JAVA_HOME` and `MAVEN_HOME`; the sandbox's elevated process did not expose those values to the normal IDE shell. The project wrappers therefore configure them per process from these locations.

Expected environment variables when directly configured:

- `JAVA_HOME` points to the JDK directory above.
- `MAVEN_HOME` points to the Maven directory above.
- User `Path` includes both `bin` directories and `C:\msys64\ucrt64\bin`.

The GnuCOBOL MSYS2 package also needs `COB_CONFIG_DIR`, `COB_LIBRARY_PATH`, `COB_COPY_DIR`, and its UCRT64 DLLs. Project `.bat` scripts call `C:\msys64\ucrt64\bin\cobenv.cmd --setenv` to establish those values for each build/run process. Open a new terminal after installing or changing user environment variables.

## Install or reproduce on Windows

### Java 21

Preferred installer:

```powershell
winget install --id Microsoft.OpenJDK.21 --exact --source winget
```

If the MSI requests administrator approval, use the Microsoft JDK ZIP package instead: download the Windows x64/ARM64 ZIP from [Microsoft's JDK 21 downloads](https://learn.microsoft.com/java/openjdk/download), extract it into a user-owned tools directory, set user `JAVA_HOME` to the extracted JDK root, and add its `bin` directory to user `Path`. The installed environment uses this archive method because the MSI requires elevation.

Verify:

```powershell
java -version
javac -version
```

### Apache Maven

Maven requires a JDK. Download the binary ZIP from [Apache Maven installation](https://maven.apache.org/install.html), extract it into a user-owned tools directory, set `MAVEN_HOME` to the extracted directory, and add `%MAVEN_HOME%\bin` to user `Path`.

Installed version: Apache Maven 3.10.0.

Verify:

```powershell
mvn --version
```

### GnuCOBOL and C compiler

For Windows, the reproducible route is MSYS2's UCRT64 environment. Install [MSYS2](https://www.msys2.org/), open the **MSYS2 UCRT64** shell, update its base packages, then install GnuCOBOL:

```bash
pacman -Syu
# If MSYS2 closes its shell during the core update, reopen UCRT64 and continue.
pacman -S --needed mingw-w64-ucrt-x86_64-gnucobol
```

The GnuCOBOL package pulls the compatible GCC compiler, runtime libraries, and GnuCOBOL dependencies. The package used here is GnuCOBOL 3.2.0 from the MSYS2 UCRT64 repository. For the package metadata and checksum, see [MSYS2 GnuCOBOL package](https://packages.msys2.org/packages/mingw-w64-ucrt-x86_64-gnucobol).

The project scripts recognize the standard MSYS2 path `C:\msys64\ucrt64`. For another MSYS2 install location, define `OPENPAYROLL_MSYS2_UCRT64` or set the GnuCOBOL environment with that installation's `cobenv.cmd` before invoking the scripts.

Verify:

```powershell
cobc --version
scripts\build-cobol.bat
scripts\test-cobol.bat
```

## Java project libraries

Maven downloads these declared dependencies into `%USERPROFILE%\.m2\repository` during the first build. The Windows build scripts explicitly point Maven at this repository. No separate installer is required.

| Artifact | Version | Use |
| --- | --- | --- |
| OpenJFX `javafx-controls` | 21.0.5 | Native desktop controls and JavaFX runtime artifacts |
| Xerial `sqlite-jdbc` | 3.47.1.0 | SQLite database driver |
| MySQL Connector/J `mysql-connector-j` | 9.7.0 | Optional MySQL database driver |
| MySQL Connector/J `mysql-connector-j` | 9.7.0 | Optional MySQL database driver |
| JUnit Jupiter | 5.11.4 | Java unit tests |
| JavaFX Maven plugin | 0.0.8 | Run the desktop app with `mvn javafx:run` |
| Maven compiler plugin | 3.13.0 | Compile with Java release 21 |
| Maven Surefire plugin | 3.5.2 | Run JUnit tests |

Maven Central must be reachable on the first build. Run `scripts/build-all.bat`; Maven caches successful downloads locally for subsequent offline builds.

## Installed and verified

The following completed successfully in the current environment:

- `java -version`: Microsoft OpenJDK 21.0.12.1 LTS.
- `mvn --version`: Apache Maven 3.10.0, using the installed Java 21 runtime.
- `cobc --version`: GnuCOBOL 3.2.0 with MSYS2/MinGW.
- `scripts/build-cobol.bat`: COBOL engine compiled.
- `scripts/test-cobol.bat`: `PASS NETPAY` and `PASS SYNTHETIC RULE MODULE ARITHMETIC`.
- `scripts/build-all.bat`: Java package built; four Java tests passed.

This verifies development dependencies and builds. Payroll estimates are available for the narrow PH-2026.1 scope documented in `GOVERNMENT_SOURCE_REVIEW_2026.md`; they are not a complete or unattended production payroll workflow.
