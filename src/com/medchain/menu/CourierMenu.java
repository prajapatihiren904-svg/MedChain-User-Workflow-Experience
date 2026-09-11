package com.medchain.menu;

import com.medchain.exception.InvalidLoginException;
import com.medchain.model.Courier;
import com.medchain.model.Transfer;
import com.medchain.service.TransferService;
import com.medchain.service.UserService;
import com.medchain.util.InputValidator;
import java.util.List;
import java.util.Scanner;

/**
 * Package: com.medchain.menu
 * Purpose: Console-based menu handling Courier actions like listing and completing assigned transfers.
 */
public class CourierMenu {
    private final UserService userService = new UserService();
    private final TransferService transferService = new TransferService();

    public void showLoginMenu(Scanner scanner) {
        System.out.println("\n--- Courier Login ---");
        System.out.print("Enter Courier Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        try {
            Courier courier = userService.loginCourier(email, password);
            System.out.println(">> Access Granted. Welcome, " + courier.getFullName());
            showPostLoginMenu(scanner, courier);
        } catch (InvalidLoginException e) {
            System.out.println(">> Login Failed: " + e.getMessage());
        }
    }

    private void showPostLoginMenu(Scanner scanner, Courier courier) {
        while (true) {
            System.out.printf("\n=== Courier Panel (%s) ===\n", courier.getFullName());
            System.out.println("1. View Assigned Transfers Queue");
            System.out.println("2. Update Transfer Status");
            System.out.println("3. Logout");
            System.out.print("Enter choice: ");
            String input = scanner.nextLine();
            int choice = InputValidator.validateMenuChoice(input, 1, 3);
            if (choice == -1) {
                System.out.println(">> Invalid option! Try again.");
                continue;
            }

            switch (choice) {
                case 1:
                    viewAssignedTransfers(courier);
                    break;
                case 2:
                    updateTransfer(scanner, courier);
                    break;
                case 3:
                    System.out.println(">> Logged out from courier portal.");
                    return;
            }
        }
    }

    private void viewAssignedTransfers(Courier courier) {
        List<Transfer> list = transferService.getTransfersByCourier(courier.getId());
        System.out.println("\n--- Active Assigned Shipments Queue ---");
        if (list.isEmpty()) {
            System.out.println("No active transfer tasks assigned to you.");
        } else {
            System.out.println("--------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s %-10s %-25s %-25s %-15s %-12s\n", "Trans ID", "Order ID", "Source Pharmacy", "Dest Pharmacy", "Distance", "Status");
            System.out.println("--------------------------------------------------------------------------------------------------------------");
            for (Transfer t : list) {
                System.out.printf("%-10d %-10d %-25s %-25s %-15.2f %-12s\n", 
                        t.getTransferId(), t.getOrderId(), t.getSourcePharmacyName(), t.getDestinationPharmacyName(), t.getDistanceKm(), t.getTransferStatus());
            }
            System.out.println("--------------------------------------------------------------------------------------------------------------");
        }
    }

    private void updateTransfer(Scanner scanner, Courier courier) {
        List<Transfer> list = transferService.getTransfersByCourier(courier.getId());
        if (list.isEmpty()) {
            System.out.println(">> No active transfers to update.");
            return;
        }

        viewAssignedTransfers(courier);
        System.out.print("Enter Transfer ID to update: ");
        String idInput = scanner.nextLine();
        try {
            int transId = Integer.parseInt(idInput.trim());
            
            // Validate that this transfer is assigned to the current courier
            Transfer target = null;
            for (Transfer t : list) {
                if (t.getTransferId() == transId) {
                    target = t;
                    break;
                }
            }

            if (target == null) {
                System.out.println(">> Invalid Transfer ID. You are not assigned to this shipment.");
                return;
            }

            System.out.printf("Current Status: %s\n", target.getTransferStatus());
            System.out.println("Select New Status:");
            if ("ASSIGNED".equals(target.getTransferStatus())) {
                System.out.println("1. Mark as IN_TRANSIT");
                System.out.println("2. Cancel update");
                System.out.print("Enter option: ");
                int opt = InputValidator.validateMenuChoice(scanner.nextLine(), 1, 2);
                if (opt == 1) {
                    transferService.updateTransferStatus(transId, "IN_TRANSIT");
                    System.out.println(">> Status updated to IN_TRANSIT. Good luck on the road!");
                }
            } else if ("IN_TRANSIT".equals(target.getTransferStatus())) {
                System.out.println("1. Mark as COMPLETED");
                System.out.println("2. Cancel update");
                System.out.print("Enter option: ");
                int opt = InputValidator.validateMenuChoice(scanner.nextLine(), 1, 2);
                if (opt == 1) {
                    transferService.updateTransferStatus(transId, "COMPLETED");
                    System.out.println(">> Status updated to COMPLETED. Package delivered and target inventory updated!");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(">> Invalid Transfer ID format.");
        } catch (Exception e) {
            System.out.println(">> Status update failed: " + e.getMessage());
        }
    }
}
