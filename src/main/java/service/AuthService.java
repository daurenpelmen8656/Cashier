package service;

import database.DatabaseConnection;
import java.sql.*;
import java.util.Scanner;

public class AuthService {
    private static Integer currentUserId = null;
    private static String currentUsername = null;
    private static boolean isAdmin = false;

    public boolean register(String username, String password, String email) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return false;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("❌ Username already exists!");
                    return false;
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, email);
                insertStmt.executeUpdate();

                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    currentUserId = generatedKeys.getInt(1);
                    currentUsername = username;
                    isAdmin = username.equals("admin");
                    System.out.println("✅ Registration successful!");

                    createDefaultCategories(conn, currentUserId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Registration failed: " + e.getMessage());
        }
        return false;
    }

    public boolean login(String username, String password) {
        String sql = "SELECT id, password FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (dbPassword.equals(password)) {
                    currentUserId = rs.getInt("id");
                    currentUsername = username;
                    isAdmin = username.equals("admin");
                    System.out.println("✅ Login successful! Welcome, " + username);
                    return true;
                } else {
                    System.out.println("❌ Incorrect password!");
                }
            } else {
                System.out.println("❌ User not found!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Login failed: " + e.getMessage());
        }
        return false;
    }

    public void logout() {
        currentUserId = null;
        currentUsername = null;
        isAdmin = false;
        System.out.println("✅ Logged out successfully!");
    }

    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    // АДМИН ФУНКЦИИ

    public boolean resetUserPassword(Scanner scanner) {
        if (!isAdmin) {
            System.out.println("❌ Admin privileges required!♾");
            return false;
        }

        System.out.print("Enter username to reset password: ");
        String username = scanner.nextLine();

        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        String sql = "UPDATE users SET password = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            int updated = pstmt.executeUpdate();

            if (updated > 0) {
                System.out.println("✅ Password reset successfully for user: " + username);
                return true;
            } else {
                System.out.println("❌ User not found: " + username);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }

    public void listAllUsers() {
        if (!isAdmin) {
            System.out.println("❌ Admin privileges required!");
            return;
        }

        String sql = "SELECT id, username, email, created_at FROM users ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== ALL USERS ===");
            System.out.printf("%-4s %-20s %-30s %s\n", "ID", "Username", "Email", "Created");
            System.out.println("-".repeat(70));

            while (rs.next()) {
                System.out.printf("%-4d %-20s %-30s %s\n",
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email") != null ? rs.getString("email") : "-",
                        rs.getString("created_at"));
            }
        } catch (SQLException e) {
            System.err.println("Error listing users: " + e.getMessage());
        }
    }

    public void viewUserTransactions(int userId) {
        if (!isAdmin) {
            System.out.println("❌ Admin privileges required!");
            return;
        }

        String sql = """
            SELECT t.id, t.type, t.amount, t.date, t.description, c.name as category, u.username
            FROM transactions t
            LEFT JOIN categories c ON t.category_id = c.id
            JOIN users u ON t.user_id = u.id
            WHERE t.user_id = ?
            ORDER BY t.date DESC
            LIMIT 20
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n=== USER TRANSACTIONS (User ID: " + userId + ") ===");
            System.out.printf("%-6s %-10s %-10s %-12s %-20s %-15s %s\n",
                    "ID", "Type", "Amount", "Date", "Description", "Category", "User");
            System.out.println("-".repeat(90));

            while (rs.next()) {
                System.out.printf("%-6d %-10s $%-9.2f %-12s %-20s %-15s %s\n",
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("date"),
                        truncate(rs.getString("description"), 18),
                        rs.getString("category") != null ? rs.getString("category") : "-",
                        rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Error viewing user transactions: " + e.getMessage());
        }
    }

    private void createDefaultCategories(Connection conn, int userId) throws SQLException {
        String[] defaultCategories = {
                "INSERT OR IGNORE INTO categories (name, type, user_id) VALUES ('Salary', 'INCOME', " + userId + ")",
                "INSERT OR IGNORE INTO categories (name, type, user_id) VALUES ('Freelance', 'INCOME', " + userId + ")",
                "INSERT OR IGNORE INTO categories (name, type, user_id) VALUES ('Food', 'EXPENSE', " + userId + ")",
                "INSERT OR IGNORE INTO categories (name, type, user_id) VALUES ('Transport', 'EXPENSE', " + userId + ")",
                "INSERT OR IGNORE INTO categories (name, type, user_id) VALUES ('Entertainment', 'EXPENSE', " + userId + ")"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : defaultCategories) {
                stmt.execute(sql);
            }
        }
    }

    private String truncate(String text, int length) {
        if (text == null) return "";
        return text.length() > length ? text.substring(0, length - 3) + "..." : text;
    }
}