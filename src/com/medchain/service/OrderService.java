package com.medchain.service;

import com.medchain.algorithm.AreaGraph;
import com.medchain.algorithm.DijkstraShortestPath;
import com.medchain.dao.*;
import com.medchain.exception.MedicineNotFoundException;
import com.medchain.exception.StockOutException;
import com.medchain.model.*;
import com.medchain.report.ReceiptGenerator;
import com.medchain.util.DBConnection;
import com.medchain.util.GSTCalculator;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Package: com.medchain.service
 * Purpose: Central orchestrator containing core business workflows for placing orders, executing Dijkstra routing, approvals, and transaction rollback.
 */
public class OrderService {
    private final CustomerDao customerDao = new CustomerDao();
    private final PharmacyDao pharmacyDao = new PharmacyDao();
    private final InventoryDao inventoryDao = new InventoryDao();
    private final OrderDao orderDao = new OrderDao();
    private final OrderItemDao orderItemDao = new OrderItemDao();
    private final TransferDao transferDao = new TransferDao();
    private final CourierDao courierDao = new CourierDao();
    private final BillDao billDao = new BillDao();

    // [CONCEPT: Custom Exception Handling, JDBC Transactions]
    public synchronized Order placeOrder(int customerId, int medicineId, int quantity) throws MedicineNotFoundException, StockOutException {
        Customer customer = customerDao.getCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found.");
        }

        // 1. Resolve preferred pharmacy or default to any in home area
        Integer preferredPharmId = customer.getPreferredPharmacyId();
        if (preferredPharmId == null) {
            List<PharmacyOwner> areaPharmacies = getApprovedPharmaciesInArea(customer.getHomeAreaId());
            if (!areaPharmacies.isEmpty()) {
                preferredPharmId = areaPharmacies.get(0).getId();
            } else {
                // System-wide fallback for preferred pharmacy
                List<PharmacyOwner> all = pharmacyDao.getAllPharmacies();
                for (PharmacyOwner p : all) {
                    if ("APPROVED".equals(p.getApprovalStatus())) {
                        preferredPharmId = p.getId();
                        break;
                    }
                }
            }
        }

        if (preferredPharmId == null) {
            throw new MedicineNotFoundException("No approved pharmacies available in the system.");
        }

        // Check total stock in entire network of approved pharmacies
        List<Inventory> allNetworkStock = inventoryDao.searchMedicineStockAcrossApprovedPharmacies(medicineId);
        int totalNetworkStock = 0;
        double unitPrice = 0.0;
        for (Inventory inv : allNetworkStock) {
            totalNetworkStock += inv.getStockQuantity();
            if (unitPrice == 0.0) {
                unitPrice = inv.getSellingPrice();
            }
        }

        if (totalNetworkStock < quantity) {
            if (totalNetworkStock > 0) {
                throw new StockOutException("Only " + totalNetworkStock + " units available across the network.", totalNetworkStock);
            } else {
                throw new MedicineNotFoundException("The requested medicine is out of stock across the entire network.");
            }
        }

        // Create Order with initial status PLACED
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setPharmacyId(preferredPharmId);
        order.setOrderStatus("PLACED");

        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            order = orderDao.insertOrder(order);

            OrderItem item = new OrderItem();
            item.setOrderId(order.getOrderId());
            item.setMedicineId(medicineId);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            orderItemDao.insertOrderItem(item);

