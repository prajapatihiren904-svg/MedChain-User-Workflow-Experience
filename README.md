# 💊 MedChain – Smart Medicine Supply Chain Management System

MedChain is a Java-based medicine supply chain management system designed to manage medicine inventory, pharmacies, customers, orders, couriers, transfers, and reporting through a structured database-driven application.

The project also demonstrates the practical use of Data Structures and Algorithms, including a manually implemented Binary Search Tree (BST) for medicine expiry management and Dijkstra's shortest-path algorithm for area-based pharmacy routing.

---

## 🚀 Key Features

- 👤 Customer registration and authentication
- 🏥 Pharmacy owner registration and approval workflow
- 🚚 Courier management and transfer assignment
- 💊 Medicine and inventory management
- 📦 Customer order management
- 🔄 Medicine inventory transfers between pharmacies
- 🗺️ Shortest-path routing between areas
- ⏳ Medicine expiry tracking using a Binary Search Tree
- 📊 Sales, audit, and inventory reporting
- 🧾 Receipt and invoice generation
- 🔐 Password handling and input validation
- 🗄️ MySQL database integration using JDBC

---

## 🧠 Data Structures & Algorithms

### 1. Dijkstra's Shortest Path

Dijkstra's algorithm is used to determine the shortest route between areas based on distance.

The implementation uses:

- Graph adjacency lists
- Priority Queue
- Distance tracking
- Parent tracking
- Path reconstruction

This is used for area-based routing and finding efficient pharmacy routes.

### 2. Medicine Expiry Binary Search Tree

A manually implemented Binary Search Tree is used for medicine inventory expiry management.

The BST supports:

- Insertion of inventory items
- In-order traversal
- Searching by expiry date
- Deletion using expiry date and batch number

In-order traversal provides inventory ordered by expiry date.

---

## 🏗️ Project Architecture

```text
src/com/medchain/
│
├── algorithm/
│   ├── AreaGraph.java
│   └── DijkstraShortestPath.java
│
├── dao/
│   ├── AdminDao.java
│   ├── AreaConnectionDao.java
│   ├── AreaDao.java
│   ├── BillDao.java
│   ├── CourierDao.java
│   ├── CustomerDao.java
│   ├── InventoryDao.java
│   ├── MedicineDao.java
│   ├── OrderDao.java
│   ├── OrderItemDao.java
│   ├── PharmacyDao.java
│   └── TransferDao.java
│
├── ds/
│   ├── BSTNode.java
│   └── MedicineExpiryBST.java
│
├── exception/
│
├── main/
│
├── menu/
│
├── model/
│
├── report/
│
├── service/
│
└── util/
    ├── DBConnection.java
    ├── DateUtil.java
    ├── GSTCalculator.java
    ├── InputValidator.java
    └── PasswordUtil.java