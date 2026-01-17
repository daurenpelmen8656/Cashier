package ui;

import controller.CategoryController;
import controller.TransactionController;

import java.util.Scanner;

public class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in);
    private final TransactionController transactionController = new TransactionController();
    private final CategoryController categoryController = new CategoryController();

    public void start() {
        System.out.println("=== Personal Finance Manager ===");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> transactionController.addTransaction();
                case "2" -> transactionController.showAllTransactions();
                case "3" -> transactionController.showCurrentMonthBalance();
                case "4" -> transactionController.showMonthlyReport();
                case "5" -> transactionController.showSavingsForecast();
                case "6" -> categoryController.addNewCategory();
                case "7" -> categoryController.showAllCategories();
                case "8" -> showCategoriesByTypeMenu();
                case "0" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }

            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Add new transaction");
        System.out.println("2. View all transactions");
        System.out.println("3. View current month balance");
        System.out.println("4. View monthly report");
        System.out.println("5. Savings forecast");
        System.out.println("6. Add new category");
        System.out.println("7. View all categories");
        System.out.println("8. View categories by type");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private void showCategoriesByTypeMenu() {
        System.out.print("Enter type (INCOME/EXPENSE): ");
        String type = scanner.nextLine().toUpperCase();

        if (type.equals("INCOME") || type.equals("EXPENSE")) {
            categoryController.showCategoriesByType(type);
        } else {
            System.out.println("Invalid type. Must be INCOME or EXPENSE.");
        }
    }
}