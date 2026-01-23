package ui;

import service.AuthService;
import service.FinanceService;
import service.ExportService;
import service.AnalyticsService;
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
        System.out.println("=".repeat(50));
        System.out.println("       PERSONAL FINANCE MANAGER v4.0");
        System.out.println("        (SQLite Portable Version)");
        System.out.println("=".repeat(50));

        // Показываем меню авторизации
        while (!authService.isLoggedIn()) {
            showAuthMenu();
        }

        // Главное меню после входа
        while (true) {
            showMainMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> showTransactionMenu();
                case "2" -> showCategoryMenu();
                case "3" -> showGoalMenu();
                case "4" -> showAnalyticsMenu();
                case "5" -> showExportMenu();
                case "9" -> {
                    authService.logout();
                    System.out.println("Goodbye!");
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

    // === МЕНЮ АВТОРИЗАЦИИ ===
    private void showAuthMenu() {
        System.out.println("\n=== AUTHENTICATION ===");
        System.out.println("1. 🔐 Login");
        System.out.println("2. 📝 Register");
        System.out.println("3. ❌ Exit");
        System.out.print("Choice: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> login();
            case "2" -> register();
            case "3" -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Invalid choice!");
        }

        if (!authService.isLoggedIn()) {
            pause();
        }
    }

    private void login() {
        System.out.println("\n=== LOGIN ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        authService.login(username, password);
    }

    private void register() {
        System.out.println("\n=== REGISTER ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Email (optional): ");
        String email = scanner.nextLine();

        authService.register(username, password, email.isEmpty() ? null : email);
    }

    // === ГЛАВНОЕ МЕНЮ ===
    private void showMainMenu() {
        String username = authService.getCurrentUsername();
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  Welcome, " + username + "!");
        System.out.println("=".repeat(50));
        System.out.println("1. 💰 Transactions");
        System.out.println("2. 📁 Categories");
        System.out.println("3. 🎯 Goals");
        System.out.println("4. 📊 Analytics");
        System.out.println("5. 💾 Export Data");
        System.out.println("9. 🔓 Logout");
        System.out.println("0. ❌ Exit");
        System.out.print("Choice: ");
    }

    // === ОСТАЛЬНЫЕ МЕТОДЫ БЕЗ ИЗМЕНЕНИЙ (как в предыдущей версии) ===
    // ... [все остальные методы остаются точно такими же как в предыдущем ответе]

    // === МЕНЮ ТРАНЗАКЦИЙ ===
    private void showTransactionMenu() {
        while (true) {
            System.out.println("\n=== TRANSACTIONS ===");
            System.out.println("1. ➕ Add transaction");
            System.out.println("2. 📋 View all transactions");
            System.out.println("3. ✏️  Edit transaction");
            System.out.println("4. 🗑️  Delete transaction");
            System.out.println("5. 🔙 Back");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> addTransaction();
                case "2" -> viewTransactions();
                case "3" -> editTransaction();
                case "4" -> deleteTransaction();
                case "5" -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void addTransaction() {
        System.out.println("\n=== ADD TRANSACTION ===");

        System.out.print("Type (income/expense): ");
        String type = scanner.nextLine().trim();
        if (!type.equalsIgnoreCase("income") && !type.equalsIgnoreCase("expense")) {
            System.out.println("Invalid type!");
            return;
        }

        System.out.print("Amount: ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount!");
            return;
        }

        System.out.print("Date (YYYY-MM-DD or Enter for today): ");
        String dateInput = scanner.nextLine().trim();
        LocalDate date;
        try {
            date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);
        } catch (Exception e) {
            System.out.println("Invalid date! Using today.");
            date = LocalDate.now();
        }

        System.out.print("Description: ");
        String description = scanner.nextLine();

        Long categoryId = selectCategory(type);

        financeService.addTransaction(type, amount, date, description, categoryId);
        System.out.println("✅ Transaction added!");
    }

    private void viewTransactions() {
        System.out.println("\n=== TRANSACTIONS ===");
        List<Map<String, Object>> transactions = financeService.getTransactions();

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.printf("%-6s %-10s %-12s %-15s %-30s %s\n",
                "ID", "Type", "Amount", "Date", "Description", "Category");
        System.out.println("-".repeat(80));

        double totalIncome = 0;
        double totalExpense = 0;

        for (Map<String, Object> t : transactions) {
            String type = (String) t.get("type");
            double amount = (double) t.get("amount");
            String category = t.get("category") != null ? (String) t.get("category") : "-";

            if (type.equals("INCOME")) totalIncome += amount;
            else totalExpense += amount;

            System.out.printf("%-6d %-10s $%-11.2f %-15s %-30s %s\n",
                    (int) t.get("id"),
                    type,
                    amount,
                    t.get("date").toString(),
                    truncate((String) t.get("description"), 28),
                    category);
        }

        System.out.println("-".repeat(80));
        System.out.printf("📊 Total Income: $%.2f | Total Expense: $%.2f | Balance: $%.2f\n",
                totalIncome, totalExpense, totalIncome - totalExpense);
    }

    private void editTransaction() {
        System.out.print("Enter transaction ID to edit: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID!");
            return;
        }

        System.out.print("New amount (or Enter to skip): ");
        String amountInput = scanner.nextLine();
        Double newAmount = null;
        if (!amountInput.isEmpty()) {
            try {
                newAmount = Double.parseDouble(amountInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount!");
                return;
            }
        }

        System.out.print("New description (or Enter to skip): ");
        String newDescription = scanner.nextLine();
        if (newDescription.isEmpty()) newDescription = null;

        financeService.updateTransaction(id, newAmount, newDescription);
        System.out.println("✅ Transaction updated!");
    }

    private void deleteTransaction() {
        System.out.print("Enter transaction ID to delete: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID!");
            return;
        }

        System.out.print("Are you sure? (yes/no): ");
        if (!scanner.nextLine().equalsIgnoreCase("yes")) {
            System.out.println("Cancelled.");
            return;
        }

        financeService.deleteTransaction(id);
        System.out.println("✅ Transaction deleted!");
    }

    // === МЕНЮ КАТЕГОРИЙ ===
    private void showCategoryMenu() {
        while (true) {
            System.out.println("\n=== CATEGORIES ===");
            System.out.println("1. ➕ Add category");
            System.out.println("2. 📋 View all categories");
            System.out.println("3. 🔙 Back");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> addCategory();
                case "2" -> viewCategories();
                case "3" -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void addCategory() {
        System.out.println("\n=== ADD CATEGORY ===");

        System.out.print("Category name: ");
        String name = scanner.nextLine();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty!");
            return;
        }

        System.out.print("Type (income/expense): ");
        String type = scanner.nextLine().trim();
        if (!type.equalsIgnoreCase("income") && !type.equalsIgnoreCase("expense")) {
            System.out.println("Invalid type!");
            return;
        }

        financeService.addCategory(name, type);
        System.out.println("✅ Category added!");
    }

    private void viewCategories() {
        System.out.println("\n=== CATEGORIES ===");
        List<Map<String, Object>> categories = financeService.getCategories();

        if (categories.isEmpty()) {
            System.out.println("No categories found.");
            return;
        }

        System.out.printf("%-6s %-20s %-10s %s\n", "ID", "Name", "Type", "Used in");
        System.out.println("-".repeat(50));

        for (Map<String, Object> c : categories) {
            System.out.printf("%-6d %-20s %-10s %d transactions\n",
                    (int) c.get("id"),
                    (String) c.get("name"),
                    (String) c.get("type"),
                    (int) c.get("usage_count"));
        }
    }

    // === МЕНЮ ЦЕЛЕЙ ===
    private void showGoalMenu() {
        while (true) {
            System.out.println("\n=== GOALS ===");
            System.out.println("1. ➕ Add goal");
            System.out.println("2. 📋 View all goals");
            System.out.println("3. 🔙 Back");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> addGoal();
                case "2" -> viewGoals();
                case "3" -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void addGoal() {
        System.out.println("\n=== ADD GOAL ===");

        System.out.print("Goal name: ");
        String name = scanner.nextLine();

        System.out.print("Target amount: ");
        double target;
        try {
            target = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount!");
            return;
        }

        System.out.print("Target date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine();
        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(dateInput);
        } catch (Exception e) {
            System.out.println("Invalid date! Using today + 1 year.");
            targetDate = LocalDate.now().plusYears(1);
        }

        financeService.addGoal(name, target, targetDate);
        System.out.println("✅ Goal added!");
    }

    private void viewGoals() {
        System.out.println("\n=== GOALS ===");
        List<Map<String, Object>> goals = financeService.getGoals();

        if (goals.isEmpty()) {
            System.out.println("No goals found.");
            return;
        }

        System.out.printf("%-6s %-20s %-12s %-12s %s\n",
                "ID", "Name", "Target", "Current", "Progress");
        System.out.println("-".repeat(65));

        for (Map<String, Object> g : goals) {
            double target = (double) g.get("target_amount");
            double current = (double) g.get("current_amount");
            double progress = target > 0 ? (current / target) * 100 : 0;

            System.out.printf("%-6d %-20s $%-11.2f $%-11.2f %.1f%%\n",
                    (int) g.get("id"),
                    (String) g.get("name"),
                    target,
                    current,
                    progress);
        }
    }

    // === МЕНЮ АНАЛИТИКИ ===
    private void showAnalyticsMenu() {
        while (true) {
            System.out.println("\n=== ANALYTICS ===");
            System.out.println("1. 📅 Monthly summary");
            System.out.println("2. 📊 Expenses by category");
            System.out.println("3. 💪 Financial health");
            System.out.println("4. 🔙 Back");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> showMonthlySummary();
                case "2" -> showExpensesByCategory();
                case "3" -> showFinancialHealth();
                case "4" -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void showMonthlySummary() {
        System.out.println("\n=== MONTHLY SUMMARY (Last 6 months) ===");
        Map<String, Double> summary = analyticsService.getMonthlySummary();

        if (summary.isEmpty()) {
            System.out.println("No data available.");
            return;
        }

        System.out.printf("%-10s %-12s %-12s %-12s\n", "Month", "Income", "Expenses", "Balance");
        System.out.println("-".repeat(50));

        Map<String, Map<String, Double>> monthlyData = new HashMap<>();
        for (Map.Entry<String, Double> entry : summary.entrySet()) {
            String key = entry.getKey();
            String month = key.substring(0, 7);
            String type = key.substring(8);

            monthlyData.putIfAbsent(month, new HashMap<>());
            monthlyData.get(month).put(type, entry.getValue());
        }

        for (Map.Entry<String, Map<String, Double>> entry : monthlyData.entrySet()) {
            String month = entry.getKey();
            Map<String, Double> data = entry.getValue();

            System.out.printf("%-10s $%-11.2f $%-11.2f $%-11.2f\n",
                    month,
                    data.getOrDefault("income", 0.0),
                    data.getOrDefault("expense", 0.0),
                    data.getOrDefault("balance", 0.0));
        }
    }

    private void showExpensesByCategory() {
        System.out.println("\n=== EXPENSES BY CATEGORY (This month) ===");
        List<Map<String, Object>> expenses = analyticsService.getExpensesByCategory();

        if (expenses.isEmpty()) {
            System.out.println("No expenses this month.");
            return;
        }

        System.out.printf("%-20s %s\n", "Category", "Amount");
        System.out.println("-".repeat(40));

        double total = 0;
        for (Map<String, Object> expense : expenses) {
            double amount = (double) expense.get("total");
            total += amount;
            System.out.printf("%-20s $%.2f\n", expense.get("category"), amount);
        }

        System.out.println("-".repeat(40));
        System.out.printf("Total expenses this month: $%.2f\n", total);
    }

    private void showFinancialHealth() {
        System.out.println("\n=== FINANCIAL HEALTH ===");
        Map<String, Double> health = analyticsService.getFinancialHealth();

        System.out.printf("💰 Total Income: $%.2f\n", health.getOrDefault("total_income", 0.0));
        System.out.printf("💸 Total Expenses: $%.2f\n", health.getOrDefault("total_expense", 0.0));
        System.out.printf("⚖️  Net Balance: $%.2f\n", health.getOrDefault("balance", 0.0));
        System.out.printf("📈 Savings Rate: %.1f%%\n", health.getOrDefault("savings_rate", 0.0));

        double savingsRate = health.getOrDefault("savings_rate", 0.0);
        System.out.println("\n💡 Analysis:");
        if (savingsRate > 20) {
            System.out.println("✅ Excellent! You're saving more than 20%");
        } else if (savingsRate > 0) {
            System.out.println("⚠️  Good, but aim for 20% savings");
        } else if (health.getOrDefault("balance", 0.0) >= 0) {
            System.out.println("⚠️  You're breaking even");
        } else {
            System.out.println("❌ Warning! Spending exceeds income!");
        }
    }

    // === МЕНЮ ЭКСПОРТА ===
    private void showExportMenu() {
        while (true) {
            System.out.println("\n=== EXPORT DATA ===");
            System.out.println("1. 📄 Export transactions to TXT");
            System.out.println("2. 📊 Export financial report");
            System.out.println("3. 🔙 Back");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> exportTransactions();
                case "2" -> exportFinancialReport();
                case "3" -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void exportTransactions() {
        System.out.print("Enter filename (e.g., transactions.txt): ");
        String filename = scanner.nextLine();
        if (filename.isEmpty()) filename = "transactions_" + System.currentTimeMillis() + ".txt";

        exportService.exportTransactionsToTxt(filename);
    }

    private void exportFinancialReport() {
        System.out.print("Enter filename (e.g., report.txt): ");
        String filename = scanner.nextLine();
        if (filename.isEmpty()) filename = "financial_report_" + System.currentTimeMillis() + ".txt";

        exportService.exportFinancialReport(filename);
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===
    private Long selectCategory(String type) {
        List<Map<String, Object>> categories = financeService.getCategoriesByType(type);

        if (categories.isEmpty()) {
            System.out.println("No categories available for " + type.toLowerCase());
            return null;
        }

        System.out.println("Available categories:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, categories.get(i).get("name"));
        }

        System.out.print("Choose category (number) or 0 for none: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 0) return null;
            if (choice > 0 && choice <= categories.size()) {
                return ((Integer) categories.get(choice - 1).get("id")).longValue();
            }
        } catch (NumberFormatException e) {
        }

        return null;
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