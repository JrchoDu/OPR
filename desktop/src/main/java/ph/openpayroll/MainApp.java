package ph.openpayroll;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ph.openpayroll.data.Database;
import ph.openpayroll.data.PayrollHistoryRepository;
import ph.openpayroll.data.PayrollRepository;
import ph.openpayroll.model.Employee;
import ph.openpayroll.model.PayrollCalculation;
import ph.openpayroll.service.BackupService;
import ph.openpayroll.service.PayrollService;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Main window and navigation for the OpenPayroll PH desktop application. */
public class MainApp extends Application {
    private Database database;
    private PayrollRepository repository;
    private PayrollHistoryRepository historyRepository;
    private Stage stage;
    private Path appRoot;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        appRoot = findApplicationRoot();

        try {
            database = new Database(appRoot.resolve("data/openpayroll.db"));
            database.initialize();
            repository = new PayrollRepository(database);
            historyRepository = new PayrollHistoryRepository(database);
        } catch (Exception exception) {
            showError("Database could not be opened. Check the application folder and logs.", exception);
            return;
        }

        BorderPane root = new BorderPane();
        root.setTop(topBar());
        root.setLeft(navigation());
        root.setCenter(dashboard());
        root.setBottom(watermark());
        root.setStyle("-fx-background-color:#f4f6f8;-fx-font-family:'Segoe UI';");

