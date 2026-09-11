package com.medchain.service;

import com.medchain.dao.CourierDao;
import com.medchain.dao.InventoryDao;
import com.medchain.dao.TransferDao;
import com.medchain.model.Courier;
import com.medchain.model.Inventory;
import com.medchain.model.Transfer;
import com.medchain.util.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Package: com.medchain.service
 * Purpose: Business logic service managing courier transfers and inventory updates on transfer completion.
 */
public class TransferService {
    private final TransferDao transferDao = new TransferDao();
    private final CourierDao courierDao = new CourierDao();
    private final InventoryDao inventoryDao = new InventoryDao();

    public List<Transfer> getTransfersByCourier(int courierId) {
        return transferDao.getTransfersByCourier(courierId);
    }

    public List<Transfer> getAllTransfers() {
        return transferDao.getAllTransfers();
    }

    public boolean updateTransferStatus(int transferId, String newStatus) {
        Transfer tr = transferDao.getTransferById(transferId);
        if (tr == null) return false;

        Connection conn = DBConnection.getConnection();
        try {
            // [CONCEPT: JDBC - Transaction boundary for complex transfer status updates]
            conn.setAutoCommit(false);

            boolean success = transferDao.updateTransferStatus(transferId, newStatus);
            if (!success) {
                conn.rollback();
                return false;
            }

            if ("IN_TRANSIT".equals(newStatus)) {
                // Mark courier as ON_DELIVERY
                if (tr.getCourierId() != null) {
                    courierDao.updateStatus(tr.getCourierId(), "ON_DELIVERY");
                }
            } else if ("COMPLETED".equals(newStatus)) {
                // 1. Release courier
                if (tr.getCourierId() != null) {
                    courierDao.updateStatus(tr.getCourierId(), "AVAILABLE");
                }

                // 2. Adjust target inventory stock
                // Find matching batch details from the source pharmacy inventory (even if stock is 0 now)
                List<Inventory> sourceInvs = inventoryDao.getInventoryByPharmacy(tr.getSourcePharmacyId());
                Inventory sourceBatch = null;
                for (Inventory s : sourceInvs) {
                    if (s.getMedicineId() == tr.getMedicineId()) {
                        sourceBatch = s;
                        break;
                    }
                }

                if (sourceBatch == null) {
                    // Fallback if source batch is not found, we fetch any active batch details in the catalog
                    throw new RuntimeException("Source inventory batch details not found for transfer ID: " + transferId);
                }

                // Check if destination pharmacy already has this batch
                List<Inventory> destInvs = inventoryDao.getInventoryByPharmacy(tr.getDestinationPharmacyId());
                Inventory destBatch = null;
                for (Inventory d : destInvs) {
                    if (d.getMedicineId() == tr.getMedicineId() && d.getBatchNumber().equals(sourceBatch.getBatchNumber())) {
                        destBatch = d;
                        break;
                    }
                }

                if (destBatch != null) {
                    // Update existing destination inventory stock
                    inventoryDao.updateStockQuantity(destBatch.getInventoryId(), destBatch.getStockQuantity() + tr.getQuantity());
                } else {
                    // Create new inventory entry for destination pharmacy copying source batch properties
                    Inventory newInv = new Inventory(
                            0,
                            tr.getDestinationPharmacyId(),
                            tr.getMedicineId(),
                            sourceBatch.getBatchNumber(),
                            sourceBatch.getExpiryDate(),
                            tr.getQuantity(),
                            sourceBatch.getPurchasePrice(),
                            sourceBatch.getSellingPrice()
                    );
                    inventoryDao.insertInventory(newInv);
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                // Ignore rollback failure logging
            }
            throw new RuntimeException("Error executing transfer status update transaction", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                // Ignore auto-commit restoration logging
            }
        }
    }
}
