import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  DoorOpen, 
  Plus, 
  Search, 
  Filter,
  CheckCircle,
  AlertTriangle,
  Calendar,
  Users,
  Bed,
  Waves,
  Sun,
  Crown,
  AlertCircle,
  Check,
  X,
  RefreshCw,
  ChevronRight,
  BarChart3
} from 'lucide-react';
import API from '../api/axios';

const Rooms = () => {
  const navigate = useNavigate();
  
  // State management
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [isFilterPanelOpen, setIsFilterPanelOpen] = useState(false);
  const [selectedFeatures, setSelectedFeatures] = useState([]);
  const [selectedFloors, setSelectedFloors] = useState([]);
  const [selectedRoomTypes, setSelectedRoomTypes] = useState([]);
  const [maintenanceModalOpen, setMaintenanceModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [maintenanceReason, setMaintenanceReason] = useState('');
  const [roomStatusSummary, setRoomStatusSummary] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  // Status metrics data
  const statusMetrics = [
    {
      key: 'all',
      label: 'All Rooms',
      icon: <Bed className="w-5 h-5" />,
      color: 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200',
      activeColor: 'bg-gray-600 text-white dark:bg-gray-700',
      progressColor: 'bg-gray-400',
      description: 'Total rooms in hotel'
    },
    {
      key: 'AVAILABLE',
      label: 'Available',
      icon: <CheckCircle className="w-5 h-5" />,
      color: 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400',
      activeColor: 'bg-green-600 text-white dark:bg-green-700',
      progressColor: 'bg-green-400',
      description: 'Ready for booking'
    },
    {
      key: 'OCCUPIED',
      label: 'Occupied',
      icon: <Users className="w-5 h-5" />,
      color: 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400',
      activeColor: 'bg-blue-600 text-white dark:bg-blue-700',
      progressColor: 'bg-blue-400',
      description: 'Currently occupied'
    },
    {
      key: 'MAINTENANCE',
      label: 'Maintenance',
      icon: <AlertTriangle className="w-5 h-5" />,
      color: 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400',
      activeColor: 'bg-red-600 text-white dark:bg-red-700',
      progressColor: 'bg-red-400',
      description: 'Under maintenance'
    },
    {
      key: 'RESERVED',
      label: 'Reserved',
      icon: <Calendar className="w-5 h-5" />,
      color: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400',
      activeColor: 'bg-yellow-600 text-white dark:bg-yellow-700',
      progressColor: 'bg-yellow-400',
      description: 'Booked for future'
    },
    {
      key: 'CLEANING',
      label: 'Cleaning',
      icon: <Waves className="w-5 h-5" />,
      color: 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400',
      activeColor: 'bg-purple-600 text-white dark:bg-purple-700',
      progressColor: 'bg-purple-400',
      description: 'Being cleaned'
    }
  ];

  // Features available for filtering
  const availableFeatures = [
    { id: 'AC', label: 'AC', icon: <Sun className="w-4 h-4" /> },
    { id: 'SEA_VIEW', label: 'Sea View', icon: <Waves className="w-4 h-4" /> },
    { id: 'BALCONY', label: 'Balcony', icon: <DoorOpen className="w-4 h-4" /> },
    { id: 'VIP', label: 'VIP', icon: <Crown className="w-4 h-4" /> }
  ];

  // Fetch rooms data
  useEffect(() => {
    fetchRoomsData();
  }, [statusFilter, refreshKey]);

  const fetchRoomsData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [roomsResponse, summaryResponse] = await Promise.all([
        statusFilter === 'all' 
          ? API.get('/rooms')
          : API.get(`/rooms/status/${statusFilter}`),
        API.get('/rooms/status/summary')
      ]);

      // Transform API data to match our component structure
      const apiRooms = roomsResponse.data?.data || [];
      const transformedRooms = apiRooms.map(room => ({
        id: room.id,
        number: room.roomNumber,
        type: room.roomType || 'Standard Room',
        floor: room.floor || 1,
        status: room.status || 'AVAILABLE',
        guest: room.currentBooking?.guestName || null,
        checkIn: room.currentBooking?.checkInDate || null,
        checkOut: room.currentBooking?.checkOutDate || null,
        features: room.features || ['AC'],
        needsAttention: room.status === 'MAINTENANCE',
        checkoutToday: false,
        lateCheckout: false,
        isVIP: room.features?.includes('VIP') || false
      }));

      setRooms(transformedRooms);
      setRoomStatusSummary(summaryResponse.data?.data || {});

    } catch (err) {
      console.error('Error fetching rooms:', err);
      setError(err.response?.data?.message || 'Failed to load room data.');
      
      // Fallback to mock data for demo
      if (err.response?.status === 404) {
        const mockRooms = [
          { id: 101, number: '101', type: 'Standard Room', floor: 1, status: 'AVAILABLE', guest: null, features: ['AC', 'SEA_VIEW'] },
          { id: 102, number: '102', type: 'Deluxe Room', floor: 1, status: 'OCCUPIED', guest: 'John Doe', features: ['AC', 'SEA_VIEW', 'BALCONY'] },
          { id: 103, number: '103', type: 'Suite', floor: 1, status: 'MAINTENANCE', guest: null, features: ['AC', 'BALCONY'] },
          { id: 104, number: '104', type: 'Standard Room', floor: 1, status: 'RESERVED', guest: 'Jane Smith', features: ['AC'] },
          { id: 201, number: '201', type: 'Deluxe Room', floor: 2, status: 'AVAILABLE', guest: null, features: ['AC', 'SEA_VIEW', 'BALCONY', 'VIP'] },
          { id: 202, number: '202', type: 'Executive Suite', floor: 2, status: 'OCCUPIED', guest: 'Robert Johnson', features: ['AC', 'SEA_VIEW', 'BALCONY', 'VIP'] },
          { id: 203, number: '203', type: 'Standard Room', floor: 2, status: 'CLEANING', guest: null, features: ['AC'] },
        ];
        setRooms(mockRooms);
        setRoomStatusSummary({
          TOTAL: 7,
          AVAILABLE: 2,
          OCCUPIED: 2,
          MAINTENANCE: 1,
          RESERVED: 1,
          CLEANING: 1
        });
        setError(null);
      }
    } finally {
      setLoading(false);
    }
  };

  // Calculate status counts from roomStatusSummary
  const statusCounts = {
    all: roomStatusSummary?.TOTAL || rooms.length,
    AVAILABLE: roomStatusSummary?.AVAILABLE || 0,
    OCCUPIED: roomStatusSummary?.OCCUPIED || 0,
    MAINTENANCE: roomStatusSummary?.MAINTENANCE || 0,
    RESERVED: roomStatusSummary?.RESERVED || 0,
    CLEANING: roomStatusSummary?.CLEANING || 0,
  };

  // Calculate percentages
  const getPercentage = (count) => {
    const total = statusCounts.all;
    return total > 0 ? Math.round((count / total) * 100) : 0;
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'AVAILABLE':
        return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400';
      case 'OCCUPIED':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400';
      case 'MAINTENANCE':
        return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400';
      case 'RESERVED':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400';
      case 'CLEANING':
        return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400';
    }
  };

  const handleRoomClick = (room) => {
    if (room.status === 'MAINTENANCE') {
      setSelectedRoom(room);
      setMaintenanceModalOpen(true);
    } else if (room.status === 'AVAILABLE') {
      navigate('/walk-in', { state: { roomId: room.id } });
    } else if (room.status === 'OCCUPIED') {
      navigate(`/guest-detail/${room.id}`);
    } else if (room.status === 'RESERVED') {
      navigate('/checkin-confirmation', { state: { roomId: room.id } });
    }
  };

  // Handle update room status
  const handleUpdateRoomStatus = async (roomId, newStatus) => {
    try {
      await API.patch(`/rooms/${roomId}/status?status=${newStatus}`);
      alert(`Room ${roomId} status updated to ${newStatus}`);
      setMaintenanceModalOpen(false);
      setSelectedRoom(null);
      setMaintenanceReason('');
      // Refresh rooms data
      setRefreshKey(prev => prev + 1);
    } catch (err) {
      console.error('Error updating room status:', err);
      alert(`Failed to update room status: ${err.message}`);
    }
  };

  // Handle feature toggle
  const handleFeatureToggle = (featureId) => {
    setSelectedFeatures(prev => 
      prev.includes(featureId) 
        ? prev.filter(f => f !== featureId)
        : [...prev, featureId]
    );
  };

  // Handle floor toggle
  const handleFloorToggle = (floor) => {
    setSelectedFloors(prev => 
      prev.includes(floor) 
        ? prev.filter(f => f !== floor)
        : [...prev, floor]
    );
  };

  // Handle room type toggle
  const handleRoomTypeToggle = (type) => {
    setSelectedRoomTypes(prev => 
      prev.includes(type) 
        ? prev.filter(t => t !== type)
        : [...prev, type]
    );
  };

  // Clear all filters
  const clearAllFilters = () => {
    setSelectedFeatures([]);
    setSelectedFloors([]);
    setSelectedRoomTypes([]);
    setSearchQuery('');
    setStatusFilter('all');
  };

  // Extract unique room types and floors
  const roomTypes = [...new Set(rooms.map(room => room.type))];
  const floors = [...new Set(rooms.map(room => room.floor))].sort((a, b) => a - b);

  // Filter rooms based on all criteria
  const filteredRooms = rooms.filter(room => {
    // Search filter
    const matchesSearch = 
      room.number.toString().includes(searchQuery) || 
      room.type.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (room.guest && room.guest.toLowerCase().includes(searchQuery.toLowerCase()));

    // Status filter (already handled by API)
    const matchesStatus = statusFilter === 'all' || room.status === statusFilter;

    // Features filter
    const matchesFeatures = selectedFeatures.length === 0 || 
      selectedFeatures.every(feature => room.features.includes(feature));

    // Floor filter
    const matchesFloor = selectedFloors.length === 0 || selectedFloors.includes(room.floor);

    // Room type filter
    const matchesRoomType = selectedRoomTypes.length === 0 || selectedRoomTypes.includes(room.type);

    return matchesSearch && matchesStatus && matchesFeatures && matchesFloor && matchesRoomType;
  });

  // Loading state
  if (loading) {
    return (
      <div className="space-y-6">
        {/* Page Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
              Room Management
              <span className="text-sm px-2 py-1 bg-red-500 text-white rounded-full animate-pulse">LIVE</span>
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">Loading room data...</p>
          </div>
        </div>

        {/* Skeleton Loader */}
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          {[1, 2, 3, 4, 5, 6].map(i => (
            <div key={i} className="card animate-pulse">
              <div className="flex items-center justify-between mb-3">
                <div className="p-2 rounded-lg bg-gray-200 dark:bg-gray-700">
                  <div className="w-5 h-5"></div>
                </div>
                <div className="h-8 w-12 bg-gray-200 dark:bg-gray-700 rounded"></div>
              </div>
              <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded mb-1"></div>
              <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded mt-2"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  // Error state (only if no fallback data)
  if (error && !rooms.length) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
              Room Management
              <span className="text-sm px-2 py-1 bg-red-500 text-white rounded-full">ERROR</span>
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">Error loading room data</p>
          </div>
        </div>

        <div className="card bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
          <div className="flex items-start gap-3">
            <AlertCircle className="text-red-600 dark:text-red-400 mt-1" size={20} />
            <div className="flex-1">
              <h3 className="font-semibold text-red-800 dark:text-red-300">Connection Error</h3>
              <p className="text-red-600 dark:text-red-400 mt-1">{error}</p>
              <p className="text-sm text-red-500 dark:text-red-400 mt-2">
                Please check if the backend server is running at http://localhost:8080
              </p>
              <button
                onClick={fetchRoomsData}
                className="mt-4 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm"
              >
                Retry
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Maintenance Modal */}
      {maintenanceModalOpen && selectedRoom && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                Room Maintenance - {selectedRoom.number}
              </h3>
              <button
                onClick={() => {
                  setMaintenanceModalOpen(false);
                  setSelectedRoom(null);
                  setMaintenanceReason('');
                }}
                className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="space-y-4">
              <div className="p-4 bg-red-50 dark:bg-red-900/20 rounded-lg">
                <div className="flex items-center gap-3">
                  <AlertTriangle className="w-6 h-6 text-red-600 dark:text-red-400" />
                  <div>
                    <h4 className="font-medium text-red-800 dark:text-red-300">Under Maintenance</h4>
                    <p className="text-sm text-red-600 dark:text-red-400 mt-1">
                      This room is currently unavailable for bookings.
                    </p>
                  </div>
                </div>
              </div>

              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Maintenance Reason
                  </label>
                  <textarea
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                    rows="3"
                    placeholder="Enter maintenance details..."
                    value={maintenanceReason}
                    onChange={(e) => setMaintenanceReason(e.target.value)}
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <button
                    onClick={() => handleUpdateRoomStatus(selectedRoom.id, 'AVAILABLE')}
                    className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center justify-center gap-2"
                  >
                    <Check className="w-4 h-4" />
                    Mark as Available
                  </button>
                  <button
                    onClick={() => setMaintenanceModalOpen(false)}
                    className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            Room Management
            <span className="text-sm px-2 py-1 bg-red-500 text-white rounded-full animate-pulse">LIVE</span>
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            Real-time overview of all room statuses • Total: {statusCounts.all} rooms
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => setRefreshKey(prev => prev + 1)}
            className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 flex items-center gap-2"
          >
            <RefreshCw size={16} />
            Refresh
          </button>
          <button
            onClick={() => navigate('/walk-in')}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center gap-2"
          >
            <Plus size={20} />
            New Booking
          </button>
        </div>
      </div>

      {/* Status Metrics Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        {statusMetrics.map((metric) => {
          const count = statusCounts[metric.key] || 0;
          const percentage = getPercentage(count);
          const isActive = statusFilter === metric.key;
          
          return (
            <div
              key={metric.key}
              onClick={() => setStatusFilter(metric.key)}
              className={`card cursor-pointer transition-all hover:shadow-lg ${
                isActive ? 'ring-2 ring-blue-500' : ''
              }`}
            >
              <div className="flex items-center justify-between mb-3">
                <div className={`p-2 rounded-lg ${isActive ? metric.activeColor : metric.color}`}>
                  {metric.icon}
                </div>
                <span className="text-2xl font-bold text-gray-900 dark:text-white">{count}</span>
              </div>
              
              <h3 className="font-semibold text-gray-900 dark:text-white mb-1">{metric.label}</h3>
              <div className="flex items-center justify-between text-sm">
                <span className="text-gray-600 dark:text-gray-400">{percentage}%</span>
                <span className="font-medium">{count}/{statusCounts.all}</span>
              </div>
              
              {/* Progress Bar */}
              <div className="mt-2 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                <div 
                  className={`h-full ${metric.progressColor} transition-all duration-300`}
                  style={{ width: `${percentage}%` }}
                />
              </div>
              
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-2">{metric.description}</p>
            </div>
          );
        })}
      </div>

      {/* Search and Filters */}
      <div className="card">
        <div className="flex flex-col gap-4">
          {/* Search and Quick Filters */}
          <div className="flex flex-col md:flex-row gap-4 items-center">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
              <input
                type="text"
                placeholder="Search by room number, type, or guest name..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            
            <div className="flex gap-2">
              <button
                onClick={() => setIsFilterPanelOpen(!isFilterPanelOpen)}
                className={`px-4 py-2 rounded-lg font-medium transition-colors flex items-center gap-2 ${
                  isFilterPanelOpen
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600'
                }`}
              >
                <Filter size={16} />
                Advanced Filters
              </button>
            </div>
          </div>

          {/* Advanced Filters Panel */}
          {isFilterPanelOpen && (
            <div className="pt-4 border-t border-gray-200 dark:border-gray-700">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {/* Room Features Filter */}
                <div>
                  <h4 className="font-medium text-gray-900 dark:text-white mb-3">Room Features</h4>
                  <div className="space-y-2">
                    {availableFeatures.map((feature) => (
                      <label key={feature.id} className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={selectedFeatures.includes(feature.id)}
                          onChange={() => handleFeatureToggle(feature.id)}
                          className="rounded text-blue-600"
                        />
                        <div className="flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
                          {feature.icon}
                          {feature.label}
                        </div>
                      </label>
                    ))}
                  </div>
                </div>

                {/* Room Type Filter */}
                <div>
                  <h4 className="font-medium text-gray-900 dark:text-white mb-3">Room Type</h4>
                  <div className="space-y-2">
                    {roomTypes.map((type) => (
                      <label key={type} className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={selectedRoomTypes.includes(type)}
                          onChange={() => handleRoomTypeToggle(type)}
                          className="rounded text-blue-600"
                        />
                        <span className="text-sm text-gray-700 dark:text-gray-300">{type}</span>
                      </label>
                    ))}
                  </div>
                </div>

                {/* Floor Filter */}
                <div>
                  <h4 className="font-medium text-gray-900 dark:text-white mb-3">Floor</h4>
                  <div className="flex flex-wrap gap-2">
                    {floors.map((floor) => (
                      <button
                        key={floor}
                        onClick={() => handleFloorToggle(floor)}
                        className={`px-3 py-2 rounded-lg transition-colors ${
                          selectedFloors.includes(floor)
                            ? 'bg-blue-600 text-white'
                            : 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600'
                        }`}
                      >
                        Floor {floor}
                      </button>
                    ))}
                  </div>

                  {/* Filter Actions */}
                  <div className="mt-6">
                    <button
                      onClick={clearAllFilters}
                      className="w-full px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
                    >
                      Clear All Filters
                    </button>
                  </div>
                </div>
              </div>

              {/* Filter Results */}
              <div className="flex items-center justify-between pt-4 mt-4 border-t border-gray-200 dark:border-gray-700">
                <div className="text-sm text-gray-600 dark:text-gray-400">
                  Showing <span className="font-semibold text-gray-900 dark:text-white">{filteredRooms.length}</span> of {rooms.length} rooms
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Room Grid */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        {filteredRooms.map((room) => (
          <div
            key={room.id}
            onClick={() => handleRoomClick(room)}
            className={`bg-white dark:bg-gray-800 rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer border ${
              room.status === 'MAINTENANCE' 
                ? 'border-red-300 dark:border-red-700' 
                : 'border-gray-200 dark:border-gray-700'
            }`}
          >
            <div className="p-4">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <h3 className="text-xl font-bold text-gray-900 dark:text-white">Room {room.number}</h3>
                  <p className="text-sm text-gray-600 dark:text-gray-400">{room.type}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-500">Floor {room.floor}</p>
                </div>
                <DoorOpen className="text-gray-400" size={18} />
              </div>
              
              <div className="space-y-2">
                <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(room.status)}`}>
                  {room.status.charAt(0).toUpperCase() + room.status.slice(1).toLowerCase()}
                </span>
                
                {/* Room Features Icons */}
                <div className="flex items-center gap-1 pt-2">
                  {room.features.includes('AC') && (
                    <Sun className="w-4 h-4 text-gray-400" title="AC" />
                  )}
                  {room.features.includes('SEA_VIEW') && (
                    <Waves className="w-4 h-4 text-blue-400" title="Sea View" />
                  )}
                  {room.features.includes('BALCONY') && (
                    <DoorOpen className="w-4 h-4 text-green-400" title="Balcony" />
                  )}
                  {room.features.includes('VIP') && (
                    <Crown className="w-4 h-4 text-yellow-400" title="VIP" />
                  )}
                </div>
                
                {room.guest && (
                  <div className="pt-2 border-t border-gray-200 dark:border-gray-700">
                    <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{room.guest}</p>
                    {room.checkIn && room.checkOut && (
                      <p className="text-xs text-gray-600 dark:text-gray-400">
                        {room.checkIn} - {room.checkOut}
                      </p>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      {filteredRooms.length === 0 && (
        <div className="text-center py-12">
          <DoorOpen className="mx-auto text-gray-400 mb-4" size={48} />
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">No rooms found</h3>
          <p className="text-gray-600 dark:text-gray-400 mt-1 mb-4">
            Try adjusting your filters or search criteria
          </p>
          <button
            onClick={clearAllFilters}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Clear all filters
          </button>
        </div>
      )}

      {/* Room Statistics Summary */}
      {roomStatusSummary && Object.keys(roomStatusSummary).length > 0 && (
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Room Statistics</h2>
            <BarChart3 className="text-gray-400" size={20} />
          </div>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {Object.entries(roomStatusSummary).map(([key, value]) => (
              <div key={key} className="text-center p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{value}</p>
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  {key.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default Rooms;