            conn.commit();
            return order;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {}
            throw new RuntimeException("Failed to place order: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {}
        }
    }

    // [CONCEPT: JDBC Transactions]
    public synchronized boolean approveOrder(int orderId) throws MedicineNotFoundException {
        Order order = orderDao.getOrderById(orderId);
        if (order == null) {
            return false;
        }
        if (!"PLACED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("Order is already processed.");
        }

        List<OrderItem> items = orderItemDao.getOrderItemsByOrderId(orderId);
        if (items.isEmpty()) {
            throw new IllegalStateException("Order has no items.");
        }

        OrderItem item = items.get(0);
        int medicineId = item.getMedicineId();
        int quantity = item.getQuantity();

        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Check if preferred pharmacy itself has stock
            List<Inventory> preferredStock = inventoryDao.getInventoryByPharmacyAndMedicine(order.getPharmacyId(), medicineId);
            int prefTotalStock = getSumStock(preferredStock);

            if (prefTotalStock >= quantity) {
                // Deduct stock locally
                deductStockOnly(quantity, preferredStock);
                orderDao.updateOrderStatus(orderId, "FULFILLED_LOCAL");
                order.setOrderStatus("FULFILLED_LOCAL");

                // Populate transient names for invoice printing
                populateOrderNames(order);

                // Generate Bill and print Receipt
                generateBill(orderId, items);
                conn.commit();
                return true;
            }

            // 2. Check same-area approved pharmacies
            Customer customer = customerDao.getCustomerById(order.getCustomerId());
            int homeAreaId = customer != null ? customer.getHomeAreaId() : 1;
            List<PharmacyOwner> homeAreaPharmacies = getApprovedPharmaciesInArea(homeAreaId);
            for (PharmacyOwner p : homeAreaPharmacies) {
                if (p.getId() == order.getPharmacyId()) continue;

                List<Inventory> localStock = inventoryDao.getInventoryByPharmacyAndMedicine(p.getId(), medicineId);
                int localTotalStock = getSumStock(localStock);

                if (localTotalStock >= quantity) {
                    // Deduct stock from this same area pharmacy
                    deductStockOnly(quantity, localStock);

                    // Create Transfer from source same-area pharmacy to preferred pharmacy
                    createTransferRecord(orderId, p.getId(), order.getPharmacyId(), medicineId, quantity, 3.0); // local default distance 3km

                    orderDao.updateOrderStatus(orderId, "FULFILLED_TRANSFER");
                    order.setOrderStatus("FULFILLED_TRANSFER");

                    populateOrderNames(order);
                    generateBill(orderId, items);
                    conn.commit();
                    return true;
                }
            }

            // 3. Run Dijkstra to find nearest remote pharmacy with stock
            AreaGraph graph = new AreaGraph();
            List<PharmacyOwner> allApprovedPharmacies = getApprovedPharmacies();

            PharmacyOwner nearestPharm = null;
            double shortestDistance = Double.MAX_VALUE;
            List<Inventory> sourceStock = null;

            for (PharmacyOwner p : allApprovedPharmacies) {
                if (p.getAreaId() == homeAreaId || p.getId() == order.getPharmacyId()) continue;

                List<Inventory> remoteStock = inventoryDao.getInventoryByPharmacyAndMedicine(p.getId(), medicineId);
                int remoteTotalStock = getSumStock(remoteStock);

                if (remoteTotalStock >= quantity) {
                    // [CONCEPT: Dijkstra Path Finding]
                    DijkstraShortestPath.PathResult pathResult = DijkstraShortestPath.findShortestPath(graph, homeAreaId, p.getAreaId());
                    if (pathResult.getDistance() < shortestDistance) {
                        shortestDistance = pathResult.getDistance();
                        nearestPharm = p;
                        sourceStock = remoteStock;
                    }
                }
            }

            if (nearestPharm != null && sourceStock != null) {
                // Deduct stock at remote pharmacy
                deductStockOnly(quantity, sourceStock);

                // Create Transfer
                createTransferRecord(orderId, nearestPharm.getId(), order.getPharmacyId(), medicineId, quantity, shortestDistance);

                orderDao.updateOrderStatus(orderId, "FULFILLED_TRANSFER");
                order.setOrderStatus("FULFILLED_TRANSFER");

                populateOrderNames(order);
                generateBill(orderId, items);
                conn.commit();
                return true;
            }

            // If stock is not available anymore
            throw new MedicineNotFoundException("Stock is no longer available across the network.");

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {}
            if (e instanceof MedicineNotFoundException) {
                throw (MedicineNotFoundException) e;
            }
            throw new RuntimeException("Approval failed: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {}
        }
    }

    public boolean rejectOrder(int orderId) {
        return orderDao.updateOrderStatus(orderId, "CANCELLED");
    }

    private void deductStockOnly(int quantity, List<Inventory> stockList) {
        int remainingToDeduct = quantity;
        for (Inventory batch : stockList) {
            if (remainingToDeduct <= 0) break;
            int deduct = Math.min(batch.getStockQuantity(), remainingToDeduct);
            inventoryDao.updateStockQuantity(batch.getInventoryId(), batch.getStockQuantity() - deduct);
            remainingToDeduct -= deduct;
        }
    }

    private void createTransferRecord(int orderId, int srcPharmId, int destPharmId, int medicineId, int quantity, double distance) {
        Transfer transfer = new Transfer();
        transfer.setOrderId(orderId);
        transfer.setSourcePharmacyId(srcPharmId);
        transfer.setDestinationPharmacyId(destPharmId);
        transfer.setMedicineId(medicineId);
        transfer.setQuantity(quantity);
        transfer.setDistanceKm(distance);
        transfer.setTransferStatus("PENDING");

        Courier courier = courierDao.getAvailableCourier();
        if (courier != null) {
            transfer.setCourierId(courier.getId());
            transfer.setTransferStatus("ASSIGNED");
            courierDao.updateStatus(courier.getId(), "ON_DELIVERY");
        }

        transferDao.insertTransfer(transfer);
    }

    private void generateBill(int orderId, List<OrderItem> items) {
        double subtotal = 0.0;
        for (OrderItem item : items) {
            subtotal += item.getQuantity() * item.getUnitPrice();
        }

        double gst = GSTCalculator.calculateGST(subtotal);
        double total = GSTCalculator.calculateTotal(subtotal);

        String invoiceNo = "INV-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + orderId;

        Bill bill = new Bill(0, orderId, invoiceNo, subtotal, gst, total, null);
        bill = billDao.insertBill(bill);

        // Create printed receipt file
        Order order = orderDao.getOrderById(orderId);
        for (OrderItem item : items) {
            // ensure medicine names are populated
            if (item.getMedicineName() == null) {
                Medicine m = new MedicineDao().getMedicineById(item.getMedicineId());
                if (m != null) item.setMedicineName(m.getMedicineName());
            }
        }
        ReceiptGenerator.generate(bill, order, items);
    }

    private int getSumStock(List<Inventory> stockList) {
        int sum = 0;
        for (Inventory i : stockList) {
            sum += i.getStockQuantity();
        }
        return sum;
    }

    private List<PharmacyOwner> getApprovedPharmaciesInArea(int areaId) {
        List<PharmacyOwner> all = pharmacyDao.getAllPharmacies();
        List<PharmacyOwner> filtered = new ArrayList<>();
        for (PharmacyOwner p : all) {
            if (p.getAreaId() == areaId && "APPROVED".equals(p.getApprovalStatus())) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    private List<PharmacyOwner> getApprovedPharmacies() {
        List<PharmacyOwner> all = pharmacyDao.getAllPharmacies();
        List<PharmacyOwner> approved = new ArrayList<>();
        for (PharmacyOwner p : all) {
            if ("APPROVED".equals(p.getApprovalStatus())) {
                approved.add(p);
            }
        }
        return approved;
    }

    private void populateOrderNames(Order order) {
        Customer c = customerDao.getCustomerById(order.getCustomerId());
        if (c != null) order.setCustomerName(c.getFullName());

        PharmacyOwner p = pharmacyDao.getPharmacyById(order.getPharmacyId());
        if (p != null) order.setPharmacyName(p.getPharmacyName());
    }
}
