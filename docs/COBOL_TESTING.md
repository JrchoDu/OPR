# COBOL testing

`TEST-NETPAY.CBL` checks a decimal subtraction. `TEST-RULE-MODULES.CBL` checks bracket lookup, bounded-base shares, cap/rate arithmetic and bracket tax arithmetic using clearly synthetic test-only values (not official rules). Run `scripts/test-cobol.*` when GnuCOBOL is installed. Before production, add official sourced minimum/maximum/boundary and rounding cases, invalid configuration and frequency cases.