        stage.setTitle("OpenPayroll PH");
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        stage.setScene(new Scene(root, 1180, 760));
        stage.show();
    }

    private Path findApplicationRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        for (Path candidate = current; candidate != null; candidate = candidate.getParent()) {
            if (Files.isDirectory(candidate.resolve("config/ph/2026"))
                    && Files.isDirectory(candidate.resolve("desktop"))) {
                return candidate;
            }
        }
        return current;
    }

    private HBox watermark() {
        Label mark = new Label("@JrchoDu");
        mark.setStyle("-fx-font-size:11px;-fx-text-fill:#829ab1;");

        HBox footer = new HBox(mark);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(0, 16, 8, 0));
        return footer;
    }

    private HBox topBar() {
        Label brand = new Label("OpenPayroll PH");
        brand.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#102a43;");

        Label subtitle = new Label("Open-Source Philippine Payroll Management System");
        subtitle.setStyle("-fx-text-fill:#627d98;");

        VBox titles = new VBox(3, brand, subtitle);
        HBox bar = new HBox(titles);
        bar.setPadding(new Insets(18, 24, 16, 24));
        bar.setStyle("-fx-background-color:white;-fx-border-color:#d9e2ec;-fx-border-width:0 0 1 0;");
        return bar;
    }

    private VBox navigation() {
        VBox navigation = new VBox(8);
        navigation.setPadding(new Insets(20, 12, 20, 12));
        navigation.setPrefWidth(190);
        navigation.setStyle("-fx-background-color:#102a43;");

        for (String name : List.of(
                "Dashboard", "Employees", "Payroll", "Payroll History", "Reports", "Settings")) {
            Button button = new Button(name);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setStyle("-fx-background-color:transparent;-fx-text-fill:white;"
                    + "-fx-alignment:center-left;-fx-padding:12;");
            button.setOnAction(event -> showPage(name));
            navigation.getChildren().add(button);
        }

        return navigation;
    }

    private void showPage(String pageName) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        root.setCenter(switch (pageName) {
            case "Employees" -> employees();
            case "Payroll" -> payroll();
            case "Payroll History" -> payrollHistory();
            case "Settings" -> settings();
            case "Reports" -> page("Reports", "Payroll summaries and CSV/PDF export are planned.");
            default -> dashboard();
        });
    }

    private VBox page(String title, String description) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:#102a43;");

        Label details = new Label(description);
        details.setWrapText(true);
        details.setMaxWidth(900);
        details.setStyle("-fx-text-fill:#627d98;");

        VBox box = new VBox(10, heading, details);
        box.setPadding(new Insets(28));
        return box;
    }

    private VBox dashboard() {
        VBox box = page("Dashboard", "Local payroll workspace · PHP");
        GridPane cards = new GridPane();
        cards.setHgap(12);
        cards.setVgap(12);

        String[] names = {"Active employees", "Payroll runs", "Rule version", "Payroll status"};
        String[] values = {
                Long.toString(countEmployees()),
                Long.toString(countRuns()),
                "PH-2026.1",
                "LIMITED CALCULATION READY"
        };

        for (int index = 0; index < names.length; index++) {
            VBox card = new VBox(10, new Label(names[index]), new Label(values[index]));
            card.setPadding(new Insets(18));
            card.setStyle("-fx-background-color:white;-fx-background-radius:6;");
            cards.add(card, index % 2, index / 2);
            GridPane.setHgrow(card, Priority.ALWAYS);
        }

        Label warning = new Label("Payroll estimates are available for supported private-sector cases. "
                + "Special tax and contribution cases are not modeled; review docs/PROJECT_STATUS.md "
                + "before using results.");
        warning.setWrapText(true);
        warning.setMaxWidth(900);
        warning.setStyle("-fx-text-fill:#8a4b08;-fx-background-color:#fff4d6;-fx-padding:12;");
        box.getChildren().addAll(cards, warning);
        return box;
    }

    private long countEmployees() {
        try {
            return repository.countEmployees();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private long countRuns() {
        try {
            return repository.countPayrollRuns();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private VBox employees() {
        VBox box = page("Employees", "Create and maintain employee salary records. "
                + "Government identifiers are not shown here.");
        TableView<Employee> table = new TableView<>();

        TableColumn<Employee, String> id = new TableColumn<>("Employee ID");
        id.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().id()));

        TableColumn<Employee, String> name = new TableColumn<>("Employee");
        name.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().fullName()));

        TableColumn<Employee, String> role = new TableColumn<>("Position");
        role.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().position()));

        TableColumn<Employee, String> salary = new TableColumn<>("Basic salary");
        salary.setCellValueFactory(cell -> new SimpleStringProperty(
                "₱" + cell.getValue().salary().toPlainString()));

        TableColumn<Employee, String> active = new TableColumn<>("Status");
        active.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().active() ? "Active" : "Archived"));

        table.getColumns().addAll(id, name, role, salary, active);
        refreshEmployees(table);

        Button add = new Button("Add employee");
        add.setOnAction(event -> employeeDialog(table, null));

        Button edit = new Button("Edit selected");
        edit.setDisable(true);
        edit.setOnAction(event -> employeeDialog(table, table.getSelectionModel().getSelectedItem()));

        Button delete = new Button("Delete selected");
        delete.setDisable(true);
        delete.setOnAction(event -> deleteEmployee(table));

        table.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> {
            edit.setDisable(selected == null);
            delete.setDisable(selected == null);
        });

        HBox actions = new HBox(8, add, edit, delete);
        actions.setPadding(new Insets(8, 0, 8, 0));
        VBox.setVgrow(table, Priority.ALWAYS);
        box.getChildren().addAll(actions, table);
        return box;
    }

    private void refreshEmployees(TableView<Employee> table) {
        try {
            table.setItems(FXCollections.observableArrayList(repository.listEmployees()));
        } catch (Exception exception) {
            showError("Employees could not be loaded.", exception);
        }
    }

    private void employeeDialog(TableView<Employee> table, Employee existing) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add employee" : "Edit employee");
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        TextField id = new TextField(existing == null ? "" : existing.id());
        id.setDisable(existing != null);
        TextField firstName = new TextField(existing == null ? "" : existing.firstName());
        TextField lastName = new TextField(existing == null ? "" : existing.lastName());
        TextField position = new TextField(existing == null ? "" : existing.position());
        TextField salary = new TextField(existing == null ? "" : existing.salary().toPlainString());
        ComboBox<String> frequency = new ComboBox<>(
                FXCollections.observableArrayList("MONTHLY", "SEMI_MONTHLY"));
        frequency.setValue(existing == null ? "MONTHLY" : existing.frequency());
        CheckBox active = new CheckBox("Employee is active");
        active.setSelected(existing == null || existing.active());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(16));
        addFormRow(form, 0, "Employee ID", id);
        addFormRow(form, 1, "First name", firstName);
        addFormRow(form, 2, "Last name", lastName);
        addFormRow(form, 3, "Position", position);
        addFormRow(form, 4, "Basic salary (PHP)", salary);
        addFormRow(form, 5, "Pay frequency", frequency);
        addFormRow(form, 6, "Status", active);
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(button -> {
            if (button != save) {
                return null;
            }
            try {
                BigDecimal amount = new BigDecimal(salary.getText().trim());
                if (amount.signum() < 0) {
                    throw new IllegalArgumentException("Salary cannot be negative");
                }

                Employee employee = new Employee(
                        id.getText().trim(),
                        firstName.getText().trim(),
                        lastName.getText().trim(),
                        position.getText().trim(),
                        amount,
                        frequency.getValue(),
                        active.isSelected());
                repository.saveEmployee(employee);
                refreshEmployees(table);
                return employee;
            } catch (Exception exception) {
                showError("Employee could not be saved. Check required fields and enter a non-negative salary.",
                        exception);
                return null;
            }
        });
        dialog.showAndWait();
    }

    private void deleteEmployee(TableView<Employee> table) {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete " + selected.fullName() + "? This cannot be undone.",
                ButtonType.OK,
                ButtonType.CANCEL);
        confirmation.setHeaderText("Delete employee");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            repository.deleteEmployee(selected.id());
            refreshEmployees(table);
        } catch (Exception exception) {
            showError(exception.getMessage(), exception);
        }
    }

    private void addFormRow(GridPane form, int row, String label, javafx.scene.Node field) {
        Label labelControl = new Label(label);
        labelControl.setStyle("-fx-text-fill:#334e68;");
        form.add(labelControl, 0, row);
        form.add(field, 1, row);
    }

    private Node payroll() {
        String description = "Calculate a monthly or semi-monthly payroll estimate. Enter earnings for one "
                + "pay period; every earnings field is treated as taxable. Loans, minimum-wage-earner "
                + "exemptions, non-taxable benefits, absences, and year-end adjustments are not modeled.";
        VBox box = page("Payroll", description);

        DatePicker from = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker to = new DatePicker(LocalDate.now());
        ComboBox<Employee> employee = new ComboBox<>(FXCollections.observableArrayList());
        employee.setMaxWidth(Double.MAX_VALUE);
        employee.setConverter(new StringConverter<>() {
            @Override
            public String toString(Employee value) {
                return value == null ? "" : value.fullName() + " (" + value.id() + ")";
            }

            @Override
            public Employee fromString(String value) {
                return null;
            }
        });
        try {
            employee.getItems().setAll(repository.listEmployees().stream()
                    .filter(Employee::active)
                    .toList());
        } catch (Exception exception) {
            showError("Employees could not be loaded for payroll.", exception);
        }

        ComboBox<String> frequency = new ComboBox<>(
                FXCollections.observableArrayList("MONTHLY", "SEMI_MONTHLY"));
        frequency.getSelectionModel().selectFirst();

        TextField basic = new TextField("0.00");
        TextField overtime = new TextField("0.00");
        TextField allowance = new TextField("0.00");
        TextField other = new TextField("0.00");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        addFormRow(form, 0, "Employee", employee);
        addFormRow(form, 1, "Period start", from);
        addFormRow(form, 2, "Period end", to);
        addFormRow(form, 3, "Frequency", frequency);
        addFormRow(form, 4, "Basic salary", basic);
        addFormRow(form, 5, "Overtime", overtime);
        addFormRow(form, 6, "Taxable allowance", allowance);
        addFormRow(form, 7, "Other deductions (post-tax)", other);

        employee.valueProperty().addListener((observable, previous, selected) -> {
            if (selected != null) {
                basic.setText(selected.salary().toPlainString());
                frequency.setValue(selected.frequency());
            }
        });

        Label status = new Label(employee.getItems().isEmpty()
                ? "Add an active employee before calculating payroll."
                : "No calculation yet");
        status.setWrapText(true);
        status.setStyle("-fx-text-fill:#486581;");

        GridPane results = new GridPane();
        results.setHgap(16);
        ColumnConstraints detailsColumn = new ColumnConstraints();
        detailsColumn.setPercentWidth(60);
        detailsColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints netPayColumn = new ColumnConstraints();
        netPayColumn.setPercentWidth(40);
        netPayColumn.setHgrow(Priority.ALWAYS);
        results.getColumnConstraints().addAll(detailsColumn, netPayColumn);
        results.setMaxWidth(Double.MAX_VALUE);
        results.setVisible(false);
        results.setManaged(false);

        Button draft = new Button("Create draft payroll period");
        draft.setOnAction(event -> createDraftPeriod(from, to, frequency, status));

        Button calculate = new Button("Calculate payroll");
        calculate.setDisable(employee.getItems().isEmpty());
        calculate.setOnAction(event -> calculatePayroll(
                employee, from, to, basic, overtime, allowance, other, frequency, status, results));

        if (!employee.getItems().isEmpty()) {
            employee.getSelectionModel().selectFirst();
        }

        Label note = new Label("Successful calculations are saved to Payroll History. Results remain estimates "
                + "and are not finalizable. See docs/PROJECT_STATUS.md.");
        note.setWrapText(true);
        note.setStyle("-fx-text-fill:#486581;");
        box.getChildren().addAll(form, draft, calculate, status, results, note);

        ScrollPane scrollPane = new ScrollPane(box);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        return scrollPane;
    }

    private void createDraftPeriod(
            DatePicker from,
            DatePicker to,
            ComboBox<String> frequency,
            Label result) {
        try {
            if (from.getValue() == null || to.getValue() == null) {
                throw new IllegalArgumentException("Choose both payroll period dates");
            }
            repository.createDraftRun(
                    from.getValue().toString(),
                    to.getValue().toString(),
                    frequency.getValue(),
                    "PH-2026.1");
            result.setText("Draft period created. Calculation results are not persisted or finalizable yet.");
        } catch (Exception exception) {
            showError("Payroll period could not be created. Check the dates and try again.", exception);
        }
    }

    private void calculatePayroll(
            ComboBox<Employee> employeePicker,
            DatePicker from,
            DatePicker to,
            TextField basic,
            TextField overtime,
            TextField allowance,
            TextField other,
            ComboBox<String> frequency,
            Label status,
            GridPane results) {
        try {
            Employee employee = employeePicker.getValue();
            if (employee == null) {
                throw new IllegalArgumentException("Select an active employee before calculating payroll");
            }
            if (from.getValue() == null || to.getValue() == null
                    || from.getValue().isAfter(to.getValue())) {
                throw new IllegalArgumentException("Choose a valid payroll period");
            }

            PayrollService service = new PayrollService(
                    appRoot.resolve("build/openpayroll-engine"),
                    appRoot.resolve("config/ph/2026"));
            String calculation = service.calculate(
                    basic.getText(),
                    overtime.getText(),
                    allowance.getText(),
                    other.getText(),
                    frequency.getValue());
            Map<String, String> amounts = parseCalculation(calculation);
            PayrollCalculation history = new PayrollCalculation(
                    0,
                    Instant.now().toString(),
                    employee.id(),
                    employee.fullName(),
                    employee.position(),
                    frequency.getValue(),
                    from.getValue().toString(),
                    to.getValue().toString(),
                    new BigDecimal(basic.getText().trim()),
                    new BigDecimal(overtime.getText().trim()),
                    new BigDecimal(allowance.getText().trim()),
                    new BigDecimal(other.getText().trim()),
                    amount(amounts, "GROSS_PAY"),
                    amount(amounts, "TAXABLE_COMPENSATION"),
                    amount(amounts, "WITHHOLDING_TAX"),
                    amount(amounts, "EMPLOYEE_SSS"),
                    amount(amounts, "EMPLOYEE_PHIC"),
                    amount(amounts, "EMPLOYEE_HDMF"),
                    amount(amounts, "TOTAL_DEDUCTIONS"),
                    amount(amounts, "NET_PAY"),
                    amount(amounts, "EMPLOYER_COST"));
            historyRepository.save(history);

            showPayrollResults(calculation, other.getText(), results);
            status.setText("Calculation saved to Payroll History \u00B7 " + frequency.getValue());
        } catch (Exception exception) {
            String detail = exception.getMessage() == null
                    ? exception.getClass().getSimpleName()
                    : exception.getMessage();
            status.setText("Calculation blocked: " + detail);
            results.setVisible(false);
            results.setManaged(false);
            showError(status.getText(), exception);
        }
    }

    private void showPayrollResults(String calculation, String otherDeduction, GridPane results) {
        Map<String, String> amounts = parseCalculation(calculation);

        VBox grossAndTaxes = resultCard("Gross Pay / Taxes");
        addResultRow(grossAndTaxes, "Gross pay", amounts.get("GROSS_PAY"));
        addResultRow(grossAndTaxes, "Taxable compensation", amounts.get("TAXABLE_COMPENSATION"));
        addResultRow(grossAndTaxes, "Employee SSS", amounts.get("EMPLOYEE_SSS"));
        addResultRow(grossAndTaxes, "Employee PhilHealth", amounts.get("EMPLOYEE_PHIC"));
        addResultRow(grossAndTaxes, "Employee Pag-IBIG", amounts.get("EMPLOYEE_HDMF"));
        addResultRow(grossAndTaxes, "Withholding tax", amounts.get("WITHHOLDING_TAX"));
        addResultRow(grossAndTaxes, "Other deductions", otherDeduction);
        addResultRow(grossAndTaxes, "Total deductions", amounts.get("TOTAL_DEDUCTIONS"));

        VBox netPay = resultCard("Net Pay");
        Label netAmount = new Label(formatMoney(amounts.get("NET_PAY")));
        netAmount.setStyle("-fx-font-size:30px;-fx-font-weight:bold;-fx-text-fill:#102a43;");
        Label takeHome = new Label("Take-home pay for this period");
        takeHome.setStyle("-fx-text-fill:#627d98;");
        netPay.getChildren().addAll(netAmount, takeHome);

        results.getChildren().clear();
        results.add(grossAndTaxes, 0, 0);
        results.add(netPay, 1, 0);
        GridPane.setHgrow(grossAndTaxes, Priority.ALWAYS);
        GridPane.setHgrow(netPay, Priority.ALWAYS);
        results.setVisible(true);
        results.setManaged(true);
    }

    private Map<String, String> parseCalculation(String calculation) {
        return calculation.lines()
                .map(line -> line.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(java.util.stream.Collectors.toMap(
                        parts -> parts[0],
                        parts -> parts[1]));
    }

    private BigDecimal amount(Map<String, String> fields, String key) {
        return new BigDecimal(fields.get(key));
    }

    private VBox resultCard(String title) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#102a43;");

        VBox card = new VBox(10, heading);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:white;-fx-background-radius:6;");
        return card;
    }

    private void addResultRow(VBox card, String label, String amount) {
        HBox row = new HBox(12);
        Label name = new Label(label);
        name.setStyle("-fx-text-fill:#486581;");
        Label value = new Label(formatMoney(amount));
        value.setStyle("-fx-text-fill:#102a43;-fx-font-weight:bold;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(name, spacer, value);
        card.getChildren().add(row);
    }

    private String formatMoney(String amount) {
        return "\u20B1" + new DecimalFormat("#,##0.00").format(new BigDecimal(amount));
    }

    private VBox payrollHistory() {
        VBox box = page("Payroll History", "Timestamped employee payroll calculations stored on this computer.");
        TableView<PayrollCalculation> table = new TableView<>();
        table.setPlaceholder(new Label("No payroll calculations have been saved yet."));
        table.getColumns().addAll(
                historyColumn("Date & Time", item -> formatTimestamp(item.calculatedAt()), 165),
                historyColumn("Employee ID", PayrollCalculation::employeeId, 110),
                historyColumn("Name", PayrollCalculation::employeeName, 150),
                historyColumn("Position", PayrollCalculation::position, 130),
                historyColumn("Pay Period", item -> item.periodStart() + " to " + item.periodEnd(), 180),
                historyColumn("Basic Salary", item -> formatMoney(item.basicSalary()), 115),
                historyColumn("Gross Pay", item -> formatMoney(item.grossPay()), 110),
                historyColumn("Taxes", item -> formatMoney(item.withholdingTax()), 95),
                historyColumn("SSS", item -> formatMoney(item.employeeSss()), 95),
                historyColumn("PhilHealth", item -> formatMoney(item.employeePhilhealth()), 105),
                historyColumn("Pag-IBIG", item -> formatMoney(item.employeePagibig()), 100),
                historyColumn("Net Pay", item -> formatMoney(item.netPay()), 110),
                historyColumn("Allowance", item -> formatMoney(item.allowance()), 105));

        try {
            table.setItems(FXCollections.observableArrayList(historyRepository.listRecent()));
        } catch (Exception exception) {
            showError("Payroll history could not be loaded.", exception);
        }

        VBox.setVgrow(table, Priority.ALWAYS);
        box.getChildren().add(table);
        return box;
    }

    private TableColumn<PayrollCalculation, String> historyColumn(
            String heading,
            java.util.function.Function<PayrollCalculation, String> value,
            double width) {
        TableColumn<PayrollCalculation, String> column = new TableColumn<>(heading);
        column.setCellValueFactory(cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    private String formatMoney(BigDecimal amount) {
        return "\u20B1" + new DecimalFormat("#,##0.00").format(amount);
    }

    private String formatTimestamp(String timestamp) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withLocale(Locale.ROOT)
                    .withZone(ZoneId.systemDefault());
            return formatter.format(Instant.parse(timestamp));
        } catch (Exception ignored) {
            return timestamp;
        }
    }

    private VBox settings() {
        String storageDescription = database.isSqlite()
                ? "Local data is stored in data/openpayroll.db. "
                : "Connected to the configured MySQL database. "
                        + "Rule bundle: PH-2026.1 (source-checked, supported scope only).";
        VBox box = page("Settings", storageDescription);
        Button backup = new Button("Back up database");
        Button restore = new Button("Restore database");
        backup.setDisable(!database.isSqlite());
        restore.setDisable(!database.isSqlite());

        Map<String, String> saved;
        try {
            saved = repository.settings();
        } catch (Exception ignored) {
            saved = Map.of();
        }

        TextField company = setting(saved, "company_name");
        TextField address = setting(saved, "company_address");
        PasswordField tin = new PasswordField();
        tin.setText(saved.getOrDefault("company_tin", ""));
        TextField frequency = setting(saved, "payroll_frequency", "MONTHLY");
        TextField period = setting(saved, "default_payroll_period");
        TextField currency = setting(saved, "currency", "PHP");
        TextField rule = setting(saved, "government_rule_version", "PH-2026.1");
        TextField backupLocation = setting(saved, "backup_location", "backups");
        TextField footer = setting(saved, "payslip_footer");

        GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(8);
        addFormRow(fields, 0, "Company name", company);
        addFormRow(fields, 1, "Company address", address);
        addFormRow(fields, 2, "Company TIN", tin);
        addFormRow(fields, 3, "Payroll frequency", frequency);
        addFormRow(fields, 4, "Default payroll period", period);
        addFormRow(fields, 5, "Currency", currency);
        addFormRow(fields, 6, "Government rule version", rule);
        addFormRow(fields, 7, "Database backup location", backupLocation);
        addFormRow(fields, 8, "Payslip footer", footer);

        Button saveSettings = new Button("Save settings");
        saveSettings.setOnAction(event -> saveSettings(
                company, address, tin, frequency, period, currency, rule, backupLocation, footer));
        backup.setOnAction(event -> backUpDatabase());
        restore.setOnAction(event -> restoreDatabase());

        box.getChildren().addAll(fields, saveSettings, backup, restore);
        if (!database.isSqlite()) {
            Label backupNote = new Label(
                    "For MySQL, use phpMyAdmin Export and Import or the MySQL backup tools.");
            backupNote.setWrapText(true);
            backupNote.setStyle("-fx-text-fill:#486581;");
            box.getChildren().add(backupNote);
        }
        return box;
    }

    private TextField setting(Map<String, String> values, String key) {
        return setting(values, key, "");
    }

    private TextField setting(Map<String, String> values, String key, String defaultValue) {
        return new TextField(values.getOrDefault(key, defaultValue));
    }

    private void saveSettings(
            TextField company,
            TextField address,
            PasswordField tin,
            TextField frequency,
            TextField period,
            TextField currency,
            TextField rule,
            TextField backupLocation,
            TextField footer) {
        try {
            repository.saveSettings(Map.of(
                    "company_name", company.getText(),
                    "company_address", address.getText(),
                    "company_tin", tin.getText(),
                    "payroll_frequency", frequency.getText(),
                    "default_payroll_period", period.getText(),
                    "currency", currency.getText(),
                    "government_rule_version", rule.getText(),
                    "backup_location", backupLocation.getText(),
                    "payslip_footer", footer.getText()));
            new Alert(Alert.AlertType.INFORMATION, "Settings saved locally.").showAndWait();
        } catch (Exception exception) {
            showError("Settings could not be saved.", exception);
        }
    }

    private void backUpDatabase() {
        try {
            Path backup = new BackupService().backup(database.path());
            new Alert(Alert.AlertType.INFORMATION, "Backup created: " + backup).showAndWait();
        } catch (Exception exception) {
            showError("Backup failed.", exception);
        }
    }

    private void restoreDatabase() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a database backup");
        var file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            new BackupService().restore(Path.of(file.toURI()), database.path());
            new Alert(Alert.AlertType.INFORMATION,
                    "Database restored. Restart the application to reload records.").showAndWait();
        } catch (Exception exception) {
            showError("Restore failed.", exception);
        }
    }

    private void showError(String message, Exception exception) {
        try {
            Path directory = appRoot == null ? Path.of("logs") : appRoot.resolve("logs");
            Files.createDirectories(directory);
            Files.writeString(
                    directory.resolve("application.log"),
                    java.time.Instant.now() + " ERROR " + message + " | " + exception
                            + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
            // Logging must not prevent the error dialog from being shown.
        }

        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
