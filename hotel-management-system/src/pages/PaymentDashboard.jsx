// src/pages/PaymentDashboard.jsx
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
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
  MoreVertical,
  CheckCircle,
  AlertCircle,
  XCircle,
  DollarSign,
  Receipt,
  ChevronDown,
  ChevronUp,
  IndianRupee,
  FileSpreadsheet,
  DownloadCloud,
  RefreshCw,
  TrendingUp,
  TrendingDown,
  ArrowUpRight,
  ArrowDownRight,
  BarChart3,
  Home,
  Building,
  Globe,
  Shield,
  FileText,
  Calculator,
  Percent,
  RotateCcw,
  Tag,
  X,
  ArrowLeft,
  Save
} from 'lucide-react';

const PaymentDashboard = () => {
  const navigate = useNavigate();
  
  // Dummy Data
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
      type: 'PAYMENT',
      transactionId: null,
      remarks: 'Advance payment',
      receivedBy: 'John Doe'
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
      type: 'PAYMENT',
      transactionId: 'UPI123456789',
      remarks: 'Full payment via UPI',
      receivedBy: 'Jane Smith'
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
      type: 'PAYMENT',
      transactionId: 'CARD987654',
      remarks: 'Credit card payment',
      receivedBy: 'John Doe'
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
      type: 'INVOICE',
      transactionId: null,
      remarks: 'Payment pending',
      receivedBy: null
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
      type: 'PAYMENT',
      transactionId: 'BNK456789123',
      remarks: 'Bank transfer',
      receivedBy: 'Robert Johnson'
    },
    {
      id: 'PAY006',
      date: '2024-01-12T15:30:00',
      guestName: 'Anjali Mehta',
      guestId: 'GST006',
      bookingId: 'BK006',
      invoiceId: 'INV006',
      roomNumber: '105',
      amountPaid: -2000,
      totalBill: 0,
      balanceDue: 0,
      paymentMethod: 'CASH',
      status: 'REFUNDED',
      type: 'REFUND',
      transactionId: 'REF789123',
      remarks: 'Service charge refund',
      receivedBy: 'John Doe'
    },
    {
      id: 'PAY007',
      date: '2024-01-12T09:15:00',
      guestName: 'Rahul Verma',
      guestId: 'GST007',
      bookingId: 'BK007',
      invoiceId: 'INV007',
      roomNumber: '106',
      amountPaid: -1500,
      totalBill: 12000,
      balanceDue: 12000,
      paymentMethod: 'UPI',
      status: 'REFUNDED',
      type: 'REFUND',
      transactionId: 'REF456789',
      remarks: 'Early check-out refund',
      receivedBy: 'Jane Smith'
    },
    {
      id: 'PAY008',
      date: '2024-01-11T16:45:00',
      guestName: 'Neha Gupta',
      guestId: 'GST008',
      bookingId: 'BK008',
      invoiceId: 'INV008',
      roomNumber: '107',
      amountPaid: -1000,
      totalBill: 9000,
      balanceDue: 9000,
      paymentMethod: 'CARD',
      status: 'REFUNDED',
      type: 'REFUND',
      transactionId: 'REF123456',
      remarks: 'Cancellation refund',
      receivedBy: 'Robert Johnson'
    }
  ]);
  
  // State
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState({
    todayCollection: 17000,
    pendingPayments: 40000,
    digitalPayments: 20000,
    cashCollected: 5000,
    totalPayments: 40000,
    paymentCount: 5,
    refunds: 4500,
    averageTransaction: 8500
  });
  
  const [filters, setFilters] = useState({
    dateRange: 'today',
    status: 'all',
    paymentMethod: 'all',
    paymentType: 'all',
    search: '',
    minAmount: '',
    maxAmount: ''
  });
  
  const [selectedRows, setSelectedRows] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [sortConfig, setSortConfig] = useState({ key: 'date', direction: 'desc' });
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [modalMode, setModalMode] = useState('payment');
  
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
      REFUNDED: { color: 'bg-purple-100 text-purple-800 border-purple-200', icon: RotateCcw }
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
  
  // Get payment type badge
  const getPaymentTypeBadge = (type) => {
    const config = {
      PAYMENT: { color: 'bg-blue-100 text-blue-800', label: 'Payment' },
      REFUND: { color: 'bg-purple-100 text-purple-800', label: 'Refund' },
      DISCOUNT: { color: 'bg-amber-100 text-amber-800', label: 'Discount' },
      INVOICE: { color: 'bg-gray-100 text-gray-800', label: 'Invoice' }
    };
    
    const TypeConfig = config[type] || config.PAYMENT;
    
    return (
      <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${TypeConfig.color}`}>
        {TypeConfig.label}
      </span>
    );
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
  
  // Handle export filtered data
  const handleExportFiltered = (format) => {
    const filtered = getFilteredPayments();
    let content = '';
    let filename = '';
    let mimeType = '';
    
    if (format === 'excel') {
      content = "ID,Date,Guest Name,Booking ID,Invoice ID,Amount Paid,Total Bill,Balance Due,Payment Method,Status,Type,Remarks\n" +
        filtered.map(payment => 
          `"${payment.id}","${formatDate(payment.date)}","${payment.guestName}","${payment.bookingId}","${payment.invoiceId}",` +
          `${payment.amountPaid},${payment.totalBill},${payment.balanceDue},"${payment.paymentMethod}","${payment.status}",` +
          `"${payment.type}","${payment.remarks}"`
        ).join("\n");
      filename = `filtered_payments_${new Date().toISOString().split('T')[0]}.csv`;
      mimeType = 'text/csv';
    } else if (format === 'pdf') {
      content = 'PDF export would be generated here';
      filename = `payments_report_${new Date().toISOString().split('T')[0]}.pdf`;
      mimeType = 'application/pdf';
    }
    
    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
    
    alert(`${format.toUpperCase()} exported successfully!`);
  };
  
  // Handle open modal
  const handleOpenModal = (mode) => {
    setModalMode(mode);
    setShowPaymentModal(true);
  };
  
  // Handle close modal
  const handleCloseModal = () => {
    setShowPaymentModal(false);
  };
  
  // Handle save from modal
  const handleSaveModal = (data) => {
    alert(`${modalMode.toUpperCase()} of ${formatCurrency(data.amount)} processed successfully!`);
    setShowPaymentModal(false);
    
    // Add to payments list (demo)
    if (modalMode === 'payment') {
      const newPayment = {
        id: `PAY${Date.now().toString().slice(-6)}`,
        date: new Date().toISOString(),
        guestName: 'Test Guest',
        guestId: 'GST999',
        bookingId: 'BK999',
        invoiceId: 'INV999',
        roomNumber: '999',
        amountPaid: data.amount,
        totalBill: data.amount,
        balanceDue: 0,
        paymentMethod: data.paymentMethod,
        status: 'PAID',
        type: 'PAYMENT',
        transactionId: `TXN${Date.now().toString().slice(-6)}`,
        remarks: data.remarks,
        receivedBy: data.receivedBy || 'John Doe'
      };
      
      setPayments(prev => [newPayment, ...prev]);
    }
  };
  
  // Handle view payment details
  const handleViewPayment = (paymentId) => {
    const payment = payments.find(p => p.id === paymentId);
    if (payment) {
      alert(`Payment Details:\nID: ${payment.id}\nGuest: ${payment.guestName}\nAmount: ${formatCurrency(payment.amountPaid)}\nStatus: ${payment.status}`);
    }
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
    if (selectedRows.length === 0) {
      alert('Please select payments first');
      return;
    }
    
    const selectedPayments = payments.filter(p => selectedRows.includes(p.id));
    
    if (action === 'export') {
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
    } else if (action === 'refund') {
      if (selectedRows.length > 1) {
        alert('Please select only one payment for refund');
        return;
      }
      const payment = selectedPayments[0];
      if (payment.amountPaid <= 0) {
        alert('Cannot refund a payment with zero or negative amount');
        return;
      }
      handleOpenModal('refund');
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
  const getFilteredPayments = () => {
    let filtered = sortedPayments.filter(payment => {
      // Search filter
      if (filters.search) {
        const searchLower = filters.search.toLowerCase();
        if (!payment.guestName.toLowerCase().includes(searchLower) &&
            !payment.bookingId.toLowerCase().includes(searchLower) &&
            !payment.invoiceId.toLowerCase().includes(searchLower) &&
            !payment.roomNumber.toLowerCase().includes(searchLower) &&
            !payment.id.toLowerCase().includes(searchLower)) {
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
      
      // Payment type filter
      if (filters.paymentType !== 'all' && payment.type !== filters.paymentType) {
        return false;
      }
      
      // Amount filters
      if (filters.minAmount && Math.abs(payment.amountPaid) < parseFloat(filters.minAmount)) {
        return false;
      }
      
      if (filters.maxAmount && Math.abs(payment.amountPaid) > parseFloat(filters.maxAmount)) {
        return false;
      }
      
      // Date range filter
      const paymentDate = new Date(payment.date);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      if (filters.dateRange === 'today') {
        const todayStr = today.toDateString();
        const paymentDateStr = paymentDate.toDateString();
        if (todayStr !== paymentDateStr) return false;
      } else if (filters.dateRange === 'week') {
        const weekAgo = new Date(today);
        weekAgo.setDate(weekAgo.getDate() - 7);
        if (paymentDate < weekAgo) return false;
      } else if (filters.dateRange === 'month') {
        const monthAgo = new Date(today);
        monthAgo.setMonth(monthAgo.getMonth() - 1);
        if (paymentDate < monthAgo) return false;
      }
      
      return true;
    });
    
    return filtered;
  };
  
  const filteredPayments = getFilteredPayments();
  
  // Calculate totals for filtered data
  const calculateTotals = () => {
    const totals = {
      payments: filteredPayments.filter(p => p.type === 'PAYMENT').reduce((sum, p) => sum + (p.amountPaid > 0 ? p.amountPaid : 0), 0),
      refunds: filteredPayments.filter(p => p.type === 'REFUND').reduce((sum, p) => sum + Math.abs(p.amountPaid), 0),
      count: filteredPayments.length
    };
    return totals;
  };
  
  const totals = calculateTotals();
  
  // KPI Cards data
  const KPI_CARDS = [
    {
      title: "Today's Collection",
      value: stats.todayCollection,
      icon: <Banknote className="w-5 h-5 text-green-600" />,
      color: 'bg-green-50 border-green-100',
      description: 'Total received today',
      trend: '+12%',
      trendUp: true,
      onClick: () => handleFilterChange('dateRange', 'today')
    },
    {
      title: "Pending Payments",
      value: stats.pendingPayments,
      icon: <Clock className="w-5 h-5 text-yellow-600" />,
      color: 'bg-yellow-50 border-yellow-100',
      description: 'Balance due',
      trend: '-5%',
      trendUp: false,
      onClick: () => handleFilterChange('status', 'PENDING')
    },
    {
      title: "Digital Payments",
      value: stats.digitalPayments,
      icon: <Smartphone className="w-5 h-5 text-blue-600" />,
      color: 'bg-blue-50 border-blue-100',
      description: 'UPI + Card + Online',
      trend: '+18%',
      trendUp: true,
      onClick: () => handleFilterChange('paymentMethod', 'UPI')
    },
    {
      title: "Cash Collected",
      value: stats.cashCollected,
      icon: <IndianRupee className="w-5 h-5 text-purple-600" />,
      color: 'bg-purple-50 border-purple-100',
      description: 'Physical cash',
      trend: '+3%',
      trendUp: true,
      onClick: () => handleFilterChange('paymentMethod', 'CASH')
    },
    {
      title: "Refunds Issued",
      value: stats.refunds,
      icon: <RotateCcw className="w-5 h-5 text-red-600" />,
      color: 'bg-red-50 border-red-100',
      description: 'Total refunded',
      trend: '+2%',
      trendUp: false,
      onClick: () => handleFilterChange('paymentType', 'REFUND')
    }
  ];
  
  // Table columns
  const columns = [
    { key: 'select', label: '', sortable: false },
    { key: 'date', label: 'Date', sortable: true },
    { key: 'guest', label: 'Guest', sortable: true },
    { key: 'booking', label: 'Booking / Invoice', sortable: true },
    { key: 'amount', label: 'Amount', sortable: true },
    { key: 'balance', label: 'Balance', sortable: true },
    { key: 'method', label: 'Method' },
    { key: 'type', label: 'Type' },
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
            <h1 className="text-3xl font-bold text-gray-900">Payment Management</h1>
            <p className="text-gray-600 mt-1">Track and manage all payments, refunds, and transactions</p>
          </div>
          
          <div className="flex flex-wrap gap-3">
            <Link
              to="/daily-sheet"
              className="flex items-center gap-2 px-4 py-2.5 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
            >
              <FileText className="w-4 h-4" />
              Daily Sheet
            </Link>
            <button
              onClick={() => handleOpenModal('refund')}
              className="flex items-center gap-2 px-4 py-2.5 border border-red-300 text-red-700 rounded-lg hover:bg-red-50 transition-colors"
            >
              <RotateCcw className="w-4 h-4" />
              Refund
            </button>
            <button
              onClick={() => handleOpenModal('payment')}
              className="flex items-center gap-2 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus className="w-4 h-4" />
              Receive Payment
            </button>
            <button
              onClick={() => handleOpenModal('discount')}
              className="flex items-center gap-2 px-4 py-2.5 bg-amber-500 text-white rounded-lg hover:bg-amber-600 transition-colors"
            >
              <Percent className="w-4 h-4" />
              Apply Discount
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
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6 mb-8">
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
              <div className={`flex items-center gap-1 text-sm ${card.trendUp ? 'text-green-600' : 'text-red-600'}`}>
                {card.trendUp ? <ArrowUpRight className="w-4 h-4" /> : <ArrowDownRight className="w-4 h-4" />}
                <span>{card.trend}</span>
              </div>
            </div>
            <div className="text-2xl font-bold text-gray-900">
              {formatCurrency(card.value)}
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
              <option value="week">Last 7 Days</option>
              <option value="month">This Month</option>
              <option value="quarter">This Quarter</option>
              <option value="all">All Time</option>
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
              <option value="REFUNDED">Refunded</option>
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
              <option value="ONLINE">Online</option>
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
                    value={filters.minAmount}
                    onChange={(e) => handleFilterChange('minAmount', e.target.value)}
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
                    value={filters.maxAmount}
                    onChange={(e) => handleFilterChange('maxAmount', e.target.value)}
                    placeholder="100000"
                    className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Payment Type
                </label>
                <select
                  value={filters.paymentType}
                  onChange={(e) => handleFilterChange('paymentType', e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                >
                  <option value="all">All Types</option>
                  <option value="PAYMENT">Payment</option>
                  <option value="REFUND">Refund</option>
                  <option value="DISCOUNT">Discount</option>
                  <option value="INVOICE">Invoice</option>
                </select>
              </div>
            </div>
            
            <div className="flex justify-end gap-3 mt-4">
              <button
                onClick={() => {
                  setFilters({
                    dateRange: 'today',
                    status: 'all',
                    paymentMethod: 'all',
                    paymentType: 'all',
                    search: '',
                    minAmount: '',
                    maxAmount: ''
                  });
                  setShowAdvancedFilters(false);
                }}
                className="px-4 py-2 text-sm text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-lg"
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

      {/* Summary Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
        <div className="text-sm text-gray-600">
          Showing {filteredPayments.length} payment(s) • 
          Total Payments: <span className="font-medium text-green-600">{formatCurrency(totals.payments)}</span> • 
          Total Refunds: <span className="font-medium text-red-600">{formatCurrency(totals.refunds)}</span>
        </div>
        
        <div className="flex gap-2">
          <button
            onClick={() => handleExportFiltered('excel')}
            className="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
          >
            <FileSpreadsheet className="w-4 h-4" />
            Export Excel
          </button>
          <button
            onClick={() => handleExportFiltered('pdf')}
            className="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
          >
            <Download className="w-4 h-4" />
            Export PDF
          </button>
          <button
            onClick={() => {
              setLoading(true);
              setTimeout(() => {
                setLoading(false);
                alert('Data refreshed');
              }, 500);
            }}
            className="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
          >
            <RefreshCw className="w-4 h-4" />
            Refresh
          </button>
        </div>
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
                className="flex items-center gap-2 px-3 py-1.5 text-sm bg-white border border-blue-300 text-blue-700 rounded hover:bg-blue-50"
              >
                <DownloadCloud className="w-4 h-4" />
                Export Selected
              </button>
              <button
                onClick={() => handleBulkAction('print')}
                className="flex items-center gap-2 px-3 py-1.5 text-sm bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                <Printer className="w-4 h-4" />
                Print Receipts
              </button>
              <button
                onClick={() => handleBulkAction('refund')}
                className="flex items-center gap-2 px-3 py-1.5 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200"
              >
                <RotateCcw className="w-4 h-4" />
                Refund Selected
              </button>
              <button
                onClick={() => setSelectedRows([])}
                className="px-3 py-1.5 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
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
                      onClick={() => handleOpenModal('payment')}
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
                        <div className="text-blue-600 text-sm">
                          Booking #{payment.bookingId}
                        </div>
                        {payment.invoiceId && (
                          <div className="text-gray-600 text-sm">
                            Invoice #{payment.invoiceId}
                          </div>
                        )}
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <div className={`font-semibold ${payment.type === 'REFUND' ? 'text-red-600' : payment.type === 'DISCOUNT' ? 'text-amber-600' : 'text-gray-900'}`}>
                        {payment.type === 'REFUND' ? '-' : ''}{formatCurrency(Math.abs(payment.amountPaid))}
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <div className={`font-semibold ${payment.balanceDue > 0 ? 'text-red-600' : 'text-green-600'}`}>
                        {formatCurrency(payment.balanceDue)}
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        {payment.paymentMethod && (
                          <div className={`p-1.5 rounded ${payment.paymentMethod === 'CASH' ? 'bg-green-100 text-green-800' : payment.paymentMethod === 'UPI' ? 'bg-blue-100 text-blue-800' : payment.paymentMethod === 'CARD' ? 'bg-purple-100 text-purple-800' : 'bg-gray-100 text-gray-800'}`}>
                            {getPaymentMethodIcon(payment.paymentMethod)}
                          </div>
                        )}
                        <span className="text-sm text-gray-700">
                          {payment.paymentMethod || 'N/A'}
                        </span>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      {getPaymentTypeBadge(payment.type)}
                    </td>
                    <td className="py-3 px-4">
                      {getStatusBadge(payment.status)}
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handleViewPayment(payment.id)}
                          className="p-1.5 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="View Details"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        {payment.type === 'PAYMENT' && payment.amountPaid > 0 && (
                          <button
                            onClick={() => handleOpenModal('refund')}
                            className="p-1.5 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            title="Refund"
                          >
                            <RotateCcw className="w-4 h-4" />
                          </button>
                        )}
                        <button
                          onClick={() => alert(`Print receipt for ${payment.id}`)}
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

      {/* Payment Modal */}
      {showPaymentModal && (
        <PaymentModal 
          mode={modalMode}
          onClose={handleCloseModal}
          onSave={handleSaveModal}
        />
      )}
    </div>
  );
};

// Payment Modal Component
const PaymentModal = ({ mode, onClose, onSave }) => {
  const [amount, setAmount] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [remarks, setRemarks] = useState('');
  const [discountType, setDiscountType] = useState('percentage');
  const [discountValue, setDiscountValue] = useState('');
  
  const getTitle = () => {
    switch(mode) {
      case 'payment': return 'Receive Payment';
      case 'refund': return 'Process Refund';
      case 'discount': return 'Apply Discount';
      default: return 'Transaction';
    }
  };
  
  const getColor = () => {
    switch(mode) {
      case 'payment': return 'bg-blue-600';
      case 'refund': return 'bg-red-600';
      case 'discount': return 'bg-amber-600';
      default: return 'bg-blue-600';
    }
  };
  
  const handleSubmit = () => {
    if (!amount || parseFloat(amount) <= 0) {
      alert('Please enter a valid amount');
      return;
    }
    
    onSave({
      amount: parseFloat(amount),
      paymentMethod,
      remarks,
      discountType,
      discountValue,
      receivedBy: localStorage.getItem('userName') || 'John Doe (Reception)'
    });
  };
  
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl p-6 max-w-md w-full">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-gray-900">{getTitle()}</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>
        
        <div className="space-y-4">
          {mode === 'discount' && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Discount Type</label>
                <select
                  value={discountType}
                  onChange={(e) => setDiscountType(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                >
                  <option value="percentage">Percentage (%)</option>
                  <option value="fixed">Fixed Amount</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  {discountType === 'percentage' ? 'Percentage' : 'Amount'}
                </label>
                <div className="relative">
                  {discountType === 'percentage' ? (
                    <>
                      <input
                        type="number"
                        value={discountValue}
                        onChange={(e) => {
                          setDiscountValue(e.target.value);
                          if (e.target.value) {
                            // Calculate 10% of sample amount for demo
                            setAmount((1000 * parseFloat(e.target.value) / 100).toString());
                          }
                        }}
                        className="w-full pl-3 pr-10 py-2 border border-gray-300 rounded-lg"
                        placeholder="10"
                      />
                      <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                        <Percent className="w-5 h-5 text-gray-400" />
                      </div>
                    </>
                  ) : (
                    <>
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center">
                        <IndianRupee className="w-5 h-5 text-gray-400" />
                      </div>
                      <input
                        type="number"
                        value={discountValue}
                        onChange={(e) => {
                          setDiscountValue(e.target.value);
                          setAmount(e.target.value);
                        }}
                        className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg"
                        placeholder="100"
                      />
                    </>
                  )}
                </div>
              </div>
            </div>
          )}
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              {mode === 'refund' ? 'Refund Amount' : mode === 'discount' ? 'Discount Amount' : 'Payment Amount'} *
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center">
                <IndianRupee className="w-5 h-5 text-gray-400" />
              </div>
              <input
                type="number"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg"
                placeholder="0.00"
                required
                disabled={mode === 'discount' && discountType === 'percentage'}
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              {mode === 'refund' ? 'Refund Method' : 'Payment Method'} *
            </label>
            <select
              value={paymentMethod}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="w-full px-3 py-3 border border-gray-300 rounded-lg"
              required
            >
              <option value="CASH">Cash</option>
              <option value="UPI">UPI</option>
              <option value="CARD">Card</option>
              <option value="BANK">Bank Transfer</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Remarks
            </label>
            <textarea
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              rows="3"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              placeholder="Add any notes or reference..."
            />
          </div>
        </div>
        
        <div className="flex gap-3 mt-6 pt-6 border-t border-gray-200">
          <button
            onClick={onClose}
            className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 flex-1"
          >
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            className={`px-4 py-2 text-white rounded-lg flex-1 ${getColor()}`}
            disabled={!amount}
          >
            {mode === 'payment' ? 'Receive Payment' : 
             mode === 'refund' ? 'Process Refund' : 'Apply Discount'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentDashboard;