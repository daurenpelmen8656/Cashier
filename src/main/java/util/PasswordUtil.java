package util;

import java.io.Console;
import java.io.IOException;

public class PasswordUtil {

    public static String readPassword() {
        Console console = System.console();
        if (console != null) {
            char[] passwordArray = console.readPassword("Password: ");
            return new String(passwordArray);
        }

        // Fallback для IDE - просто запрашиваем пароль открыто
        System.out.print("Password: ");
        try {
            StringBuilder password = new StringBuilder();
            while (true) {
                int ch = System.in.read();
                if (ch == '\n' || ch == '\r') {
                    System.out.println();
                    break;
                } else if (ch == 8 || ch == 127) { // Backspace
                    if (password.length() > 0) {
                        password.deleteCharAt(password.length() - 1);
                    }
                } else if (ch >= 32 && ch <= 126) { // Printable chars
                    password.append((char) ch);
                    System.out.print("*");
                }
            }
            return password.toString();
        } catch (IOException e) {
            // Если возникает ошибка, используем простой ввод
            return readPasswordSimple();
        }
    }

    public static String readPasswordSimple() {
        System.out.print("Password: ");
        try {
            // Очищаем буфер System.in
            System.in.skip(System.in.available());
        } catch (IOException e) {
            // Игнорируем
        }

        // Простой ввод через Scanner
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        return scanner.nextLine();
    }
}