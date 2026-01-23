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

            // Проверяем, существует ли пользователь
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("❌ Username already exists!");
                    return false;
                }
            }

            // Регистрируем нового пользователя
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password); // Без шифрования!
                insertStmt.setString(3, email);
                insertStmt.executeUpdate();

                // Получаем ID нового пользователя
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    currentUserId = generatedKeys.getInt(1);
                    currentUsername = username;
                    System.out.println("✅ Registration successful! User ID: " + currentUserId);

                    // Создаём категории по умолчанию для нового пользователя
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
                if (dbPassword.equals(password)) { // Простое сравнение, без шифрования
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
                "INSERT INTO categories (name, type, user_id) VALUES ('Salary', 'INCOME', ?) ON CONFLICT DO NOTHING",
                "INSERT INTO categories (name, type, user_id) VALUES ('Freelance', 'INCOME', ?) ON CONFLICT DO NOTHING",
                "INSERT INTO categories (name, type, user_id) VALUES ('Food', 'EXPENSE', ?) ON CONFLICT DO NOTHING",
                "INSERT INTO categories (name, type, user_id) VALUES ('Transport', 'EXPENSE', ?) ON CONFLICT DO NOTHING",
                "INSERT INTO categories (name, type, user_id) VALUES ('Entertainment', 'EXPENSE', ?) ON CONFLICT DO NOTHING"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : defaultCategories) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, userId);
                    pstmt.executeUpdate();
                }
            }
        }
    }
}