package com.medchain.report;

import com.medchain.dao.BillDao;
import com.medchain.dao.OrderDao;
import com.medchain.dao.PharmacyDao;
import com.medchain.model.Bill;
import com.medchain.model.Order;
import com.medchain.model.PharmacyOwner;
import com.medchain.dao.InventoryDao;
import com.medchain.model.Inventory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.report
 * Purpose: Generates system-wide admin audit logs including sales metrics, approval queues, and low stock notifications.
 */
public class AuditReportGenerator {
    // [CONCEPT: File Handling]
    public static void generate() {
        OrderDao orderDao = new OrderDao();
        BillDao billDao = new BillDao();
        PharmacyDao pharmacyDao = new PharmacyDao();
        InventoryDao inventoryDao = new InventoryDao();

        List<Order> allOrders = orderDao.getOrdersWithDetails();
        List<PharmacyOwner> allPharmacies = pharmacyDao.getAllPharmacies();

        int totalOrders = allOrders.size();
        int fulfilledOrders = 0;
        double systemRevenue = 0.0;
        double systemGst = 0.0;

        for (Order o : allOrders) {
            String status = o.getOrderStatus();
            if ("FULFILLED_LOCAL".equals(status) || "FULFILLED_TRANSFER".equals(status)) {
                fulfilledOrders++;
                Bill bill = billDao.getBillByOrderId(o.getOrderId());
                if (bill != null) {
                    systemRevenue += bill.getSubtotal();
                    systemGst += bill.getGstAmount();
                }
            }
        }

        List<PharmacyOwner> pendingApprovals = new ArrayList<>();
        for (PharmacyOwner p : allPharmacies) {
            if ("PENDING".equals(p.getApprovalStatus())) {
                pendingApprovals.add(p);
            }
        }

        // Aggregate low stock alerts (stock <= 15) across approved pharmacies
        List<Inventory> lowStockItems = new ArrayList<>();
        for (PharmacyOwner p : allPharmacies) {
            if ("APPROVED".equals(p.getApprovalStatus())) {
                List<Inventory> invs = inventoryDao.getInventoryByPharmacy(p.getId());
                for (Inventory inv : invs) {
                    if (inv.getStockQuantity() <= 15) {
                        lowStockItems.add(inv);
                    }
                }
            }
        }

        File dir = new File("reports");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String dateStr = LocalDate.now().toString();
        File file = new File(dir, "audit_" + dateStr + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // [CONCEPT: String Handling - StringBuilder]
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================\n");
            sb.append("                       MEDCHAIN                         \n");
            sb.append("                 SYSTEM AUDIT REPORT                    \n");
            sb.append("========================================================\n");
            sb.append(String.format("Audit Date      : %s\n", dateStr));
            sb.append("--------------------------------------------------------\n");
            sb.append("                  SYSTEM TRANSACTION TOTALS             \n");
            sb.append("--------------------------------------------------------\n");
            sb.append(String.format("Total System Orders     : %d\n", totalOrders));
            sb.append(String.format("Fulfilled Orders        : %d\n", fulfilledOrders));
            sb.append(String.format("Total Subtotal Revenue  : Rs. %.2f\n", systemRevenue));
            sb.append(String.format("Total GST Collected     : Rs. %.2f\n", systemGst));
            sb.append(String.format("Gross System Sales      : Rs. %.2f\n", systemRevenue + systemGst));
            sb.append("--------------------------------------------------------\n");
            sb.append("               PENDING PHARMACY APPROVAL QUEUE          \n");
            sb.append("--------------------------------------------------------\n");
            if (pendingApprovals.isEmpty()) {
                sb.append("No pending pharmacy registrations.\n");
            } else {
                sb.append(String.format("%-5s %-25s %-20s\n", "ID", "Pharmacy Name", "Owner Name"));
                sb.append("--------------------------------------------------------\n");
                for (PharmacyOwner p : pendingApprovals) {
                    sb.append(String.format("%-5d %-25s %-20s\n", p.getId(), p.getPharmacyName(), p.getFullName()));
                }
            }
            sb.append("--------------------------------------------------------\n");
            sb.append("              LOW STOCK INVENTORY WARNINGS (<=15)       \n");
            sb.append("--------------------------------------------------------\n");
            if (lowStockItems.isEmpty()) {
                sb.append("No low stock alerts in active pharmacies.\n");
            } else {
                sb.append(String.format("%-25s %-12s %-8s %-10s\n", "Pharmacy", "Medicine", "Qty", "Batch"));
                sb.append("--------------------------------------------------------\n");
                for (Inventory inv : lowStockItems) {
                    // Find pharmacy name
                    String pName = "Unknown";
                    for (PharmacyOwner p : allPharmacies) {
                        if (p.getId() == inv.getPharmacyId()) {
                            pName = p.getPharmacyName();
                            break;
                        }
                    }
                    // Limit pharmacy name length for alignment
                    if (pName.length() > 22) {
                        pName = pName.substring(0, 19) + "...";
                    }
                    sb.append(String.format("%-25s %-12s %-8d %-10s\n", 
                            pName, inv.getMedicineName(), inv.getStockQuantity(), inv.getBatchNumber()));
                }
            }
            sb.append("========================================================\n");

            writer.write(sb.toString());
            System.out.println(">> System audit report generated: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error generating audit report file: " + e.getMessage());
        }
    }
}
