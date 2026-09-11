package com.medchain.report;

import com.medchain.model.Bill;
import com.medchain.model.Order;
import com.medchain.model.OrderItem;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Package: com.medchain.report
 * Purpose: File writer utility generating detailed billing receipts in .txt format.
 */
public class ReceiptGenerator {
    // [CONCEPT: File Handling]
    public static void generate(Bill bill, Order order, List<OrderItem> items) {
        File dir = new File("receipts");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "invoice_" + bill.getInvoiceNumber() + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // [CONCEPT: String Handling - StringBuilder]
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================\n");
            sb.append("                       MEDCHAIN                         \n");
            sb.append("                   BILLING RECEIPT                      \n");
            sb.append("========================================================\n");
            sb.append(String.format("Invoice No  : %s\n", bill.getInvoiceNumber()));
            sb.append(String.format("Date & Time : %s\n", bill.getBillDate().toString()));
            sb.append(String.format("Customer    : %s (ID: %d)\n", order.getCustomerName(), order.getCustomerId()));
            sb.append(String.format("Pharmacy    : %s (ID: %d)\n", order.getPharmacyName(), order.getPharmacyId()));
            sb.append(String.format("Order Status: %s\n", order.getOrderStatus()));
            sb.append("--------------------------------------------------------\n");
            sb.append(String.format("%-25s %-10s %-8s %-10s\n", "Medicine Name", "Qty", "Price", "Total"));
            sb.append("--------------------------------------------------------\n");
            for (OrderItem item : items) {
                double total = item.getQuantity() * item.getUnitPrice();
                sb.append(String.format("%-25s %-10d %-8.2f %-10.2f\n", 
                        item.getMedicineName(), item.getQuantity(), item.getUnitPrice(), total));
            }
            sb.append("--------------------------------------------------------\n");
            sb.append(String.format("%-36s : Rs. %-8.2f\n", "Subtotal", bill.getSubtotal()));
            sb.append(String.format("%-36s : Rs. %-8.2f (12%%)\n", "GST Amount", bill.getGstAmount()));
            sb.append("--------------------------------------------------------\n");
            sb.append(String.format("%-36s : Rs. %-8.2f\n", "GRAND TOTAL", bill.getTotalAmount()));
            sb.append("========================================================\n");
            sb.append("            Thank you for using MedChain!               \n");
            sb.append("========================================================\n");

            writer.write(sb.toString());
            System.out.println(">> Receipt generated successfully: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error generating receipt file: " + e.getMessage());
        }
    }
}
