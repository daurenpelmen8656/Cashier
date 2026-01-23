package service;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsService {
    private final AuthService authService = new AuthService();

    private Integer getUserId() {
        return authService.getCurrentUserId();
    }

    public Map<String, Double> getMonthlySummary() {
        Integer userId = getUserId();
        if (userId == null) return new HashMap<>();

        Map<String, Double> summary = new HashMap<>();
        String sql = """
            SELECT 
                strftime('%Y-%m', date) as month,
                SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income,
                SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense
            FROM transactions
            WHERE user_id = ? AND date >= date('now', '-6 months')
            GROUP BY strftime('%Y-%m', date)
            ORDER BY month DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String month = rs.getString("month");
                double income = rs.getDouble("income");
                double expense = rs.getDouble("expense");
                summary.put(month + "_income", income);
                summary.put(month + "_expense", expense);
                summary.put(month + "_balance", income - expense);
            }
        } catch (SQLException e) {
            System.err.println("Error getting monthly summary: " + e.getMessage());
        }
        return summary;
    }

    public List<Map<String, Object>> getExpensesByCategory() {
        Integer userId = getUserId();
        if (userId == null) return new ArrayList<>();

        List<Map<String, Object>> expenses = new ArrayList<>();
        String sql = """
            SELECT c.name, SUM(t.amount) as total
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.type = 'EXPENSE' AND t.user_id = ? 
                AND t.date >= date('now', 'start of month')
            GROUP BY c.name
            ORDER BY total DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> expense = new HashMap<>();
                expense.put("category", rs.getString("name"));
                expense.put("total", rs.getDouble("total"));
                expenses.add(expense);
            }
        } catch (SQLException e) {
            System.err.println("Error getting expenses by category: " + e.getMessage());
        }
        return expenses;
    }

    public Map<String, Double> getFinancialHealth() {
        Integer userId = getUserId();
        if (userId == null) return new HashMap<>();

        Map<String, Double> health = new HashMap<>();
        String sqlIncome = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND user_id = ?";
        String sqlExpense = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Получаем доходы
            try (PreparedStatement pstmt = conn.prepareStatement(sqlIncome)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    health.put("total_income", rs.getDouble(1));
                }
            }

            // Получаем расходы
            try (PreparedStatement pstmt = conn.prepareStatement(sqlExpense)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    health.put("total_expense", rs.getDouble(1));
                }
            }

            double totalIncome = health.getOrDefault("total_income", 0.0);
            double totalExpense = health.getOrDefault("total_expense", 0.0);
            health.put("balance", totalIncome - totalExpense);
            health.put("savings_rate", totalIncome > 0 ? ((totalIncome - totalExpense) / totalIncome) * 100 : 0);

        } catch (SQLException e) {
            System.err.println("Error getting financial health: " + e.getMessage());
        }
        return health;
    }
}