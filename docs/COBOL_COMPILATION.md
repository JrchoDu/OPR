# COBOL compilation

Run `scripts/build-cobol.bat` on Windows or `./scripts/build-cobol.sh` on Unix. The Windows script loads the MSYS2 GnuCOBOL environment from `C:\msys64\ucrt64\bin\cobenv.cmd`; the Unix script expects `cobc` already on `PATH`. Both compile free-format sources and include every calculation module. Use `-Wall` during development; treat compiler diagnostics as review items. Output goes to `build/` and is ignored by Git. See `DEPENDENCIES.md` for toolchain versions and installation steps.
