package com.medchain.menu;

import com.medchain.dao.AreaDao;
import com.medchain.dao.MedicineDao;
import com.medchain.dao.OrderDao;
import com.medchain.exception.DuplicateUserException;
import com.medchain.exception.InvalidLoginException;
import com.medchain.exception.PharmacyNotApprovedException;
import com.medchain.model.*;
import com.medchain.report.SalesReportGenerator;
import com.medchain.service.InventoryService;
import com.medchain.service.OrderService;
import com.medchain.service.UserService;
import com.medchain.util.DateUtil;
import com.medchain.util.InputValidator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Package: com.medchain.menu
 * Purpose: Console-based menu handling Pharmacy registration, logins, inventory, sales reporting and BST expiry tracking.
 */
public class PharmacyMenu {
    private final UserService userService = new UserService();
    private final InventoryService inventoryService = new InventoryService();
    private final OrderService orderService = new OrderService();
    private final AreaDao areaDao = new AreaDao();
    private final MedicineDao medicineDao = new MedicineDao();
    private final OrderDao orderDao = new OrderDao();

    public void showPreLoginMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Pharmacy Portal ---");
            System.out.println("1. Register New Pharmacy");
            System.out.println("2. Log In Owner Account");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter choice: ");
            String input = scanner.nextLine();
            int choice = InputValidator.validateMenuChoice(input, 1, 3);
            if (choice == -1) {
                System.out.println(">> Invalid option! Try again.");
                continue;
            }

