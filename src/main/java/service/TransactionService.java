package service;

import entity.Transaction;
import repository.TransactionRepository;

import java.time.LocalDate;
import java.util.List;

public class TransactionService {
    private final TransactionRepository transactionRepository = new TransactionRepository();

    public void addTransaction(String type, Double amount, LocalDate date, String description, Long categoryId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (!type.equals("INCOME") && !type.equals("EXPENSE")) {
            throw new IllegalArgumentException("Type must be 'INCOME' or 'EXPENSE'");
        }

        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setDescription(description);
        transaction.setCategoryId(categoryId);

        transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return transactionRepository.findByDateRange(startDate, endDate);
    }

    public Double getBalanceForMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Double totalIncome = transactionRepository.getTotalByTypeAndPeriod("INCOME", startDate, endDate);
        Double totalExpense = transactionRepository.getTotalByTypeAndPeriod("EXPENSE", startDate, endDate);

        return (totalIncome != null ? totalIncome : 0.0) - (totalExpense != null ? totalExpense : 0.0);
    }

    // Бизнес-логика: Анализ расходов
    public void analyzeExpenses(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Double totalExpenses = transactionRepository.getTotalByTypeAndPeriod("EXPENSE", startDate, endDate);
        Double totalIncome = transactionRepository.getTotalByTypeAndPeriod("INCOME", startDate, endDate);

        if (totalExpenses != null && totalExpenses > 0 && totalIncome != null && totalIncome > 0) {
            double expenseRatio = (totalExpenses / totalIncome) * 100;
            System.out.printf("Expense to Income Ratio: %.2f%%\n", expenseRatio);

            if (expenseRatio > 80) {
                System.out.println("Warning: Your expenses are more than 80% of your income!");
                System.out.println("Suggestion: Try to reduce non-essential expenses.");
            } else if (expenseRatio < 50) {
                System.out.println("Good: You're saving more than half of your income!");
            }
        }
    }

    // Бизнес-логика: Прогноз накоплений
    public void calculateSavingsForecast(double monthlyIncome, double monthlyExpense, int months) {
        double monthlySavings = monthlyIncome - monthlyExpense;
        double totalSavings = monthlySavings * months;

        System.out.printf("Monthly savings: $%.2f\n", monthlySavings);
        System.out.printf("After %d months you could save: $%.2f\n", months, totalSavings);

        if (monthlySavings < 0) {
            System.out.println("Warning: You're spending more than you earn!");
            double neededReduction = Math.abs(monthlySavings);
            System.out.printf("You need to reduce expenses by $%.2f per month\n", neededReduction);
        }
    }
}