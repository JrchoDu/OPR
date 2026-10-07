package ph.openpayroll.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ph.openpayroll.model.Employee;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayrollRepositoryTest {
    @TempDir
    Path temp;

    @Test
    void savesAndReadsEmployeeUsingExactDecimalSalary() throws Exception {
        Database database = new Database(temp.resolve("test.db"));
        database.initialize();
        PayrollRepository repository = new PayrollRepository(database);

        repository.saveEmployee(new Employee(
                "EMP-T1", "Ada", "Lovelace", "Analyst",
                new BigDecimal("12345.67"), "MONTHLY", true));

        Employee found = repository.listEmployees().getFirst();
        assertEquals("EMP-T1", found.id());
        assertEquals(new BigDecimal("12345.67"), found.salary());
        assertEquals(1, repository.countEmployees());
    }

    @Test
    void rejectsNegativeSalary() throws Exception {
        Database database = new Database(temp.resolve("test.db"));
        database.initialize();
        PayrollRepository repository = new PayrollRepository(database);
        Employee employee = new Employee(
                "EMP-T2", "Ada", "Lovelace", "Analyst",
                new BigDecimal("-0.01"), "MONTHLY", true);

        assertThrows(IllegalArgumentException.class, () -> repository.saveEmployee(employee));
    }

    @Test
    void recordsDraftPayrollPeriodAndRejectsReversedDates() throws Exception {
        Database database = new Database(temp.resolve("test.db"));
        database.initialize();
        PayrollRepository repository = new PayrollRepository(database);

        repository.createDraftRun("2026-10-01", "2026-10-31", "MONTHLY", "PH-2026.1");
        assertEquals(1, repository.countPayrollRuns());
        assertThrows(IllegalArgumentException.class,
                () -> repository.createDraftRun(
                        "2026-10-31", "2026-10-01", "MONTHLY", "PH-2026.1"));
    }
}
