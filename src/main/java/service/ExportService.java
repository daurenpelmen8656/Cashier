package service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ExportService {
    private final FinanceService financeService = new FinanceService();
    private final AuthService authService = new AuthService();

    public void exportTransactionsToTxt(String filename) {
        if (!authService.isLoggedIn()) {
            System.out.println("❌ You must be logged in!");
            return;
        }

        List<Map<String, Object>> transactions = financeService.getTransactions();

        if (transactions.isEmpty()) {
            System.out.println("No transactions to export!");
            return;
        }

        try (FileWriter writer = new FileWriter(filename)) {
            // Заголовок с именем пользователя
            writer.write("=".repeat(60) + "\n");
            writer.write("          FINANCIAL TRANSACTIONS REPORT\n");
            writer.write("User: " + authService.getCurrentUsername() + "\n");
            writer.write("=".repeat(60) + "\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");

            // Таблица
            writer.write(String.format("%-6s %-10s %-12s %-15s %-30s %s\n",
                    "ID", "Type", "Amount", "Date", "Description", "Category"));
            writer.write("-".repeat(80) + "\n");

            double totalIncome = 0;
            double totalExpense = 0;

            for (Map<String, Object> t : transactions) {
                String type = (String) t.get("type");
                double amount = (double) t.get("amount");
                String category = t.get("category") != null ? (String) t.get("category") : "-";

                if (type.equals("INCOME")) totalIncome += amount;
                else totalExpense += amount;

                writer.write(String.format("%-6d %-10s $%-11.2f %-15s %-30s %s\n",
                        (int) t.get("id"),
                        type,
                        amount,
                        t.get("date").toString(),
                        truncate((String) t.get("description"), 28),
                        category));
            }

            // Итоги
            writer.write("\n" + "=".repeat(60) + "\n");
            writer.write("SUMMARY:\n");
            writer.write(String.format("Total Income:  $%.2f\n", totalIncome));
            writer.write(String.format("Total Expense: $%.2f\n", totalExpense));
            writer.write(String.format("Net Balance:   $%.2f\n", totalIncome - totalExpense));
            writer.write("=".repeat(60) + "\n");

            System.out.println("✅ Transactions exported to " + filename);

        } catch (IOException e) {
            System.err.println("Error exporting to file: " + e.getMessage());
        }
    }

    public void exportFinancialReport(String filename) {
        if (!authService.isLoggedIn()) {
            System.out.println("❌ You must be logged in!");
            return;
        }

        List<Map<String, Object>> transactions = financeService.getTransactions();
        List<Map<String, Object>> goals = financeService.getGoals();

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("=".repeat(60) + "\n");
            writer.write("          COMPREHENSIVE FINANCIAL REPORT\n");
            writer.write("User: " + authService.getCurrentUsername() + "\n");
            writer.write("=".repeat(60) + "\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");

            // 1. Общая статистика
            double totalIncome = 0;
            double totalExpense = 0;
            for (Map<String, Object> t : transactions) {
                String type = (String) t.get("type");
                double amount = (double) t.get("amount");
                if (type.equals("INCOME")) totalIncome += amount;
                else totalExpense += amount;
            }

            writer.write("FINANCIAL OVERVIEW:\n");
            writer.write(String.format("Total Income:      $%.2f\n", totalIncome));
            writer.write(String.format("Total Expenses:    $%.2f\n", totalExpense));
            writer.write(String.format("Net Balance:       $%.2f\n", totalIncome - totalExpense));
            writer.write(String.format("Savings Rate:      %.1f%%\n\n",
                    totalIncome > 0 ? ((totalIncome - totalExpense) / totalIncome) * 100 : 0));

            // 2. Последние транзакции
            writer.write("RECENT TRANSACTIONS (Last 20):\n");
            writer.write("-".repeat(60) + "\n");
            int count = 0;
            for (Map<String, Object> t : transactions) {
                if (count++ >= 20) break;
                writer.write(String.format("%s | %s | $%.2f | %s\n",
                        t.get("date"),
                        t.get("type"),
                        t.get("amount"),
                        t.get("description")));
            }

            // 3. Цели
            if (!goals.isEmpty()) {
                writer.write("\nFINANCIAL GOALS:\n");
                writer.write("-".repeat(60) + "\n");
                for (Map<String, Object> goal : goals) {
                    double target = (double) goal.get("target_amount");
                    double current = (double) goal.get("current_amount");
                    double progress = target > 0 ? (current / target) * 100 : 0;

                    writer.write(String.format("%s: $%.2f/$%.2f (%.1f%%)\n",
                            goal.get("name"), current, target, progress));
                }
            }

            writer.write("\n" + "=".repeat(60) + "\n");
            writer.write("End of Report\n");

            System.out.println("✅ Financial report exported to " + filename);

        } catch (IOException e) {
            System.err.println("Error exporting report: " + e.getMessage());
        }
    }

    private String truncate(String text, int length) {
        if (text == null) return "";
        return text.length() > length ? text.substring(0, length - 3) + "..." : text;
    }
}