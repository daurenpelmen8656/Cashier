package controller;

import entity.Transaction;
import service.TransactionService;
import service.CategoryService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class TransactionController {
    private final TransactionService transactionService = new TransactionService();
    private final CategoryController categoryController = new CategoryController();
    private final Scanner scanner = new Scanner(System.in);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void addTransaction() {
        System.out.print("Enter type (INCOME/EXPENSE): ");
        String type = scanner.nextLine().toUpperCase();

        if (!type.equals("INCOME") && !type.equals("EXPENSE")) {
            System.out.println("Invalid type. Must be INCOME or EXPENSE.");
            return;
        }

        System.out.print("Enter amount: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter date (YYYY-MM-DD) or press Enter for today: ");
            String dateInput = scanner.nextLine();
            LocalDate date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput, dateFormatter);

            System.out.print("Enter description: ");
            String description = scanner.nextLine();

            Long categoryId = categoryController.selectCategoryId(type);

            transactionService.addTransaction(type, amount, date, description, categoryId);
            System.out.println("Transaction added successfully!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void showAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println("=== All Transactions ===");
        System.out.printf("%-5s %-10s %-10s %-12s %-30s %s\n",
                "ID", "Type", "Amount", "Date", "Description", "Category ID");
        System.out.println("-".repeat(80));

        for (Transaction t : transactions) {
            System.out.printf("%-5d %-10s %-10.2f %-12s %-30s %d\n",
                    t.getId(),
                    t.getType(),
                    t.getAmount(),
                    t.getDate(),
                    t.getDescription().length() > 30 ? t.getDescription().substring(0, 27) + "..." : t.getDescription(),
                    t.getCategoryId());
        }
    }

    public void showMonthlyReport() {
        System.out.print("Enter year (e.g., 2024): ");
        int year = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine());

        try {
            List<Transaction> transactions = transactionService.getTransactionsByMonth(year, month);

            System.out.printf("=== Transactions for %d-%02d ===\n", year, month);
            if (transactions.isEmpty()) {
                System.out.println("No transactions for this period.");
                return;
            }

            double totalIncome = 0;
            double totalExpense = 0;

            System.out.printf("%-10s %-10s %-12s %-30s\n", "Type", "Amount", "Date", "Description");
            System.out.println("-".repeat(62));

            for (Transaction t : transactions) {
                System.out.printf("%-10s %-10.2f %-12s %-30s\n",
                        t.getType(),
                        t.getAmount(),
                        t.getDate(),
                        t.getDescription().length() > 30 ? t.getDescription().substring(0, 27) + "..." : t.getDescription());

                if (t.getType().equals("INCOME")) {
                    totalIncome += t.getAmount();
                } else {
                    totalExpense += t.getAmount();
                }
            }

            System.out.println("\n=== Summary ===");
            System.out.printf("Total Income: $%.2f\n", totalIncome);
            System.out.printf("Total Expense: $%.2f\n", totalExpense);
            System.out.printf("Balance: $%.2f\n", totalIncome - totalExpense);

            transactionService.analyzeExpenses(year, month);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void showCurrentMonthBalance() {
        LocalDate now = LocalDate.now();
        double balance = transactionService.getBalanceForMonth(now.getYear(), now.getMonthValue());
        System.out.printf("Current month (%s) balance: $%.2f\n",
                YearMonth.from(now), balance);
    }

    public void showSavingsForecast() {
        System.out.print("Enter your average monthly income: ");
        double monthlyIncome = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter your average monthly expenses: ");
        double monthlyExpenses = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter number of months to forecast: ");
        int months = Integer.parseInt(scanner.nextLine());

        transactionService.calculateSavingsForecast(monthlyIncome, monthlyExpenses, months);
    }
}