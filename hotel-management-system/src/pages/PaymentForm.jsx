// src/pages/PaymentForm.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Save,
  Printer,
  Mail,
  Calendar,
  User,
  Home,
  CreditCard,
  Smartphone,
  Wallet,
  Building,
  Globe,
  Check,
  X,
  ChevronRight,
  DollarSign,
  Receipt,
  Search,
  Clock,
  Shield,
  QrCode,
  IndianRupee,
  FileText,
  AlertCircle,
  CheckCircle,
  XCircle,
  Banknote,
  Plus,
  Minus,
  Calculator
} from 'lucide-react';

const PaymentForm = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const invoiceId = queryParams.get('invoiceId');
  const bookingId = queryParams.get('bookingId');
  
  // Dummy data for invoices
  const [invoices, setInvoices] = useState([
    {
      id: 'INV001',
      totalAmount: 15000,
      paidAmount: 5000,
      balanceDue: 10000,
      bookingId: 'BK001',
      guestName: 'Rajesh Kumar',
      guestEmail: 'rajesh@example.com',
      guestPhone: '9876543210',
      roomNumber: '101',
      checkIn: '2024-01-15',
      checkOut: '2024-01-17',
      nights: 2,
      status: 'PARTIAL',
      items: [
        { name: 'Deluxe Room (2 nights)', amount: 12000 },
        { name: 'Tax (18%)', amount: 2160 },
        { name: 'Food & Beverages', amount: 840 }
      ]
    },
    {
      id: 'INV002',
      totalAmount: 12000,
      paidAmount: 12000,
      balanceDue: 0,
      bookingId: 'BK002',
      guestName: 'Priya Sharma',
      guestEmail: 'priya@example.com',
      guestPhone: '9876543211',
      roomNumber: '202',
      checkIn: '2024-01-16',
      checkOut: '2024-01-18',
      nights: 2,
      status: 'PAID',
      items: [
        { name: 'Standard Room (2 nights)', amount: 10000 },
        { name: 'Tax (18%)', amount: 1800 },
        { name: 'Laundry Service', amount: 200 }
      ]
    },
    {
      id: 'INV003',
      totalAmount: 20000,
      paidAmount: 8000,
      balanceDue: 12000,
      bookingId: 'BK003',
      guestName: 'Amit Patel',
      guestEmail: 'amit@example.com',
      guestPhone: '9876543212',
      roomNumber: '301',
      checkIn: '2024-01-14',
      checkOut: '2024-01-16',
      nights: 2,
      status: 'PARTIAL',
      items: [
        { name: 'Suite Room (2 nights)', amount: 18000 },
        { name: 'Tax (18%)', amount: 3240 },
        { name: 'Spa Service', amount: -1240 }
      ]
    },
    {
      id: 'INV004',
      totalAmount: 18000,
      paidAmount: 0,
      balanceDue: 18000,
      bookingId: 'BK004',
      guestName: 'Sneha Reddy',
      guestEmail: 'sneha@example.com',
      guestPhone: '9876543213',
      roomNumber: '102',
      checkIn: '2024-01-18',
      checkOut: '2024-01-20',
      nights: 2,
      status: 'PENDING',
      items: [
        { name: 'Deluxe Room (2 nights)', amount: 15000 },
        { name: 'Tax (18%)', amount: 2700 },
        { name: 'Mini Bar', amount: 300 }
      ]
    }
  ]);
  
  // State
  const [loading, setLoading] = useState(false);
  const [invoice, setInvoice] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [showSearch, setShowSearch] = useState(!invoiceId);
  const [qrCodeVisible, setQrCodeVisible] = useState(false);
  const [showInvoiceBreakdown, setShowInvoiceBreakdown] = useState(false);
  
  // Form data
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
    gatewayTxnId: '',
    cashDenomination: ''
  });
  
  // Errors
  const [errors, setErrors] = useState({});
  
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
  
  // Find invoice by ID
  const findInvoiceById = (id) => {
    return invoices.find(inv => inv.id === id);
  };
  
  // Initialize with invoice if invoiceId is provided
  useEffect(() => {
    if (invoiceId) {
      const foundInvoice = findInvoiceById(invoiceId);
      if (foundInvoice) {
        setInvoice(foundInvoice);
        setFormData(prev => ({
          ...prev,
          invoiceId,
          amount: foundInvoice.balanceDue
        }));
        setShowSearch(false);
      }
    }
  }, [invoiceId]);
  
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
        transactionId: '',
        cashDenomination: ''
      }));
    }
  };
  
  // Handle search
  const handleSearch = () => {
    if (!searchTerm.trim()) {
      setSearchResults([]);
      return;
    }
    
    const filtered = invoices.filter(item => 
      item.guestName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.roomNumber.includes(searchTerm) ||
      item.bookingId.toLowerCase().includes(searchTerm.toLowerCase())
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
    setSearchResults([]);
  };
  
  // Handle calculate cash
  const handleCalculateCash = () => {
    const denominations = {
      2000: 0, 500: 0, 200: 0, 100: 0, 50: 0, 20: 0, 10: 0, 5: 0, 2: 0, 1: 0
    };
    
    let amount = formData.amount;
    const keys = Object.keys(denominations).sort((a, b) => b - a);
    
    keys.forEach(denom => {
      const denomInt = parseInt(denom);
      if (amount >= denomInt) {
        denominations[denom] = Math.floor(amount / denomInt);
        amount = amount % denomInt;
      }
    });
    
    const cashBreakdown = Object.entries(denominations)
      .filter(([_, count]) => count > 0)
      .map(([denom, count]) => `${denom}x${count}`)
      .join(', ');
    
    handleInputChange('cashDenomination', cashBreakdown);
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
    
    // Simulate API call delay
    setTimeout(() => {
      setLoading(false);
      
      // Create payment record
      const paymentRecord = {
        id: `PAY${Date.now().toString().slice(-6)}`,
        ...formData,
        guestName: invoice?.guestName || 'Guest',
        roomNumber: invoice?.roomNumber || 'N/A',
        bookingId: invoice?.bookingId || 'N/A',
        timestamp: new Date().toISOString(),
        status: formData.amount === invoice?.balanceDue ? 'PAID' : 'PARTIAL'
      };
      
      console.log('Payment recorded:', paymentRecord);
      
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
            gatewayTxnId: '',
            cashDenomination: ''
          });
          setInvoice(null);
          setShowSearch(true);
          setErrors({});
          break;
          
        case 'save-view':
          alert('Payment recorded! Viewing receipt...');
          // In real app, this would open receipt in new tab
          break;
      }
    }, 1500);
  };
  
  // Handle cancel
  const handleCancel = () => {
    if (window.confirm('Are you sure? Any unsaved changes will be lost.')) {
      navigate('/payments');
    }
  };
  
  // Payment methods
  const paymentMethods = [
    { 
      id: 'CASH', 
      label: 'Cash', 
      icon: Banknote, 
      color: 'bg-green-100 text-green-800',
      description: 'Physical cash payment'
    },
    { 
      id: 'UPI', 
      label: 'UPI', 
      icon: Smartphone, 
      color: 'bg-blue-100 text-blue-800',
      description: 'UPI QR Code or ID'
    },
    { 
      id: 'CARD', 
      label: 'Card', 
      icon: CreditCard, 
      color: 'bg-purple-100 text-purple-800',
      description: 'Credit/Debit Card'
    },
    { 
      id: 'BANK', 
      label: 'Bank Transfer', 
      icon: Building, 
      color: 'bg-indigo-100 text-indigo-800',
      description: 'NEFT/RTGS/IMPS'
    },
    { 
      id: 'ONLINE', 
      label: 'Online Gateway', 
      icon: Globe, 
      color: 'bg-orange-100 text-orange-800',
      description: 'Razorpay/Stripe etc.'
    }
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
                    <div className="relative">
                      <QrCode className="w-32 h-32 text-gray-800" />
                      <div className="absolute inset-0 flex items-center justify-center">
                        <div className="w-16 h-16 bg-white rounded-lg flex items-center justify-center">
                          <IndianRupee className="w-8 h-8 text-green-600" />
                        </div>
                      </div>
                    </div>
                  </div>
                  <p className="text-sm text-gray-600 mb-2">Scan to pay via UPI</p>
                  <div className="text-lg font-bold text-gray-900">
                    {formatCurrency(formData.amount)}
                  </div>
                  <div className="text-xs text-gray-500 mt-1">
                    Hotel Payment • Invoice #{invoice?.id}
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
                <Banknote className="w-5 h-5 text-green-600" />
                <span className="text-sm font-medium text-green-800">Cash Payment Instructions</span>
              </div>
              <ul className="text-xs text-green-700 space-y-1">
                <li>• Verify cash amount carefully</li>
                <li>• Check for counterfeit notes</li>
                <li>• Issue receipt immediately</li>
                <li>• Deposit cash in safe/locker</li>
                <li>• Update cash register tally</li>
              </ul>
            </div>
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="block text-sm font-medium text-gray-700">
                  Cash Denomination (Optional)
                </label>
                <button
                  type="button"
                  onClick={handleCalculateCash}
                  className="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800"
                >
                  <Calculator className="w-4 h-4" />
                  Calculate
                </button>
              </div>
              <textarea
                value={formData.cashDenomination}
                onChange={(e) => handleInputChange('cashDenomination', e.target.value)}
                rows="2"
                placeholder="e.g., 2000x5, 500x2, 100x10"
                className="block w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
        );
    }
  };
  
  // Get status badge for invoice
  const getInvoiceStatusBadge = (status) => {
    const config = {
      PAID: { color: 'bg-green-100 text-green-800 border-green-200', icon: CheckCircle },
      PARTIAL: { color: 'bg-yellow-100 text-yellow-800 border-yellow-200', icon: AlertCircle },
      PENDING: { color: 'bg-red-100 text-red-800 border-red-200', icon: XCircle }
    };
    
    const StatusConfig = config[status] || config.PENDING;
    const Icon = StatusConfig.icon;
    
    return (
      <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border ${StatusConfig.color}`}>
        <Icon className="w-3 h-3 mr-1" />
        {status}
      </span>
    );
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
                                Invoice #{item.id} • Room {item.roomNumber} • Booking #{item.bookingId}
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
                              <div className="mt-1">
                                {getInvoiceStatusBadge(item.status)}
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
                  <div className="flex gap-2">
                    <button
                      onClick={() => setShowInvoiceBreakdown(!showInvoiceBreakdown)}
                      className="text-sm text-blue-600 hover:text-blue-800"
                    >
                      {showInvoiceBreakdown ? 'Hide Details' : 'View Details'}
                    </button>
                    <button
                      onClick={() => setShowSearch(true)}
                      className="text-sm text-gray-600 hover:text-gray-800"
                    >
                      Change
                    </button>
                  </div>
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
                
                {/* Invoice Breakdown */}
                {showInvoiceBreakdown && (
                  <div className="mt-6 pt-6 border-t border-gray-200">
                    <h3 className="text-sm font-medium text-gray-700 mb-3">Invoice Breakdown</h3>
                    <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                      <div className="space-y-2">
                        {invoice.items.map((item, index) => (
                          <div key={index} className="flex justify-between text-sm">
                            <span className="text-gray-600">{item.name}</span>
                            <span className={`font-medium ${item.amount >= 0 ? 'text-gray-900' : 'text-red-600'}`}>
                              {formatCurrency(item.amount)}
                            </span>
                          </div>
                        ))}
                        <div className="pt-2 border-t border-gray-200">
                          <div className="flex justify-between font-medium">
                            <span className="text-gray-900">Total Amount</span>
                            <span className="text-gray-900">{formatCurrency(invoice.totalAmount)}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
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
                      <div className="absolute inset-y-0 right-0 pr-3 flex items-center gap-2">
                        <button
                          type="button"
                          onClick={() => handleInputChange('amount', Math.max(0, formData.amount - 100))}
                          className="p-1 text-gray-400 hover:text-gray-600"
                        >
                          <Minus className="w-4 h-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => handleInputChange('amount', invoice.balanceDue)}
                          className="text-sm text-blue-600 hover:text-blue-800"
                        >
                          Pay Full
                        </button>
                        <button
                          type="button"
                          onClick={() => handleInputChange('amount', Math.min(invoice.balanceDue, formData.amount + 100))}
                          className="p-1 text-gray-400 hover:text-gray-600"
                        >
                          <Plus className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                    {errors.amount && (
                      <p className="mt-1 text-sm text-red-600">{errors.amount}</p>
                    )}
                    <div className="mt-2 text-sm text-gray-500">
                      Balance after payment: <span className={`font-medium ${
                        (invoice.balanceDue - formData.amount) > 0 ? 'text-red-600' : 'text-green-600'
                      }`}>
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
                      <div className="pt-2 border-t border-blue-200">
                        <div className="text-xs text-blue-600">
                          Invoice status will be updated to: <span className="font-medium">
                            {(invoice.balanceDue - formData.amount) === 0 ? 'PAID' : 'PARTIAL'}
                          </span>
                        </div>
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
                Invoice #{invoice.id} • Balance Due: {formatCurrency(invoice.balanceDue)}
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
                
                {invoice?.guestEmail && (
                  <button
                    onClick={() => handleSubmit('save-email')}
                    disabled={loading || !invoice}
                    className="flex items-center gap-2 px-6 py-2.5 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    <Mail className="w-4 h-4" />
                    Save & Email
                  </button>
                )}
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
              <li key={key}>• {error}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default PaymentForm;