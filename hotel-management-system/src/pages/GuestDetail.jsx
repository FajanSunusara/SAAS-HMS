import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, Mail, Phone, MapPin, Calendar, DollarSign, Star, 
  CreditCard, FileText, Plus, Edit, Bed, User, Map, Home, 
  Clock, CheckCircle, XCircle, AlertCircle, Copy, Download,
  Shield, Award, Gift, Coffee, Wind, Newspaper, Thermometer,
  Upload, Eye, Hash, Building, Briefcase, Globe, Heart,
  TrendingUp, Activity, History, CreditCard as CreditCardIcon,
  ShoppingBag, Settings, UserCheck, Receipt
} from 'lucide-react';
import { useState } from 'react';

const GuestDetail = () => {
  const { guestId } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('overview');

  // Guest Status Options: 'checked-in', 'checked-out', 'upcoming', 'walk-in', 'blacklisted'
  const [guestStatus, setGuestStatus] = useState('checked-in');

  // API: GET /api/guests/{guestId} - Get guest details
  // API: GET /api/guests/{guestId}/identity - Get identity info
  // API: GET /api/guests/{guestId}/current-stay - Get current stay
  // API: GET /api/guests/{guestId}/booking-history - Get booking history
  // API: GET /api/guests/{guestId}/activity - Get activity timeline

  const guest = {
    id: guestId,
    name: 'John Doe',
    email: 'john.doe@email.com',
    phone: '+1 (555) 123-4567',
    gender: 'Male',
    dateOfBirth: 'Jan 15, 1985',
    nationality: 'American',
    profilePhoto: 'https://ui-avatars.com/api/?name=John+Doe&size=128',
    status: guestStatus,
    guestId: 'GUEST-84729',
    memberSince: 'January 15, 2022',
    loyaltyTier: 'Gold Member',
    loyaltyPoints: 12450,
    totalStays: 8,
    lifetimeValue: '$12,450.50',
    vipLevel: 'VIP',
    corporateAccount: 'TechCorp Inc.',
    accountManager: 'Sarah Johnson'
  };

  const identity = {
    idType: 'Passport',
    idNumber: 'P987654321',
    issuingAuthority: 'US Department of State',
    issueCountry: 'United States',
    issueDate: 'Jun 15, 2020',
    expiryDate: 'Jun 14, 2030',
    documentUrl: 'https://example.com/documents/passport.jpg',
    verified: true,
    verificationDate: 'Oct 25, 2023 10:30 AM',
    verifiedBy: 'Michael Chen'
  };

  const address = {
    line1: '123 Park Avenue',
    line2: 'Suite 1504',
    city: 'New York',
    state: 'NY',
    country: 'United States',
    zipCode: '10022',
    type: 'Home'
  };

  const preferences = {
    bedType: 'King Size',
    pillow: 'Firm (2 extra)',
    roomTemp: '22°C',
    floorLevel: 'High Floor (12+)',
    smoking: 'Non-Smoking',
    newspaper: 'Wall Street Journal',
    amenities: 'Coffee maker, Extra towels, Bathrobe',
    food: 'Vegetarian options preferred',
    allergies: 'Peanuts, Shellfish',
    specialRequests: 'Early check-in when available, Quiet room away from elevator',
    vipNotes: 'Anniversary celebration - arrange champagne & flowers'
  };

  const currentStay = {
    id: 'STAY-928374',
    roomNumber: '1204',
    roomType: 'Deluxe King',
    floor: 12,
    view: 'City View',
    checkIn: 'Oct 26, 2023 14:00',
    checkOut: 'Nov 02, 2023 11:00',
    nights: 7,
    nightsElapsed: 4,
    nightsRemaining: 3,
    ratePlan: 'Corporate Flexible Rate',
    ratePerNight: 180,
    includes: ['Breakfast', 'WiFi', 'Gym access'],
    bookingSource: 'Direct Website',
    bookedBy: 'Jane Smith (Corporate Travel)',
    specialInstructions: 'Early check-in requested, Deliver newspaper at 7 AM'
  };

  const financialSummary = {
    roomCharges: 1260.00,
    serviceCharges: 115.50,
    taxes: 180.25,
    totalCharges: 1555.75,
    advancePaid: 500.00,
    pendingAmount: 1055.75,
    creditLimit: 5000.00,
    availableCredit: 3944.25,
    lastPaymentDate: 'Oct 26, 2023',
    paymentMethod: 'Corporate Account',
    dueDate: currentStay.checkOut
  };

  const bookingHistory = [
    { 
      id: 'BK-843921', 
      dates: 'May 12, 2023 - May 15, 2023', 
      roomType: 'Standard Queen', 
      room: '405', 
      status: 'Checked Out', 
      total: '$450.00',
      revenue: 450.00,
      source: 'Booking.com',
      notes: 'Business trip'
    },
    { 
      id: 'BK-759234', 
      dates: 'Dec 22, 2022 - Dec 26, 2022', 
      roomType: 'Deluxe King', 
      room: '1208', 
      status: 'Checked Out', 
      total: '$980.00',
      revenue: 980.00,
      source: 'Direct',
      notes: 'Family vacation'
    },
    { 
      id: 'BK-612598', 
      dates: 'Jan 05, 2022 - Jan 08, 2022', 
      roomType: 'Standard Queen', 
      room: '312', 
      status: 'Cancelled', 
      total: '$425.50',
      revenue: 100.00,
      source: 'Expedia',
      notes: 'Cancellation fee applied'
    },
    { 
      id: 'BK-498723', 
      dates: 'Aug 15, 2021 - Aug 18, 2021', 
      roomType: 'Executive Suite', 
      room: '1502', 
      status: 'Checked Out', 
      total: '$1,250.00',
      revenue: 1250.00,
      source: 'Corporate',
      notes: 'VIP treatment requested'
    },
  ];

  const paymentHistory = [
    { date: 'Oct 26, 2023 14:30', transactionId: 'TXN-847291', amount: '$500.00', method: 'Credit Card', purpose: 'Advance Deposit', invoice: 'INV-921734' },
    { date: 'May 15, 2023 10:15', transactionId: 'TXN-736482', amount: '$450.00', method: 'Debit Card', purpose: 'Final Bill', invoice: 'INV-843921' },
    { date: 'Dec 26, 2022 09:45', transactionId: 'TXN-619384', amount: '$980.00', method: 'Corporate Billing', purpose: 'Final Bill', invoice: 'INV-759234' },
    { date: 'Jan 08, 2022 11:20', transactionId: 'TXN-527491', amount: '$100.00', method: 'Cash', purpose: 'Cancellation Fee', invoice: 'INV-612598' },
  ];

  const serviceHistory = [
    { date: 'Oct 28, 2023 10:30', service: 'Laundry Service', description: '2 Shirts, 1 Trouser (Express)', charge: '$25.00', status: 'Delivered' },
    { date: 'Oct 27, 2023 19:45', service: 'Room Service', description: 'Dinner - Steak + Wine', charge: '$68.50', status: 'Billed' },
    { date: 'Oct 26, 2023 15:20', service: 'Minibar Restock', description: 'Soda, Water, Snacks', charge: '$22.00', status: 'Billed' },
    { date: 'Oct 26, 2023 14:45', service: 'Extra Pillows', description: '2 Additional pillows', charge: '$0.00', status: 'Fulfilled' },
  ];

  const activityTimeline = [
    { 
      time: 'Oct 28, 2023 10:30', 
      event: 'Laundry Service Delivered', 
      staff: 'Housekeeping - Maria',
      icon: 'service',
      type: 'service'
    },
    { 
      time: 'Oct 27, 2023 19:45', 
      event: 'Dinner Ordered via Room Service', 
      staff: 'Room Service - Kitchen',
      icon: 'food',
      type: 'charge'
    },
    { 
      time: 'Oct 27, 2023 15:30', 
      event: 'Payment Received - $500.00', 
      staff: 'Front Desk - Sarah',
      icon: 'payment',
      type: 'payment'
    },
    { 
      time: 'Oct 26, 2023 14:15', 
      event: 'Guest Checked-in', 
      staff: 'Reception - John',
      icon: 'checkin',
      type: 'checkin'
    },
  ];

  // Get status badge color
  const getStatusBadgeClass = (status) => {
    switch(status) {
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

  return (
    <div className="space-y-6 p-4 md:p-6">
      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
      >
        <ArrowLeft size={20} />
        Back to Guests
      </button>

      {/* Guest Identity Header */}
      <div className="card">
        <div className="flex flex-col md:flex-row md:items-start justify-between gap-6">
          <div className="flex items-start gap-4">
            <img
              src={guest.profilePhoto}
              alt={guest.name}
              className="w-20 h-20 md:w-24 md:h-24 rounded-full object-cover border-4 border-white dark:border-gray-800 shadow-lg"
            />
            <div>
              <div className="flex flex-wrap items-center gap-3 mb-2">
                <h1 className="text-2xl md:text-3xl font-bold">{guest.name}</h1>
                <div className="flex flex-wrap gap-2">
                  <span className={`px-3 py-1 rounded-full text-xs font-medium flex items-center gap-1 ${getStatusBadgeClass(guest.status)}`}>
                    {guest.status === 'checked-in' && '✓'}
                    {guest.status.charAt(0).toUpperCase() + guest.status.slice(1).replace('-', ' ')}
                  </span>
                  <span className="px-3 py-1 bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400 rounded-full text-xs font-medium flex items-center gap-1">
                    <Star size={12} fill="currentColor" />
                    {guest.vipLevel}
                  </span>
                  <span className="px-3 py-1 bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400 rounded-full text-xs font-medium">
                    {guest.loyaltyTier}
                  </span>
                </div>
              </div>
              
              <div className="flex flex-wrap items-center gap-4 text-sm text-gray-600 dark:text-gray-400">
                <div className="flex items-center gap-1">
                  <Hash size={14} />
                  <span>ID: {guest.guestId}</span>
                </div>
                {guest.corporateAccount && (
                  <div className="flex items-center gap-1">
                    <Building size={14} />
                    <span>{guest.corporateAccount}</span>
                  </div>
                )}
                <div className="flex items-center gap-1">
                  <TrendingUp size={14} />
                  <span>LTV: {guest.lifetimeValue}</span>
                </div>
                <div className="flex items-center gap-1">
                  <Calendar size={14} />
                  <span>Stays: {guest.totalStays}</span>
                </div>
              </div>

              <div className="flex flex-wrap gap-2 mt-3">
                <a href={`mailto:${guest.email}`} className="flex items-center gap-2 text-sm text-primary-600 hover:text-primary-700 transition-colors">
                  <Mail size={16} />
                  <span>{guest.email}</span>
                </a>
                <a href={`tel:${guest.phone}`} className="flex items-center gap-2 text-sm text-primary-600 hover:text-primary-700 transition-colors">
                  <Phone size={16} />
                  <span>{guest.phone}</span>
                </a>
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="flex flex-wrap gap-2">
            <button 
              onClick={() => navigate(`/guests/${guestId}/edit`)}
              className="btn-secondary flex items-center gap-2"
            >
              <Edit size={18} />
              Edit Guest
            </button>
            <button className="btn-primary flex items-center gap-2">
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
                onClick={() => setActiveTab(tab.id)}
                className={`px-4 py-3 font-medium border-b-2 transition-colors flex items-center gap-2 whitespace-nowrap ${
                  activeTab === tab.id
                    ? 'border-primary-600 text-primary-600'
                    : 'border-transparent text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
                }`}
              >
                <Icon size={18} />
                {tab.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && (
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
                <button className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1">
                  <Edit size={16} />
                  Edit
                </button>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Full Name</label>
                  <p className="font-medium">{guest.name}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Gender</label>
                  <p className="font-medium">{guest.gender}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Date of Birth</label>
                  <p className="font-medium">{guest.dateOfBirth}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Nationality</label>
                  <p className="font-medium">{guest.nationality}</p>
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Contact Information</label>
                  <div className="flex flex-wrap gap-4 mt-2">
                    <div className="flex items-center gap-2">
                      <Phone size={16} className="text-gray-500" />
                      <span>{guest.phone}</span>
                      <button className="ml-2 text-gray-500 hover:text-gray-700">
                        <Copy size={14} />
                      </button>
                    </div>
                    <div className="flex items-center gap-2">
                      <Mail size={16} className="text-gray-500" />
                      <span>{guest.email}</span>
                      <button className="ml-2 text-gray-500 hover:text-gray-700">
                        <Copy size={14} />
                      </button>
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
                  <p className="font-medium">{identity.idType}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">ID Number</label>
                  <div className="flex items-center gap-2">
                    <p className="font-mono font-medium">{identity.idNumber}</p>
                    <button className="text-gray-500 hover:text-gray-700">
                      <Copy size={14} />
                    </button>
                  </div>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Issuing Authority</label>
                  <p className="font-medium">{identity.issuingAuthority}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Issue Country</label>
                  <p className="font-medium">{identity.issueCountry}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Validity</label>
                  <p className="font-medium">{identity.issueDate} - {identity.expiryDate}</p>
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-400 mb-1">Verification</label>
                  <p className="text-sm">
                    {identity.verified ? `Verified on ${identity.verificationDate} by ${identity.verifiedBy}` : 'Pending verification'}
                  </p>
                </div>
              </div>
              {identity.documentUrl && (
                <div className="mt-4 pt-4 border-t dark:border-gray-700">
                  <div className="flex items-center justify-between">
                    <label className="block text-sm font-medium mb-2">Document Preview</label>
                    <button className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1">
                      <Download size={16} />
                      Download
                    </button>
                  </div>
                  <div className="relative h-40 bg-gray-100 dark:bg-gray-800 rounded-lg overflow-hidden">
                    <img 
                      src={identity.documentUrl} 
                      alt="ID Document" 
                      className="w-full h-full object-contain"
                    />
                    <button className="absolute top-2 right-2 bg-white/80 dark:bg-gray-900/80 p-2 rounded-lg hover:bg-white dark:hover:bg-gray-900">
                      <Eye size={18} />
                    </button>
                  </div>
                </div>
              )}
            </div>

            {/* Address Information */}
            <div className="card">
              <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
                <Home size={20} />
                Address Information
              </h2>
              <div className="space-y-3">
                <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                  <p className="font-medium">{address.line1}</p>
                  {address.line2 && <p>{address.line2}</p>}
                  <p>{address.city}, {address.state} {address.zipCode}</p>
                  <p>{address.country}</p>
                  <div className="flex items-center justify-between mt-3">
                    <span className="text-sm text-gray-600 dark:text-gray-400">{address.type} Address</span>
                    <button className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1">
                      <Copy size={14} />
                      Copy Address
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* RIGHT COLUMN - Live Operations & Finance */}
          <div className="space-y-6">
            {/* Current Stay Summary */}
            {guest.status === 'checked-in' && (
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
                        <p className="text-xl font-bold">{currentStay.floor}</p>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4 mt-4">
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Check-in</p>
                        <p className="font-medium">{currentStay.checkIn}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Check-out</p>
                        <p className="font-medium">{currentStay.checkOut}</p>
                      </div>
                    </div>
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
                  </div>

                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-gray-600 dark:text-gray-400">Rate Plan</span>
                      <span className="font-medium">{currentStay.ratePlan}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-gray-600 dark:text-gray-400">Rate</span>
                      <span className="font-medium">${currentStay.ratePerNight}/night</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-gray-600 dark:text-gray-400">Includes</span>
                      <span className="text-sm text-right">{currentStay.includes.join(', ')}</span>
                    </div>
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
                    <span className="font-medium">${financialSummary.roomCharges.toFixed(2)}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Service Charges</span>
                    <span className="font-medium">${financialSummary.serviceCharges.toFixed(2)}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Taxes & Fees</span>
                    <span className="font-medium">${financialSummary.taxes.toFixed(2)}</span>
                  </div>
                  <div className="border-t dark:border-gray-700 pt-2">
                    <div className="flex items-center justify-between font-semibold">
                      <span>Total Charges</span>
                      <span>${financialSummary.totalCharges.toFixed(2)}</span>
                    </div>
                  </div>
                </div>

                <div className="pt-4 border-t dark:border-gray-700">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm">Advance Paid</span>
                    <span className="font-medium text-green-600">${financialSummary.advancePaid.toFixed(2)}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-semibold">Pending Amount</span>
                    <span className="text-xl font-bold text-red-600">${financialSummary.pendingAmount.toFixed(2)}</span>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">Due at checkout: {financialSummary.dueDate}</p>
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
                {activityTimeline.map((activity, index) => (
                  <div key={index} className="flex items-start">
                    <div className="flex-shrink-0 w-8 h-8 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center">
                      {getEventIcon(activity.icon)}
                    </div>
                    <div className="ml-4 flex-1">
                      <div className="flex items-center justify-between">
                        <p className="text-sm font-medium">{activity.event}</p>
                        <span className="text-xs text-gray-500">{activity.time.split(' ')[1]}</span>
                      </div>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                        {activity.time.split(' ')[0]} • By {activity.staff}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
              <button className="w-full mt-4 text-sm text-primary-600 hover:text-primary-700">
                Show All Activity
              </button>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'current-stay' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Stay Details */}
          <div className="lg:col-span-2 card">
            <h2 className="text-xl font-semibold mb-4">Current Stay Details</h2>
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
                    <span className="font-medium">{currentStay.roomType}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Floor</span>
                    <span className="font-medium">{currentStay.floor}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">View</span>
                    <span className="font-medium">{currentStay.view}</span>
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
                    <p className="text-2xl font-bold">{currentStay.nights} Nights</p>
                  </div>
                </div>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Check-in</span>
                    <span className="font-medium">{currentStay.checkIn}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Check-out</span>
                    <span className="font-medium">{currentStay.checkOut}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Days Remaining</span>
                    <span className="font-medium text-red-600">{currentStay.nightsRemaining} days</span>
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
                    <p className="text-2xl font-bold">{currentStay.ratePlan}</p>
                  </div>
                </div>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Rate per Night</span>
                    <span className="font-medium">${currentStay.ratePerNight}/night</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Total Stay Cost</span>
                    <span className="font-medium">${(currentStay.ratePerNight * currentStay.nights).toFixed(2)}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Includes</span>
                    <span className="text-sm text-right">{currentStay.includes.join(', ')}</span>
                  </div>
                </div>
              </div>

              <div className="bg-purple-50 dark:bg-purple-900/20 p-5 rounded-xl">
                <div className="flex items-center gap-3 mb-4">
                  <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/40 rounded-lg flex items-center justify-center">
                    <Receipt size={24} className="text-purple-600" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 dark:text-gray-400">Booking Details</p>
                    <p className="text-2xl font-bold">{currentStay.id}</p>
                  </div>
                </div>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Booking Source</span>
                    <span className="font-medium">{currentStay.bookingSource}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Booked By</span>
                    <span className="font-medium">{currentStay.bookedBy}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Special Instructions</span>
                    <span className="text-sm text-right">{currentStay.specialInstructions}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-6">
              <button className="btn-primary w-full">View Full Folio</button>
            </div>
          </div>

          {/* Payment Summary */}
          <div className="card">
            <h2 className="text-xl font-semibold mb-4">Payment Summary</h2>
            <div className="space-y-6">
              <div className="p-4 bg-red-50 dark:bg-red-900/20 rounded-lg">
                <p className="text-sm text-gray-600 dark:text-gray-400">Current Balance</p>
                <p className="text-3xl font-bold text-red-600">${financialSummary.pendingAmount.toFixed(2)}</p>
                <p className="text-xs text-gray-500">Due at check-out ({currentStay.checkOut})</p>
              </div>
              
              <div className="space-y-4">
                <div className="p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Total Paid</p>
                  <p className="text-xl font-semibold text-green-600">${financialSummary.advancePaid.toFixed(2)}</p>
                  <p className="text-xs text-gray-500">Advance deposit</p>
                </div>
                
                <div className="p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Credit Limit</p>
                  <p className="text-xl font-semibold">${financialSummary.creditLimit.toFixed(2)}</p>
                  <p className="text-xs text-gray-500">Available: ${financialSummary.availableCredit.toFixed(2)}</p>
                </div>
              </div>
              
              <div className="space-y-3">
                <button className="btn-primary w-full">Process Payment</button>
                <button className="btn-outline w-full">View All Charges</button>
                <button className="btn-secondary w-full">Extend Stay</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'stay-history' && (
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
                        <button className="text-gray-500 hover:text-gray-700">
                          <Copy size={14} />
                        </button>
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <div className="text-sm">{stay.dates}</div>
                      <div className="text-xs text-gray-500">{stay.nights || '3'} nights</div>
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
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                        stay.status === 'Checked Out' 
                          ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
                          : stay.status === 'Cancelled'
                          ? 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
                          : 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
                      }`}>
                        {stay.status}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="font-semibold">{stay.total}</div>
                      <div className="text-xs text-gray-500">Revenue: ${stay.revenue.toFixed(2)}</div>
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
                Showing {bookingHistory.length} of {bookingHistory.length} stays
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

      {activeTab === 'payments' && (
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
          
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 dark:bg-gray-700/50">
                <tr>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Date & Time</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Transaction ID</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Amount</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Payment Method</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold">Purpose</th>
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
                        <button className="text-gray-500 hover:text-gray-700">
                          <Copy size={14} />
                        </button>
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <div className={`font-semibold ${payment.amount.startsWith('$') ? 'text-green-600' : 'text-red-600'}`}>
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
                        payment.purpose.includes('Deposit') 
                          ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
                          : payment.purpose.includes('Final')
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
              <p className="text-2xl font-bold">$2,030.00</p>
            </div>
            <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
              <p className="text-sm text-gray-600 dark:text-gray-400">Outstanding Balance</p>
              <p className="text-2xl font-bold text-red-600">${financialSummary.pendingAmount.toFixed(2)}</p>
            </div>
            <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
              <p className="text-sm text-gray-600 dark:text-gray-400">Payment Methods Used</p>
              <p className="text-xl font-bold">3</p>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'services' && (
        <div className="card">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold">Service History (Current Stay)</h2>
            <button className="btn-primary flex items-center gap-2">
              <Plus size={18} />
              New Service Request
            </button>
          </div>
          
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
              <p className="text-2xl font-bold">{serviceHistory.length}</p>
            </div>
            <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
              <p className="text-sm text-gray-600 dark:text-gray-400">Completed</p>
              <p className="text-2xl font-bold">2</p>
            </div>
            <div className="p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
              <p className="text-sm text-gray-600 dark:text-gray-400">Pending</p>
              <p className="text-2xl font-bold">1</p>
            </div>
            <div className="p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
              <p className="text-sm text-gray-600 dark:text-gray-400">Total Charges</p>
              <p className="text-2xl font-bold">$115.50</p>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'preferences' && (
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
              {Object.entries(preferences).map(([key, value]) => (
                <div key={key} className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
                  <div>
                    <span className="text-sm font-medium capitalize">{key.replace(/([A-Z])/g, ' $1').trim()}</span>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">{value}</p>
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
                  <p className="text-xl font-semibold">{guest.loyaltyTier}</p>
                  <p className="text-xs text-gray-500">Since: {guest.memberSince}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Total Points</p>
                  <p className="text-2xl font-bold text-primary-600">{guest.loyaltyPoints.toLocaleString()}</p>
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
                    <li className="flex items-center gap-2">
                      <CheckCircle size={14} className="text-green-500 flex-shrink-0" />
                      Free room upgrade (subject to availability)
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle size={14} className="text-green-500 flex-shrink-0" />
                      Late checkout until 2:00 PM
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle size={14} className="text-green-500 flex-shrink-0" />
                      Welcome drink on arrival
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle size={14} className="text-green-500 flex-shrink-0" />
                      10% discount on F&B
                    </li>
                  </ul>
                </div>
              </div>
            </div>

            {/* Corporate Information */}
            {guest.corporateAccount && (
              <div className="card">
                <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
                  <Building size={20} />
                  Corporate Information
                </h2>
                <div className="space-y-3">
                  <div>
                    <p className="text-sm text-gray-600 dark:text-gray-400">Company</p>
                    <p className="font-medium">{guest.corporateAccount}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 dark:text-gray-400">Account Manager</p>
                    <p className="font-medium">{guest.accountManager}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 dark:text-gray-400">Payment Terms</p>
                    <p className="font-medium">Net 30 • Credit Limit: ${financialSummary.creditLimit.toFixed(2)}</p>
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