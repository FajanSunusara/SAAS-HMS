-- USERS
DELETE FROM USERS;
INSERT INTO USERS(username, password_hash, role, full_name, email, status) VALUES
( 'admin', 'password123', 'ADMIN', 'Admin User', 'admin@example.com', 'ACTIVE'),
( 'manager1', 'password123', 'MANAGER', 'Manager One', 'manager1@example.com', 'ACTIVE'),
( 'reception1', 'password123', 'STAFF', 'Reception Staff', 'reception1@example.com', 'ACTIVE'),
( 'staff1', 'password123', 'STAFF', 'Staff Member', 'staff1@example.com', 'ACTIVE'),
( 'manager2', 'password123', 'MANAGER', 'Manager Two', 'manager2@example.com', 'ACTIVE'),
( 'reception2', 'password123', 'STAFF', 'Receptionist Two', 'reception2@example.com', 'ACTIVE'),
( 'housekeep1', 'password123', 'STAFF', 'HK Senior', 'hk1@example.com', 'ACTIVE'),
( 'account1', 'password123', 'ACCOUNTANT', 'Accounts One', 'acc1@example.com', 'ACTIVE');

-- GUESTS
DELETE FROM GUESTS;
INSERT INTO GUESTS(name, phone, email, address, gst_number, id_type, id_number) VALUES
( 'John Doe', '1234567890', 'john.doe@example.com', '123 Main St, Anytown', 'GST123456', 'Passport', 'P1234567'),
( 'Jane Smith', '0987654321', 'jane.smith@example.com', '456 Oak Ave, Somewhere', 'GST654321', 'Driver License', 'D7654321'),
( 'Michael Brown', '1112223333', 'michael.b@example.com', '789 Pine Rd, Nowhere', NULL, 'Passport', 'P9988776'),
( 'Emily White', '2223334444', 'emily.w@example.com', '987 Elm St, Hometown', 'GST444444', 'Aadhar Card', 'A555555'),
( 'Robert Johnson', '5551112233', 'robert.j@example.com', '12 Baker St, London', NULL, 'Passport', 'UKP112233'),
( 'Olivia Davis', '7778889999', 'olivia.d@example.com', '22 Kings Rd, Sydney', 'GST778899', 'Passport', 'AUS998877'),
( 'William Garcia', '9090808070', 'william.g@example.com', '21 5th Ave, NYC', NULL, 'Driver License', 'NYD774411'),
( 'Sophia Martinez', '8080909070', 'sophia.m@example.com', 'Via Roma 13, Rome', NULL, 'Passport', 'ITP778899'),
( 'Liam Wilson', '6060504030', 'liam.w@example.com', 'Queen St, Toronto', 'GST998877', 'Passport', 'CAP556677'),
( 'Ava Taylor', '9898989898', 'ava.t@example.com', 'MG Road, Bengaluru', 'GST112233', 'Aadhar Card', 'AAD112233'),
( 'Noah Anderson', '7878787878', 'noah.a@example.com', 'Market St, SF', NULL, 'Passport', 'USP223344'),
( 'Mia Thomas', '6767676767', 'mia.t@example.com', 'Harbour Rd, Auckland', 'GST334455', 'Passport', 'NZP334455'),
( 'James Lee', '5656565656', 'james.l@example.com', 'Orchard Rd, Singapore', 'GST556677', 'Passport', 'SGP445566'),
( 'Isabella Harris', '4545454545', 'isa.h@example.com', 'Champs Elysees, Paris', NULL, 'Passport', 'FRP667788'),
( 'Lucas Clark', '3434343434', 'lucas.c@example.com', 'Oxford St, London', NULL, 'Passport', 'UKP334455');

