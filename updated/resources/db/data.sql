-- data.sql

DELETE FROM USERS;

INSERT INTO USERS (username, password_hash, role, full_name, email, status) VALUES
('admin','password123','ADMIN','Admin User','admin@example.com','ACTIVE'),
('manager1','password123','MANAGER','Manager One','manager1@example.com','ACTIVE'),
('reception1','password123','STAFF','Reception Staff','reception1@example.com','ACTIVE'),
('staff1','password123','STAFF','Staff Member','staff1@example.com','ACTIVE'),
('manager2','password123','MANAGER','Manager Two','manager2@example.com','ACTIVE'),
('reception2','password123','STAFF','Receptionist Two','reception2@example.com','ACTIVE'),
('housekeep1','password123','STAFF','HK Senior','hk1@example.com','ACTIVE'),
('account1','password123','ACCOUNTANT','Accounts One','acc1@example.com','ACTIVE');

-- GUESTS
DELETE FROM GUESTS;

INSERT INTO GUESTS (name, phone, email, address, gst_number, id_type, id_number, nationality) VALUES
('John Doe','1234567890','john.doe@example.com','123 Main St, Anytown','GST123456','Passport','P1234567','India'),
('Jane Smith','0987654321','jane.smith@example.com','456 Oak Ave, Somewhere','GST654321','Driver License','D7654321','India'),
('Michael Brown','1112223333','michael.b@example.com','789 Pine Rd, Nowhere',NULL,'Passport','P9988776','India'),
('Emily White','2223334444','emily.w@example.com','987 Elm St, Hometown','GST444444','Aadhar Card','A555555','India'),
('Robert Johnson','5551112233','robert.j@example.com','12 Baker St, London',NULL,'Passport','UKP112233','United Kingdom'),
('Olivia Davis','7778889999','olivia.d@example.com','22 Kings Rd, Sydney','GST778899','Passport','AUS998877','Australia'),
('William Garcia','9090808070','william.g@example.com','21 5th Ave, NYC',NULL,'Driver License','NYD774411','United States'),
('Sophia Martinez','8080909070','sophia.m@example.com','Via Roma 13, Rome',NULL,'Passport','ITP778899','Italy'),
('Liam Wilson','6060504030','liam.w@example.com','Queen St, Toronto','GST998877','Passport','CAP556677','Canada'),
('Ava Taylor','9898989898','ava.t@example.com','MG Road, Bengaluru','GST112233','Aadhar Card','AAD112233','India'),
('Noah Anderson','7878787878','noah.a@example.com','Market St, SF',NULL,'Passport','USP223344','United States'),
('Mia Thomas','6767676767','mia.t@example.com','Harbour Rd, Auckland','GST334455','Passport','NZP334455','New Zealand'),
('James Lee','5656565656','james.l@example.com','Orchard Rd, Singapore','GST556677','Passport','SGP445566','Singapore'),
('Isabella Harris','4545454545','isa.h@example.com','Champs Elysees, Paris',NULL,'Passport','FRP667788','France'),
('Lucas Clark','3434343434','lucas.c@example.com','Oxford St, London',NULL,'Passport','UKP334455','United Kingdom');

DELETE FROM ROOMS;

INSERT INTO ROOMS (room_no, room_type, floor, beds, ac, price, status, description) VALUES
('101','Single',1,1,TRUE,2500.00,'Available','Standard single room.'),
('102','Double',1,2,TRUE,3500.00,'Occupied','Standard double room.'),
('201','Suite',2,2,TRUE,7500.00,'Occupied','Luxury suite with a view.'),
('202','Twin',2,2,FALSE,2800.00,'Under Maintenance','Room with twin beds.'),
('301','Single',3,1,TRUE,2600.00,'Available','Single room with city view.'),
('302','Double',3,2,TRUE,3600.00,'Available','Spacious double room.'),
('303','Deluxe',3,1,TRUE,4200.00,'Available','Deluxe single with balcony.'),
('304','Suite',3,2,TRUE,8200.00,'Cleaning','Premium suite corner room.'),
('401','Twin',4,2,TRUE,3100.00,'Occupied','Twin beds with AC.'),
('402','Double',4,2,TRUE,3800.00,'Available','Double with garden view.'),
('403','Single',4,1,FALSE,2200.00,'Maintenance','Non-AC budget single.'),
('404','Deluxe',4,1,TRUE,4500.00,'Available','Deluxe high-floor single.');

