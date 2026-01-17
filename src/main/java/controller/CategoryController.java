package controller;

import entity.Category;
import service.CategoryService;

import java.util.List;
import java.util.Scanner;

public class CategoryController {
    private final CategoryService categoryService = new CategoryService();
    private final Scanner scanner = new Scanner(System.in);

    public void showAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("No categories found.");
            return;
        }

        System.out.println("=== All Categories ===");
        for (Category category : categories) {
            System.out.printf("%d. %s (%s)\n",
                    category.getId(),
                    category.getName(),
                    category.getType());
        }
    }

    public void showCategoriesByType(String type) {
        List<Category> categories = type.equals("INCOME")
                ? categoryService.getIncomeCategories()
                : categoryService.getExpenseCategories();

        System.out.println("=== " + type + " Categories ===");
        for (Category category : categories) {
            System.out.printf("%d. %s\n", category.getId(), category.getName());
        }
    }

    public void addNewCategory() {
        System.out.print("Enter category name: ");
        String name = scanner.nextLine();

        System.out.print("Enter type (INCOME/EXPENSE): ");
        String type = scanner.nextLine().toUpperCase();

        if (!type.equals("INCOME") && !type.equals("EXPENSE")) {
            System.out.println("Invalid type. Must be INCOME or EXPENSE.");
            return;
        }

        try {
            categoryService.addCategory(name, type);
            System.out.println("Category added successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public Long selectCategoryId(String type) {
        List<Category> categories = type.equals("INCOME")
                ? categoryService.getIncomeCategories()
                : categoryService.getExpenseCategories();

        if (categories.isEmpty()) {
            System.out.println("No " + type.toLowerCase() + " categories available.");
            return null;
        }

        System.out.println("Select " + type.toLowerCase() + " category:");
        for (Category category : categories) {
            System.out.printf("%d. %s\n", category.getId(), category.getName());
        }

        System.out.print("Enter category ID (0 for none): ");
        try {
            long choice = Long.parseLong(scanner.nextLine());
            return choice == 0 ? null : choice;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. No category selected.");
            return null;
        }
    }
}