            switch (choice) {
                case 1:
                    registerPharmacy(scanner);
                    break;
                case 2:
                    loginPharmacy(scanner);
                    break;
                case 3:
                    return;
            }
        }
    }

    private void registerPharmacy(Scanner scanner) {
        System.out.println("\n--- Pharmacy Registration ---");
        
        String name = "";
        while (name.isEmpty()) {
            System.out.print("Enter Pharmacy Name (or 'q' to cancel): ");
            name = scanner.nextLine().trim();
            if (name.equalsIgnoreCase("q")) {
                System.out.println(">> Registration cancelled.");
                return;
            }
            if (name.isEmpty()) {
                System.out.println(">> Pharmacy Name cannot be empty.");
            }
        }

        String ownerName = "";
        while (ownerName.isEmpty()) {
            System.out.print("Enter Owner Full Name (or 'q' to cancel): ");
            ownerName = scanner.nextLine().trim();
            if (ownerName.equalsIgnoreCase("q")) {
                System.out.println(">> Registration cancelled.");
                return;
            }
            if (ownerName.isEmpty()) {
                System.out.println(">> Owner Name cannot be empty.");
            }
        }

        String email = "";
        while (true) {
            System.out.print("Enter Business Email (or 'q' to cancel): ");
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

        // List Areas
        List<Area> areas = areaDao.getAllAreas();
        System.out.println("\nSelect Geographical Area:");
        for (Area area : areas) {
            System.out.printf("  %d. %s\n", area.getAreaId(), area.getAreaName());
        }
        
        int areaId = -1;
        while (areaId == -1) {
            System.out.print("Enter Area ID (or 'q' to cancel): ");
            String areaInput = scanner.nextLine().trim();
            if (areaInput.equalsIgnoreCase("q")) {
                System.out.println(">> Registration cancelled.");
                return;
            }
            areaId = InputValidator.validateMenuChoice(areaInput, 1, areas.size());
            if (areaId == -1) {
                System.out.println(">> Invalid Area selection. Please choose from the list.");
            }
        }

        String address = "";
        while (address.isEmpty()) {
            System.out.print("Enter Local Shop Address (or 'q' to cancel): ");
            address = scanner.nextLine().trim();
            if (address.equalsIgnoreCase("q")) {
                System.out.println(">> Registration cancelled.");
                return;
            }
            if (address.isEmpty()) {
                System.out.println(">> Address cannot be empty.");
            }
        }

        try {
            userService.registerPharmacy(name, ownerName, email, password, areaId, address);
            System.out.println(">> Pharmacy registered successfully! Account is PENDING approval from System Admin.");
        } catch (DuplicateUserException e) {
            System.out.println(">> Registration Failed: " + e.getMessage());
        }
    }

    private void loginPharmacy(Scanner scanner) {
        System.out.println("\n--- Pharmacy Owner Login ---");
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        try {
            PharmacyOwner owner = userService.loginPharmacy(email, password);
            System.out.println(">> Access Granted. Welcome, " + owner.getPharmacyName());
            showPostLoginMenu(scanner, owner);
        } catch (InvalidLoginException e) {
            System.out.println(">> Login Failed: " + e.getMessage());
        } catch (PharmacyNotApprovedException e) {
            System.out.println(">> Access Denied: " + e.getMessage());
        }
    }

    private void showPostLoginMenu(Scanner scanner, PharmacyOwner owner) {
        while (true) {
            System.out.printf("\n=== Pharmacy Admin Panel (%s) ===\n", owner.getPharmacyName());
            System.out.println("1. View Current Inventory");
            System.out.println("2. Add Stock (New Batch)");
            System.out.println("3. Update Stock Quantity");
            System.out.println("4. Delete Inventory Batch");
            System.out.println("5. View Expiring Medicines (BST Sorted)");
            System.out.println("6. View Pharmacy Orders");
            System.out.println("7. Manage Pending Orders");
            System.out.println("8. Generate Pharmacy Sales Report");
            System.out.println("9. Logout");
            System.out.print("Enter choice: ");
            String input = scanner.nextLine();
            int choice = InputValidator.validateMenuChoice(input, 1, 9);
            if (choice == -1) {
                System.out.println(">> Invalid option! Try again.");
                continue;
            }

            switch (choice) {
                case 1:
                    viewInventory(owner);
                    break;
                case 2:
                    addInventoryBatch(scanner, owner);
                    break;
                case 3:
                    updateInventoryStock(scanner, owner);
                    break;
                case 4:
                    deleteInventoryBatch(scanner, owner);
                    break;
                case 5:
                    viewExpiringMedicines(owner);
                    break;
                case 6:
                    viewOrders(owner);
                    break;
                case 7:
                    managePendingOrders(scanner, owner);
                    break;
                case 8:
                    generateSalesReport(owner);
                    break;
                case 9:
                    System.out.println(">> Logged out from pharmacy owner session.");
                    return;
            }
        }
    }

    private void viewInventory(PharmacyOwner owner) {
        List<Inventory> list = inventoryService.getInventoryByPharmacy(owner.getId());
        if (list.isEmpty()) {
            System.out.println(">> Inventory is currently empty.");
        } else {
            System.out.println("\n-----------------------------------------------------------------------------------------------------");
            System.out.printf("%-5s %-30s %-12s %-12s %-8s %-10s %-10s\n", "ID", "Medicine Name", "Batch", "Expiry Date", "Qty", "Cost", "Price");
            System.out.println("-----------------------------------------------------------------------------------------------------");
            for (Inventory inv : list) {
                System.out.printf("%-5d %-30s %-12s %-12s %-8d %-10.2f %-10.2f\n", 
                        inv.getInventoryId(), inv.getMedicineName(), inv.getBatchNumber(), inv.getExpiryDate().toString(), 
                        inv.getStockQuantity(), inv.getPurchasePrice(), inv.getSellingPrice());
            }
            System.out.println("-----------------------------------------------------------------------------------------------------");
        }
    }

    private void addInventoryBatch(Scanner scanner, PharmacyOwner owner) {
        System.out.print("\nEnter Medicine ID to add: ");
        String medInput = scanner.nextLine();
        try {
            int medId = Integer.parseInt(medInput.trim());
            Medicine med = medicineDao.getMedicineById(medId);
            if (med == null) {
                System.out.println(">> Medicine not found in catalog. Ask Admin to register it first.");
                return;
            }

            System.out.print("Enter Batch Number (e.g. BAT-091): ");
            String batch = scanner.nextLine().trim();
            System.out.print("Enter Expiry Date (yyyy-MM-dd): ");
            String dateStr = scanner.nextLine().trim();
            LocalDate expiry = DateUtil.parseDate(dateStr);
            if (expiry == null) {
                System.out.println(">> Invalid Date format. Must be yyyy-MM-dd.");
                return;
            }

            System.out.print("Enter Quantity: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Purchase Price (Cost): ");
            double buyPrice = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Enter Selling Price: ");
            double sellPrice = Double.parseDouble(scanner.nextLine().trim());

            inventoryService.addInventoryItem(owner.getId(), medId, batch, expiry, qty, buyPrice, sellPrice);
            System.out.println(">> Batch added to inventory successfully.");
        } catch (NumberFormatException e) {
            System.out.println(">> Invalid number input. Operation failed.");
        } catch (IllegalArgumentException e) {
            System.out.println(">> Error: " + e.getMessage());
        }
    }

    private void updateInventoryStock(Scanner scanner, PharmacyOwner owner) {
        viewInventory(owner);
        System.out.print("Enter Inventory Record ID to update: ");
        String idInput = scanner.nextLine();
        System.out.print("Enter New Stock Quantity: ");
        String qtyInput = scanner.nextLine();
        try {
            int invId = Integer.parseInt(idInput.trim());
            int newQty = Integer.parseInt(qtyInput.trim());
            if (newQty < 0) {
                System.out.println(">> Quantity cannot be negative.");
                return;
            }

            // Ensure this inventory row belongs to this pharmacy
            List<Inventory> mine = inventoryService.getInventoryByPharmacy(owner.getId());
            boolean matches = false;
            for (Inventory inv : mine) {
                if (inv.getInventoryId() == invId) {
                    matches = true;
                    break;
                }
            }

            if (!matches) {
                System.out.println(">> Invalid inventory ID. You do not own this stock record.");
                return;
            }

            inventoryService.updateStock(invId, newQty);
            System.out.println(">> Stock quantity updated successfully!");
        } catch (NumberFormatException e) {
            System.out.println(">> Invalid numeric format.");
        }
    }

    private void deleteInventoryBatch(Scanner scanner, PharmacyOwner owner) {
        System.out.print("\nEnter Medicine ID of batch to delete: ");
        int medId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter Batch Number: ");
        String batch = scanner.nextLine().trim();

        boolean deleted = inventoryService.removeInventoryBatch(owner.getId(), medId, batch);
        if (deleted) {
            System.out.println(">> Batch removed from inventory.");
        } else {
            System.out.println(">> Batch not found. Deletion cancelled.");
        }
    }

    private void viewExpiringMedicines(PharmacyOwner owner) {
        // [CONCEPT: DS BST Usage - Displays sorted medicines using BST in-order traversal]
        List<Inventory> sorted = inventoryService.getExpiringMedicinesSorted(owner.getId());
        if (sorted.isEmpty()) {
            System.out.println(">> No medicines in stock to check.");
        } else {
            System.out.println("\n--- Expiring Medicines (Sorted Nearest-Expiry-First via BST) ---");
            System.out.println("---------------------------------------------------------------------------------------------");
            System.out.printf("%-30s %-12s %-15s %-10s %-12s\n", "Medicine Name", "Batch", "Expiry Date", "Stock", "Status");
            System.out.println("---------------------------------------------------------------------------------------------");
            LocalDate today = LocalDate.now();
            for (Inventory inv : sorted) {
                String status = "Safe";
                if (inv.getExpiryDate().isBefore(today)) {
                    status = "EXPIRED";
                } else if (inv.getExpiryDate().isBefore(today.plusMonths(3))) {
                    status = "WARNING (<3m)";
                }
                System.out.printf("%-30s %-12s %-15s %-10d %-12s\n", 
                        inv.getMedicineName(), inv.getBatchNumber(), inv.getExpiryDate().toString(), inv.getStockQuantity(), status);
            }
            System.out.println("---------------------------------------------------------------------------------------------");
        }
    }

    private void viewOrders(PharmacyOwner owner) {
        List<Order> list = orderDao.getOrdersByPharmacy(owner.getId());
        if (list.isEmpty()) {
            System.out.println(">> No orders found for your pharmacy.");
        } else {
            System.out.println("\n---------------------------------------------------------------------------");
            System.out.printf("%-10s %-25s %-25s %-15s\n", "Order ID", "Customer Name", "Order Date", "Status");
            System.out.println("---------------------------------------------------------------------------");
            for (Order o : list) {
                System.out.printf("%-10d %-25s %-25s %-15s\n", 
                        o.getOrderId(), o.getCustomerName(), o.getOrderDate().toString(), o.getOrderStatus());
            }
            System.out.println("---------------------------------------------------------------------------");
        }
    }

    private void generateSalesReport(PharmacyOwner owner) {
        System.out.println(">> Generating Sales report file...");
        SalesReportGenerator.generate(owner.getId());
        System.out.println(">> Report generated in 'reports/' folder.");
    }

    private void managePendingOrders(Scanner scanner, PharmacyOwner owner) {
        List<Order> list = orderDao.getOrdersByPharmacy(owner.getId());
        List<Order> pending = new ArrayList<>();
        for (Order o : list) {
            if ("PLACED".equals(o.getOrderStatus())) {
                pending.add(o);
            }
        }

        if (pending.isEmpty()) {
            System.out.println(">> No pending orders to approve for your pharmacy.");
            return;
        }

        System.out.println("\n--- Pending Orders for Approval ---");
        System.out.println("---------------------------------------------------------------------------");
        System.out.printf("%-10s %-25s %-25s %-15s\n", "Order ID", "Customer Name", "Order Date", "Status");
        System.out.println("---------------------------------------------------------------------------");
        for (Order o : pending) {
            System.out.printf("%-10d %-25s %-25s %-15s\n", 
                    o.getOrderId(), o.getCustomerName(), o.getOrderDate().toString(), o.getOrderStatus());
        }
        System.out.println("---------------------------------------------------------------------------");

        System.out.print("Enter Order ID to process (or 'q' to go back): ");
        String ordInput = scanner.nextLine().trim();
        if (ordInput.equalsIgnoreCase("q")) {
            return;
        }

        try {
            int orderId = Integer.parseInt(ordInput);
            Order chosen = null;
            for (Order o : pending) {
                if (o.getOrderId() == orderId) {
                    chosen = o;
                    break;
                }
            }

            if (chosen == null) {
                System.out.println(">> Invalid Order ID. Must select from the pending list.");
                return;
            }

            System.out.print("Approve or Reject order? (a/r): ");
            String decision = scanner.nextLine().trim().toLowerCase();
            if (decision.equals("a") || decision.equals("approve")) {
                boolean success = orderService.approveOrder(orderId);
                if (success) {
                    System.out.println(">> Order successfully APPROVED and FULFILLED!");
                } else {
                    System.out.println(">> Order processing failed.");
                }
            } else if (decision.equals("r") || decision.equals("reject")) {
                orderService.rejectOrder(orderId);
                System.out.println(">> Order successfully REJECTED and CANCELLED.");
            } else {
                System.out.println(">> Invalid choice. Operation cancelled.");
            }
        } catch (NumberFormatException e) {
            System.out.println(">> Invalid numeric format.");
        } catch (Exception e) {
            System.out.println(">> Error processing order: " + e.getMessage());
        }
    }
}