DELETE FROM STAFF;

INSERT INTO STAFF (first_name, last_name, role, department, floor, join_date, is_active, email, phone) VALUES
('Jane','Doe','Manager','Management',0,'2022-01-15',TRUE,'jane.doe@hotel.com','9876543210'),
('John','Smith','Clerk','Front Office',1,'2023-03-20',TRUE,'john.smith@hotel.com','8765432109'),
('Alice','Williams','Housekeeper','Housekeeping',1,'2024-05-10',TRUE,'alice.w@hotel.com','7654321098'),
('Bob','Wilson','Accountant','Accounting',0,'2023-08-01',TRUE,'bob.w@hotel.com','9345678901'),
('Priya','Sharma','Housekeeper','Housekeeping',3,'2024-10-11',TRUE,'priya.s@hotel.com','9123456780'),
('Ravi','Patel','Technician','Maintenance',0,'2024-02-05',TRUE,'ravi.p@hotel.com','9234567810');

DELETE FROM REFERENCE_SOURCES;

INSERT INTO REFERENCE_SOURCES (source_name, contact, commission_percentage, fixed_amount, notes) VALUES
('Expedia','travel.agency@expedia.com',15.00,0.00,'Online travel agent.'),
('Walk-in',NULL,0.00,0.00,'Direct booking from a walk-in guest.'),
('Corporate Client','corp.client@biz.com',10.00,500.00,'Corporate agreement.'),
('Booking.com','partners@booking.com',15.00,0.00,'OTA partner.'),
('MakeMyTrip','b2b@makemytrip.com',12.50,0.00,'Domestic OTA.');

DELETE FROM SERVICES;

INSERT INTO SERVICES (name, type, description, price, active) VALUES
('Laundry Service','Laundry','Standard wash and fold.',500.00,TRUE),
('Room Service','Food & Beverage','24-hour in-room dining.',0.00,TRUE),
('Airport Shuttle','Transportation','One-way airport transfer.',1200.00,TRUE),
('Spa Treatment','Wellness','Relaxing spa session.',2500.00,TRUE),
('Mini Bar','Food & Beverage','In-room mini bar items.',0.00,TRUE),
('City Tour','Experience','Half-day city tour.',1800.00,TRUE);

DELETE FROM RESERVATIONS;

INSERT INTO RESERVATIONS (guest_id, room_no, room_type, start_date, end_date, status, num_guests, notes, source_id, advance_paid, payment_status) VALUES
(5,'304','Suite','2025-09-01','2025-09-05','Confirmed',2,'Anniversary stay.',1, 1500.00, 'Partial'),
(7,'302','Double','2025-08-26','2025-08-29','Confirmed',2,'Late arrival.',4, 2000.00, 'Partial'),
(9,'402','Double','2025-09-10','2025-09-14','Pending',2,'Corporate rate.',3, 0.00, 'Pending'),
(10,'404','Deluxe','2025-08-23','2025-08-27','Confirmed',1,'High floor.',5, 500.00, 'Partial'),
(13,'201','Suite','2025-09-02','2025-09-07','Confirmed',2,'Premium view.',1, 0.00, 'Pending'),
(14,'303','Deluxe','2025-08-29','2025-09-01','Confirmed',2,'Deluxe preferred.',4, 1000.00, 'Partial'),
(1,'101','Single','2025-09-20','2025-09-25','Confirmed',1,'Direct booking, walk-in.',2, 0.00, 'Pending'),
(2,'102','Double','2025-09-21','2025-09-24','Pending',2,'Expedia booking.',1, 0.00, 'Pending');

DELETE FROM BOOKINGS;

