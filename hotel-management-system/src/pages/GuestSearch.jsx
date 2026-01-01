import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Search, Filter, Users, UserCheck, Calendar, 
  Phone, Mail, MapPin, Bed, DollarSign, 
  ArrowRight, MoreVertical, CreditCard, LogOut,
  Plus, Download, Eye
} from 'lucide-react';

const GuestSearch = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('current');
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  // Mock data for current guests
  const currentGuests = [
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
      loyaltyTier: 'Gold'
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
      loyaltyTier: 'Silver'
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
      loyaltyTier: 'Platinum'
    },
    {
      id: 'GUEST-61938',
      name: 'Emma Wilson',
      email: 'emma.w@email.com',
      phone: '+1 (555) 234-5678',
      room: '512',
      roomType: 'Standard Queen',
      checkIn: 'Oct 30, 2023',
      checkOut: 'Nov 01, 2023',
      nights: 2,
      balance: '$325.25',
      status: 'checked-in',
      vipLevel: 'Regular',
      loyaltyTier: 'Basic'
    },
    {
      id: 'GUEST-52749',
      name: 'Robert Garcia',
      email: 'robert.g@email.com',
      phone: '+1 (555) 876-5432',
      room: '908',
      roomType: 'Deluxe King',
      checkIn: 'Oct 27, 2023',
      checkOut: 'Nov 03, 2023',
      nights: 7,
      balance: '$1,245.80',
      status: 'checked-in',
      vipLevel: 'VIP',
      loyaltyTier: 'Gold'
    },
  ];

  // Mock data for all guests (including historical)
  const allGuests = [
    ...currentGuests,
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
      loyaltyTier: 'Silver'
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
      loyaltyTier: 'Platinum'
    },
    {
      id: 'GUEST-27583',
      name: 'Jennifer Lee',
      email: 'jennifer.l@email.com',
      phone: '+1 (555) 654-3210',
      lastStay: 'Oct 05, 2023 - Oct 09, 2023',
      totalStays: 8,
      lifetimeValue: '$9,245.75',
      lastRoom: '1602',
      status: 'checked-out',
      vipLevel: 'VIP',
      loyaltyTier: 'Diamond'
    },
    {
      id: 'GUEST-18495',
      name: 'James Brown',
      email: 'james.b@email.com',
      phone: '+1 (555) 543-2109',
      lastStay: 'Sep 28, 2023 - Oct 02, 2023',
      totalStays: 12,
      lifetimeValue: '$15,780.25',
      lastRoom: '1104',
      status: 'checked-out',
      vipLevel: 'VIP',
      loyaltyTier: 'Platinum'
    },
    {
      id: 'GUEST-93746',
      name: 'Maria Garcia',
      email: 'maria.g@email.com',
      phone: '+1 (555) 432-1098',
      lastStay: 'Sep 20, 2023 - Sep 22, 2023',
      totalStays: 2,
      lifetimeValue: '$890.00',
      lastRoom: '208',
      status: 'checked-out',
      vipLevel: 'Regular',
      loyaltyTier: 'Basic'
    },
  ];

  // Filter guests based on search query and status
  const filteredCurrentGuests = currentGuests.filter(guest => {
    const matchesSearch = 
      guest.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      guest.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      guest.phone.includes(searchQuery) ||
      guest.room.includes(searchQuery) ||
      guest.id.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesStatus = statusFilter === 'all' || guest.status === statusFilter;
    
    return matchesSearch && matchesStatus;
  });

  const filteredAllGuests = allGuests.filter(guest => {
    const matchesSearch = 
      guest.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      guest.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      guest.phone.includes(searchQuery) ||
      (guest.lastRoom && guest.lastRoom.includes(searchQuery)) ||
      guest.id.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesStatus = statusFilter === 'all' || guest.status === statusFilter;
    
    return matchesSearch && matchesStatus;
  });

  // Get status badge class
  const getStatusBadgeClass = (status) => {
    switch(status) {
      case 'checked-in':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case 'checked-out':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
      case 'upcoming':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    }
  };

  // Get VIP badge class
  const getVipBadgeClass = (vipLevel) => {
    switch(vipLevel) {
      case 'VIP':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    }
  };

  // Get loyalty tier badge class
  const getLoyaltyBadgeClass = (tier) => {
    switch(tier) {
      case 'Diamond':
        return 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400';
      case 'Platinum':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case 'Gold':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
      case 'Silver':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    }
  };

  // Handle guest click (navigate to guest detail)
  const handleGuestClick = (guestId) => {
    navigate(`/guest-detail/${guestId}`);
  };

  // Handle checkout action
  const handleCheckout = (guestId, e) => {
    e.stopPropagation();
    navigate(`/checkout/${guestId}`);
  };

  // Handle payment action
  const handlePayment = (guestId, e) => {
    e.stopPropagation();
    navigate(`/payments`, { state: { guestId } });
  };

  // Handle new guest action
  const handleNewGuest = () => {
    navigate('/guests/new');
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
        <button 
          onClick={handleNewGuest}
          className="btn-primary flex items-center gap-2"
        >
          <Plus size={18} />
          New Guest
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Current Guests</p>
              <p className="text-2xl font-bold">8</p>
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
              <p className="text-2xl font-bold">3</p>
            </div>
            <div className="w-12 h-12 bg-green-100 dark:bg-green-900/20 rounded-lg flex items-center justify-center">
              <UserCheck size={24} className="text-green-600" />
            </div>
          </div>
          <div className="mt-2">
            <p className="text-xs text-gray-500">Expected: 5</p>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Check-outs Today</p>
              <p className="text-2xl font-bold">4</p>
            </div>
            <div className="w-12 h-12 bg-orange-100 dark:bg-orange-900/20 rounded-lg flex items-center justify-center">
              <LogOut size={24} className="text-orange-600" />
            </div>
          </div>
          <div className="mt-2">
            <p className="text-xs text-gray-500">Pending: 2</p>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Occupancy Rate</p>
              <p className="text-2xl font-bold">85%</p>
            </div>
            <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/20 rounded-lg flex items-center justify-center">
              <Bed size={24} className="text-purple-600" />
            </div>
          </div>
          <div className="mt-2">
            <p className="text-xs text-gray-500">152/180 rooms occupied</p>
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
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2">
              <Filter size={18} className="text-gray-500" />
              <select 
                className="input-sm"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="all">All Status</option>
                <option value="checked-in">Checked-in</option>
                <option value="checked-out">Checked-out</option>
                <option value="upcoming">Upcoming</option>
              </select>
            </div>
            
            <button className="btn-outline flex items-center gap-2">
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
            onClick={() => setActiveTab('current')}
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
            onClick={() => setActiveTab('all')}
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

      {/* Tab Content - Current Guests */}
      {activeTab === 'current' && (
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
                {filteredCurrentGuests.map((guest) => (
                  <tr 
                    key={guest.id}
                    className="hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer"
                    onClick={() => handleGuestClick(guest.id)}
                  >
                    <td className="px-4 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-primary-100 dark:bg-primary-900/20 rounded-full flex items-center justify-center">
                          <span className="font-semibold text-primary-600">
                            {guest.name.charAt(0)}
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
                          onClick={(e) => handleCheckout(guest.id, e)}
                          className="btn-danger text-sm px-3 py-1.5 flex items-center gap-1"
                        >
                          <LogOut size={14} />
                          Checkout
                        </button>
                        
                        <button 
                          onClick={(e) => handlePayment(guest.id, e)}
                          className="btn-primary text-sm px-3 py-1.5 flex items-center gap-1"
                        >
                          <CreditCard size={14} />
                          Payment
                        </button>
                        
                        <button 
                          onClick={(e) => {
                            e.stopPropagation();
                            handleGuestClick(guest.id);
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
            
            {filteredCurrentGuests.length === 0 && (
              <div className="text-center py-12">
                <Users size={48} className="mx-auto text-gray-400" />
                <h3 className="mt-4 text-lg font-medium">No guests found</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-1">
                  Try adjusting your search or filter to find what you're looking for.
                </p>
              </div>
            )}
          </div>
          
          <div className="mt-6 pt-6 border-t dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div className="text-sm text-gray-600 dark:text-gray-400">
                Showing {filteredCurrentGuests.length} of {currentGuests.length} current guests
              </div>
              <div className="flex items-center gap-2">
                <button className="px-3 py-1 border rounded-md text-sm">Previous</button>
                <button className="px-3 py-1 bg-primary-600 text-white rounded-md text-sm">1</button>
                <button className="px-3 py-1 border rounded-md text-sm">Next</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab Content - All Guests */}
      {activeTab === 'all' && (
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
                {filteredAllGuests.map((guest) => (
                  <tr 
                    key={guest.id}
                    className="hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer"
                    onClick={() => handleGuestClick(guest.id)}
                  >
                    <td className="px-4 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-primary-100 dark:bg-primary-900/20 rounded-full flex items-center justify-center">
                          <span className="font-semibold text-primary-600">
                            {guest.name.charAt(0)}
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
                          {guest.lastStay || `${guest.checkIn} - ${guest.checkOut}`}
                        </div>
                        <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 mt-1">
                          <Bed size={14} />
                          <span>Room {guest.lastRoom || guest.room}</span>
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
                          <div className="text-sm">Jan 2022</div>
                        </div>
                      </div>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div className="font-bold text-lg">
                        {guest.lifetimeValue || '$0.00'}
                      </div>
                      <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        Lifetime value
                      </p>
                      <div className="mt-2">
                        <div className="flex items-center justify-between text-xs mb-1">
                          <span>Avg. spend</span>
                          <span>${(parseFloat(guest.lifetimeValue?.replace(/[^0-9.]/g, '') || 0) / (guest.totalStays || 1)).toFixed(2)}</span>
                        </div>
                      </div>
                    </td>
                    
                    <td className="px-4 py-4">
                      <div className="flex items-center gap-2">
                        <button 
                          onClick={(e) => {
                            e.stopPropagation();
                            handleGuestClick(guest.id);
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
            
            {filteredAllGuests.length === 0 && (
              <div className="text-center py-12">
                <Users size={48} className="mx-auto text-gray-400" />
                <h3 className="mt-4 text-lg font-medium">No guests found</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-1">
                  Try adjusting your search or filter to find what you're looking for.
                </p>
              </div>
            )}
          </div>
          
          <div className="mt-6 pt-6 border-t dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div className="text-sm text-gray-600 dark:text-gray-400">
                Showing {filteredAllGuests.length} of {allGuests.length} guests
              </div>
              <div className="flex items-center gap-2">
                <button className="px-3 py-1 border rounded-md text-sm">Previous</button>
                <button className="px-3 py-1 bg-primary-600 text-white rounded-md text-sm">1</button>
                <button className="px-3 py-1 border rounded-md text-sm">Next</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Quick Stats Footer */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-green-100 dark:bg-green-900/20 rounded-lg flex items-center justify-center">
              <UserCheck size={20} className="text-green-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Most Frequent Guest</p>
              <p className="font-medium">James Brown</p>
              <p className="text-xs text-gray-500">12 stays • $15,780.25 spent</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900/20 rounded-lg flex items-center justify-center">
              <DollarSign size={20} className="text-blue-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Highest LTV Guest</p>
              <p className="font-medium">Jennifer Lee</p>
              <p className="text-xs text-gray-500">8 stays • $9,245.75 spent</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-purple-100 dark:bg-purple-900/20 rounded-lg flex items-center justify-center">
              <Calendar size={20} className="text-purple-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Check-ins Today</p>
              <p className="font-medium">3 Expected</p>
              <p className="text-xs text-gray-500">2 completed • 1 pending</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default GuestSearch;