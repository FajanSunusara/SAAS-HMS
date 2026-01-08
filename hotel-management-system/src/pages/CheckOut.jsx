import { useState, useEffect } from 'react';
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
  Wallet,
  Loader,
  RefreshCw,
  Eye,
  Printer,
  Download,
  Trash2,
  Edit,
  MoreVertical
} from 'lucide-react';
import { checkoutService } from '../api/checkoutService';
import { paymentService } from '../api/paymentService';
import { bookingService } from '../api/bookingService';
import { invoiceService } from '../api/invoiceService';
import { roomService } from '../api/roomService';

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
    markRoomVacant: true,
    triggerHousekeeping: true,
    generateInvoice: true,
    sendInvoice: false
  });

  // Data states
  const [departures, setDepartures] = useState([]);
  const [additionalCharges, setAdditionalCharges] = useState([]);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [stats, setStats] = useState({
    totalDepartures: 0,
    pendingPayments: 0,
    completedCheckouts: 0,
    totalRevenue: 0
  });

  // View mode states
  const [viewMode, setViewMode] = useState('table');
  const [activeTab, setActiveTab] = useState('all');
  
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

  // Fetch data on component mount
  useEffect(() => {
    fetchTodayDepartures();
  }, []);

  // Fetch today's departures
  const fetchTodayDepartures = async () => {
    setLoading(true);
    try {
      console.log('🔍 Fetching today\'s departures...');
      const response = await checkoutService.getTodayCheckOuts();
      console.log('📊 API Response:', response);
      
      const bookings = response.data || [];
      console.log(`✅ Found ${bookings.length} bookings`);
      
      // Map API response to frontend format
      const mappedDepartures = bookings.map(booking => ({
        id: booking.bookingId,
        bookingCode: booking.bookingCode,
        guestName: `${booking.guest?.firstName || ''} ${booking.guest?.lastName || ''}`.trim() || 'Unknown Guest',
        roomNo: booking.rooms?.[0]?.roomNumber || 'N/A',
        roomType: booking.rooms?.[0]?.type || 'Standard Room',
        checkIn: booking.checkInDate,
        checkInTime: booking.checkInTime || '14:00',
        checkOut: booking.checkOutDate,
        checkOutTime: booking.checkOutTime || '12:00',
        nights: booking.nights || 1,
        adults: booking.adults || 1,
        children: booking.children || 0,
        roomCharges: parseFloat(booking.roomCharges || 0),
        services: parseFloat(booking.serviceCharges || 0),
        taxes: parseFloat(booking.taxAmount || 0),
        totalAmount: parseFloat(booking.totalAmount || 0),
        amountPaid: parseFloat(booking.amountPaid || 0),
        balance: parseFloat(booking.balanceDue || 0),
        status: mapBookingStatus(booking.status),
        email: booking.guest?.email || '',
        phone: booking.guest?.phoneNumber || '',
        idType: booking.guest?.idType || 'Passport',
        idNumber: booking.guest?.idNumber || '',
        nationality: booking.guest?.nationality || '',
        paymentMethod: getPaymentMethodFromStatus(booking.paymentStatus),
        checkedOutAt: booking.actualCheckOut,
        paymentStatus: booking.paymentStatus,
        bookingSource: booking.bookingSource,
        guestId: booking.guest?.guestId,
        roomId: booking.rooms?.[0]?.roomId
      }));

      console.log('📝 Mapped departures:', mappedDepartures);
      setDepartures(mappedDepartures);
      updateStats(mappedDepartures);

    } catch (error) {
      console.error('❌ Error fetching departures:', error);
      showToast('Failed to load departures', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Helper functions
  const mapBookingStatus = (status) => {
    const statusMap = {
      'CHECKED_IN': 'Checked In',
      'CHECKED_OUT': 'Checked Out',
      'CONFIRMED': 'Confirmed',
      'PENDING': 'Pending',
      'PAYMENT_DUE': 'Pending Payment',
      'PAID': 'Paid',
      'PARTIAL': 'Partial Payment',
      'CANCELLED': 'Cancelled'
    };
    return statusMap[status] || status;
  };

  const getPaymentMethodFromStatus = (paymentStatus) => {
    if (paymentStatus === 'PAID') return 'card';
    if (paymentStatus === 'PARTIAL') return 'cash';
    return 'cash';
  };

  const updateStats = (departuresData) => {
    const totalDepartures = departuresData.length;
    const pendingPayments = departuresData.filter(d => 
      d.status === 'Pending Payment' || d.status === 'Partial Payment'
    ).length;
    const completedCheckouts = departuresData.filter(d => 
      d.status === 'Checked Out'
    ).length;
    const totalRevenue = departuresData.reduce((sum, d) => sum + d.totalAmount, 0);
    
    setStats({
      totalDepartures,
      pendingPayments,
      completedCheckouts,
      totalRevenue: parseFloat(totalRevenue.toFixed(2))
    });
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'checked out':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case 'paid':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case 'pending payment':
      case 'partial payment':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      case 'checked in':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    }
  };

  const getPaymentMethodIcon = (method) => {
    switch (method?.toLowerCase()) {
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
    if (!guest) return;
    
    setSelectedGuest(guest);
    
    if (guest.balance > 0) {
      setShowPaymentModal(true);
    } else {
      setShowCheckoutModal(true);
    }
  };

  const handleProcessPayment = async () => {
    if (!selectedGuest) return;
    
    setProcessing(true);
    try {
      const paymentData = {
        bookingId: selectedGuest.id,
        amount: selectedGuest.balance,
        paymentMethod: paymentMethod.toUpperCase(),
        currency: 'INR',
        referenceNumber: `PAY-${Date.now()}`,
        notes: 'Final payment at check-out'
      };
      
      await paymentService.processPayment(paymentData);
      
      showToast('Payment processed successfully', 'success');
      setShowPaymentModal(false);
      
      // Refresh data
      fetchTodayDepartures();
      
      // If balance is now 0, open checkout modal
      setShowCheckoutModal(true);
    } catch (error) {
      console.error('Error processing payment:', error);
      showToast('Failed to process payment', 'error');
    } finally {
      setProcessing(false);
    }
  };

  const handleCompleteCheckout = async () => {
    if (!selectedGuest) return;
    
    setProcessing(true);
    try {
      const checkoutData = {
        discountAmount: discountAmount,
        discountType: discountType === 'percentage' ? 'PERCENTAGE' : 'FLAT',
        discountReason: 'Check-out discount',
        paymentMethod: paymentMethod.toUpperCase(),
        referenceNumber: `CHECKOUT-${Date.now()}`,
        markRoomVacant: confirmationOptions.markRoomVacant,
        triggerHousekeeping: confirmationOptions.triggerHousekeeping,
        generateInvoice: confirmationOptions.generateInvoice,
        sendInvoice: confirmationOptions.sendInvoice,
        additionalNotes: 'Check-out completed'
      };
      
      // Update booking status to CHECKED_OUT
      await bookingService.updateBookingStatus(selectedGuest.id, 'CHECKED_OUT');
      
      // Update room status
      if (confirmationOptions.markRoomVacant) {
        await roomService.updateRoomStatus(selectedGuest.roomId, 'AVAILABLE');
      } else if (confirmationOptions.triggerHousekeeping) {
        await roomService.updateRoomStatus(selectedGuest.roomId, 'CLEANING');
      }
      
      // Generate invoice if requested
      if (confirmationOptions.generateInvoice) {
        await invoiceService.generateInvoice(selectedGuest.id);
      }
      
      showToast('Check-out completed successfully', 'success');
      setShowCheckoutModal(false);
      
      // Refresh data
      fetchTodayDepartures();
      
    } catch (error) {
      console.error('Error processing check-out:', error);
      showToast('Failed to complete check-out', 'error');
    } finally {
      setProcessing(false);
    }
  };

  const handleSearch = () => {
    // Client-side search
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 300);
  };

  const updateGuestStatus = async (bookingId, newStatus) => {
    try {
      await bookingService.updateBookingStatus(bookingId, newStatus);
      showToast('Status updated successfully', 'success');
      fetchTodayDepartures();
    } catch (error) {
      console.error('Error updating status:', error);
      showToast('Failed to update status', 'error');
    }
  };

  const handleDiscountChange = (value) => {
    setDiscountAmount(Math.max(0, parseFloat(value) || 0));
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
    const total = subtotal + tax - discount;
    return total > 0 ? total : 0;
  };

  const filterDepartures = (departures) => {
    return departures.filter(departure => {
      // Search filter
      const matchesSearch = searchQuery === '' || 
        departure.guestName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        departure.roomNo.includes(searchQuery) ||
        departure.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
        departure.bookingCode.toLowerCase().includes(searchQuery.toLowerCase());

      // Tab filter
      let matchesTab = true;
      switch (activeTab) {
        case 'pending':
          matchesTab = departure.status === 'Pending Payment' || departure.status === 'Partial Payment';
          break;
        case 'completed':
          matchesTab = departure.status === 'Checked Out';
          break;
        case 'paid':
          matchesTab = departure.status === 'Paid';
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

      return matchesSearch && matchesTab && matchesRoomType && matchesStatus && matchesPayment && matchesBalance;
    });
  };

  const showToast = (message, type = 'success') => {
    // Create toast element
    const toast = document.createElement('div');
    toast.className = `fixed top-4 right-4 px-4 py-2 rounded-lg shadow-lg z-50 animate-fade-in flex items-center gap-2 ${
      type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'
    }`;
    toast.innerHTML = `
      ${type === 'success' ? '<svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg>' : '<svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path></svg>'}
      <span>${message}</span>
    `;
    document.body.appendChild(toast);
    
    setTimeout(() => {
      toast.classList.add('opacity-0', 'transition-opacity', 'duration-300');
      setTimeout(() => document.body.removeChild(toast), 300);
    }, 3000);
  };

  const clearAllFilters = () => {
    setRoomTypeFilter('all');
    setStatusFilter('all');
    setPaymentFilter('all');
    setBalanceFilter('all');
    setDateRange({ checkIn: '', checkOut: '' });
    setSearchQuery('');
    setActiveTab('all');
    fetchTodayDepartures();
  };

  // Get unique values for filters
  const roomTypes = ['all', ...new Set(departures.map(d => d.roomType).filter(Boolean))];
  const statuses = ['all', ...new Set(departures.map(d => d.status).filter(Boolean))];
  const paymentMethods = ['all', 'cash', 'card', 'upi'];

  const filteredDepartures = filterDepartures(departures);

  // Loading state
  if (loading && departures.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Loader className="animate-spin text-blue-600 mx-auto mb-4" size={48} />
          <p className="text-gray-600">Loading departures...</p>
        </div>
      </div>
    );
  }

  // Table View Component
  const TableView = () => (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead className="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-900 dark:text-gray-100">Guest</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-900 dark:text-gray-100">Room</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-900 dark:text-gray-100">Check-In</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-900 dark:text-gray-100">Check-Out</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-900 dark:text-gray-100">Nights</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-900 dark:text-gray-100">Total</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-900 dark:text-gray-100">Paid</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-900 dark:text-gray-100">Balance</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-900 dark:text-gray-100">Status</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-900 dark:text-gray-100">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
          {filteredDepartures.map((departure) => (
            <tr key={departure.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50">
              <td className="px-4 py-3">
                <div>
                  <p className="font-medium text-gray-900 dark:text-white">{departure.guestName}</p>
                  <p className="text-xs text-gray-600 dark:text-gray-400">{departure.email}</p>
                </div>
              </td>
              <td className="px-4 py-3">
                <div>
                  <p className="font-medium text-gray-900 dark:text-white">{departure.roomNo}</p>
                  <p className="text-xs text-gray-600 dark:text-gray-400">{departure.roomType}</p>
                </div>
              </td>
              <td className="px-4 py-3 text-sm text-gray-900 dark:text-gray-300">
                {departure.checkIn} {departure.checkInTime}
              </td>
              <td className="px-4 py-3 text-sm text-gray-900 dark:text-gray-300">
                {departure.checkOut} {departure.checkOutTime}
              </td>
              <td className="px-4 py-3 text-center text-gray-900 dark:text-gray-300">
                {departure.nights}
              </td>
              <td className="px-4 py-3 text-sm font-semibold text-gray-900 dark:text-white">
                ₹{departure.totalAmount.toFixed(2)}
              </td>
              <td className="px-4 py-3 text-sm text-green-600 dark:text-green-400">
                ₹{departure.amountPaid.toFixed(2)}
              </td>
              <td className="px-4 py-3">
                <span className={`font-semibold ${
                  departure.balance > 0 
                    ? 'text-red-600 dark:text-red-400' 
                    : 'text-green-600 dark:text-green-400'
                }`}>
                  ₹{departure.balance.toFixed(2)}
                </span>
              </td>
              <td className="px-4 py-3">
                <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(departure.status)}`}>
                  {departure.status}
                </span>
              </td>
              <td className="px-4 py-3">
                <div className="flex gap-2">
                  <button
                    onClick={() => navigate(`/bookings/${departure.id}`)}
                    className="p-2 bg-blue-100 hover:bg-blue-200 dark:bg-blue-900/30 dark:hover:bg-blue-800/50 text-blue-600 dark:text-blue-400 rounded-lg"
                    title="View Details"
                  >
                    <Eye size={16} />
                  </button>
                  {departure.status !== 'Checked Out' && departure.status !== 'Paid' && (
                    <>
                      {departure.balance > 0 && (
                        <button
                          onClick={() => {
                            setSelectedGuest(departure);
                            setShowPaymentModal(true);
                          }}
                          className="p-2 bg-yellow-100 hover:bg-yellow-200 dark:bg-yellow-900/30 dark:hover:bg-yellow-800/50 text-yellow-600 dark:text-yellow-400 rounded-lg"
                          title="Make Payment"
                        >
                          <DollarSign size={16} />
                        </button>
                      )}
                      <button
                        onClick={() => handleCheckOut(departure.id)}
                        className="p-2 bg-green-100 hover:bg-green-200 dark:bg-green-900/30 dark:hover:bg-green-800/50 text-green-600 dark:text-green-400 rounded-lg"
                        title="Check Out"
                      >
                        <Check size={16} />
                      </button>
                    </>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  // Card View Component
  const CardView = () => (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {filteredDepartures.map((departure) => (
        <div key={departure.id} className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 hover:shadow-xl transition-shadow duration-300">
          <div className="p-5">
            {/* Card Header */}
            <div className="flex justify-between items-start mb-4">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <User size={18} className="text-gray-500 dark:text-gray-400" />
                  <h3 className="font-bold text-lg text-gray-900 dark:text-white">{departure.guestName}</h3>
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
            <div className="grid grid-cols-2 gap-3 mb-4 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Check-in</p>
                <p className="font-medium text-sm text-gray-900 dark:text-white">{departure.checkIn}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Check-out</p>
                <p className="font-medium text-sm text-gray-900 dark:text-white">{departure.checkOut}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Nights</p>
                <p className="font-medium text-sm text-gray-900 dark:text-white">{departure.nights}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Guests</p>
                <p className="font-medium text-sm text-gray-900 dark:text-white">{departure.adults}A, {departure.children}C</p>
              </div>
            </div>

            {/* Financial Info */}
            <div className="space-y-2 mb-4">
              <div className="flex justify-between items-center">
                <span className="text-gray-600 dark:text-gray-400">Total Amount</span>
                <span className="font-bold text-lg text-gray-900 dark:text-white">₹{departure.totalAmount.toFixed(2)}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600 dark:text-gray-400">Paid</span>
                <span className="font-semibold text-green-600 dark:text-green-400">₹{departure.amountPaid.toFixed(2)}</span>
              </div>
              <div className="flex justify-between items-center pt-2 border-t dark:border-gray-700">
                <span className="font-semibold text-gray-900 dark:text-white">Balance</span>
                <span className={`font-bold text-lg ${departure.balance > 0 ? 'text-red-600 dark:text-red-400' : 'text-green-600 dark:text-green-400'}`}>
                  ₹{departure.balance.toFixed(2)}
                </span>
              </div>
            </div>

            {/* Payment Method & Time */}
            <div className="flex items-center gap-2 mb-4 p-2 bg-gray-50 dark:bg-gray-700/50 rounded">
              {getPaymentMethodIcon(departure.paymentMethod)}
              <span className="text-sm text-gray-900 dark:text-white capitalize">{departure.paymentMethod}</span>
              {departure.checkedOutAt && (
                <span className="ml-auto text-xs text-gray-500 dark:text-gray-400 flex items-center gap-1">
                  <Clock size={12} />
                  {new Date(departure.checkedOutAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                </span>
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex gap-2">
              <button
                onClick={() => navigate(`/bookings/${departure.id}`)}
                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg text-sm font-medium flex items-center justify-center gap-2"
              >
                <Eye size={16} />
                View
              </button>
              {departure.status !== 'Checked Out' && departure.status !== 'Paid' && (
                <>
                  {departure.balance > 0 && (
                    <button
                      onClick={() => {
                        setSelectedGuest(departure);
                        setShowPaymentModal(true);
                      }}
                      className="flex-1 bg-yellow-600 hover:bg-yellow-700 text-white py-2 rounded-lg text-sm font-medium flex items-center justify-center gap-2"
                    >
                      <DollarSign size={16} />
                      Pay
                    </button>
                  )}
                  <button
                    onClick={() => handleCheckOut(departure.id)}
                    className="flex-1 bg-green-600 hover:bg-green-700 text-white py-2 rounded-lg text-sm font-medium flex items-center justify-center gap-2"
                  >
                    <Check size={16} />
                    Check Out
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      ))}
    </div>
  );

  // Checkout Modal
  const CheckoutModal = () => {
    if (!selectedGuest) return null;

    const finalTotal = calculateTotal();
    const discountValue = discountType === 'percentage' 
      ? (selectedGuest.roomCharges + selectedGuest.services) * discountAmount / 100
      : discountAmount;

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-6xl max-h-[90vh] overflow-hidden">
          <div className="flex items-center justify-between p-6 border-b dark:border-gray-700">
            <div>
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Guest Checkout</h2>
              <p className="text-gray-600 dark:text-gray-400 mt-1">Finalize stay and generate invoice</p>
            </div>
            <div className="flex items-center gap-4">
              <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(selectedGuest.status)}`}>
                {selectedGuest.status}
              </span>
              <button
                onClick={() => setShowCheckoutModal(false)}
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg text-gray-600 dark:text-gray-400"
              >
                <X size={24} />
              </button>
            </div>
          </div>

          <div className="flex flex-col lg:flex-row h-[calc(90vh-8rem)]">
            <div className="lg:w-2/3 overflow-y-auto p-6">
              <div className="space-y-6">
                {/* Guest Information */}
                <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                  <h3 className="text-lg font-semibold mb-4 flex items-center gap-2 text-gray-900 dark:text-white">
                    <User size={20} />
                    Guest Information
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Full Name</p>
                      <p className="font-medium text-gray-900 dark:text-white">{selectedGuest.guestName}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Phone</p>
                      <p className="font-medium text-gray-900 dark:text-white flex items-center gap-2">
                        <Phone size={16} />
                        {selectedGuest.phone}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Email</p>
                      <p className="font-medium text-gray-900 dark:text-white flex items-center gap-2">
                        <Mail size={16} />
                        {selectedGuest.email}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Room</p>
                      <p className="font-medium text-gray-900 dark:text-white">{selectedGuest.roomNo} • {selectedGuest.roomType}</p>
                    </div>
                  </div>
                </div>

                {/* Stay Details */}
                <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                  <h3 className="text-lg font-semibold mb-4 flex items-center gap-2 text-gray-900 dark:text-white">
                    <Calendar size={20} />
                    Stay Details
                  </h3>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Check-in</p>
                      <p className="font-medium text-gray-900 dark:text-white">{selectedGuest.checkIn} {selectedGuest.checkInTime}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Check-out</p>
                      <p className="font-medium text-gray-900 dark:text-white">{selectedGuest.checkOut} {selectedGuest.checkOutTime}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Nights Stayed</p>
                      <p className="font-medium text-lg text-gray-900 dark:text-white">{selectedGuest.nights}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Guests</p>
                      <p className="font-medium text-gray-900 dark:text-white">{selectedGuest.adults} Adults, {selectedGuest.children} Children</p>
                    </div>
                  </div>
                </div>

                {/* Charges Breakdown */}
                <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                  <h3 className="text-lg font-semibold mb-4 flex items-center gap-2 text-gray-900 dark:text-white">
                    <DollarSign size={20} />
                    Charges Breakdown
                  </h3>
                  <div className="space-y-3">
                    <div className="flex justify-between items-center">
                      <span className="text-gray-700 dark:text-gray-300">Room Charges ({selectedGuest.nights} nights)</span>
                      <span className="font-semibold text-gray-900 dark:text-white">₹{selectedGuest.roomCharges.toFixed(2)}</span>
                    </div>
                    {selectedGuest.services > 0 && (
                      <div className="flex justify-between items-center">
                        <span className="text-gray-700 dark:text-gray-300">Additional Services</span>
                        <span className="font-semibold text-gray-900 dark:text-white">₹{selectedGuest.services.toFixed(2)}</span>
                      </div>
                    )}
                    <div className="flex justify-between items-center pt-3 border-t dark:border-gray-600">
                      <span className="font-medium text-gray-900 dark:text-white">Subtotal</span>
                      <span className="font-semibold text-gray-900 dark:text-white">₹{(selectedGuest.roomCharges + selectedGuest.services).toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between items-center text-red-600 dark:text-red-400">
                      <span>Taxes (10%)</span>
                      <span>₹{selectedGuest.taxes.toFixed(2)}</span>
                    </div>
                  </div>
                </div>

                {/* Discounts */}
                <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                  <h3 className="text-lg font-semibold mb-4 text-gray-900 dark:text-white">Discounts & Adjustments</h3>
                  <div className="space-y-4">
                    <div className="flex gap-4">
                      <div className="flex-1">
                        <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">Discount Type</label>
                        <select
                          value={discountType}
                          onChange={(e) => setDiscountType(e.target.value)}
                          className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                        >
                          <option value="percentage">Percentage (%)</option>
                          <option value="flat">Flat Amount (₹)</option>
                        </select>
                      </div>
                      <div className="flex-1">
                        <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">Discount Value</label>
                        <input
                          type="number"
                          value={discountAmount}
                          onChange={(e) => handleDiscountChange(e.target.value)}
                          className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                          placeholder="0"
                          min="0"
                          step="0.01"
                        />
                      </div>
                    </div>
                    {discountAmount > 0 && (
                      <div className="p-3 bg-green-50 dark:bg-green-900/20 rounded-lg">
                        <p className="text-green-700 dark:text-green-400 text-sm">
                          Discount applied: ₹{discountValue.toFixed(2)} ({discountType === 'percentage' ? `${discountAmount}%` : 'Flat amount'})
                        </p>
                      </div>
                    )}
                  </div>
                </div>

                {/* Checkout Actions */}
                <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                  <h3 className="text-lg font-semibold mb-4 flex items-center gap-2 text-gray-900 dark:text-white">
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
                          className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600"
                        />
                        <span className="text-sm text-gray-700 dark:text-gray-300">
                          {key === 'markRoomVacant' && 'Mark room as vacant'}
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

            {/* Bill Summary Sidebar */}
            <div className="lg:w-1/3 border-l dark:border-gray-700 bg-gray-50 dark:bg-gray-800">
              <div className="p-6 h-full flex flex-col">
                <h3 className="text-xl font-bold mb-6 text-gray-900 dark:text-white">Bill Summary</h3>
                
                <div className="mb-6">
                  <div className="flex items-center gap-3 mb-2">
                    <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center">
                      <User size={20} className="text-blue-600 dark:text-blue-400" />
                    </div>
                    <div>
                      <p className="font-semibold text-gray-900 dark:text-white">{selectedGuest.guestName}</p>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Room {selectedGuest.roomNo}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                    <Calendar size={16} />
                    <span>{selectedGuest.nights} nights • {selectedGuest.checkIn} to {selectedGuest.checkOut}</span>
                  </div>
                </div>

                <div className="space-y-3 mb-6">
                  <div className="flex justify-between text-gray-700 dark:text-gray-300">
                    <span>Room Charges</span>
                    <span>₹{selectedGuest.roomCharges.toFixed(2)}</span>
                  </div>
                  {selectedGuest.services > 0 && (
                    <div className="flex justify-between text-gray-700 dark:text-gray-300">
                      <span>Additional Services</span>
                      <span>₹{selectedGuest.services.toFixed(2)}</span>
                    </div>
                  )}
                  {discountAmount > 0 && (
                    <div className="flex justify-between text-green-600 dark:text-green-400">
                      <span>Discount</span>
                      <span>-₹{discountValue.toFixed(2)}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-red-600 dark:text-red-400">
                    <span>Taxes</span>
                    <span>₹{selectedGuest.taxes.toFixed(2)}</span>
                  </div>
                  <div className="pt-3 border-t dark:border-gray-700">
                    <div className="flex justify-between items-center">
                      <span className="font-semibold text-gray-900 dark:text-white">Total Amount</span>
                      <span className="text-2xl font-bold text-gray-900 dark:text-white">₹{finalTotal.toFixed(2)}</span>
                    </div>
                  </div>
                </div>

                <div className="mb-6">
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-gray-600 dark:text-gray-400">Advance Paid</span>
                    <span className="font-semibold text-green-600 dark:text-green-400">₹{selectedGuest.amountPaid.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-600 dark:text-gray-400">Balance Due</span>
                    <span className={`text-lg font-bold ${
                      selectedGuest.balance > 0 ? 'text-red-600 dark:text-red-400' : 'text-green-600 dark:text-green-400'
                    }`}>
                      ₹{selectedGuest.balance.toFixed(2)}
                    </span>
                  </div>
                </div>

                <button
                  onClick={handleCompleteCheckout}
                  disabled={processing}
                  className="w-full bg-green-600 hover:bg-green-700 disabled:bg-green-400 text-white py-3 px-4 rounded-lg font-semibold flex items-center justify-center gap-2 transition-colors"
                >
                  {processing ? (
                    <Loader className="animate-spin" size={20} />
                  ) : (
                    <Check size={20} />
                  )}
                  {processing ? 'Processing...' : 'Complete Checkout'}
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

  // Payment Modal
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
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg text-gray-600 dark:text-gray-400"
              >
                <X size={24} />
              </button>
            </div>

            <div className="mb-6">
              <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-gray-600 dark:text-gray-400">Guest</span>
                  <span className="font-semibold text-gray-900 dark:text-white">{selectedGuest.guestName}</span>
                </div>
                <div className="flex justify-between items-center mb-2">
                  <span className="text-gray-600 dark:text-gray-400">Room</span>
                  <span className="font-semibold text-gray-900 dark:text-white">{selectedGuest.roomNo}</span>
                </div>
                <div className="flex justify-between items-center pt-3 border-t dark:border-gray-600">
                  <span className="text-lg font-semibold text-gray-900 dark:text-white">Balance Due</span>
                  <span className="text-2xl font-bold text-red-600 dark:text-red-400">₹{selectedGuest.balance.toFixed(2)}</span>
                </div>
              </div>
            </div>

            <div className="mb-6">
              <h4 className="font-semibold mb-3 text-gray-900 dark:text-white">Payment Method</h4>
              <div className="grid grid-cols-2 gap-3">
                {['cash', 'card', 'upi'].map((method) => (
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
                    <span className="text-sm font-medium capitalize text-gray-900 dark:text-white">{method}</span>
                  </button>
                ))}
              </div>
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">Reference Number (Optional)</label>
              <input
                type="text"
                className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                placeholder="Enter reference number"
              />
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setShowPaymentModal(false)}
                className="flex-1 bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 py-3 rounded-lg font-medium transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleProcessPayment}
                disabled={processing}
                className="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-green-400 text-white py-3 rounded-lg font-semibold transition-colors"
              >
                {processing ? 'Processing...' : `Process Payment (₹${selectedGuest.balance.toFixed(2)})`}
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6 p-6">
      {/* Loading overlay */}
      {processing && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <Loader className="animate-spin text-white" size={48} />
        </div>
      )}

      {/* Page Header */}
      <div>
        <div className="flex justify-between items-center mb-2">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Check-Out Operations</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage guest departures and payments</p>
          </div>
          <button
            onClick={fetchTodayDepartures}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors flex items-center gap-2"
          >
            <RefreshCw size={18} />
            Refresh
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Total Departures</p>
              <p className="text-3xl font-bold mt-2 text-gray-900 dark:text-white">{stats.totalDepartures}</p>
            </div>
            <div className="p-3 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
              <Calendar className="text-blue-600 dark:text-blue-400" size={24} />
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Pending Payments</p>
              <p className="text-3xl font-bold mt-2 text-gray-900 dark:text-white">{stats.pendingPayments}</p>
            </div>
            <div className="p-3 bg-yellow-100 dark:bg-yellow-900/30 rounded-lg">
              <DollarSign className="text-yellow-600 dark:text-yellow-400" size={24} />
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Checked Out</p>
              <p className="text-3xl font-bold mt-2 text-gray-900 dark:text-white">{stats.completedCheckouts}</p>
            </div>
            <div className="p-3 bg-green-100 dark:bg-green-900/30 rounded-lg">
              <CheckCircle className="text-green-600 dark:text-green-400" size={24} />
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Total Revenue</p>
              <p className="text-3xl font-bold mt-2 text-gray-900 dark:text-white">₹{stats.totalRevenue.toFixed(2)}</p>
            </div>
            <div className="p-3 bg-purple-100 dark:bg-purple-900/30 rounded-lg">
              <CreditCard className="text-purple-600 dark:text-purple-400" size={24} />
            </div>
          </div>
        </div>
      </div>

      {/* Search and Filter Bar */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
        <div className="flex flex-col md:flex-row gap-4 items-center">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
            <input
              type="text"
              placeholder="Search by guest name, room number, or booking code..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            />
          </div>
          
          <div className="flex gap-2">
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="px-4 py-2 bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 rounded-lg font-medium flex items-center gap-2"
            >
              <Filter size={18} />
              Filters
              {showFilters && <ChevronDown size={18} className="transform rotate-180" />}
            </button>
            
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
          </div>
        </div>

        {/* Advanced Filters Panel */}
        {showFilters && (
          <div className="mt-6 p-4 border border-gray-200 dark:border-gray-700 rounded-lg">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-semibold text-lg text-gray-900 dark:text-white">Advanced Filters</h3>
              <button
                onClick={clearAllFilters}
                className="text-sm text-red-600 hover:text-red-700"
              >
                Clear All Filters
              </button>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              {/* Status Tabs */}
              <div className="lg:col-span-4">
                <div className="flex space-x-1">
                  {[
                    { id: 'all', label: 'All Departures', count: departures.length },
                    { id: 'pending', label: 'Pending Payment', count: departures.filter(d => d.status === 'Pending Payment' || d.status === 'Partial Payment').length },
                    { id: 'completed', label: 'Checked Out', count: departures.filter(d => d.status === 'Checked Out').length },
                    { id: 'paid', label: 'Paid', count: departures.filter(d => d.status === 'Paid').length }
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
              </div>

              {/* Other Filters */}
              <div>
                <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">Room Type</label>
                <select
                  value={roomTypeFilter}
                  onChange={(e) => setRoomTypeFilter(e.target.value)}
                  className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                >
                  <option value="all">All Room Types</option>
                  {roomTypes.filter(t => t !== 'all').map((type) => (
                    <option key={type} value={type}>{type}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">Payment Method</label>
                <select
                  value={paymentFilter}
                  onChange={(e) => setPaymentFilter(e.target.value)}
                  className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                >
                  <option value="all">All Methods</option>
                  {paymentMethods.filter(m => m !== 'all').map((method) => (
                    <option key={method} value={method}>{method.toUpperCase()}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">Balance</label>
                <select
                  value={balanceFilter}
                  onChange={(e) => setBalanceFilter(e.target.value)}
                  className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                >
                  <option value="all">All Balances</option>
                  <option value="with-balance">With Balance</option>
                  <option value="without-balance">Without Balance</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2 text-gray-700 dark:text-gray-300">Date Range</label>
                <input
                  type="date"
                  value={dateRange.checkOut}
                  onChange={(e) => setDateRange(prev => ({ ...prev, checkOut: e.target.value }))}
                  className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                />
              </div>
            </div>
          </div>
        )}

        {/* Results Count */}
        <div className="mt-4 flex justify-between items-center">
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Showing <span className="font-semibold text-gray-900 dark:text-white">{filteredDepartures.length}</span> of{' '}
            <span className="font-semibold text-gray-900 dark:text-white">{departures.length}</span> departures
          </p>
          {loading && (
            <div className="flex items-center gap-2">
              <Loader className="animate-spin text-blue-600" size={16} />
              <span className="text-sm text-gray-600 dark:text-gray-400">Loading...</span>
            </div>
          )}
        </div>
      </div>

      {/* Departures Display */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
        <div className="mb-4">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Today's Departures</h2>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            Manage guest check-outs and payments for today
          </p>
        </div>

        {filteredDepartures.length === 0 ? (
          <div className="text-center py-12">
            <AlertCircle className="mx-auto text-gray-400 mb-4" size={48} />
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">No Departures Found</h3>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              {departures.length === 0 
                ? 'No departures scheduled for today. Check back later!'
                : 'Try adjusting your filters or search query'
              }
            </p>
            {departures.length > 0 && (
              <button
                onClick={clearAllFilters}
                className="mt-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium"
              >
                Clear All Filters
              </button>
            )}
          </div>
        ) : viewMode === 'table' ? (
          <TableView />
        ) : (
          <CardView />
        )}
      </div>

      {/* Modals */}
      {showCheckoutModal && <CheckoutModal />}
      {showPaymentModal && <PaymentModal />}

      {/* CSS Animations */}
      <style jsx>{`
        @keyframes fade-in {
          from { opacity: 0; transform: translateY(-10px); }
          to { opacity: 1; transform: translateY(0); }
        }
        .animate-fade-in {
          animation: fade-in 0.3s ease-out;
        }
      `}</style>
    </div>
  );
};

export default CheckOut;