-- ROOMS
DELETE FROM ROOMS;
INSERT INTO ROOMS(room_no, room_type, floor, beds, ac, price, status, description) VALUES
( '101', 'Single', 1, 1, TRUE, 2500.00, 'Available', 'Standard single room.'),
( '102', 'Double', 1, 2, TRUE, 3500.00, 'Available', 'Standard double room.'),
( '201', 'Suite', 2, 2, TRUE, 7500.00, 'Occupied', 'Luxury suite with a view.'),
( '202', 'Twin', 2, 2, FALSE, 2800.00, 'Under Maintenance', 'Room with twin beds.'),
( '301', 'Single', 3, 1, TRUE, 2600.00, 'Available', 'Single room with city view.'),
( '302', 'Double', 3, 2, TRUE, 3600.00, 'Available', 'Spacious double room.'),
( '303', 'Deluxe', 3, 1, TRUE, 4200.00, 'Available', 'Deluxe single with balcony.'),
( '304', 'Suite', 3, 2, TRUE, 8200.00, 'Cleaning', 'Premium suite corner room.'),
( '401', 'Twin', 4, 2, TRUE, 3100.00, 'Occupied', 'Twin beds with AC.'),
( '402', 'Double', 4, 2, TRUE, 3800.00, 'Available', 'Double with garden view.'),
( '403', 'Single', 4, 1, FALSE, 2200.00, 'Maintenance', 'Non-AC budget single.'),
( '404', 'Deluxe', 4, 1, TRUE, 4500.00, 'Available', 'Deluxe high-floor single.');

-- STAFF
DELETE FROM STAFF;
INSERT INTO STAFF(first_name, last_name, role, department, floor, join_date, is_active, email, phone) VALUES
( 'Jane', 'Doe', 'Manager', 'Management', 0, '2022-01-15', TRUE, 'jane.doe@hotel.com', '9876543210'),
( 'John', 'Smith', 'Clerk', 'Front Office', 1, '2023-03-20', TRUE, 'john.smith@hotel.com', '8765432109'),
( 'Alice', 'Williams', 'Housekeeper', 'Housekeeping', 1, '2024-05-10', TRUE, 'alice.w@hotel.com', '7654321098'),
( 'Bob', 'Wilson', 'Accountant', 'Accounting', 0, '2023-08-01', TRUE, 'bob.w@hotel.com', '9345678901'),
( 'Priya', 'Sharma', 'Housekeeper', 'Housekeeping', 3, '2024-10-11', TRUE, 'priya.s@hotel.com', '9123456780'),
( 'Ravi', 'Patel', 'Technician', 'Maintenance', 0, '2024-02-05', TRUE, 'ravi.p@hotel.com', '9234567810');

-- REFERENCE_SOURCES
DELETE FROM REFERENCE_SOURCES;
INSERT INTO REFERENCE_SOURCES(source_name, contact, commission_percentage, fixed_amount, notes) VALUES
( 'Expedia', 'travel.agency@expedia.com', 15.00, 0.00, 'Online travel agent.'),
( 'Walk-in', NULL, 0.00, 0.00, 'Direct booking from a walk-in guest.'),
( 'Corporate Client', 'corp.client@biz.com', 10.00, 500.00, 'Corporate agreement.'),
( 'Booking.com', 'partners@booking.com', 15.00, 0.00, 'OTA partner.'),
( 'MakeMyTrip', 'b2b@makemytrip.com', 12.50, 0.00, 'Domestic OTA.');

-- SERVICES
DELETE FROM SERVICES;
INSERT INTO SERVICES(name, type, description, price, active) VALUES
( 'Laundry Service', 'Laundry', 'Standard wash and fold.', 500.00, TRUE),
( 'Room Service', 'Food & Beverage', '24-hour in-room dining.', 0.00, TRUE),
( 'Airport Shuttle', 'Transportation', 'One-way airport transfer.', 1200.00, TRUE),
( 'Spa Treatment', 'Wellness', 'Relaxing spa session.', 2500.00, TRUE),
( 'Mini Bar', 'Food & Beverage', 'In-room mini bar items.', 0.00, TRUE),
( 'City Tour', 'Experience', 'Half-day city tour.', 1800.00, TRUE);

