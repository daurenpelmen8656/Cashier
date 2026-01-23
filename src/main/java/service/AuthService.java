package service;

import database.DatabaseConnection;
import java.sql.*;

public class AuthService {
    private static Integer currentUserId = null;
    private static String currentUsername = null;

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
                    System.out.println("✅ Registration successful! User ID: " + currentUserId);

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
        System.out.println("✅ Logged out successfully!");
    }

    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUsername() {
        return currentUsername;
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
}
