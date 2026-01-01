
CREATE TABLE IF NOT EXISTS USERS (
    id IDENTITY PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255),
    status VARCHAR(50),
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS guests (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    address VARCHAR(500),
    gst_number VARCHAR(15),
    id_type VARCHAR(50),
    id_number VARCHAR(255),
    nationality VARCHAR(100) -- ADD THIS LINE
);


CREATE TABLE IF NOT EXISTS rooms (
    id IDENTITY PRIMARY KEY,
    room_no VARCHAR(10) NOT NULL UNIQUE,
    room_type VARCHAR(50) NOT NULL,
    floor INT,
    beds INT,
    ac BOOLEAN,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    description VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS staff (
    id IDENTITY PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    floor INT,
    join_date DATE,
    is_active BOOLEAN,
    email VARCHAR(255),
    phone VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS reference_sources (
    id IDENTITY PRIMARY KEY,
    source_name VARCHAR(255) NOT NULL UNIQUE,
    contact VARCHAR(255),
    commission_percentage DECIMAL(5, 2),
    fixed_amount DECIMAL(10, 2),
    notes VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS services (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN
);

CREATE TABLE IF NOT EXISTS reservations (
    id IDENTITY PRIMARY KEY,
    guest_id INT,
    room_no VARCHAR(10),
    room_type VARCHAR(50),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    num_guests INT,
    notes VARCHAR(500),
    source_id INT,
    advance_paid DECIMAL(10,2) DEFAULT 0,
    payment_status VARCHAR(20) DEFAULT 'Pending',
    FOREIGN KEY (guest_id) REFERENCES guests(id),
    FOREIGN KEY (room_no) REFERENCES rooms(room_no),
    FOREIGN KEY (source_id) REFERENCES reference_sources(id)
);

CREATE TABLE IF NOT EXISTS bookings (
    id IDENTITY PRIMARY KEY,
    reservation_id INT,
    guest_id INT,
    room_no VARCHAR(10),
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_amount DECIMAL(10, 2),
    advance_paid DECIMAL(10, 2),
    payment_status VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    nationality VARCHAR(100) DEFAULT 'India',
    rate_per_night DECIMAL(10,2),
    document_link VARCHAR(255),
    gst_rate DECIMAL(5,2) DEFAULT 18.00,
    base_amount DECIMAL(10,2),
    gst_amount DECIMAL(10,2),
    gst_included BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    FOREIGN KEY (guest_id) REFERENCES guests(id),
    FOREIGN KEY (room_no) REFERENCES rooms(room_no)
);

CREATE TABLE IF NOT EXISTS payments (
    id IDENTITY PRIMARY KEY,
    booking_id INT,
    guest_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    payer VARCHAR(255),
    notes VARCHAR(500),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (guest_id) REFERENCES guests(id)
);

CREATE TABLE IF NOT EXISTS invoices (
    id IDENTITY PRIMARY KEY,
    booking_id INT,
    guest_id INT,
    invoice_number VARCHAR(255) NOT NULL UNIQUE,
    invoice_date DATE NOT NULL,
    subtotal DECIMAL(10, 2),
    gst DECIMAL(10, 2),
    total DECIMAL(10, 2) NOT NULL,
    paid_amount DECIMAL(10, 2),
    due_amount DECIMAL(10, 2),
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (guest_id) REFERENCES guests(id)
);

CREATE TABLE IF NOT EXISTS attendance (
    id IDENTITY PRIMARY KEY,
    staff_id INT,
    attendance_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    check_in TIME,
    check_out TIME,
    notes VARCHAR(500),
    FOREIGN KEY (staff_id) REFERENCES staff(id)
);

CREATE TABLE IF NOT EXISTS housekeeping (
    id IDENTITY PRIMARY KEY,
    room_no VARCHAR(10) NOT NULL,
    assigned_staff_id INT,
    task_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP,
    completed_at TIMESTAMP,
    notes VARCHAR(500),
    FOREIGN KEY (room_no) REFERENCES rooms(room_no),
    FOREIGN KEY (assigned_staff_id) REFERENCES staff(id)
);

CREATE TABLE IF NOT EXISTS reservation_payments (
    id IDENTITY PRIMARY KEY,
    reservation_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    method VARCHAR(50),
    notes VARCHAR(500),
    FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

CREATE TABLE IF NOT EXISTS service_used (
    id IDENTITY PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    service_date DATE NOT NULL,
    service VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    total_qty INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE IF NOT EXISTS service_items (
    id IDENTITY PRIMARY KEY,
    service_used_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (service_used_id) REFERENCES service_used(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS room_change_history (
    id IDENTITY PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    old_room_no VARCHAR(10) NOT NULL,
    new_room_no VARCHAR(10) NOT NULL,
    change_reason VARCHAR(255),
    adjustment_amount DECIMAL(10, 2) DEFAULT 0,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE IF NOT EXISTS daily_charges_log (
    id IDENTITY PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    calculation_date DATE NOT NULL,
    days_stayed INT NOT NULL,
    daily_rate DECIMAL(10, 2) NOT NULL,
    charges_incurred DECIMAL(10, 2) NOT NULL,
    pending_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);


CREATE TABLE IF NOT EXISTS hotel_expenses (
    id IDENTITY PRIMARY KEY,
    expense_date DATE NOT NULL,
    category VARCHAR(100) NOT NULL, -- From properties file
    subcategory VARCHAR(100), -- From properties file
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50), -- From properties file
    vendor_name VARCHAR(255),
    vendor_contact VARCHAR(100),
    invoice_number VARCHAR(100),
    transaction_id VARCHAR(255),
    department VARCHAR(50), -- From properties file
    approved_by INT, -- Staff ID who approved
    paid_by INT, -- Staff ID who made payment
      room_no VARCHAR(10), -- For room maintenance, repairs, etc.
  
    gst_applicable BOOLEAN DEFAULT FALSE,
    gst_rate DECIMAL(5,2) DEFAULT 0.00,
    gst_amount DECIMAL(10, 2) DEFAULT 0.00,
    net_amount DECIMAL(10, 2),

    status VARCHAR(50) DEFAULT 'Pending', -- Pending, Approved, Paid, Rejected
    receipt_path VARCHAR(500), -- File path to scanned receipt
    notes VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    

    FOREIGN KEY (approved_by) REFERENCES staff(id),
    FOREIGN KEY (paid_by) REFERENCES staff(id),
    FOREIGN KEY (room_no) REFERENCES rooms(room_no)
);





CREATE TABLE IF NOT EXISTS return_payments (
    id IDENTITY PRIMARY KEY,
    return_date DATE NOT NULL,
    booking_id INT, -- Links to existing booking
    guest_id INT NOT NULL, -- Links to existing guest
    original_payment_id INT, -- Links to original payment
    return_type VARCHAR(50) NOT NULL, -- Full Refund, Partial Refund, Cancellation, Adjustment
    return_reason VARCHAR(100) NOT NULL, -- Cancellation, Overbooking, Service Issue, etc.
    original_amount DECIMAL(10, 2),
    return_amount DECIMAL(10, 2) NOT NULL,
    deduction_amount DECIMAL(10, 2) DEFAULT 0.00,
    return_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    reference_number VARCHAR(100),
    processed_by INT,
    approved_by INT,
    gst_return_amount DECIMAL(10, 2) DEFAULT 0.00,
    base_return_amount DECIMAL(10, 2),
    processing_fee DECIMAL(10, 2) DEFAULT 0.00,
    bank_account_details VARCHAR(500),
    status VARCHAR(50) DEFAULT 'Pending',
    notes VARCHAR(1000),
    receipt_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (guest_id) REFERENCES guests(id),
    FOREIGN KEY (original_payment_id) REFERENCES payments(id),
    FOREIGN KEY (processed_by) REFERENCES staff(id),
    FOREIGN KEY (approved_by) REFERENCES staff(id)
);


cREATE TABLE IF NOT EXISTS vendors (
    vendor_id IDENTITY PRIMARY KEY,
    vendor_name VARCHAR(255) NOT NULL UNIQUE,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    address VARCHAR(500),
    gst_number VARCHAR(15),
    payment_terms VARCHAR(100),
    credit_limit DECIMAL(12, 2) DEFAULT 0.00,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS inventory_categories (
    category_id IDENTITY PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS inventory_items (
    item_id IDENTITY PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    category_id INT,
    category VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL DEFAULT 'Main Store',
    description VARCHAR(1000),
    available_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT NOT NULL DEFAULT 10,
    max_stock_level INT NOT NULL DEFAULT 1000,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    unit VARCHAR(50) DEFAULT 'pieces',
    vendor_id INT,
    supplier VARCHAR(255),
    supplier_contact VARCHAR(255),
    barcode VARCHAR(100),
    sku VARCHAR(100),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES inventory_categories(category_id),
    FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id)
);


CREATE TABLE IF NOT EXISTS inventory_transactions (
    transaction_id IDENTITY PRIMARY KEY,
    trans_number VARCHAR(100) NOT NULL UNIQUE,
    item_id INT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2),
    total_value DECIMAL(12, 2),
    location VARCHAR(100),
    reference_number VARCHAR(100),
    reference_type VARCHAR(50),
    reference_id INT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    handled_by VARCHAR(100) NOT NULL,
    notes VARCHAR(1000),
    status VARCHAR(50) DEFAULT 'Completed',
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id)
);


CREATE TABLE IF NOT EXISTS room_assignments (
    assignment_id IDENTITY PRIMARY KEY,
    room_no VARCHAR(10) NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    return_date TIMESTAMP,
    expected_return_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'Assigned',
    assigned_by VARCHAR(100) NOT NULL,
    returned_by VARCHAR(100),
    condition_on_assignment VARCHAR(100) DEFAULT 'Good',
    condition_on_return VARCHAR(100),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_no) REFERENCES rooms(room_no),
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id)
);


CREATE TABLE IF NOT EXISTS purchase_orders (
    po_id IDENTITY PRIMARY KEY,
    po_number VARCHAR(100) NOT NULL UNIQUE,
    vendor_id INT NOT NULL,
    vendor_name VARCHAR(255) NOT NULL,
    vendor_contact VARCHAR(255),
    order_date DATE NOT NULL,
    expected_date DATE,
    delivery_date DATE,
    total_items INT DEFAULT 0,
    total_amount DECIMAL(12, 2) DEFAULT 0.00,
    tax_amount DECIMAL(12, 2) DEFAULT 0.00,
    final_amount DECIMAL(12, 2) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'Draft',
    payment_terms VARCHAR(100),
    shipping_address VARCHAR(500),
    notes VARCHAR(1000),
    created_by VARCHAR(100) NOT NULL,
    approved_by VARCHAR(100),
    received_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id)
);


CREATE TABLE IF NOT EXISTS purchase_order_items (
    poi_id IDENTITY PRIMARY KEY,
    po_id INT NOT NULL,
    item_id INT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    quantity_ordered INT NOT NULL,
    quantity_received INT DEFAULT 0,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    received_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'Pending',
    notes VARCHAR(500),
    FOREIGN KEY (po_id) REFERENCES purchase_orders(po_id),
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id)
);


CREATE TABLE IF NOT EXISTS stock_alerts (
    alert_id IDENTITY PRIMARY KEY,
    item_id INT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    alert_message VARCHAR(500),
    priority VARCHAR(20) DEFAULT 'Medium',
    alert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by VARCHAR(100),
    acknowledged_at TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMP,
    auto_generated BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id)
);


CREATE TABLE IF NOT EXISTS photo_id_verifications (
    id IDENTITY PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    verification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_persons INT NOT NULL,
    main_guest_photo_path VARCHAR(500),
    verified_by VARCHAR(100),
    status VARCHAR(50) DEFAULT 'Pending',
    notes VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE IF NOT EXISTS verified_persons (
    id IDENTITY PRIMARY KEY,
    verification_id BIGINT NOT NULL,
    person_name VARCHAR(255) NOT NULL,
    person_index INT NOT NULL, -- 1 for main guest, 2,3... for others
    id_type VARCHAR(100) NOT NULL,
    id_number VARCHAR(255),
    id_photo_path VARCHAR(500),
    coming_from VARCHAR(255),
    proceeding_to VARCHAR(255),
    contact_number VARCHAR(50),
    relationship_with_guest VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (verification_id) REFERENCES photo_id_verifications(id) ON DELETE CASCADE
);


ALTER TABLE bookings ALTER COLUMN document_link VARCHAR(500);


CREATE INDEX IF NOT EXISTS idx_inventory_items_category ON inventory_items(category);
CREATE INDEX IF NOT EXISTS idx_inventory_items_location ON inventory_items(location);
CREATE INDEX IF NOT EXISTS idx_inventory_items_active ON inventory_items(active);
CREATE INDEX IF NOT EXISTS idx_inventory_items_sku ON inventory_items(sku);
CREATE INDEX IF NOT EXISTS idx_inventory_items_barcode ON inventory_items(barcode);
CREATE INDEX IF NOT EXISTS idx_inventory_items_supplier ON inventory_items(supplier);

CREATE INDEX IF NOT EXISTS idx_inventory_transactions_item_id ON inventory_transactions(item_id);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_date ON inventory_transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_type ON inventory_transactions(transaction_type);

CREATE INDEX IF NOT EXISTS idx_room_assignments_room_no ON room_assignments(room_no);
CREATE INDEX IF NOT EXISTS idx_room_assignments_item_id ON room_assignments(item_id);
CREATE INDEX IF NOT EXISTS idx_room_assignments_status ON room_assignments(status);

CREATE INDEX IF NOT EXISTS idx_purchase_orders_status ON purchase_orders(status);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_vendor ON purchase_orders(vendor_id);

CREATE INDEX IF NOT EXISTS idx_stock_alerts_type ON stock_alerts(alert_type);
CREATE INDEX IF NOT EXISTS idx_stock_alerts_acknowledged ON stock_alerts(acknowledged);