-- RESERVATIONS
DELETE FROM RESERVATIONS;
INSERT INTO RESERVATIONS(guest_id, room_no, room_type, start_date, end_date, status, num_guests, notes) VALUES
( '101', 'Single', '2025-08-20', '2025-08-25', 'Confirmed', 1, 'Quiet room.'),
( '102', 'Double', '2025-08-22', '2025-08-28', 'Pending', 2, 'Via Expedia.'),
( '201', 'Suite', '2025-08-25', '2025-08-31', 'Confirmed', 2, 'Extra bed.'),
( '301', 'Single', '2025-08-28', '2025-08-30', 'Confirmed', 1, 'Business trip.'),
( '304', 'Suite', '2025-09-01', '2025-09-05', 'Confirmed', 2, 'Anniversary stay.'),
( '303', 'Deluxe', '2025-08-18', '2025-08-21', 'Cancelled', 2, 'Changed plans.'),
( '302', 'Double', '2025-08-26', '2025-08-29', 'Confirmed', 2, 'Late arrival.'),
( '401', 'Twin', '2025-08-19', '2025-08-22', 'Confirmed', 2, 'Twin preferred.'),
( '402', 'Double', '2025-09-10', '2025-09-14', 'Pending', 2, 'Corporate rate.'),
( '404', 'Deluxe', '2025-08-23', '2025-08-27', 'Confirmed', 1, 'High floor.'),
( '101', 'Single', '2025-07-10', '2025-07-12', 'Completed', 1, 'Short trip.'),
( '102', 'Double', '2025-07-14', '2025-07-18', 'Completed', 2, 'Family.'),
( '201', 'Suite', '2025-09-02', '2025-09-07', 'Confirmed', 2, 'Premium view.'),
( '301', 'Single', '2025-08-15', '2025-08-18', 'Completed', 1, 'City break.'),
( '303', 'Deluxe', '2025-08-29', '2025-09-01', 'Confirmed', 2, 'Deluxe preferred.');

-- BOOKINGS
DELETE FROM BOOKINGS;
INSERT INTO BOOKINGS (reservation_id, guest_id, room_no, check_in_date, check_out_date, total_amount, advance_paid, payment_status, status) VALUES
( '101', '2025-08-20', '2025-08-25', 12500.00, 2500.00, 'Partial', 'Checked-in'),
( '201', '2025-08-25', '2025-08-31', 45000.00, 45000.00, 'Paid', 'Confirmed'),
( NULL, 4, '302', '2025-08-29', '2025-09-02', 14400.00, 5000.00, 'Partial', 'Reserved'),
( '102', '2025-08-22', '2025-08-28', 21000.00, 0.00, 'Pending', 'Reserved'),
( '304', '2025-09-01', '2025-09-05', 32800.00, 8000.00, 'Partial', 'Confirmed'),
( '303', '2025-08-18', '2025-08-21', 12600.00, 0.00, 'Pending', 'Cancelled'),
( '302', '2025-08-26', '2025-08-29', 10800.00, 3000.00, 'Partial', 'Confirmed'),
( '401', '2025-08-19', '2025-08-22', 9300.00, 9300.00, 'Paid', 'Checked-in'),
( '402', '2025-09-10', '2025-09-14', 15200.00, 0.00, 'Pending', 'Reserved'),
( '404', '2025-08-23', '2025-08-27', 18000.00, 6000.00, 'Partial', 'Checked-in'),
( '101', '2025-07-10', '2025-07-12', 5000.00, 5000.00, 'Paid', 'Completed'),
( '102', '2025-07-14', '2025-07-18', 14000.00, 14000.00, 'Paid', 'Completed'),
( '201', '2025-09-02', '2025-09-07', 37500.00, 10000.00, 'Partial', 'Confirmed'),
( '301', '2025-08-15', '2025-08-18', 7800.00, 7800.00, 'Paid', 'Completed'),
( '303', '2025-08-29', '2025-09-01', 13500.00, 4500.00, 'Partial', 'Reserved');

-- PAYMENTS
DELETE FROM PAYMENTS;
INSERT INTO PAYMENTS (booking_id, guest_id, amount, method, transaction_id, payer, notes, payment_date) VALUES
( 2500.00, 'Credit Card', 'TXN123456789', 'John Doe', 'Advance payment for booking.', '2025-08-19 15:30:00'),
( 45000.00, 'Bank Transfer', 'BT987654321', 'Emily White', 'Full payment for the suite booking.', '2025-08-24 10:00:00'),
( 3000.00, 'UPI', 'UPI12345', 'Emily White', 'Advance for reservation.', '2025-08-25 11:15:00'),
( 2000.00, 'Cash', 'CASH56789', 'Emily White', 'Top-up advance.', '2025-08-26 09:20:00'),
( 7000.00, 'Credit Card', 'TXN888111', 'Jane Smith', 'Partial prepayment.', '2025-08-21 14:40:00'),
( 3000.00, 'Credit Card', 'TXN222333', 'William Garcia', 'Advance.', '2025-08-24 16:50:00'),
( 9300.00, 'Debit Card', 'DC555666', 'Sophia Martinez', 'Full on check-in.', '2025-08-19 12:05:00'),
( 6000.00, 'UPI', 'UPI98989', 'Ava Taylor', 'Advance for deluxe.', '2025-08-22 18:00:00'),
( 5000.00, 'Credit Card', 'TXN445566', 'Noah Anderson', 'Paid.', '2025-07-09 10:00:00'),
( 14000.00, 'Credit Card', 'TXN778899', 'Mia Thomas', 'Paid.', '2025-07-13 12:30:00'),
( 10000.00, 'Bank Transfer', 'BT223344', 'James Lee', 'Advance.', '2025-08-30 09:45:00'),
( 7800.00, 'Cash', 'CASH334455', 'Isabella Harris', 'Settled on checkout.', '2025-08-18 11:59:00'),
( 4500.00, 'Credit Card', 'TXN991122', 'Lucas Clark', 'Advance deluxe.', '2025-08-27 08:25:00');

