package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void init() {
        String[] createTables = {
                """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(50) NOT NULL,
                email VARCHAR(100),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS categories (
                id SERIAL PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                type VARCHAR(10) CHECK (type IN ('INCOME', 'EXPENSE')) NOT NULL,
                user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                UNIQUE(name, user_id)
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS transactions (
                id SERIAL PRIMARY KEY,
                type VARCHAR(10) CHECK (type IN ('INCOME', 'EXPENSE')) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                date DATE NOT NULL DEFAULT CURRENT_DATE,
                description TEXT,
                category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
                user_id INTEGER REFERENCES users(id) ON DELETE CASCADE
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS goals (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                target_amount DECIMAL(10,2) NOT NULL,
                current_amount DECIMAL(10,2) DEFAULT 0,
                target_date DATE NOT NULL,
                user_id INTEGER REFERENCES users(id) ON DELETE CASCADE
            )
            """
        };

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;

            try (Statement stmt = conn.createStatement()) {
                // 1. Создаём таблицы
                System.out.println("Creating tables...");
                for (String sql : createTables) {
                    try {
                        stmt.execute(sql);
                    } catch (Exception e) {
                        System.out.println("Warning: " + e.getMessage());
                    }
                }

                // 2. Проверяем и добавляем недостающие колонки если нужно
                addMissingColumns(conn);

                // 3. Проверяем есть ли пользователи
                String checkUsers = "SELECT COUNT(*) FROM users";
                var rs = stmt.executeQuery(checkUsers);
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

    private static void addMissingColumns(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Проверяем и добавляем user_id в categories если нет
            if (!columnExists(conn, "categories", "user_id")) {
                System.out.println("Adding user_id column to categories table...");
                stmt.execute("ALTER TABLE categories ADD COLUMN user_id INTEGER REFERENCES users(id) ON DELETE CASCADE");
                stmt.execute("ALTER TABLE categories ADD CONSTRAINT categories_name_user_unique UNIQUE (name, user_id)");
            }

            // Проверяем и добавляем user_id в transactions если нет
            if (!columnExists(conn, "transactions", "user_id")) {
                System.out.println("Adding user_id column to transactions table...");
                stmt.execute("ALTER TABLE transactions ADD COLUMN user_id INTEGER REFERENCES users(id) ON DELETE CASCADE");
            }

            // Проверяем и добавляем user_id в goals если нет
            if (!columnExists(conn, "goals", "user_id")) {
                System.out.println("Adding user_id column to goals table...");
                stmt.execute("ALTER TABLE goals ADD COLUMN user_id INTEGER REFERENCES users(id) ON DELETE CASCADE");
            }

        } catch (Exception e) {
            System.out.println("Warning while adding columns: " + e.getMessage());
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, columnName);
            return columns.next();
        } catch (Exception e) {
            return false;
        }
    }
}