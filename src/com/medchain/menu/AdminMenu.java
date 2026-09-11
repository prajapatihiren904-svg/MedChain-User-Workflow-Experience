package com.medchain.menu;

import com.medchain.dao.*;
import com.medchain.exception.DuplicateUserException;
import com.medchain.exception.InvalidLoginException;
import com.medchain.model.*;
import com.medchain.report.AuditReportGenerator;
import com.medchain.service.PharmacyService;
import com.medchain.service.UserService;
import com.medchain.util.InputValidator;
import com.medchain.util.PasswordUtil;
import java.util.List;
import java.util.Scanner;

/**
 * Package: com.medchain.menu
 * Purpose: Console-based menu handling Admin actions like Approvals, Graph creation, Catalog entry, and Audits.
 */
public class AdminMenu {
    private final UserService userService = new UserService();
    private final PharmacyService pharmacyService = new PharmacyService();
    private final AreaDao areaDao = new AreaDao();
    private final AreaConnectionDao areaConnectionDao = new AreaConnectionDao();
    private final MedicineDao medicineDao = new MedicineDao();
    private final CourierDao courierDao = new CourierDao();
    private final OrderDao orderDao = new OrderDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final TransferDao transferDao = new TransferDao();

    public void showLoginMenu(Scanner scanner) {
        System.out.println("\n--- Admin Login ---");
        System.out.print("Enter Admin Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        try {
            Admin admin = userService.loginAdmin(email, password);
            System.out.println(">> Access Granted. Welcome, " + admin.getFullName());
            showPostLoginMenu(scanner);
        } catch (InvalidLoginException e) {
            System.out.println(">> Login Failed: " + e.getMessage());
        }
    }

    private void showPostLoginMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== Admin Panel ===");
            System.out.println("1. Manage Pharmacy Registration Approvals");
            System.out.println("2. Add New Area");
            System.out.println("3. Add Area Connection (Graph Road Link)");
            System.out.println("4. Register New Medicine (Catalog)");
            System.out.println("5. Register New Courier");
            System.out.println("6. View Customer List");
            System.out.println("7. View System-wide Order Details");
            System.out.println("8. View Active Transfers Queue");
            System.out.println("9. Generate System-wide Audit Report");
            System.out.println("10. View Trigger-populated Order Audit Logs");
            System.out.println("11. Logout");
            System.out.print("Enter choice: ");
            String input = scanner.nextLine();
            int choice = InputValidator.validateMenuChoice(input, 1, 11);
            if (choice == -1) {
                System.out.println(">> Invalid option! Try again.");
                continue;
            }

