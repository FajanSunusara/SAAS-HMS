// src/pages/DailySheet.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Calendar,
  Download,
  Printer,
  ChevronRight,
  IndianRupee,
  FileText,
  Search,
  Filter,
  ChevronDown,
  ChevronUp,
  Eye,
  Edit,
  RefreshCw,
  Home,
  User,
  CreditCard,
  Smartphone,
  Building,
  Wallet,
  CheckCircle,
  AlertCircle,
  Clock,
  Check,
  X,
  Loader,
  FileSpreadsheet,
  DownloadCloud,
  Users,
  TrendingUp,
  TrendingDown,
  MoreVertical,
  Filter as FilterIcon,
  BarChart3,
  DollarSign,
  Bed,
  Key,
  LogOut,
  Plus,
  Minus,
  FileBarChart
} from 'lucide-react';

const DailySheet = () => {
  // Today's date
  const today = new Date();
  const [selectedDate, setSelectedDate] = useState(today.toISOString().split('T')[0]);
  const [loading, setLoading] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  
  // Date options
  const [dateType, setDateType] = useState('today'); // today, yesterday, custom
  
  // KPI Summary Data
  const [kpiSummary, setKpiSummary] = useState({
    newBookings: 8,
    occupiedRooms: { current: 21, total: 30 },
    todaysCollection: 48500,
    pendingAmount: 12300,
    totalRevenue: 65800
  });
  
  // Daily Sheet Data - Room-wise breakdown
  const [dailySheetData, setDailySheetData] = useState([
    {
      id: 1,
      roomNo: '101',
      roomStatus: 'occupied',
      guestName: 'Rajesh Kumar',
      checkIn: '2024-03-10 14:00',
      checkOut: '2024-03-12 12:00',
      todaysCharge: 2500,
      paymentReceived: 2500,
      paymentMode: 'UPI',
      pendingAmount: 0,
      totalBill: 7500,
      bookingType: 'standard'
    },
    {
      id: 2,
      roomNo: '102',
      roomStatus: 'occupied',
      guestName: 'Priya Sharma',
      checkIn: '2024-03-11 15:30',
      checkOut: '2024-03-13 12:00',
      todaysCharge: 3000,
      paymentReceived: 1500,
      paymentMode: 'CASH',
      pendingAmount: 1500,
      totalBill: 9000,
      bookingType: 'deluxe'
    },
    {
      id: 3,
      roomNo: '103',
      roomStatus: 'checkout',
      guestName: 'Amit Patel',
      checkIn: '2024-03-10 12:00',
      checkOut: '2024-03-11 12:00',
      todaysCharge: 2000,
      paymentReceived: 2000,
      paymentMode: 'CARD',
      pendingAmount: 0,
      totalBill: 2000,
      bookingType: 'standard'
    },
    {
      id: 4,
      roomNo: '104',
      roomStatus: 'vacant',
      guestName: '-',
      checkIn: '-',
      checkOut: '-',
      todaysCharge: 0,
      paymentReceived: 0,
      paymentMode: '-',
      pendingAmount: 0,
      totalBill: 0,
      bookingType: '-'
    },
    {
      id: 5,
      roomNo: '105',
      roomStatus: 'occupied',
      guestName: 'Vikram Singh',
      checkIn: '2024-03-09 16:00',
      checkOut: '2024-03-15 12:00',
      todaysCharge: 3500,
      paymentReceived: 3500,
      paymentMode: 'BANK',
      pendingAmount: 0,
      totalBill: 24500,
      bookingType: 'suite'
    },
    {
      id: 6,
      roomNo: '106',
      roomStatus: 'occupied',
      guestName: 'Sunita Reddy',
      checkIn: '2024-03-12 13:00',
      checkOut: '2024-03-14 12:00',
      todaysCharge: 2800,
      paymentReceived: 2800,
      paymentMode: 'UPI',
      pendingAmount: 0,
      totalBill: 5600,
      bookingType: 'deluxe'
    },
    {
      id: 7,
      roomNo: '107',
      roomStatus: 'occupied',
      guestName: 'Rohit Verma',
      checkIn: '2024-03-11 18:00',
      checkOut: '2024-03-13 12:00',
      todaysCharge: 2200,
      paymentReceived: 1000,
      paymentMode: 'CASH',
      pendingAmount: 1200,
      totalBill: 4400,
      bookingType: 'standard'
    },
    {
      id: 8,
      roomNo: '108',
      roomStatus: 'occupied',
      guestName: 'Anjali Mehta',
      checkIn: '2024-03-10 14:30',
      checkOut: '2024-03-12 12:00',
      todaysCharge: 3200,
      paymentReceived: 3200,
      paymentMode: 'CARD',
      pendingAmount: 0,
      totalBill: 6400,
      bookingType: 'deluxe'
    },
    {
      id: 9,
      roomNo: '109',
      roomStatus: 'checkout',
      guestName: 'Kiran Desai',
      checkIn: '2024-03-10 12:00',
      checkOut: '2024-03-11 12:00',
      todaysCharge: 1800,
      paymentReceived: 1800,
      paymentMode: 'UPI',
      pendingAmount: 0,
      totalBill: 1800,
      bookingType: 'standard'
    },
    {
      id: 10,
      roomNo: '110',
      roomStatus: 'vacant',
      guestName: '-',
      checkIn: '-',
      checkOut: '-',
      todaysCharge: 0,
      paymentReceived: 0,
      paymentMode: '-',
      pendingAmount: 0,
      totalBill: 0,
      bookingType: '-'
    }
  ]);
  
  // Filters state
  const [filters, setFilters] = useState({
    roomStatus: 'all',
    paymentMode: 'all',
    pendingOnly: false,
    search: ''
  });
  
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
    if (!dateString || dateString === '-') return '-';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  };
  
  // Format date only (without time)
  const formatDateOnly = (dateString) => {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    }).format(date);
  };
  
  // Get room status badge
  const getRoomStatusBadge = (status) => {
    const config = {
      occupied: { 
        color: 'bg-green-100 text-green-800', 
        icon: Bed,
        label: 'Occupied'
      },
      vacant: { 
        color: 'bg-gray-100 text-gray-800', 
        icon: Key,
        label: 'Vacant'
      },
      checkout: { 
        color: 'bg-orange-100 text-orange-800', 
        icon: LogOut,
        label: 'Checkout'
      }
    };
    
    const StatusConfig = config[status] || config.vacant;
    const Icon = StatusConfig.icon;
    
    return (
      <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${StatusConfig.color}`}>
        <Icon className="w-3 h-3 mr-1" />
        {StatusConfig.label}
      </span>
    );
  };
  
  // Get payment mode icon
  const getPaymentModeIcon = (mode) => {
    const icons = {
      CASH: { icon: Wallet, color: 'text-green-600 bg-green-100' },
      UPI: { icon: Smartphone, color: 'text-blue-600 bg-blue-100' },
      CARD: { icon: CreditCard, color: 'text-purple-600 bg-purple-100' },
      BANK: { icon: Building, color: 'text-indigo-600 bg-indigo-100' }
    };
    
    const config = icons[mode] || { icon: DollarSign, color: 'text-gray-600 bg-gray-100' };
    const Icon = config.icon;
    
    return (
      <div className={`p-1.5 rounded ${config.color}`}>
        <Icon className="w-4 h-4" />
      </div>
    );
  };
  
  // Get payment mode label
  const getPaymentModeLabel = (mode) => {
    const labels = {
      CASH: 'Cash',
      UPI: 'UPI',
      CARD: 'Card',
      BANK: 'Bank'
    };
    return labels[mode] || mode;
  };
  
  // Handle date change
  const handleDateChange = (e) => {
    const newDate = e.target.value;
    setSelectedDate(newDate);
    setDateType('custom');
    
    // In real app, this would fetch data for selected date
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 500);
  };
  
  // Handle quick date selection
  const handleQuickDate = (type) => {
    setDateType(type);
    const date = new Date();
    
    if (type === 'yesterday') {
      date.setDate(date.getDate() - 1);
    }
    // else if type === 'today', use current date
    
    setSelectedDate(date.toISOString().split('T')[0]);
    
    // In real app, fetch data
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 500);
  };
  
  // Handle filter change
  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };
  
  // Handle export
  const handleExport = (format) => {
    const content = format === 'pdf' 
      ? 'PDF content would be generated here'
      : 'Excel content would be generated here';
    
    const filename = `daily_sheet_${selectedDate}.${format}`;
    const blob = new Blob([content], { type: format === 'pdf' ? 'application/pdf' : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
    
    alert(`${format.toUpperCase()} exported successfully!`);
  };
  
  // Handle print
  const handlePrint = () => {
    window.print();
  };
  
  // Handle refresh
  const handleRefresh = () => {
    setLoading(true);
    setTimeout(() => {
      alert('Data refreshed successfully!');
      setLoading(false);
    }, 1000);
  };
  
  // Calculate filtered data
  const getFilteredData = () => {
    let filtered = [...dailySheetData];
    
    // Apply room status filter
    if (filters.roomStatus !== 'all') {
      filtered = filtered.filter(item => item.roomStatus === filters.roomStatus);
    }
    
    // Apply payment mode filter
    if (filters.paymentMode !== 'all') {
      filtered = filtered.filter(item => item.paymentMode === filters.paymentMode);
    }
    
    // Apply pending only filter
    if (filters.pendingOnly) {
      filtered = filtered.filter(item => item.pendingAmount > 0);
    }
    
    // Apply search filter
    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(item => 
        item.roomNo.toLowerCase().includes(searchLower) ||
        item.guestName.toLowerCase().includes(searchLower)
      );
    }
    
    return filtered;
  };
  
  // Calculate totals
  const calculateTotals = () => {
    const filtered = getFilteredData();
    const totals = {
      todaysCharge: filtered.reduce((sum, item) => sum + item.todaysCharge, 0),
      paymentReceived: filtered.reduce((sum, item) => sum + item.paymentReceived, 0),
      pendingAmount: filtered.reduce((sum, item) => sum + item.pendingAmount, 0),
      totalBill: filtered.reduce((sum, item) => sum + item.totalBill, 0),
      count: filtered.length
    };
    
    return totals;
  };
  
  const filteredData = getFilteredData();
  const totals = calculateTotals();
  
  // Get active date label
  const getActiveDateLabel = () => {
    if (dateType === 'today') return 'Today';
    if (dateType === 'yesterday') return 'Yesterday';
    return formatDateOnly(selectedDate);
  };
  
  // KPI Cards Data
  const kpiCards = [
    {
      title: 'New Bookings',
      value: kpiSummary.newBookings,
      icon: <FileText className="w-5 h-5 text-blue-600" />,
      color: 'bg-blue-50 border-blue-100',
      description: 'Created today',
      trend: '+2',
      trendUp: true
    },
    {
      title: 'Occupied Rooms',
      value: `${kpiSummary.occupiedRooms.current} / ${kpiSummary.occupiedRooms.total}`,
      icon: <Bed className="w-5 h-5 text-green-600" />,
      color: 'bg-green-50 border-green-100',
      description: 'Current occupancy',
      trend: '+3',
      trendUp: true
    },
    {
      title: "Today's Collection",
      value: kpiSummary.todaysCollection,
      icon: <IndianRupee className="w-5 h-5 text-purple-600" />,
      color: 'bg-purple-50 border-purple-100',
      description: 'Received today',
      trend: '+8%',
      trendUp: true
    },
    {
      title: 'Pending Amount',
      value: kpiSummary.pendingAmount,
      icon: <Clock className="w-5 h-5 text-yellow-600" />,
      color: 'bg-yellow-50 border-yellow-100',
      description: 'Yet to collect',
      trend: '-5%',
      trendUp: false
    },
    {
      title: 'Total Revenue',
      value: kpiSummary.totalRevenue,
      icon: <DollarSign className="w-5 h-5 text-indigo-600" />,
      color: 'bg-indigo-50 border-indigo-100',
      description: 'Generated today',
      trend: '+12%',
      trendUp: true
    }
  ];
  
  return (
    <div className="container mx-auto px-4 py-6 print:px-0 print:py-0">
      {/* Loading Overlay */}
      {loading && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg shadow-xl">
            <div className="flex items-center gap-3">
              <Loader className="w-5 h-5 animate-spin text-blue-600" />
              <span className="text-gray-700">Loading daily sheet...</span>
            </div>
          </div>
        </div>
      )}
      
      {/* Page Header */}
      <div className="mb-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
          <div>
            <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
              <Link to="/dashboard" className="hover:text-blue-600">Dashboard</Link>
              <ChevronRight className="w-4 h-4" />
              <Link to="/finance" className="hover:text-blue-600">Finance</Link>
              <ChevronRight className="w-4 h-4" />
              <span className="text-blue-600 font-medium">Daily Sheet</span>
            </div>
            <h1 className="text-3xl font-bold text-gray-900">Daily Sheet</h1>
            <p className="text-gray-600 mt-1">Room-wise daily financial report</p>
          </div>
          
          <div className="flex items-center gap-3">
            <div className="px-4 py-2 bg-gray-100 rounded-lg">
              <span className="text-sm text-gray-600">Active Date:</span>
              <span className="ml-2 text-sm font-medium text-gray-900">
                {getActiveDateLabel()}
              </span>
            </div>
          </div>
        </div>
        
        {/* Filter & Action Bar */}
        <div className="bg-white border border-gray-200 rounded-xl p-4 mb-6">
          <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
            {/* Left Side - Filters */}
            <div className="flex flex-col md:flex-row md:items-center gap-4">
              {/* Date Picker & Quick Chips */}
              <div className="flex items-center gap-3">
                <div className="relative">
                  <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                  <input
                    type="date"
                    value={selectedDate}
                    onChange={handleDateChange}
                    className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg bg-white"
                  />
                </div>
                
                <div className="flex gap-2">
                  <button
                    onClick={() => handleQuickDate('today')}
                    className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                      dateType === 'today'
                        ? 'bg-blue-100 text-blue-700 border border-blue-200'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                  >
                    Today
                  </button>
                  <button
                    onClick={() => handleQuickDate('yesterday')}
                    className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                      dateType === 'yesterday'
                        ? 'bg-blue-100 text-blue-700 border border-blue-200'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                  >
                    Yesterday
                  </button>
                  <Link
  to="/payments"
  className="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
>
  <CreditCard className="w-4 h-4" />
  Payments
</Link>
                </div>
              </div>
              
              {/* Search */}
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search room or guest..."
                  value={filters.search}
                  onChange={(e) => handleFilterChange('search', e.target.value)}
                  className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg w-full md:w-64"
                />
              </div>
            </div>
            
            {/* Right Side - Actions */}
            <div className="flex flex-wrap gap-2">
              <button
                onClick={() => setShowFilters(!showFilters)}
                className="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
              >
                <FilterIcon className="w-4 h-4" />
                Filters
              </button>
              <button
                onClick={handleRefresh}
                className="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
              >
                <RefreshCw className="w-4 h-4" />
                Load
              </button>
              <button
                onClick={() => handleExport('pdf')}
                className="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
              >
                <FileSpreadsheet className="w-4 h-4" />
                PDF
              </button>
              <button
                onClick={() => handleExport('excel')}
                className="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
              >
                <DownloadCloud className="w-4 h-4" />
                Excel
              </button>
              <button
                onClick={handlePrint}
                className="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors"
              >
                <Printer className="w-4 h-4" />
                Print
              </button>
            </div>
          </div>
          
          {/* Advanced Filters */}
          {showFilters && (
            <div className="mt-4 pt-4 border-t border-gray-200">
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Room Status
                  </label>
                  <select
                    value={filters.roomStatus}
                    onChange={(e) => handleFilterChange('roomStatus', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  >
                    <option value="all">All Status</option>
                    <option value="occupied">Occupied</option>
                    <option value="vacant">Vacant</option>
                    <option value="checkout">Checkout</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Payment Mode
                  </label>
                  <select
                    value={filters.paymentMode}
                    onChange={(e) => handleFilterChange('paymentMode', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  >
                    <option value="all">All Methods</option>
                    <option value="CASH">Cash</option>
                    <option value="UPI">UPI</option>
                    <option value="CARD">Card</option>
                    <option value="BANK">Bank</option>
                  </select>
                </div>
                
                <div className="flex items-center">
                  <label className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      checked={filters.pendingOnly}
                      onChange={(e) => handleFilterChange('pendingOnly', e.target.checked)}
                      className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span className="text-sm text-gray-700">Pending Amount Only</span>
                  </label>
                </div>
                
                <div className="flex items-end">
                  <button
                    onClick={() => {
                      setFilters({
                        roomStatus: 'all',
                        paymentMode: 'all',
                        pendingOnly: false,
                        search: ''
                      });
                    }}
                    className="px-4 py-2 text-sm text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-lg"
                  >
                    Clear All Filters
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* KPI Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4 mb-8 print:grid-cols-3">
        {kpiCards.map((card, index) => (
          <div
            key={index}
            className={`${card.color} border rounded-xl p-4 transition-all duration-200 hover:shadow-sm`}
          >
            <div className="flex items-center justify-between mb-3">
              <div className="p-2 bg-white rounded-lg shadow-sm">
                {card.icon}
              </div>
              <div className={`flex items-center gap-1 text-sm ${
                card.trendUp === true ? 'text-green-600' : 
                card.trendUp === false ? 'text-red-600' : 'text-gray-600'
              }`}>
                {card.trendUp === true && <TrendingUp className="w-4 h-4" />}
                {card.trendUp === false && <TrendingDown className="w-4 h-4" />}
                <span>{card.trend}</span>
              </div>
            </div>
            <div className="text-2xl font-bold text-gray-900 mb-1">
              {typeof card.value === 'number' && card.value >= 1000 
                ? formatCurrency(card.value)
                : card.value}
            </div>
            <div className="text-sm font-medium text-gray-900">{card.title}</div>
            <div className="text-xs text-gray-600 mt-1">{card.description}</div>
          </div>
        ))}
      </div>

      {/* Daily Sheet Table */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden mb-8">
        {/* Table Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">
              Room-wise Financial Summary
            </h2>
            <div className="text-sm text-gray-600">
              Showing {filteredData.length} room(s)
            </div>
          </div>
        </div>
        
        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Room No</th>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Room Status</th>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Guest Name</th>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Check-In</th>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Check-Out</th>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Today's Charge</th>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Payment Received</th>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Payment Mode</th>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Pending Amount</th>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Total Bill</th>
                <th className="py-3 px-6 text-left text-sm font-semibold text-gray-900">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredData.length === 0 ? (
                <tr>
                  <td colSpan={11} className="py-12 text-center">
                    <div className="text-gray-500">No rooms found for the selected filters</div>
                    <div className="text-sm text-gray-400 mt-2">
                      Try changing filters or select a different date
                    </div>
                  </td>
                </tr>
              ) : (
                filteredData.map((room) => (
                  <tr key={room.id} className="hover:bg-gray-50">
                    <td className="py-4 px-6">
                      <div className="text-sm font-medium text-gray-900">
                        {room.roomNo}
                      </div>
                      <div className="text-xs text-gray-500">
                        {room.bookingType !== '-' ? room.bookingType.charAt(0).toUpperCase() + room.bookingType.slice(1) : ''}
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      {getRoomStatusBadge(room.roomStatus)}
                    </td>
                    <td className="py-4 px-6">
                      <div className="text-sm font-medium text-gray-900">
                        {room.guestName}
                      </div>
                    </td>
                    <td className="py-4 px-6 text-sm text-gray-700">
                      {formatDate(room.checkIn)}
                    </td>
                    <td className="py-4 px-6 text-sm text-gray-700">
                      {formatDate(room.checkOut)}
                    </td>
                    <td className="py-4 px-6">
                      <div className="text-sm font-medium text-gray-900">
                        {formatCurrency(room.todaysCharge)}
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      <div className="text-sm font-medium text-green-600">
                        {formatCurrency(room.paymentReceived)}
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      <div className="flex items-center gap-2">
                        {room.paymentMode !== '-' && getPaymentModeIcon(room.paymentMode)}
                        <span className="text-sm text-gray-700">
                          {getPaymentModeLabel(room.paymentMode)}
                        </span>
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      <div className={`text-sm font-medium ${room.pendingAmount > 0 ? 'text-yellow-600' : 'text-gray-600'}`}>
                        {formatCurrency(room.pendingAmount)}
                        {room.pendingAmount > 0 && (
                          <span className="ml-2 text-xs px-2 py-0.5 bg-yellow-100 text-yellow-800 rounded-full">
                            Pending
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      <div className="text-sm font-bold text-gray-900">
                        {formatCurrency(room.totalBill)}
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      <div className="flex items-center gap-2">
                        <button
                          className="p-1.5 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="View Details"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          className="p-1.5 text-gray-600 hover:text-yellow-600 hover:bg-yellow-50 rounded-lg transition-colors"
                          title="Edit"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          className="p-1.5 text-gray-600 hover:text-purple-600 hover:bg-purple-50 rounded-lg transition-colors"
                          title="More options"
                        >
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
        
        {/* Footer Summary Row */}
        {filteredData.length > 0 && (
          <div className="bg-gray-50 border-t border-gray-200 px-6 py-4">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div>
                <div className="text-sm text-gray-600 mb-1">Total Today Charge</div>
                <div className="text-lg font-bold text-gray-900">{formatCurrency(totals.todaysCharge)}</div>
              </div>
              <div>
                <div className="text-sm text-gray-600 mb-1">Total Collected</div>
                <div className="text-lg font-bold text-green-600">{formatCurrency(totals.paymentReceived)}</div>
              </div>
              <div>
                <div className="text-sm text-gray-600 mb-1">Total Pending</div>
                <div className="text-lg font-bold text-yellow-600">{formatCurrency(totals.pendingAmount)}</div>
              </div>
              <div>
                <div className="text-sm text-gray-600 mb-1">Total Revenue</div>
                <div className="text-lg font-bold text-indigo-600">{formatCurrency(totals.totalBill)}</div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Empty State Illustration */}
      {filteredData.length === 0 && !loading && (
        <div className="text-center py-12">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
            <FileBarChart className="w-8 h-8 text-gray-400" />
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">No records found</h3>
          <p className="text-gray-600 max-w-md mx-auto mb-6">
            No daily sheet records found for the selected date and filters. Try changing the date or clear the filters.
          </p>
          <div className="flex gap-3 justify-center">
            <button
              onClick={() => handleQuickDate('today')}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Load Today's Data
            </button>
            <button
              onClick={() => {
                setFilters({
                  roomStatus: 'all',
                  paymentMode: 'all',
                  pendingOnly: false,
                  search: ''
                });
              }}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Clear All Filters
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default DailySheet;