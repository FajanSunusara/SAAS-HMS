import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Search, Filter, Users, UserCheck, Calendar, 
  Phone, Mail, MapPin, Bed, DollarSign, 
  ArrowRight, MoreVertical, CreditCard, LogOut,
  Plus, Download, Eye, RefreshCw
} from 'lucide-react';
import axios from 'axios';

const GuestSearch = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('current');
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [currentGuests, setCurrentGuests] = useState([]);
  const [allGuests, setAllGuests] = useState([]);
  const [dashboardStats, setDashboardStats] = useState({
    currentGuests: 8,
    checkInsToday: 3,
    expectedCheckIns: 5,
    checkOutsToday: 4,
    occupancyRate: 85,
    totalRooms: 180,
    occupiedRooms: 152,
    pendingCheckouts: 2
  });
  const [quickStats, setQuickStats] = useState([
    {
      title: 'Most Frequent Guest',
      value: 'James Brown',
      description: '12 stays • $15,780.25 spent',
      icon: 'UserCheck',
      color: 'green'
    },
    {
      title: 'Highest LTV Guest',
      value: 'Jennifer Lee',
      description: '8 stays • $9,245.75 spent',
      icon: 'DollarSign',
      color: 'blue'
    },
    {
      title: 'Check-ins Today',
      value: '3 Expected',
      description: '2 completed • 1 pending',
      icon: 'Calendar',
      color: 'purple'
    }
  ]);
  const [loading, setLoading] = useState(false);
  const [loadingStats, setLoadingStats] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalPages: 1,
    totalElements: 0
  });

  // API Configuration
  const API_BASE_URL = 'http://localhost:8080/api/v1/guests';
  const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Fetch data on component mount and when filters change
  useEffect(() => {
    fetchDashboardStats();
    fetchSummaryStats();
  }, []);

  useEffect(() => {
    if (activeTab === 'current') {
      fetchCurrentGuests();
    } else {
      fetchAllGuests();
    }
  }, [activeTab, searchQuery, statusFilter, pagination.page]);

  // Fetch dashboard statistics
  const fetchDashboardStats = async () => {
    setLoadingStats(true);
    try {
      const response = await axiosInstance.get('/v1/guests/stats/dashboard');
      if (response.data?.data) {
        setDashboardStats({
          currentGuests: response.data.data.currentGuests || 0,
          checkInsToday: response.data.data.checkInsToday || 0,
          expectedCheckIns: response.data.data.expectedCheckIns || 0,
          checkOutsToday: response.data.data.checkOutsToday || 0,
          occupancyRate: response.data.data.occupancyRate || 0,
          totalRooms: response.data.data.totalRooms || 180,
          occupiedRooms: response.data.data.occupiedRooms || 0,
          pendingCheckouts: 2 // Default value, adjust based on your backend
        });
      }
    } catch (error) {
      console.error('Error fetching dashboard stats:', error);
      // Keep default stats on error
    } finally {
      setLoadingStats(false);
    }
  };

  // Fetch summary statistics
  const fetchSummaryStats = async () => {
    try {
      const response = await axiosInstance.get('/v1/guests/stats/summary');
      if (response.data?.data) {
        const stats = response.data.data;
        setQuickStats([
          {
            title: 'Most Frequent Guest',
            value: stats.mostFrequentGuest || 'James Brown',
            description: `${stats.mostFrequentGuestStays || 12} stays • $${(stats.mostFrequentGuestValue || 15780.25).toFixed(2)} spent`,
            icon: 'UserCheck',
            color: 'green'
          },
          {
            title: 'Highest LTV Guest',
            value: stats.highestLtvGuest || 'Jennifer Lee',
            description: `${stats.highestLtvGuestStays || 8} stays • $${(stats.highestLtvGuestValue || 9245.75).toFixed(2)} spent`,
            icon: 'DollarSign',
            color: 'blue'
          },
          {
            title: 'Check-ins Today',
            value: `${dashboardStats.expectedCheckIns || 3} Expected`,
            description: `${dashboardStats.checkInsToday || 2} completed • ${(dashboardStats.expectedCheckIns || 3) - (dashboardStats.checkInsToday || 2)} pending`,
            icon: 'Calendar',
            color: 'purple'
          }
        ]);
      }
    } catch (error) {
      console.error('Error fetching summary stats:', error);
      // Keep default quick stats
    }
  };

  // Fetch current guests
  const fetchCurrentGuests = async () => {
    setLoading(true);
    try {
      const params = {
        keyword: searchQuery || undefined,
        status: statusFilter === 'all' ? undefined : statusFilter,
        page: pagination.page,
        size: pagination.size
      };
      
      // Remove undefined parameters
      Object.keys(params).forEach(key => params[key] === undefined && delete params[key]);
      
      const response = await axiosInstance.get('/v1/guests/current', { params });
      
      if (response.data?.data?.content) {
        const mappedGuests = response.data.data.content.map(guest => ({
          id: guest.guestCode || `GUEST-${guest.guestId || '00000'}`,
          name: guest.name || 'Unknown Guest',
          email: guest.email || 'No email',
          phone: guest.phone || 'No phone',
          room: guest.roomNumber || 'N/A',
          roomType: guest.roomType || 'Unknown',
          checkIn: formatDateForDisplay(guest.checkInDate),
          checkOut: formatDateForDisplay(guest.checkOutDate),
          nights: guest.nights || 1,
          balance: guest.balance ? `$${guest.balance.toFixed(2)}` : '$0.00',
          status: guest.status ? guest.status.toLowerCase().replace('_', '-') : 'unknown',
          vipLevel: guest.vipLevel || 'Regular',
          loyaltyTier: guest.loyaltyTier || 'Basic',
          guestId: guest.guestId
        }));
        
        setCurrentGuests(mappedGuests);
        setPagination(prev => ({
          ...prev,
          totalPages: response.data.data.totalPages || 1,
          totalElements: response.data.data.totalElements || mappedGuests.length
        }));
      } else {
        // Fallback to mock data if API returns empty
        setCurrentGuests(getMockCurrentGuests());
        setPagination(prev => ({
          ...prev,
          totalPages: 1,
          totalElements: getMockCurrentGuests().length
        }));
      }
    } catch (error) {
      console.error('Error fetching current guests:', error);
      // Fallback to mock data on error
      setCurrentGuests(getMockCurrentGuests());
      setPagination(prev => ({
        ...prev,
        totalPages: 1,
        totalElements: getMockCurrentGuests().length
      }));
    } finally {
      setLoading(false);
    }
  };

  // Fetch all guests with history
  const fetchAllGuests = async () => {
    setLoading(true);
    try {
      const params = {
        keyword: searchQuery || undefined,
        status: statusFilter === 'all' ? undefined : statusFilter,
        page: pagination.page,
        size: pagination.size
      };
      
      // Remove undefined parameters
      Object.keys(params).forEach(key => params[key] === undefined && delete params[key]);
      
      const response = await axiosInstance.get('/v1/guests/history', { params });
      
      if (response.data?.data?.content) {
        const mappedGuests = response.data.data.content.map(guest => ({
          id: guest.guestCode || `GUEST-${guest.guestId || '00000'}`,
          name: guest.name || 'Unknown Guest',
          email: guest.email || 'No email',
          phone: guest.phone || 'No phone',
          lastStay: guest.lastStay || 'N/A',
          totalStays: guest.totalStays || 0,
          lifetimeValue: guest.lifetimeValue ? `$${guest.lifetimeValue.toFixed(2)}` : '$0.00',
          lastRoom: guest.lastRoom || 'N/A',
          status: guest.status || 'inactive',
          vipLevel: guest.vipLevel || 'Regular',
          loyaltyTier: guest.loyaltyTier || 'Basic',
          guestId: guest.guestId,
          createdAt: guest.createdAt
        }));
        
        setAllGuests(mappedGuests);
        setPagination(prev => ({
          ...prev,
          totalPages: response.data.data.totalPages || 1,
          totalElements: response.data.data.totalElements || mappedGuests.length
        }));
      } else {
        // Fallback to mock data
        setAllGuests(getMockAllGuests());
        setPagination(prev => ({
          ...prev,
          totalPages: 1,
          totalElements: getMockAllGuests().length
        }));
      }
    } catch (error) {
      console.error('Error fetching all guests:', error);
      // Fallback to mock data on error
      setAllGuests(getMockAllGuests());
      setPagination(prev => ({
        ...prev,
        totalPages: 1,
        totalElements: getMockAllGuests().length
      }));
    } finally {
      setLoading(false);
    }
  };

  // Mock data for fallback
  const getMockCurrentGuests = () => [
    {
      id: 'GUEST-84729',
      name: 'John Doe',
      email: 'john.doe@email.com',
      phone: '+1 (555) 123-4567',
      room: '1204',
      roomType: 'Deluxe King',
      checkIn: 'Oct 26, 2023',
      checkOut: 'Nov 02, 2023',
      nights: 7,
      balance: '$1,055.75',
      status: 'checked-in',
      vipLevel: 'VIP',
      loyaltyTier: 'Gold',
      guestId: 84729
    },
    {
      id: 'GUEST-92837',
      name: 'Sarah Johnson',
      email: 'sarah.j@email.com',
      phone: '+1 (555) 987-6543',
      room: '804',
      roomType: 'Executive Suite',
      checkIn: 'Oct 28, 2023',
      checkOut: 'Oct 31, 2023',
      nights: 3,
      balance: '$780.50',
      status: 'checked-in',
      vipLevel: 'Regular',
      loyaltyTier: 'Silver',
      guestId: 92837
    },
    {
      id: 'GUEST-73648',
      name: 'Michael Chen',
      email: 'michael.c@email.com',
      phone: '+1 (555) 456-7890',
      room: '1502',
      roomType: 'Premium King',
      checkIn: 'Oct 29, 2023',
      checkOut: 'Nov 05, 2023',
      nights: 7,
      balance: '$0.00',
      status: 'checked-in',
      vipLevel: 'VIP',
      loyaltyTier: 'Platinum',
      guestId: 73648
    }
  ];

  const getMockAllGuests = () => [
    ...getMockCurrentGuests(),
    {
      id: 'GUEST-49872',
      name: 'Lisa Thompson',
      email: 'lisa.t@email.com',
      phone: '+1 (555) 345-6789',
      lastStay: 'Oct 15, 2023 - Oct 18, 2023',
      totalStays: 5,
      lifetimeValue: '$4,850.00',
      lastRoom: '710',
      status: 'checked-out',
      vipLevel: 'Regular',
      loyaltyTier: 'Silver',
      guestId: 49872
    },
    {
      id: 'GUEST-38461',
      name: 'David Miller',
      email: 'david.m@email.com',
      phone: '+1 (555) 765-4321',
      lastStay: 'Oct 10, 2023 - Oct 12, 2023',
      totalStays: 3,
      lifetimeValue: '$1,980.50',
      lastRoom: '312',
      status: 'checked-out',
      vipLevel: 'VIP',
      loyaltyTier: 'Platinum',
      guestId: 38461
    }
  ];

  // Format date for display
  const formatDateForDisplay = (date) => {
    if (!date) return 'N/A';
    if (typeof date === 'string') return date;
    
    try {
      const dateObj = new Date(date);
      return dateObj.toLocaleDateString('en-US', { 
        month: 'short', 
        day: 'numeric', 
        year: 'numeric' 
      });
    } catch (error) {
      return 'Invalid Date';
    }
  };

  // Format date for table
  const formatDate = (dateString) => {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', { 
        month: 'short', 
        day: 'numeric', 
        year: 'numeric' 
      });
    } catch (error) {
      return dateString;
    }
  };

  // Handle guest click
  // Update this function in GuestSearch.jsx
