package com.medchain.menu;

import com.medchain.dao.AreaDao;
import com.medchain.dao.BillDao;
import com.medchain.dao.MedicineDao;
import com.medchain.dao.OrderDao;
import com.medchain.dao.PharmacyDao;
import com.medchain.dao.InventoryDao;
import com.medchain.exception.DuplicateUserException;
import com.medchain.exception.InvalidLoginException;
import com.medchain.exception.StockOutException;
import com.medchain.model.*;
import com.medchain.service.OrderService;
import com.medchain.service.UserService;
import com.medchain.util.InputValidator;
import java.util.List;
import java.util.Scanner;

/**
 * Package: com.medchain.menu
 * Purpose: Console-based menu handling Customer registrations, searches, history and ordering operations.
 */
public class CustomerMenu {
    private final UserService userService = new UserService();
    private final OrderService orderService = new OrderService();
    private final AreaDao areaDao = new AreaDao();
    private final PharmacyDao pharmacyDao = new PharmacyDao();
    private final MedicineDao medicineDao = new MedicineDao();
    private final OrderDao orderDao = new OrderDao();
    private final BillDao billDao = new BillDao();
    private final InventoryDao inventoryDao = new InventoryDao();

    public void showPreLoginMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Customer Portal ---");
            System.out.println("1. Register Account");
            System.out.println("2. Log In");
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
                    registerCustomer(scanner);
                    break;
                case 2:
                    loginCustomer(scanner);
                    break;
                case 3:
                    return;
            }
        }
    }

    private void registerCustomer(Scanner scanner) {
        System.out.println("\n--- Customer Registration ---");
        System.out.print("Enter Full Name: ");
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
            System.out.print("Enter Phone (10 digits, or 'q' to cancel): ");
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

        // List Areas
        List<Area> areas = areaDao.getAllAreas();
        System.out.println("\nSelect Home Area:");
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

        // List approved pharmacies for preference (Optional)
        List<PharmacyOwner> pharms = pharmacyDao.getAllPharmacies();
        System.out.println("\nSelect Preferred Pharmacy (Optional - Enter 0 to skip, or 'q' to cancel):");
        for (PharmacyOwner p : pharms) {
            if ("APPROVED".equals(p.getApprovalStatus())) {
                System.out.printf("  %d. %s (%s)\n", p.getId(), p.getPharmacyName(), p.getAddress());
            }
        }
        Integer prefId = null;
        while (true) {
            System.out.print("Enter Pharmacy ID or 0: ");
            String prefInput = scanner.nextLine().trim();
            if (prefInput.equalsIgnoreCase("q")) {
                System.out.println(">> Registration cancelled.");
                return;
            }
            try {
                int pId = Integer.parseInt(prefInput);
                if (pId == 0) {
                    break;
                }
                PharmacyOwner test = pharmacyDao.getPharmacyById(pId);
                if (test != null && "APPROVED".equals(test.getApprovalStatus())) {
                    prefId = pId;
                    break;
                } else {
                    System.out.println(">> Pharmacy not approved or does not exist. Choose another or enter 0.");
                }
            } catch (NumberFormatException e) {
                System.out.println(">> Please enter a valid number or 0.");
            }
        }

        try {
            userService.registerCustomer(name, email, password, phone, areaId, prefId);
            System.out.println(">> Customer registered successfully! You can now log in.");
        } catch (DuplicateUserException e) {
            System.out.println(">> Registration Failed: " + e.getMessage());
        }
    }

    private void loginCustomer(Scanner scanner) {
        System.out.println("\n--- Customer Login ---");
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        try {
            Customer customer = userService.loginCustomer(email, password);
            System.out.println(">> Logged in successfully! Welcome, " + customer.getFullName());
            showPostLoginMenu(scanner, customer);
        } catch (InvalidLoginException e) {
            System.out.println(">> Login Failed: " + e.getMessage());
        }
    }

    private void showPostLoginMenu(Scanner scanner, Customer customer) {
        while (true) {
            System.out.printf("\n=== Customer Menu (%s) ===\n", customer.getFullName());
            System.out.println("1. Search Medicines");
            System.out.println("2. View Prices Across Network");
            System.out.println("3. Place Medicine Order");
            System.out.println("4. View Billing Receipts");
            System.out.println("5. View Order History");
            System.out.println("6. Logout");
            System.out.print("Enter choice: ");
            String input = scanner.nextLine();
            int choice = InputValidator.validateMenuChoice(input, 1, 6);
            if (choice == -1) {
                System.out.println(">> Invalid option! Try again.");
                continue;
            }

            switch (choice) {
                case 1:
                    searchMedicines(scanner, customer);
                    break;
                case 2:
                    viewPrices(scanner);
                    break;
                case 3:
                    placeOrder(scanner, customer);
                    break;
                case 4:
                    viewBills(customer);
                    break;
                case 5:
                    viewOrderHistory(customer);
                    break;
                case 6:
                    System.out.println(">> Logged out from customer portal.");
                    return;
            }
        }
    }

    private void searchMedicines(Scanner scanner, Customer customer) {
        System.out.print("\nEnter medicine name to search: ");
        String query = scanner.nextLine().trim();
        if (query.isEmpty()) {
            System.out.println(">> Search query cannot be empty.");
            return;
        }

        List<Medicine> list = medicineDao.searchMedicinesByName(query);
        if (list.isEmpty()) {
            System.out.println(">> No matching medicines found in catalog.");
        } else {
            System.out.println("\n----------------------------------------------------------------------");
            System.out.printf("%-6s %-5s %-30s %-25s\n", "Index", "ID", "Medicine Name", "Manufacturer");
            System.out.println("----------------------------------------------------------------------");
            int idx = 1;
            for (Medicine m : list) {
                System.out.printf("%-6d %-5d %-30s %-25s\n", idx++, m.getMedicineId(), m.getMedicineName(), m.getManufacturer());
            }
            System.out.println("----------------------------------------------------------------------");
            System.out.print("\nDo you want to order one of these medicines? (y/n): ");
            String ans = scanner.nextLine().trim().toLowerCase();
            if (ans.equals("y") || ans.equals("yes")) {
                Medicine chosen = null;
                if (list.size() == 1) {
                    chosen = list.get(0);
                    System.out.println(">> Selecting: " + chosen.getMedicineName());
                } else {
                    int selectedIndex = -1;
                    while (selectedIndex == -1) {
                        System.out.print("Enter Index (1 to " + list.size() + ") or 'q' to cancel: ");
                        String sel = scanner.nextLine().trim();
                        if (sel.equalsIgnoreCase("q")) {
                            return;
                        }
                        selectedIndex = InputValidator.validateMenuChoice(sel, 1, list.size());
                        if (selectedIndex == -1) {
                            System.out.println(">> Invalid index selection. Please choose from the list.");
                        }
                    }
                    chosen = list.get(selectedIndex - 1);
                }
                
                System.out.print("Enter Quantity to order: ");
                String qtyInput = scanner.nextLine();
                try {
                    int qty = Integer.parseInt(qtyInput.trim());
                    if (qty <= 0) {
                        System.out.println(">> Quantity must be greater than zero.");
                        return;
                    }
                    executeOrderProcess(customer, chosen.getMedicineId(), qty, scanner);
                } catch (NumberFormatException e) {
                    System.out.println(">> Invalid quantity input. Order cancelled.");
                }
            }
        }
    }

    private void viewPrices(Scanner scanner) {
        System.out.print("\nEnter Medicine ID to view prices: ");
        String medInput = scanner.nextLine();
        try {
            int medId = Integer.parseInt(medInput.trim());
            Medicine med = medicineDao.getMedicineById(medId);
            if (med == null) {
                System.out.println(">> Medicine not found in catalog.");
                return;
            }

            List<Inventory> stocks = inventoryDao.searchMedicineStockAcrossApprovedPharmacies(medId);
            System.out.printf("\nPrices for: %s (%s)\n", med.getMedicineName(), med.getCategory());
            if (stocks.isEmpty()) {
                System.out.println(">> Currently out of stock in all approved pharmacies.");
            } else {
                System.out.println("-----------------------------------------------------------------------------");
                System.out.printf("%-25s %-12s %-15s %-12s\n", "Pharmacy Name", "Batch", "Expiry Date", "Price (INR)");
                System.out.println("-----------------------------------------------------------------------------");
                for (Inventory inv : stocks) {
                    PharmacyOwner ph = pharmacyDao.getPharmacyById(inv.getPharmacyId());
                    String name = ph != null ? ph.getPharmacyName() : "Unknown";
                    System.out.printf("%-25s %-12s %-15s Rs. %-10.2f\n", 
                            name, inv.getBatchNumber(), inv.getExpiryDate().toString(), inv.getSellingPrice());
                }
                System.out.println("-----------------------------------------------------------------------------");
            }
        } catch (NumberFormatException e) {
            System.out.println(">> Invalid ID format.");
        }
    }

    private void placeOrder(Scanner scanner, Customer customer) {
        System.out.print("\nEnter Medicine ID: ");
        String medInput = scanner.nextLine();
        System.out.print("Enter Quantity to order: ");
        String qtyInput = scanner.nextLine();

        try {
            int medId = Integer.parseInt(medInput.trim());
            int qty = Integer.parseInt(qtyInput.trim());

            if (qty <= 0) {
                System.out.println(">> Quantity must be greater than zero.");
                return;
            }

            Medicine med = medicineDao.getMedicineById(medId);
            if (med == null) {
                System.out.println(">> Medicine does not exist in catalog.");
                return;
            }

            executeOrderProcess(customer, medId, qty, scanner);

        } catch (NumberFormatException e) {
            System.out.println(">> Invalid inputs. Order cancelled.");
        }
    }

    private void executeOrderProcess(Customer customer, int medId, int qty, Scanner scanner) {
        try {
            System.out.println(">> Processing order branching rules...");
            Order order = orderService.placeOrder(customer.getId(), medId, qty);
            System.out.println("\n>>> SUCCESS! Order Placed Successfully!");
            System.out.printf("  Order ID : %d\n", order.getOrderId());
            System.out.printf("  Status   : %s\n", order.getOrderStatus());
            System.out.println(">> Order is pending pharmacy approval.");
        } catch (StockOutException e) {
            System.out.println(">> Requested quantity is not available. Max available stock is: " + e.getAvailableStock());
            System.out.print(">> Do you want to place the order for the available limit of " + e.getAvailableStock() + " units? (y/n): ");
            String ans = scanner.nextLine().trim().toLowerCase();
            if (ans.equals("y") || ans.equals("yes")) {
                executeOrderProcess(customer, medId, e.getAvailableStock(), scanner);
            } else {
                System.out.println(">> Order cancelled.");
            }
        } catch (Exception e) {
            System.out.println(">> Order Failed: " + e.getMessage());
        }
    }

    private void viewBills(Customer customer) {
        List<Bill> bills = billDao.getBillsByCustomer(customer.getId());
        if (bills.isEmpty()) {
            System.out.println(">> No billing invoices found.");
        } else {
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.printf("%-10s %-20s %-12s %-12s %-12s\n", "Bill ID", "Invoice Number", "Subtotal", "GST (12%)", "Total (INR)");
            System.out.println("--------------------------------------------------------------------------------");
            for (Bill b : bills) {
                System.out.printf("%-10d %-20s %-12.2f %-12.2f %-12.2f\n", 
                        b.getBillId(), b.getInvoiceNumber(), b.getSubtotal(), b.getGstAmount(), b.getTotalAmount());
            }
            System.out.println("--------------------------------------------------------------------------------");
        }
    }

    private void viewOrderHistory(Customer customer) {
        List<Order> orders = orderDao.getOrdersByCustomer(customer.getId());
        if (orders.isEmpty()) {
            System.out.println(">> No orders found in your history.");
        } else {
            System.out.println("\n---------------------------------------------------------------------------");
            System.out.printf("%-10s %-25s %-25s %-30s\n", "Order ID", "Pharmacy", "Order Date", "Status Description");
            System.out.println("---------------------------------------------------------------------------");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            for (Order o : orders) {
                String friendlyStatus = o.getOrderStatus();
                if ("PLACED".equals(o.getOrderStatus())) {
                    friendlyStatus = "Awaiting Approval";
                } else if ("FULFILLED_LOCAL".equals(o.getOrderStatus())) {
                    friendlyStatus = "Confirmed (Local Fulfill)";
                } else if ("FULFILLED_TRANSFER".equals(o.getOrderStatus())) {
                    friendlyStatus = "Confirmed (Transfer Fulfill)";
                } else if ("CANCELLED".equals(o.getOrderStatus())) {
                    friendlyStatus = "Cancelled / Rejected";
                }
                String formattedDate = o.getOrderDate() != null ? sdf.format(o.getOrderDate()) : "N/A";
                System.out.printf("%-10d %-25s %-25s %-30s\n", 
                        o.getOrderId(), o.getPharmacyName(), formattedDate, friendlyStatus);
            }
            System.out.println("---------------------------------------------------------------------------");
        }
    }
}
