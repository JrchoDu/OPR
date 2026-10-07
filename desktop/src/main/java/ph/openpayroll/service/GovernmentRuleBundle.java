package ph.openpayroll.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Reads and validates the sourced CSV bundle before any rule values reach COBOL. */
final class GovernmentRuleBundle {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999.99");

    private GovernmentRuleBundle() {}

    static List<String> requestFields(Path directory, String frequency) throws IOException {
        List<List<String>> sss = rows(
                directory.resolve("sss.csv"),
                List.of("verified", "lower_salary", "upper_salary", "employee_share", "employer_share"));
        List<List<String>> phic = rows(
                directory.resolve("philhealth.csv"),
                List.of("verified", "rate", "minimum_base", "maximum_base",
                        "employee_share_percent", "employer_share_percent"));
        List<List<String>> pagibig = rows(
                directory.resolve("pagibig.csv"),
                List.of("verified", "employee_rate_low", "employee_rate_high", "employee_threshold",
                        "employer_rate", "maximum_base"));
        String taxFile = frequency.equals("MONTHLY") ? "bir-monthly.csv" : "bir-semi-monthly.csv";
        List<List<String>> tax = rows(
                directory.resolve(taxFile),
                List.of("verified", "pay_frequency", "minimum", "maximum", "base_tax",
                        "percentage", "excess_over"));

        validateSss(sss);
        validatePhilhealth(phic);
        validatePagibig(pagibig);
        validateTax(tax, frequency);

        List<String> result = new ArrayList<>();
        result.add("RULES_VERIFIED=YES");
        result.add("SSS_COUNT=" + sss.size());
        for (List<String> row : sss) {
            result.add("SSS_ROW=" + String.join(",", row.subList(1, 5)));
        }
        List<String> p = phic.getFirst();
        result.add("PHIC=" + String.join(",", p.subList(1, 6)));
        List<String> h = pagibig.getFirst();
        result.add("PAGIBIG=" + String.join(",", h.subList(1, 6)));
        result.add("TAX_COUNT=" + tax.size());
        for (List<String> row : tax) {
            result.add("TAX_ROW=" + String.join(",", row.subList(2, 7)));
        }
        return result;
    }

