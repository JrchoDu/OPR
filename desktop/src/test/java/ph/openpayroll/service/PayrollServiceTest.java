package ph.openpayroll.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollServiceTest {
    @TempDir
    Path temp;

    @Test
    void rejectsNegativeInputsBeforeLaunchingEngine() {
        PayrollService service = new PayrollService(Path.of("missing-engine"), temp);

        assertThrows(IllegalArgumentException.class,
                () -> service.calculate("-1.00", "0", "0", "0", "MONTHLY"));
    }

    @Test
    void rejectsMalformedOrOverPrecisionAmountsBeforeLaunchingEngine() {
        PayrollService service = new PayrollService(Path.of("missing-engine"), temp);

        assertThrows(IllegalArgumentException.class,
                () -> service.calculate("12x", "0", "0", "0", "MONTHLY"));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate("1.001", "0", "0", "0", "MONTHLY"));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate("1e3", "0", "0", "0", "MONTHLY"));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate("1000000000.00", "0", "0", "0", "MONTHLY"));
    }

    @Test
    void rejectsUnsupportedFrequencyBeforeLaunchingEngine() {
        PayrollService service = new PayrollService(Path.of("missing-engine"), temp);

        assertThrows(IllegalArgumentException.class,
                () -> service.calculate("1", "0", "0", "0", "WEEKLY"));
    }

    @Test
    void reportsMissingRuleBundleBeforeLaunchingEngine() {
        Path missing = temp.resolve("missing");
        PayrollService service = new PayrollService(Path.of("missing-engine"), missing);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.calculate("1", "0", "0", "0", "MONTHLY"));
        assertTrue(error.getMessage().contains("Government rule directory is missing"));
    }

    @Test
    void reportsEngineLaunchFailure() throws IOException {
        Path config = copyRuleBundle("config");
        PayrollService service = new PayrollService(Path.of("missing-engine"), config);

        assertThrows(IOException.class,
                () -> service.calculate("1", "0", "0", "0", "MONTHLY"));
    }

    @Test
    void refusesUnverifiedRuleRowsBeforeLaunchingEngine() throws IOException {
        Path config = copyRuleBundle("unverified");
        Path sss = config.resolve("sss.csv");
        String contents = Files.readString(sss, StandardCharsets.UTF_8)
                .replaceFirst("YES,0\\.00,5249\\.99", "NO,0.00,5249.99");
        Files.writeString(sss, contents, StandardCharsets.UTF_8);

        PayrollService service = new PayrollService(Path.of("missing-engine"), config);
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.calculate("1", "0", "0", "0", "MONTHLY"));
        assertTrue(error.getMessage().contains("Unverified or malformed row"));
    }

    private Path copyRuleBundle(String folder) throws IOException {
        Path destination = Files.createDirectory(temp.resolve(folder));
        List<String> tables = List.of(
                "sss.csv",
                "philhealth.csv",
                "pagibig.csv",
                "bir-monthly.csv",
                "bir-semi-monthly.csv");

        for (String table : tables) {
            Files.copy(Path.of("..", "config", "ph", "2026", table), destination.resolve(table));
        }
        return destination;
    }
}