            switch (choice) {
                case 1:
                    managePharmacyApprovals(scanner);
                    break;
                case 2:
                    addNewArea(scanner);
                    break;
                case 3:
                    addConnection(scanner);
                    break;
                case 4:
                    addNewMedicine(scanner);
                    break;
                case 5:
                    registerCourier(scanner);
                    break;
                case 6:
                    viewCustomers();
                    break;
                case 7:
                    viewAllOrders();
                    break;
                case 8:
                    viewAllTransfers();
                    break;
                case 9:
                    generateSystemAudit();
                    break;
                case 10:
                    viewOrderAuditLogs();
                    break;
                case 11:
                    System.out.println(">> Logged out from admin portal.");
                    return;
            }
        }
    }

    private void managePharmacyApprovals(Scanner scanner) {
        while (true) {
            List<PharmacyOwner> pending = pharmacyService.getPendingPharmacies();
            System.out.println("\n--- Pending Pharmacy Approvals ---");
            if (pending.isEmpty()) {
                System.out.println("No pharmacies awaiting approval.");
                return;
            }

            System.out.println("-------------------------------------------------------------------------------------");
            System.out.printf("%-5s %-25s %-20s %-25s\n", "ID", "Pharmacy Name", "Owner Name", "Email");
            System.out.println("-------------------------------------------------------------------------------------");
            for (PharmacyOwner p : pending) {
                System.out.printf("%-5d %-25s %-20s %-25s\n", p.getId(), p.getPharmacyName(), p.getFullName(), p.getEmail());
            }
            System.out.println("-------------------------------------------------------------------------------------");

            System.out.print("Enter Pharmacy ID to approve/reject (0 to go back): ");
            String pInput = scanner.nextLine();
            try {
                int id = Integer.parseInt(pInput.trim());
                if (id == 0) return;

                // Validate matching pending pharmacy
                boolean found = false;
                for (PharmacyOwner po : pending) {
                    if (po.getId() == id) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.out.println(">> Pharmacy ID not in pending queue.");
                    continue;
                }

                System.out.print("Approve or Reject? (A/R): ");
                String action = scanner.nextLine().trim().toUpperCase();
                if ("A".equals(action)) {
                    pharmacyService.approvePharmacy(id);
                    System.out.println(">> Pharmacy ID " + id + " has been APPROVED.");
                } else if ("R".equals(action)) {
                    pharmacyService.rejectPharmacy(id);
                    System.out.println(">> Pharmacy ID " + id + " has been REJECTED.");
                } else {
                    System.out.println(">> Invalid action choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println(">> Invalid pharmacy ID numeric format.");
            }
        }
    }

    private void addNewArea(Scanner scanner) {
        System.out.print("\nEnter new Area Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println(">> Area name cannot be empty.");
            return;
        }

        try {
            Area area = areaDao.insertArea(name);
            if (area != null) {
                System.out.printf(">> Area '%s' created successfully with ID %d!\n", area.getAreaName(), area.getAreaId());
            }
        } catch (Exception e) {
            System.out.println(">> Error adding area: " + e.getMessage());
        }
    }

    private void addConnection(Scanner scanner) {
        List<Area> areas = areaDao.getAllAreas();
        System.out.println("\nSelect Source and Target Areas:");
        for (Area area : areas) {
            System.out.printf("  %d. %s\n", area.getAreaId(), area.getAreaName());
        }

        try {
            System.out.print("Enter Source Area ID: ");
            int a1 = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Target Area ID: ");
            int a2 = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Road Distance (in km): ");
            double distance = Double.parseDouble(scanner.nextLine().trim());

            if (distance <= 0) {
                System.out.println(">> Distance must be greater than zero.");
                return;
            }

            boolean success = areaConnectionDao.insertConnection(a1, a2, distance);
            if (success) {
                System.out.printf(">> Connection created between area %d and %d of %.2f km.\n", a1, a2, distance);
            }
        } catch (NumberFormatException e) {
            System.out.println(">> Invalid numeric format inputs.");
        } catch (Exception e) {
            System.out.println(">> Connection creation failed (Check duplicates or invalid IDs).");
        }
    }

    private void addNewMedicine(Scanner scanner) {
        System.out.print("\nEnter Medicine Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Manufacturer Name: ");
        String manuf = scanner.nextLine().trim();
        System.out.print("Enter Category (e.g. Antibiotic): ");
        String category = scanner.nextLine().trim();

        if (name.isEmpty() || manuf.isEmpty()) {
            System.out.println(">> Medicine Name and Manufacturer cannot be empty.");
            return;
        }

        Medicine med = new Medicine(0, name, manuf, category);
        med = medicineDao.insertMedicine(med);
        if (med != null) {
            System.out.printf(">> Medicine '%s' registered with ID %d!\n", med.getMedicineName(), med.getMedicineId());
        }
    }

    private void registerCourier(Scanner scanner) {
        System.out.println("\n--- Register Courier ---");
        
        String name = "";
        while (name.isEmpty()) {
            System.out.print("Enter Full Name (or 'q' to cancel): ");
            name = scanner.nextLine().trim();
            if (name.equalsIgnoreCase("q")) {
                System.out.println(">> Registration cancelled.");
                return;
            }
            if (name.isEmpty()) {
                System.out.println(">> Name cannot be empty.");
            }
        }

        String email = "";
        while (true) {
            System.out.print("Enter Email Address (or 'q' to cancel): ");
            email = scanner.nextLine().trim();
            if (email.equalsIgnoreCase("q")) {
                System.out.println(">> Registration cancelled.");
                return;
            }
            if (InputValidator.isValidEmail(email)) {
                break;
            }
            System.out.println(">> Invalid Email format.");
        }

        String password = "";
        while (true) {
            System.out.print("Enter Password (at least 6 characters with 1 number, or 'q' to cancel): ");
            password = scanner.nextLine().trim();
            if (password.equalsIgnoreCase("q")) {
                System.out.println(">> Registration cancelled.");
                return;
            }
            if (InputValidator.isValidPassword(password)) {
                break;
            }
            System.out.println(">> Password must be at least 6 characters, contain at least one number, and not be entirely numeric.");
        }

        String phone = "";
        while (true) {
            System.out.print("Enter Phone Number (or 'q' to cancel): ");
            phone = scanner.nextLine().trim();
            if (phone.equalsIgnoreCase("q")) {
                System.out.println(">> Registration cancelled.");
                return;
            }
            if (InputValidator.isValidPhone(phone)) {
                break;
            }
            System.out.println(">> Invalid Phone number (must be 10 digits starting with 6-9).");
        }

        try {
            String hash = PasswordUtil.hashPassword(password);
            Courier courier = new Courier(0, name, email, hash, phone, "AVAILABLE");
            courierDao.insertCourier(courier);
            System.out.println(">> Courier successfully registered and set to AVAILABLE!");
        } catch (DuplicateUserException e) {
            System.out.println(">> Registration Failed: " + e.getMessage());
        }
    }

    private void viewCustomers() {
        List<Customer> list = customerDao.getAllCustomers();
        if (list.isEmpty()) {
            System.out.println(">> No customers registered in system.");
        } else {
            System.out.println("\n-----------------------------------------------------------------------------------------------");
            System.out.printf("%-5s %-25s %-25s %-12s %-10s\n", "ID", "Customer Name", "Email", "Phone", "Home Area");
            System.out.println("-----------------------------------------------------------------------------------------------");
            for (Customer c : list) {
                System.out.printf("%-5d %-25s %-25s %-12s %-10d\n", c.getId(), c.getFullName(), c.getEmail(), c.getPhone(), c.getHomeAreaId());
            }
            System.out.println("-----------------------------------------------------------------------------------------------");
        }
    }

    private void viewAllOrders() {
        List<Order> list = orderDao.getOrdersWithDetails();
        if (list.isEmpty()) {
            System.out.println(">> No orders placed in the system.");
        } else {
            System.out.println("\n---------------------------------------------------------------------------------------------");
            System.out.printf("%-10s %-25s %-25s %-25s %-20s\n", "Order ID", "Customer Name", "Pharmacy Name", "Order Date", "Status");
            System.out.println("---------------------------------------------------------------------------------------------");
            for (Order o : list) {
                System.out.printf("%-10d %-25s %-25s %-25s %-20s\n", 
                        o.getOrderId(), o.getCustomerName(), o.getPharmacyName(), o.getOrderDate().toString(), o.getOrderStatus());
            }
            System.out.println("---------------------------------------------------------------------------------------------");
        }
    }

    private void viewAllTransfers() {
        List<Transfer> list = transferDao.getAllTransfers();
        if (list.isEmpty()) {
            System.out.println(">> No active inventory transfer routes recorded.");
        } else {
            System.out.println("\n--------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s %-10s %-25s %-25s %-15s %-15s %-10s\n", "Trans ID", "Order ID", "Source Pharmacy", "Dest Pharmacy", "Courier", "Distance", "Status");
            System.out.println("--------------------------------------------------------------------------------------------------------------");
            for (Transfer t : list) {
                String cName = t.getCourierName() != null ? t.getCourierName() : "UNASSIGNED";
                System.out.printf("%-10d %-10d %-25s %-25s %-15s %-15.2f %-10s\n", 
                        t.getTransferId(), t.getOrderId(), t.getSourcePharmacyName(), t.getDestinationPharmacyName(), cName, t.getDistanceKm(), t.getTransferStatus());
            }
            System.out.println("--------------------------------------------------------------------------------------------------------------");
        }
    }

    private void generateSystemAudit() {
        System.out.println(">> Compiling database stats and generating system audit file...");
        AuditReportGenerator.generate();
        System.out.println(">> Audit report generated inside 'reports/' folder.");
    }

    private void viewOrderAuditLogs() {
        System.out.println("\n--- Order Status Audit Logs (Populated by DBMS Trigger) ---");
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("%-8s %-10s %-25s %-25s %-25s\n", "Log ID", "Order ID", "Old Status", "New Status", "Changed At");
        System.out.println("-----------------------------------------------------------------------------------------");
        
        String sql = "SELECT log_id, order_id, old_status, new_status, changed_at FROM Order_Logs ORDER BY log_id DESC";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        
        try (java.sql.Connection conn = com.medchain.util.DBConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                int logId = rs.getInt("log_id");
                int orderId = rs.getInt("order_id");
                String oldStatus = rs.getString("old_status");
                String newStatus = rs.getString("new_status");
                java.sql.Timestamp changedAt = rs.getTimestamp("changed_at");
                String formattedDate = changedAt != null ? sdf.format(changedAt) : "N/A";
                
                System.out.printf("%-8d %-10d %-25s %-25s %-25s\n", 
                        logId, orderId, oldStatus, newStatus, formattedDate);
            }
            if (!found) {
                System.out.println(">> No status changes logged yet. Approve or cancel an order to test trigger!");
            }
            System.out.println("-----------------------------------------------------------------------------------------");
        } catch (java.sql.SQLException e) {
            System.out.println(">> Error querying audit logs: " + e.getMessage());
        }
    }
}