-- INVOICES
DELETE FROM INVOICES;
INSERT INTO INVOICES (booking_id, guest_id, invoice_number, invoice_date, subtotal, gst, total, paid_amount, due_amount) VALUES
( 'INV-2025-001', '2025-08-20', 10593.22, 1906.78, 12500.00, 2500.00, 10000.00),
( 'INV-2025-002', '2025-08-25', 38135.59, 6864.41, 45000.00, 45000.00, 0.00),
( 'INV-2025-003', '2025-08-29', 12203.39, 2196.61, 14400.00, 5000.00, 9400.00),
( 'INV-2025-004', '2025-08-22', 17796.61, 3203.39, 21000.00, 7000.00, 14000.00),
( 'INV-2025-005', '2025-09-01', 27881.36, 4918.64, 32800.00, 8000.00, 24800.00),
( 'INV-2025-006', '2025-08-18', 10677.97, 1922.03, 12600.00, 0.00, 12600.00),
( 'INV-2025-007', '2025-08-26', 9152.54, 1647.46, 10800.00, 3000.00, 7800.00),
( 'INV-2025-008', '2025-08-19', 7906.78, 1393.22, 9300.00, 9300.00, 0.00),
( 'INV-2025-009', '2025-09-10', 12915.25, 2284.75, 15200.00, 0.00, 15200.00),
( 'INV-2025-010', '2025-08-23', 15254.24, 2745.76, 18000.00, 6000.00, 12000.00),
( 'INV-2025-011', '2025-07-10', 4237.29, 762.71, 5000.00, 5000.00, 0.00),
( 'INV-2025-012', '2025-07-14', 11864.41, 2135.59, 14000.00, 14000.00, 0.00),
( 'INV-2025-013', '2025-09-02', 30847.46, 5647.54, 37500.00, 10000.00, 27500.00),
( 'INV-2025-014', '2025-08-15', 6610.17, 1189.83, 7800.00, 7800.00, 0.00),
( 'INV-2025-015', '2025-08-29', 11440.68, 2059.32, 13500.00, 4500.00, 9000.00);

-- ATTENDANCE
DELETE FROM ATTENDANCE;
INSERT INTO ATTENDANCE (staff_id, attendance_date, status, check_in, check_out, notes) VALUES
( '2025-08-20', 'Present', '09:00:00', '18:00:00', 'On time.'),
( '2025-08-20', 'Present', '08:30:00', '17:30:00', 'Cleaned rooms 101, 102.'),
( '2025-08-21', 'Present', '08:55:00', NULL, NULL),
( '2025-08-21', 'Present', '09:15:00', '18:10:00', 'Deep clean level 3.'),
( '2025-08-21', 'Present', '10:00:00', '19:00:00', 'AC filters replaced.');

-- HOUSEKEEPING
DELETE FROM HOUSEKEEPING;
INSERT INTO HOUSEKEEPING (room_no, assigned_staff_id, task_type, status, assigned_at, completed_at, notes) VALUES
( '101', 3, 'Cleaning', 'In Progress', '2025-08-21 09:00:00', NULL, 'Post check-out clean.'),
( '201', 3, 'Cleaning', 'Pending', '2025-08-21 11:00:00', NULL, 'Routine clean before arrival.'),
( '304', 5, 'Cleaning', 'Completed', '2025-08-31 10:00:00', '2025-08-31 12:00:00', 'Suite deep clean done.'),
( '403', 6, 'Maintenance', 'In Progress', '2025-08-22 09:30:00', NULL, 'Electrical inspection.'),
( '304', 3, 'Turn-down', 'Pending', '2025-09-01 18:00:00', NULL, 'Evening service.');

-- End of seed
