package ui;

import service.AuthService;
import service.FinanceService;
import service.ExportService;
import service.AnalyticsService;
import util.PasswordUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService = new AuthService();
    private final FinanceService financeService = new FinanceService();
    private final ExportService exportService = new ExportService();
    private final AnalyticsService analyticsService = new AnalyticsService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void start() {
        printWelcome();

        while (!authService.isLoggedIn()) {
            showAuthMenu();
        }

        while (true) {
            showMainMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> showTransactionMenu();
                case "2" -> showCategoryMenu();
                case "3" -> showGoalMenu();
                case "4" -> showAnalyticsMenu();
                case "5" -> showExportMenu();
                case "6" -> showAdminMenu();
                case "9" -> {
                    authService.logout();
                    return;
                }
                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }

            pause();
        }
    }

    private void printWelcome() {
        System.out.println("=".repeat(50));
        System.out.println("       PERSONAL FINANCE MANAGER v5.0");
        System.out.println("=".repeat(50));
    }

    // === АВТОРИЗАЦИЯ ===
    private void showAuthMenu() {
        System.out.println("\n=== AUTHENTICATION ===");
        System.out.println("1. 🔐 Login");
        System.out.println("2. 📝 Register");
        System.out.println("3. ❌ Exit");
        System.out.print("Choice: ");

        switch (scanner.nextLine()) {
            case "1" -> login();
            case "2" -> register();
            case "3" -> System.exit(0);
            default -> System.out.println("Invalid choice!");
        }

        if (!authService.isLoggedIn()) pause();
    }

    private void login() {
        System.out.println("\n=== LOGIN ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();

        // Используем простую версию для IDE (чтобы избежать IOException)
        String password = PasswordUtil.readPasswordSimple();

        authService.login(username, password);
    }

    private void register() {
        System.out.println("\n=== REGISTER ===");

        System.out.print("Username: ");
        String username = scanner.nextLine();
        if (username.length() < 3) {
            System.out.println("Username must be at least 3 characters!");
            return;
        }

        System.out.print("Password: ");
        String password = PasswordUtil.readPasswordSimple();
        if (password.length() < 4) {
            System.out.println("Password must be at least 4 characters!");
            return;
        }

        System.out.print("Email (optional): ");
        String email = scanner.nextLine();

        authService.register(username, password, email.isEmpty() ? null : email);
    }

    // === ГЛАВНОЕ МЕНЮ ===
    private void showMainMenu() {
        String username = authService.getCurrentUsername();
        String adminBadge = authService.isAdmin() ? " 👑" : "";

        System.out.println("\n" + "=".repeat(50));
        System.out.println("  Welcome, " + username + adminBadge + "!");
        System.out.println("=".repeat(50));
        System.out.println("1. 💰 Transactions");
        System.out.println("2. 📁 Categories");
        System.out.println("3. 🎯 Goals");
        System.out.println("4. 📊 Analytics");
        System.out.println("5. 💾 Export Data");
        if (authService.isAdmin()) {
            System.out.println("6. 🔧 Admin Tools");
        }
        System.out.println("9. 🔓 Logout");
        System.out.println("0. ❌ Exit");
        System.out.print("Choice: ");
    }

    // === АДМИН МЕНЮ ===
    private void showAdminMenu() {
        if (!authService.isAdmin()) {
            System.out.println("❌ Admin privileges required!");
            return;
        }

        while (true) {
            System.out.println("\n=== ADMIN TOOLS ===");
            System.out.println("1. 👥 List all users");
            System.out.println("2. 👀 View user transactions");
            System.out.println("3. 🔑 Reset user password");
            System.out.println("4. 📊 System statistics");
            System.out.println("5. 🔙 Back");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> authService.listAllUsers();
                case "2" -> viewUserTransactions();
                case "3" -> authService.resetUserPassword(scanner);
                case "4" -> showSystemStats();
                case "5" -> { return; }
                default -> System.out.println("Invalid choice!");
            }

            pause();
        }
    }

    private void viewUserTransactions() {
        System.out.print("Enter user ID: ");
        try {
            int userId = Integer.parseInt(scanner.nextLine());
            authService.viewUserTransactions(userId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid user ID!");
        }
    }

    private void showSystemStats() {
        System.out.println("\n=== SYSTEM STATISTICS ===");

        // Упрощенная версия без сложных SQL запросов
        try (var conn = database.DatabaseConnection.getConnection();
             var stmt = conn.createStatement()) {

            String[][] stats = {
                    {"Total users:", "SELECT COUNT(*) as count FROM users"},
                    {"Total transactions:", "SELECT COUNT(*) as count FROM transactions"},
                    {"Total categories:", "SELECT COUNT(*) as count FROM categories"},
                    {"Total goals:", "SELECT COUNT(*) as count FROM goals"}
            };

            for (String[] stat : stats) {
                String label = stat[0];
                String query = stat[1];

                var rs = stmt.executeQuery(query);
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.printf("%-25s %d\n", label, count);
                }
            }

            // Размер базы данных (приблизительно)
            System.out.printf("%-25s %s\n", "Database file:", "finance_manager.db");

        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
        }
    }

    // === ТРАНЗАКЦИИ ===
    private void showTransactionMenu() {
        while (true) {
            System.out.println("\n=== TRANSACTIONS ===");
            System.out.println("1. ➕ Add");
            System.out.println("2. 📋 View");
            System.out.println("3. ✏️  Edit");
            System.out.println("4. 🗑️  Delete");
            System.out.println("5. 🔙 Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> addTransaction();
                case "2" -> viewTransactions();
                case "3" -> editTransaction();
                case "4" -> deleteTransaction();
                case "5" -> { return; }
                default -> System.out.println("Invalid choice!");
            }

            pause();
        }
    }

    private void addTransaction() {
        System.out.println("\n=== ADD TRANSACTION ===");

        String type = getValidInput("Type (income/expense): ",
                input -> input.equalsIgnoreCase("income") || input.equalsIgnoreCase("expense"),
                "Invalid type! Use 'income' or 'expense'");
        if (type == null) return;

        Double amount = getValidDouble("Amount: ", a -> a > 0, "Amount must be positive!");
        if (amount == null) return;

        LocalDate date = getDate("Date (YYYY-MM-DD or Enter for today): ");

        System.out.print("Description: ");
        String description = scanner.nextLine();

        Long categoryId = selectCategory(type);

        financeService.addTransaction(type, amount, date, description, categoryId);
    }

    private void viewTransactions() {
        System.out.println("\n=== TRANSACTIONS ===");
        var transactions = financeService.getTransactions();

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        printTransactionsTable(transactions);
    }

    private void printTransactionsTable(List<Map<String, Object>> transactions) {
        System.out.printf("%-6s %-10s %-10s %-12s %-25s %s\n",
                "ID", "Type", "Amount", "Date", "Description", "Category");
        System.out.println("-".repeat(80));

        double totalIncome = 0, totalExpense = 0;

        for (var t : transactions) {
            String type = (String) t.get("type");
            double amount = (double) t.get("amount");
            String category = t.get("category") != null ? (String) t.get("category") : "-";

            if (type.equals("INCOME")) totalIncome += amount;
            else totalExpense += amount;

            System.out.printf("%-6d %-10s $%-9.2f %-12s %-25s %s\n",
                    (int) t.get("id"), type, amount,
                    t.get("date"), truncate((String) t.get("description"), 23), category);
        }

        System.out.println("-".repeat(80));
        System.out.printf("📊 Income: $%.2f | Expense: $%.2f | Balance: $%.2f\n",
                totalIncome, totalExpense, totalIncome - totalExpense);
    }

    private void editTransaction() {
        Integer id = getInteger("Enter transaction ID to edit: ");
        if (id == null) return;

        System.out.print("New amount (or Enter to skip): ");
        String amountInput = scanner.nextLine();
        Double newAmount = amountInput.isEmpty() ? null : parseDouble(amountInput);

        System.out.print("New description (or Enter to skip): ");
        String newDescription = scanner.nextLine();
        if (newDescription.isEmpty()) newDescription = null;

        if (newAmount == null && newDescription == null) {
            System.out.println("Nothing to update!");
            return;
        }

        financeService.updateTransaction(id, newAmount, newDescription);
    }

    private void deleteTransaction() {
        Integer id = getInteger("Enter transaction ID to delete: ");
        if (id == null) return;

        if (confirm("Are you sure?")) {
            financeService.deleteTransaction(id);
        } else {
            System.out.println("Cancelled.");
        }
    }

    // === КАТЕГОРИИ ===
    private void showCategoryMenu() {
        while (true) {
            System.out.println("\n=== CATEGORIES ===");
            System.out.println("1. ➕ Add");
            System.out.println("2. 📋 View");
            System.out.println("3. 🔙 Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> addCategory();
                case "2" -> viewCategories();
                case "3" -> { return; }
                default -> System.out.println("Invalid choice!");
            }

            pause();
        }
    }

    private void addCategory() {
        System.out.println("\n=== ADD CATEGORY ===");

        System.out.print("Name: ");
        String name = scanner.nextLine();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty!");
            return;
        }

        String type = getValidInput("Type (income/expense): ",
                input -> input.equalsIgnoreCase("income") || input.equalsIgnoreCase("expense"),
                "Invalid type!");
        if (type == null) return;

        financeService.addCategory(name, type);
    }

    private void viewCategories() {
        System.out.println("\n=== CATEGORIES ===");
        var categories = financeService.getCategories();

        if (categories.isEmpty()) {
            System.out.println("No categories found.");
            return;
        }

        System.out.printf("%-6s %-20s %-10s %s\n", "ID", "Name", "Type", "Used in");
        System.out.println("-".repeat(50));

        for (var c : categories) {
            System.out.printf("%-6d %-20s %-10s %d transactions\n",
                    (int) c.get("id"),
                    (String) c.get("name"),
                    (String) c.get("type"),
                    (int) c.get("usage_count"));
        }
    }

    // === ЦЕЛИ ===
    private void showGoalMenu() {
        while (true) {
            System.out.println("\n=== GOALS ===");
            System.out.println("1. ➕ Add");
            System.out.println("2. 📋 View");
            System.out.println("3. 🔙 Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> addGoal();
                case "2" -> viewGoals();
                case "3" -> { return; }
                default -> System.out.println("Invalid choice!");
            }

            pause();
        }
    }

    private void addGoal() {
        System.out.println("\n=== ADD GOAL ===");

        System.out.print("Name: ");
        String name = scanner.nextLine();

        Double target = getValidDouble("Target amount: ", a -> a > 0, "Amount must be positive!");
        if (target == null) return;

        LocalDate targetDate = getDate("Target date (YYYY-MM-DD): ");
        if (targetDate == null) {
            System.out.println("Invalid date! Using today + 1 year.");
            targetDate = LocalDate.now().plusYears(1);
        }

        financeService.addGoal(name, target, targetDate);
    }

    private void viewGoals() {
        System.out.println("\n=== GOALS ===");
        var goals = financeService.getGoals();

        if (goals.isEmpty()) {
            System.out.println("No goals found.");
            return;
        }

        System.out.printf("%-6s %-20s %-12s %-12s %s\n",
                "ID", "Name", "Target", "Current", "Progress");
        System.out.println("-".repeat(65));

        for (var g : goals) {
            double target = (double) g.get("target_amount");
            double current = (double) g.get("current_amount");
            double progress = target > 0 ? (current / target) * 100 : 0;

            System.out.printf("%-6d %-20s $%-11.2f $%-11.2f %.1f%%\n",
                    (int) g.get("id"),
                    (String) g.get("name"),
                    target, current, progress);
        }
    }

    // === АНАЛИТИКА ===
    private void showAnalyticsMenu() {
        while (true) {
            System.out.println("\n=== ANALYTICS ===");
            System.out.println("1. 📅 Monthly summary");
            System.out.println("2. 📊 Expenses by category");
            System.out.println("3. 💪 Financial health");
            System.out.println("4. 🔙 Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> showMonthlySummary();
                case "2" -> showExpensesByCategory();
                case "3" -> showFinancialHealth();
                case "4" -> { return; }
                default -> System.out.println("Invalid choice!");
            }

            pause();
        }
    }

    private void showMonthlySummary() {
        System.out.println("\n=== MONTHLY SUMMARY ===");
        var summary = analyticsService.getMonthlySummary();

        if (summary.isEmpty()) {
            System.out.println("No data available.");
            return;
        }

        System.out.printf("%-10s %-12s %-12s %-12s\n", "Month", "Income", "Expenses", "Balance");
        System.out.println("-".repeat(50));

        var monthlyData = new HashMap<String, Map<String, Double>>();
        summary.forEach((key, value) -> {
            String month = key.substring(0, 7);
            String type = key.substring(8);
            monthlyData.putIfAbsent(month, new HashMap<>());
            monthlyData.get(month).put(type, value);
        });

        monthlyData.forEach((month, data) -> {
            System.out.printf("%-10s $%-11.2f $%-11.2f $%-11.2f\n",
                    month,
                    data.getOrDefault("income", 0.0),
                    data.getOrDefault("expense", 0.0),
                    data.getOrDefault("balance", 0.0));
        });
    }

    private void showExpensesByCategory() {
        System.out.println("\n=== EXPENSES BY CATEGORY ===");
        var expenses = analyticsService.getExpensesByCategory();

        if (expenses.isEmpty()) {
            System.out.println("No expenses this month.");
            return;
        }

        System.out.printf("%-20s %s\n", "Category", "Amount");
        System.out.println("-".repeat(40));

        double total = 0;
        for (var expense : expenses) {
            double amount = (double) expense.get("total");
            total += amount;
            System.out.printf("%-20s $%.2f\n", expense.get("category"), amount);
        }

        System.out.println("-".repeat(40));
        System.out.printf("Total: $%.2f\n", total);
    }

    private void showFinancialHealth() {
        System.out.println("\n=== FINANCIAL HEALTH ===");
        var health = analyticsService.getFinancialHealth();

        System.out.printf("💰 Income: $%.2f\n", health.getOrDefault("total_income", 0.0));
        System.out.printf("💸 Expenses: $%.2f\n", health.getOrDefault("total_expense", 0.0));
        System.out.printf("⚖️  Balance: $%.2f\n", health.getOrDefault("balance", 0.0));
        System.out.printf("📈 Savings: %.1f%%\n", health.getOrDefault("savings_rate", 0.0));

        double savingsRate = health.getOrDefault("savings_rate", 0.0);
        System.out.println("\n💡 Analysis:");
        if (savingsRate > 20) System.out.println("✅ Excellent savings!");
        else if (savingsRate > 0) System.out.println("⚠️  Aim for 20% savings");
        else if (health.getOrDefault("balance", 0.0) >= 0) System.out.println("⚠️  Breaking even");
        else System.out.println("❌ Spending > Income!");
    }

    // === ЭКСПОРТ ===
    private void showExportMenu() {
        while (true) {
            System.out.println("\n=== EXPORT DATA ===");
            System.out.println("1. 📄 Export transactions");
            System.out.println("2. 📊 Export full report");
            System.out.println("3. 🔙 Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> exportTransactions();
                case "2" -> exportFinancialReport();
                case "3" -> { return; }
                default -> System.out.println("Invalid choice!");
            }

            pause();
        }
    }

    private void exportTransactions() {
        System.out.print("Filename (or Enter for default): ");
        String filename = scanner.nextLine();
        if (filename.isEmpty()) filename = "transactions_" + System.currentTimeMillis() + ".txt";

        exportService.exportTransactionsToTxt(filename);
    }

    private void exportFinancialReport() {
        System.out.print("Filename (or Enter for default): ");
        String filename = scanner.nextLine();
        if (filename.isEmpty()) filename = "report_" + System.currentTimeMillis() + ".txt";

        exportService.exportFinancialReport(filename);
    }

    // === УТИЛИТЫ ВВОДА ===
    private String getValidInput(String prompt, java.util.function.Predicate<String> validator, String errorMsg) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        if (!validator.test(input)) {
            System.out.println(errorMsg);
            return null;
        }
        return input;
    }

    private Integer getInteger(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number!");
            return null;
        }
    }

    private Double getValidDouble(String prompt, java.util.function.Predicate<Double> validator, String errorMsg) {
        System.out.print(prompt);
        try {
            Double value = Double.parseDouble(scanner.nextLine());
            if (!validator.test(value)) {
                System.out.println(errorMsg);
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number!");
            return null;
        }
    }

    private Double parseDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate getDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        try {
            return input.isEmpty() ? LocalDate.now() : LocalDate.parse(input);
        } catch (Exception e) {
            System.out.println("Invalid date! Using today.");
            return LocalDate.now();
        }
    }

    private boolean confirm(String message) {
        System.out.print(message + " (yes/no): ");
        return scanner.nextLine().equalsIgnoreCase("yes");
    }

    private Long selectCategory(String type) {
        var categories = financeService.getCategoriesByType(type);

        if (categories.isEmpty()) {
            System.out.println("No categories available for " + type.toLowerCase());
            return null;
        }

        System.out.println("Available categories:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, categories.get(i).get("name"));
        }

        System.out.print("Choose category (0 for none): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 0 || choice > categories.size()) return null;
            return ((Integer) categories.get(choice - 1).get("id")).longValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String truncate(String text, int length) {
        if (text == null) return "";
        return text.length() > length ? text.substring(0, length - 3) + "..." : text;
    }

    private void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}