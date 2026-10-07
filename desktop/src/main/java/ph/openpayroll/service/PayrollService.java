package ph.openpayroll.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Validates the Java-to-COBOL boundary and accepts only an explicit successful result. */
public final class PayrollService {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999.99");
    private static final long ENGINE_TIMEOUT_SECONDS = 20;
    private static final List<String> RESULT_AMOUNTS = List.of("GROSS_PAY", "EMPLOYEE_SSS", "EMPLOYER_SSS",
            "EMPLOYEE_PHIC", "EMPLOYER_PHIC", "EMPLOYEE_HDMF", "EMPLOYER_HDMF", "TAXABLE_COMPENSATION",
            "WITHHOLDING_TAX", "TOTAL_DEDUCTIONS", "NET_PAY", "EMPLOYER_COST");

    private final Path executable;
    private final Path configDir;

    public PayrollService(Path executable, Path configDir) {
        this.executable = executable.toAbsolutePath().normalize();
        this.configDir = configDir.toAbsolutePath().normalize();
    }

    public String calculate(String basic, String overtime, String allowance, String other, String frequency)
            throws Exception {
        BigDecimal basicAmount = amount("basic salary", basic);
        BigDecimal overtimeAmount = amount("overtime", overtime);
        BigDecimal allowanceAmount = amount("taxable allowance", allowance);
        BigDecimal otherAmount = amount("other deduction", other);
        String payFrequency = frequency == null ? "" : frequency.trim().toUpperCase(Locale.ROOT);
        if (!payFrequency.equals("MONTHLY") && !payFrequency.equals("SEMI_MONTHLY")) {
            throw new IllegalArgumentException("Pay frequency must be MONTHLY or SEMI_MONTHLY");
        }
        if (!Files.isDirectory(configDir)) {
            throw new IllegalStateException("Government rule directory is missing: " + configDir);
        }
        List<String> ruleFields = GovernmentRuleBundle.requestFields(configDir, payFrequency);

        Path workDir = Files.createTempDirectory("openpayroll-");
        Path input = workDir.resolve("request.txt");
        Path output = workDir.resolve("result.txt");
        Path processLog = workDir.resolve("engine.log");
        try {
            String payload = "BASIC_SALARY=" + basicAmount.toPlainString() + "\n"
                    + "OVERTIME=" + overtimeAmount.toPlainString() + "\n"
                    + "TAXABLE_ALLOWANCE=" + allowanceAmount.toPlainString() + "\n"
                    + "OTHER_DEDUCTION=" + otherAmount.toPlainString() + "\n"
                    + "PAY_FREQUENCY=" + payFrequency + "\n"
                    + String.join("\n", ruleFields) + "\n";
            Files.writeString(input, payload, StandardCharsets.UTF_8);

            Path engine = executable;
            if (isWindows() && !Files.exists(engine) && Files.exists(Path.of(engine + ".exe"))) {
                engine = Path.of(engine + ".exe");
            }
            Process process = new ProcessBuilder(engine.toString(), input.toString(), output.toString())
                    .redirectErrorStream(true)
                    .redirectOutput(processLog.toFile())
                    .start();
            if (!process.waitFor(ENGINE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IllegalStateException("COBOL calculation timed out");
            }

            String log = Files.exists(processLog) ? Files.readString(processLog, StandardCharsets.UTF_8).trim() : "";
            if (process.exitValue() != 0) {
                throw new IllegalStateException("COBOL calculation failed (exit " + process.exitValue() + ")"
                        + (log.isEmpty() ? "" : ": " + log));
            }
            if (!Files.isRegularFile(output) || Files.size(output) == 0) {
                throw new IllegalStateException("COBOL engine returned no result" + (log.isEmpty() ? "" : ": " + log));
            }
            String result = Files.readString(output, StandardCharsets.UTF_8);
            Map<String, String> fields = parseResult(result);
            if (!"OK".equals(fields.get("STATUS"))) {
                String code = fields.getOrDefault("ERROR_CODE", "ENGINE_REJECTED");
                String message = fields.getOrDefault(
                        "ERROR_MESSAGE",
                        "The COBOL engine did not approve this calculation.");
                throw new IllegalStateException(code + ": " + message);
            }
            for (String key : RESULT_AMOUNTS) {
                String value = fields.get(key);
                if (value == null || !value.matches("[0-9]{1,9}\\.[0-9]{2}")) {
                    throw new IllegalStateException("COBOL engine returned a missing or invalid amount: " + key);
                }
            }
            return result;
        } finally {
            deleteQuietly(output);
            deleteQuietly(input);
            deleteQuietly(processLog);
            deleteQuietly(workDir);
        }
    }

    private static BigDecimal amount(String label, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        String normalized = text.trim();
        if (!normalized.matches("[0-9]+(?:\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException(label + " must use digits with at most two decimal places");
        }
        final BigDecimal value;
        try {
            value = new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a decimal amount", e);
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException(label + " cannot be negative");
        }
        if (value.scale() > 2 || value.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException(label
                    + " must be between 0.00 and 999,999,999.99 with at most two decimal places");
        }
        return value.setScale(2);
    }

    private static Map<String, String> parseResult(String result) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String line : result.lines().toList()) {
            int separator = line.indexOf('=');
            if (separator <= 0) {
                throw new IllegalStateException("COBOL engine returned a malformed result");
            }
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            if (fields.putIfAbsent(key, value) != null) {
                throw new IllegalStateException("COBOL engine returned a duplicate result field: " + key);
            }
        }
        return fields;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Temporary files are best-effort cleanup; calculation results never depend on them.
        }
    }
}