INSERT INTO BOOKINGS (reservation_id, guest_id, room_no, check_in_date, check_out_date, total_amount, advance_paid, payment_status, status, nationality, rate_per_night, document_link, gst_rate, base_amount, gst_amount, gst_included) VALUES
(NULL,1,'101','2025-08-20','2025-08-25',12500.00,2500.00,'Partial','Checked-in', 'India', 2500.00, NULL, 18.00, 10593.22, 1906.78, FALSE),
(NULL,2,'102','2025-08-21','2025-08-24',21000.00,7000.00,'Partial','Confirmed', 'India', 3500.00, NULL, 18.00, 17796.61, 3203.39, FALSE),
(NULL,4,'301','2025-08-22','2025-08-24',7800.00,2000.00,'Partial','Checked-in', 'India', 2600.00, NULL, 18.00, 6610.17, 1189.83, FALSE),
(NULL,8,'401','2025-08-21','2025-08-24',9300.00,9300.00,'Paid','Checked-in', 'India', 3100.00, NULL, 18.00, 7881.36, 1418.64, FALSE),
(NULL,11,'101','2025-07-10','2025-07-12',5000.00,5000.00,'Paid','Completed', 'India', 2500.00, NULL, 18.00, 4237.29, 762.71, FALSE),
(NULL,12,'102','2025-07-14','2025-07-18',14000.00,14000.00,'Paid','Completed', 'India', 3500.00, NULL, 18.00, 11864.41, 2135.59, FALSE);

DELETE FROM PAYMENTS;

INSERT INTO PAYMENTS (booking_id, guest_id, amount, method, transaction_id, payer, notes, payment_date) VALUES
(1,1,2500.00,'Credit Card','TXN123456789','John Doe','Advance payment for booking.','2025-08-19 15:30:00'),
(2,2,7000.00,'Credit Card','TXN888111','Jane Smith','Partial prepayment.','2025-08-21 14:40:00'),
(4,8,9300.00,'Debit Card','DC555666','Sophia Martinez','Full on check-in.','2025-08-19 12:05:00'),
(5,11,5000.00,'Credit Card','TXN445566','Noah Anderson','Paid.','2025-07-09 10:00:00'),
(6,12,14000.00,'Credit Card','TXN778899','Mia Thomas','Paid.','2025-07-13 12:30:00');

DELETE FROM INVOICES;

INSERT INTO INVOICES (booking_id, guest_id, invoice_number, invoice_date, subtotal, gst, total, paid_amount, due_amount) VALUES
(1,1,'INV-2025-001','2025-08-20',10593.22,1906.78,12500.00,2500.00,10000.00),
(2,2,'INV-2025-004','2025-08-22',17796.61,3203.39,21000.00,7000.00,14000.00),
(4,8,'INV-2025-008','2025-08-19',7906.78,1393.22,9300.00,9300.00,0.00),
(5,11,'INV-2025-011','2025-07-10',4237.29,762.71,5000.00,5000.00,0.00),
(6,12,'INV-2025-012','2025-07-14',11864.41,2135.59,14000.00,14000.00,0.00);

DELETE FROM ATTENDANCE;

INSERT INTO ATTENDANCE (staff_id, attendance_date, status, check_in, check_out, notes) VALUES
(2,'2025-08-20','Present','09:00:00','18:00:00','On time.'),
(3,'2025-08-20','Present','08:30:00','17:30:00','Cleaned rooms 101, 102.'),
(2,'2025-08-21','Present','08:55:00',NULL,NULL),
(5,'2025-08-21','Present','09:15:00','18:10:00','Deep clean level 3.'),
(6,'2025-08-21','Present','10:00:00','19:00:00','AC filters replaced.');

DELETE FROM HOUSEKEEPING;

INSERT INTO HOUSEKEEPING (room_no, assigned_staff_id, task_type, status, assigned_at, completed_at, notes) VALUES
('101',(SELECT id FROM staff WHERE first_name = 'Alice'),'Cleaning','Completed','2025-09-29 08:00:00','2025-09-29 10:00:00','Daily cleaning completed'),
('102',(SELECT id FROM staff WHERE first_name = 'Alice'),'Cleaning','In Progress','2025-09-29 09:00:00',NULL,'Guest checkout cleaning'),
('201',(SELECT id FROM staff WHERE first_name = 'Priya'),'Inspection','Pending','2025-09-29 10:00:00',NULL,'Pre-arrival inspection'),
('202',(SELECT id FROM staff WHERE first_name = 'Priya'),'Maintenance','In Progress','2025-09-29 09:30:00',NULL,'AC repair work'),
('301',(SELECT id FROM staff WHERE first_name = 'Priya'),'Deep Clean','Pending','2025-09-29 11:00:00',NULL,'Suite deep cleaning');

