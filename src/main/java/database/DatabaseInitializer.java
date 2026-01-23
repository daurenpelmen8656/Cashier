package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void init() {
        String[] createTables = {
                """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                email TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT CHECK (type IN ('INCOME', 'EXPENSE')) NOT NULL,
                user_id INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                UNIQUE(name, user_id)
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT CHECK (type IN ('INCOME', 'EXPENSE')) NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL DEFAULT CURRENT_DATE,
                description TEXT,
                category_id INTEGER,
                user_id INTEGER NOT NULL,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                target_amount REAL NOT NULL,
                current_amount REAL DEFAULT 0,
                target_date TEXT NOT NULL,
                user_id INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """
        };

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;

            try (Statement stmt = conn.createStatement()) {
                // Создаём таблицы
                System.out.println("Creating tables...");
                for (String sql : createTables) {
                    stmt.execute(sql);
                }

                // Проверяем есть ли пользователи
                String checkUsers = "SELECT COUNT(*) FROM users";
                ResultSet rs = stmt.executeQuery(checkUsers);
                if (rs.next() && rs.getInt(1) == 0) {
                    String addAdmin = "INSERT INTO users (username, password, email) VALUES ('admin', 'admin123', 'admin@example.com')";
                    stmt.execute(addAdmin);
                    System.out.println("✅ Default admin user created (admin/admin123)");
                }

                System.out.println("✅ Database initialized successfully!");

            }
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed!");
            e.printStackTrace();
        }
    }
}