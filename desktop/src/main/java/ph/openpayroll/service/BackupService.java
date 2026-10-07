package ph.openpayroll.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Creates consistent SQLite backups and restores selected backup files. */
public final class BackupService {
    private static final DateTimeFormatter BACKUP_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");

    public Path backup(Path database) throws Exception {
        Path directory = Path.of("backups");
        Files.createDirectories(directory);

        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP);
        Path destination = directory.resolve("openpayroll-" + timestamp + ".db");
        int suffix = 1;

        while (Files.exists(destination)) {
            destination = directory.resolve("openpayroll-" + timestamp + "-" + suffix++ + ".db");
        }

        // VACUUM INTO creates a consistent snapshot, including committed WAL data.
        String escapedDestination = destination.toAbsolutePath().toString().replace("'", "''");
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database.toAbsolutePath());
             var statement = connection.createStatement()) {
            statement.execute("VACUUM INTO '" + escapedDestination + "'");
        }

        return destination;
    }

    public void restore(Path backup, Path destination) throws Exception {
        if (!Files.isRegularFile(backup)) {
            throw new IllegalArgumentException("Backup file not found");
        }

        Files.createDirectories(destination.toAbsolutePath().getParent());
        if (Files.exists(destination)) {
            backup(destination); // Retain a timestamped copy before replacing the database.
        }

        Files.copy(backup, destination, StandardCopyOption.REPLACE_EXISTING);
    }
}
