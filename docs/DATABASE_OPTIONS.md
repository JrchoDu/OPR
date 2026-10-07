# Database options

OpenPayroll PH supports two database modes:

- **SQLite (default):** local file at `data/openpayroll.db`.
- **MySQL (optional):** connect to a running MySQL server using JDBC.

phpMyAdmin is a browser-based administration tool. It lets you create and manage a MySQL database, but the desktop application connects to the MySQL server itself, not to the phpMyAdmin web page.

## Connect to MySQL

1. Start your MySQL server.
2. In phpMyAdmin, create an empty database such as `openpayroll` and a dedicated application user. Grant that user permission to create tables and to select, insert, update, and delete rows in that database. Do not use the MySQL `root` account in the payroll application.
3. In the PowerShell terminal that will launch OpenPayroll, set the JDBC URL and credentials. For a MySQL server on the same computer with the default port, use:

   ```powershell
   $env:OPENPAYROLL_DB_URL = 'jdbc:mysql://127.0.0.1:3306/openpayroll'
   $env:OPENPAYROLL_DB_USER = 'openpayroll_app'
   $env:OPENPAYROLL_DB_PASSWORD = 'your-local-password'
   scripts\run.bat
   ```

   Replace the host, port, database name, username, and password with the values for your server. Keep the password in the local terminal environment; do not commit it to the repository or paste it into source code.

4. On startup the app creates its required tables in the selected MySQL database. Check phpMyAdmin for tables such as `employees`, `payroll_calculations`, and `application_settings`.

When `OPENPAYROLL_DB_URL` is unset, the app continues to use SQLite. MySQL is not a drop-in file replacement: the SQLite database at `data/openpayroll.db` is not copied or migrated automatically. The MySQL database starts with its own data. Back up the SQLite file before switching, and plan a separate migration if you need to transfer existing employees or history.

The Settings screen disables SQLite file backup/restore while connected to MySQL. Use phpMyAdmin's Export/Import features or MySQL's backup tools for the server database.

## Other options

| Database | Best fit | Current status |
| --- | --- | --- |
| SQLite | One desktop installation with local storage and modest write concurrency | Supported and used by default. SQLite permits many readers but one writer at a time. |
| MySQL | Desktop clients connecting to a MySQL server managed by a team or hosting provider | Supported through JDBC settings above. |
| PostgreSQL | Multi-computer or multi-user deployments that need a shared server database | Not yet integrated; requires a driver, schema/SQL changes, and verification. |
| MariaDB | Server-based deployment where MariaDB is the chosen platform | Not yet tested or officially supported by this MySQL integration. |
| Microsoft SQL Server | Organizations already operating Microsoft's database platform | Not yet integrated. |

## Choosing a database

Keep SQLite for a single computer or a small local payroll desk. Use MySQL when you already have a MySQL server and need the database managed through phpMyAdmin. For multiple workstations, make sure the application can securely reach the server; do not put the SQLite database file on a shared network drive.

Database durability still needs backups. For SQLite, use Settings to create a timestamped backup and copy it to separate protected storage. For MySQL, schedule server backups or export the database from phpMyAdmin and store the export separately from the server.

## References

- [SQLite: Appropriate Uses For SQLite](https://www.sqlite.org/whentouse.html)
- [MySQL Connector/J Maven installation](https://dev.mysql.com/doc/connector-j/en/connector-j-installing-maven.html)
- [MySQL Connector/J documentation](https://dev.mysql.com/doc/connector-j/en/)
- [phpMyAdmin documentation](https://docs.phpmyadmin.net/en/latest/)
- [PostgreSQL documentation](https://www.postgresql.org/docs/current/)
