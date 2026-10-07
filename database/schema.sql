PRAGMA foreign_keys = ON;

CREATE TABLE employees (
    id TEXT PRIMARY KEY,
    first_name TEXT NOT NULL,
    middle_name TEXT,
    last_name TEXT NOT NULL,
    birth_date TEXT,
    employment_status TEXT,
    position TEXT,
    department TEXT,
    date_hired TEXT,
    pay_frequency TEXT NOT NULL,
    basic_salary TEXT NOT NULL,
    tin TEXT,
    sss_number TEXT,
    philhealth_number TEXT,
    pagibig_number TEXT,
    bank_info TEXT,
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL
);

CREATE TABLE payroll_runs (
    id TEXT PRIMARY KEY,
    period_start TEXT NOT NULL,
    period_end TEXT NOT NULL,
    pay_frequency TEXT NOT NULL,
    status TEXT NOT NULL CHECK (
        status IN ('DRAFT', 'CALCULATED', 'REVIEWED', 'APPROVED', 'FINALIZED', 'CANCELLED')
    ),
    rule_version TEXT NOT NULL,
    created_at TEXT NOT NULL,
    finalized_at TEXT
);

CREATE TABLE payroll_entries (
    id TEXT PRIMARY KEY,
    payroll_run_id TEXT NOT NULL REFERENCES payroll_runs(id),
    employee_id TEXT NOT NULL REFERENCES employees(id),
    inputs_json TEXT NOT NULL,
    results_json TEXT NOT NULL
);

CREATE TABLE payroll_calculations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    calculated_at TEXT NOT NULL,
    employee_id TEXT NOT NULL,
    employee_name TEXT NOT NULL,
    position TEXT NOT NULL,
    pay_frequency TEXT NOT NULL,
    period_start TEXT NOT NULL,
    period_end TEXT NOT NULL,
    basic_salary TEXT NOT NULL,
    overtime TEXT NOT NULL,
    allowance TEXT NOT NULL,
    other_deductions TEXT NOT NULL,
    gross_pay TEXT NOT NULL,
    taxable_compensation TEXT NOT NULL,
    withholding_tax TEXT NOT NULL,
    employee_sss TEXT NOT NULL,
    employee_philhealth TEXT NOT NULL,
    employee_pagibig TEXT NOT NULL,
    total_deductions TEXT NOT NULL,
    net_pay TEXT NOT NULL,
    employer_cost TEXT NOT NULL
);

CREATE TABLE application_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

CREATE TABLE audit_logs (
    id INTEGER PRIMARY KEY,
    actor TEXT NOT NULL,
    action TEXT NOT NULL,
    entity TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    before_value TEXT,
    after_value TEXT
);
