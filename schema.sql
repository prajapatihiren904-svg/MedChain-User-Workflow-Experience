-- MedChain Database Schema & Seed Data
-- Database course compliance: 3rd Normal Form (3NF), InnoDB engine, constraints, indices, and sample data.

CREATE DATABASE IF NOT EXISTS medchain_db;
USE medchain_db;

-- Drop dependent tables first to avoid FK constraints violations
DROP TABLE IF EXISTS Order_Logs;
DROP TABLE IF EXISTS Bills;
DROP TABLE IF EXISTS Transfers;
DROP TABLE IF EXISTS Order_Items;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS Inventory;
DROP TABLE IF EXISTS Area_Connections;
DROP TABLE IF EXISTS Customers;
DROP TABLE IF EXISTS Pharmacies;
DROP TABLE IF EXISTS Couriers;
DROP TABLE IF EXISTS Admins;
DROP TABLE IF EXISTS Medicines;
DROP TABLE IF EXISTS Areas;

-- 1. Areas
CREATE TABLE Areas (
    area_id INT AUTO_INCREMENT PRIMARY KEY,
    area_name VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Medicines (Catalog)
CREATE TABLE Medicines (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_name VARCHAR(150) NOT NULL,
    manufacturer VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    INDEX idx_med_name (medicine_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Admins
CREATE TABLE Admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Couriers
CREATE TABLE Couriers (
    courier_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    status ENUM('AVAILABLE', 'ON_DELIVERY') DEFAULT 'AVAILABLE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Pharmacies
CREATE TABLE Pharmacies (
    pharmacy_id INT AUTO_INCREMENT PRIMARY KEY,
    pharmacy_name VARCHAR(100) NOT NULL,
    owner_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    area_id INT NOT NULL,
    address VARCHAR(255) NOT NULL,
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    registered_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (area_id) REFERENCES Areas(area_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Customers
CREATE TABLE Customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    home_area_id INT NOT NULL,
    preferred_pharmacy_id INT NULL,
    registered_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (home_area_id) REFERENCES Areas(area_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (preferred_pharmacy_id) REFERENCES Pharmacies(pharmacy_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Area_Connections (M:N self-reference for Areas forming Area Graph)
CREATE TABLE Area_Connections (
    connection_id INT AUTO_INCREMENT PRIMARY KEY,
    area_id_1 INT NOT NULL,
    area_id_2 INT NOT NULL,
    distance_km DECIMAL(5,2) NOT NULL,
    UNIQUE KEY uq_connection (area_id_1, area_id_2),
    FOREIGN KEY (area_id_1) REFERENCES Areas(area_id) ON DELETE CASCADE,
    FOREIGN KEY (area_id_2) REFERENCES Areas(area_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Inventory (Per-pharmacy medicine stock, batch details, prices)
CREATE TABLE Inventory (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    pharmacy_id INT NOT NULL,
    medicine_id INT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    purchase_price DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    UNIQUE KEY uq_pharm_med_batch (pharmacy_id, medicine_id, batch_number),
    FOREIGN KEY (pharmacy_id) REFERENCES Pharmacies(pharmacy_id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Orders
CREATE TABLE Orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    pharmacy_id INT NOT NULL,
    order_status ENUM('PLACED', 'FULFILLED_LOCAL', 'FULFILLED_TRANSFER', 'CANCELLED') DEFAULT 'PLACED',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id) ON DELETE RESTRICT,
    FOREIGN KEY (pharmacy_id) REFERENCES Pharmacies(pharmacy_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Order_Items
CREATE TABLE Order_Items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Transfers (Inter-pharmacy inventory transfer via Dijkstra shortest path routing)
CREATE TABLE Transfers (
    transfer_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    source_pharmacy_id INT NOT NULL,
    destination_pharmacy_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,
    courier_id INT NULL,
    transfer_status ENUM('PENDING', 'ASSIGNED', 'IN_TRANSIT', 'COMPLETED') DEFAULT 'PENDING',
    distance_km DECIMAL(6,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (source_pharmacy_id) REFERENCES Pharmacies(pharmacy_id) ON DELETE RESTRICT,
    FOREIGN KEY (destination_pharmacy_id) REFERENCES Pharmacies(pharmacy_id) ON DELETE RESTRICT,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id) ON DELETE RESTRICT,
    FOREIGN KEY (courier_id) REFERENCES Couriers(courier_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Bills
CREATE TABLE Bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    invoice_number VARCHAR(30) UNIQUE NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    gst_amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. Order_Logs (Audit trail table populated by DBMS Trigger)
CREATE TABLE Order_Logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    old_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- DBMS Trigger: Fulfills DBMS project requirements by logging all order status changes automatically
DELIMITER //
CREATE TRIGGER after_order_status_update
AFTER UPDATE ON Orders
FOR EACH ROW
BEGIN
    IF OLD.order_status <> NEW.order_status THEN
        INSERT INTO Order_Logs (order_id, old_status, new_status)
        VALUES (NEW.order_id, OLD.order_status, NEW.order_status);
    END IF;
END //
DELIMITER ;

-- ALTER TABLE ... ADD FOREIGN KEY alternate syntax demonstration (as reference)
-- ALTER TABLE Orders ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES Customers(customer_id);

/* -------------------------------------------------------------------------- */
-- SEED DATA INSERTION
-- All dummy data is based on Ahmedabad, Gujarat locations,
-- Gujarati names, and Indian pharmaceutical companies
-- (many of which are headquartered in Ahmedabad itself).
/* -------------------------------------------------------------------------- */

-- 1. Insert 15 Ahmedabad Areas (Real localities)
INSERT INTO Areas (area_id, area_name) VALUES
(1,  'Navrangpura'),
(2,  'Maninagar'),
(3,  'Bopal'),
(4,  'Satellite'),
(5,  'Vastrapur'),
(6,  'Paldi'),
(7,  'Chandkheda'),
(8,  'Naranpura'),
(9,  'Gota'),
(10, 'Thaltej'),
(11, 'Memnagar'),
(12, 'Bodakdev'),
(13, 'Shahibaug'),
(14, 'Ellis Bridge'),
(15, 'Prahlad Nagar');

-- 2. Insert 20 Area Connections (Graph Edges) — realistic Ahmedabad road distances
-- The graph is undirected; only one row per pair is stored, the Java code traverses both ways.
INSERT INTO Area_Connections (area_id_1, area_id_2, distance_km) VALUES
-- Central Ahmedabad cluster
(1,  8,  3.00),   -- Navrangpura  ↔ Naranpura       (via Polytechnic Road)
(1,  14, 2.50),   -- Navrangpura  ↔ Ellis Bridge     (via CG Road)
(1,  11, 2.80),   -- Navrangpura  ↔ Memnagar         (via Gujarat University Road)
(14, 6,  1.80),   -- Ellis Bridge ↔ Paldi            (via Nehru Bridge)
(6,  2,  5.50),   -- Paldi        ↔ Maninagar        (via Lal Darwaja-Kalupur route)
(1,  13, 4.00),   -- Navrangpura  ↔ Shahibaug        (via Usmanpura-RTO)
-- Western Ahmedabad spine (SG Highway corridor)
(11, 5,  2.00),   -- Memnagar     ↔ Vastrapur        (via IIM Road)
(5,  12, 1.50),   -- Vastrapur    ↔ Bodakdev         (via Judges Bunglow Road)
(12, 4,  2.20),   -- Bodakdev     ↔ Satellite        (via Anand Nagar Road)
(4,  15, 2.00),   -- Satellite    ↔ Prahlad Nagar    (via 100 Feet Road)
(15, 10, 3.50),   -- Prahlad Nagar↔ Thaltej          (via SG Highway south)
(10, 3,  5.00),   -- Thaltej      ↔ Bopal            (via South Bopal Road)
-- Northern corridor
(8,  9,  6.50),   -- Naranpura    ↔ Gota             (via Drive-In to Gota)
(9,  7,  4.00),   -- Gota         ↔ Chandkheda       (via Sola-Chandkheda Road)
-- Cross connections for graph richness (Dijkstra multiple paths)
(5,  4,  3.80),   -- Vastrapur    ↔ Satellite        (via Judges Bunglow direct)
(6,  14, 1.80),   -- Paldi        ↔ Ellis Bridge     (duplicate direction safety check)
(8,  11, 2.20),   -- Naranpura    ↔ Memnagar         (via ISRO-Jodhpur Cross Road)
(10, 5,  4.80),   -- Thaltej      ↔ Vastrapur        (via SG Highway north)
(13, 8,  3.50),   -- Shahibaug    ↔ Naranpura        (via Sabarmati Ashram Road)
(3,  15, 4.50);   -- Bopal        ↔ Prahlad Nagar    (via Iskcon-Bopal Ring Road)

-- 3. Insert Admins
-- Password: admin123 → SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO Admins (admin_id, full_name, email, password_hash) VALUES
(1, 'Darshan Trivedi', 'darshan.trivedi@medchain.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');

-- 4. Insert Couriers (local Ahmedabad delivery personnel)
-- Password: courier123 → SHA-256: ad0d62ed22683ac6ab70219c233608bfc612ad8f3e9ca7244bd03ce7f79dd3c4
INSERT INTO Couriers (courier_id, full_name, email, password_hash, phone, status) VALUES
(1, 'Rajesh Thakor',     'rajesh.thakor@medchain.com',  'ad0d62ed22683ac6ab70219c233608bfc612ad8f3e9ca7244bd03ce7f79dd3c4', '9879012345', 'AVAILABLE'),
(2, 'Bhavesh Rabari',    'bhavesh.rabari@medchain.com', 'ad0d62ed22683ac6ab70219c233608bfc612ad8f3e9ca7244bd03ce7f79dd3c4', '9879012346', 'AVAILABLE'),
(3, 'Kishan Makwana',    'kishan.makwana@medchain.com', 'ad0d62ed22683ac6ab70219c233608bfc612ad8f3e9ca7244bd03ce7f79dd3c4', '9879012347', 'AVAILABLE');

-- 5. Insert Pharmacies (based on real Ahmedabad locations and Gujarati owners)
-- Password: pharmacy123 → SHA-256: ed5273f7ab1e24f89704b06074e12565a17da3d0457bd9a5271b43816f985d57
INSERT INTO Pharmacies (pharmacy_id, pharmacy_name, owner_name, email, password_hash, area_id, address, approval_status) VALUES
(1, 'Zydus Pharmacy',                  'Dr. Ramesh Desai',     'zydus.navrangpura@medchain.com',     'ed5273f7ab1e24f89704b06074e12565a17da3d0457bd9a5271b43816f985d57', 1,  'Opp. HL Commerce College, CG Road, Navrangpura, Ahmedabad - 380009',      'APPROVED'),
(2, 'Torrent Pharma Retail',           'Dr. Ketan Joshi',      'torrent.maninagar@medchain.com',     'ed5273f7ab1e24f89704b06074e12565a17da3d0457bd9a5271b43816f985d57', 2,  'Nr. Maninagar Railway Crossing, Maninagar, Ahmedabad - 380008',           'APPROVED'),
(3, 'Intas MediCare Store',            'Dr. Suresh Bhatt',     'intas.bopal@medchain.com',           'ed5273f7ab1e24f89704b06074e12565a17da3d0457bd9a5271b43816f985d57', 3,  'Opp. Shivalik Heights, South Bopal, Ahmedabad - 380058',                  'APPROVED'),
(4, 'Cadila Healthcare Pharmacy',      'Dr. Harish Pandya',    'cadila.satellite@medchain.com',      'ed5273f7ab1e24f89704b06074e12565a17da3d0457bd9a5271b43816f985d57', 4,  'Nr. Rajhans Cinema, Jodhpur Cross Road, Satellite, Ahmedabad - 380015',   'APPROVED'),
(5, 'Sun Pharma Wellness Hub',         'Dr. Nitin Parikh',     'sunpharma.vastrapur@medchain.com',   'ed5273f7ab1e24f89704b06074e12565a17da3d0457bd9a5271b43816f985d57', 5,  'Nr. Vastrapur Lake, Vastrapur, Ahmedabad - 380015',                       'APPROVED'),
(6, 'Aarogyam Pharmacy Chandkheda',    'Dr. Jayesh Solanki',   'aarogyam.chandkheda@medchain.com',   'ed5273f7ab1e24f89704b06074e12565a17da3d0457bd9a5271b43816f985d57', 7,  'Nr. Chandkheda Bus Stand, New CG Road, Chandkheda, Ahmedabad - 382424',   'APPROVED'),
(7, 'MedPlus Prahlad Nagar',           'Dr. Ankit Raval',      'medplus.prahladnagar@medchain.com',  'ed5273f7ab1e24f89704b06074e12565a17da3d0457bd9a5271b43816f985d57', 15, 'Nr. Honest Restaurant, 100 Feet Road, Prahlad Nagar, Ahmedabad - 380015', 'PENDING'),
(8, 'Netmeds Store Shahibaug',         'Dr. Maulik Oza',       'netmeds.shahibaug@medchain.com',     'ed5273f7ab1e24f89704b06074e12565a17da3d0457bd9a5271b43816f985d57', 13, 'Nr. Shahibaug Gymkhana, Shahibaug, Ahmedabad - 380004',                   'PENDING');

-- 6. Insert Customers (Gujarati names, residing across Ahmedabad)
-- Password: customer123 → SHA-256: b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6
INSERT INTO Customers (customer_id, full_name, email, password_hash, phone, home_area_id, preferred_pharmacy_id) VALUES
(1, 'Vikram Desai',        'vikram.desai@gmail.com',    'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', '9925012345', 1,  1),     -- Lives Navrangpura, prefers Zydus Pharmacy
(2, 'Priya Joshi',         'priya.joshi@gmail.com',     'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', '9925012346', 3,  3),     -- Lives Bopal, prefers Intas MediCare
(3, 'Rohan Trivedi',       'rohan.trivedi@gmail.com',   'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', '9925012347', 2,  2),     -- Lives Maninagar, prefers Torrent Retail
(4, 'Nisha Bhatt',         'nisha.bhatt@gmail.com',     'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', '9925012348', 7,  6),     -- Lives Chandkheda, prefers Aarogyam
(5, 'Harsh Patel',         'harsh.patel@gmail.com',     'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', '9925012349', 4,  4);     -- Lives Satellite, prefers Cadila

-- 7. Insert Medicines (featuring Ahmedabad-headquartered pharma companies)
-- Zydus Cadila (Ahmedabad), Torrent Pharmaceuticals (Ahmedabad), Intas Pharmaceuticals (Ahmedabad),
-- Sun Pharma, Cadila Healthcare, Alembic (Vadodara, Gujarat), plus national brands.
INSERT INTO Medicines (medicine_id, medicine_name, manufacturer, category) VALUES
(1,  'Crocin Advance 500mg',      'GSK India',                'Analgesic'),
(2,  'Amoxicillin 500mg',         'Cipla Ltd',                'Antibiotic'),
(3,  'Atorva 10mg',               'Zydus Cadila, Ahmedabad',  'Cardiovascular'),
(4,  'Glycomet 500mg',            'USV Pvt Ltd',              'Anti-diabetic'),
(5,  'Brufen 400mg',              'Abbott India',             'Analgesic'),
(6,  'Okacet 10mg',               'Zydus Cadila, Ahmedabad',  'Anti-allergic'),
(7,  'Pantocid 40mg',             'Sun Pharma, Gujarat',      'Gastrointestinal'),
(8,  'Azee 500mg',                'Cipla Ltd',                'Antibiotic'),
(9,  'Montair 10mg',              'Cipla Ltd',                'Respiratory'),
(10, 'Amlodac 5mg',               'Zydus Cadila, Ahmedabad',  'Cardiovascular'),
(11, 'Torsid 10mg',               'Torrent Pharma, Ahmedabad','Cardiovascular'),
(12, 'Dolo 650mg',                'Micro Labs',               'Analgesic'),
(13, 'Pan-D Capsule',             'Alkem Labs',               'Gastrointestinal'),
(14, 'Augmentin 625 Duo',         'GSK India',                'Antibiotic'),
(15, 'Glyciphage SR 500mg',       'Franco-Indian, Gujarat',   'Anti-diabetic');

-- 8. Insert Inventory (with mixed expiry dates for BST sorting/search demo)
-- Current date assumed: July 2026. Some batches near-expiry, some expired, some long-dated.
INSERT INTO Inventory (pharmacy_id, medicine_id, batch_number, expiry_date, stock_quantity, purchase_price, selling_price) VALUES
-- Zydus Pharmacy, Navrangpura (ID 1) — well stocked
(1, 1,  'ZP-CR-2601',  '2026-08-15',  50,  18.00, 25.50),   -- Crocin, expiring soon
(1, 1,  'ZP-CR-2602',  '2028-01-10', 200,  17.50, 25.50),   -- Crocin, long dated
(1, 2,  'ZP-AM-2601',  '2026-09-20',  30,  42.00, 62.00),   -- Amoxicillin
(1, 3,  'ZP-AT-2601',  '2028-06-15',  80,  85.00, 125.00),  -- Atorva (Zydus own brand)
(1, 6,  'ZP-OK-2601',  '2027-11-30', 150,  28.00, 42.00),   -- Okacet (Zydus own brand)
(1, 10, 'ZP-AD-2601',  '2028-03-25', 100,  35.00, 52.00),   -- Amlodac (Zydus own brand)
(1, 12, 'ZP-DL-2601',  '2026-07-20',  15,  22.00, 32.00),   -- Dolo 650, EXPIRING in days

-- Torrent Pharma Retail, Maninagar (ID 2) — moderate stock
(2, 1,  'TP-CR-2601',  '2026-07-30',  60,  18.50, 26.00),   -- Crocin, very close to expiry
(2, 4,  'TP-GL-2601',  '2027-04-10',  50,  38.00, 55.00),   -- Glycomet
(2, 7,  'TP-PC-2601',  '2027-09-01',  90,  65.00, 95.00),   -- Pantocid
(2, 11, 'TP-TS-2601',  '2028-08-15',  70,  48.00, 72.00),   -- Torsid (Torrent own brand)
(2, 14, 'TP-AG-2601',  '2027-05-20',  25,  145.00, 198.00), -- Augmentin 625

-- Intas MediCare Store, Bopal (ID 3) — selective stock
(3, 1,  'IM-CR-2601',  '2027-11-20', 120,  17.00, 24.50),   -- Crocin
(3, 2,  'IM-AM-2601',  '2026-08-01',  10,  44.00, 65.00),   -- Amoxicillin, very low stock near expiry
(3, 8,  'IM-AZ-2601',  '2028-03-10',  45,  72.00, 105.00),  -- Azee 500
(3, 13, 'IM-PD-2601',  '2027-06-15',  80,  55.00, 78.00),   -- Pan-D
(3, 5,  'IM-BR-2601',  '2028-12-31',  60,  28.00, 42.00),   -- Brufen 400

-- Cadila Healthcare Pharmacy, Satellite (ID 4) — mixed stock
(4, 1,  'CH-CR-2601',  '2026-07-25',  10,  19.00, 27.00),   -- Crocin, nearly expired + low qty
(4, 3,  'CH-AT-2601',  '2027-11-15',  50,  86.00, 126.00),  -- Atorva
(4, 9,  'CH-MN-2601',  '2028-05-18',  35,  58.00, 85.00),   -- Montair
(4, 15, 'CH-GS-2601',  '2027-10-01',  65,  42.00, 62.00),   -- Glyciphage SR

-- Sun Pharma Wellness Hub, Vastrapur (ID 5) — good stock, slightly higher prices
(5, 7,  'SP-PC-2601',  '2028-02-28', 110,  62.00, 92.00),   -- Pantocid (Sun Pharma own brand)
(5, 5,  'SP-BR-2601',  '2027-08-15',  40,  30.00, 45.00),   -- Brufen 400
(5, 12, 'SP-DL-2601',  '2027-06-10',  90,  21.00, 31.00),   -- Dolo 650
(5, 2,  'SP-AM-2601',  '2028-09-30',  75,  40.00, 60.00),   -- Amoxicillin

-- Aarogyam Pharmacy, Chandkheda (ID 6) — remote area, limited variety
(6, 1,  'AP-CR-2601',  '2027-03-15',  80,  19.50, 28.00),   -- Crocin
(6, 12, 'AP-DL-2601',  '2027-12-20',  45,  22.00, 33.00),   -- Dolo 650
(6, 4,  'AP-GL-2601',  '2026-11-30',  30,  39.00, 57.00),   -- Glycomet
(6, 6,  'AP-OK-2601',  '2026-08-10',  20,  29.00, 44.00);   -- Okacet, expiring soon

/* -------------------------------------------------------------------------- */
-- SAMPLE DML OPERATIONS & JOINS
/* -------------------------------------------------------------------------- */

-- A. INNER JOIN: Retrieve full Order Details with Customer name, Pharmacy name, and Status
-- Demonstrates multi-table INNER JOIN across Orders, Customers, and Pharmacies.
SELECT o.order_id,
       c.full_name     AS customer_name,
       p.pharmacy_name AS pharmacy_name,
       o.order_status,
       o.order_date
FROM Orders o
INNER JOIN Customers c  ON o.customer_id = c.customer_id
INNER JOIN Pharmacies p ON o.pharmacy_id = p.pharmacy_id;

-- B. LEFT JOIN: Find Medicines that have zero or no stock at Intas MediCare Store (Pharmacy ID 3, Bopal)
-- This returns ALL 15 medicines and shows stock as 0 for those not stocked at Pharmacy 3.
SELECT m.medicine_name,
       m.manufacturer,
       m.category,
       COALESCE(i.stock_quantity, 0) AS available_stock
FROM Medicines m
LEFT JOIN Inventory i ON m.medicine_id = i.medicine_id AND i.pharmacy_id = 3
ORDER BY available_stock ASC;

-- C. SELECT with WHERE — Find all pharmacies in the Satellite area (area_id = 4)
SELECT pharmacy_id, pharmacy_name, owner_name, address, approval_status
FROM Pharmacies
WHERE area_id = 4;

-- D. UPDATE — Approve a PENDING pharmacy (Netmeds Store Shahibaug, ID 8)
UPDATE Pharmacies SET approval_status = 'APPROVED' WHERE pharmacy_id = 8;

-- E. DELETE — Remove an expired medicine batch from inventory
DELETE FROM Inventory
WHERE pharmacy_id = 4 AND medicine_id = 1 AND batch_number = 'CH-CR-2601';

-- F. Aggregate Query — Count total medicines in stock per approved pharmacy
SELECT p.pharmacy_name,
       COUNT(DISTINCT i.medicine_id) AS medicine_variety,
       SUM(i.stock_quantity)         AS total_units
FROM Pharmacies p
INNER JOIN Inventory i ON p.pharmacy_id = i.pharmacy_id
WHERE p.approval_status = 'APPROVED'
GROUP BY p.pharmacy_id, p.pharmacy_name
ORDER BY total_units DESC;

-- G. Transaction example — Full Order Placement Workflow
-- Scenario: Vikram Desai (ID 1) orders 5 strips of Crocin from Zydus Pharmacy (ID 1)
START TRANSACTION;
-- Step 1: Insert Order record
INSERT INTO Orders (customer_id, pharmacy_id, order_status) VALUES (1, 1, 'FULFILLED_LOCAL');
SET @new_order_id = LAST_INSERT_ID();
-- Step 2: Insert Order Items
INSERT INTO Order_Items (order_id, medicine_id, quantity, unit_price) VALUES (@new_order_id, 1, 5, 25.50);
-- Step 3: Reduce stock from nearest-expiry batch first (BATCH ZP-CR-2601)
UPDATE Inventory SET stock_quantity = stock_quantity - 5 WHERE pharmacy_id = 1 AND medicine_id = 1 AND batch_number = 'ZP-CR-2601';
-- Step 4: Generate billing invoice (subtotal=127.50, GST@12%=15.30, total=142.80)
INSERT INTO Bills (order_id, invoice_number, subtotal, gst_amount, total_amount)
VALUES (@new_order_id, 'INV-20260711-001', 127.50, 15.30, 142.80);
COMMIT;
-- On any failure above, replace COMMIT with: ROLLBACK;
