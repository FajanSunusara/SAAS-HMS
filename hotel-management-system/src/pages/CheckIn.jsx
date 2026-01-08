import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Filter, X, Check, Loader } from 'lucide-react';
import { bookingService } from '../api/bookingService';
import { checkinService } from '../api/checkinService';
import { roomService } from '../api/roomService';

const CheckIn = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [showFilterModal, setShowFilterModal] = useState(false);
  const [loading, setLoading] = useState(false);
  const [arrivals, setArrivals] = useState([]);
  const [stats, setStats] = useState([
    { label: "Today's Check-ins", value: 0, subtext: '/ 0 Total' },
    { label: 'Occupied Rooms', value: 0, subtext: '0% Occupancy' },
    { label: "Today's Arrivals", value: 0, subtext: '0 Pending' },
    { label: 'Canceled Arrivals', value: 0, subtext: 'Action Required' },
  ]);
  
  // Advanced filter states
  const [roomTypeFilters, setRoomTypeFilters] = useState([]);
  const [statusFilters, setStatusFilters] = useState([]);
  const [bookingSourceFilters, setBookingSourceFilters] = useState([]);
  const [guestTypeFilters, setGuestTypeFilters] = useState([]);
  
  // Available filter options
  const roomTypes = ['Standard Room', 'Deluxe Suite', 'Executive Suite', 'Presidential Suite'];
  const statusOptions = ['PENDING', 'CHECKED_IN', 'PAYMENT_DUE', 'LATE_ARRIVAL'];
  const bookingSources = ['WEBSITE', 'OTA', 'DIRECT_CALL', 'TRAVEL_AGENT', 'CORPORATE'];
  const guestTypes = ['VIP', 'NEW_GUEST', 'RETURNING_GUEST', 'CORPORATE'];

  // Fetch data on component mount
  useEffect(() => {
    fetchDashboardData();
  }, []);

