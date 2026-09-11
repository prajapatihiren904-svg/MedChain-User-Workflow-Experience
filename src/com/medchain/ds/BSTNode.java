package com.medchain.ds;

import com.medchain.model.Inventory;
import java.time.LocalDate;

/**
 * Package: com.medchain.ds
 * Purpose: Manual Data Structure Node representing a single element inside the Medicine Expiry BST.
 */
public class BSTNode {
    private LocalDate key; // Expiry date
    private Inventory value; // Inventory item
    private BSTNode left;
    private BSTNode right;

    public BSTNode(Inventory value) {
        this.key = value.getExpiryDate();
        this.value = value;
        this.left = null;
        this.right = null;
    }

    public LocalDate getKey() {
        return key;
    }

    public void setKey(LocalDate key) {
        this.key = key;
    }

    public Inventory getValue() {
        return value;
    }

    public void setValue(Inventory value) {
        this.value = value;
    }

    public BSTNode getLeft() {
        return left;
    }

    public void setLeft(BSTNode left) {
        this.left = left;
    }

    public BSTNode getRight() {
        return right;
    }

    public void setRight(BSTNode right) {
        this.right = right;
    }
}
