import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Filter, X, Check } from 'lucide-react';

const CheckIn = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [showFilterModal, setShowFilterModal] = useState(false);
  
  // Advanced filter states
  const [roomTypeFilters, setRoomTypeFilters] = useState([]);
  const [statusFilters, setStatusFilters] = useState([]);
  const [bookingSourceFilters, setBookingSourceFilters] = useState([]);
  const [guestTypeFilters, setGuestTypeFilters] = useState([]);
  
  // Available filter options
  const roomTypes = ['Standard Room', 'Deluxe Suite', 'Executive Suite', 'Presidential Suite'];
  const statusOptions = ['Pending', 'Checked In', 'Payment Due', 'Late Arrival'];
  const bookingSources = ['Website', 'OTA', 'Direct Call', 'Travel Agent', 'Corporate'];
  const guestTypes = ['VIP', 'New Guest', 'Returning Guest', 'Corporate'];

  // API: GET /api/checkins/today - Get today's check-ins
  // API: POST /api/checkins - Process check-in
  // API: GET /api/checkins/arrivals - Get expected arrivals
  // API: PATCH /api/checkins/{id}/cancel - Cancel check-in

  const stats = [
    { label: "Today's Check-ins", value: 3, subtext: '/ 15 Total' },
    { label: 'Occupied Rooms', value: 85, subtext: '85% Occupancy' },
    { label: "Today's Arrivals", value: 15, subtext: '12 Pending' },
    { label: 'Canceled Arrivals', value: 2, subtext: 'Action Required' },
  ];

  const arrivals = [
    {
      id: 1,
      guestName: 'John Anderson',
      bookingId: 'BK-847291',
      roomType: 'Deluxe Suite',
      roomNo: '301',
      arrivalTime: '09:00 AM',
      status: 'Pending',
      nights: 3,
      amount: '$450.00',
      phone: '+1 (555) 123-4567',
      email: 'john.anderson@email.com',
      bookingSource: 'Website',
      guestType: 'Returning Guest'
    },
    {
      id: 2,
      guestName: 'Sarah Williams',
      bookingId: 'BK-736482',
      roomType: 'Standard Room',
      roomNo: '205',
      arrivalTime: '11:30 AM',
      status: 'Checked In',
      nights: 2,
      amount: '$240.00',
      phone: '+1 (555) 987-6543',
      email: 'sarah.williams@email.com',
      bookingSource: 'OTA',
      guestType: 'New Guest'
    },
    {
      id: 3,
      guestName: 'Michael Brown',
      bookingId: 'BK-619384',
      roomType: 'Executive Suite',
      roomNo: '412',
      arrivalTime: '02:00 PM',
      status: 'Payment Due',
      nights: 5,
      amount: '$1,250.00',
      phone: '+1 (555) 456-7890',
      email: 'michael.brown@email.com',
      bookingSource: 'Corporate',
      guestType: 'Corporate'
    },
    {
      id: 4,
      guestName: 'Emily Davis',
      bookingId: 'BK-527491',
      roomType: 'Standard Room',
      roomNo: '108',
      arrivalTime: '03:30 PM',
      status: 'Pending',
      nights: 1,
      amount: '$120.00',
      phone: '+1 (555) 321-0987',
      email: 'emily.davis@email.com',
      bookingSource: 'Direct Call',
      guestType: 'VIP'
    },
    {
      id: 5,
      guestName: 'Robert Johnson',
      bookingId: 'BK-402837',
      roomType: 'Presidential Suite',
      roomNo: '502',
      arrivalTime: '04:45 PM',
      status: 'Late Arrival',
      nights: 7,
      amount: '$2,800.00',
      phone: '+1 (555) 654-3210',
      email: 'robert.johnson@email.com',
      bookingSource: 'Travel Agent',
      guestType: 'VIP'
    },
  ];

  const getStatusColor = (status) => {
    switch (status) {
      case 'Checked In':
        return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400';
      case 'Pending':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400';
      case 'Payment Due':
        return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400';
      case 'Late Arrival':
        return 'bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400';
    }
  };

  const handleCheckIn = (arrivalId) => {
    // API: POST /api/checkins
    navigate('/checkin-confirmation');
  };

  const handleFilterChange = (filterType, value) => {
    switch (filterType) {
      case 'roomType':
        setRoomTypeFilters(prev =>
          prev.includes(value)
            ? prev.filter(item => item !== value)
            : [...prev, value]
        );
        break;
      case 'status':
        setStatusFilters(prev =>
          prev.includes(value)
            ? prev.filter(item => item !== value)
            : [...prev, value]
        );
        break;
      case 'bookingSource':
        setBookingSourceFilters(prev =>
          prev.includes(value)
            ? prev.filter(item => item !== value)
            : [...prev, value]
        );
        break;
      case 'guestType':
        setGuestTypeFilters(prev =>
          prev.includes(value)
            ? prev.filter(item => item !== value)
            : [...prev, value]
        );
        break;
    }
  };

  const applyFilters = () => {
    setShowFilterModal(false);
    // Show confirmation toast
    showToast('Filters applied successfully');
  };

  const resetFilters = () => {
    setRoomTypeFilters([]);
    setStatusFilters([]);
    setBookingSourceFilters([]);
    setGuestTypeFilters([]);
    setSearchQuery('');
  };

  const showToast = (message) => {
    // In a real app, you would use a toast library or context
    const toast = document.createElement('div');
    toast.className = 'fixed top-4 right-4 bg-green-600 text-white px-4 py-2 rounded-lg shadow-lg z-50 animate-fade-in';
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
      toast.classList.add('animate-fade-out');
      setTimeout(() => document.body.removeChild(toast), 300);
    }, 3000);
  };

  const filteredArrivals = arrivals.filter(arrival => {
    // Global search filter
    const matchesSearch = searchQuery === '' ||
      arrival.guestName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      arrival.bookingId.toLowerCase().includes(searchQuery.toLowerCase()) ||
      arrival.roomNo.includes(searchQuery) ||
      arrival.roomType.toLowerCase().includes(searchQuery.toLowerCase()) ||
      arrival.status.toLowerCase().includes(searchQuery.toLowerCase());

    // Advanced filters
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

  // Handle keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e) => {
      // Ctrl+F or Cmd+F for search focus
      if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
        e.preventDefault();
        document.querySelector('input[type="text"]')?.focus();
      }
      // Escape to close modal
      if (e.key === 'Escape' && showFilterModal) {
        setShowFilterModal(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [showFilterModal]);

  return (
    <div className="space-y-6">
      {/* Add CSS animations for toast */}
      <style jsx>{`
        @keyframes fade-in {
          from { opacity: 0; transform: translateY(-10px); }
          to { opacity: 1; transform: translateY(0); }
        }
        @keyframes fade-out {
          from { opacity: 1; transform: translateY(0); }
          to { opacity: 0; transform: translateY(-10px); }
        }
        .animate-fade-in {
          animation: fade-in 0.3s ease-out;
        }
        .animate-fade-out {
          animation: fade-out 0.3s ease-out;
        }
      `}</style>

      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Check-In Command Center</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Overview of today's operations</p>
        </div>
        <button
          onClick={() => navigate('/walk-in')}
          className="btn-primary flex items-center gap-2"
        >
          <Plus size={20} />
          New Walk-In
        </button>
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
              className="input-field pl-10"
              aria-label="Search arrivals"
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery('')}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                aria-label="Clear search"
              >
                <X size={18} />
              </button>
            )}
          </div>
          
          {/* Filter Button */}
          <button
            onClick={() => setShowFilterModal(true)}
            className="px-4 py-2 bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 rounded-lg font-medium transition-colors flex items-center gap-2"
            aria-label="Open filters"
          >
            <Filter size={18} />
            Filter
            {(roomTypeFilters.length > 0 || statusFilters.length > 0 || 
              bookingSourceFilters.length > 0 || guestTypeFilters.length > 0) && (
              <span className="bg-primary-600 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                {roomTypeFilters.length + statusFilters.length + 
                 bookingSourceFilters.length + guestTypeFilters.length}
              </span>
            )}
          </button>

          {/* Reset Filters Button */}
          {(roomTypeFilters.length > 0 || statusFilters.length > 0 || 
            bookingSourceFilters.length > 0 || guestTypeFilters.length > 0 || searchQuery) && (
            <button
              onClick={resetFilters}
              className="px-4 py-2 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 font-medium"
            >
              Reset All
            </button>
          )}
        </div>

        {/* Active Filters Display */}
        {(roomTypeFilters.length > 0 || statusFilters.length > 0 || 
          bookingSourceFilters.length > 0 || guestTypeFilters.length > 0) && (
          <div className="mt-4 flex flex-wrap gap-2">
            {roomTypeFilters.map(type => (
              <span key={type} className="px-3 py-1 bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400 rounded-full text-sm flex items-center gap-1">
                {type}
                <button onClick={() => handleFilterChange('roomType', type)} className="ml-1 hover:text-blue-600">
                  <X size={14} />
                </button>
              </span>
            ))}
            {statusFilters.map(status => (
              <span key={status} className={`px-3 py-1 rounded-full text-sm flex items-center gap-1 ${getStatusColor(status)}`}>
                {status}
                <button onClick={() => handleFilterChange('status', status)} className="ml-1 hover:opacity-75">
                  <X size={14} />
                </button>
              </span>
            ))}
            {bookingSourceFilters.map(source => (
              <span key={source} className="px-3 py-1 bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400 rounded-full text-sm flex items-center gap-1">
                {source}
                <button onClick={() => handleFilterChange('bookingSource', source)} className="ml-1 hover:text-purple-600">
                  <X size={14} />
                </button>
              </span>
            ))}
            {guestTypeFilters.map(type => (
              <span key={type} className="px-3 py-1 bg-amber-100 text-amber-800 dark:bg-amber-900/20 dark:text-amber-400 rounded-full text-sm flex items-center gap-1">
                {type}
                <button onClick={() => handleFilterChange('guestType', type)} className="ml-1 hover:text-amber-600">
                  <X size={14} />
                </button>
              </span>
            ))}
          </div>
        )}
      </div>

      {/* Filter Modal */}
      {showFilterModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            {/* Modal Header */}
            <div className="flex items-center justify-between p-6 border-b dark:border-gray-700">
              <div>
                <h2 className="text-xl font-bold text-gray-900 dark:text-white">Advanced Filters</h2>
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  Filter arrivals by multiple criteria
                </p>
              </div>
              <button
                onClick={() => setShowFilterModal(false)}
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
                aria-label="Close modal"
              >
                <X size={24} />
              </button>
            </div>

            {/* Modal Content */}
            <div className="p-6 space-y-8">
              {/* Room Type Filter */}
              <div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Room Type</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                  {roomTypes.map(type => (
                    <button
                      key={type}
                      onClick={() => handleFilterChange('roomType', type)}
                      className={`px-4 py-3 rounded-lg border-2 transition-all ${
                        roomTypeFilters.includes(type)
                          ? 'border-primary-600 bg-primary-50 dark:bg-primary-900/20 text-primary-600 dark:text-primary-400'
                          : 'border-gray-200 dark:border-gray-700 hover:border-primary-400 hover:bg-gray-50 dark:hover:bg-gray-700'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-medium">{type}</span>
                        {roomTypeFilters.includes(type) && <Check size={18} />}
                      </div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Status Filter */}
              <div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Arrival Status</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                  {statusOptions.map(status => (
                    <button
                      key={status}
                      onClick={() => handleFilterChange('status', status)}
                      className={`px-4 py-3 rounded-lg border-2 transition-all ${
                        statusFilters.includes(status)
                          ? 'border-primary-600 bg-primary-50 dark:bg-primary-900/20 text-primary-600 dark:text-primary-400'
                          : 'border-gray-200 dark:border-gray-700 hover:border-primary-400 hover:bg-gray-50 dark:hover:bg-gray-700'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-medium">{status}</span>
                        {statusFilters.includes(status) && <Check size={18} />}
                      </div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Booking Source Filter */}
              <div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Booking Source</h3>
                <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
                  {bookingSources.map(source => (
                    <button
                      key={source}
                      onClick={() => handleFilterChange('bookingSource', source)}
                      className={`px-4 py-3 rounded-lg border-2 transition-all ${
                        bookingSourceFilters.includes(source)
                          ? 'border-primary-600 bg-primary-50 dark:bg-primary-900/20 text-primary-600 dark:text-primary-400'
                          : 'border-gray-200 dark:border-gray-700 hover:border-primary-400 hover:bg-gray-50 dark:hover:bg-gray-700'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-medium">{source}</span>
                        {bookingSourceFilters.includes(source) && <Check size={18} />}
                      </div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Guest Type Filter */}
              <div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Guest Type</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                  {guestTypes.map(type => (
                    <button
                      key={type}
                      onClick={() => handleFilterChange('guestType', type)}
                      className={`px-4 py-3 rounded-lg border-2 transition-all ${
                        guestTypeFilters.includes(type)
                          ? 'border-primary-600 bg-primary-50 dark:bg-primary-900/20 text-primary-600 dark:text-primary-400'
                          : 'border-gray-200 dark:border-gray-700 hover:border-primary-400 hover:bg-gray-50 dark:hover:bg-gray-700'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-medium">{type}</span>
                        {guestTypeFilters.includes(type) && <Check size={18} />}
                      </div>
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Modal Footer */}
            <div className="p-6 border-t dark:border-gray-700 flex justify-between">
              <button
                onClick={resetFilters}
                className="px-6 py-3 text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium"
              >
                Reset All Filters
              </button>
              <div className="flex gap-3">
                <button
                  onClick={() => setShowFilterModal(false)}
                  className="px-6 py-3 border-2 border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg font-medium hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={applyFilters}
                  className="px-6 py-3 bg-primary-600 text-white rounded-lg font-medium hover:bg-primary-700 transition-colors"
                >
                  Apply Filters
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Arrivals Table */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold">Today's Expected Arrivals</h2>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Showing {filteredArrivals.length} of {arrivals.length} arrivals
          </p>
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
                      <div className="flex gap-2 mt-1">
                        <span className="text-xs px-2 py-1 bg-amber-100 text-amber-800 dark:bg-amber-900/20 dark:text-amber-400 rounded">
                          {arrival.guestType}
                        </span>
                        <span className="text-xs px-2 py-1 bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400 rounded">
                          {arrival.bookingSource}
                        </span>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm font-mono">{arrival.bookingId}</td>
                  <td className="px-4 py-3 text-sm">{arrival.roomType}</td>
                  <td className="px-4 py-3 text-sm font-semibold">{arrival.roomNo}</td>
                  <td className="px-4 py-3 text-sm">{arrival.arrivalTime}</td>
                  <td className="px-4 py-3">
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(arrival.status)}`}>
                      {arrival.status}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      {arrival.status !== 'Checked In' && (
                        <button
                          onClick={() => handleCheckIn(arrival.id)}
                          className="px-3 py-1 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm font-medium transition-colors"
                        >
                          Check In
                        </button>
                      )}
                      <button className="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition-colors">
                        View
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filteredArrivals.length === 0 && (
          <div className="text-center py-12">
            <Search size={48} className="mx-auto text-gray-400 mb-4" />
            <p className="text-gray-500">No arrivals found matching your criteria</p>
            {(searchQuery || roomTypeFilters.length > 0 || statusFilters.length > 0 || 
              bookingSourceFilters.length > 0 || guestTypeFilters.length > 0) && (
              <button
                onClick={resetFilters}
                className="mt-4 text-primary-600 hover:text-primary-700 font-medium"
              >
                Clear all filters
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default CheckIn;