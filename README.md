# MedChain — Decentralized Pharmacy Network Management System

MedChain is a Core Java console application connecting to MySQL via JDBC. It models a decentralized pharmacy network that manages inventory, handles order branching, automates inter-pharmacy stock transfers using Dijkstra's shortest path, and implements manual Binary Search Tree (BST) sorting for medicine expiry checking.

---

## SETUP & EXECUTION INSTRUCTIONS

Follow these steps to compile and run the application on your local machine:

### 1. Database Setup
1. Open **XAMPP Control Panel** and start the **Apache** and **MySQL** services.
2. Open your web browser and go to: `http://localhost/phpmyadmin`
3. Click on the **SQL** tab.
4. Copy the entire contents of [schema.sql](file:///schema.sql) and execute it. This will create the database `medchain_db` and seed it with all necessary geographical areas, connections, admin accounts, couriers, pharmacies, customer profiles, and medicine inventories.

### 2. Properties Configuration
Verify the MySQL credentials inside the `db.properties` file located at the project root. The default configuration connects to:
- **Host**: `localhost:3306`
- **Username**: `root`
- **Password**: *(empty)*

### 3. Placing the JDBC Driver
Download the MySQL Connector/J driver `.jar` file and place it inside the `lib/` directory at the project root. For example:
`C:\Users\lenovo\.gemini\antigravity-ide\scratch\MedChain\lib\mysql-connector-j-8.x.x.jar`

### 4. Compilation
- **Windows**: Double-click [compile.bat](file:///compile.bat) or execute it in PowerShell/Command Prompt.
- **Unix/Linux**: Run `./compile.sh` in the terminal.

### 5. Running the Application
- **Windows**: Double-click [run.bat](file:///run.bat) or execute `run.bat` in PowerShell/Command Prompt.
- **Unix/Linux**: Run `./run.sh` in the terminal.

---

## PACKAGE ARCHITECTURE SUMMARY

| Package | Purpose |
|---|---|
| `com.medchain.main` | Single entry point (`Main.java`). boots connection and runs the root portal loop. |
| `com.medchain.model` | Domain POJOs mapping MySQL tables. Utilizes inheritance (`User` base class). |
| `com.medchain.dao` | Data Access Objects containing all JDBC `PreparedStatement` query/insert execution logic. |
| `com.medchain.service` | Business logic orchestrator (transaction bounds, order branching, Dijkstra routing). |
| `com.medchain.menu` | UI Layer holding role-based console scanner input menus (`CustomerMenu`, `PharmacyMenu`, etc.). |
| `com.medchain.exception` | Custom checked exceptions representing business failures (e.g. `StockOutException`). |
| `com.medchain.util` | Utilities (JDBC Singleton `DBConnection`, `PasswordUtil` hashing, `GSTCalculator`, validation). |
| `com.medchain.algorithm` | Shortest path finder (`DijkstraShortestPath`) and graph adjacency mapping (`AreaGraph`). |
| `com.medchain.ds` | Manual sorting structure (`MedicineExpiryBST` and `BSTNode`) sorting inventory by expiry date. |
| `com.medchain.report` | Text report writers (`ReceiptGenerator`, `SalesReportGenerator`, `AuditReportGenerator`). |

---

## ARCHITECTURAL DIAGRAMS

### 1. Entity Relationship (ER) Diagram
```mermaid
erDiagram
    AREAS ||--o{ PHARMACIES : "has"
    AREAS ||--o{ CUSTOMERS : "resides_in"
    AREAS ||--o{ AREA_CONNECTIONS : "originates"
    AREAS ||--o{ AREA_CONNECTIONS : "terminates"
    PHARMACIES ||--o{ INVENTORY : "holds"
    PHARMACIES ||--o{ ORDERS : "receives"
    PHARMACIES ||--o{ TRANSFERS : "transfers_from"
    PHARMACIES ||--o{ TRANSFERS : "transfers_to"
    CUSTOMERS ||--o{ ORDERS : "places"
    MEDICINES ||--o{ INVENTORY : "cataloged_in"
    MEDICINES ||--o{ ORDER_ITEMS : "ordered"
    MEDICINES ||--o{ TRANSFERS : "shipped"
    ORDERS ||--o{ ORDER_ITEMS : "details"
    ORDERS ||--|| BILLS : "billed_by"
    ORDERS ||--o{ TRANSFERS : "requires"
    COURIERS ||--o{ TRANSFERS : "delivers"

    AREAS {
        int area_id PK
        varchar area_name
    }
    AREA_CONNECTIONS {
        int connection_id PK
        int area_id_1 FK
        int area_id_2 FK
        decimal distance_km
    }
    PHARMACIES {
        int pharmacy_id PK
        varchar pharmacy_name
        varchar owner_name
        varchar email
        varchar password_hash
        int area_id FK
        varchar address
        varchar approval_status
        timestamp registered_on
    }
    CUSTOMERS {
        int customer_id PK
        varchar full_name
        varchar email
        varchar password_hash
        varchar phone
        int home_area_id FK
        int preferred_pharmacy_id FK
        timestamp registered_on
    }
    COURIERS {
        int courier_id PK
        varchar full_name
        varchar email
        varchar password_hash
        varchar phone
        varchar status
    }
    ADMINS {
        int admin_id PK
        varchar full_name
        varchar email
        varchar password_hash
    }
    MEDICINES {
        int medicine_id PK
        varchar medicine_name
        varchar manufacturer
        varchar category
    }
    INVENTORY {
        int inventory_id PK
        int pharmacy_id FK
        int medicine_id FK
        varchar batch_number
        date expiry_date
        int stock_quantity
        decimal purchase_price
        decimal selling_price
    }
    ORDERS {
        int order_id PK
        int customer_id FK
        int pharmacy_id FK
        varchar order_status
        timestamp order_date
    }
    ORDER_ITEMS {
        int order_item_id PK
        int order_id FK
        int medicine_id FK
        int quantity
        decimal unit_price
    }
    TRANSFERS {
        int transfer_id PK
        int order_id FK
        int source_pharmacy_id FK
        int destination_pharmacy_id FK
        int medicine_id FK
        int quantity
        int courier_id FK
        varchar transfer_status
        decimal distance_km
    }
    BILLS {
        int bill_id PK
        int order_id FK
        varchar invoice_number
        decimal subtotal
        decimal gst_amount
        decimal total_amount
        timestamp bill_date
    }
```

### 2. Class Diagram
```mermaid
classDiagram
    class User {
        <<abstract>>
        #int id
        #String fullName
        #String email
        #String passwordHash
        +getId() int
        +getFullName() String
        +getEmail() String
    }
    class Customer {
        -String phone
        -int homeAreaId
        -Integer preferredPharmacyId
        -Timestamp registeredOn
    }
    class PharmacyOwner {
        -String pharmacyName
        -int areaId
        -String address
        -String approvalStatus
        -Timestamp registeredOn
    }
    class Admin {
    }
    class Courier {
        -String phone
        -String status
    }
    
    User <|-- Customer
    User <|-- PharmacyOwner
    User <|-- Admin
    User <|-- Courier

    class Order {
        +int orderId
        +int customerId
        +int pharmacyId
        +String orderStatus
        +Timestamp orderDate
    }
    class Inventory {
        +int inventoryId
        +int pharmacyId
        +int medicineId
        +String batchNumber
        +LocalDate expiryDate
        +int stockQuantity
        +double sellingPrice
    }
    class Transfer {
        +int transferId
        +int orderId
        +int sourcePharmacyId
        +int destinationPharmacyId
        +int medicineId
        +int quantity
        +Integer courierId
        +String transferStatus
        +double distanceKm
    }
    class Bill {
        +int billId
        +int orderId
        +String invoiceNumber
        +double subtotal
        +double gstAmount
        +double totalAmount
    }

    Customer "1" -- "0..*" Order : places
    PharmacyOwner "1" -- "0..*" Inventory : maintains
    Order "1" -- "0..*" Transfer : creates
    Courier "1" -- "0..*" Transfer : routes
    Order "1" -- "1" Bill : generates

    class CustomerDao {
        +insertCustomer(Customer) Customer
        +authenticate(String, String) Customer
    }
    class PharmacyDao {
        +insertPharmacy(PharmacyOwner) PharmacyOwner
        +authenticate(String, String) PharmacyOwner
        +updateApprovalStatus(int, String) boolean
    }
    class OrderService {
        +placeOrder(int, int, int) Order
    }
    class DijkstraShortestPath {
        +findShortestPath(AreaGraph, int, int) PathResult
    }
    
    OrderService ..> CustomerDao : depends
    OrderService ..> PharmacyDao : depends
    OrderService ..> DijkstraShortestPath : calls
```

### 3. Use Case Diagram
```mermaid
flowchart TB
    subgraph CustomerUseCases["Customer Actions"]
        C1["Register Customer Account"]
        C2["Search Medicine Catalog"]
        C3["View Prices across Pharmacies"]
        C4["Place Medicine Order"]
        C5["View Personal Order History / Invoices"]
    end

    subgraph PharmacyUseCases["Pharmacy Actions"]
        P1["Register Pharmacy Profile"]
        P2["Add Inventory Batches"]
        P3["Update Stock / Prices"]
        P4["View Near Expiry Medicines"]
        P5["Generate Sales Summary Report"]
    end

    subgraph AdminUseCases["Admin Actions"]
        A1["Approve Pending Pharmacies"]
        A2["Configure Areas & Distance Graph"]
        A3["Register Catalog Medicine"]
        A4["Register Courier Account"]
        A5["Generate Global Audit Logs"]
    end

    subgraph CourierUseCases["Courier Actions"]
        R1["View Assigned Deliveries"]
        R2["Update Transfer Status (Delivered)"]
    end

    CustomerActor((Customer)) --> C1
    CustomerActor --> C2
    CustomerActor --> C3
    CustomerActor --> C4
    CustomerActor --> C5

    PharmacyActor((Pharmacy Owner)) --> P1
    PharmacyActor --> P2
    PharmacyActor --> P3
    PharmacyActor --> P4
    PharmacyActor --> P5

    AdminActor((Admin)) --> A1
    AdminActor --> A2
    AdminActor --> A3
    AdminActor --> A4
    AdminActor --> A5

    CourierActor((Courier)) --> R1
    CourierActor --> R2
```

### 4. Sequence Diagram: Order Placement with Transfer Fallback
```mermaid
sequenceDiagram
    autonumber
    actor Customer as Customer (Menu)
    participant Service as OrderService
    participant InvDao as InventoryDao
    participant Dijkstra as DijkstraShortestPath
    participant TransDao as TransferDao
    participant BillDao as BillDao
    participant Receipt as ReceiptGenerator

    Customer->>Service: placeOrder(customerId, medId, qty)
    activate Service
    Service->>InvDao: getInventoryByPharmacyAndMedicine(prefId, medId)
    InvDao-->>Service: Return local stock (qty < requested)
    
    Service->>InvDao: getInventoryByAreaAndMedicine(homeAreaId, medId)
    InvDao-->>Service: Return same area stock (empty or insufficient)
    
    Note over Service, Dijkstra: Trigger Dijkstra Shortest Path Search
    Service->>Dijkstra: findShortestPath(graph, homeAreaId, remoteAreaId)
    Dijkstra-->>Service: Return PathResult (distance, shortest path)
    
    Service->>InvDao: updateStockQuantity(remoteBatchId, newStock)
    Service->>TransDao: insertTransfer(transferDetails, courierId)
    Service->>BillDao: insertBill(billDetails)
    
    Service->>Receipt: generate(bill, order, items)
    Receipt-->>Customer: Text receipt written on disk
    
    Service-->>Customer: Return Order (Status: FULFILLED_TRANSFER)
    deactivate Service
```

### 5. Activity Diagram: Branching Fulfillment Logic
```mermaid
flowchart TD
    Start([Customer places order for quantity Q]) --> CheckPref{Is Q in stock at preferred pharmacy?}
    
    CheckPref -- Yes --> LocalFulfill[1. Deduct preferred stock<br>2. Generate FULFILLED_LOCAL order<br>3. Print receipt bill] --> End([Process Complete])
    
    CheckPref -- No --> CheckHome{Is Q in stock at any APPROVED pharmacy in same Area?}
    
    CheckHome -- Yes --> AreaFulfill[1. Deduct same-area pharmacy stock<br>2. Generate FULFILLED_LOCAL order<br>3. Print receipt bill] --> End
    
    CheckHome -- No --> GraphSearch[1. Search all approved pharmacies with stock<br>2. Run Dijkstra from home area to their areas] --> CheckPath{Path found to remote stock?}
    
    CheckPath -- Yes --> TransferFulfill[1. Choose nearest pharmacy<br>2. Deduct remote stock<br>3. Create FULFILLED_TRANSFER order<br>4. Generate Transfer & Assign Courier<br>5. Print receipt bill] --> End
    
    CheckPath -- No --> OutOfStockException[Throw MedicineNotFoundException<br>Display Error Message] --> End
```

### 6. Data Flow Diagram (DFD) Level 0: System Context
```mermaid
flowchart TD
    Customer((Customer)) -- "Search query, orders, registration" --> MedChain["[1.0] MedChain System"]
    MedChain -- "Billing Invoices, Receipt.txt" --> Customer

    PharmOwner((Pharmacy Owner)) -- "Inventory updates, registrations" --> MedChain
    MedChain -- "Sales report.txt" --> PharmOwner

    Admin((System Admin)) -- "Approvals, graph inputs, catalog medicines" --> MedChain
    MedChain -- "Audit reports.txt" --> Admin

    Courier((Courier)) -- "Transit updates" --> MedChain
    MedChain -- "Assigned transfer tasks" --> Courier
```

### 7. Data Flow Diagram (DFD) Level 1: Key Operations
```mermaid
flowchart TD
    Customer((Customer)) -- Registration details --> P1["[1.0] Registration & Approval"]
    PharmOwner((Pharmacy Owner)) -- Registration details --> P1
    Admin((System Admin)) -- Approval actions --> P1

    PharmOwner -- Stock data --> P2["[2.0] Inventory Management"]
    P2 -- Read/Write stock --> InventoryDB[(Inventory Table)]

    Customer -- Medicine search query --> P3["[3.0] Ordering Process"]
    InventoryDB -- Stock status --> P3
    P3 -- Create orders --> OrdersDB[(Orders Table)]
    
    OrdersDB -- Order details --> P4["[4.0] Billing & Reports"]
    P4 -- Create receipts/reports --> ReceiptsDir["receipts/ reports/ Directory"]

    OrdersDB -- Needs transfer --> P5["[5.0] Routing & Dispatch"]
    P5 -- Adjacency reads --> ConnectionsDB[(Connections Table)]
    P5 -- Routing path computations --> DijkstraProcess["[Dijkstra Engine]"]
    P5 -- Dispatch tasks --> Courier((Courier))
```

### 8. Data Flow Diagram (DFD) Level 2: Ordering Fulfill Detail
```mermaid
flowchart TD
    OrderRequest((Order Request)) --> SearchLocal["[3.1] Search Preferred & Same-Area Stocks"]
    SearchLocal -- "In Stock?" --> CheckLocal{Local Stock OK?}
    
    CheckLocal -- Yes --> LocalProc["[3.2] Generate Local Order Items & Update Inventory"]
    
    CheckLocal -- No --> DijkstraProc["[3.3] Trigger Dijkstra & Locate Remote Stock"]
    DijkstraProc -- "Shortest road path" --> CheckRemote{Remote Stock OK?}
    
    CheckRemote -- Yes --> TransferProc["[3.4] Generate Transfer Order & Assign Courier Task"]
    CheckRemote -- No --> ErrorProc["[3.5] Raise MedicineNotFoundException"]
    
    LocalProc --> BillGen["[3.6] Generate Bill Invoice & Print Receipt Text"]
    TransferProc --> BillGen
```
