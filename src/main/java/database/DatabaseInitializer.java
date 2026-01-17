package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initializeDatabase() {
        String createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                type TEXT CHECK(type IN ('INCOME', 'EXPENSE')) NOT NULL
            );
            """;

        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT CHECK(type IN ('INCOME', 'EXPENSE')) NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                description TEXT,
                category_id INTEGER,
                FOREIGN KEY (category_id) REFERENCES categories(id)
            );
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createCategoriesTable);
            stmt.execute(createTransactionsTable);

            // Insert default categories if empty
            insertDefaultCategories(stmt);

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private static void insertDefaultCategories(Statement stmt) throws SQLException {
        String checkEmpty = "SELECT COUNT(*) FROM categories";
        var rs = stmt.executeQuery(checkEmpty);
        if (rs.getInt(1) == 0) {
            String insertDefaults = """
                INSERT INTO categories (name, type) VALUES 
                ('Salary', 'INCOME'),
                ('Freelance', 'INCOME'),
                ('Investment', 'INCOME'),
                ('Food', 'EXPENSE'),
                ('Transport', 'EXPENSE'),
                ('Entertainment', 'EXPENSE'),
                ('Rent', 'EXPENSE'),
                ('Utilities', 'EXPENSE');
                """;
            stmt.execute(insertDefaults);
        }
    }
}