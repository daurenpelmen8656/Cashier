import database.DatabaseInitializer;
import ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        // Initialize database
        DatabaseInitializer.initializeDatabase();

        // Start console interface
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}