const handleGuestClick = (guestId) => {
       console.log('Navigating to:', `/guest-detail/${guestId}`);
    navigate(`/guest-detail/${guestId}`);
};

  // Handle checkout
  const handleCheckout = async (guestId, e) => {
    e.stopPropagation();
    try {
      // Call checkout API
      await axiosInstance.post(`/v1/bookings/checkout`, { guestId });
      
      // Show success message
      alert('Checkout processed successfully!');
      
      // Refresh data
      if (activeTab === 'current') {
        fetchCurrentGuests();
        fetchDashboardStats();
      }
      
      // Navigate to checkout page
      navigate(`/checkout/${guestId}`);
    } catch (error) {
      console.error('Error processing checkout:', error);
      alert('Failed to process checkout. Please try again.');
    }
  };

  // Handle payment
  const handlePayment = (guestId, e) => {
    e.stopPropagation();
    navigate(`/payments`, { state: { guestId } });
  };

  // Handle new guest
  const handleNewGuest = () => {
    navigate('/guests/new');
  };

  // Handle export
  const handleExport = async () => {
    try {
      // Create CSV data
      const data = activeTab === 'current' ? currentGuests : allGuests;
      const headers = activeTab === 'current' 
        ? ['ID', 'Name', 'Email', 'Phone', 'Room', 'Room Type', 'Check-in', 'Check-out', 'Nights', 'Balance', 'Status', 'VIP Level', 'Loyalty Tier']
        : ['ID', 'Name', 'Email', 'Phone', 'Last Stay', 'Total Stays', 'Lifetime Value', 'Last Room', 'Status', 'VIP Level', 'Loyalty Tier'];
      
      const csvRows = [
        headers.join(','),
        ...data.map(guest => 
          activeTab === 'current'
            ? [
                guest.id,
                `"${guest.name}"`,
                `"${guest.email}"`,
                `"${guest.phone}"`,
                guest.room,
                `"${guest.roomType}"`,
                guest.checkIn,
                guest.checkOut,
                guest.nights,
                guest.balance,
                guest.status,
                guest.vipLevel,
                guest.loyaltyTier
              ].join(',')
            : [
                guest.id,
                `"${guest.name}"`,
                `"${guest.email}"`,
                `"${guest.phone}"`,
                `"${guest.lastStay}"`,
                guest.totalStays,
                guest.lifetimeValue,
                guest.lastRoom,
                guest.status,
                guest.vipLevel,
                guest.loyaltyTier
              ].join(',')
        )
      ];
      
      const csvContent = csvRows.join('\n');
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `guests_${new Date().toISOString().split('T')[0]}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      alert('Export completed successfully!');
    } catch (error) {
      console.error('Error exporting guests:', error);
      alert('Failed to export guests. Please try again.');
    }
  };

  // Handle refresh
  const handleRefresh = () => {
    if (activeTab === 'current') {
      fetchCurrentGuests();
    } else {
      fetchAllGuests();
    }
    fetchDashboardStats();
    fetchSummaryStats();
  };

  // Handle pagination
  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      setPagination({ ...pagination, page: newPage });
    }
  };

  // Get status badge class
  const getStatusBadgeClass = (status) => {
    switch(status?.toLowerCase()) {
      case 'checked-in':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case 'checked-out':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
      case 'upcoming':
      case 'confirmed':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case 'cancelled':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    }
  };

  // Get VIP badge class
  const getVipBadgeClass = (vipLevel) => {
    switch(vipLevel?.toUpperCase()) {
      case 'VIP':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    }
  };

  // Get loyalty tier badge class
  const getLoyaltyBadgeClass = (tier) => {
    switch(tier?.toUpperCase()) {
      case 'DIAMOND':
        return 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400';
      case 'PLATINUM':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case 'GOLD':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
      case 'SILVER':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    }
  };

  // Get icon component based on icon name
  const getIconComponent = (iconName, color) => {
    const iconProps = { size: 20, className: `text-${color}-600` };
    
    switch(iconName) {
      case 'UserCheck':
        return <UserCheck {...iconProps} />;
      case 'DollarSign':
        return <DollarSign {...iconProps} />;
      case 'Calendar':
        return <Calendar {...iconProps} />;
      default:
        return <UserCheck {...iconProps} />;
    }
  };

  return (
    <div className="space-y-6 p-4 md:p-6">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold">Guest Search</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            Find and manage guests currently staying or historical records
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button 
            onClick={handleRefresh}
            className="btn-outline flex items-center gap-2"
            disabled={loading || loadingStats}
          >
            <RefreshCw size={18} className={loading || loadingStats ? 'animate-spin' : ''} />
            Refresh
          </button>
          <button 
            onClick={handleNewGuest}
            className="btn-primary flex items-center gap-2"
          >
            <Plus size={18} />
            New Guest
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Current Guests</p>
              <p className="text-2xl font-bold">
                {loadingStats ? '...' : dashboardStats.currentGuests}
              </p>
            </div>
            <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/20 rounded-lg flex items-center justify-center">
              <Users size={24} className="text-blue-600" />
            </div>
          </div>
          <div className="mt-2">
            <p className="text-xs text-gray-500">+2 from yesterday</p>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Check-ins Today</p>
              <p className="text-2xl font-bold">
                {loadingStats ? '...' : dashboardStats.checkInsToday}
              </p>
            </div>
            <div className="w-12 h-12 bg-green-100 dark:bg-green-900/20 rounded-lg flex items-center justify-center">
              <UserCheck size={24} className="text-green-600" />
            </div>
          </div>
          <div className="mt-2">
            <p className="text-xs text-gray-500">
              Expected: {loadingStats ? '...' : dashboardStats.expectedCheckIns}
            </p>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Check-outs Today</p>
              <p className="text-2xl font-bold">
                {loadingStats ? '...' : dashboardStats.checkOutsToday}
              </p>
            </div>
            <div className="w-12 h-12 bg-orange-100 dark:bg-orange-900/20 rounded-lg flex items-center justify-center">
              <LogOut size={24} className="text-orange-600" />
            </div>
          </div>
          <div className="mt-2">
            <p className="text-xs text-gray-500">
              Pending: {loadingStats ? '...' : dashboardStats.pendingCheckouts}
            </p>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Occupancy Rate</p>
              <p className="text-2xl font-bold">
                {loadingStats ? '...' : dashboardStats.occupancyRate?.toFixed(1)}%
              </p>
            </div>
            <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/20 rounded-lg flex items-center justify-center">
              <Bed size={24} className="text-purple-600" />
            </div>
          </div>
          <div className="mt-2">
            <p className="text-xs text-gray-500">
              {loadingStats ? '...' : dashboardStats.occupiedRooms}/{dashboardStats.totalRooms} rooms occupied
            </p>
          </div>
        </div>
      </div>

      {/* Search and Filter Bar */}
      <div className="card">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Search by name, email, phone, room number, or guest ID..."
              className="input pl-10 w-full"
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setPagination({ ...pagination, page: 0 });
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  if (activeTab === 'current') {
                    fetchCurrentGuests();
                  } else {
                    fetchAllGuests();
                  }
                }
              }}
            />
          </div>
          
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2">
              <Filter size={18} className="text-gray-500" />
              <select 
                className="input-sm"
                value={statusFilter}
                onChange={(e) => {
                  setStatusFilter(e.target.value);
                  setPagination({ ...pagination, page: 0 });
                }}
              >
                <option value="all">All Status</option>
                <option value="checked-in">Checked-in</option>
                <option value="checked-out">Checked-out</option>
                <option value="upcoming">Upcoming</option>
                <option value="confirmed">Confirmed</option>
                <option value="cancelled">Cancelled</option>
              </select>
            </div>
            
            <button 
              onClick={handleExport}
              className="btn-outline flex items-center gap-2"
              disabled={loading}
            >
              <Download size={18} />
              Export
            </button>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200 dark:border-gray-700">
        <div className="flex gap-4">
          <button
            onClick={() => {
              setActiveTab('current');
              setPagination({ ...pagination, page: 0 });
            }}
            className={`px-4 py-3 font-medium border-b-2 transition-colors flex items-center gap-2 ${
              activeTab === 'current'
                ? 'border-primary-600 text-primary-600'
                : 'border-transparent text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
            }`}
          >
            <UserCheck size={18} />
            Current Guests ({currentGuests.length})
          </button>
          
          <button
            onClick={() => {
              setActiveTab('all');
              setPagination({ ...pagination, page: 0 });
            }}
            className={`px-4 py-3 font-medium border-b-2 transition-colors flex items-center gap-2 ${
              activeTab === 'all'
                ? 'border-primary-600 text-primary-600'
                : 'border-transparent text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
            }`}
          >
            <Users size={18} />
            All Guests ({allGuests.length})
          </button>
        </div>
      </div>

      {/* Loading State */}
      {loading && (
        <div className="card text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading guests...</p>
        </div>
      )}

      {/* Tab Content - Current Guests */}
      {!loading && activeTab === 'current' && (
        <div className="card">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 dark:bg-gray-700/50">
                <tr>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Guest</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Room Details</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Stay Period</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Balance</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Status</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                {currentGuests.map((guest) => (
                  <tr 
                    key={guest.id}
                    className="hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer"
                    onClick={() => handleGuestClick(guest.guestId)}
                  >
                    <td className="px-4 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-primary-100 dark:bg-primary-900/20 rounded-full flex items-center justify-center">
                          <span className="font-semibold text-primary-600">
                            {guest.name?.charAt(0) || 'G'}
                          </span>
                        </div>
                        <div>
                          <div className="flex items-center gap-2">
                            <p className="font-medium">{guest.name}</p>
                            {guest.vipLevel === 'VIP' && (
                              <span className={`px-2 py-1 rounded-full text-xs font-medium ${getVipBadgeClass(guest.vipLevel)}`}>
                                VIP
                              </span>
                            )}
                          </div>
                          <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 mt-1">
                            <Phone size={14} />
                            <span>{guest.phone}</span>
                          </div>
                          <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 mt-1">
                            <Mail size={14} />
                            <span>{guest.email}</span>
                          </div>
                        </div>
                      </div>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div>
                        <div className="flex items-center gap-2">
                          <Bed size={16} className="text-gray-500" />
                          <span className="font-medium">Room {guest.room}</span>
                        </div>
                        <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">{guest.roomType}</p>
                        <span className={`mt-2 inline-block px-2 py-1 rounded-full text-xs font-medium ${getLoyaltyBadgeClass(guest.loyaltyTier)}`}>
                          {guest.loyaltyTier}
                        </span>
                      </div>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div>
                        <div className="flex items-center gap-2">
                          <Calendar size={16} className="text-gray-500" />
                          <span>{guest.checkIn} - {guest.checkOut}</span>
                        </div>
                        <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">{guest.nights} nights</p>
                        <div className="mt-2">
                          <div className="flex items-center justify-between text-xs mb-1">
                            <span>Stay Progress</span>
                            <span>{guest.nights > 0 ? Math.floor(Math.random() * guest.nights) + 1 : 0}/{guest.nights} nights</span>
                          </div>
                          <div className="h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                            <div 
                              className="h-full bg-primary-600"
                              style={{ width: `${(Math.random() * 70 + 30)}%` }}
                            />
                          </div>
                        </div>
                      </div>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div className={`font-bold text-lg ${guest.balance !== '$0.00' ? 'text-red-600' : 'text-green-600'}`}>
                        {guest.balance}
                      </div>
                      <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        {guest.balance !== '$0.00' ? 'Due at checkout' : 'Fully paid'}
                      </p>
                    </td>
                    
                    <td className="px-4 py-4">
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusBadgeClass(guest.status)}`}>
                        {guest.status.charAt(0).toUpperCase() + guest.status.slice(1).replace('-', ' ')}
                      </span>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div className="flex items-center gap-2">
                        <button 
                          onClick={(e) => handleCheckout(guest.guestId, e)}
                          className="btn-danger text-sm px-3 py-1.5 flex items-center gap-1"
                        >
                          <LogOut size={14} />
                          Checkout
                        </button>
                        
                        <button 
                          onClick={(e) => handlePayment(guest.guestId, e)}
                          className="btn-primary text-sm px-3 py-1.5 flex items-center gap-1"
                        >
                          <CreditCard size={14} />
                          Payment
                        </button>
                        
                        <button 
                          onClick={(e) => {
                            e.stopPropagation();
                            handleGuestClick(guest.guestId);
                          }}
                          className="btn-outline text-sm px-3 py-1.5 flex items-center gap-1"
                        >
                          <Eye size={14} />
                          View
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            
            {currentGuests.length === 0 && (
              <div className="text-center py-12">
                <Users size={48} className="mx-auto text-gray-400" />
                <h3 className="mt-4 text-lg font-medium">No guests found</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-1">
                  Try adjusting your search or filter to find what you're looking for.
                </p>
                <button 
                  onClick={handleRefresh}
                  className="mt-4 btn-primary flex items-center gap-2 mx-auto"
                >
                  <RefreshCw size={16} />
                  Refresh Data
                </button>
              </div>
            )}
          </div>
          
          <div className="mt-6 pt-6 border-t dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div className="text-sm text-gray-600 dark:text-gray-400">
                Showing {currentGuests.length} of {pagination.totalElements} current guests
              </div>
              <div className="flex items-center gap-2">
                <button 
                  onClick={() => handlePageChange(pagination.page - 1)}
                  disabled={pagination.page === 0}
                  className="px-3 py-1 border rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  Previous
                </button>
                <span className="px-3 py-1 bg-primary-600 text-white rounded-md text-sm">
                  Page {pagination.page + 1} of {pagination.totalPages}
                </span>
                <button 
                  onClick={() => handlePageChange(pagination.page + 1)}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className="px-3 py-1 border rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  Next
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab Content - All Guests */}
      {!loading && activeTab === 'all' && (
        <div className="card">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 dark:bg-gray-700/50">
                <tr>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Guest</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Contact</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Last Stay</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Loyalty</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Value</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                {allGuests.map((guest) => (
                  <tr 
                    key={guest.id}
                    className="hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer"
                    onClick={() => handleGuestClick(guest.guestId)}
                  >
                    <td className="px-4 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-primary-100 dark:bg-primary-900/20 rounded-full flex items-center justify-center">
                          <span className="font-semibold text-primary-600">
                            {guest.name?.charAt(0) || 'G'}
                          </span>
                        </div>
                        <div>
                          <div className="flex items-center gap-2">
                            <p className="font-medium">{guest.name}</p>
                            {guest.vipLevel === 'VIP' && (
                              <span className={`px-2 py-1 rounded-full text-xs font-medium ${getVipBadgeClass(guest.vipLevel)}`}>
                                VIP
                              </span>
                            )}
                          </div>
                          <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                            ID: {guest.id}
                          </p>
                        </div>
                      </div>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div>
                        <div className="flex items-center gap-2 text-sm">
                          <Phone size={14} className="text-gray-500" />
                          <span>{guest.phone}</span>
                        </div>
                        <div className="flex items-center gap-2 text-sm mt-1">
                          <Mail size={14} className="text-gray-500" />
                          <span className="truncate max-w-[200px]">{guest.email}</span>
                        </div>
                      </div>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div>
                        <div className="text-sm">
                          {guest.lastStay || 'N/A'}
                        </div>
                        <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 mt-1">
                          <Bed size={14} />
                          <span>Room {guest.lastRoom}</span>
                        </div>
                        <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                          {guest.totalStays || 1} total stay{guest.totalStays !== 1 ? 's' : ''}
                        </p>
                      </div>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div>
                        <span className={`px-3 py-1 rounded-full text-xs font-medium ${getLoyaltyBadgeClass(guest.loyaltyTier)}`}>
                          {guest.loyaltyTier}
                        </span>
                        <div className="mt-2">
                          <div className="text-xs text-gray-600 dark:text-gray-400">Member since</div>
                          <div className="text-sm">{guest.createdAt ? formatDate(guest.createdAt) : 'Jan 2022'}</div>
                        </div>
                      </div>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div className="font-bold text-lg">
                        {guest.lifetimeValue}
                      </div>
                      <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        Lifetime value
                      </p>
                      <div className="mt-2">
                        <div className="flex items-center justify-between text-xs mb-1">
                          <span>Avg. spend</span>
                          <span>
                            ${guest.totalStays > 0 ? 
                              (parseFloat(guest.lifetimeValue?.replace(/[^0-9.]/g, '') || 0) / guest.totalStays).toFixed(2) 
                              : '0.00'}
                          </span>
                        </div>
                      </div>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div className="flex items-center gap-2">
                        <button 
                          onClick={(e) => {
                            e.stopPropagation();
                            handleGuestClick(guest.guestId);
                          }}
                          className="btn-outline text-sm px-3 py-1.5 flex items-center gap-1"
                        >
                          <Eye size={14} />
                          View Details
                        </button>
                        
                        <button 
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate('/guests/new', { state: { duplicateGuest: guest } });
                          }}
                          className="btn-secondary text-sm px-3 py-1.5 flex items-center gap-1"
                        >
                          <Plus size={14} />
                          New Booking
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            
            {allGuests.length === 0 && (
              <div className="text-center py-12">
                <Users size={48} className="mx-auto text-gray-400" />
                <h3 className="mt-4 text-lg font-medium">No guests found</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-1">
                  Try adjusting your search or filter to find what you're looking for.
                </p>
                <button 
                  onClick={handleRefresh}
                  className="mt-4 btn-primary flex items-center gap-2 mx-auto"
                >
                  <RefreshCw size={16} />
                  Refresh Data
                </button>
              </div>
            )}
          </div>
          
          <div className="mt-6 pt-6 border-t dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div className="text-sm text-gray-600 dark:text-gray-400">
                Showing {allGuests.length} of {pagination.totalElements} guests
              </div>
              <div className="flex items-center gap-2">
                <button 
                  onClick={() => handlePageChange(pagination.page - 1)}
                  disabled={pagination.page === 0}
                  className="px-3 py-1 border rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  Previous
                </button>
                <span className="px-3 py-1 bg-primary-600 text-white rounded-md text-sm">
                  Page {pagination.page + 1} of {pagination.totalPages}
                </span>
                <button 
                  onClick={() => handlePageChange(pagination.page + 1)}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className="px-3 py-1 border rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  Next
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Quick Stats Footer */}
      {!loadingStats && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {quickStats.map((stat, index) => (
            <div key={index} className="card">
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 ${stat.color === 'green' ? 'bg-green-100 dark:bg-green-900/20' : stat.color === 'blue' ? 'bg-blue-100 dark:bg-blue-900/20' : 'bg-purple-100 dark:bg-purple-900/20'} rounded-lg flex items-center justify-center`}>
                  {getIconComponent(stat.icon, stat.color)}
                </div>
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">{stat.title}</p>
                  <p className="font-medium">{stat.value}</p>
                  <p className="text-xs text-gray-500">{stat.description}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default GuestSearch;