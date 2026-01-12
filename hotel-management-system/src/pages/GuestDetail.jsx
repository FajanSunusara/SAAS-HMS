import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, Mail, Phone, MapPin, Calendar, DollarSign, Star, 
  CreditCard, FileText, Plus, Edit, Bed, User, Map, Home, 
  Clock, CheckCircle, XCircle, AlertCircle, Copy, Download,
  Shield, Award, Gift, Coffee, Wind, Newspaper, Thermometer,
  Upload, Eye, Hash, Building, Briefcase, Globe, Heart,
  TrendingUp, Activity, History, CreditCard as CreditCardIcon,
  ShoppingBag, Settings, UserCheck, Receipt, RefreshCw
} from 'lucide-react';
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import axios from 'axios';

const GuestDetail = () => {
  const { guestId } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('overview');
  
  // Create axios instance with error handling
  const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Mock data for fallback when API fails
  const mockGuest = {
    guestId: guestId,
    name: 'John Doe',
    email: 'john@example.com',
    phone: '+1 (555) 123-4567',
    gender: 'Male',
    dateOfBirth: 'Jan 15, 1985',
    nationality: 'American',
    profilePhoto: 'https://ui-avatars.com/api/?name=John+Doe&size=128',
    status: 'checked-in',
    vipLevel: 'GOLD',
    loyaltyTier: 'Gold',
    lifetimeValue: '$2,500.00',
    totalStays: 5,
    corporateAccount: 'TechCorp Inc'
  };

  const mockIdentity = {
    idType: 'Passport',
    idNumber: 'P12345678',
    issuingAuthority: 'US Government',
    issueCountry: 'USA',
    issueDate: '2022-01-01',
    expiryDate: '2032-01-01',
    verified: true,
    verificationDate: '2023-05-15',
    verifiedBy: 'Admin User',
    documentUrl: null
  };

  const mockAddress = {
    line1: '123 Main Street',
    line2: 'Suite 500',
    city: 'New York',
    state: 'NY',
    zipCode: '10001',
    country: 'USA',
    type: 'Home'
  };

  const mockCurrentStay = {
    roomNumber: '304',
    roomType: 'Deluxe King',
    floor: '3',
    checkIn: 'Jan 10, 2026',
    checkOut: 'Jan 15, 2026',
    nights: 5,
    nightsElapsed: 2,
    nightsRemaining: 3,
    ratePlan: 'Corporate Rate',
    ratePerNight: 250,
    includes: ['Breakfast', 'Wi-Fi', 'Parking'],
    id: 'BK-20260110-304',
    bookingSource: 'Direct',
    bookedBy: 'John Doe',
    specialInstructions: 'Early check-in requested'
  };

  const mockFinancialSummary = {
    roomCharges: 1000.00,
    serviceCharges: 250.00,
    taxes: 150.00,
    totalCharges: 1400.00,
    advancePaid: 500.00,
    pendingAmount: 900.00,
    dueDate: 'Jan 15, 2026',
    creditLimit: 5000.00,
    availableCredit: 3600.00
  };

  // Main guest details query - TanStack Query v5 syntax
  const { 
    data: guestData, 
    isLoading: isLoadingGuest, 
    error: guestError,
    refetch: refetchGuest
  } = useQuery({
    queryKey: ['guestDetail', guestId],
    queryFn: async () => {
      try {
        const response = await axiosInstance.get(`/v1/guest-detail/${guestId}`);
        return response.data.data || {};
      } catch (error) {
        console.warn('API failed, using mock data:', error.message);
        // Return mock data structure
        return {
          guest: mockGuest,
          identity: mockIdentity,
          address: mockAddress,
          currentStay: mockCurrentStay,
          financialSummary: mockFinancialSummary,
          preferences: {
            loyaltyTier: 'Gold',
            memberSince: '2022-06-01',
            loyaltyPoints: 12500,
            benefits: ['Late Checkout', 'Room Upgrade', 'Free Breakfast'],
            corporateAccount: 'TechCorp Inc',
            accountManager: 'Jane Smith',
            paymentTerms: 'Net 30'
          }
        };
      }
    },
    enabled: !!guestId,
    refetchOnWindowFocus: false,
    staleTime: 300000,
    retry: 1,
  });

  // Current stay query
  const { 
    data: currentStayData, 
    isLoading: isLoadingCurrentStay,
    refetch: refetchCurrentStay 
  } = useQuery({
    queryKey: ['currentStay', guestId],
    queryFn: async () => {
      try {
        const response = await axiosInstance.get(`/v1/guest-detail/${guestId}/current-stay`);
        return response.data.data || mockCurrentStay;
      } catch (error) {
        console.warn('Current stay API failed, using mock data');
        return mockCurrentStay;
      }
    },
    enabled: activeTab === 'current-stay' && !!guestId,
    staleTime: 60000,
    retry: 1,
  });

  // Booking history query
  const { 
    data: bookingHistoryData, 
    isLoading: isLoadingBookingHistory,
    refetch: refetchBookingHistory 
  } = useQuery({
    queryKey: ['bookingHistory', guestId],
    queryFn: async () => {
      try {
        const response = await axiosInstance.get(`/v1/guest-detail/${guestId}/booking-history`);
        return response.data.data || { bookings: [] };
      } catch (error) {
        console.warn('Booking history API failed, using mock data');
        return {
          bookings: [
            { id: 'BK-20260110-304', dates: 'Jan 10 - Jan 15, 2026', nights: 5, room: '304', roomType: 'Deluxe King', source: 'Direct', status: 'checked-in', total: '$1,400.00', revenue: 1400 },
            { id: 'BK-20251220-201', dates: 'Dec 20 - Dec 25, 2025', nights: 5, room: '201', roomType: 'Standard Queen', source: 'Booking.com', status: 'checked-out', total: '$850.00', revenue: 850 },
            { id: 'BK-20251115-512', dates: 'Nov 15 - Nov 18, 2025', nights: 3, room: '512', roomType: 'Executive Suite', source: 'Corporate', status: 'checked-out', total: '$1,200.00', revenue: 1200 }
          ],
          total: 3
        };
      }
    },
    enabled: activeTab === 'stay-history' && !!guestId,
    staleTime: 300000,
    retry: 1,
  });

  // Payment history query
  const { 
    data: paymentHistoryData, 
    isLoading: isLoadingPaymentHistory,
    refetch: refetchPaymentHistory 
  } = useQuery({
    queryKey: ['paymentHistory', guestId],
    queryFn: async () => {
      try {
        const response = await axiosInstance.get(`/v1/guest-detail/${guestId}/payment-history`);
        return response.data.data || { payments: [] };
      } catch (error) {
        console.warn('Payment history API failed, using mock data');
        return {
          payments: [
            { date: 'Jan 10, 2026 14:30', transactionId: 'TXN-001', amount: '$500.00', method: 'Credit Card', purpose: 'Advance Deposit', invoice: 'INV-001' },
            { date: 'Jan 11, 2026 10:15', transactionId: 'TXN-002', amount: '$250.00', method: 'Cash', purpose: 'Service Charge', invoice: 'INV-002' },
            { date: 'Jan 12, 2026 09:45', transactionId: 'TXN-003', amount: '$150.00', method: 'Credit Card', purpose: 'Food & Beverage', invoice: 'INV-003' }
          ],
          totalPayments: 900,
          paymentMethodsUsed: 2
        };
      }
    },
    enabled: activeTab === 'payments' && !!guestId,
    staleTime: 300000,
    retry: 1,
  });

  // Service history query
  const { 
    data: serviceHistoryData, 
    isLoading: isLoadingServiceHistory,
    refetch: refetchServiceHistory 
  } = useQuery({
    queryKey: ['serviceHistory', guestId],
    queryFn: async () => {
      try {
        const response = await axiosInstance.get(`/v1/guest-detail/${guestId}/service-history`);
        return response.data.data || { services: [] };
      } catch (error) {
        console.warn('Service history API failed, using mock data');
        return {
          services: [
            { date: 'Jan 10, 2026 16:30', service: 'Room Service', description: 'Dinner for two', charge: '$75.00', status: 'Delivered' },
            { date: 'Jan 11, 2026 10:00', service: 'Laundry', description: '2 shirts, 1 suit', charge: '$45.00', status: 'Delivered' },
            { date: 'Jan 12, 2026 08:30', service: 'Airport Transfer', description: 'To JFK Airport', charge: '$130.00', status: 'Scheduled' }
          ],
          totalServices: 3,
          completed: 2,
          pending: 1,
          totalCharges: 250
        };
      }
    },
    enabled: activeTab === 'services' && !!guestId,
    staleTime: 300000,
    retry: 1,
  });

  // Preferences query
  const { 
    data: preferencesData, 
    isLoading: isLoadingPreferences,
    refetch: refetchPreferences 
  } = useQuery({
    queryKey: ['preferences', guestId],
    queryFn: async () => {
      try {
        const response = await axiosInstance.get(`/v1/guest-detail/${guestId}/preferences`);
        return response.data.data || {};
      } catch (error) {
        console.warn('Preferences API failed, using mock data');
        return {
          loyaltyTier: 'Gold',
          memberSince: '2022-06-01',
          loyaltyPoints: 12500,
          benefits: ['Late Checkout', 'Room Upgrade', 'Free Breakfast'],
          corporateAccount: 'TechCorp Inc',
          accountManager: 'Jane Smith',
          paymentTerms: 'Net 30',
          roomPreferences: {
            smoking: 'Non-smoking',
            floor: 'High floor',
            bedType: 'King bed',
            pillowType: 'Feather pillows',
            temperature: '72°F',
            newspaper: 'New York Times',
            amenities: 'Extra towels, Coffee maker'
          }
        };
      }
    },
    enabled: activeTab === 'preferences' && !!guestId,
    staleTime: 300000,
    retry: 1,
  });

  // Activity timeline query
  const { 
    data: activityTimelineData, 
    isLoading: isLoadingActivity,
    refetch: refetchActivity 
  } = useQuery({
    queryKey: ['activityTimeline', guestId],
    queryFn: async () => {
      try {
        const response = await axiosInstance.get(`/v1/guest-detail/${guestId}/activity-timeline`);
        return response.data.data || { activities: [] };
      } catch (error) {
        console.warn('Activity timeline API failed, using mock data');
        return {
          activities: [
            { event: 'Checked in to Room 304', time: 'Jan 10, 2026 14:30', staff: 'Reception', icon: 'checkin' },
            { event: 'Payment received $500.00', time: 'Jan 10, 2026 14:45', staff: 'Front Desk', icon: 'payment' },
            { event: 'Room service ordered', time: 'Jan 10, 2026 16:30', staff: 'Room Service', icon: 'service' },
            { event: 'Extended stay by 1 day', time: 'Jan 11, 2026 09:15', staff: 'Reception', icon: 'booking' },
            { event: 'Breakfast order placed', time: 'Jan 12, 2026 08:00', staff: 'Restaurant', icon: 'food' }
          ]
        };
      }
    },
    enabled: activeTab === 'overview' && !!guestId,
    staleTime: 60000,
    retry: 1,
  });

  // Financial summary query
  const { 
    data: financialSummaryData, 
    isLoading: isLoadingFinancial,
    refetch: refetchFinancial 
  } = useQuery({
    queryKey: ['financialSummary', guestId],
    queryFn: async () => {
      try {
        const response = await axiosInstance.get(`/v1/guest-detail/${guestId}/financial-summary`);
        return response.data.data || mockFinancialSummary;
      } catch (error) {
        console.warn('Financial summary API failed, using mock data');
        return mockFinancialSummary;
      }
    },
    enabled: !!guestId,
    staleTime: 60000,
    retry: 1,
  });

  // Consolidate data from queries
  const guest = guestData?.guest || mockGuest;
  const identity = guestData?.identity || mockIdentity;
  const address = guestData?.address || mockAddress;
  const preferences = guestData?.preferences || preferencesData || {};
  const currentStay = guestData?.currentStay || currentStayData || mockCurrentStay;
  const financialSummary = guestData?.financialSummary || financialSummaryData || mockFinancialSummary;
  const bookingHistory = bookingHistoryData?.bookings || [];
  const paymentHistory = paymentHistoryData?.payments || [];
  const serviceHistory = serviceHistoryData?.services || [];
  const activityTimeline = guestData?.activityTimeline?.activities || activityTimelineData?.activities || [];

  // Loading state
  const isLoading = isLoadingGuest || 
    (activeTab === 'current-stay' && isLoadingCurrentStay) ||
    (activeTab === 'stay-history' && isLoadingBookingHistory) ||
    (activeTab === 'payments' && isLoadingPaymentHistory) ||
    (activeTab === 'services' && isLoadingServiceHistory) ||
    (activeTab === 'preferences' && isLoadingPreferences) ||
    (activeTab === 'overview' && isLoadingActivity) ||
    isLoadingFinancial;

  // Handle tab change
  const handleTabChange = (tabId) => {
    setActiveTab(tabId);
  };

  // Handle refresh all data
  const handleRefresh = () => {
    refetchGuest();
    switch(activeTab) {
      case 'current-stay':
        refetchCurrentStay();
        break;
      case 'stay-history':
        refetchBookingHistory();
        break;
      case 'payments':
        refetchPaymentHistory();
        break;
      case 'services':
        refetchServiceHistory();
        break;
      case 'preferences':
        refetchPreferences();
        break;
      case 'overview':
        refetchActivity();
        refetchFinancial();
        break;
    }
  };

  // Handle copy to clipboard
  const handleCopy = (text) => {
    navigator.clipboard.writeText(text);
    // Show a toast notification
    console.log('Copied to clipboard:', text);
  };

  // Get status badge color
  const getStatusBadgeClass = (status) => {
    if (!status) return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    
    switch(status.toLowerCase()) {
      case 'checked-in':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case 'checked-out':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
      case 'upcoming':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case 'walk-in':
        return 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-400';
      case 'blacklisted':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      case 'confirmed':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case 'cancelled':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    }
  };

  // Get event icon
  const getEventIcon = (icon) => {
    switch(icon) {
      case 'checkin':
        return <CheckCircle size={16} className="text-green-500" />;
      case 'payment':
        return <DollarSign size={16} className="text-blue-500" />;
      case 'service':
        return <Coffee size={16} className="text-purple-500" />;
      case 'food':
        return <Heart size={16} className="text-pink-500" />;
      case 'booking':
        return <Calendar size={16} className="text-indigo-500" />;
      default:
        return <Activity size={16} className="text-gray-500" />;
    }
  };

  // Loading component
  if (isLoadingGuest) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading guest details...</p>
        </div>
      </div>
    );
  }

  // Main render
  return (
    <div className="space-y-6 p-4 md:p-6">
      {/* Back Button and Refresh */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
        >
          <ArrowLeft size={20} />
          Back to Guests
        </button>
        
        <button
          onClick={handleRefresh}
          disabled={isLoading}
          className="flex items-center gap-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
        >
          <RefreshCw size={18} className={isLoading ? 'animate-spin' : ''} />
          {isLoading ? 'Refreshing...' : 'Refresh'}
        </button>
      </div>

      {/* Guest Identity Header */}
      <div className="card">
        <div className="flex flex-col md:flex-row md:items-start justify-between gap-6">
          <div className="flex items-start gap-4">
            <img
              src={guest.profilePhoto || 'https://ui-avatars.com/api/?name=Guest&size=128'}
              alt={guest.name || 'Guest'}
              className="w-20 h-20 md:w-24 md:h-24 rounded-full object-cover border-4 border-white dark:border-gray-800 shadow-lg"
              onError={(e) => {
                e.target.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(guest.name || 'Guest')}&size=128`;
              }}
            />
            <div>
              <div className="flex flex-wrap items-center gap-3 mb-2">
                <h1 className="text-2xl md:text-3xl font-bold">{guest.name || 'Loading...'}</h1>
                <div className="flex flex-wrap gap-2">
                  <span className={`px-3 py-1 rounded-full text-xs font-medium flex items-center gap-1 ${getStatusBadgeClass(guest.status)}`}>
                    {guest.status === 'checked-in' && '✓'}
                    {guest.status ? guest.status.charAt(0).toUpperCase() + guest.status.slice(1).replace('-', ' ') : 'Loading...'}
                  </span>
                  {guest.vipLevel && guest.vipLevel !== 'REGULAR' && (
                    <span className="px-3 py-1 bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400 rounded-full text-xs font-medium flex items-center gap-1">
                      <Star size={12} fill="currentColor" />
                      {guest.vipLevel}
                    </span>
                  )}
                  {guest.loyaltyTier && (
                    <span className="px-3 py-1 bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400 rounded-full text-xs font-medium">
                      {guest.loyaltyTier}
                    </span>
                  )}
                </div>
              </div>
              
              <div className="flex flex-wrap items-center gap-4 text-sm text-gray-600 dark:text-gray-400">
                <div className="flex items-center gap-1">
                  <Hash size={14} />
                  <span>ID: {guest.guestId || 'Loading...'}</span>
                </div>
                {guest.corporateAccount && guest.corporateAccount !== 'N/A' && (
                  <div className="flex items-center gap-1">
                    <Building size={14} />
                    <span>{guest.corporateAccount}</span>
                  </div>
                )}
                <div className="flex items-center gap-1">
                  <TrendingUp size={14} />
                  <span>LTV: {guest.lifetimeValue || '$0.00'}</span>
                </div>
                <div className="flex items-center gap-1">
                  <Calendar size={14} />
                  <span>Stays: {guest.totalStays || 0}</span>
                </div>
              </div>

              <div className="flex flex-wrap gap-4 mt-3">
                {guest.email && guest.email !== 'No email' && (
                  <a href={`mailto:${guest.email}`} className="flex items-center gap-2 text-sm text-primary-600 hover:text-primary-700 transition-colors">
                    <Mail size={16} />
                    <span>{guest.email}</span>
                  </a>
                )}
                {guest.phone && guest.phone !== 'No phone' && (
                  <a href={`tel:${guest.phone}`} className="flex items-center gap-2 text-sm text-primary-600 hover:text-primary-700 transition-colors">
                    <Phone size={16} />
                    <span>{guest.phone}</span>
                  </a>
                )}
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="flex flex-wrap gap-2">
            <button 
              onClick={() => navigate(`/guests/${guestId}/edit`)}
              className="btn-secondary flex items-center gap-2"
              disabled={isLoading}
            >
              <Edit size={18} />
              Edit Guest
            </button>
            <button 
              className="btn-primary flex items-center gap-2"
              disabled={isLoading}
            >
              <Bed size={18} />
              New Booking
            </button>
            {guest.status === 'checked-in' && (
              <button className="btn-danger flex items-center gap-2">
                <DollarSign size={18} />
                Checkout
              </button>
            )}
            <button className="btn-outline flex items-center gap-2">
              <FileText size={18} />
              View Invoice
            </button>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200 dark:border-gray-700">
        <div className="flex gap-4 overflow-x-auto">
          {[
            { id: 'overview', label: 'Overview', icon: UserCheck },
            { id: 'current-stay', label: 'Current Stay', icon: Bed },
            { id: 'stay-history', label: 'Stay History', icon: History },
            { id: 'payments', label: 'Payments', icon: CreditCardIcon },
            { id: 'services', label: 'Services', icon: ShoppingBag },
            { id: 'preferences', label: 'Preferences', icon: Settings }
          ].map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => handleTabChange(tab.id)}
                disabled={isLoading}
                className={`px-4 py-3 font-medium border-b-2 transition-colors flex items-center gap-2 whitespace-nowrap ${
                  activeTab === tab.id
                    ? 'border-primary-600 text-primary-600'
                    : 'border-transparent text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
                } ${isLoading ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                <Icon size={18} />
                {tab.label}
                {((tab.id === 'stay-history' && isLoadingBookingHistory) ||
                  (tab.id === 'payments' && isLoadingPaymentHistory) ||
                  (tab.id === 'services' && isLoadingServiceHistory) ||
                  (tab.id === 'preferences' && isLoadingPreferences) ||
                  (tab.id === 'current-stay' && isLoadingCurrentStay) ||
                  (tab.id === 'overview' && isLoadingActivity)) && (
                  <RefreshCw size={14} className="animate-spin ml-1" />
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Tab Content Loading State */}
      {isLoading && activeTab !== 'overview' && (
        <div className="card text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading {activeTab.replace('-', ' ')} data...</p>
        </div>
      )}

      {/* Tab Content - Overview */}
      {!isLoading && activeTab === 'overview' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* LEFT COLUMN - Guest Profile Data */}
          <div className="lg:col-span-2 space-y-6">
            {/* Personal Information Card */}
            <div className="card">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-semibold flex items-center gap-2">
                  <User size={20} />
                  Personal Information
                </h2>
                <button 
                  className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1"
                  onClick={() => navigate(`/guests/${guestId}/edit`)}
                >
                  <Edit size={16} />
                  Edit
                </button>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Full Name</label>
                  <p className="font-medium">{guest.name || 'N/A'}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Gender</label>
                  <p className="font-medium">{guest.gender || 'N/A'}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Date of Birth</label>
                  <p className="font-medium">{guest.dateOfBirth || 'N/A'}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Nationality</label>
                  <p className="font-medium">{guest.nationality || 'N/A'}</p>
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Contact Information</label>
                  <div className="flex flex-wrap gap-4 mt-2">
                    <div className="flex items-center gap-2">
                      <Phone size={16} className="text-gray-500" />
                      <span>{guest.phone || 'N/A'}</span>
                      {guest.phone && guest.phone !== 'No phone' && (
                        <button 
                          className="ml-2 text-gray-500 hover:text-gray-700"
                          onClick={() => handleCopy(guest.phone)}
                        >
                          <Copy size={14} />
                        </button>
                      )}
                    </div>
                    <div className="flex items-center gap-2">
                      <Mail size={16} className="text-gray-500" />
                      <span>{guest.email || 'N/A'}</span>
                      {guest.email && guest.email !== 'No email' && (
                        <button 
                          className="ml-2 text-gray-500 hover:text-gray-700"
                          onClick={() => handleCopy(guest.email)}
                        >
                          <Copy size={14} />
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Identity & Verification Details */}
            <div className="card">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-semibold flex items-center gap-2">
                  <Shield size={20} />
                  Identity & Verification
                </h2>
                <div className="flex items-center gap-2">
                  {identity.verified ? (
                    <span className="flex items-center gap-1 text-sm text-green-600 dark:text-green-400">
                      <CheckCircle size={16} />
                      Verified
                    </span>
                  ) : (
                    <span className="flex items-center gap-1 text-sm text-yellow-600 dark:text-yellow-400">
                      <AlertCircle size={16} />
                      Pending
                    </span>
                  )}
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">ID Type</label>
                  <p className="font-medium">{identity.idType || 'N/A'}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">ID Number</label>
                  <div className="flex items-center gap-2">
                    <p className="font-mono font-medium">{identity.idNumber || 'N/A'}</p>
                    {identity.idNumber && identity.idNumber !== 'N/A' && (
                      <button 
                        className="text-gray-500 hover:text-gray-700"
                        onClick={() => handleCopy(identity.idNumber)}
                      >
                        <Copy size={14} />
                      </button>
                    )}
                  </div>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Issuing Authority</label>
                  <p className="font-medium">{identity.issuingAuthority || 'N/A'}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Issue Country</label>
                  <p className="font-medium">{identity.issueCountry || 'N/A'}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Validity</label>
                  <p className="font-medium">{identity.issueDate || 'N/A'} - {identity.expiryDate || 'N/A'}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Verification</label>
                  <p className="text-sm">
                    {identity.verified ? `Verified on ${identity.verificationDate} by ${identity.verifiedBy}` : 'Pending verification'}
                  </p>
                </div>
              </div>
            </div>

            {/* Address Information */}
            <div className="card">
              <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
                <Home size={20} />
                Address Information
              </h2>
              <div className="space-y-3">
                <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                  <p className="font-medium">{address.line1 || 'N/A'}</p>
                  {address.line2 && <p>{address.line2}</p>}
                  <p>{address.city || ''}, {address.state || ''} {address.zipCode || ''}</p>
                  <p>{address.country || 'N/A'}</p>
                  <div className="flex items-center justify-between mt-3">
                    <span className="text-sm text-gray-600 dark:text-gray-400">{address.type || 'Home'} Address</span>
                    {address.line1 && address.line1 !== 'N/A' && (
                      <button 
                        className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1"
                        onClick={() => handleCopy(`${address.line1}, ${address.city}, ${address.state} ${address.zipCode}`)}
                      >
                        <Copy size={14} />
                        Copy Address
                      </button>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* RIGHT COLUMN - Live Operations & Finance */}
          <div className="space-y-6">
            {/* Current Stay Summary */}
            {guest.status === 'checked-in' && currentStay.roomNumber && (
              <div className="card">
                <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
                  <Bed size={20} />
                  Current Stay
                </h2>
                <div className="space-y-4">
                  <div className="bg-primary-50 dark:bg-primary-900/20 p-4 rounded-lg">
                    <div className="flex items-center justify-between mb-2">
                      <div>
                        <p className="text-2xl font-bold">Room {currentStay.roomNumber}</p>
                        <p className="text-sm text-gray-600 dark:text-gray-400">{currentStay.roomType}</p>
                      </div>
                      <div className="text-right">
                        <p className="text-sm text-gray-600 dark:text-gray-400">Floor</p>
                        <p className="text-xl font-bold">{currentStay.floor || 'N/A'}</p>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4 mt-4">
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Check-in</p>
                        <p className="font-medium">{currentStay.checkIn || 'N/A'}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Check-out</p>
                        <p className="font-medium">{currentStay.checkOut || 'N/A'}</p>
                      </div>
                    </div>
                    {currentStay.nights && currentStay.nightsElapsed && (
                      <div className="mt-4">
                        <div className="flex items-center justify-between text-sm">
                          <span>Stay Progress</span>
                          <span>{currentStay.nightsElapsed} of {currentStay.nights} nights</span>
                        </div>
                        <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden mt-1">
                          <div 
                            className="h-full bg-primary-600"
                            style={{ width: `${(currentStay.nightsElapsed / currentStay.nights) * 100}%` }}
                          />
                        </div>
                      </div>
                    )}
                  </div>

                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-gray-600 dark:text-gray-400">Rate Plan</span>
                      <span className="font-medium">{currentStay.ratePlan || 'N/A'}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-gray-600 dark:text-gray-400">Rate</span>
                      <span className="font-medium">${currentStay.ratePerNight || 0}/night</span>
                    </div>
                    {currentStay.includes && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Includes</span>
                        <span className="text-sm text-right">{currentStay.includes.join(', ')}</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* Financial Summary Card */}
            <div className="card">
              <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
                <DollarSign size={20} />
                Financial Summary
              </h2>
              <div className="space-y-4">
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Room Charges</span>
                    <span className="font-medium">${financialSummary.roomCharges?.toFixed(2) || '0.00'}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Service Charges</span>
                    <span className="font-medium">${financialSummary.serviceCharges?.toFixed(2) || '0.00'}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Taxes & Fees</span>
                    <span className="font-medium">${financialSummary.taxes?.toFixed(2) || '0.00'}</span>
                  </div>
                  <div className="border-t dark:border-gray-700 pt-2">
                    <div className="flex items-center justify-between font-semibold">
                      <span>Total Charges</span>
                      <span>${financialSummary.totalCharges?.toFixed(2) || '0.00'}</span>
                    </div>
                  </div>
                </div>

                <div className="pt-4 border-t dark:border-gray-700">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm">Advance Paid</span>
                    <span className="font-medium text-green-600">${financialSummary.advancePaid?.toFixed(2) || '0.00'}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-semibold">Pending Amount</span>
                    <span className="text-xl font-bold text-red-600">${financialSummary.pendingAmount?.toFixed(2) || '0.00'}</span>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">Due at checkout: {financialSummary.dueDate || 'N/A'}</p>
                </div>

                <button className="btn-primary w-full mt-4">
                  Process Payment
                </button>
              </div>
            </div>

            {/* Activity Timeline */}
            <div className="card">
              <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
                <Activity size={20} />
                Activity Timeline
              </h2>
              <div className="space-y-4">
                {activityTimeline.length > 0 ? (
                  activityTimeline.map((activity, index) => (
                    <div key={index} className="flex items-start">
                      <div className="flex-shrink-0 w-8 h-8 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center">
                        {getEventIcon(activity.icon)}
                      </div>
                      <div className="ml-4 flex-1">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium">{activity.event}</p>
                          <span className="text-xs text-gray-500">{activity.time ? activity.time.split(' ')[1] : ''}</span>
                        </div>
                        <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                          {activity.time ? activity.time.split(' ')[0] : ''} • By {activity.staff || 'Staff'}
                        </p>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-4 text-gray-500">
                    No recent activity
                  </div>
                )}
              </div>
              <button className="w-full mt-4 text-sm text-primary-600 hover:text-primary-700">
                Show All Activity
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Tab Content - Current Stay */}
      {!isLoading && activeTab === 'current-stay' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Stay Details */}
          <div className="lg:col-span-2 card">
            <h2 className="text-xl font-semibold mb-4">Current Stay Details</h2>
            {currentStay.roomNumber ? (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="bg-primary-50 dark:bg-primary-900/20 p-5 rounded-xl">
                    <div className="flex items-center gap-3 mb-4">
                      <div className="w-12 h-12 bg-primary-100 dark:bg-primary-900/40 rounded-lg flex items-center justify-center">
                        <Bed size={24} className="text-primary-600" />
                      </div>
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Room Number</p>
                        <p className="text-2xl font-bold">Room {currentStay.roomNumber}</p>
                      </div>
                    </div>
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Room Type</span>
                        <span className="font-medium">{currentStay.roomType || 'N/A'}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Floor</span>
                        <span className="font-medium">{currentStay.floor || 'N/A'}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">View</span>
                        <span className="font-medium">{currentStay.view || 'N/A'}</span>
                      </div>
                    </div>
                  </div>

                  <div className="bg-blue-50 dark:bg-blue-900/20 p-5 rounded-xl">
                    <div className="flex items-center gap-3 mb-4">
                      <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/40 rounded-lg flex items-center justify-center">
                        <Calendar size={24} className="text-blue-600" />
                      </div>
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Stay Duration</p>
                        <p className="text-2xl font-bold">{currentStay.nights || 0} Nights</p>
                      </div>
                    </div>
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Check-in</span>
                        <span className="font-medium">{currentStay.checkIn || 'N/A'}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Check-out</span>
                        <span className="font-medium">{currentStay.checkOut || 'N/A'}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Days Remaining</span>
                        <span className="font-medium text-red-600">{currentStay.nightsRemaining || 0} days</span>
                      </div>
                    </div>
                  </div>

                  <div className="bg-green-50 dark:bg-green-900/20 p-5 rounded-xl">
                    <div className="flex items-center gap-3 mb-4">
                      <div className="w-12 h-12 bg-green-100 dark:bg-green-900/40 rounded-lg flex items-center justify-center">
                        <DollarSign size={24} className="text-green-600" />
                      </div>
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Rate Plan</p>
                        <p className="text-2xl font-bold">{currentStay.ratePlan || 'N/A'}</p>
                      </div>
                    </div>
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Rate per Night</span>
                        <span className="font-medium">${currentStay.ratePerNight || 0}/night</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Total Stay Cost</span>
                        <span className="font-medium">${((currentStay.ratePerNight || 0) * (currentStay.nights || 0)).toFixed(2)}</span>
                      </div>
                      {currentStay.includes && (
                        <div className="flex items-center justify-between">
                          <span className="text-sm text-gray-600 dark:text-gray-400">Includes</span>
                          <span className="text-sm text-right">{currentStay.includes.join(', ')}</span>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="bg-purple-50 dark:bg-purple-900/20 p-5 rounded-xl">
                    <div className="flex items-center gap-3 mb-4">
                      <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/40 rounded-lg flex items-center justify-center">
                        <Receipt size={24} className="text-purple-600" />
                      </div>
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Booking Details</p>
                        <p className="text-2xl font-bold">{currentStay.id || 'N/A'}</p>
                      </div>
                    </div>
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Booking Source</span>
                        <span className="font-medium">{currentStay.bookingSource || 'N/A'}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Booked By</span>
                        <span className="font-medium">{currentStay.bookedBy || 'N/A'}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600 dark:text-gray-400">Special Instructions</span>
                        <span className="text-sm text-right">{currentStay.specialInstructions || 'None'}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="mt-6">
                  <button className="btn-primary w-full">View Full Folio</button>
                </div>
              </>
            ) : (
              <div className="text-center py-12">
                <Bed size={48} className="mx-auto text-gray-400" />
                <h3 className="mt-4 text-lg font-medium">No current stay</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-1">
                  Guest is not currently checked in.
                </p>
              </div>
            )}
          </div>

          {/* Payment Summary */}
          <div className="card">
            <h2 className="text-xl font-semibold mb-4">Payment Summary</h2>
            <div className="space-y-6">
              <div className="p-4 bg-red-50 dark:bg-red-900/20 rounded-lg">
                <p className="text-sm text-gray-600 dark:text-gray-400">Current Balance</p>
                <p className="text-3xl font-bold text-red-600">${financialSummary.pendingAmount?.toFixed(2) || '0.00'}</p>
                <p className="text-xs text-gray-500">Due at check-out ({currentStay.checkOut || 'N/A'})</p>
              </div>
              
              <div className="space-y-4">
                <div className="p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Total Paid</p>
                  <p className="text-xl font-semibold text-green-600">${financialSummary.advancePaid?.toFixed(2) || '0.00'}</p>
                  <p className="text-xs text-gray-500">Advance deposit</p>
                </div>
                
                <div className="p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Credit Limit</p>
                  <p className="text-xl font-semibold">${financialSummary.creditLimit?.toFixed(2) || '0.00'}</p>
                  <p className="text-xs text-gray-500">Available: ${financialSummary.availableCredit?.toFixed(2) || '0.00'}</p>
                </div>
              </div>
              
              <div className="space-y-3">
                <button className="btn-primary w-full">Process Payment</button>
                <button className="btn-outline w-full">View All Charges</button>
                {currentStay.roomNumber && (
                  <button className="btn-secondary w-full">Extend Stay</button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab Content - Stay History */}
      {!isLoading && activeTab === 'stay-history' && (
        <div className="card">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold">Stay History</h2>
            <div className="flex items-center gap-3">
              <select className="input-sm">
                <option>All Status</option>
                <option>Checked Out</option>
                <option>Cancelled</option>
                <option>No-show</option>
              </select>
              <button className="btn-primary flex items-center gap-2">
                Export History
              </button>
            </div>
          </div>
          
          {bookingHistory.length > 0 ? (
            <>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 dark:bg-gray-700/50">
                    <tr>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Booking ID</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Dates</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Room Details</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Source</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Status</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Total Bill</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                    {bookingHistory.map((stay, index) => (
                      <tr key={index} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <div className="font-medium">{stay.id}</div>
                            <button 
                              className="text-gray-500 hover:text-gray-700"
                              onClick={() => handleCopy(stay.id)}
                            >
                              <Copy size={14} />
                            </button>
                          </div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="text-sm">{stay.dates}</div>
                          <div className="text-xs text-gray-500">{stay.nights || '0'} nights</div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="font-medium">{stay.roomType}</div>
                          <div className="text-sm text-gray-500">Room {stay.room}</div>
                        </td>
                        <td className="px-4 py-3">
                          <span className="text-sm bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded">
                            {stay.source}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusBadgeClass(stay.status)}`}>
                            {stay.status}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <div className="font-semibold">{stay.total}</div>
                          <div className="text-xs text-gray-500">Revenue: ${stay.revenue?.toFixed(2) || '0.00'}</div>
                        </td>
                        <td className="px-4 py-3">
                          <button 
                            onClick={() => navigate(`/bookings/${stay.id}`)}
                            className="text-primary-600 hover:text-primary-700 text-sm font-medium"
                          >
                            View Details
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              
              <div className="mt-6 pt-6 border-t dark:border-gray-700">
                <div className="flex items-center justify-between">
                  <div className="text-sm text-gray-600 dark:text-gray-400">
                    Showing {bookingHistory.length} of {bookingHistoryData?.total || bookingHistory.length} stays
                  </div>
                  <div className="flex items-center gap-2">
                    <button className="px-3 py-1 border rounded-md text-sm">Previous</button>
                    <button className="px-3 py-1 bg-primary-600 text-white rounded-md text-sm">1</button>
                    <button className="px-3 py-1 border rounded-md text-sm">Next</button>
                  </div>
                </div>
              </div>
            </>
          ) : (
            <div className="text-center py-12">
              <History size={48} className="mx-auto text-gray-400" />
              <h3 className="mt-4 text-lg font-medium">No stay history found</h3>
              <p className="text-gray-600 dark:text-gray-400 mt-1">
                This guest has no previous stays.
              </p>
            </div>
          )}
        </div>
      )}

      {/* Tab Content - Payments */}
      {!isLoading && activeTab === 'payments' && (
        <div className="card">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold">Payment History</h2>
            <div className="flex items-center gap-3">
              <select className="input-sm">
                <option>All Methods</option>
                <option>Credit Card</option>
                <option>Cash</option>
                <option>Corporate Billing</option>
              </select>
              <button className="btn-primary flex items-center gap-2">
                <Plus size={18} />
                Add Payment
              </button>
            </div>
          </div>
          
          {paymentHistory.length > 0 ? (
            <>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 dark:bg-gray-700/50">
                    <tr>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Date & Time</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Transaction ID</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Amount</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Payment Method</th>
                      <th className="px-4-py-3 text-left text-sm font-semibold">Purpose</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Invoice</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Receipt</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                    {paymentHistory.map((payment, index) => (
                      <tr key={index} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                        <td className="px-4 py-3">
                          <div className="text-sm">{payment.date}</div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <span className="font-mono text-sm">{payment.transactionId}</span>
                            <button 
                              className="text-gray-500 hover:text-gray-700"
                              onClick={() => handleCopy(payment.transactionId)}
                            >
                              <Copy size={14} />
                            </button>
                          </div>
                        </td>
                        <td className="px-4 py-3">
                          <div className={`font-semibold ${payment.amount?.startsWith('$') ? 'text-green-600' : 'text-red-600'}`}>
                            {payment.amount}
                          </div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <CreditCard size={14} className="text-gray-500" />
                            <span className="text-sm">{payment.method}</span>
                          </div>
                        </td>
                        <td className="px-4 py-3">
                          <span className={`text-sm px-2 py-1 rounded ${
                            payment.purpose?.includes('Deposit') 
                              ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
                              : payment.purpose?.includes('Final')
                              ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
                              : 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
                          }`}>
                            {payment.purpose}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <button 
                            onClick={() => navigate(`/invoice/${payment.invoice}`)}
                            className="text-primary-600 hover:text-primary-700 text-sm font-medium"
                          >
                            {payment.invoice}
                          </button>
                        </td>
                        <td className="px-4 py-3">
                          <button className="btn-outline text-xs px-3 py-1">
                            Download
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              
              <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Total Payments</p>
                  <p className="text-2xl font-bold">${paymentHistoryData?.totalPayments?.toFixed(2) || '0.00'}</p>
                </div>
                <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Outstanding Balance</p>
                  <p className="text-2xl font-bold text-red-600">${financialSummary.pendingAmount?.toFixed(2) || '0.00'}</p>
                </div>
                <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Payment Methods Used</p>
                  <p className="text-xl font-bold">{paymentHistoryData?.paymentMethodsUsed || 0}</p>
                </div>
              </div>
            </>
          ) : (
            <div className="text-center py-12">
              <CreditCard size={48} className="mx-auto text-gray-400" />
              <h3 className="mt-4 text-lg font-medium">No payment history found</h3>
              <p className="text-gray-600 dark:text-gray-400 mt-1">
                This guest has no payment records.
              </p>
            </div>
          )}
        </div>
      )}

      {/* Tab Content - Services */}
      {!isLoading && activeTab === 'services' && (
        <div className="card">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold">Service History (Current Stay)</h2>
            <button className="btn-primary flex items-center gap-2">
              <Plus size={18} />
              New Service Request
            </button>
          </div>
          
          {serviceHistory.length > 0 ? (
            <>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 dark:bg-gray-700/50">
                    <tr>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Date & Time</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Service</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Description</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Charge</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Status</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                    {serviceHistory.map((service, index) => (
                      <tr key={index} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                        <td className="px-4 py-3">
                          <div className="text-sm">{service.date}</div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="font-medium">{service.service}</div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="text-sm text-gray-600 dark:text-gray-400">{service.description}</div>
                        </td>
                        <td className="px-4 py-3">
                          <div className={`font-semibold ${service.charge === '$0.00' ? 'text-gray-500' : 'text-green-600'}`}>
                            {service.charge}
                          </div>
                        </td>
                        <td className="px-4 py-3">
                          <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                            service.status === 'Delivered' || service.status === 'Fulfilled'
                              ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
                              : service.status === 'Billed'
                              ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
                              : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
                          }`}>
                            {service.status}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <button className="text-primary-600 hover:text-primary-700 text-sm">
                            View Details
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              
              <div className="mt-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Total Services</p>
                  <p className="text-2xl font-bold">{serviceHistoryData?.totalServices || serviceHistory.length}</p>
                </div>
                <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Completed</p>
                  <p className="text-2xl font-bold">{serviceHistoryData?.completed || 0}</p>
                </div>
                <div className="p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Pending</p>
                  <p className="text-2xl font-bold">{serviceHistoryData?.pending || 0}</p>
                </div>
                <div className="p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Total Charges</p>
                  <p className="text-2xl font-bold">${serviceHistoryData?.totalCharges?.toFixed(2) || '0.00'}</p>
                </div>
              </div>
            </>
          ) : (
            <div className="text-center py-12">
              <ShoppingBag size={48} className="mx-auto text-gray-400" />
              <h3 className="mt-4 text-lg font-medium">No service history found</h3>
              <p className="text-gray-600 dark:text-gray-400 mt-1">
                No services have been requested for this guest.
              </p>
            </div>
          )}
        </div>
      )}

      {/* Tab Content - Preferences */}
      {!isLoading && activeTab === 'preferences' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Room Preferences */}
          <div className="card lg:col-span-2">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold">Room Preferences</h2>
              <button className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1">
                <Edit size={16} />
                Edit Preferences
              </button>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {preferences.roomPreferences && Object.entries(preferences.roomPreferences).map(([key, value]) => (
                <div key={key} className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
                  <div>
                    <span className="text-sm font-medium capitalize">{key.replace(/([A-Z])/g, ' $1').trim()}</span>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">{value || 'Not specified'}</p>
                  </div>
                  <div className="text-right">
                    <span className={`text-xs px-2 py-1 rounded ${
                      key.includes('vip') ? 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400' :
                      key.includes('allerg') ? 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400' :
                      'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
                    }`}>
                      {key.includes('vip') ? 'VIP' : 'Standard'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Loyalty Information */}
          <div className="space-y-6">
            <div className="card">
              <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
                <Award size={20} />
                Loyalty Information
              </h2>
              <div className="space-y-4">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Membership Tier</p>
                  <p className="text-xl font-semibold">{preferences.loyaltyTier || 'Basic Member'}</p>
                  <p className="text-xs text-gray-500">Since: {preferences.memberSince || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Total Points</p>
                  <p className="text-2xl font-bold text-primary-600">
                    {preferences.loyaltyPoints ? preferences.loyaltyPoints.toLocaleString() : '0'}
                  </p>
                  <div className="mt-2">
                    <div className="flex items-center justify-between text-xs mb-1">
                      <span>Gold Tier</span>
                      <span>Platinum Tier</span>
                    </div>
                    <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                      <div className="h-full bg-primary-600" style={{ width: '70%' }} />
                    </div>
                    <p className="text-xs text-gray-500 mt-1">5,550 points to Platinum</p>
                  </div>
                </div>
                <div>
                  <p className="text-sm font-medium mb-2">Benefits</p>
                  <ul className="space-y-2 text-sm text-gray-600 dark:text-gray-400">
                    {preferences.benefits ? (
                      preferences.benefits.map((benefit, index) => (
                        <li key={index} className="flex items-center gap-2">
                          <CheckCircle size={14} className="text-green-500 flex-shrink-0" />
                          {benefit}
                        </li>
                      ))
                    ) : (
                      <li className="text-gray-500">No benefits available</li>
                    )}
                  </ul>
                </div>
              </div>
            </div>

            {/* Corporate Information */}
            {preferences.corporateAccount && preferences.corporateAccount !== 'N/A' && (
              <div className="card">
                <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
                  <Building size={20} />
                  Corporate Information
                </h2>
                <div className="space-y-3">
                  <div>
                    <p className="text-sm text-gray-600 dark:text-gray-400">Company</p>
                    <p className="font-medium">{preferences.corporateAccount}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 dark:text-gray-400">Account Manager</p>
                    <p className="font-medium">{preferences.accountManager || 'N/A'}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 dark:text-gray-400">Payment Terms</p>
                    <p className="font-medium">{preferences.paymentTerms || 'N/A'}</p>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default GuestDetail;