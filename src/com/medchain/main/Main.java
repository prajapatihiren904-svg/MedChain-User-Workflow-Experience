package com.medchain.main;

import com.medchain.menu.*;
import com.medchain.util.DBConnection;
import com.medchain.util.InputValidator;
import java.sql.Connection;
import java.util.Scanner;

/**
 * Package: com.medchain.main
 * Purpose: Primary entry point. boots the console app, verifies the JDBC database link, and runs the root portal loop.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("         WELCOME TO MEDCHAIN PHARMACY             ");
        System.out.println("           NETWORK MANAGEMENT SYSTEM              ");
        System.out.println("==================================================");
        
        System.out.println(">> Initializing Database connection...");
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println(">> Database connection established successfully!");
            }
        } catch (Exception e) {
            System.err.println(">> ERROR: Failed to establish database connection. Make sure XAMPP MySQL is running.");
            System.err.println(">> Details: " + e.getMessage());
            System.out.println(">> Bootstrapping cancelled.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        CustomerMenu customerMenu = new CustomerMenu();
        PharmacyMenu pharmacyMenu = new PharmacyMenu();
        AdminMenu adminMenu = new AdminMenu();
        CourierMenu courierMenu = new CourierMenu();

        while (true) {
            System.out.println("\n===== MEDCHAIN ROOT PORTAL =====");
            System.out.println("1. Customer Portal");
            System.out.println("2. Pharmacy Owner Portal");
            System.out.println("3. System Admin Portal");
            System.out.println("4. Courier Portal");
            System.out.println("5. Exit System");
            System.out.print("Select your portal: ");
            
            String input = scanner.nextLine();
            int choice = InputValidator.validateMenuChoice(input, 1, 5);
            if (choice == -1) {
                System.out.println(">> Invalid selection. Please enter a number between 1 and 5.");
                continue;
            }

            switch (choice) {
                case 1:
                    customerMenu.showPreLoginMenu(scanner);
                    break;
                case 2:
                    pharmacyMenu.showPreLoginMenu(scanner);
                    break;
                case 3:
                    adminMenu.showLoginMenu(scanner);
                    break;
                case 4:
                    courierMenu.showLoginMenu(scanner);
                    break;
                case 5:
                    System.out.println("\n>> Thank you for using MedChain. Shutting down.");
                    System.out.println("==================================================");
                    scanner.close();
                    System.exit(0);
            }
        }
    }
}
