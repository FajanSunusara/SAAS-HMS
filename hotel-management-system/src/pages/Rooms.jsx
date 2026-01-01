import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  DoorOpen, 
  Plus, 
  Search, 
  Filter,
  CheckCircle,
  XCircle,
  AlertTriangle,
  Calendar,
  Users,
  Bed,
  Building,
  Home,
  Waves,
  Sun,
  Accessibility,  
  Clock,
  Crown,
  AlertCircle,
  Check,
  X
} from 'lucide-react';

const Rooms = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [isFilterPanelOpen, setIsFilterPanelOpen] = useState(false);
  const [selectedFeatures, setSelectedFeatures] = useState([]);
  const [selectedFloors, setSelectedFloors] = useState([]);
  const [selectedRoomTypes, setSelectedRoomTypes] = useState([]);
  const [maintenanceModalOpen, setMaintenanceModalOpen] = useState(false);
  const [selectedMaintenanceRoom, setSelectedMaintenanceRoom] = useState(null);

  // Status metrics data
  const statusMetrics = [
    {
      key: 'all',
      label: 'All Rooms',
      icon: <Bed className="w-5 h-5" />,
      color: 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200',
      activeColor: 'bg-gray-600 text-white dark:bg-gray-700',
      progressColor: 'bg-gray-400'
    },
    {
      key: 'available',
      label: 'Available',
      icon: <CheckCircle className="w-5 h-5" />,
      color: 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400',
      activeColor: 'bg-green-600 text-white dark:bg-green-700',
      progressColor: 'bg-green-400'
    },
    {
      key: 'occupied',
      label: 'Occupied',
      icon: <Users className="w-5 h-5" />,
      color: 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400',
      activeColor: 'bg-blue-600 text-white dark:bg-blue-700',
      progressColor: 'bg-blue-400'
    },
    {
      key: 'maintenance',
      label: 'Maintenance',
      icon: <AlertTriangle className="w-5 h-5" />,
      color: 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400',
      activeColor: 'bg-red-600 text-white dark:bg-red-700',
      progressColor: 'bg-red-400'
    },
    {
      key: 'reserved',
      label: 'Reserved',
      icon: <Calendar className="w-5 h-5" />,
      color: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400',
      activeColor: 'bg-yellow-600 text-white dark:bg-yellow-700',
      progressColor: 'bg-yellow-400'
    },
    {
      key: 'cleaning',
      label: 'Cleaning',
      icon: <Waves className="w-5 h-5" />,
      color: 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400',
      activeColor: 'bg-purple-600 text-white dark:bg-purple-700',
      progressColor: 'bg-purple-400'
    }
  ];

  // Features available for filtering
 const availableFeatures = [
  { id: 'ac', label: 'AC', icon: <Sun className="w-4 h-4" /> },
  { id: 'sea-view', label: 'Sea View', icon: <Waves className="w-4 h-4" /> },
  { id: 'balcony', label: 'Balcony', icon: <Home className="w-4 h-4" /> },
  { id: 'accessible', label: 'Accessible', icon: <Accessibility className="w-4 h-4" /> }, // Changed here
  { id: 'vip', label: 'VIP', icon: <Crown className="w-4 h-4" /> }
];

  // Room types for filtering
  const roomTypes = ['Standard Room', 'Deluxe Room', 'Suite', 'Executive Suite', 'Presidential Suite'];

  // Room data with additional features
  const rooms = [
    { 
      id: 101, 
      number: '101', 
      type: 'Standard Room', 
      floor: 1, 
      status: 'available', 
      guest: null, 
      checkIn: null, 
      checkOut: null,
      features: ['ac', 'sea-view'],
      needsAttention: false,
      checkoutToday: false,
      lateCheckout: false
    },
    { 
      id: 102, 
      number: '102', 
      type: 'Deluxe Room', 
      floor: 1, 
      status: 'occupied', 
      guest: 'John Doe', 
      checkIn: '2024-12-20', 
      checkOut: '2024-12-25',
      features: ['ac', 'sea-view', 'balcony'],
      needsAttention: false,
      checkoutToday: true,
      lateCheckout: false,
      isVIP: true
    },
    { 
      id: 103, 
      number: '103', 
      type: 'Suite', 
      floor: 1, 
      status: 'maintenance', 
      guest: null, 
      checkIn: null, 
      checkOut: null,
      features: ['ac', 'balcony', 'accessible'],
      needsAttention: true,
      checkoutToday: false,
      lateCheckout: false
    },
    { 
      id: 104, 
      number: '104', 
      type: 'Standard Room', 
      floor: 1, 
      status: 'reserved', 
      guest: 'Jane Smith', 
      checkIn: '2024-12-22', 
      checkOut: '2024-12-24',
      features: ['ac'],
      needsAttention: false,
      checkoutToday: false,
      lateCheckout: false
    },
    { 
      id: 201, 
      number: '201', 
      type: 'Deluxe Room', 
      floor: 2, 
      status: 'available', 
      guest: null, 
      checkIn: null, 
      checkOut: null,
      features: ['ac', 'sea-view', 'balcony', 'vip'],
      needsAttention: false,
      checkoutToday: false,
      lateCheckout: false
    },
    { 
      id: 202, 
      number: '202', 
      type: 'Executive Suite', 
      floor: 2, 
      status: 'occupied', 
      guest: 'Robert Johnson', 
      checkIn: '2024-12-19', 
      checkOut: '2024-12-23',
      features: ['ac', 'sea-view', 'balcony', 'accessible', 'vip'],
      needsAttention: false,
      checkoutToday: false,
      lateCheckout: true
    },
    { 
      id: 203, 
      number: '203', 
      type: 'Standard Room', 
      floor: 2, 
      status: 'cleaning', 
      guest: null, 
      checkIn: null, 
      checkOut: null,
      features: ['ac'],
      needsAttention: false,
      checkoutToday: false,
      lateCheckout: false
    },
    { 
      id: 301, 
      number: '301', 
      type: 'Presidential Suite', 
      floor: 3, 
      status: 'reserved', 
      guest: 'Alice Brown', 
      checkIn: '2024-12-21', 
      checkOut: '2024-12-26',
      features: ['ac', 'sea-view', 'balcony', 'accessible', 'vip'],
      needsAttention: false,
      checkoutToday: false,
      lateCheckout: false
    },
    { 
      id: 304, 
      number: '301', 
      type: 'Presidential Suite', 
      floor: 3, 
      status: 'reserved', 
      guest: 'Alice Brown', 
      checkIn: '2024-12-21', 
      checkOut: '2024-12-26',
      features: ['ac', 'sea-view', 'balcony', 'accessible', 'vip'],
      needsAttention: false,
      checkoutToday: false,
      lateCheckout: false
    },
    { 
      id: 302, 
      number: '301', 
      type: 'Presidential Suite', 
      floor: 3, 
      status: 'reserved', 
      guest: 'Alice Brown', 
      checkIn: '2024-12-21', 
      checkOut: '2024-12-26',
      features: ['ac', 'sea-view', 'balcony', 'accessible', 'vip'],
      needsAttention: false,
      checkoutToday: false,
      lateCheckout: false
    },
    { 
      id: 303, 
      number: '301', 
      type: 'Presidential Suite', 
      floor: 3, 
      status: 'reserved', 
      guest: 'Alice Brown', 
      checkIn: '2024-12-21', 
      checkOut: '2024-12-26',
      features: ['ac', 'sea-view', 'balcony', 'accessible', 'vip'],
      needsAttention: false,
      checkoutToday: false,
      lateCheckout: false
    },
  ];

  // Calculate status counts
  const statusCounts = {
    all: rooms.length,
    available: rooms.filter(r => r.status === 'available').length,
    occupied: rooms.filter(r => r.status === 'occupied').length,
    maintenance: rooms.filter(r => r.status === 'maintenance').length,
    reserved: rooms.filter(r => r.status === 'reserved').length,
    cleaning: rooms.filter(r => r.status === 'cleaning').length,
  };

  // Calculate percentages
  const getPercentage = (count) => {
    return Math.round((count / rooms.length) * 100);
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'available':
        return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400';
      case 'occupied':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400';
      case 'maintenance':
        return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400';
      case 'reserved':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400';
      case 'cleaning':
        return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400';
    }
  };

  const handleRoomClick = (room) => {
    // Navigate based on room status
    if (room.status === 'available') {
      navigate('/walk-in');
    } else if (room.status === 'occupied') {
      navigate(`/guest-detail/${room.id}`);
    } else if (room.status === 'maintenance') {
      setSelectedMaintenanceRoom(room);
      setMaintenanceModalOpen(true);
    } else if (room.status === 'reserved') {
      navigate('/checkin-confirmation');
    }
  };

  // Handle remove from maintenance
  const handleRemoveFromMaintenance = (roomId) => {
    // API: PATCH /api/rooms/{roomId}/status - Update room status to 'available'
    console.log(`Removing room ${roomId} from maintenance`);
    // In a real app, you would update the state or make an API call here
    alert(`Room ${roomId} has been removed from maintenance and is now available.`);
    setMaintenanceModalOpen(false);
    setSelectedMaintenanceRoom(null);
  };

  // Handle feature toggle
  const handleFeatureToggle = (featureId) => {
    if (selectedFeatures.includes(featureId)) {
      setSelectedFeatures(selectedFeatures.filter(f => f !== featureId));
    } else {
      setSelectedFeatures([...selectedFeatures, featureId]);
    }
  };

  // Handle floor toggle
  const handleFloorToggle = (floor) => {
    if (selectedFloors.includes(floor)) {
      setSelectedFloors(selectedFloors.filter(f => f !== floor));
    } else {
      setSelectedFloors([...selectedFloors, floor]);
    }
  };

  // Handle room type toggle
  const handleRoomTypeToggle = (type) => {
    if (selectedRoomTypes.includes(type)) {
      setSelectedRoomTypes(selectedRoomTypes.filter(t => t !== type));
    } else {
      setSelectedRoomTypes([...selectedRoomTypes, type]);
    }
  };

  // Clear all filters
  const clearAllFilters = () => {
    setSelectedFeatures([]);
    setSelectedFloors([]);
    setSelectedRoomTypes([]);
    setSearchQuery('');
    setStatusFilter('all');
  };

  // Filter rooms based on all criteria
  const filteredRooms = rooms.filter(room => {
    // Search filter
    const matchesSearch = 
      room.number.includes(searchQuery) || 
      room.type.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (room.guest && room.guest.toLowerCase().includes(searchQuery.toLowerCase()));

    // Status filter
    const matchesStatus = statusFilter === 'all' || room.status === statusFilter;

    // Features filter
    const matchesFeatures = selectedFeatures.length === 0 || 
      selectedFeatures.every(feature => room.features.includes(feature));

    // Floor filter
    const matchesFloor = selectedFloors.length === 0 || selectedFloors.includes(room.floor);

    // Room type filter
    const matchesRoomType = selectedRoomTypes.length === 0 || selectedRoomTypes.includes(room.type);

    // Quick filters
    const matchesQuickFilters = true; // Add your quick filter logic here

    return matchesSearch && matchesStatus && matchesFeatures && matchesFloor && matchesRoomType && matchesQuickFilters;
  });

  // Calculate filtered count
  const filteredCount = filteredRooms.length;

  return (
    <div className="space-y-6">
      {/* Maintenance Modal */}
      {maintenanceModalOpen && selectedMaintenanceRoom && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                Maintenance - Room {selectedMaintenanceRoom.number}
              </h3>
              <button
                onClick={() => setMaintenanceModalOpen(false)}
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
                    Maintenance Reason (Optional)
                  </label>
                  <textarea
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                    rows="3"
                    placeholder="Enter maintenance details..."
                    defaultValue="Plumbing repair and room refurbishment"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Maintenance Duration
                  </label>
                  <select className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white">
                    <option>2 hours (Quick Clean)</option>
                    <option selected>1 day (Minor Repair)</option>
                    <option>3 days (Major Repair)</option>
                    <option>1 week (Renovation)</option>
                  </select>
                </div>
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  onClick={() => handleRemoveFromMaintenance(selectedMaintenanceRoom.id)}
                  className="flex-1 btn-primary flex items-center justify-center gap-2"
                >
                  <Check className="w-4 h-4" />
                  Remove from Maintenance
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
      )}

      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            Room Management
            <span className="text-sm px-2 py-1 bg-red-500 text-white rounded-full animate-pulse">LIVE</span>
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Real-time overview of all room statuses</p>
        </div>
        <button
          onClick={() => navigate('/walk-in')}
          className="btn-primary flex items-center gap-2"
        >
          <Plus size={20} />
          New Booking / Walk-in
        </button>
      </div>

      {/* Status Metrics Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        {statusMetrics.map((metric) => {
          const count = statusCounts[metric.key];
          const percentage = getPercentage(count);
          const isActive = statusFilter === metric.key;
          
          return (
            <div
              key={metric.key}
              onClick={() => setStatusFilter(metric.key)}
              className={`card cursor-pointer transition-all hover:shadow-lg ${
                isActive ? 'ring-2 ring-primary-500' : ''
              }`}
            >
              <div className="flex items-center justify-between mb-3">
                <div className={`p-2 rounded-lg ${isActive ? metric.activeColor : metric.color}`}>
                  {metric.icon}
                </div>
                <span className="text-2xl font-bold">{count}</span>
              </div>
              
              <h3 className="font-semibold text-gray-900 dark:text-white mb-1">{metric.label}</h3>
              <div className="flex items-center justify-between text-sm">
                <span className="text-gray-600 dark:text-gray-400">{percentage}%</span>
                <span className="font-medium">{count}/{rooms.length}</span>
              </div>
              
              {/* Progress Bar */}
              <div className="mt-2 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                <div 
                  className={`h-full ${metric.progressColor} transition-all duration-300`}
                  style={{ width: `${percentage}%` }}
                />
              </div>
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
                className="input-field pl-10"
              />
            </div>
            
            <div className="flex gap-2 flex-wrap">
              <button
                onClick={() => setIsFilterPanelOpen(!isFilterPanelOpen)}
                className={`px-4 py-2 rounded-lg font-medium transition-colors flex items-center gap-2 ${
                  isFilterPanelOpen
                    ? 'bg-primary-600 text-white'
                    : 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600'
                }`}
              >
                <Filter size={16} />
                Advanced Filters
              </button>
              
              {/* Quick Filters */}
              <button className="px-3 py-2 rounded-lg font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400 hover:bg-yellow-200 dark:hover:bg-yellow-900/30 transition-colors flex items-center gap-2">
                <Clock size={16} />
                Checkout Today
              </button>
              <button className="px-3 py-2 rounded-lg font-medium bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400 hover:bg-purple-200 dark:hover:bg-purple-900/30 transition-colors flex items-center gap-2">
                <Crown size={16} />
                VIP
              </button>
              <button className="px-3 py-2 rounded-lg font-medium bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400 hover:bg-orange-200 dark:hover:bg-orange-900/30 transition-colors flex items-center gap-2">
                <Clock size={16} />
                Late Checkout
              </button>
              <button className="px-3 py-2 rounded-lg font-medium bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400 hover:bg-red-200 dark:hover:bg-red-900/30 transition-colors flex items-center gap-2">
                <AlertCircle size={16} />
                Needs Attention
              </button>
            </div>
          </div>

          {/* Advanced Filters Panel */}
          {isFilterPanelOpen && (
            <div className="pt-4 border-t border-gray-200 dark:border-gray-700">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {/* Room Status Multi-select */}
                <div>
                  <h4 className="font-medium text-gray-900 dark:text-white mb-3">Room Status</h4>
                  <div className="space-y-2">
                    {statusMetrics.slice(1).map((metric) => (
                      <label key={metric.key} className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={selectedFeatures.includes(metric.key)}
                          onChange={() => handleFeatureToggle(metric.key)}
                          className="rounded text-primary-600"
                        />
                        <div className={`px-2 py-1 rounded text-xs font-medium ${metric.color}`}>
                          {metric.label}
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
                          className="rounded text-primary-600"
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
                    {[1, 2, 3].map((floor) => (
                      <button
                        key={floor}
                        onClick={() => handleFloorToggle(floor)}
                        className={`px-3 py-2 rounded-lg transition-colors ${
                          selectedFloors.includes(floor)
                            ? 'bg-primary-600 text-white'
                            : 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600'
                        }`}
                      >
                        Floor {floor}
                      </button>
                    ))}
                  </div>

                  {/* Features Filter */}
                  <h4 className="font-medium text-gray-900 dark:text-white mt-4 mb-3">Room Features</h4>
                  <div className="space-y-2">
                    {availableFeatures.map((feature) => (
                      <label key={feature.id} className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={selectedFeatures.includes(feature.id)}
                          onChange={() => handleFeatureToggle(feature.id)}
                          className="rounded text-primary-600"
                        />
                        <div className="flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
                          {feature.icon}
                          {feature.label}
                        </div>
                      </label>
                    ))}
                  </div>
                </div>
              </div>

              {/* Filter Actions */}
              <div className="flex items-center justify-between pt-4 mt-4 border-t border-gray-200 dark:border-gray-700">
                <div className="text-sm text-gray-600 dark:text-gray-400">
                  Showing <span className="font-semibold text-gray-900 dark:text-white">{filteredCount}</span> of {rooms.length} rooms
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={clearAllFilters}
                    className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
                  >
                    Clear All Filters
                  </button>
                  <button
                    onClick={() => console.log('Save preset')}
                    className="px-4 py-2 text-sm font-medium bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 rounded-lg transition-colors"
                  >
                    Save Filter Preset
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

     {/* Room Grid */}
<div className="grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-2">
  {filteredRooms.map((room) => (
    <div
      key={room.id}
      onClick={() => handleRoomClick(room)}
      className={`bg-white dark:bg-gray-800 rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer p-3 border border-gray-100 dark:border-gray-700 ${
        room.needsAttention ? 'ring-1 ring-red-500' : ''
      }`}
    >
      <div className="flex items-start justify-between mb-2">
        <div>
          <h3 className="text-lg font-bold">Room {room.number}</h3>
          <p className="text-xs text-gray-600 dark:text-gray-400">{room.type}</p>
          <p className="text-xs text-gray-500 dark:text-gray-500">Floor {room.floor}</p>
        </div>
        <div className="flex flex-col items-end gap-1">
          <DoorOpen className="text-gray-400" size={18} />
        </div>
      </div>
      
      <div className="space-y-1.5">
        <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${getStatusColor(room.status)}`}>
          {room.status.charAt(0).toUpperCase() + room.status.slice(1)}
        </span>
        
        {/* Room Features Icons */}
        <div className="flex items-center gap-1 pt-1">
          {room.features.includes('ac') && (
            <Sun className="w-3 h-3 text-gray-400" title="AC" />
          )}
          {room.features.includes('sea-view') && (
            <Waves className="w-3 h-3 text-blue-400" title="Sea View" />
          )}
          {room.features.includes('balcony') && (
            <Home className="w-3 h-3 text-green-400" title="Balcony" />
          )}
          {room.features.includes('accessible') && (
            <Users className="w-3 h-3 text-purple-400" title="Accessible" />
          )}
          {room.features.includes('vip') && (
            <Crown className="w-3 h-3 text-yellow-400" title="VIP" />
          )}
        </div>
        
        {room.guest && (
          <div className="pt-1 border-t border-gray-200 dark:border-gray-700">
            <p className="text-xs font-medium truncate">{room.guest}</p>
            <p className="text-xs text-gray-600 dark:text-gray-400 truncate">
              {room.checkIn} - {room.checkOut}
            </p>
          </div>
        )}
      </div>
    </div>
  ))}
</div>

      {filteredRooms.length === 0 && (
        <div className="text-center py-12">
          <DoorOpen className="mx-auto text-gray-400 mb-4" size={48} />
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">No rooms found</h3>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            Try adjusting your filters or search criteria
          </p>
          <button
            onClick={clearAllFilters}
            className="mt-4 px-4 py-2 text-sm font-medium text-primary-600 hover:text-primary-700 dark:text-primary-400"
          >
            Clear all filters
          </button>
        </div>
      )}
    </div>
  );
};

export default Rooms;