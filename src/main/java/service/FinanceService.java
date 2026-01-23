package service;

import database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class FinanceService {
    private final AuthService authService = new AuthService();

    private Integer getUserId() {
        return authService.getCurrentUserId();
    }

    public void addTransaction(String type, double amount, LocalDate date, String description, Long categoryId) {
        Integer userId = getUserId();
        if (userId == null) {
            System.out.println("❌ You must be logged in!");
            return;
        }

        String sql = "INSERT INTO transactions (type, amount, date, description, category_id, user_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type.toUpperCase());
            pstmt.setDouble(2, amount);
            pstmt.setString(3, date.toString());
            pstmt.setString(4, description);
            if (categoryId != null) {
                pstmt.setLong(5, categoryId);
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            pstmt.setInt(6, userId);

            pstmt.executeUpdate();
            System.out.println("✅ Transaction added!");
        } catch (SQLException e) {
            System.err.println("Error adding transaction: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getTransactions() {
        Integer userId = getUserId();
        if (userId == null) return new ArrayList<>();

        List<Map<String, Object>> transactions = new ArrayList<>();
        String sql = """
            SELECT t.id, t.type, t.amount, t.date, t.description, c.name as category
            FROM transactions t
            LEFT JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ?
            ORDER BY t.date DESC
            LIMIT 50
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("id", rs.getInt("id"));
                transaction.put("type", rs.getString("type"));
                transaction.put("amount", rs.getDouble("amount"));
                transaction.put("date", rs.getString("date"));
                transaction.put("description", rs.getString("description"));
                transaction.put("category", rs.getString("category"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error getting transactions: " + e.getMessage());
        }
        return transactions;
    }

    public void updateTransaction(int id, Double newAmount, String newDescription) {
        Integer userId = getUserId();
        if (userId == null) return;

        StringBuilder sql = new StringBuilder("UPDATE transactions SET ");
        List<Object> params = new ArrayList<>();

        if (newAmount != null) {
            sql.append("amount = ?, ");
            params.add(newAmount);
        }
        if (newDescription != null) {
            sql.append("description = ?, ");
            params.add(newDescription);
        }

        if (params.isEmpty()) return;

        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ? AND user_id = ?");
        params.add(id);
        params.add(userId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                System.out.println("✅ Transaction updated!");
            } else {
                System.out.println("❌ Transaction not found or you don't have permission!");
            }
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
        }
    }

    public void deleteTransaction(int id) {
        Integer userId = getUserId();
        if (userId == null) return;

        String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            int deleted = pstmt.executeUpdate();
            if (deleted > 0) {
                System.out.println("✅ Transaction deleted!");
            } else {
                System.out.println("❌ Transaction not found or you don't have permission!");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
        }
    }

    public void addCategory(String name, String type) {
        Integer userId = getUserId();
        if (userId == null) return;

        String sql = "INSERT INTO categories (name, type, user_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, type.toUpperCase());
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
            System.out.println("✅ Category added!");
        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getCategories() {
        Integer userId = getUserId();
        if (userId == null) return new ArrayList<>();

        List<Map<String, Object>> categories = new ArrayList<>();
        String sql = """
            SELECT c.*, COUNT(t.id) as usage_count
            FROM categories c
            LEFT JOIN transactions t ON c.id = t.category_id AND t.user_id = ?
            WHERE c.user_id = ?
            GROUP BY c.id
            ORDER BY c.type, c.name
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> category = new HashMap<>();
                category.put("id", rs.getInt("id"));
                category.put("name", rs.getString("name"));
                category.put("type", rs.getString("type"));
                category.put("usage_count", rs.getInt("usage_count"));
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
        }
        return categories;
    }

    public List<Map<String, Object>> getCategoriesByType(String type) {
        Integer userId = getUserId();
        if (userId == null) return new ArrayList<>();

        List<Map<String, Object>> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM categories WHERE type = ? AND user_id = ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type.toUpperCase());
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> category = new HashMap<>();
                category.put("id", rs.getInt("id"));
                category.put("name", rs.getString("name"));
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories by type: " + e.getMessage());
        }
        return categories;
    }

    public void addGoal(String name, double targetAmount, LocalDate targetDate) {
        Integer userId = getUserId();
        if (userId == null) return;

        String sql = "INSERT INTO goals (name, target_amount, target_date, user_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setDouble(2, targetAmount);
            pstmt.setString(3, targetDate.toString());
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
            System.out.println("✅ Goal added!");
        } catch (SQLException e) {
            System.err.println("Error adding goal: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getGoals() {
        Integer userId = getUserId();
        if (userId == null) return new ArrayList<>();

        List<Map<String, Object>> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY target_date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> goal = new HashMap<>();
                goal.put("id", rs.getInt("id"));
                goal.put("name", rs.getString("name"));
                goal.put("target_amount", rs.getDouble("target_amount"));
                goal.put("current_amount", rs.getDouble("current_amount"));
                goal.put("target_date", rs.getString("target_date"));
                goals.add(goal);
            }
        } catch (SQLException e) {
            System.err.println("Error getting goals: " + e.getMessage());
        }
        return goals;
    }
}