// src/pages/Payment.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import {
  Plus,
  Download,
  Calendar,
  Eye,
  Printer,
  Mail,
  Search,
  Filter,
  ChevronRight,
  Clock,
  Wallet,
  Smartphone,
  Banknote,
  CreditCard,
  User,
  // ChevronUpDown,
  MoreVertical,
  CheckCircle,
  AlertCircle,
  XCircle,
  ArrowLeft,
  Save,
  Home,
  Building,
  Globe,
  // Check,
  X,
  DollarSign,
  Receipt,
  ChevronDown,
  ChevronUp,
  // FileText,
  Shield,
  QrCode,
  IndianRupee
} from 'lucide-react';

const Payment = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const queryParams = new URLSearchParams(location.search);
  const isPaymentForm = location.pathname === '/payments/new' || location.pathname.includes('/payments/');
  
  // If we're in payment form mode, render the PaymentForm component
  if (isPaymentForm) {
    return <PaymentForm />;
  }
  
  // Otherwise render the Payment Dashboard
  return <PaymentDashboard />;
};

// Payment Dashboard Component
const PaymentDashboard = () => {
  const navigate = useNavigate();
  const [payments, setPayments] = useState([
    {
      id: 'PAY001',
      date: '2024-01-15T10:30:00',
      guestName: 'Rajesh Kumar',
      guestId: 'GST001',
      bookingId: 'BK001',
      invoiceId: 'INV001',
      roomNumber: '101',
      amountPaid: 5000,
      totalBill: 15000,
      balanceDue: 10000,
      paymentMethod: 'CASH',
      status: 'PARTIAL',
      transactionId: null,
      remarks: 'Advance payment'
    },
    {
      id: 'PAY002',
      date: '2024-01-15T14:45:00',
      guestName: 'Priya Sharma',
      guestId: 'GST002',
      bookingId: 'BK002',
      invoiceId: 'INV002',
      roomNumber: '202',
      amountPaid: 12000,
      totalBill: 12000,
      balanceDue: 0,
      paymentMethod: 'UPI',
      status: 'PAID',
      transactionId: 'UPI123456789',
      remarks: 'Full payment via UPI'
    },
    {
      id: 'PAY003',
      date: '2024-01-14T16:20:00',
      guestName: 'Amit Patel',
      guestId: 'GST003',
      bookingId: 'BK003',
      invoiceId: 'INV003',
      roomNumber: '301',
      amountPaid: 8000,
      totalBill: 20000,
      balanceDue: 12000,
      paymentMethod: 'CARD',
      status: 'PARTIAL',
      transactionId: 'CARD987654',
      remarks: 'Credit card payment'
    },
    {
      id: 'PAY004',
      date: '2024-01-14T11:15:00',
      guestName: 'Sneha Reddy',
      guestId: 'GST004',
      bookingId: 'BK004',
      invoiceId: 'INV004',
      roomNumber: '102',
      amountPaid: 0,
      totalBill: 18000,
      balanceDue: 18000,
      paymentMethod: null,
      status: 'PENDING',
      transactionId: null,
      remarks: 'Payment pending'
    },
    {
      id: 'PAY005',
      date: '2024-01-13T09:45:00',
      guestName: 'Vikram Singh',
      guestId: 'GST005',
      bookingId: 'BK005',
      invoiceId: 'INV005',
      roomNumber: '203',
      amountPaid: 15000,
      totalBill: 15000,
      balanceDue: 0,
      paymentMethod: 'BANK',
      status: 'PAID',
      transactionId: 'BNK456789123',
      remarks: 'Bank transfer'
    }
  ]);
  
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState({
    todayCollection: 17000,
    pendingPayments: 40000,
    digitalPayments: 20000,
    cashCollected: 5000,
    totalPayments: 40000,
    paymentCount: 5
  });
  
  const [filters, setFilters] = useState({
    dateRange: 'today',
    status: 'all',
    paymentMethod: 'all',
    search: '',
    startDate: '',
    endDate: ''
  });
  
  const [selectedRows, setSelectedRows] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [sortConfig, setSortConfig] = useState({ key: 'date', direction: 'desc' });
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);
  
  // Format currency
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(amount);
  };
  
  // Format date
  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  };
  
  // Format short date
  const formatShortDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    }).format(date);
  };
  
  // Get status badge
  const getStatusBadge = (status) => {
    const config = {
      PAID: { color: 'bg-green-100 text-green-800 border-green-200', icon: CheckCircle },
      PARTIAL: { color: 'bg-yellow-100 text-yellow-800 border-yellow-200', icon: AlertCircle },
      PENDING: { color: 'bg-red-100 text-red-800 border-red-200', icon: XCircle },
      REFUNDED: { color: 'bg-purple-100 text-purple-800 border-purple-200', icon: CheckCircle }
    };
    
    const StatusConfig = config[status] || config.PENDING;
    const Icon = StatusConfig.icon;
    
    return (
      <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${StatusConfig.color}`}>
        <Icon className="w-3 h-3 mr-1" />
        {status}
      </span>
    );
  };
  
  // Get payment method icon
  const getPaymentMethodIcon = (method) => {
    const icons = {
      CASH: <Wallet className="w-4 h-4 text-green-600" />,
      UPI: <Smartphone className="w-4 h-4 text-blue-600" />,
      CARD: <CreditCard className="w-4 h-4 text-purple-600" />,
      BANK: <Building className="w-4 h-4 text-indigo-600" />,
      ONLINE: <Globe className="w-4 h-4 text-orange-600" />
    };
    return icons[method] || <Wallet className="w-4 h-4 text-gray-600" />;
  };
  
  // Handle filter change
  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setCurrentPage(1);
  };
  
  // Handle search
  const handleSearch = (e) => {
    handleFilterChange('search', e.target.value);
  };
  
  // Handle export
  const handleExport = () => {
    const csvContent = "data:text/csv;charset=utf-8," 
      + "ID,Date,Guest Name,Booking ID,Invoice ID,Amount Paid,Total Bill,Balance Due,Payment Method,Status\n"
      + payments.map(payment => 
          `${payment.id},${formatDate(payment.date)},${payment.guestName},${payment.bookingId},${payment.invoiceId},${payment.amountPaid},${payment.totalBill},${payment.balanceDue},${payment.paymentMethod},${payment.status}`
        ).join("\n");
    
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `payments_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };
  
  // Handle add payment
  const handleAddPayment = () => {
    navigate('/payments/new');
  };
  
  // Handle view invoice
  const handleViewInvoice = (invoiceId) => {
    navigate(`/invoice/${invoiceId}`);
  };
  
  // Handle view receipt
  const handleViewReceipt = (paymentId) => {
    alert(`Generating receipt for payment ${paymentId}`);
    // In real app, this would generate a PDF
  };
  
  // Handle add payment to invoice
  const handleAddPaymentToInvoice = (invoiceId) => {
    navigate(`/payments/new?invoiceId=${invoiceId}`);
  };
  
  // Handle row select
  const handleRowSelect = (paymentId) => {
    setSelectedRows(prev => 
      prev.includes(paymentId) 
        ? prev.filter(id => id !== paymentId)
        : [...prev, paymentId]
    );
  };
  
  // Handle bulk action
  const handleBulkAction = (action) => {
    if (action === 'export') {
      const selectedPayments = payments.filter(p => selectedRows.includes(p.id));
      const csvContent = "data:text/csv;charset=utf-8," 
        + "ID,Date,Guest Name,Booking ID,Invoice ID,Amount Paid,Total Bill,Balance Due,Payment Method,Status\n"
        + selectedPayments.map(payment => 
            `${payment.id},${formatDate(payment.date)},${payment.guestName},${payment.bookingId},${payment.invoiceId},${payment.amountPaid},${payment.totalBill},${payment.balanceDue},${payment.paymentMethod},${payment.status}`
          ).join("\n");
      
      const encodedUri = encodeURI(csvContent);
      const link = document.createElement("a");
      link.setAttribute("href", encodedUri);
      link.setAttribute("download", `selected_payments_${new Date().toISOString().split('T')[0]}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } else if (action === 'print') {
      alert(`Printing receipts for ${selectedRows.length} selected payments`);
    }
  };
  
  // Handle sort
  const handleSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };
  
  // Sort payments
  const sortedPayments = [...payments].sort((a, b) => {
    if (sortConfig.key === 'date') {
      return sortConfig.direction === 'asc' 
        ? new Date(a.date) - new Date(b.date)
        : new Date(b.date) - new Date(a.date);
    }
    if (sortConfig.key === 'amount') {
      return sortConfig.direction === 'asc' 
        ? a.amountPaid - b.amountPaid
        : b.amountPaid - a.amountPaid;
    }
    if (sortConfig.key === 'balance') {
      return sortConfig.direction === 'asc' 
        ? a.balanceDue - b.balanceDue
        : b.balanceDue - a.balanceDue;
    }
    return 0;
  });
  
  // Filter payments
  const filteredPayments = sortedPayments.filter(payment => {
    // Search filter
    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      if (!payment.guestName.toLowerCase().includes(searchLower) &&
          !payment.bookingId.toLowerCase().includes(searchLower) &&
          !payment.invoiceId.toLowerCase().includes(searchLower) &&
          !payment.roomNumber.toLowerCase().includes(searchLower)) {
        return false;
      }
    }
    
    // Status filter
    if (filters.status !== 'all' && payment.status !== filters.status) {
      return false;
    }
    
    // Payment method filter
    if (filters.paymentMethod !== 'all' && payment.paymentMethod !== filters.paymentMethod) {
      return false;
    }
    
    // Date range filter (simplified)
    if (filters.dateRange === 'today') {
      const today = new Date().toDateString();
      const paymentDate = new Date(payment.date).toDateString();
      if (today !== paymentDate) return false;
    }
    
    return true;
  });
  
  // KPI Cards data
  const KPI_CARDS = [
    {
      title: "Today's Collection",
      value: stats.todayCollection,
      icon: <Banknote className="w-5 h-5 text-green-600" />,
      color: 'bg-green-50 border-green-100',
      description: 'Total received today',
      onClick: () => handleFilterChange('dateRange', 'today')
    },
    {
      title: "Pending Payments",
      value: stats.pendingPayments,
      icon: <Clock className="w-5 h-5 text-yellow-600" />,
      color: 'bg-yellow-50 border-yellow-100',
      description: 'Balance due',
      onClick: () => handleFilterChange('status', 'PENDING')
    },
    {
      title: "Digital Payments",
      value: stats.digitalPayments,
      icon: <Smartphone className="w-5 h-5 text-blue-600" />,
      color: 'bg-blue-50 border-blue-100',
      description: 'UPI + Card + Online',
      onClick: () => handleFilterChange('paymentMethod', 'DIGITAL')
    },
    {
      title: "Cash Collected",
      value: stats.cashCollected,
      icon: <IndianRupee className="w-5 h-5 text-purple-600" />,
      color: 'bg-purple-50 border-purple-100',
      description: 'Physical cash',
      onClick: () => handleFilterChange('paymentMethod', 'CASH')
    }
  ];
  
  // Table columns
  const columns = [
    { key: 'select', label: '', sortable: false },
    { key: 'date', label: 'Date', sortable: true },
    { key: 'guest', label: 'Guest Name', sortable: true },
    { key: 'booking', label: 'Booking / Invoice', sortable: true },
    { key: 'amount', label: 'Amount Paid', sortable: true },
    { key: 'total', label: 'Total Bill', sortable: true },
    { key: 'balance', label: 'Balance Due', sortable: true },
    { key: 'method', label: 'Payment Method' },
    { key: 'status', label: 'Status' },
    { key: 'actions', label: 'Actions', sortable: false }
  ];
  
  return (
    <div className="container mx-auto px-4 py-6">
      {/* Page Header */}
      <div className="mb-8">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
              <Link to="/dashboard" className="hover:text-blue-600">Dashboard</Link>
              <ChevronRight className="w-4 h-4" />
              <span className="text-blue-600 font-medium">Payments</span>
            </div>
            <h1 className="text-3xl font-bold text-gray-900">Payments</h1>
            <p className="text-gray-600 mt-1">Track and manage all guest payments</p>
          </div>
          
          <div className="flex flex-wrap gap-3">
            <button
              onClick={handleExport}
              className="flex items-center gap-2 px-4 py-2.5 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
            >
              <Download className="w-4 h-4" />
              Export
            </button>
            <button
              onClick={handleAddPayment}
              className="flex items-center gap-2 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus className="w-4 h-4" />
              Receive Payment
            </button>
          </div>
        </div>
        
        <div className="flex items-center gap-2 mt-4 text-sm text-gray-500">
          <Calendar className="w-4 h-4" />
          <span>{new Date().toLocaleDateString('en-US', { 
            weekday: 'long', 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
          })}</span>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {KPI_CARDS.map((card, index) => (
          <div
            key={index}
            onClick={card.onClick}
            className={`${card.color} border rounded-xl p-6 cursor-pointer hover:shadow-md transition-all duration-200`}
          >
            <div className="flex items-center justify-between mb-4">
              <div className="p-2 bg-white rounded-lg shadow-sm">
                {card.icon}
              </div>
              <div className="text-2xl font-bold text-gray-900">
                {formatCurrency(card.value)}
              </div>
            </div>
            <div className="font-semibold text-gray-900">{card.title}</div>
            <div className="text-sm text-gray-600 mt-1">{card.description}</div>
          </div>
        ))}
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl border border-gray-200 p-4 mb-6">
        <div className="flex flex-col md:flex-row md:items-center gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search by guest, invoice, booking ID..."
                value={filters.search}
                onChange={handleSearch}
                className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
          
          <div className="flex flex-wrap gap-3">
            <select
              value={filters.dateRange}
              onChange={(e) => handleFilterChange('dateRange', e.target.value)}
              className="px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="today">Today</option>
              <option value="yesterday">Yesterday</option>
              <option value="week">This Week</option>
              <option value="month">This Month</option>
              <option value="quarter">This Quarter</option>
              <option value="custom">Custom Range</option>
            </select>
            
            <select
              value={filters.status}
              onChange={(e) => handleFilterChange('status', e.target.value)}
              className="px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="all">All Status</option>
              <option value="PAID">Paid</option>
              <option value="PARTIAL">Partial</option>
              <option value="PENDING">Pending</option>
            </select>
            
            <select
              value={filters.paymentMethod}
              onChange={(e) => handleFilterChange('paymentMethod', e.target.value)}
              className="px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="all">All Methods</option>
              <option value="CASH">Cash</option>
              <option value="UPI">UPI</option>
              <option value="CARD">Card</option>
              <option value="BANK">Bank Transfer</option>
            </select>
            
            <button
              onClick={() => setShowAdvancedFilters(!showAdvancedFilters)}
              className="flex items-center gap-2 px-4 py-2.5 border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              <Filter className="w-4 h-4" />
              More Filters
              {showAdvancedFilters ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </button>
          </div>
        </div>
        
        {/* Advanced Filters */}
        {showAdvancedFilters && (
          <div className="mt-6 pt-6 border-t border-gray-200">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Minimum Amount
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <IndianRupee className="w-4 h-4 text-gray-400" />
                  </div>
                  <input
                    type="number"
                    placeholder="0"
                    className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Maximum Amount
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <IndianRupee className="w-4 h-4 text-gray-400" />
                  </div>
                  <input
                    type="number"
                    placeholder="100000"
                    className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Received By
                </label>
                <select className="w-full px-3 py-2 border border-gray-300 rounded-lg">
                  <option>All Staff</option>
                  <option>John Doe (Reception)</option>
                  <option>Jane Smith (Manager)</option>
                  <option>Robert Johnson (Owner)</option>
                </select>
              </div>
            </div>
            
            <div className="flex justify-end gap-3 mt-4">
              <button
                onClick={() => {
                  handleFilterChange('dateRange', 'today');
                  handleFilterChange('status', 'all');
                  handleFilterChange('paymentMethod', 'all');
                  handleFilterChange('search', '');
                  setShowAdvancedFilters(false);
                }}
                className="px-4 py-2 text-sm text-gray-700 hover:text-gray-900"
              >
                Clear All
              </button>
              <button
                onClick={() => setShowAdvancedFilters(false)}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Apply Filters
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Bulk Actions Bar */}
      {selectedRows.length > 0 && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <div className="flex items-center justify-between">
            <div className="text-sm text-blue-800">
              {selectedRows.length} payment(s) selected
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => handleBulkAction('export')}
                className="px-3 py-1.5 text-sm bg-white border border-blue-300 text-blue-700 rounded hover:bg-blue-50"
              >
                Export Selected
              </button>
              <button
                onClick={() => handleBulkAction('print')}
                className="px-3 py-1.5 text-sm bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                Print Receipts
              </button>
              <button
                onClick={() => setSelectedRows([])}
                className="px-3 py-1.5 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200"
              >
                Clear Selection
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Payments Table */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                {columns.map((column) => (
                  <th
                    key={column.key}
                    className="py-3 px-4 text-left text-sm font-semibold text-gray-900 whitespace-nowrap"
                  >
                    {column.key === 'select' ? (
                      <input
                        type="checkbox"
                        checked={selectedRows.length === filteredPayments.length && filteredPayments.length > 0}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setSelectedRows(filteredPayments.map(p => p.id));
                          } else {
                            setSelectedRows([]);
                          }
                        }}
                        className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                      />
                    ) : (
                      <div className="flex items-center gap-1">
                        {column.label}
                        {column.sortable && (
                          <button 
                            onClick={() => handleSort(column.key)}
                            className="text-gray-400 hover:text-gray-600"
                          >
                            {sortConfig.key === column.key && sortConfig.direction === 'asc' ? (
                              <ChevronUp className="w-4 h-4" />
                            ) : (
                              <ChevronDown className="w-4 h-4" />
                            )}
                          </button>
                        )}
                      </div>
                    )}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {loading ? (
                Array.from({ length: 5 }).map((_, index) => (
                  <tr key={index}>
                    <td className="px-4 py-3"><div className="h-4 bg-gray-200 rounded animate-pulse"></div></td>
                    {columns.slice(1).map((column) => (
                      <td key={column.key} className="py-3 px-4">
                        <div className="h-4 bg-gray-200 rounded animate-pulse"></div>
                      </td>
                    ))}
                  </tr>
                ))
              ) : filteredPayments.length === 0 ? (
                <tr>
                  <td colSpan={columns.length} className="py-12 text-center">
                    <div className="text-gray-500">No payments found</div>
                    <button
                      onClick={handleAddPayment}
                      className="mt-4 text-blue-600 hover:text-blue-800 font-medium"
                    >
                      + Record your first payment
                    </button>
                  </td>
                </tr>
              ) : (
                filteredPayments.map((payment) => (
                  <tr 
                    key={payment.id} 
                    className={`hover:bg-gray-50 ${selectedRows.includes(payment.id) ? 'bg-blue-50' : ''}`}
                  >
                    <td className="px-4 py-3">
                      <input
                        type="checkbox"
                        checked={selectedRows.includes(payment.id)}
                        onChange={() => handleRowSelect(payment.id)}
                        className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                      />
                    </td>
                    <td className="py-3 px-4 text-sm text-gray-900 whitespace-nowrap">
                      {formatDate(payment.date)}
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                          <User className="w-4 h-4 text-blue-600" />
                        </div>
                        <div>
                          <div className="font-medium text-gray-900">
                            {payment.guestName}
                          </div>
                          <div className="text-xs text-gray-500">
                            Room {payment.roomNumber}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="space-y-1">
                        <Link
                          to={`/reservations/${payment.bookingId}`}
                          className="text-blue-600 hover:text-blue-800 hover:underline block text-sm"
                        >
                          Booking #{payment.bookingId}
                        </Link>
                        {payment.invoiceId && (
                          <Link
                            to={`/invoice/${payment.invoiceId}`}
                            className="text-gray-600 hover:text-gray-800 hover:underline block text-sm"
                          >
                            Invoice #{payment.invoiceId}
                          </Link>
                        )}
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="font-semibold text-gray-900">
                        {formatCurrency(payment.amountPaid)}
                      </div>
                    </td>
                    <td className="py-3 px-4 text-gray-700">
                      {formatCurrency(payment.totalBill)}
                    </td>
                    <td className="py-3 px-4">
                      <div className={`font-semibold ${
                        payment.balanceDue > 0 ? 'text-red-600' : 'text-green-600'
                      }`}>
                        {formatCurrency(payment.balanceDue)}
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <div className={`p-1.5 rounded ${
                          payment.paymentMethod === 'CASH' ? 'bg-green-100 text-green-800' :
                          payment.paymentMethod === 'UPI' ? 'bg-blue-100 text-blue-800' :
                          payment.paymentMethod === 'CARD' ? 'bg-purple-100 text-purple-800' :
                          'bg-gray-100 text-gray-800'
                        }`}>
                          {getPaymentMethodIcon(payment.paymentMethod)}
                        </div>
                        <span className="text-sm text-gray-700">
                          {payment.paymentMethod || 'N/A'}
                        </span>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      {getStatusBadge(payment.status)}
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handleViewInvoice(payment.invoiceId)}
                          className="p-1.5 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="View Invoice"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        {payment.balanceDue > 0 && (
                          <button
                            onClick={() => handleAddPaymentToInvoice(payment.invoiceId)}
                            className="p-1.5 text-gray-600 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                            title="Add Payment"
                          >
                            <Plus className="w-4 h-4" />
                          </button>
                        )}
                        <button
                          onClick={() => handleViewReceipt(payment.id)}
                          className="p-1.5 text-gray-600 hover:text-purple-600 hover:bg-purple-50 rounded-lg transition-colors"
                          title="Print Receipt"
                        >
                          <Printer className="w-4 h-4" />
                        </button>
                        <button className="p-1.5 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors">
                          <MoreVertical className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {!loading && filteredPayments.length > 0 && (
          <div className="flex items-center justify-between px-6 py-4 border-t border-gray-200">
            <div className="text-sm text-gray-700">
              Showing {Math.min(filteredPayments.length, 10)} of {filteredPayments.length} payments
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                disabled={currentPage === 1}
                className="px-3 py-1.5 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              {[1, 2, 3].map((page) => (
                <button
                  key={page}
                  onClick={() => setCurrentPage(page)}
                  className={`px-3 py-1.5 border rounded ${
                    currentPage === page
                      ? 'bg-blue-600 text-white border-blue-600'
                      : 'border-gray-300 text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  {page}
                </button>
              ))}
              <button
                onClick={() => setCurrentPage(prev => prev + 1)}
                disabled={currentPage === 3}
                className="px-3 py-1.5 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

// Payment Form Component (Receive Payment)
const PaymentForm = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const invoiceId = queryParams.get('invoiceId');
  
  const [loading, setLoading] = useState(false);
  const [invoice, setInvoice] = useState(invoiceId ? {
    id: invoiceId,
    totalAmount: 15000,
    paidAmount: 5000,
    balanceDue: 10000,
    bookingId: 'BK001',
    guestName: 'Rajesh Kumar',
    roomNumber: '101',
    checkIn: '2024-01-15',
    checkOut: '2024-01-17',
    nights: 2,
    status: 'PARTIAL'
  } : null);
  
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState([
    { id: 'INV001', guestName: 'Rajesh Kumar', roomNumber: '101', totalAmount: 15000, balanceDue: 10000, bookingId: 'BK001', checkIn: '2024-01-15', checkOut: '2024-01-17' },
    { id: 'INV002', guestName: 'Priya Sharma', roomNumber: '202', totalAmount: 12000, balanceDue: 0, bookingId: 'BK002', checkIn: '2024-01-16', checkOut: '2024-01-18' },
    { id: 'INV003', guestName: 'Amit Patel', roomNumber: '301', totalAmount: 20000, balanceDue: 12000, bookingId: 'BK003', checkIn: '2024-01-14', checkOut: '2024-01-16' },
  ]);
  const [showSearch, setShowSearch] = useState(!invoiceId);
  
  const [formData, setFormData] = useState({
    invoiceId: invoiceId || '',
    paymentType: 'FULL',
    amount: invoiceId ? 10000 : 0,
    paymentMethod: 'CASH',
    paymentDate: new Date().toISOString().split('T')[0],
    receivedBy: localStorage.getItem('userName') || 'John Doe (Reception)',
    remarks: '',
    transactionId: '',
    upiId: '',
    cardLastFour: '',
    cardAuthCode: '',
    bankName: '',
    gateway: '',
    gatewayTxnId: ''
  });
  
  const [errors, setErrors] = useState({});
  const [qrCodeVisible, setQrCodeVisible] = useState(false);
  
  // Format currency
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(amount);
  };
  
  // Format date
  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    }).format(date);
  };
  
  // Handle input change
  const handleInputChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    
    // Clear error when field is modified
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: null }));
    }
    
    // Auto-calculate amount for full payment
    if (field === 'paymentType' && value === 'FULL' && invoice) {
      setFormData(prev => ({ ...prev, amount: invoice.balanceDue }));
    }
    
    // Reset method-specific fields when method changes
    if (field === 'paymentMethod') {
      setFormData(prev => ({
        ...prev,
        upiId: '',
        cardLastFour: '',
        cardAuthCode: '',
        bankName: '',
        gateway: '',
        gatewayTxnId: '',
        transactionId: ''
      }));
    }
  };
  
  // Handle search
  const handleSearch = () => {
    if (!searchTerm.trim()) {
      setSearchResults([]);
      return;
    }
    
    const filtered = [
      { id: 'INV001', guestName: 'Rajesh Kumar', roomNumber: '101', totalAmount: 15000, balanceDue: 10000, bookingId: 'BK001', checkIn: '2024-01-15', checkOut: '2024-01-17' },
      { id: 'INV002', guestName: 'Priya Sharma', roomNumber: '202', totalAmount: 12000, balanceDue: 0, bookingId: 'BK002', checkIn: '2024-01-16', checkOut: '2024-01-18' },
      { id: 'INV003', guestName: 'Amit Patel', roomNumber: '301', totalAmount: 20000, balanceDue: 12000, bookingId: 'BK003', checkIn: '2024-01-14', checkOut: '2024-01-16' },
    ].filter(item => 
      item.guestName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.roomNumber.includes(searchTerm)
    );
    
    setSearchResults(filtered);
  };
  
  // Handle select invoice
  const handleSelectInvoice = (selectedInvoice) => {
    setInvoice(selectedInvoice);
    setFormData(prev => ({
      ...prev,
      invoiceId: selectedInvoice.id,
      amount: selectedInvoice.balanceDue
    }));
    setShowSearch(false);
    setSearchTerm('');
  };
  
  // Validate form
  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.invoiceId) {
      newErrors.invoiceId = 'Please select an invoice';
    }
    
    if (!formData.amount || formData.amount <= 0) {
      newErrors.amount = 'Please enter a valid amount';
    }
    
    if (invoice && formData.amount > invoice.balanceDue) {
      newErrors.amount = `Amount cannot exceed balance due (${formatCurrency(invoice.balanceDue)})`;
    }
    
    if (!formData.paymentMethod) {
      newErrors.paymentMethod = 'Please select a payment method';
    }
    
    // Validate method-specific fields
    if (formData.paymentMethod === 'UPI') {
      if (!formData.upiId) {
        newErrors.upiId = 'UPI ID is required';
      } else if (!formData.upiId.includes('@')) {
        newErrors.upiId = 'Enter a valid UPI ID (e.g., username@upi)';
      }
    }
    
    if (formData.paymentMethod === 'CARD') {
      if (!formData.cardLastFour || formData.cardLastFour.length !== 4) {
        newErrors.cardLastFour = 'Enter last 4 digits of card';
      } else if (!/^\d+$/.test(formData.cardLastFour)) {
        newErrors.cardLastFour = 'Only numbers allowed';
      }
    }
    
    if (formData.paymentMethod === 'BANK' && !formData.transactionId) {
      newErrors.transactionId = 'Transaction ID is required';
    }
    
    if (formData.paymentMethod === 'ONLINE' && !formData.gatewayTxnId) {
      newErrors.gatewayTxnId = 'Gateway Transaction ID is required';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  // Handle submit
  const handleSubmit = async (action) => {
    if (!validateForm()) return;
    
    setLoading(true);
    
    // Simulate API call
    setTimeout(() => {
      setLoading(false);
      
      switch(action) {
        case 'save':
          alert('Payment recorded successfully!');
          navigate('/payments');
          break;
          
        case 'save-print':
          alert('Payment recorded and receipt printed!');
          navigate('/payments');
          break;
          
        case 'save-email':
          alert('Payment recorded and receipt emailed!');
          navigate('/payments');
          break;
          
        case 'save-another':
          alert('Payment recorded! You can now add another payment.');
          // Reset form for next payment
          setFormData({
            invoiceId: '',
            paymentType: 'FULL',
            amount: 0,
            paymentMethod: 'CASH',
            paymentDate: new Date().toISOString().split('T')[0],
            receivedBy: localStorage.getItem('userName') || 'John Doe (Reception)',
            remarks: '',
            transactionId: '',
            upiId: '',
            cardLastFour: '',
            cardAuthCode: '',
            bankName: '',
            gateway: '',
            gatewayTxnId: ''
          });
          setInvoice(null);
          setShowSearch(true);
          setErrors({});
          break;
      }
    }, 1000);
  };
  
  // Handle cancel
  const handleCancel = () => {
    if (window.confirm('Are you sure? Any unsaved changes will be lost.')) {
      navigate('/payments');
    }
  };
  
  // Payment methods
  const paymentMethods = [
    { id: 'CASH', label: 'Cash', icon: Wallet, color: 'bg-green-100 text-green-800', description: 'Physical cash payment' },
    { id: 'UPI', label: 'UPI', icon: Smartphone, color: 'bg-blue-100 text-blue-800', description: 'UPI QR Code or ID' },
    { id: 'CARD', label: 'Card', icon: CreditCard, color: 'bg-purple-100 text-purple-800', description: 'Credit/Debit Card' },
    { id: 'BANK', label: 'Bank Transfer', icon: Building, color: 'bg-indigo-100 text-indigo-800', description: 'NEFT/RTGS/IMPS' },
    { id: 'ONLINE', label: 'Online Gateway', icon: Globe, color: 'bg-orange-100 text-orange-800', description: 'Razorpay/Stripe etc.' }
  ];
  
  // Render method-specific fields
  const renderMethodFields = () => {
    switch(formData.paymentMethod) {
      case 'UPI':
        return (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                UPI ID
              </label>
              <input
                type="text"
                value={formData.upiId}
                onChange={(e) => handleInputChange('upiId', e.target.value)}
                placeholder="username@upi"
                className={`block w-full px-3 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                  errors.upiId ? 'border-red-300' : 'border-gray-300'
                }`}
              />
              {errors.upiId && (
                <p className="mt-1 text-sm text-red-600">{errors.upiId}</p>
              )}
              <p className="mt-1 text-xs text-gray-500">
                Enter UPI ID (e.g., 9876543210@upi, username@okhdfcbank)
              </p>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                UPI Transaction ID (Optional)
              </label>
              <input
                type="text"
                value={formData.transactionId}
                onChange={(e) => handleInputChange('transactionId', e.target.value)}
                placeholder="Transaction reference number"
                className="block w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            
            <div className="pt-4 border-t border-gray-200">
              <div className="flex items-center justify-between mb-3">
                <h4 className="text-sm font-medium text-gray-700">QR Code Payment</h4>
                <button
                  type="button"
                  onClick={() => setQrCodeVisible(!qrCodeVisible)}
                  className="text-sm text-blue-600 hover:text-blue-800"
                >
                  {qrCodeVisible ? 'Hide QR' : 'Show QR'}
                </button>
              </div>
              
              {qrCodeVisible && (
                <div className="bg-white border border-gray-200 rounded-lg p-4 text-center">
                  <div className="w-48 h-48 mx-auto bg-gray-100 rounded-lg flex items-center justify-center mb-3">
                    <QrCode className="w-32 h-32 text-gray-400" />
                  </div>
                  <p className="text-sm text-gray-600 mb-2">Scan to pay via UPI</p>
                  <div className="text-xs text-gray-500">
                    Amount: {formatCurrency(formData.amount)}
                  </div>
                </div>
              )}
            </div>
          </div>
        );
        
      case 'CARD':
        return (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Card Last 4 Digits
              </label>
              <input
                type="text"
                value={formData.cardLastFour}
                onChange={(e) => handleInputChange('cardLastFour', e.target.value.replace(/\D/g, '').slice(0, 4))}
                placeholder="1234"
                maxLength="4"
                className={`block w-full px-3 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                  errors.cardLastFour ? 'border-red-300' : 'border-gray-300'
                }`}
              />
              {errors.cardLastFour && (
                <p className="mt-1 text-sm text-red-600">{errors.cardLastFour}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Authorization Code
              </label>
              <input
                type="text"
                value={formData.cardAuthCode}
                onChange={(e) => handleInputChange('cardAuthCode', e.target.value)}
                placeholder="6-digit code"
                className="block w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Card Type
              </label>
              <select className="block w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
                <option value="">Select Card Type</option>
                <option value="VISA">VISA</option>
                <option value="MASTERCARD">MasterCard</option>
                <option value="RUPAY">RuPay</option>
                <option value="AMEX">American Express</option>
              </select>
            </div>
          </div>
        );
        
      case 'BANK':
        return (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Bank Name
              </label>
              <input
                type="text"
                value={formData.bankName}
                onChange={(e) => handleInputChange('bankName', e.target.value)}
                placeholder="e.g., HDFC Bank, SBI, ICICI"
                className="block w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Transaction ID / Reference
              </label>
              <input
                type="text"
                value={formData.transactionId}
                onChange={(e) => handleInputChange('transactionId', e.target.value)}
                placeholder="Bank reference number"
                className={`block w-full px-3 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                  errors.transactionId ? 'border-red-300' : 'border-gray-300'
                }`}
              />
              {errors.transactionId && (
                <p className="mt-1 text-sm text-red-600">{errors.transactionId}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Transaction Date
              </label>
              <input
                type="date"
                className="block w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
        );
        
      case 'ONLINE':
        return (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Payment Gateway
              </label>
              <select
                value={formData.gateway}
                onChange={(e) => handleInputChange('gateway', e.target.value)}
                className="block w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">Select Gateway</option>
                <option value="RAZORPAY">Razorpay</option>
                <option value="STRIPE">Stripe</option>
                <option value="PAYPAL">PayPal</option>
                <option value="PAYTM">PayTM</option>
                <option value="PHONEPE">PhonePe</option>
                <option value="GPAY">Google Pay</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Gateway Transaction ID
              </label>
              <input
                type="text"
                value={formData.gatewayTxnId}
                onChange={(e) => handleInputChange('gatewayTxnId', e.target.value)}
                placeholder="Gateway reference"
                className={`block w-full px-3 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                  errors.gatewayTxnId ? 'border-red-300' : 'border-gray-300'
                }`}
              />
              {errors.gatewayTxnId && (
                <p className="mt-1 text-sm text-red-600">{errors.gatewayTxnId}</p>
              )}
            </div>
            <div className="md:col-span-2">
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div className="flex items-center gap-2 mb-2">
                  <Shield className="w-5 h-5 text-blue-600" />
                  <span className="text-sm font-medium text-blue-800">Secure Payment</span>
                </div>
                <p className="text-xs text-blue-700">
                  Online payments are processed securely through PCI-DSS compliant gateways.
                  No card details are stored in our system.
                </p>
              </div>
            </div>
          </div>
        );
        
      default: // CASH
        return (
          <div className="space-y-4">
            <div className="bg-green-50 border border-green-200 rounded-lg p-4">
              <div className="flex items-center gap-2 mb-2">
                <Wallet className="w-5 h-5 text-green-600" />
                <span className="text-sm font-medium text-green-800">Cash Payment Instructions</span>
              </div>
              <ul className="text-xs text-green-700 space-y-1">
                <li>â€¢ Verify cash amount carefully</li>
                <li>â€¢ Check for counterfeit notes</li>
                <li>â€¢ Issue receipt immediately</li>
                <li>â€¢ Deposit cash in safe/locker</li>
                <li>â€¢ Update cash register tally</li>
              </ul>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Cash Denomination (Optional)
              </label>
              <textarea
                rows="2"
                placeholder="e.g., 2000x5, 500x2, 100x10"
                className="block w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
        );
    }
  };
  
  return (
    <div className="container mx-auto px-4 py-6">
      {/* Page Header */}
      <div className="mb-8">
        <div className="flex items-center gap-4 mb-4">
          <button
            onClick={handleCancel}
            className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
              <Link to="/dashboard" className="hover:text-blue-600">Dashboard</Link>
              <ChevronRight className="w-4 h-4" />
              <Link to="/payments" className="hover:text-blue-600">Payments</Link>
              <ChevronRight className="w-4 h-4" />
              <span className="text-blue-600 font-medium">Receive Payment</span>
            </div>
            <h1 className="text-3xl font-bold text-gray-900">Receive Payment</h1>
            <p className="text-gray-600 mt-1">Record payment for guest booking/invoice</p>
          </div>
        </div>
      </div>

      {/* Two Column Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column - Payment Form */}
        <div className="lg:col-span-2 space-y-6">
          {/* Invoice/Booking Search */}
          {showSearch ? (
            <div className="bg-white rounded-xl border border-gray-200 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <Search className="w-5 h-5" />
                Search Invoice / Booking
              </h2>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Search by Guest Name, Invoice ID, Booking ID, or Room Number
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                      placeholder="Enter search term..."
                      className="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />
                    <button
                      onClick={handleSearch}
                      className="px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                    >
                      Search
                    </button>
                  </div>
                </div>
                
                {searchResults.length > 0 && (
                  <div className="border border-gray-200 rounded-lg overflow-hidden">
                    <div className="max-h-96 overflow-y-auto">
                      {searchResults.map((item) => (
                        <button
                          key={item.id}
                          onClick={() => handleSelectInvoice(item)}
                          className="w-full text-left p-4 hover:bg-gray-50 border-b border-gray-100 last:border-b-0 transition-colors"
                        >
                          <div className="flex justify-between items-start">
                            <div>
                              <div className="font-medium text-gray-900">
                                {item.guestName}
                              </div>
                              <div className="text-sm text-gray-600 mt-1">
                                Invoice #{item.id} â€¢ Room {item.roomNumber} â€¢ Booking #{item.bookingId}
                              </div>
                              <div className="text-xs text-gray-500 mt-1">
                                {formatDate(item.checkIn)} - {formatDate(item.checkOut)}
                              </div>
                            </div>
                            <div className="text-right">
                              <div className="font-medium text-gray-900">
                                {formatCurrency(item.totalAmount)}
                              </div>
                              <div className={`text-sm ${
                                item.balanceDue > 0 ? 'text-red-600' : 'text-green-600'
                              }`}>
                                Due: {formatCurrency(item.balanceDue)}
                              </div>
                            </div>
                          </div>
                        </button>
                      ))}
                    </div>
                  </div>
                )}
                
                {searchTerm && searchResults.length === 0 && (
                  <div className="text-center py-8 text-gray-500">
                    No invoices or bookings found
                  </div>
                )}
              </div>
            </div>
          ) : invoice ? (
            <>
              {/* Selected Invoice Details */}
              <div className="bg-white rounded-xl border border-gray-200 p-6">
                <div className="flex justify-between items-start mb-6">
                  <h2 className="text-lg font-semibold text-gray-900">
                    Selected Invoice
                  </h2>
                  <button
                    onClick={() => setShowSearch(true)}
                    className="text-sm text-blue-600 hover:text-blue-800"
                  >
                    Change
                  </button>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Guest Name
                    </label>
                    <div className="flex items-center gap-2 p-2.5 bg-gray-50 border border-gray-200 rounded">
                      <User className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-900">{invoice.guestName}</span>
                    </div>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Room Number
                    </label>
                    <div className="flex items-center gap-2 p-2.5 bg-gray-50 border border-gray-200 rounded">
                      <Home className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-900">{invoice.roomNumber}</span>
                    </div>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Stay Dates
                    </label>
                    <div className="flex items-center gap-2 p-2.5 bg-gray-50 border border-gray-200 rounded">
                      <Calendar className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-900">
                        {formatDate(invoice.checkIn)} - {formatDate(invoice.checkOut)}
                      </span>
                    </div>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Invoice ID
                    </label>
                    <div className="p-2.5 bg-gray-50 border border-gray-200 rounded">
                      <span className="text-gray-900 font-mono">#{invoice.id}</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Amount Details */}
              <div className="bg-white rounded-xl border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-6 flex items-center gap-2">
                  <DollarSign className="w-5 h-5 text-green-600" />
                  Amount Details
                </h2>
                
                <div className="space-y-4">
                  <div className="flex justify-between items-center py-2 border-b border-gray-100">
                    <span className="text-gray-600">Total Bill Amount</span>
                    <span className="text-lg font-semibold text-gray-900">
                      {formatCurrency(invoice.totalAmount)}
                    </span>
                  </div>
                  
                  <div className="flex justify-between items-center py-2 border-b border-gray-100">
                    <span className="text-gray-600">Paid Amount</span>
                    <span className="text-lg font-semibold text-green-600">
                      {formatCurrency(invoice.paidAmount)}
                    </span>
                  </div>
                  
                  <div className="flex justify-between items-center py-2">
                    <span className="text-gray-600">Balance Due</span>
                    <span className={`text-xl font-bold ${
                      invoice.balanceDue > 0 ? 'text-red-600' : 'text-green-600'
                    }`}>
                      {formatCurrency(invoice.balanceDue)}
                    </span>
                  </div>
                  
                  {/* Progress Bar */}
                  <div className="pt-4">
                    <div className="flex justify-between text-sm text-gray-600 mb-2">
                      <span>Payment Progress</span>
                      <span>{((invoice.paidAmount / invoice.totalAmount) * 100).toFixed(1)}%</span>
                    </div>
                    <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                      <div 
                        className="h-full bg-green-500 rounded-full transition-all duration-300"
                        style={{ width: `${Math.min((invoice.paidAmount / invoice.totalAmount) * 100, 100)}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Payment Details */}
              <div className="bg-white rounded-xl border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-6">
                  Payment Details
                </h2>
                
                <div className="space-y-6">
                  {/* Payment Type */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-3">
                      Payment Type
                    </label>
                    <div className="flex gap-4">
                      {['FULL', 'PARTIAL'].map((type) => (
                        <label
                          key={type}
                          className="flex items-center gap-2 cursor-pointer"
                        >
                          <input
                            type="radio"
                            name="paymentType"
                            checked={formData.paymentType === type}
                            onChange={() => handleInputChange('paymentType', type)}
                            className="w-4 h-4 text-blue-600"
                          />
                          <span className="text-gray-700">
                            {type === 'FULL' ? 'Full Payment' : 'Partial Payment'}
                          </span>
                        </label>
                      ))}
                    </div>
                  </div>

                  {/* Amount Paid */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Amount to Pay
                    </label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <IndianRupee className="w-5 h-5 text-gray-500" />
                      </div>
                      <input
                        type="number"
                        value={formData.amount}
                        onChange={(e) => handleInputChange('amount', parseFloat(e.target.value))}
                        max={invoice.balanceDue}
                        min="1"
                        step="0.01"
                        className={`block w-full pl-10 pr-3 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                          errors.amount ? 'border-red-300' : 'border-gray-300'
                        }`}
                        disabled={formData.paymentType === 'FULL'}
                      />
                      <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                        <button
                          type="button"
                          onClick={() => handleInputChange('amount', invoice.balanceDue)}
                          className="text-sm text-blue-600 hover:text-blue-800"
                        >
                          Pay Full ({formatCurrency(invoice.balanceDue)})
                        </button>
                      </div>
                    </div>
                    {errors.amount && (
                      <p className="mt-1 text-sm text-red-600">{errors.amount}</p>
                    )}
                    <div className="mt-2 text-sm text-gray-500">
                      Balance after payment: <span className="font-medium">
                        {formatCurrency(invoice.balanceDue - formData.amount)}
                      </span>
                    </div>
                  </div>

                  {/* Payment Method */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-3">
                      Payment Method
                    </label>
                    <div className="grid grid-cols-2 md:grid-cols-5 gap-3 mb-6">
                      {paymentMethods.map((method) => {
                        const Icon = method.icon;
                        return (
                          <label
                            key={method.id}
                            className={`flex flex-col items-center justify-center p-3 border rounded-lg cursor-pointer transition-all ${
                              formData.paymentMethod === method.id
                                ? 'border-blue-500 bg-blue-50'
                                : 'border-gray-300 hover:border-gray-400'
                            }`}
                          >
                            <input
                              type="radio"
                              name="paymentMethod"
                              value={method.id}
                              checked={formData.paymentMethod === method.id}
                              onChange={(e) => handleInputChange('paymentMethod', e.target.value)}
                              className="sr-only"
                            />
                            <div className={`p-2 rounded-lg ${method.color.split(' ')[0]} mb-2`}>
                              <Icon className="w-5 h-5" />
                            </div>
                            <span className="text-sm font-medium text-gray-700">
                              {method.label}
                            </span>
                            <span className="text-xs text-gray-500 mt-1 text-center">
                              {method.description}
                            </span>
                          </label>
                        );
                      })}
                    </div>
                    
                    {/* Method-specific fields */}
                    <div className="mt-4">
                      <h4 className="text-sm font-medium text-gray-700 mb-4">
                        {formData.paymentMethod === 'CASH' ? 'Cash Payment Details' :
                         formData.paymentMethod === 'UPI' ? 'UPI Payment Details' :
                         formData.paymentMethod === 'CARD' ? 'Card Payment Details' :
                         formData.paymentMethod === 'BANK' ? 'Bank Transfer Details' :
                         'Online Payment Details'}
                      </h4>
                      {renderMethodFields()}
                    </div>
                  </div>

                  {/* Additional Details */}
                  <div className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Payment Date
                        </label>
                        <input
                          type="date"
                          value={formData.paymentDate}
                          onChange={(e) => handleInputChange('paymentDate', e.target.value)}
                          className="block w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        />
                      </div>
                      
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Received By
                        </label>
                        <div className="flex items-center gap-2 p-2.5 bg-gray-50 border border-gray-200 rounded">
                          <User className="w-4 h-4 text-gray-500" />
                          <span className="text-gray-900">{formData.receivedBy}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Remarks (Optional)
                      </label>
                      <textarea
                        value={formData.remarks}
                        onChange={(e) => handleInputChange('remarks', e.target.value)}
                        rows="3"
                        className="block w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        placeholder="Add any additional notes about this payment..."
                      />
                    </div>
                  </div>
                </div>
              </div>
            </>
          ) : null}
        </div>

        {/* Right Column - Booking Summary */}
        <div className="lg:col-span-1">
          <div className="sticky top-6">
            {invoice ? (
              <div className="bg-white rounded-xl border border-gray-200 p-6 space-y-6">
                <h3 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                  <Receipt className="w-5 h-5" />
                  Booking Summary
                </h3>
                
                <div className="space-y-4">
                  <div>
                    <div className="text-sm text-gray-500">Guest Name</div>
                    <div className="font-medium text-gray-900">
                      {invoice.guestName}
                    </div>
                  </div>
                  
                  <div>
                    <div className="text-sm text-gray-500">Booking ID</div>
                    <div className="font-medium text-blue-600">
                      #{invoice.bookingId}
                    </div>
                  </div>
                  
                  <div>
                    <div className="text-sm text-gray-500">Invoice ID</div>
                    <div className="font-medium text-gray-900">
                      #{invoice.id}
                    </div>
                  </div>
                  
                  <div>
                    <div className="text-sm text-gray-500">Room Number</div>
                    <div className="font-medium text-gray-900">
                      {invoice.roomNumber}
                    </div>
                  </div>
                  
                  <div>
                    <div className="text-sm text-gray-500">Stay Duration</div>
                    <div className="font-medium text-gray-900">
                      {invoice.nights} night(s)
                    </div>
                  </div>
                  
                  <div className="pt-4 border-t border-gray-200">
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-500">Total Bill</span>
                        <span className="font-medium text-gray-900">
                          {formatCurrency(invoice.totalAmount)}
                        </span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-500">Paid So Far</span>
                        <span className="font-medium text-green-600">
                          {formatCurrency(invoice.paidAmount)}
                        </span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-500">Balance Due</span>
                        <span className={`font-medium ${
                          invoice.balanceDue > 0 ? 'text-red-600' : 'text-green-600'
                        }`}>
                          {formatCurrency(invoice.balanceDue)}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="pt-4 border-t border-gray-200">
                    <div className="text-sm text-gray-500 mb-2">Payment Status</div>
                    <div className="flex items-center gap-2">
                      <div className={`w-3 h-3 rounded-full ${
                        invoice.balanceDue === 0 ? 'bg-green-500' :
                        invoice.paidAmount > 0 ? 'bg-yellow-500' : 'bg-red-500'
                      }`}></div>
                      <span className="font-medium text-gray-900">
                        {invoice.balanceDue === 0 ? 'Paid in Full' :
                         invoice.paidAmount > 0 ? 'Partially Paid' : 'Payment Pending'}
                      </span>
                    </div>
                  </div>
                </div>
                
                {/* Payment Preview */}
                {formData.amount > 0 && (
                  <div className="bg-blue-50 border border-blue-100 rounded-lg p-4">
                    <div className="text-sm text-blue-800 font-medium mb-2">
                      Payment Preview
                    </div>
                    <div className="space-y-2 text-sm text-blue-700">
                      <div className="flex justify-between">
                        <span>Payment Amount:</span>
                        <span className="font-medium">
                          {formatCurrency(formData.amount)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Payment Method:</span>
                        <span className="font-medium">
                          {paymentMethods.find(m => m.id === formData.paymentMethod)?.label}
                        </span>
                      </div>
                      <div className="flex justify-between pt-2 border-t border-blue-200">
                        <span>New Balance:</span>
                        <span className="font-medium">
                          {formatCurrency(invoice.balanceDue - formData.amount)}
                        </span>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="bg-white rounded-xl border border-gray-200 p-8 text-center">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Receipt className="w-8 h-8 text-gray-400" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No Booking Selected
                </h3>
                <p className="text-gray-600 text-sm">
                  Select a booking or invoice to view details and receive payment
                </p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Action Bar */}
      {invoice && (
        <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-6 py-4 shadow-lg z-10">
          <div className="container mx-auto">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="text-sm text-gray-600">
                Invoice #{invoice.id} â€¢ Balance Due: {formatCurrency(invoice.balanceDue)}
              </div>
              
              <div className="flex flex-wrap gap-3">
                <button
                  onClick={handleCancel}
                  className="px-6 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                
                <button
                  onClick={() => handleSubmit('save-another')}
                  disabled={loading}
                  className="px-6 py-2.5 bg-gray-800 text-white rounded-lg hover:bg-gray-900 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  Save & Add Another
                </button>
                
                <button
                  onClick={() => handleSubmit('save')}
                  disabled={loading || !invoice}
                  className="flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  <Save className="w-4 h-4" />
                  {loading ? 'Processing...' : 'Save Payment'}
                </button>
                
                <button
                  onClick={() => handleSubmit('save-print')}
                  disabled={loading || !invoice}
                  className="flex items-center gap-2 px-6 py-2.5 bg-gray-800 text-white rounded-lg hover:bg-gray-900 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  <Printer className="w-4 h-4" />
                  Save & Print
                </button>
                
                <button
                  onClick={() => handleSubmit('save-email')}
                  disabled={loading || !invoice}
                  className="flex items-center gap-2 px-6 py-2.5 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  <Mail className="w-4 h-4" />
                  Save & Email
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
      
      {/* Error Message */}
      {Object.keys(errors).length > 0 && (
        <div className="fixed bottom-24 left-1/2 transform -translate-x-1/2 bg-red-50 border border-red-200 text-red-700 px-6 py-3 rounded-lg shadow-lg max-w-md z-20">
          <div className="flex items-center gap-2 mb-2">
            <X className="w-5 h-5" />
            <span className="font-medium">Please fix the following errors:</span>
          </div>
          <ul className="text-sm space-y-1">
            {Object.entries(errors).map(([key, error]) => (
              <li key={key}>â€¢ {error}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default Payment;