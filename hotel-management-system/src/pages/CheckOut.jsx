import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Search, 
  DollarSign, 
  CheckCircle, 
  AlertCircle, 
  User, 
  Phone, 
  Calendar,
  Home,
  CreditCard,
  Mail,
  X,
  Check,
  FileText,
  Shield,
  Send,
  Filter,
  Grid,
  List,
  ChevronDown,
  Building,
  Clock,
  CreditCard as CardIcon,
  Smartphone,
  Wallet
} from 'lucide-react';

const CheckOut = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [showCheckoutModal, setShowCheckoutModal] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [selectedGuest, setSelectedGuest] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('cash');
  const [discountAmount, setDiscountAmount] = useState(0);
  const [discountType, setDiscountType] = useState('percentage');
  const [confirmationOptions, setConfirmationOptions] = useState({
    markVacant: true,
    triggerHousekeeping: true,
    generateInvoice: true,
    sendInvoice: true
  });

  // View mode states
  const [viewMode, setViewMode] = useState('table'); // 'table' or 'card'
  const [activeTab, setActiveTab] = useState('all'); // 'all', 'pending', 'completed', 'balanced'
  
  // Filter states
  const [showFilters, setShowFilters] = useState(false);
  const [roomTypeFilter, setRoomTypeFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [paymentFilter, setPaymentFilter] = useState('all');
  const [dateRange, setDateRange] = useState({
    checkIn: '',
    checkOut: ''
  });
  const [balanceFilter, setBalanceFilter] = useState('all');

  // API: GET /api/checkouts/today - Get today's checkouts
  // API: POST /api/checkouts - Process checkout
  // API: GET /api/checkouts/{guestId}/bill - Get guest final bill
  // API: POST /api/checkouts/{guestId}/payment - Process final payment

  const stats = [
    { label: 'Total Departures Today', value: 5 },
    { label: 'Pending Payments', value: 2 },
    { label: 'Completed Check-outs', value: 3 },
  ];

  const departures = [
    {
      id: 1,
      guestName: 'Emily Carter',
      roomNo: '401',
      roomType: 'Deluxe Suite',
      checkIn: '2024-10-26',
      checkInTime: '14:30',
      checkOut: '2024-10-29',
      checkOutTime: '11:00',
      nights: 3,
      adults: 2,
      children: 1,
      roomCharges: 540.00,
      services: 125.50,
      taxes: 66.55,
      totalAmount: 732.05,
      amountPaid: 732.05,
      balance: 0.00,
      status: 'Balanced',
      email: 'emily.carter@email.com',
      phone: '+1 (555) 111-2222',
      idType: 'Passport',
      idNumber: 'AB123456',
      nationality: 'United States',
      paymentMethod: 'card',
      checkedOutAt: '2024-10-29 11:15'
    },
    {
      id: 2,
      guestName: 'David Rodriguez',
      roomNo: '215',
      roomType: 'Standard Room',
      checkIn: '2024-10-27',
      checkInTime: '15:45',
      checkOut: '2024-10-29',
      checkOutTime: '11:00',
      nights: 2,
      adults: 1,
      children: 0,
      roomCharges: 360.00,
      services: 85.00,
      taxes: 44.50,
      totalAmount: 489.50,
      amountPaid: 400.00,
      balance: 89.50,
      status: 'Pending Payment',
      email: 'david.rodriguez@email.com',
      phone: '+1 (555) 333-4444',
      idType: 'Driver License',
      idNumber: 'DL789012',
      nationality: 'Spain',
      paymentMethod: 'cash',
      checkedOutAt: null
    },
    {
      id: 3,
      guestName: 'Jessica Chen',
      roomNo: '602',
      roomType: 'Executive Suite',
      checkIn: '2024-10-24',
      checkInTime: '12:15',
      checkOut: '2024-10-29',
      checkOutTime: '11:00',
      nights: 5,
      adults: 2,
      children: 0,
      roomCharges: 900.00,
      services: 210.00,
      taxes: 111.00,
      totalAmount: 1221.00,
      amountPaid: 1221.00,
      balance: 0.00,
      status: 'Checked Out',
      email: 'jessica.chen@email.com',
      phone: '+1 (555) 555-6666',
      idType: 'Passport',
      idNumber: 'CD456789',
      nationality: 'China',
      paymentMethod: 'upi',
      checkedOutAt: '2024-10-29 10:45'
    },
    {
      id: 4,
      guestName: 'Mark Thompson',
      roomNo: '308',
      roomType: 'Standard Room',
      checkIn: '2024-10-28',
      checkInTime: '19:30',
      checkOut: '2024-10-29',
      checkOutTime: '11:00',
      nights: 1,
      adults: 1,
      children: 0,
      roomCharges: 180.00,
      services: 45.00,
      taxes: 22.50,
      totalAmount: 247.50,
      amountPaid: 150.00,
      balance: 97.50,
      status: 'Pending Payment',
      email: 'mark.thompson@email.com',
      phone: '+1 (555) 777-8888',
      idType: 'ID Card',
      idNumber: 'ID345678',
      nationality: 'United Kingdom',
      paymentMethod: 'cash',
      checkedOutAt: null
    },
    {
      id: 5,
      guestName: 'Lisa Anderson',
      roomNo: '510',
      roomType: 'Deluxe Room',
      checkIn: '2024-10-25',
      checkInTime: '16:20',
      checkOut: '2024-10-29',
      checkOutTime: '11:00',
      nights: 4,
      adults: 2,
      children: 2,
      roomCharges: 720.00,
      services: 165.00,
      taxes: 88.50,
      totalAmount: 973.50,
      amountPaid: 973.50,
      balance: 0.00,
      status: 'Checked Out',
      email: 'lisa.anderson@email.com',
      phone: '+1 (555) 999-0000',
      idType: 'Passport',
      idNumber: 'EF901234',
      nationality: 'Canada',
      paymentMethod: 'card',
      checkedOutAt: '2024-10-29 11:30'
    },
  ];

  const additionalCharges = [
    { description: 'Room Service (2x)', amount: 45.00 },
    { description: 'Spa Treatment', amount: 80.50 },
    { description: 'Laundry Service', amount: 35.00 },
    { description: 'Mini Bar', amount: 65.00 },
  ];

  const getStatusColor = (status) => {
    switch (status) {
      case 'Checked Out':
        return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400';
      case 'Balanced':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400';
      case 'Pending Payment':
        return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400';
    }
  };

  const getPaymentMethodIcon = (method) => {
    switch (method) {
      case 'card':
        return <CardIcon size={16} className="text-blue-500" />;
      case 'cash':
        return <Wallet size={16} className="text-green-500" />;
      case 'upi':
        return <Smartphone size={16} className="text-purple-500" />;
      default:
        return <CreditCard size={16} className="text-gray-500" />;
    }
  };

  const handleCheckOut = (guestId) => {
    const guest = departures.find(d => d.id === guestId);
    setSelectedGuest(guest);
    
    if (guest.balance > 0) {
      setShowPaymentModal(true);
    } else {
      setShowCheckoutModal(true);
    }
  };

  const handleProcessPayment = () => {
    // API: POST /api/checkouts/{guestId}/payment
    alert(`Payment processed for ${selectedGuest.guestName}`);
    setShowPaymentModal(false);
    setShowCheckoutModal(true);
  };

  const handleCompleteCheckout = () => {
    // API: POST /api/checkouts
    alert(`Checkout completed for ${selectedGuest.guestName}`);
    setShowCheckoutModal(false);
  };

  const handlePaymentMethodChange = (method) => {
    setPaymentMethod(method);
  };

  const handleDiscountChange = (value) => {
    setDiscountAmount(value);
  };

  const handleConfirmationOptionChange = (option) => {
    setConfirmationOptions(prev => ({
      ...prev,
      [option]: !prev[option]
    }));
  };

  const calculateTotal = () => {
    if (!selectedGuest) return 0;
    
    const subtotal = selectedGuest.roomCharges + selectedGuest.services;
    let discount = discountAmount;
    
    if (discountType === 'percentage' && discount > 0) {
      discount = (subtotal * discountAmount) / 100;
    }
    
    const tax = selectedGuest.taxes || 0;
    return subtotal + tax - discount;
  };

  const filterDepartures = (departures) => {
    return departures.filter(departure => {
      // Search filter
      const matchesSearch = 
        departure.guestName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        departure.roomNo.includes(searchQuery) ||
        departure.email.toLowerCase().includes(searchQuery.toLowerCase());

      // Tab filter
      let matchesTab = true;
      switch (activeTab) {
        case 'pending':
          matchesTab = departure.status === 'Pending Payment';
          break;
        case 'completed':
          matchesTab = departure.status === 'Checked Out';
          break;
        case 'balanced':
          matchesTab = departure.status === 'Balanced';
          break;
        default:
          matchesTab = true;
      }

      // Room type filter
      const matchesRoomType = roomTypeFilter === 'all' || departure.roomType === roomTypeFilter;

      // Status filter
      const matchesStatus = statusFilter === 'all' || departure.status === statusFilter;

      // Payment filter
      const matchesPayment = paymentFilter === 'all' || departure.paymentMethod === paymentFilter;

      // Balance filter
      let matchesBalance = true;
      switch (balanceFilter) {
        case 'with-balance':
          matchesBalance = departure.balance > 0;
          break;
        case 'without-balance':
          matchesBalance = departure.balance === 0;
          break;
        case 'high-balance':
          matchesBalance = departure.balance > 50;
          break;
        default:
          matchesBalance = true;
      }

      // Date range filter
      let matchesDateRange = true;
      if (dateRange.checkIn) {
        matchesDateRange = matchesDateRange && departure.checkIn >= dateRange.checkIn;
      }
      if (dateRange.checkOut) {
        matchesDateRange = matchesDateRange && departure.checkOut <= dateRange.checkOut;
      }

      return matchesSearch && matchesTab && matchesRoomType && matchesStatus && matchesPayment && matchesBalance && matchesDateRange;
    });
  };

  const filteredDepartures = filterDepartures(departures);

  // Get unique room types for filter dropdown
  const roomTypes = ['all', ...new Set(departures.map(d => d.roomType))];
  const statuses = ['all', 'Checked Out', 'Pending Payment', 'Balanced'];
  const paymentMethods = ['all', 'cash', 'card', 'upi'];

  const CheckoutModal = () => {
    if (!selectedGuest) return null;

    const finalTotal = calculateTotal();

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-6xl max-h-[90vh] overflow-hidden">
          <div className="flex items-center justify-between p-6 border-b dark:border-gray-700">
            <div>
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Guest Checkout</h2>
              <p className="text-gray-600 dark:text-gray-400 mt-1">Finalize stay and generate invoice</p>
            </div>
            <div className="flex items-center gap-4">
              <span className="px-3 py-1 bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400 rounded-full text-sm font-medium">
                Checked-In
              </span>
              <button
                onClick={() => setShowCheckoutModal(false)}
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
              >
                <X size={24} />
              </button>
            </div>
          </div>

          <div className="flex flex-col lg:flex-row h-[calc(90vh-8rem)]">
            <div className="lg:w-2/3 overflow-y-auto p-6">
              <div className="space-y-6">
                <div className="card">
                  <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                    <User size={20} />
                    Guest Information
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Full Name</p>
                      <p className="font-medium">{selectedGuest.guestName}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Phone</p>
                      <p className="font-medium flex items-center gap-2">
                        <Phone size={16} />
                        {selectedGuest.phone}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">ID Type & Number</p>
                      <p className="font-medium">{selectedGuest.idType} • {selectedGuest.idNumber}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Nationality</p>
                      <p className="font-medium">{selectedGuest.nationality}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Email</p>
                      <p className="font-medium flex items-center gap-2">
                        <Mail size={16} />
                        {selectedGuest.email}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="card">
                  <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                    <Calendar size={20} />
                    Stay Details
                  </h3>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Room No.</p>
                      <p className="font-medium text-lg">{selectedGuest.roomNo}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Room Type</p>
                      <p className="font-medium">{selectedGuest.roomType}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Check-in</p>
                      <p className="font-medium">{selectedGuest.checkIn} {selectedGuest.checkInTime}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Check-out</p>
                      <p className="font-medium">{selectedGuest.checkOut} {selectedGuest.checkOutTime}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Nights Stayed</p>
                      <p className="font-medium text-lg">{selectedGuest.nights}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Guests</p>
                      <p className="font-medium">{selectedGuest.adults} Adults, {selectedGuest.children} Children</p>
                    </div>
                  </div>
                </div>

                <div className="card">
                  <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                    <DollarSign size={20} />
                    Charges Breakdown
                  </h3>
                  <div className="space-y-3">
                    <div className="flex justify-between items-center">
                      <span>Room Charges ({selectedGuest.nights} nights)</span>
                      <span className="font-semibold">${selectedGuest.roomCharges.toFixed(2)}</span>
                    </div>
                    {additionalCharges.map((charge, index) => (
                      <div key={index} className="flex justify-between items-center">
                        <span className="text-gray-600 dark:text-gray-400">{charge.description}</span>
                        <span>${charge.amount.toFixed(2)}</span>
                      </div>
                    ))}
                    <div className="flex justify-between items-center pt-3 border-t dark:border-gray-700">
                      <span className="font-medium">Subtotal</span>
                      <span className="font-semibold">${(selectedGuest.roomCharges + selectedGuest.services).toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between items-center text-red-600">
                      <span>Taxes (10%)</span>
                      <span>${selectedGuest.taxes.toFixed(2)}</span>
                    </div>
                  </div>
                </div>

                <div className="card">
                  <h3 className="text-lg font-semibold mb-4">Discounts & Adjustments</h3>
                  <div className="space-y-4">
                    <div className="flex gap-4">
                      <div className="flex-1">
                        <label className="block text-sm font-medium mb-2">Discount Type</label>
                        <select
                          value={discountType}
                          onChange={(e) => setDiscountType(e.target.value)}
                          className="input-field"
                        >
                          <option value="percentage">Percentage (%)</option>
                          <option value="flat">Flat Amount ($)</option>
                        </select>
                      </div>
                      <div className="flex-1">
                        <label className="block text-sm font-medium mb-2">Discount Value</label>
                        <input
                          type="number"
                          value={discountAmount}
                          onChange={(e) => handleDiscountChange(parseFloat(e.target.value) || 0)}
                          className="input-field"
                          placeholder="0"
                          min="0"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Reason (Optional)</label>
                      <input
                        type="text"
                        className="input-field"
                        placeholder="Special offer, loyalty discount, etc."
                      />
                    </div>
                  </div>
                </div>

                <div className="card">
                  <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                    <Shield size={20} />
                    Checkout Actions
                  </h3>
                  <div className="space-y-3">
                    {Object.entries(confirmationOptions).map(([key, value]) => (
                      <label key={key} className="flex items-center gap-3 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={value}
                          onChange={() => handleConfirmationOptionChange(key)}
                          className="rounded border-gray-300 dark:border-gray-600"
                        />
                        <span className="text-sm">
                          {key === 'markVacant' && 'Mark room as vacant'}
                          {key === 'triggerHousekeeping' && 'Trigger housekeeping'}
                          {key === 'generateInvoice' && 'Generate invoice'}
                          {key === 'sendInvoice' && 'Send invoice via email'}
                        </span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            <div className="lg:w-1/3 border-l dark:border-gray-700 lg:sticky lg:top-0 h-full">
              <div className="p-6 h-full flex flex-col">
                <h3 className="text-xl font-bold mb-6">Bill Summary</h3>
                
                <div className="mb-6">
                  <div className="flex items-center gap-3 mb-2">
                    <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center">
                      <User size={20} className="text-blue-600 dark:text-blue-400" />
                    </div>
                    <div>
                      <p className="font-semibold">{selectedGuest.guestName}</p>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Room {selectedGuest.roomNo}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                    <Calendar size={16} />
                    <span>{selectedGuest.nights} nights • {selectedGuest.checkIn} to {selectedGuest.checkOut}</span>
                  </div>
                </div>

                <div className="space-y-3 mb-6">
                  <div className="flex justify-between">
                    <span className="text-gray-600 dark:text-gray-400">Room Charges</span>
                    <span>${selectedGuest.roomCharges.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600 dark:text-gray-400">Additional Services</span>
                    <span>${selectedGuest.services.toFixed(2)}</span>
                  </div>
                  {discountAmount > 0 && (
                    <div className="flex justify-between text-green-600">
                      <span>Discount</span>
                      <span>-${discountType === 'percentage' ? 
                        ((selectedGuest.roomCharges + selectedGuest.services) * discountAmount / 100).toFixed(2) : 
                        discountAmount.toFixed(2)}
                      </span>
                    </div>
                  )}
                  <div className="flex justify-between text-red-600">
                    <span>Taxes</span>
                    <span>${selectedGuest.taxes.toFixed(2)}</span>
                  </div>
                  <div className="pt-3 border-t dark:border-gray-700">
                    <div className="flex justify-between items-center">
                      <span className="font-semibold">Total Amount</span>
                      <span className="text-2xl font-bold">${finalTotal.toFixed(2)}</span>
                    </div>
                  </div>
                </div>

                <div className="mb-6">
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-gray-600 dark:text-gray-400">Advance Paid</span>
                    <span className="font-semibold">${selectedGuest.amountPaid.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-600 dark:text-gray-400">Balance Due</span>
                    <span className={`text-lg font-bold ${
                      selectedGuest.balance > 0 ? 'text-red-600' : 'text-green-600'
                    }`}>
                      ${selectedGuest.balance.toFixed(2)}
                    </span>
                  </div>
                </div>

                <button
                  onClick={handleCompleteCheckout}
                  className="w-full bg-green-600 hover:bg-green-700 text-white py-3 px-4 rounded-lg font-semibold flex items-center justify-center gap-2"
                >
                  <Check size={20} />
                  Complete Checkout
                </button>

                <div className="mt-4 flex gap-2">
                  <button className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded-lg text-sm font-medium flex items-center justify-center gap-2">
                    <FileText size={16} />
                    Preview Invoice
                  </button>
                  <button className="flex-1 bg-purple-600 hover:bg-purple-700 text-white py-2 px-4 rounded-lg text-sm font-medium flex items-center justify-center gap-2">
                    <Send size={16} />
                    Send Invoice
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const PaymentModal = () => {
    if (!selectedGuest) return null;

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-md">
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white">Process Payment</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-1">Collect pending balance</p>
              </div>
              <button
                onClick={() => setShowPaymentModal(false)}
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
              >
                <X size={24} />
              </button>
            </div>

            <div className="mb-6">
              <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-gray-600 dark:text-gray-400">Guest</span>
                  <span className="font-semibold">{selectedGuest.guestName}</span>
                </div>
                <div className="flex justify-between items-center mb-2">
                  <span className="text-gray-600 dark:text-gray-400">Room</span>
                  <span className="font-semibold">{selectedGuest.roomNo}</span>
                </div>
                <div className="flex justify-between items-center pt-3 border-t dark:border-gray-600">
                  <span className="text-lg font-semibold">Balance Due</span>
                  <span className="text-2xl font-bold text-red-600">${selectedGuest.balance.toFixed(2)}</span>
                </div>
              </div>
            </div>

            <div className="mb-6">
              <h4 className="font-semibold mb-3">Payment Method</h4>
              <div className="grid grid-cols-2 gap-3">
                {['cash', 'card', 'upi', 'split'].map((method) => (
                  <button
                    key={method}
                    onClick={() => handlePaymentMethodChange(method)}
                    className={`p-4 rounded-lg border-2 flex flex-col items-center gap-2 ${
                      paymentMethod === method
                        ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                        : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
                    }`}
                  >
                    {getPaymentMethodIcon(method)}
                    <span className="text-sm font-medium capitalize">{method}</span>
                  </button>
                ))}
              </div>
            </div>

            {paymentMethod === 'card' && (
              <div className="space-y-4 mb-6">
                <div>
                  <label className="block text-sm font-medium mb-2">Card Number</label>
                  <input type="text" className="input-field" placeholder="1234 5678 9012 3456" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-2">Expiry Date</label>
                    <input type="text" className="input-field" placeholder="MM/YY" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">CVV</label>
                    <input type="text" className="input-field" placeholder="123" />
                  </div>
                </div>
              </div>
            )}

            {paymentMethod === 'upi' && (
              <div className="mb-6">
                <label className="block text-sm font-medium mb-2">UPI ID</label>
                <input type="text" className="input-field" placeholder="username@bank" />
              </div>
            )}

            <div className="mb-6">
              <label className="block text-sm font-medium mb-2">Reference Number (Optional)</label>
              <input type="text" className="input-field" placeholder="Enter reference number" />
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setShowPaymentModal(false)}
                className="flex-1 bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 py-3 rounded-lg font-medium"
              >
                Cancel
              </button>
              <button
                onClick={handleProcessPayment}
                className="flex-1 bg-green-600 hover:bg-green-700 text-white py-3 rounded-lg font-semibold"
              >
                Process Payment (${selectedGuest.balance.toFixed(2)})
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // Card View Component
  const CardView = () => (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {filteredDepartures.map((departure) => (
        <div key={departure.id} className="card hover:shadow-lg transition-shadow duration-300">
          <div className="p-5">
            {/* Card Header */}
            <div className="flex justify-between items-start mb-4">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <User size={18} className="text-gray-500" />
                  <h3 className="font-bold text-lg">{departure.guestName}</h3>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                  <Building size={14} />
                  <span>Room {departure.roomNo} • {departure.roomType}</span>
                </div>
              </div>
              <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(departure.status)}`}>
                {departure.status}
              </span>
            </div>

            {/* Stay Info */}
            <div className="grid grid-cols-2 gap-3 mb-4 p-3 bg-gray-50 dark:bg-gray-700/30 rounded-lg">
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Check-in</p>
                <p className="font-medium text-sm">{departure.checkIn}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Check-out</p>
                <p className="font-medium text-sm">{departure.checkOut}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Nights</p>
                <p className="font-medium text-sm">{departure.nights}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Guests</p>
                <p className="font-medium text-sm">{departure.adults}A, {departure.children}C</p>
              </div>
            </div>

            {/* Financial Info */}
            <div className="space-y-2 mb-4">
              <div className="flex justify-between items-center">
                <span className="text-gray-600 dark:text-gray-400">Total Amount</span>
                <span className="font-bold text-lg">${departure.totalAmount.toFixed(2)}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600 dark:text-gray-400">Paid</span>
                <span className="font-semibold">${departure.amountPaid.toFixed(2)}</span>
              </div>
              <div className="flex justify-between items-center pt-2 border-t dark:border-gray-700">
                <span className="font-semibold">Balance</span>
                <span className={`font-bold text-lg ${departure.balance > 0 ? 'text-red-600' : 'text-green-600'}`}>
                  ${departure.balance.toFixed(2)}
                </span>
              </div>
            </div>

            {/* Payment Method */}
            <div className="flex items-center gap-2 mb-4 p-2 bg-gray-50 dark:bg-gray-700/30 rounded">
              {getPaymentMethodIcon(departure.paymentMethod)}
              <span className="text-sm capitalize">{departure.paymentMethod}</span>
              {departure.checkedOutAt && (
                <span className="ml-auto text-xs text-gray-500 flex items-center gap-1">
                  <Clock size={12} />
                  {departure.checkedOutAt.split(' ')[1]}
                </span>
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex gap-2">
              {departure.status !== 'Checked Out' && (
                <>
                  {departure.balance > 0 && (
                    <button
                      onClick={() => {
                        setSelectedGuest(departure);
                        setShowPaymentModal(true);
                      }}
                      className="flex-1 bg-yellow-600 hover:bg-yellow-700 text-white py-2 rounded-lg text-sm font-medium"
                    >
                      Pay Balance
                    </button>
                  )}
                  <button
                    onClick={() => handleCheckOut(departure.id)}
                    className="flex-1 bg-green-600 hover:bg-green-700 text-white py-2 rounded-lg text-sm font-medium"
                  >
                    Check Out
                  </button>
                </>
              )}
              <button
                onClick={() => navigate(`/guest-detail/${departure.id}`)}
                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg text-sm font-medium"
              >
                Details
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );

  // Table View Component
  const TableView = () => (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead className="bg-gray-50 dark:bg-gray-700/50">
          <tr>
            <th className="px-4 py-3 text-left text-sm font-semibold">Guest Name</th>
            <th className="px-4 py-3 text-left text-sm font-semibold">Room No.</th>
            <th className="px-4 py-3 text-left text-sm font-semibold">Check-In</th>
            <th className="px-4 py-3 text-left text-sm font-semibold">Check-Out</th>
            <th className="px-4 py-3 text-left text-sm font-semibold">Nights</th>
            <th className="px-4 py-3 text-left text-sm font-semibold">Total Amount</th>
            <th className="px-4 py-3 text-left text-sm font-semibold">Balance</th>
            <th className="px-4 py-3 text-left text-sm font-semibold">Status</th>
            <th className="px-4 py-3 text-left text-sm font-semibold">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
          {filteredDepartures.map((departure) => (
            <tr key={departure.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
              <td className="px-4 py-3">
                <div>
                  <p className="font-medium">{departure.guestName}</p>
                  <p className="text-xs text-gray-600 dark:text-gray-400">{departure.email}</p>
                </div>
              </td>
              <td className="px-4 py-3 text-sm font-semibold">{departure.roomNo}</td>
              <td className="px-4 py-3 text-sm">{departure.checkIn}</td>
              <td className="px-4 py-3 text-sm">{departure.checkOut}</td>
              <td className="px-4 py-3 text-sm">{departure.nights}</td>
              <td className="px-4 py-3 text-sm font-semibold">${departure.totalAmount.toFixed(2)}</td>
              <td className="px-4 py-3">
                <span className={`font-semibold ${
                  departure.balance > 0 
                    ? 'text-red-600' 
                    : 'text-green-600'
                }`}>
                  ${departure.balance.toFixed(2)}
                </span>
              </td>
              <td className="px-4 py-3">
                <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(departure.status)}`}>
                  {departure.status}
                </span>
              </td>
              <td className="px-4 py-3">
                <div className="flex gap-2">
                  {departure.status !== 'Checked Out' && (
                    <>
                      {departure.balance > 0 && (
                        <button
                          onClick={() => {
                            setSelectedGuest(departure);
                            setShowPaymentModal(true);
                          }}
                          className="px-3 py-1 bg-yellow-600 hover:bg-yellow-700 text-white rounded-lg text-sm font-medium"
                        >
                          Pay
                        </button>
                      )}
                      <button
                        onClick={() => handleCheckOut(departure.id)}
                        className="px-3 py-1 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm font-medium"
                      >
                        Check Out
                      </button>
                    </>
                  )}
                  <button
                    onClick={() => navigate(`/guest-detail/${departure.id}`)}
                    className="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium"
                  >
                    View
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  const clearAllFilters = () => {
    setRoomTypeFilter('all');
    setStatusFilter('all');
    setPaymentFilter('all');
    setBalanceFilter('all');
    setDateRange({ checkIn: '', checkOut: '' });
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Check-Out Operations</h1>
        <p className="text-gray-600 dark:text-gray-400 mt-1">Overview and actions for all guest departures</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {stats.map((stat, index) => (
          <div key={index} className="card">
            <p className="text-sm text-gray-600 dark:text-gray-400">{stat.label}</p>
            <p className="text-3xl font-bold mt-2">{stat.value}</p>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div className="card">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div className="flex space-x-1">
            {[
              { id: 'all', label: 'All Departures', count: departures.length },
              { id: 'pending', label: 'Pending Payment', count: departures.filter(d => d.status === 'Pending Payment').length },
              { id: 'completed', label: 'Checked Out', count: departures.filter(d => d.status === 'Checked Out').length },
              { id: 'balanced', label: 'Balanced', count: departures.filter(d => d.status === 'Balanced').length }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-4 py-2 rounded-lg font-medium text-sm transition-colors ${
                  activeTab === tab.id
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'
                }`}
              >
                {tab.label}
                <span className="ml-2 bg-gray-200 dark:bg-gray-700 px-2 py-0.5 rounded-full text-xs">
                  {tab.count}
                </span>
              </button>
            ))}
          </div>

          {/* View Mode Toggle */}
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2 bg-gray-100 dark:bg-gray-700 rounded-lg p-1">
              <button
                onClick={() => setViewMode('table')}
                className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                  viewMode === 'table'
                    ? 'bg-white dark:bg-gray-800 shadow'
                    : 'text-gray-600 dark:text-gray-400'
                }`}
              >
                <List size={18} />
              </button>
              <button
                onClick={() => setViewMode('card')}
                className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                  viewMode === 'card'
                    ? 'bg-white dark:bg-gray-800 shadow'
                    : 'text-gray-600 dark:text-gray-400'
                }`}
              >
                <Grid size={18} />
              </button>
            </div>

            {/* Filter Toggle */}
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="px-4 py-2 bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 rounded-lg font-medium text-sm flex items-center gap-2"
            >
              <Filter size={18} />
              Filters
              {showFilters && <ChevronDown size={18} className="transform rotate-180" />}
            </button>
          </div>
        </div>

        {/* Advanced Filters Panel */}
        {showFilters && (
          <div className="mt-6 p-4 border border-gray-200 dark:border-gray-700 rounded-lg">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-semibold text-lg">Advanced Filters</h3>
              <button
                onClick={clearAllFilters}
                className="text-sm text-red-600 hover:text-red-700"
              >
                Clear All Filters
              </button>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              {/* Room Type Filter */}
              <div>
                <label className="block text-sm font-medium mb-2">Room Type</label>
                <select
                  value={roomTypeFilter}
                  onChange={(e) => setRoomTypeFilter(e.target.value)}
                  className="input-field"
                >
                  {roomTypes.map((type) => (
                    <option key={type} value={type}>
                      {type === 'all' ? 'All Room Types' : type}
                    </option>
                  ))}
                </select>
              </div>

              {/* Status Filter */}
              <div>
                <label className="block text-sm font-medium mb-2">Status</label>
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className="input-field"
                >
                  {statuses.map((status) => (
                    <option key={status} value={status}>
                      {status === 'all' ? 'All Statuses' : status}
                    </option>
                  ))}
                </select>
              </div>

              {/* Payment Method Filter */}
              <div>
                <label className="block text-sm font-medium mb-2">Payment Method</label>
                <select
                  value={paymentFilter}
                  onChange={(e) => setPaymentFilter(e.target.value)}
                  className="input-field"
                >
                  {paymentMethods.map((method) => (
                    <option key={method} value={method}>
                      {method === 'all' ? 'All Methods' : method.toUpperCase()}
                    </option>
                  ))}
                </select>
              </div>

              {/* Balance Filter */}
              <div>
                <label className="block text-sm font-medium mb-2">Balance</label>
                <select
                  value={balanceFilter}
                  onChange={(e) => setBalanceFilter(e.target.value)}
                  className="input-field"
                >
                  <option value="all">All Balances</option>
                  <option value="with-balance">With Balance</option>
                  <option value="without-balance">Without Balance</option>
                  <option value="high-balance">High Balance (&gt;$50)</option>
                </select>
              </div>

              {/* Date Range Filters */}
              <div>
                <label className="block text-sm font-medium mb-2">Check-in From</label>
                <input
                  type="date"
                  value={dateRange.checkIn}
                  onChange={(e) => setDateRange(prev => ({ ...prev, checkIn: e.target.value }))}
                  className="input-field"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">Check-out To</label>
                <input
                  type="date"
                  value={dateRange.checkOut}
                  onChange={(e) => setDateRange(prev => ({ ...prev, checkOut: e.target.value }))}
                  className="input-field"
                />
              </div>

              {/* Search Filter */}
              <div className="lg:col-span-2">
                <label className="block text-sm font-medium mb-2">Search</label>
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                  <input
                    type="text"
                    placeholder="Search by guest name, room number, or email..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="input-field pl-10"
                  />
                </div>
              </div>
            </div>

            {/* Active Filters */}
            <div className="mt-4 flex flex-wrap gap-2">
              {roomTypeFilter !== 'all' && (
                <span className="px-3 py-1 bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400 rounded-full text-xs">
                  Room Type: {roomTypeFilter}
                </span>
              )}
              {statusFilter !== 'all' && (
                <span className="px-3 py-1 bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400 rounded-full text-xs">
                  Status: {statusFilter}
                </span>
              )}
              {paymentFilter !== 'all' && (
                <span className="px-3 py-1 bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400 rounded-full text-xs">
                  Payment: {paymentFilter.toUpperCase()}
                </span>
              )}
              {balanceFilter !== 'all' && (
                <span className="px-3 py-1 bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400 rounded-full text-xs">
                  Balance: {balanceFilter.replace('-', ' ')}
                </span>
              )}
              {dateRange.checkIn && (
                <span className="px-3 py-1 bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400 rounded-full text-xs">
                  From: {dateRange.checkIn}
                </span>
              )}
              {dateRange.checkOut && (
                <span className="px-3 py-1 bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400 rounded-full text-xs">
                  To: {dateRange.checkOut}
                </span>
              )}
            </div>
          </div>
        )}

        {/* Results Count */}
        <div className="mt-4 flex justify-between items-center">
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Showing <span className="font-semibold">{filteredDepartures.length}</span> of{' '}
            <span className="font-semibold">{departures.length}</span> departures
          </p>
          {Object.values({
            roomTypeFilter, 
            statusFilter, 
            paymentFilter, 
            balanceFilter,
            checkIn: dateRange.checkIn,
            checkOut: dateRange.checkOut
          }).some(filter => filter !== 'all' && filter !== '') && (
            <button
              onClick={clearAllFilters}
              className="text-sm text-blue-600 hover:text-blue-700"
            >
              Clear filters
            </button>
          )}
        </div>
      </div>

      {/* Departures Display */}
      <div className="card">
        <div className="mb-4">
          <h2 className="text-xl font-semibold">Today's Departures</h2>
          <div className="flex items-center gap-2 mt-2">
            <span className="text-sm text-gray-600 dark:text-gray-400">
              View: <span className="font-medium capitalize">{viewMode} view</span>
            </span>
            <span className="text-sm text-gray-600 dark:text-gray-400">
              • Tab: <span className="font-medium capitalize">{activeTab}</span>
            </span>
          </div>
        </div>

        {viewMode === 'table' ? <TableView /> : <CardView />}

        {filteredDepartures.length === 0 && (
          <div className="text-center py-12">
            <AlertCircle className="mx-auto text-gray-400 mb-4" size={48} />
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">No Departures Found</h3>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              Try adjusting your filters or search query
            </p>
            <button
              onClick={clearAllFilters}
              className="mt-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium"
            >
              Clear All Filters
            </button>
          </div>
        )}
      </div>

      {/* Modals */}
      {showCheckoutModal && <CheckoutModal />}
      {showPaymentModal && <PaymentModal />}
    </div>
  );
};

export default CheckOut;