package ph.openpayroll.service;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollCalculationIntegrationTest {
    private final Path repository = Path.of("..").toAbsolutePath().normalize();
    private final Path rules = repository.resolve("config/ph/2026");

    @Test
    void calculatesMonthlyPayrollUsingTheCobolEngine() throws Exception {
        PayrollService service = serviceOrSkip();
        String result = service.calculate("25000.00", "0.00", "0.00", "0.00", "MONTHLY");

        assertTrue(result.contains("EMPLOYEE_SSS=1250.00"), result);
        assertTrue(result.contains("EMPLOYER_SSS=2530.00"), result);
        assertTrue(result.contains("EMPLOYEE_PHIC=625.00"), result);
        assertTrue(result.contains("EMPLOYEE_HDMF=200.00"), result);
        assertTrue(result.contains("WITHHOLDING_TAX=313.80"), result);
        assertTrue(result.contains("NET_PAY=22611.20"), result);
        assertTrue(result.contains("EMPLOYER_COST=28355.00"), result);
    }

    @Test
    void calculatesSemiMonthlyPayrollUsingTheSemiMonthlyTaxTable() throws Exception {
        PayrollService service = serviceOrSkip();
        String result = service.calculate("12500.00", "0.00", "0.00", "0.00", "SEMI_MONTHLY");

        assertTrue(result.contains("EMPLOYEE_SSS=625.00"), result);
        assertTrue(result.contains("EMPLOYEE_PHIC=312.50"), result);
        assertTrue(result.contains("EMPLOYEE_HDMF=100.00"), result);
        assertTrue(result.contains("WITHHOLDING_TAX=156.83"), result);
        assertTrue(result.contains("NET_PAY=11305.67"), result);
    }

    private PayrollService serviceOrSkip() {
        Path executable = repository.resolve("build/openpayroll-engine");
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        if (windows && !Files.exists(executable) && Files.exists(Path.of(executable + ".exe"))) {
            executable = Path.of(executable + ".exe");
        }

        Assumptions.assumeTrue(
                Files.isRegularFile(executable),
                "Build the COBOL engine before running payroll integration tests");
        return new PayrollService(executable, rules);
    }
}
