package ph.openpayroll.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GovernmentRuleBundleTest {
    @TempDir
    Path temp;

    @Test
    void loadsAndValidatesMonthlyAndSemiMonthlySources() throws IOException {
        Path config = sourceConfig();
        List<String> monthly = GovernmentRuleBundle.requestFields(config, "MONTHLY");
        List<String> semiMonthly = GovernmentRuleBundle.requestFields(config, "SEMI_MONTHLY");

        assertEquals("RULES_VERIFIED=YES", monthly.getFirst());
        assertTrue(monthly.contains("SSS_COUNT=61"));
        assertTrue(monthly.contains("TAX_COUNT=6"));
        assertTrue(monthly.contains("TAX_ROW=666667.00,999999999.99,183541.80,0.350000,666667.00"));
        assertTrue(semiMonthly.contains("TAX_ROW=333333.00,999999999.99,91770.70,0.350000,333333.00"));
    }

    @Test
    void rejectsAChangedSssBracket() throws IOException {
        Path config = copyBundle();
        Path table = config.resolve("sss.csv");
        String source = Files.readString(table)
                .replace("YES,5250.00,5749.99", "YES,5251.00,5749.99");
        Files.writeString(table, source);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> GovernmentRuleBundle.requestFields(config, "MONTHLY"));
        assertTrue(error.getMessage().contains("SSS brackets"));
    }

    @Test
    void rejectsIncompleteBirTable() throws IOException {
        Path config = copyBundle();
        Path table = config.resolve("bir-monthly.csv");
        List<String> lines = Files.readAllLines(table);
        Files.write(table, lines.subList(0, lines.size() - 1));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> GovernmentRuleBundle.requestFields(config, "MONTHLY"));
        assertTrue(error.getMessage().contains("six MONTHLY brackets"));
    }

    private Path copyBundle() throws IOException {
        Path config = Files.createDirectory(temp.resolve("rules"));
        List<String> tables = List.of(
                "sss.csv",
                "philhealth.csv",
                "pagibig.csv",
                "bir-monthly.csv",
                "bir-semi-monthly.csv");

        for (String table : tables) {
            Files.copy(sourceConfig().resolve(table), config.resolve(table));
        }
        return config;
    }

    private Path sourceConfig() {
        return Path.of("..", "config", "ph", "2026").toAbsolutePath().normalize();
    }
}
