package service;

import entity.Category;
import repository.CategoryRepository;

import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepository = new CategoryRepository();

    public void addCategory(String name, String type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (!type.equals("INCOME") && !type.equals("EXPENSE")) {
            throw new IllegalArgumentException("Type must be 'INCOME' or 'EXPENSE'");
        }

        Category category = new Category();
        category.setName(name);
        category.setType(type);
        categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getIncomeCategories() {
        return categoryRepository.findByType("INCOME");
    }

    public List<Category> getExpenseCategories() {
        return categoryRepository.findByType("EXPENSE");
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public void deleteCategory(Long id) {
        categoryRepository.delete(id);
    }
}