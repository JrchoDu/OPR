# Copybooks

A COBOL copybook is reusable source included with `COPY`. This first increment declares module layouts in each program's `LINKAGE SECTION` and does not yet need a copybook. As shared layouts stabilize, move those definitions into copybooks so caller and callee use the same field order and decimal precision.
