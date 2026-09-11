package com.medchain.service;

import com.medchain.dao.InventoryDao;
import com.medchain.ds.MedicineExpiryBST;
import com.medchain.model.Inventory;
import java.time.LocalDate;
import java.util.List;

/**
 * Package: com.medchain.service
 * Purpose: Business logic service managing inventory stock levels, prices and expiry checking using manual BST.
 */
public class InventoryService {
    private final InventoryDao inventoryDao = new InventoryDao();

    public Inventory addInventoryItem(int pharmacyId, int medicineId, String batchNumber, LocalDate expiryDate, int quantity, double purchasePrice, double sellingPrice) {
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot add a medicine batch that is already expired.");
        }
        Inventory inv = new Inventory(0, pharmacyId, medicineId, batchNumber, expiryDate, quantity, purchasePrice, sellingPrice);
        return inventoryDao.insertInventory(inv);
    }

    public boolean updateStock(int inventoryId, int newQuantity) {
        return inventoryDao.updateStockQuantity(inventoryId, newQuantity);
    }

    public boolean removeInventoryBatch(int pharmacyId, int medicineId, String batchNumber) {
        return inventoryDao.deleteInventoryBatch(pharmacyId, medicineId, batchNumber);
    }

    public List<Inventory> getInventoryByPharmacy(int pharmacyId) {
        return inventoryDao.getInventoryByPharmacy(pharmacyId);
    }

    // [CONCEPT: DS BST Usage]
    public List<Inventory> getExpiringMedicinesSorted(int pharmacyId) {
        List<Inventory> listFromDb = inventoryDao.getInventoryByPharmacy(pharmacyId);
        
        // Rebuild BST at runtime from DB records
        MedicineExpiryBST bst = new MedicineExpiryBST();
        for (Inventory inv : listFromDb) {
            bst.insert(inv);
        }

        // Return in-order traversed items (ascending chronological expiry date)
        return bst.inorderTraversal();
    }
}