    private static List<List<String>> rows(Path path, List<String> expectedHeader) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Missing government table: " + path.getFileName());
        }
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<List<String>> result = new ArrayList<>();
        List<String> header = null;
        for (String sourceLine : lines) {
            String line = sourceLine.strip();
            if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                line = line.substring(1).strip();
            }
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            List<String> fields = splitCsv(line);
            if (header == null) {
                if (!fields.equals(expectedHeader)) {
                    throw new IllegalStateException(
                            "Unexpected table columns in " + path.getFileName());
                }
                header = fields;
                continue;
            }
            if (fields.size() != expectedHeader.size() || !fields.getFirst().equals("YES")) {
                throw new IllegalStateException("Unverified or malformed row in " + path.getFileName());
            }
            result.add(fields);
        }
        if (header == null || result.isEmpty()) {
            throw new IllegalStateException(
                    "Government table has no verified rows: " + path.getFileName());
        }
        return result;
    }

    private static List<String> splitCsv(String line) {
        return List.of(line.split(",", -1)).stream().map(String::strip).toList();
    }

    private static void validateSss(List<List<String>> rows) {
        if (rows.size() != 61) {
            throw new IllegalStateException(
                    "SSS table must contain all 61 employee salary-credit brackets");
        }
        BigDecimal expectedLower = BigDecimal.ZERO;
        BigDecimal previousEe = BigDecimal.ZERO;
        BigDecimal previousEr = BigDecimal.ZERO;
        for (int index = 0; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            BigDecimal lower = decimal(row.get(1), "SSS lower salary");
            BigDecimal upper = decimal(row.get(2), "SSS upper salary");
            BigDecimal ee = decimal(row.get(3), "SSS employee contribution");
            BigDecimal er = decimal(row.get(4), "SSS employer contribution");
            BigDecimal msc = new BigDecimal("5000").add(new BigDecimal("500").multiply(BigDecimal.valueOf(index)));
            BigDecimal expectedEe = msc.multiply(new BigDecimal("0.05"));
            BigDecimal ec = index <= 19 ? new BigDecimal("10") : new BigDecimal("30");
            BigDecimal expectedEr = msc.multiply(new BigDecimal("0.10")).add(ec);
            BigDecimal expectedUpper = index == 0 ? new BigDecimal("5249.99")
                    : index == 60 ? MAX_AMOUNT : lower.add(new BigDecimal("499.99"));
            if (lower.compareTo(expectedLower) != 0 || upper.compareTo(lower) < 0
                    || upper.compareTo(expectedUpper) != 0 || ee.compareTo(expectedEe) != 0
                    || er.compareTo(expectedEr) != 0 || ee.compareTo(previousEe) < 0
                    || er.compareTo(previousEr) < 0) {
                throw new IllegalStateException(
                        "SSS brackets must be ordered, contiguous, and have nondecreasing shares");
            }
            expectedLower = upper.add(new BigDecimal("0.01"));
            previousEe = ee;
            previousEr = er;
        }
        if (decimal(rows.getLast().get(2), "SSS final upper salary").compareTo(MAX_AMOUNT) != 0) {
            throw new IllegalStateException("SSS table does not cover the supported salary range");
        }
    }

    private static void validatePhilhealth(List<List<String>> rows) {
        if (rows.size() != 1) {
            throw new IllegalStateException("PhilHealth table must have exactly one current schedule");
        }
        List<String> row = rows.getFirst();
        BigDecimal rate = decimal(row.get(1), "PhilHealth rate");
        BigDecimal floor = decimal(row.get(2), "PhilHealth minimum base");
        BigDecimal ceiling = decimal(row.get(3), "PhilHealth maximum base");
        BigDecimal ee = decimal(row.get(4), "PhilHealth employee split");
        BigDecimal er = decimal(row.get(5), "PhilHealth employer split");
        if (rate.compareTo(new BigDecimal("0.05")) != 0 || floor.compareTo(new BigDecimal("10000")) != 0
                || ceiling.compareTo(new BigDecimal("100000")) != 0 || ee.compareTo(new BigDecimal("0.5")) != 0
                || er.compareTo(new BigDecimal("0.5")) != 0) {
            throw new IllegalStateException("PhilHealth rate, bounds, or share split is invalid");
        }
    }

    private static void validatePagibig(List<List<String>> rows) {
        if (rows.size() != 1) {
            throw new IllegalStateException("Pag-IBIG table must have exactly one current schedule");
        }
        List<String> row = rows.getFirst();
        BigDecimal low = decimal(row.get(1), "Pag-IBIG lower employee rate");
        BigDecimal high = decimal(row.get(2), "Pag-IBIG upper employee rate");
        BigDecimal threshold = decimal(row.get(3), "Pag-IBIG employee threshold");
        BigDecimal employer = decimal(row.get(4), "Pag-IBIG employer rate");
        BigDecimal cap = decimal(row.get(5), "Pag-IBIG salary cap");
        if (low.compareTo(new BigDecimal("0.01")) != 0 || high.compareTo(new BigDecimal("0.02")) != 0
                || threshold.compareTo(new BigDecimal("1500")) != 0 || employer.compareTo(new BigDecimal("0.02")) != 0
                || cap.compareTo(new BigDecimal("10000")) != 0) {
            throw new IllegalStateException("Pag-IBIG rates, threshold, or cap is invalid");
        }
    }

    private static void validateTax(List<List<String>> rows, String frequency) {
        if (rows.size() != 6) {
            throw new IllegalStateException("BIR table must contain all six " + frequency + " brackets");
        }
        String[][] monthly = {
                {"20833.00", "0.00", "0.000000", "0.00"},
                {"33332.99", "0.00", "0.150000", "20833.00"},
                {"66666.99", "1875.00", "0.200000", "33333.00"},
                {"166666.99", "8541.80", "0.250000", "66667.00"},
                {"666666.99", "33541.80", "0.300000", "166667.00"},
                {"999999999.99", "183541.80", "0.350000", "666667.00"}
        };
        String[][] semiMonthly = {
                {"10417.00", "0.00", "0.000000", "0.00"},
                {"16666.99", "0.00", "0.150000", "10417.00"},
                {"33332.99", "937.50", "0.200000", "16667.00"},
                {"83332.99", "4270.70", "0.250000", "33333.00"},
                {"333332.99", "16770.70", "0.300000", "83333.00"},
                {"999999999.99", "91770.70", "0.350000", "333333.00"}
        };
        String[][] expected = frequency.equals("MONTHLY") ? monthly : semiMonthly;
        BigDecimal expectedLower = BigDecimal.ZERO;
        BigDecimal previousRate = BigDecimal.ZERO;
        for (int index = 0; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            if (!row.get(1).equals(frequency)) {
                throw new IllegalStateException("BIR frequency does not match payroll frequency");
            }
            BigDecimal lower = decimal(row.get(2), "BIR lower bound");
            BigDecimal upper = decimal(row.get(3), "BIR upper bound");
            BigDecimal baseTax = decimal(row.get(4), "BIR base tax");
            BigDecimal rate = decimal(row.get(5), "BIR marginal rate");
            BigDecimal excess = decimal(row.get(6), "BIR excess threshold");
            String[] expectedRow = expected[index];
            if (lower.compareTo(expectedLower) != 0 || upper.compareTo(lower) < 0 || baseTax.signum() < 0
                    || rate.compareTo(previousRate) < 0 || excess.compareTo(lower) > 0
                    || upper.compareTo(new BigDecimal(expectedRow[0])) != 0
                    || baseTax.compareTo(new BigDecimal(expectedRow[1])) != 0
                    || rate.compareTo(new BigDecimal(expectedRow[2])) != 0
                    || excess.compareTo(new BigDecimal(expectedRow[3])) != 0) {
                throw new IllegalStateException("BIR brackets must be ordered, contiguous, and valid");
            }
            expectedLower = upper.add(new BigDecimal("0.01"));
            previousRate = rate;
        }
        if (decimal(rows.getLast().get(3), "BIR final upper bound").compareTo(MAX_AMOUNT) != 0) {
            throw new IllegalStateException("BIR table does not cover the supported taxable range");
        }
    }

    private static BigDecimal decimal(String text, String label) {
        if (!text.matches("[0-9]+(?:\\.[0-9]+)?")) {
            throw new IllegalStateException(label + " is not a valid nonnegative decimal");
        }
        BigDecimal value = new BigDecimal(text);
        if (value.compareTo(MAX_AMOUNT) > 0) throw new IllegalStateException(label + " exceeds the COBOL amount limit");
        return value;
    }
}