DELETE FROM RESERVATION_PAYMENTS;

INSERT INTO RESERVATION_PAYMENTS (reservation_id, amount, method, notes) VALUES
(1, 1500.00, 'Credit Card', 'Advance payment for anniversary stay.'),
(2, 2000.00, 'Online Transfer', 'Prepayment for Booking.com reservation.'),
(4, 500.00, 'Cash', 'Partial payment upon reservation.'),
(6, 1000.00, 'Credit Card', 'Advance payment via Booking.com.');

DELETE FROM hotel_expenses;

INSERT INTO hotel_expenses (expense_date, category, subcategory, description, amount, payment_method, vendor_name, vendor_contact, department, approved_by, paid_by, room_no, gst_applicable, gst_rate, gst_amount, net_amount, status, notes) VALUES
('2025-09-25', 'Maintenance & Repairs', 'AC Servicing & Repair', 'Monthly AC maintenance for all rooms', 15000.00, 'Bank Transfer', 'Cool Air Services', 'coolair@services.com', 'Maintenance', 1, 1, NULL, TRUE, 18.00, 2288.14, 12711.86, 'Paid', 'Quarterly maintenance contract'),
('2025-09-24', 'Maintenance & Repairs', 'Plumbing Work', 'Bathroom fixture replacement Room 202', 3500.00, 'Cash', 'City Plumbers', '9876543210', 'Maintenance', 1, 1, '202', TRUE, 18.00, 533.90, 2966.10, 'Paid', 'Washbasin and faucet replaced'),
('2025-09-23', 'Utilities & Bills', 'Electricity Bill', 'Monthly electricity consumption', 25000.00, 'Online Transfer', 'State Electricity Board', 'seb@gov.in', 'Administration', 1, 4, NULL, FALSE, 0.00, 0.00, 25000.00, 'Paid', 'September 2025 bill'),
('2025-09-23', 'Utilities & Bills', 'Internet & WiFi', 'High-speed internet service', 4500.00, 'Auto Debit', 'Fiber Net Services', 'support@fibernet.com', 'Administration', 1, 1, NULL, TRUE, 18.00, 686.44, 3813.56, 'Paid', 'Monthly internet bill'),
('2025-09-22', 'Staff Expenses', 'Monthly Salaries', 'September salary for housekeeping staff', 45000.00, 'Bank Transfer', 'Staff Payroll', NULL, 'Human Resources', 1, 1, NULL, FALSE, 0.00, 0.00, 45000.00, 'Paid', 'Salary for 6 housekeeping staff'),
('2025-09-21', 'Staff Expenses', 'Staff Uniforms', 'New uniforms for front office staff', 8000.00, 'Credit Card', 'Hotel Uniform Supply', '8765432109', 'Human Resources', 1, 1, NULL, TRUE, 12.00, 857.14, 7142.86, 'Paid', 'Uniforms for 4 reception staff'),
('2025-09-20', 'Supplies & Materials', 'Cleaning Supplies', 'Monthly cleaning supplies stock', 6500.00, 'Cash', 'Clean World Suppliers', '7654321098', 'Housekeeping', 1, 1, NULL, TRUE, 12.00, 696.43, 5803.57, 'Paid', 'Detergents, disinfectants, toilet paper'),
('2025-09-19', 'Supplies & Materials', 'Guest Toiletries', 'Bathroom amenities restocking', 12000.00, 'Bank Transfer', 'Hotel Amenities Co', 'sales@amenities.com', 'Housekeeping', 1, 1, NULL, TRUE, 12.00, 1285.71, 10714.29, 'Paid', 'Shampoo, soap, towels for all rooms'),
('2025-09-18', 'Marketing & Advertising', 'Online Advertising', 'Google Ads campaign for October', 10000.00, 'Credit Card', 'Digital Marketing Pro', 'contact@digitalmarketing.com', 'Marketing', 1, 1, NULL, TRUE, 18.00, 1525.42, 8474.58, 'Paid', 'Google Ads and Facebook marketing'),
('2025-09-17', 'Technology & Software', 'Software Licenses', 'Hotel management software renewal', 18000.00, 'Bank Transfer', 'HotelTech Solutions', 'billing@hoteltech.com', 'Administration', 1, 1, NULL, TRUE, 18.00, 2745.76, 15254.24, 'Paid', 'Annual license renewal');

