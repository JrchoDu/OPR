# GnuCOBOL installation

## Windows (primary target)

OpenPayroll PH was built with GnuCOBOL 3.2.0 from the MSYS2 UCRT64 repository. MSYS2 supplies GnuCOBOL, GCC (the C compiler used by `cobc`), and matching runtime libraries as one compatible toolchain.

1. Install MSYS2 from [msys2.org](https://www.msys2.org/).
2. Open the **MSYS2 UCRT64** terminal.
3. Update base packages with `pacman -Syu`. If the terminal closes during the core update, reopen UCRT64 and run the command again.
4. Install GnuCOBOL and dependencies with `pacman -S --needed mingw-w64-ucrt-x86_64-gnucobol`.
5. Verify `cobc --version` reports 3.2.x.

The package ID and current package metadata are listed at [MSYS2 Packages](https://packages.msys2.org/packages/mingw-w64-ucrt-x86_64-gnucobol). The build scripts call `C:\msys64\ucrt64\bin\cobenv.cmd --setenv`, which configures `PATH`, `COB_CONFIG_DIR`, `COB_COPY_DIR`, and `COB_LIBRARY_PATH`. If MSYS2 is installed elsewhere, update the hardcoded standard path in the three `.bat` scripts or call the matching `cobenv.cmd` first.

Run `scripts\build-cobol.bat` and `scripts\test-cobol.bat` from the repository root.

## Linux/macOS

Install the distribution's GnuCOBOL package and native C compiler using its package manager. GnuCOBOL translates COBOL to C and compiles it with a native C compiler. Check upstream [GnuCOBOL files and installation notes](https://sourceforge.net/projects/gnucobol/files/) for source builds and platform details. Verify both `cobc --version` and the compiler/runtime can run a small program.

The `*.sh` scripts expect `cobc` and the GnuCOBOL runtime on `PATH`.
