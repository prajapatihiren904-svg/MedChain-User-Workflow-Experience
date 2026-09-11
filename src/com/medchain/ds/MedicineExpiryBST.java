package com.medchain.ds;

import com.medchain.model.Inventory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.ds
 * Purpose: Manually implemented Binary Search Tree used at runtime to sort and filter pharmacy inventories by medicine expiry dates.
 */
public class MedicineExpiryBST {
    private BSTNode root;

    public MedicineExpiryBST() {

        this.root = null;
    }

    // Insert an inventory item
    public void insert(Inventory item) {

        root = insertRec(root, item);
    }

    private BSTNode insertRec(BSTNode current, Inventory item) {
        if (current == null) {
            return new BSTNode(item);
        }

        // Compare dates: earlier dates go to the left, same or later to the right
        if (item.getExpiryDate().isBefore(current.getKey())) {
            current.setLeft(insertRec(current.getLeft(), item));
        } else {
            current.setRight(insertRec(current.getRight(), item));
        }

        return current;
    }

    // Inorder traversal to return list sorted nearest-expiry-first (ascending date order)
    public List<Inventory> inorderTraversal() {
        List<Inventory> sortedList = new ArrayList<>();
        inorderRec(root, sortedList);
        return sortedList;
    }

    private void inorderRec(BSTNode current, List<Inventory> sortedList) {
        if (current != null) {
            inorderRec(current.getLeft(), sortedList);
            sortedList.add(current.getValue());
            inorderRec(current.getRight(), sortedList);
        }
    }

    // Search by expiry date
    public List<Inventory> search(LocalDate expiry) {
        List<Inventory> result = new ArrayList<>();
        searchRec(root, expiry, result);
        return result;
    }

    private void searchRec(BSTNode current, LocalDate expiry, List<Inventory> result) {
        if (current == null) return;

        if (current.getKey().equals(expiry)) {
            result.add(current.getValue());
        }

        // Expiry dates are ordered, so check left if search key is less than current key,
        // and right if greater or equal. Since we inserted >= to the right, we must search both
        // subtrees if there's any chance of matches. To be safe, binary search:
        if (expiry.isBefore(current.getKey())) {
            searchRec(current.getLeft(), expiry, result);
        } else if (expiry.isAfter(current.getKey())) {
            searchRec(current.getRight(), expiry, result);
        } else {
            // Equal: check both left and right (in case duplicates are placed on both sides or right)
            searchRec(current.getLeft(), expiry, result);
            searchRec(current.getRight(), expiry, result);
        }
    }

    // Delete inventory item by expiry date and batch number
    public void delete(LocalDate expiry, String batchNumber) {
        root = deleteRec(root, expiry, batchNumber);
    }

    private BSTNode deleteRec(BSTNode current, LocalDate expiry, String batchNumber) {
        if (current == null) return null;

        if (expiry.isBefore(current.getKey())) {
            current.setLeft(deleteRec(current.getLeft(), expiry, batchNumber));
        } else if (expiry.isAfter(current.getKey())) {
            current.setRight(deleteRec(current.getRight(), expiry, batchNumber));
        } else {
            // Equal key: check if this node matches the batch number
            if (current.getValue().getBatchNumber().equals(batchNumber)) {
                // Node found to delete!
                if (current.getLeft() == null) {
                    return current.getRight();
                } else if (current.getRight() == null) {
                    return current.getLeft();
                }

                // Node with two children: Get the inorder successor (smallest in the right subtree)
                BSTNode successor = minValueNode(current.getRight());
                current.setKey(successor.getKey());
                current.setValue(successor.getValue());
                
                // Delete the successor
                current.setRight(deleteRec(current.getRight(), successor.getKey(), successor.getValue().getBatchNumber()));
            } else {
                // Expiry matches but batch number does not. Equal elements might be in both left and right subtrees
                current.setLeft(deleteRec(current.getLeft(), expiry, batchNumber));
                current.setRight(deleteRec(current.getRight(), expiry, batchNumber));
            }
        }
        return current;
    }

    private BSTNode minValueNode(BSTNode node) {
        BSTNode current = node;
        while (current.getLeft() != null) {
            current = current.getLeft();
        }
        return current;
    }
}