DELETE FROM return_payments;

INSERT INTO return_payments (
return_date, booking_id, guest_id, original_payment_id, return_type, return_reason,
original_amount, return_amount, deduction_amount, return_method, transaction_id,
reference_number, processed_by, approved_by, gst_return_amount, base_return_amount,
processing_fee, bank_account_details, status, notes, created_at, processed_at, completed_at
) VALUES
('2025-08-24', 1, 1, 1, 'Partial Refund', 'Early Checkout',
12500.00, 2500.00, 0.00, 'Credit Card Reversal', 'REV-TXN123456789',
'RET-2025-001', 2, 1, 381.36, 2118.64,
0.00, NULL, 'Completed',
'Guest checked out 1 day early. Refunded 1 night charge (₹2,500). Original booking: 5 nights, stayed 4 nights.',
'2025-08-24 10:30:00', '2025-08-24 11:15:00', '2025-08-24 14:30:00'),
('2025-08-20', 2, 2, 2, 'Partial Refund', 'Cancellation',
21000.00, 5250.00, 1750.00, 'Bank Transfer', 'BT-REF-888111',
'RET-2025-002', 2, 1, 800.85, 4449.15,
50.00, 'SBI Acc: 123456789, IFSC: SBIN0001234', 'Completed',
'Booking cancelled 1 day before check-in. Applied 25% cancellation charge. Original advance ₹7,000, refunding ₹5,250 after charges.',
'2025-08-20 16:45:00', '2025-08-20 17:20:00', '2025-08-21 10:00:00');

DELETE FROM vendors;

INSERT INTO vendors (vendor_name, contact_person, email, phone, address, gst_number, payment_terms, credit_limit, active) VALUES
('Premium Linen Suppliers', 'Rajesh Kumar', 'rajesh@premiumlinen.com', '9876543210', '123 Textile Street, Mumbai, Maharashtra 400001', '27ABCDE1234F1Z5', 'Net 30', 100000.00, TRUE),
('Hotel Amenities India', 'Priya Sharma', 'priya@hotelamenitiesi.com', '9876543211', '456 Supply Road, Delhi, NCR 110001', '07FGHIJ5678K2L6', 'Net 15', 75000.00, TRUE),
('CleanMax Solutions', 'Amit Singh', 'amit@cleanmax.com', '9876543212', '789 Clean Street, Pune, Maharashtra 411001', '27KLMNO9012P3Q7', 'COD', 50000.00, TRUE),
('TechnoElectro Hub', 'Sunita Patel', 'sunita@technoelectro.com', '9876543213', '321 Tech Park, Bangalore, Karnataka 560001', '29RSTUV3456W8X9', 'Net 45', 150000.00, TRUE);

DELETE FROM inventory_items;

INSERT INTO inventory_items (item_name, category, location, description, available_quantity, reserved_quantity, min_stock_level, max_stock_level, unit_price, unit, supplier, supplier_contact, sku, barcode, updated_by) VALUES
('Premium Cotton Bed Sheet Set', 'Linen', 'Main Store', 'Premium 100% cotton bed sheet set with pillow covers', 45, 5, 20, 200, 850.00, 'sets', 'Premium Linen Suppliers', '9876543210', 'LIN001', '1234567890001', 'admin'),
('Luxury Bath Towel', 'Linen', 'Main Store', 'Premium cotton bath towels 70x140cm, 500 GSM', 82, 18, 30, 300, 450.00, 'pieces', 'Premium Linen Suppliers', '9876543210', 'LIN002', '1234567890002', 'admin'),
('Hand Towel Set', 'Linen', 'Main Store', 'Cotton hand towels 40x60cm, 400 GSM', 148, 12, 40, 400, 280.00, 'pieces', 'Premium Linen Suppliers', '9876543210', 'LIN003', '1234567890003', 'admin');

-- Show summary of inserted data
SELECT 'Data Summary' as Info;

SELECT 'Core Data' as Category,
(SELECT COUNT(*) FROM USERS) as Users,
(SELECT COUNT(*) FROM guests) as Guests,
(SELECT COUNT(*) FROM rooms) as Rooms,
(SELECT COUNT(*) FROM staff) as Staff,
(SELECT COUNT(*) FROM services) as Services;