const fetchDashboardData = async () => {
  setLoading(true);
  try {
    console.log('Fetching dashboard data...');
    
    // Fetch today's check-ins
    const checkinsResponse = await checkinService.getTodayCheckIns();
    console.log('Check-ins API response:', checkinsResponse);
    const checkins = checkinsResponse.data || [];
    console.log('Check-ins data:', checkins);
    
    // Fetch today's expected arrivals
    const arrivalsResponse = await bookingService.getAllBookings();
    console.log('Arrivals API response:', arrivalsResponse);
    const allArrivals = arrivalsResponse.data || [];
    console.log('All arrivals data:', allArrivals);
    
    // Filter for today's arrivals
    const today = new Date().toISOString().split('T')[0];
    console.log('Today:', today);
    
    const todayArrivals = allArrivals.filter(arrival => {
      const arrivalDate = new Date(arrival.checkInDate).toISOString().split('T')[0];
      console.log('Arrival checkInDate:', arrival.checkInDate, 'Converted:', arrivalDate);
      return arrivalDate === today;
    });
    
    console.log('Today\'s arrivals:', todayArrivals);
    
    // Update arrivals state
    const mappedArrivals = mapBookingToArrival(todayArrivals);
    console.log('Mapped arrivals:', mappedArrivals);
    setArrivals(mappedArrivals);
    
    // Fetch room status for occupancy calculation
    const roomSummaryResponse = await roomService.getRoomStatusSummary();
    console.log('Room summary response:', roomSummaryResponse);
    const roomSummary = roomSummaryResponse.data || {};
    console.log('Room summary data:', roomSummary);
    
    // Update stats
    updateStats(checkins, todayArrivals, roomSummary);
    
  } catch (error) {
    console.error('Error fetching dashboard data:', error);
    showToast('Failed to load dashboard data', 'error');
  } finally {
    setLoading(false);
  }
};


  const updateStats = (checkins, todayArrivals, roomSummary) => {
    const totalRooms = roomSummary.TOTAL || 100;
    const occupiedRooms = roomSummary.OCCUPIED || 0;
    const occupancyRate = totalRooms > 0 ? Math.round((occupiedRooms / totalRooms) * 100) : 0;
    
    const pendingArrivals = todayArrivals.filter(a => a.status === 'PENDING').length;
    const checkedInCount = checkins.length;
    const canceledCount = todayArrivals.filter(a => a.status === 'CANCELLED').length;
    
    setStats([
      { 
        label: "Today's Check-ins", 
        value: checkedInCount, 
        subtext: `/ ${todayArrivals.length} Total` 
      },
      { 
        label: 'Occupied Rooms', 
        value: occupiedRooms, 
        subtext: `${occupancyRate}% Occupancy` 
      },
      { 
        label: "Today's Arrivals", 
        value: todayArrivals.length, 
        subtext: `${pendingArrivals} Pending` 
      },
      { 
        label: 'Canceled Arrivals', 
        value: canceledCount, 
        subtext: 'Action Required' 
      },
    ]);
  };

  const mapBookingToArrival = (bookings) => {
    return bookings.map(booking => ({
      id: booking.id,
      guestName: booking.guestName || `${booking.guest?.firstName} ${booking.guest?.lastName}`,
      bookingId: booking.bookingCode,
      roomType: booking.roomType || booking.room?.type,
      roomNo: booking.roomNumber || booking.room?.number,
      arrivalTime: formatTime(booking.checkInDate),
      status: booking.status,
      nights: calculateNights(booking.checkInDate, booking.checkOutDate),
      amount: `$${booking.totalAmount || 0}`,
      phone: booking.guest?.phoneNumber,
      email: booking.guest?.email,
      bookingSource: booking.source,
      guestType: booking.guestType
    }));
  };

  const formatTime = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: true 
    });
  };

  const calculateNights = (checkInDate, checkOutDate) => {
    const checkIn = new Date(checkInDate);
    const checkOut = new Date(checkOutDate);
    const diffTime = Math.abs(checkOut - checkIn);
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  };

  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case 'CHECKED_IN':
        return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400';
      case 'PENDING':
      case 'CONFIRMED':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400';
      case 'PAYMENT_DUE':
        return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400';
      case 'LATE_ARRIVAL':
        return 'bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400';
    }
  };

  const getDisplayStatus = (status) => {
    const statusMap = {
      'CHECKED_IN': 'Checked In',
      'PENDING': 'Pending',
      'CONFIRMED': 'Confirmed',
      'PAYMENT_DUE': 'Payment Due',
      'LATE_ARRIVAL': 'Late Arrival',
      'CANCELLED': 'Cancelled'
    };
    return statusMap[status] || status;
  };

  const handleCheckIn = async (arrivalId) => {
    try {
      setLoading(true);
      // First, update booking status to CHECKED_IN
      await bookingService.updateBookingStatus(arrivalId, 'CHECKED_IN');
      
      // Then, update room status to OCCUPIED
      const arrival = arrivals.find(a => a.id === arrivalId);
      if (arrival.roomId) {
        await roomService.updateRoomStatus(arrival.roomId, 'OCCUPIED');
      }
      
      showToast('Check-in processed successfully', 'success');
      
      // Refresh data
      fetchDashboardData();
      
      // Navigate to check-in confirmation with data
      navigate(`/checkin-confirmation/${arrivalId}`);
      
    } catch (error) {
      console.error('Error processing check-in:', error);
      showToast('Failed to process check-in', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelCheckIn = async (arrivalId) => {
    try {
      const reason = prompt('Please enter reason for cancellation:');
      if (reason) {
        setLoading(true);
        await checkinService.cancelCheckIn(arrivalId, reason);
        showToast('Check-in cancelled', 'success');
        fetchDashboardData();
      }
    } catch (error) {
      console.error('Error cancelling check-in:', error);
      showToast('Failed to cancel check-in', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    try {
      setLoading(true);
      const response = await checkinService.searchBookingsForCheckIn(searchQuery, {
        roomType: roomTypeFilters.join(','),
        status: statusFilters.join(','),
        source: bookingSourceFilters.join(','),
        guestType: guestTypeFilters.join(',')
      });
      
      if (response.data) {
        setArrivals(mapBookingToArrival(response.data.content || response.data));
      }
    } catch (error) {
      console.error('Error searching bookings:', error);
      showToast('Search failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showToast = (message, type = 'success') => {
    // Using a toast library or custom toast implementation
    const toast = document.createElement('div');
    toast.className = `fixed top-4 right-4 px-4 py-2 rounded-lg shadow-lg z-50 animate-fade-in ${
      type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'
    }`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
      toast.classList.add('animate-fade-out');
      setTimeout(() => document.body.removeChild(toast), 300);
    }, 3000);
  };

  // Add keyboard shortcuts handler
  useEffect(() => {
    const handleKeyDown = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
        e.preventDefault();
        document.querySelector('input[type="text"]')?.focus();
      }
      if (e.key === 'Enter' && searchQuery) {
        handleSearch();
      }
      if (e.key === 'Escape' && showFilterModal) {
        setShowFilterModal(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [showFilterModal, searchQuery]);

  const filteredArrivals = arrivals.filter(arrival => {
    // Client-side filtering as fallback
    const matchesSearch = searchQuery === '' ||
      arrival.guestName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      arrival.bookingId.toLowerCase().includes(searchQuery.toLowerCase()) ||
      arrival.roomNo.includes(searchQuery) ||
      arrival.roomType.toLowerCase().includes(searchQuery.toLowerCase()) ||
      getDisplayStatus(arrival.status).toLowerCase().includes(searchQuery.toLowerCase());

    const matchesRoomType = roomTypeFilters.length === 0 || 
      roomTypeFilters.includes(arrival.roomType);
    const matchesStatus = statusFilters.length === 0 || 
      statusFilters.includes(arrival.status);
    const matchesBookingSource = bookingSourceFilters.length === 0 || 
      bookingSourceFilters.includes(arrival.bookingSource);
    const matchesGuestType = guestTypeFilters.length === 0 || 
      guestTypeFilters.includes(arrival.guestType);

    return matchesSearch && matchesRoomType && matchesStatus && 
           matchesBookingSource && matchesGuestType;
  });

  if (loading && arrivals.length === 0) {
    return (
      <div className="flex items-center justify-center h-96">
        <Loader className="animate-spin text-primary-600" size={48} />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Loading overlay */}
      {loading && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <Loader className="animate-spin text-white" size={48} />
        </div>
      )}

      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Check-In Command Center</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            Overview of today's operations • Last updated: {new Date().toLocaleTimeString()}
          </p>
        </div>
        <div className="flex gap-3">
          <button
            onClick={fetchDashboardData}
            className="px-4 py-2 bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 rounded-lg font-medium transition-colors"
          >
            Refresh
          </button>
          <button
            onClick={() => navigate('/walk-in')}
            className="btn-primary flex items-center gap-2"
          >
            <Plus size={20} />
            New Walk-In
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat, index) => (
          <div key={index} className="card">
            <p className="text-sm text-gray-600 dark:text-gray-400">{stat.label}</p>
            <div className="flex items-baseline gap-2 mt-2">
              <p className="text-3xl font-bold">{stat.value}</p>
              <p className="text-sm text-gray-500">{stat.subtext}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Search and Filter Bar */}
      <div className="card">
        <div className="flex flex-col md:flex-row gap-4 items-center">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
            <input
              type="text"
              placeholder="Search by guest name, booking ID, room number, room type, or status..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              className="input-field pl-10"
              aria-label="Search arrivals"
            />
            {searchQuery && (
              <button
                onClick={() => {
                  setSearchQuery('');
                  fetchDashboardData();
                }}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                aria-label="Clear search"
              >
                <X size={18} />
              </button>
            )}
          </div>
          
          <button
            onClick={handleSearch}
            className="px-4 py-2 bg-primary-600 hover:bg-primary-700 text-white rounded-lg font-medium transition-colors"
          >
            Search
          </button>
          
          <button
            onClick={() => setShowFilterModal(true)}
            className="px-4 py-2 bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 rounded-lg font-medium transition-colors flex items-center gap-2"
          >
            <Filter size={18} />
            Filter
          </button>
        </div>
      </div>

      {/* Arrivals Table */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold">Today's Expected Arrivals</h2>
          <div className="flex items-center gap-4">
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Showing {filteredArrivals.length} of {arrivals.length} arrivals
            </p>
            <button
              onClick={fetchDashboardData}
              className="text-sm text-primary-600 hover:text-primary-700 font-medium"
            >
              Refresh Data
            </button>
          </div>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 dark:bg-gray-700/50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-semibold">Guest Name</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Booking ID</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Room Type</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Room No.</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Arrival Time</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Status</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
              {filteredArrivals.map((arrival) => (
                <tr key={arrival.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                  <td className="px-4 py-3">
                    <div>
                      <p className="font-medium">{arrival.guestName}</p>
                      <p className="text-xs text-gray-600 dark:text-gray-400">{arrival.email}</p>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm font-mono">{arrival.bookingId}</td>
                  <td className="px-4 py-3 text-sm">{arrival.roomType}</td>
                  <td className="px-4 py-3 text-sm font-semibold">{arrival.roomNo}</td>
                  <td className="px-4 py-3 text-sm">{arrival.arrivalTime}</td>
                  <td className="px-4 py-3">
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(arrival.status)}`}>
                      {getDisplayStatus(arrival.status)}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      {arrival.status === 'PENDING' || arrival.status === 'CONFIRMED' ? (
                        <>
                          <button
                            onClick={() => handleCheckIn(arrival.id)}
                            className="px-3 py-1 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm font-medium transition-colors"
                          >
                            Check In
                          </button>
                          <button
                            onClick={() => handleCancelCheckIn(arrival.id)}
                            className="px-3 py-1 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium transition-colors"
                          >
                            Cancel
                          </button>
                        </>
                      ) : arrival.status === 'CHECKED_IN' ? (
                        <span className="px-3 py-1 bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400 rounded-lg text-sm">
                          Checked In
                        </span>
                      ) : null}
                      <button
                        onClick={() => navigate(`/bookings/${arrival.id}`)}
                        className="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition-colors"
                      >
                        View
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          
          {filteredArrivals.length === 0 && (
            <div className="text-center py-12">
              <Search size={48} className="mx-auto text-gray-400 mb-4" />
              <p className="text-gray-500">No arrivals found</p>
              <button
                onClick={fetchDashboardData}
                className="mt-4 text-primary-600 hover:text-primary-700 font-medium"
              >
                Refresh data
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CheckIn;