package com.medchain.report;

import com.medchain.dao.BillDao;
import com.medchain.dao.OrderDao;
import com.medchain.dao.OrderItemDao;
import com.medchain.dao.PharmacyDao;
import com.medchain.model.Bill;
import com.medchain.model.Order;
import com.medchain.model.OrderItem;
import com.medchain.model.PharmacyOwner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Package: com.medchain.report
 * Purpose: Generates aggregated sales metrics and reports for individual pharmacies.
 */
public class SalesReportGenerator {
    // [CONCEPT: File Handling]
    public static void generate(int pharmacyId) {
        PharmacyDao pharmacyDao = new PharmacyDao();
        OrderDao orderDao = new OrderDao();
        OrderItemDao orderItemDao = new OrderItemDao();
        BillDao billDao = new BillDao();

        PharmacyOwner pharmacy = pharmacyDao.getPharmacyById(pharmacyId);
        if (pharmacy == null) {
            System.err.println("Error: Pharmacy not found for ID " + pharmacyId);
            return;
        }

        List<Order> orders = orderDao.getOrdersByPharmacy(pharmacyId);

        int totalOrdersCount = orders.size();
        int fulfilledLocalCount = 0;
        int fulfilledTransferCount = 0;
        int cancelledCount = 0;
        double totalRevenue = 0.0;
        double totalGst = 0.0;

        // Tracks quantity sold per medicine
        Map<String, Integer> medicineSalesCount = new HashMap<>();

        for (Order o : orders) {
            String status = o.getOrderStatus();
            if ("FULFILLED_LOCAL".equals(status)) {
                fulfilledLocalCount++;
            } else if ("FULFILLED_TRANSFER".equals(status)) {
                fulfilledTransferCount++;
            } else if ("CANCELLED".equals(status)) {
                cancelledCount++;
            }

            // Accumulate billing revenue if order was fulfilled
            if ("FULFILLED_LOCAL".equals(status) || "FULFILLED_TRANSFER".equals(status)) {
                Bill bill = billDao.getBillByOrderId(o.getOrderId());
                if (bill != null) {
                    totalRevenue += bill.getSubtotal();
                    totalGst += bill.getGstAmount();
                }

                // Count medicine items
                List<OrderItem> items = orderItemDao.getOrderItemsByOrderId(o.getOrderId());
                for (OrderItem item : items) {
                    String name = item.getMedicineName();
                    medicineSalesCount.put(name, medicineSalesCount.getOrDefault(name, 0) + item.getQuantity());
                }
            }
        }

        File dir = new File("reports");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String dateStr = LocalDate.now().toString();
        File file = new File(dir, "sales_" + pharmacyId + "_" + dateStr + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // [CONCEPT: String Handling - StringBuilder]
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================\n");
            sb.append("                       MEDCHAIN                         \n");
            sb.append("                 PHARMACY SALES REPORT                  \n");
            sb.append("========================================================\n");
            sb.append(String.format("Pharmacy Name   : %s\n", pharmacy.getPharmacyName()));
            sb.append(String.format("Owner Name      : %s\n", pharmacy.getFullName()));
            sb.append(String.format("Report Date     : %s\n", dateStr));
            sb.append("--------------------------------------------------------\n");
            sb.append("                      ORDER METRICS                     \n");
            sb.append("--------------------------------------------------------\n");
            sb.append(String.format("Total Orders Placed     : %d\n", totalOrdersCount));
            sb.append(String.format("  - Fulfilled Locally   : %d\n", fulfilledLocalCount));
            sb.append(String.format("  - Fulfilled Transfer  : %d\n", fulfilledTransferCount));
            sb.append(String.format("  - Cancelled Orders    : %d\n", cancelledCount));
            sb.append("--------------------------------------------------------\n");
            sb.append("                     REVENUE METRICS                    \n");
            sb.append("--------------------------------------------------------\n");
            sb.append(String.format("Total Revenue (Subtotal): Rs. %.2f\n", totalRevenue));
            sb.append(String.format("Total GST Collected     : Rs. %.2f\n", totalGst));
            sb.append(String.format("Gross Sales (incl. GST) : Rs. %.2f\n", totalRevenue + totalGst));
            sb.append("--------------------------------------------------------\n");
            sb.append("                  ITEMIZED SALES SUMMARY                \n");
            sb.append("--------------------------------------------------------\n");
            sb.append(String.format("%-40s %-15s\n", "Medicine Name", "Total Qty Sold"));
            sb.append("--------------------------------------------------------\n");
            if (medicineSalesCount.isEmpty()) {
                sb.append("No medicines sold yet.\n");
            } else {
                for (Map.Entry<String, Integer> entry : medicineSalesCount.entrySet()) {
                    sb.append(String.format("%-40s %-15d\n", entry.getKey(), entry.getValue()));
                }
            }
            sb.append("========================================================\n");

            writer.write(sb.toString());
            System.out.println(">> Pharmacy sales report generated: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error generating sales report file: " + e.getMessage());
        }
    }
}
