import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Calendar, Plus, Filter, Search, TrendingUp, TrendingDown, 
  Download, Eye, Edit, List, BarChart3, X, ChevronLeft, 
  ChevronRight, Users, Home, DollarSign, Bed, Check, 
  Clock, AlertCircle, ChevronDown, ChevronUp, ArrowUpDown,
  Grid, ChevronRight as ChevronRightIcon, ChevronLeft as ChevronLeftIcon
} from 'lucide-react';

const ReservationBooking = () => {
  const navigate = useNavigate();
  
  // State Management
  const [selectedFilter, setSelectedFilter] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [activeView, setActiveView] = useState('calendar');
  const [showFilters, setShowFilters] = useState(false);
  const [currentCenterDate, setCurrentCenterDate] = useState(new Date('2024-08-07'));
  const [currentMonth, setCurrentMonth] = useState(new Date('2024-08-01'));
  const [appliedFilters, setAppliedFilters] = useState({
    roomType: [],
    floor: [],
    status: [],
    amenities: [],
    source: [],
    paymentStatus: []
  });
  const [sortConfig, setSortConfig] = useState({ key: 'time', direction: 'asc' });

  // Calculate date range based on center date (3 days before and after)
  const getWeekDates = (centerDate) => {
    const dates = [];
    const center = new Date(centerDate);
    
    // Get 3 days before
    for (let i = 3; i > 0; i--) {
      const date = new Date(center);
      date.setDate(center.getDate() - i);
      dates.push(date);
    }
    
    // Add center date
    dates.push(new Date(center));
    
    // Get 3 days after
    for (let i = 1; i <= 3; i++) {
      const date = new Date(center);
      date.setDate(center.getDate() + i);
      dates.push(date);
    }
    
    return dates;
  };

  // Format date range for display
  const formatWeekRange = (dates) => {
    if (dates.length < 7) return '';
    const start = dates[0];
    const end = dates[6];
    return `${start.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} - ${end.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`;
  };

  // Navigation functions
  const goToPreviousDay = () => {
    const newDate = new Date(currentCenterDate);
    newDate.setDate(newDate.getDate() - 1);
    setCurrentCenterDate(newDate);
  };

  const goToNextDay = () => {
    const newDate = new Date(currentCenterDate);
    newDate.setDate(newDate.getDate() + 1);
    setCurrentCenterDate(newDate);
  };

  const goToPreviousWeek = () => {
    const newDate = new Date(currentCenterDate);
    newDate.setDate(newDate.getDate() - 7);
    setCurrentCenterDate(newDate);
  };

  const goToNextWeek = () => {
    const newDate = new Date(currentCenterDate);
    newDate.setDate(newDate.getDate() + 7);
    setCurrentCenterDate(newDate);
  };

  const goToToday = () => {
    setCurrentCenterDate(new Date());
    setCurrentMonth(new Date());
  };

  // Month navigation
  const goToPreviousMonth = () => {
    const newDate = new Date(currentMonth);
    newDate.setMonth(newDate.getMonth() - 1);
    setCurrentMonth(newDate);
  };

  const goToNextMonth = () => {
    const newDate = new Date(currentMonth);
    newDate.setMonth(newDate.getMonth() + 1);
    setCurrentMonth(newDate);
  };

  // Handle date selection from input
  const handleDateSelect = (dateString) => {
    const selectedDate = new Date(dateString);
    setCurrentCenterDate(selectedDate);
  };

  // Get current week dates
  const currentWeekDates = getWeekDates(currentCenterDate);
  const weekRangeString = formatWeekRange(currentWeekDates);

  // Dashboard Stats
  const stats = [
    { 
      label: "Today's Arrivals", 
      value: 12, 
      icon: Users,
      change: '+2', 
      trending: 'up',
      description: 'Expected check-ins',
      color: 'text-blue-500',
      bgColor: 'bg-blue-50 dark:bg-blue-900/20'
    },
    { 
      label: "Today's Departures", 
      value: 8, 
      icon: Home,
      change: '-1', 
      trending: 'down',
      description: 'Expected check-outs',
      color: 'text-green-500',
      bgColor: 'bg-green-50 dark:bg-green-900/20'
    },
    { 
      label: 'Occupancy Rate', 
      value: '84%', 
      icon: Bed,
      change: '+3%',
      trending: 'up',
      description: 'Current occupancy',
      color: 'text-purple-500',
      bgColor: 'bg-purple-50 dark:bg-purple-900/20'
    },
    { 
      label: 'Revenue Today', 
      value: '$2,845', 
      icon: DollarSign,
      change: '+12.5%', 
      trending: 'up',
      description: 'Earnings today',
      color: 'text-yellow-500',
      bgColor: 'bg-yellow-50 dark:bg-yellow-900/20'
    },
  ];

  // Booking Status Options
  const bookingStatus = [
    { label: 'Confirmed', count: 18, color: 'bg-blue-500', value: 'confirmed' },
    { label: 'Checked In', count: 15, color: 'bg-green-500', value: 'checked-in' },
    { label: 'Tentative', count: 8, color: 'bg-yellow-500', value: 'tentative' },
    { label: 'No Show', count: 4, color: 'bg-red-500', value: 'no-show' },
    { label: 'Cancelled', count: 3, color: 'bg-gray-500', value: 'cancelled' },
  ];

  // Filter Options
  const filterOptions = {
    roomType: ['Standard', 'Deluxe', 'Suite', 'Presidential'],
    floor: ['1', '2', '3', '4', '5'],
    amenities: ['WiFi', 'TV', 'AC', 'Mini Bar', 'Balcony', 'Sea View', 'Jacuzzi'],
    source: ['Website', 'OTA', 'Phone', 'Travel Agent', 'Walk-in'],
    paymentStatus: ['Paid', 'Pending', 'Partially Paid', 'Refunded'],
  };

  // Original Data
  const allRooms = [
    { id: 101, type: 'Standard', floor: '1', price: 120, amenities: ['WiFi', 'TV'], maxOccupancy: 2 },
    { id: 102, type: 'Standard', floor: '1', price: 120, amenities: ['WiFi', 'TV'], maxOccupancy: 2 },
    { id: 103, type: 'Standard', floor: '1', price: 120, amenities: ['WiFi', 'TV'], maxOccupancy: 2 },
    { id: 104, type: 'Standard', floor: '1', price: 120, amenities: ['WiFi', 'TV'], maxOccupancy: 2 },
    { id: 201, type: 'Deluxe', floor: '2', price: 180, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar'], maxOccupancy: 3 },
    { id: 202, type: 'Deluxe', floor: '2', price: 180, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar'], maxOccupancy: 3 },
    { id: 203, type: 'Deluxe', floor: '2', price: 180, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar'], maxOccupancy: 3 },
    { id: 204, type: 'Deluxe', floor: '2', price: 180, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar'], maxOccupancy: 3 },
    { id: 301, type: 'Suite', floor: '3', price: 250, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar', 'Balcony'], maxOccupancy: 4 },
    { id: 302, type: 'Suite', floor: '3', price: 250, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar', 'Balcony'], maxOccupancy: 4 },
    { id: 303, type: 'Suite', floor: '3', price: 250, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar', 'Balcony'], maxOccupancy: 4 },
    { id: 401, type: 'Presidential', floor: '4', price: 450, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar', 'Balcony', 'Jacuzzi'], maxOccupancy: 6 },
    { id: 402, type: 'Presidential', floor: '4', price: 450, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar', 'Balcony', 'Jacuzzi'], maxOccupancy: 6 },
    { id: 501, type: 'Suite', floor: '5', price: 300, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar', 'Balcony', 'Sea View'], maxOccupancy: 4 },
    { id: 502, type: 'Suite', floor: '5', price: 300, amenities: ['WiFi', 'TV', 'AC', 'Mini Bar', 'Balcony', 'Sea View'], maxOccupancy: 4 },
  ];

  // Sample bookings for calendar view - Updated with guestId
  const calendarBookings = [
    { id: 1, roomId: 101, guest: 'John Anderson', guestId: '1', checkIn: '2024-08-07', checkOut: '2024-08-10', status: 'confirmed', adults: 2, children: 1, source: 'Website', payment: 'Paid' },
    { id: 2, roomId: 201, guest: 'Sarah Williams', guestId: '2', checkIn: '2024-08-07', checkOut: '2024-08-09', status: 'checked-in', adults: 2, children: 0, source: 'OTA', payment: 'Paid' },
    { id: 3, roomId: 301, guest: 'Michael Brown', guestId: '3', checkIn: '2024-08-07', checkOut: '2024-08-12', status: 'tentative', adults: 3, children: 1, source: 'Phone', payment: 'Pending' },
    { id: 4, roomId: 102, guest: 'Emily Davis', guestId: '4', checkIn: '2024-08-07', checkOut: '2024-08-08', status: 'confirmed', adults: 1, children: 0, source: 'Website', payment: 'Paid' },
    { id: 5, roomId: 401, guest: 'Robert Johnson', guestId: '5', checkIn: '2024-08-07', checkOut: '2024-08-14', status: 'no-show', adults: 2, children: 2, source: 'Travel Agent', payment: 'Refunded' },
    { id: 6, roomId: 202, guest: 'David Wilson', guestId: '6', checkIn: '2024-08-06', checkOut: '2024-08-08', status: 'confirmed', adults: 2, children: 0, source: 'Website', payment: 'Paid' },
    { id: 7, roomId: 302, guest: 'Lisa Martinez', guestId: '7', checkIn: '2024-08-08', checkOut: '2024-08-11', status: 'confirmed', adults: 2, children: 1, source: 'OTA', payment: 'Partially Paid' },
    { id: 8, roomId: 103, guest: 'James Taylor', guestId: '8', checkIn: '2024-08-09', checkOut: '2024-08-10', status: 'checked-in', adults: 1, children: 0, source: 'Walk-in', payment: 'Paid' },
    { id: 9, roomId: 203, guest: 'Maria Garcia', guestId: '9', checkIn: '2024-08-05', checkOut: '2024-08-07', status: 'cancelled', adults: 2, children: 0, source: 'Website', payment: 'Refunded' },
    { id: 10, roomId: 501, guest: 'Thomas Clark', guestId: '10', checkIn: '2024-08-10', checkOut: '2024-08-15', status: 'tentative', adults: 3, children: 0, source: 'Travel Agent', payment: 'Pending' },
  ];

  // List View Reservations
  const allReservations = [
    {
      id: 1,
      bookingId: 'BK-2024-0012',
      guestId: '1',
      time: '09:00 AM',
      guest: 'John Anderson',
      room: '101',
      type: 'Standard',
      status: 'Confirmed',
      checkIn: '2024-08-07',
      checkOut: '2024-08-10',
      nights: 3,
      amount: '$360.00',
      paymentStatus: 'Paid',
      source: 'Website',
      floor: '1',
      specialRequests: 'Late check-in requested'
    },
    {
      id: 2,
      bookingId: 'BK-2024-0013',
      guestId: '2',
      time: '11:30 AM',
      guest: 'Sarah Williams',
      room: '201',
      type: 'Deluxe',
      status: 'Checked In',
      checkIn: '2024-08-07',
      checkOut: '2024-08-09',
      nights: 2,
      amount: '$360.00',
      paymentStatus: 'Paid',
      source: 'OTA',
      floor: '2',
      specialRequests: 'Non-smoking room'
    },
    {
      id: 3,
      bookingId: 'BK-2024-0014',
      guestId: '3',
      time: '02:00 PM',
      guest: 'Michael Brown',
      room: '301',
      type: 'Suite',
      status: 'Tentative',
      checkIn: '2024-08-07',
      checkOut: '2024-08-12',
      nights: 5,
      amount: '$1,250.00',
      paymentStatus: 'Pending',
      source: 'Phone',
      floor: '3',
      specialRequests: 'Anniversary celebration'
    },
    {
      id: 4,
      bookingId: 'BK-2024-0015',
      guestId: '4',
      time: '03:30 PM',
      guest: 'Emily Davis',
      room: '102',
      type: 'Standard',
      status: 'Confirmed',
      checkIn: '2024-08-07',
      checkOut: '2024-08-08',
      nights: 1,
      amount: '$120.00',
      paymentStatus: 'Paid',
      source: 'Website',
      floor: '1',
      specialRequests: 'Early check-in if possible'
    },
    {
      id: 5,
      bookingId: 'BK-2024-0016',
      guestId: '5',
      time: '04:45 PM',
      guest: 'Robert Johnson',
      room: '401',
      type: 'Presidential',
      status: 'No Show',
      checkIn: '2024-08-07',
      checkOut: '2024-08-14',
      nights: 7,
      amount: '$3,150.00',
      paymentStatus: 'Refunded',
      source: 'Travel Agent',
      floor: '4',
      specialRequests: 'Airport pickup required'
    },
    {
      id: 6,
      bookingId: 'BK-2024-0017',
      guestId: '6',
      time: '10:15 AM',
      guest: 'David Wilson',
      room: '202',
      type: 'Deluxe',
      status: 'Confirmed',
      checkIn: '2024-08-06',
      checkOut: '2024-08-08',
      nights: 2,
      amount: '$360.00',
      paymentStatus: 'Paid',
      source: 'Website',
      floor: '2',
      specialRequests: 'High floor preferred'
    },
    {
      id: 7,
      bookingId: 'BK-2024-0018',
      guestId: '7',
      time: '01:20 PM',
      guest: 'Lisa Martinez',
      room: '302',
      type: 'Suite',
      status: 'Confirmed',
      checkIn: '2024-08-08',
      checkOut: '2024-08-11',
      nights: 3,
      amount: '$750.00',
      paymentStatus: 'Partially Paid',
      source: 'OTA',
      floor: '3',
      specialRequests: 'Connecting room if available'
    },
    {
      id: 8,
      bookingId: 'BK-2024-0019',
      guestId: '8',
      time: '06:00 PM',
      guest: 'James Taylor',
      room: '103',
      type: 'Standard',
      status: 'Checked In',
      checkIn: '2024-08-09',
      checkOut: '2024-08-10',
      nights: 1,
      amount: '$120.00',
      paymentStatus: 'Paid',
      source: 'Walk-in',
      floor: '1',
      specialRequests: 'Late check-out requested'
    },
    {
      id: 9,
      bookingId: 'BK-2024-0020',
      guestId: '9',
      time: '09:45 AM',
      guest: 'Maria Garcia',
      room: '203',
      type: 'Deluxe',
      status: 'Cancelled',
      checkIn: '2024-08-05',
      checkOut: '2024-08-07',
      nights: 2,
      amount: '$360.00',
      paymentStatus: 'Refunded',
      source: 'Website',
      floor: '2',
      specialRequests: ''
    },
    {
      id: 10,
      bookingId: 'BK-2024-0021',
      guestId: '10',
      time: '03:00 PM',
      guest: 'Thomas Clark',
      room: '501',
      type: 'Suite',
      status: 'Tentative',
      checkIn: '2024-08-10',
      checkOut: '2024-08-15',
      nights: 5,
      amount: '$1,500.00',
      paymentStatus: 'Pending',
      source: 'Travel Agent',
      floor: '5',
      specialRequests: 'Sea view required'
    },
  ];

  // Month Data for Overview
  const monthData = [
    { date: '2024-08-01', occupancy: 72, revenue: 2850, arrivals: 8, departures: 6 },
    { date: '2024-08-02', occupancy: 78, revenue: 3120, arrivals: 10, departures: 4 },
    { date: '2024-08-03', occupancy: 85, revenue: 3400, arrivals: 12, departures: 5 },
    { date: '2024-08-04', occupancy: 88, revenue: 3520, arrivals: 9, departures: 6 },
    { date: '2024-08-05', occupancy: 82, revenue: 3280, arrivals: 7, departures: 8 },
    { date: '2024-08-06', occupancy: 79, revenue: 3160, arrivals: 8, departures: 11 },
    { date: '2024-08-07', occupancy: 84, revenue: 3360, arrivals: 12, departures: 8 },
    { date: '2024-08-08', occupancy: 90, revenue: 3600, arrivals: 15, departures: 9 },
    { date: '2024-08-09', occupancy: 92, revenue: 3680, arrivals: 14, departures: 7 },
    { date: '2024-08-10', occupancy: 88, revenue: 3520, arrivals: 11, departures: 10 },
    { date: '2024-08-11', occupancy: 85, revenue: 3400, arrivals: 9, departures: 8 },
    { date: '2024-08-12', occupancy: 83, revenue: 3320, arrivals: 7, departures: 6 },
    { date: '2024-08-13', occupancy: 80, revenue: 3200, arrivals: 6, departures: 5 },
    { date: '2024-08-14', occupancy: 78, revenue: 3120, arrivals: 8, departures: 9 },
    { date: '2024-08-15', occupancy: 75, revenue: 3000, arrivals: 5, departures: 7 },
    { date: '2024-08-16', occupancy: 82, revenue: 3280, arrivals: 10, departures: 6 },
    { date: '2024-08-17', occupancy: 88, revenue: 3520, arrivals: 12, departures: 4 },
    { date: '2024-08-18', occupancy: 90, revenue: 3600, arrivals: 13, departures: 5 },
    { date: '2024-08-19', occupancy: 86, revenue: 3440, arrivals: 11, departures: 8 },
    { date: '2024-08-20', occupancy: 84, revenue: 3360, arrivals: 9, departures: 7 },
    { date: '2024-08-21', occupancy: 81, revenue: 3240, arrivals: 8, departures: 9 },
    { date: '2024-08-22', occupancy: 79, revenue: 3160, arrivals: 7, departures: 6 },
    { date: '2024-08-23', occupancy: 83, revenue: 3320, arrivals: 10, departures: 7 },
    { date: '2024-08-24', occupancy: 87, revenue: 3480, arrivals: 12, departures: 5 },
    { date: '2024-08-25', occupancy: 89, revenue: 3560, arrivals: 13, departures: 6 },
    { date: '2024-08-26', occupancy: 85, revenue: 3400, arrivals: 11, departures: 9 },
    { date: '2024-08-27', occupancy: 82, revenue: 3280, arrivals: 9, departures: 8 },
    { date: '2024-08-28', occupancy: 80, revenue: 3200, arrivals: 8, departures: 10 },
    { date: '2024-08-29', occupancy: 78, revenue: 3120, arrivals: 7, departures: 9 },
    { date: '2024-08-30', occupancy: 76, revenue: 3040, arrivals: 6, departures: 8 },
    { date: '2024-08-31', occupancy: 74, revenue: 2960, arrivals: 5, departures: 7 },
  ];

  // Helper Functions
  const getStatusColor = (status) => {
    switch (status.toLowerCase()) {
      case 'confirmed':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300 border border-blue-200 dark:border-blue-800';
      case 'checked in':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300 border border-green-200 dark:border-green-800';
      case 'tentative':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300 border border-yellow-200 dark:border-yellow-800';
      case 'no show':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300 border border-red-200 dark:border-red-800';
      case 'cancelled':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300 border border-gray-200 dark:border-gray-800';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300';
    }
  };

  const getPaymentStatusColor = (status) => {
    switch (status) {
      case 'Paid':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300';
      case 'Pending':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300';
      case 'Partially Paid':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300';
      case 'Refunded':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300';
    }
  };

  const getCalendarStatusColor = (status) => {
    switch (status) {
      case 'confirmed':
        return 'bg-blue-500/20 border-l-4 border-blue-500 hover:bg-blue-500/30';
      case 'checked-in':
        return 'bg-green-500/20 border-l-4 border-green-500 hover:bg-green-500/30';
      case 'tentative':
        return 'bg-yellow-500/20 border-l-4 border-yellow-500 hover:bg-yellow-500/30';
      case 'no-show':
        return 'bg-red-500/20 border-l-4 border-red-500 hover:bg-red-500/30';
      case 'cancelled':
        return 'bg-gray-500/20 border-l-4 border-gray-500 hover:bg-gray-500/30';
      default:
        return 'bg-gray-500/20 border-l-4 border-gray-500 hover:bg-gray-500/30';
    }
  };

  const getOccupancyColor = (percentage) => {
    if (percentage >= 90) return 'bg-gradient-to-br from-red-500 to-red-600';
    if (percentage >= 75) return 'bg-gradient-to-br from-orange-500 to-orange-600';
    if (percentage >= 50) return 'bg-gradient-to-br from-yellow-500 to-yellow-600';
    return 'bg-gradient-to-br from-green-500 to-green-600';
  };

  // Filter Functions
  const filterRooms = () => {
    return allRooms.filter(room => {
      if (appliedFilters.roomType.length > 0 && !appliedFilters.roomType.includes(room.type)) {
        return false;
      }
      if (appliedFilters.floor.length > 0 && !appliedFilters.floor.includes(room.floor)) {
        return false;
      }
      if (appliedFilters.amenities.length > 0) {
        const hasAllAmenities = appliedFilters.amenities.every(amenity => 
          room.amenities.includes(amenity)
        );
        if (!hasAllAmenities) return false;
      }
      return true;
    });
  };

  const filterBookings = () => {
    return calendarBookings.filter(booking => {
      const room = allRooms.find(r => r.id === booking.roomId);
      if (!room) return false;
      
      // Status Filter
      if (appliedFilters.status.length > 0 && !appliedFilters.status.includes(booking.status)) {
        return false;
      }
      
      // Source Filter
      if (appliedFilters.source.length > 0 && !appliedFilters.source.includes(booking.source)) {
        return false;
      }
      
      // Payment Status Filter
      if (appliedFilters.paymentStatus.length > 0 && !appliedFilters.paymentStatus.includes(booking.payment)) {
        return false;
      }
      
      // Room Type Filter
      if (appliedFilters.roomType.length > 0 && !appliedFilters.roomType.includes(room.type)) {
        return false;
      }
      
      // Floor Filter
      if (appliedFilters.floor.length > 0 && !appliedFilters.floor.includes(room.floor)) {
        return false;
      }
      
      // Amenities Filter
      if (appliedFilters.amenities.length > 0) {
        const hasAllAmenities = appliedFilters.amenities.every(amenity => 
          room.amenities.includes(amenity)
        );
        if (!hasAllAmenities) return false;
      }
      
      return true;
    });
  };

  const filterReservations = () => {
    let filtered = allReservations.filter(reservation => {
      // Status Filter (quick filter buttons)
      if (selectedFilter !== 'all') {
        const statusValue = reservation.status.toLowerCase().replace(' ', '-');
        if (statusValue !== selectedFilter) return false;
      }
      
      // Search Query
      if (searchQuery && !reservation.guest.toLowerCase().includes(searchQuery.toLowerCase()) &&
          !reservation.room.includes(searchQuery) &&
          !reservation.bookingId.toLowerCase().includes(searchQuery.toLowerCase())) {
        return false;
      }
      
      // Status Filter (from advanced filters)
      const reservationStatus = reservation.status.toLowerCase().replace(' ', '-');
      if (appliedFilters.status.length > 0 && !appliedFilters.status.includes(reservationStatus)) {
        return false;
      }
      
      // Source Filter
      if (appliedFilters.source.length > 0 && !appliedFilters.source.includes(reservation.source)) {
        return false;
      }
      
      // Payment Status Filter
      if (appliedFilters.paymentStatus.length > 0 && !appliedFilters.paymentStatus.includes(reservation.paymentStatus)) {
        return false;
      }
      
      // Room Type Filter
      if (appliedFilters.roomType.length > 0 && !appliedFilters.roomType.includes(reservation.type)) {
        return false;
      }
      
      // Floor Filter
      if (appliedFilters.floor.length > 0 && !appliedFilters.floor.includes(reservation.floor)) {
        return false;
      }
      
      return true;
    });
    
    // Apply Sorting
    filtered.sort((a, b) => {
      if (sortConfig.key === 'time') {
        const timeA = a.time.includes('PM') ? parseInt(a.time) + 12 : parseInt(a.time);
        const timeB = b.time.includes('PM') ? parseInt(b.time) + 12 : parseInt(b.time);
        return sortConfig.direction === 'asc' ? timeA - timeB : timeB - timeA;
      }
      
      if (sortConfig.key === 'amount') {
        const amountA = parseFloat(a.amount.replace('$', '').replace(',', ''));
        const amountB = parseFloat(b.amount.replace('$', '').replace(',', ''));
        return sortConfig.direction === 'asc' ? amountA - amountB : amountB - amountA;
      }
      
      if (sortConfig.key === 'nights') {
        return sortConfig.direction === 'asc' ? a.nights - b.nights : b.nights - a.nights;
      }
      
      if (sortConfig.key === 'guest') {
        return sortConfig.direction === 'asc' 
          ? a.guest.localeCompare(b.guest)
          : b.guest.localeCompare(a.guest);
      }
      
      if (sortConfig.key === 'room') {
        return sortConfig.direction === 'asc' 
          ? parseInt(a.room) - parseInt(b.room)
          : parseInt(b.room) - parseInt(a.room);
      }
      
      return 0;
    });
    
    return filtered;
  };

  const getBookingForRoomAndDate = (roomId, date) => {
    const dateStr = date.toISOString().split('T')[0];
    const filteredBookings = filterBookings();
    return filteredBookings.find(booking => 
      booking.roomId === roomId && 
      dateStr >= booking.checkIn && 
      dateStr < booking.checkOut
    );
  };

  // Sort function
  const handleSort = (key) => {
    setSortConfig(prev => ({
      key,
      direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc'
    }));
  };

  // Filter toggle functions
  const toggleFilter = (category, value) => {
    setAppliedFilters(prev => {
      const currentValues = prev[category] || [];
      if (currentValues.includes(value)) {
        return {
          ...prev,
          [category]: currentValues.filter(v => v !== value)
        };
      } else {
        return {
          ...prev,
          [category]: [...currentValues, value]
        };
      }
    });
  };

  const clearAllFilters = () => {
    setAppliedFilters({
      roomType: [],
      floor: [],
      status: [],
      amenities: [],
      source: [],
      paymentStatus: []
    });
    setSelectedFilter('all');
    setSearchQuery('');
  };

  // Get month data for current month
  const getCurrentMonthData = () => {
    const currentMonthStr = currentMonth.toISOString().slice(0, 7); // YYYY-MM
    return monthData.filter(day => day.date.startsWith(currentMonthStr));
  };

  // Calculate month statistics
  const getMonthStatistics = () => {
    const currentMonthData = getCurrentMonthData();
    const stats = {
      totalRevenue: currentMonthData.reduce((sum, day) => sum + day.revenue, 0),
      avgOccupancy: Math.round(currentMonthData.reduce((sum, day) => sum + day.occupancy, 0) / currentMonthData.length),
      peakOccupancy: Math.max(...currentMonthData.map(day => day.occupancy)),
      totalArrivals: currentMonthData.reduce((sum, day) => sum + day.arrivals, 0),
      totalDepartures: currentMonthData.reduce((sum, day) => sum + day.departures, 0),
    };
    return stats;
  };

  // Add keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (activeView !== 'calendar') return;
      
      switch (e.key) {
        case 'ArrowLeft':
          e.preventDefault();
          goToPreviousDay();
          break;
        case 'ArrowRight':
          e.preventDefault();
          goToNextDay();
          break;
        case 'ArrowUp':
          e.preventDefault();
          goToPreviousWeek();
          break;
        case 'ArrowDown':
          e.preventDefault();
          goToNextWeek();
          break;
        case 'Home':
          e.preventDefault();
          goToToday();
          break;
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [activeView, currentCenterDate]);

  // Filter Panel Component
  const FilterPanel = () => {
    const activeFilterCount = Object.values(appliedFilters).flat().length;
    
    return (
      <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-6 shadow-lg">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Filters</h3>
            {activeFilterCount > 0 && (
              <span className="bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300 text-xs px-2 py-1 rounded-full">
                {activeFilterCount} active
              </span>
            )}
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={clearAllFilters}
              className="text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
            >
              Clear all
            </button>
            <button
              onClick={() => setShowFilters(false)}
              className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        <div className="space-y-6 max-h-[60vh] overflow-y-auto pr-2">
          {/* Room Type Filter */}
          <div>
            <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Room Type</h4>
            <div className="space-y-2">
              {filterOptions.roomType.map(type => (
                <label key={type} className="flex items-center gap-3 cursor-pointer p-2 hover:bg-gray-50 dark:hover:bg-gray-750 rounded-lg">
                  <input 
                    type="checkbox" 
                    checked={appliedFilters.roomType.includes(type)}
                    onChange={() => toggleFilter('roomType', type)}
                    className="rounded border-gray-300 dark:border-gray-600 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="text-sm text-gray-700 dark:text-gray-300">{type}</span>
                  <span className="text-xs text-gray-500 dark:text-gray-500 ml-auto">
                    {allRooms.filter(r => r.type === type).length} rooms
                  </span>
                </label>
              ))}
            </div>
          </div>

          {/* Floor Filter */}
          <div>
            <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Floor</h4>
            <div className="flex flex-wrap gap-2">
              {filterOptions.floor.map(floor => (
                <button
                  key={floor}
                  onClick={() => toggleFilter('floor', floor)}
                  className={`px-3 py-2 text-sm rounded-lg transition-all ${
                    appliedFilters.floor.includes(floor)
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                  }`}
                >
                  Floor {floor}
                </button>
              ))}
            </div>
          </div>

          {/* Status Filter */}
          <div>
            <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Booking Status</h4>
            <div className="space-y-2">
              {bookingStatus.map(status => (
                <label key={status.value} className="flex items-center gap-3 cursor-pointer p-2 hover:bg-gray-50 dark:hover:bg-gray-750 rounded-lg">
                  <input 
                    type="checkbox" 
                    checked={appliedFilters.status.includes(status.value)}
                    onChange={() => toggleFilter('status', status.value)}
                    className="rounded border-gray-300 dark:border-gray-600 text-blue-600 focus:ring-blue-500"
                  />
                  <div className="flex items-center gap-2">
                    <div className={`w-2 h-2 rounded-full ${status.color}`}></div>
                    <span className="text-sm text-gray-700 dark:text-gray-300">{status.label}</span>
                  </div>
                  <span className="text-xs text-gray-500 dark:text-gray-500 ml-auto">
                    {status.count}
                  </span>
                </label>
              ))}
            </div>
          </div>

          {/* Booking Source Filter */}
          <div>
            <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Booking Source</h4>
            <div className="space-y-2">
              {filterOptions.source.map(source => (
                <label key={source} className="flex items-center gap-3 cursor-pointer p-2 hover:bg-gray-50 dark:hover:bg-gray-750 rounded-lg">
                  <input 
                    type="checkbox" 
                    checked={appliedFilters.source.includes(source)}
                    onChange={() => toggleFilter('source', source)}
                    className="rounded border-gray-300 dark:border-gray-600 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="text-sm text-gray-700 dark:text-gray-300">{source}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Payment Status Filter */}
          <div>
            <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Payment Status</h4>
            <div className="space-y-2">
              {filterOptions.paymentStatus.map(status => (
                <label key={status} className="flex items-center gap-3 cursor-pointer p-2 hover:bg-gray-50 dark:hover:bg-gray-750 rounded-lg">
                  <input 
                    type="checkbox" 
                    checked={appliedFilters.paymentStatus.includes(status)}
                    onChange={() => toggleFilter('paymentStatus', status)}
                    className="rounded border-gray-300 dark:border-gray-600 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="text-sm text-gray-700 dark:text-gray-300">{status}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Amenities Filter */}
          <div>
            <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Amenities</h4>
            <div className="space-y-2">
              {filterOptions.amenities.map(amenity => (
                <label key={amenity} className="flex items-center gap-3 cursor-pointer p-2 hover:bg-gray-50 dark:hover:bg-gray-750 rounded-lg">
                  <input 
                    type="checkbox" 
                    checked={appliedFilters.amenities.includes(amenity)}
                    onChange={() => toggleFilter('amenities', amenity)}
                    className="rounded border-gray-300 dark:border-gray-600 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="text-sm text-gray-700 dark:text-gray-300">{amenity}</span>
                </label>
              ))}
            </div>
          </div>
        </div>

        {/* Active Filters */}
        {activeFilterCount > 0 && (
          <div className="pt-6 mt-6 border-t border-gray-200 dark:border-gray-700">
            <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Active Filters</h4>
            <div className="flex flex-wrap gap-2">
              {appliedFilters.roomType.map(type => (
                <span key={`room-${type}`} className="inline-flex items-center gap-1 px-3 py-1.5 bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 text-xs rounded-full">
                  {type}
                  <button onClick={() => toggleFilter('roomType', type)} className="ml-1">
                    <X size={12} />
                  </button>
                </span>
              ))}
              {appliedFilters.floor.map(floor => (
                <span key={`floor-${floor}`} className="inline-flex items-center gap-1 px-3 py-1.5 bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 text-xs rounded-full">
                  Floor {floor}
                  <button onClick={() => toggleFilter('floor', floor)} className="ml-1">
                    <X size={12} />
                  </button>
                </span>
              ))}
              {appliedFilters.status.map(status => {
                const statusInfo = bookingStatus.find(s => s.value === status);
                return (
                  <span key={`status-${status}`} className="inline-flex items-center gap-1 px-3 py-1.5 bg-purple-100 dark:bg-purple-900/30 text-purple-800 dark:text-purple-300 text-xs rounded-full">
                    <div className={`w-2 h-2 rounded-full ${statusInfo?.color}`}></div>
                    {statusInfo?.label}
                    <button onClick={() => toggleFilter('status', status)} className="ml-1">
                      <X size={12} />
                    </button>
                  </span>
                );
              })}
              {appliedFilters.source.map(source => (
                <span key={`source-${source}`} className="inline-flex items-center gap-1 px-3 py-1.5 bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300 text-xs rounded-full">
                  {source}
                  <button onClick={() => toggleFilter('source', source)} className="ml-1">
                    <X size={12} />
                  </button>
                </span>
              ))}
              {appliedFilters.paymentStatus.map(status => (
                <span key={`payment-${status}`} className="inline-flex items-center gap-1 px-3 py-1.5 bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 text-xs rounded-full">
                  {status}
                  <button onClick={() => toggleFilter('paymentStatus', status)} className="ml-1">
                    <X size={12} />
                  </button>
                </span>
              ))}
              {appliedFilters.amenities.map(amenity => (
                <span key={`amenity-${amenity}`} className="inline-flex items-center gap-1 px-3 py-1.5 bg-gray-100 dark:bg-gray-900/30 text-gray-800 dark:text-gray-300 text-xs rounded-full">
                  {amenity}
                  <button onClick={() => toggleFilter('amenities', amenity)} className="ml-1">
                    <X size={12} />
                  </button>
                </span>
              ))}
            </div>
          </div>
        )}

        <div className="flex gap-3 pt-6 mt-6 border-t border-gray-200 dark:border-gray-700">
          <button
            onClick={clearAllFilters}
            className="flex-1 px-4 py-2.5 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
          >
            Clear All
          </button>
          <button
            onClick={() => setShowFilters(false)}
            className="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Apply Filters
          </button>
        </div>
      </div>
    );
  };

  // Calendar View Component
  const CalendarView = () => {
    const filteredRooms = filterRooms();
    const weekDates = getWeekDates(currentCenterDate);
    const today = new Date();
    const activeFilterCount = Object.values(appliedFilters).flat().length;
    
    // Format for date input
    const formatDateForInput = (date) => {
      return date.toISOString().split('T')[0];
    };

    return (
      <div className="space-y-4">
        {/* Calendar Controls */}
        <div className="flex flex-col md:flex-row gap-4 items-start md:items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              {/* Week Navigation */}
              <button 
                onClick={goToPreviousWeek}
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
                title="Previous Week"
              >
                <ChevronLeft size={20} />
              </button>
              
              {/* Day Navigation */}
              <button 
                onClick={goToPreviousDay}
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
                title="Previous Day"
              >
                <ChevronLeftIcon size={16} />
              </button>
              
              {/* Date Display and Selection */}
              <div className="flex flex-col items-center">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                  {weekRangeString}
                </h2>
                <div className="flex items-center gap-2 mt-1">
                  <span className="text-sm text-gray-600 dark:text-gray-400">Center Date:</span>
                  <input
                    type="date"
                    value={formatDateForInput(currentCenterDate)}
                    onChange={(e) => handleDateSelect(e.target.value)}
                    className="text-sm px-2 py-1 border border-gray-300 dark:border-gray-600 rounded bg-white dark:bg-gray-800"
                  />
                </div>
              </div>
              
              {/* Day Navigation */}
              <button 
                onClick={goToNextDay}
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
                title="Next Day"
              >
                <ChevronRightIcon size={16} />
              </button>
              
              {/* Week Navigation */}
              <button 
                onClick={goToNextWeek}
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
                title="Next Week"
              >
                <ChevronRight size={20} />
              </button>
            </div>
            
            <button 
              onClick={goToToday}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition-colors"
            >
              Today
            </button>
          </div>
          
          <div className="flex items-center gap-2">
            <div className="relative">
              <button 
                onClick={() => setShowFilters(!showFilters)}
                className="flex items-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-lg text-sm font-medium text-gray-700 dark:text-gray-300 transition-colors"
              >
                <Filter size={16} />
                Filters
                {activeFilterCount > 0 && (
                  <span className="bg-blue-600 text-white text-xs w-5 h-5 flex items-center justify-center rounded-full">
                    {activeFilterCount}
                  </span>
                )}
              </button>
              {showFilters && (
                <div className="absolute right-0 top-full mt-2 z-50 w-80">
                  <FilterPanel />
                </div>
              )}
            </div>
            <button className="flex items-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-lg text-sm font-medium text-gray-700 dark:text-gray-300 transition-colors">
              <Download size={16} />
              Export
            </button>
          </div>
        </div>

        {/* Calendar Grid */}
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
          <div className="overflow-x-auto">
            <div className="min-w-[800px]">
              {/* Header Row - Days */}
              <div className="grid grid-cols-8 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900/50">
                <div className="p-4 font-semibold text-gray-700 dark:text-gray-300">Rooms</div>
                {weekDates.map((day, index) => {
                  const isToday = day.toDateString() === today.toDateString();
                  const isCenterDate = day.toDateString() === currentCenterDate.toDateString();
                  
                  return (
                    <div 
                      key={index} 
                      className={`p-4 text-center border-l border-gray-200 dark:border-gray-700 ${
                        isCenterDate ? 'bg-blue-50 dark:bg-blue-900/20' : ''
                      } cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors`}
                      onClick={() => handleDateSelect(day.toISOString().split('T')[0])}
                    >
                      <div className="text-sm text-gray-500 dark:text-gray-400 font-medium">
                        {day.toLocaleDateString('en-US', { weekday: 'short' })}
                      </div>
                      <div className={`text-lg font-semibold mt-1 ${
                        isToday 
                          ? 'text-blue-600 dark:text-blue-400 font-bold'
                          : isCenterDate
                          ? 'bg-blue-600 text-white w-8 h-8 flex items-center justify-center rounded-full mx-auto'
                          : 'text-gray-900 dark:text-white'
                      }`}>
                        {day.getDate()}
                      </div>
                      <div className="text-xs mt-1">
                        {isToday && <span className="text-blue-600 dark:text-blue-400">Today</span>}
                        {isCenterDate && !isToday && <span className="text-blue-600 dark:text-blue-400">Selected</span>}
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Room Rows */}
              {filteredRooms.map(room => (
                <div key={room.id} className="grid grid-cols-8 border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-750/50 transition-colors">
                  <div className="p-4">
                    <div className="font-semibold text-gray-900 dark:text-white">Room {room.id}</div>
                    <div className="text-sm text-gray-600 dark:text-gray-400 flex items-center gap-1 mt-1">
                      <span>{room.type}</span>
                      <span className="text-xs px-1.5 py-0.5 bg-gray-100 dark:bg-gray-700 rounded">Floor {room.floor}</span>
                    </div>
                    <div className="text-xs text-gray-500 dark:text-gray-500 mt-1">
                      ${room.price}/night
                    </div>
                  </div>
                  
                  {weekDates.map((day, dayIndex) => {
                    const booking = getBookingForRoomAndDate(room.id, day);
                    const isToday = day.toDateString() === today.toDateString();
                    const isCenterDate = day.toDateString() === currentCenterDate.toDateString();
                    
                    return (
                      <div 
                        key={dayIndex} 
                        className={`p-2 border-l border-gray-200 dark:border-gray-700 min-h-[100px] ${
                          isToday ? 'bg-blue-50/30 dark:bg-blue-900/10' : 
                          isCenterDate ? 'bg-blue-50/20 dark:bg-blue-900/5' : ''
                        }`}
                      >
                        {booking ? (
                          <div 
                            className={`${getCalendarStatusColor(booking.status)} rounded-lg p-3 h-full cursor-pointer hover:shadow-sm transition-all group`}
                            onClick={() => navigate(`/guest-detail/${booking.guestId}`)}
                          >
                            <div className="font-medium text-sm truncate text-gray-900 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400">
                              {booking.guest}
                            </div>
                            <div className="text-xs text-gray-600 dark:text-gray-400 mt-1 capitalize">
                              {booking.status.replace('-', ' ')}
                            </div>
                            <div className="text-xs text-gray-500 dark:text-gray-500 mt-1">
                              {booking.adults} Adult{booking.adults !== 1 ? 's' : ''}
                              {booking.children > 0 && `, ${booking.children} Child${booking.children !== 1 ? 'ren' : ''}`}
                            </div>
                            <div className="text-xs text-blue-600 dark:text-blue-400 mt-1 opacity-0 group-hover:opacity-100 transition-opacity">
                              Click to view guest details →
                            </div>
                          </div>
                        ) : (
                          <button 
                            className="w-full h-full flex items-center justify-center text-gray-400 hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/10 rounded-lg transition-colors"
                            onClick={() => navigate('/booking-form', { 
                              state: { 
                                roomId: room.id,
                                roomType: room.type,
                                date: day.toISOString().split('T')[0],
                                price: room.price
                              } 
                            })}
                          >
                            <Plus size={16} />
                          </button>
                        )}
                      </div>
                    );
                  })}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Navigation Help */}
        <div className="bg-gray-50 dark:bg-gray-800/50 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div>
              <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300">Navigation Help</h3>
              <div className="flex flex-wrap gap-4 mt-2 text-sm text-gray-600 dark:text-gray-400">
                <div className="flex items-center gap-2">
                  <div className="px-2 py-1 bg-gray-200 dark:bg-gray-700 rounded text-xs">← →</div>
                  <span>Navigate by day</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="px-2 py-1 bg-gray-200 dark:bg-gray-700 rounded text-xs">↑ ↓</div>
                  <span>Navigate by week</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="px-2 py-1 bg-gray-200 dark:bg-gray-700 rounded text-xs">Home</div>
                  <span>Jump to today</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="px-2 py-1 bg-gray-200 dark:bg-gray-700 rounded text-xs">Click Date</div>
                  <span>Select center date</span>
                </div>
              </div>
            </div>
            <div className="text-sm text-gray-600 dark:text-gray-400">
              Showing: 3 days before and after {currentCenterDate.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}
            </div>
          </div>
        </div>
      </div>
    );
  };

  // List View Component
  const ListView = () => {
    const filteredReservations = filterReservations();
    const activeFilterCount = Object.values(appliedFilters).flat().length;
    
    return (
      <div className="space-y-4">
        {/* List View Header */}
        <div className="flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white">All Reservations</h2>
            <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
              Showing {filteredReservations.length} of {allReservations.length} reservations
            </p>
          </div>
          <div className="flex items-center gap-2">
            <div className="relative">
              <button 
                onClick={() => setShowFilters(!showFilters)}
                className="flex items-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-lg text-sm font-medium text-gray-700 dark:text-gray-300 transition-colors"
              >
                <Filter size={16} />
                Filters
                {activeFilterCount > 0 && (
                  <span className="bg-blue-600 text-white text-xs w-5 h-5 flex items-center justify-center rounded-full">
                    {activeFilterCount}
                  </span>
                )}
              </button>
              {showFilters && (
                <div className="absolute right-0 top-full mt-2 z-50 w-80">
                  <FilterPanel />
                </div>
              )}
            </div>
            <button className="flex items-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-lg text-sm font-medium text-gray-700 dark:text-gray-300 transition-colors">
              <Download size={16} />
              Export CSV
            </button>
          </div>
        </div>

        {/* Search Bar */}
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-4">
          <div className="flex flex-col md:flex-row gap-4 items-center justify-between">
            <div className="flex-1 relative max-w-md">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
              <input
                type="text"
                placeholder="Search by guest name, room number, or booking ID..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="input-field pl-10 w-full"
              />
            </div>
            <div className="flex items-center gap-2">
              <select className="input-field text-sm">
                <option>All Sources</option>
                {filterOptions.source.map(source => (
                  <option key={source}>{source}</option>
                ))}
              </select>
              <select className="input-field text-sm">
                <option>All Payment Status</option>
                {filterOptions.paymentStatus.map(status => (
                  <option key={status}>{status}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Reservations Table */}
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 dark:bg-gray-900/50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">
                    <input type="checkbox" className="rounded border-gray-300 dark:border-gray-600" />
                  </th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer"
                    onClick={() => handleSort('time')}
                  >
                    <div className="flex items-center gap-1">
                      Time
                      {sortConfig.key === 'time' && (
                        sortConfig.direction === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />
                      )}
                    </div>
                  </th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer"
                    onClick={() => handleSort('guest')}
                  >
                    <div className="flex items-center gap-1">
                      Guest
                      {sortConfig.key === 'guest' && (
                        sortConfig.direction === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />
                      )}
                    </div>
                  </th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer"
                    onClick={() => handleSort('room')}
                  >
                    <div className="flex items-center gap-1">
                      Room
                      {sortConfig.key === 'room' && (
                        sortConfig.direction === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />
                      )}
                    </div>
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">
                    Type
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">
                    Stay
                  </th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer"
                    onClick={() => handleSort('nights')}
                  >
                    <div className="flex items-center gap-1">
                      Nights
                      {sortConfig.key === 'nights' && (
                        sortConfig.direction === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />
                      )}
                    </div>
                  </th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer"
                    onClick={() => handleSort('amount')}
                  >
                    <div className="flex items-center gap-1">
                      Amount
                      {sortConfig.key === 'amount' && (
                        sortConfig.direction === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />
                      )}
                    </div>
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                {filteredReservations.map((reservation) => (
                  <tr 
                    key={reservation.id} 
                    className="hover:bg-gray-50 dark:hover:bg-gray-750/50 transition-colors group"
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <input type="checkbox" className="rounded border-gray-300 dark:border-gray-600" />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-mono text-gray-900 dark:text-white">{reservation.time}</div>
                      <div className="text-xs text-gray-500 dark:text-gray-500">{reservation.bookingId}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div 
                        className="font-medium text-gray-900 dark:text-white hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer"
                        onClick={() => navigate(`/guest-detail/${reservation.guestId}`)}
                      >
                        {reservation.guest}
                      </div>
                      <div className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        Source: {reservation.source}
                      </div>
                      {reservation.specialRequests && (
                        <div className="text-xs text-gray-500 dark:text-gray-500 mt-1 truncate max-w-xs">
                          <span className="italic">"{reservation.specialRequests}"</span>
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="font-medium text-gray-900 dark:text-white">Room {reservation.room}</div>
                      <div className="text-sm text-gray-600 dark:text-gray-400">Floor {reservation.floor}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900 dark:text-white">{reservation.type}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm text-gray-900 dark:text-white">
                        {reservation.checkIn} → {reservation.checkOut}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900 dark:text-white">{reservation.nights}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="font-medium text-gray-900 dark:text-white">{reservation.amount}</div>
                      <span className={`text-xs px-2 py-1 rounded-full ${getPaymentStatusColor(reservation.paymentStatus)}`}>
                        {reservation.paymentStatus}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-3 py-1.5 rounded-full text-xs font-medium ${getStatusColor(reservation.status)}`}>
                        {reservation.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex gap-2">
                        <button 
                          className="p-2 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-lg text-blue-600 dark:text-blue-400"
                          onClick={() => navigate(`/guest-detail/${reservation.guestId}`)}
                        >
                          <Eye size={16} />
                        </button>
                        <button 
                          className="p-2 hover:bg-green-50 dark:hover:bg-green-900/20 rounded-lg text-green-600 dark:text-green-400"
                          onClick={() => navigate('/booking-form', { 
                            state: { 
                              bookingId: reservation.id,
                              guestId: reservation.guestId
                            } 
                          })}
                        >
                          <Edit size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {filteredReservations.length === 0 && (
            <div className="text-center py-12">
              <Calendar className="mx-auto text-gray-400 mb-4" size={48} />
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">No reservations found</h3>
              <p className="text-gray-600 dark:text-gray-400 mt-1">
                Try adjusting your search or filters
              </p>
              <button 
                onClick={() => navigate('/booking-form')}
                className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
              >
                Create New Booking
              </button>
            </div>
          )}
        </div>

        {/* Pagination */}
        <div className="flex items-center justify-between">
          <div className="text-sm text-gray-600 dark:text-gray-400">
            Showing 1 to {filteredReservations.length} of {allReservations.length} entries
          </div>
          <div className="flex items-center gap-2">
            <button className="px-3 py-1.5 border border-gray-300 dark:border-gray-600 rounded text-sm hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
              Previous
            </button>
            <button className="px-3 py-1.5 bg-blue-600 text-white rounded text-sm">1</button>
            <button className="px-3 py-1.5 border border-gray-300 dark:border-gray-600 rounded text-sm hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
              2
            </button>
            <button className="px-3 py-1.5 border border-gray-300 dark:border-gray-600 rounded text-sm hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
              Next
            </button>
          </div>
        </div>
      </div>
    );
  };

  // Month View Component
  const MonthView = () => {
    const currentMonthData = getCurrentMonthData();
    const monthStatistics = getMonthStatistics();
    const activeFilterCount = Object.values(appliedFilters).flat().length;
    
    // Get days in month
    const getDaysInMonth = () => {
      const year = currentMonth.getFullYear();
      const month = currentMonth.getMonth();
      const date = new Date(year, month, 1);
      const days = [];
      
      while (date.getMonth() === month) {
        days.push(new Date(date));
        date.setDate(date.getDate() + 1);
      }
      
      return days;
    };
    
    const daysInMonth = getDaysInMonth();
    const monthName = currentMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    
    // Get occupancy for a specific date
    const getOccupancyForDate = (date) => {
      const dateStr = date.toISOString().split('T')[0];
      const dayData = currentMonthData.find(d => d.date === dateStr);
      return dayData ? dayData.occupancy : 0;
    };
    
    // Get first day of month (0 = Sunday, 1 = Monday, etc.)
    const firstDayOfMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), 1).getDay();
    
    // Create empty cells for days before first day of month
    const emptyCells = [];
    for (let i = 0; i < firstDayOfMonth; i++) {
      emptyCells.push(i);
    }

    return (
      <div className="space-y-4">
        {/* Month View Header */}
        <div className="flex flex-col md:flex-row gap-4 items-start md:items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <button 
                onClick={goToPreviousMonth}
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
                title="Previous Month"
              >
                <ChevronLeft size={20} />
              </button>
              
              <div className="flex flex-col items-center">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                  {monthName}
                </h2>
                <div className="flex items-center gap-2 mt-1">
                  <span className="text-sm text-gray-600 dark:text-gray-400">Selected Month:</span>
                  <input
                    type="month"
                    value={currentMonth.toISOString().slice(0, 7)}
                    onChange={(e) => setCurrentMonth(new Date(e.target.value))}
                    className="text-sm px-2 py-1 border border-gray-300 dark:border-gray-600 rounded bg-white dark:bg-gray-800"
                  />
                </div>
              </div>
              
              <button 
                onClick={goToNextMonth}
                className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
                title="Next Month"
              >
                <ChevronRight size={20} />
              </button>
            </div>
            
            <button 
              onClick={goToToday}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition-colors"
            >
              Current Month
            </button>
          </div>
          
          <div className="flex items-center gap-2">
            <div className="relative">
              <button 
                onClick={() => setShowFilters(!showFilters)}
                className="flex items-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-lg text-sm font-medium text-gray-700 dark:text-gray-300 transition-colors"
              >
                <Filter size={16} />
                Filters
                {activeFilterCount > 0 && (
                  <span className="bg-blue-600 text-white text-xs w-5 h-5 flex items-center justify-center rounded-full">
                    {activeFilterCount}
                  </span>
                )}
              </button>
              {showFilters && (
                <div className="absolute right-0 top-full mt-2 z-50 w-80">
                  <FilterPanel />
                </div>
              )}
            </div>
            <button className="flex items-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-lg text-sm font-medium text-gray-700 dark:text-gray-300 transition-colors">
              <Download size={16} />
              Export
            </button>
          </div>
        </div>

        {/* Month Statistics */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Average Occupancy</p>
                <p className="text-2xl font-bold mt-2 text-gray-900 dark:text-white">{monthStatistics.avgOccupancy}%</p>
              </div>
              <div className="p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                <TrendingUp size={24} className="text-blue-500" />
              </div>
            </div>
            <div className="mt-4 text-sm text-green-500">
              +5% from last month
            </div>
          </div>
          
          <div className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Total Revenue</p>
                <p className="text-2xl font-bold mt-2 text-gray-900 dark:text-white">${monthStatistics.totalRevenue.toLocaleString()}</p>
              </div>
              <div className="p-3 bg-green-50 dark:bg-green-900/20 rounded-lg">
                <TrendingUp size={24} className="text-green-500" />
              </div>
            </div>
            <div className="mt-4 text-sm text-green-500">
              +18% from last month
            </div>
          </div>
          
          <div className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Peak Occupancy</p>
                <p className="text-2xl font-bold mt-2 text-gray-900 dark:text-white">{monthStatistics.peakOccupancy}%</p>
              </div>
              <div className="p-3 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
                <TrendingUp size={24} className="text-purple-500" />
              </div>
            </div>
            <div className="mt-4 text-sm text-gray-600 dark:text-gray-400">
              Highest occupancy day
            </div>
          </div>
          
          <div className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Total Arrivals</p>
                <p className="text-2xl font-bold mt-2 text-gray-900 dark:text-white">{monthStatistics.totalArrivals}</p>
              </div>
              <div className="p-3 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
                <Users size={24} className="text-yellow-500" />
              </div>
            </div>
            <div className="mt-4 text-sm text-gray-600 dark:text-gray-400">
              {monthStatistics.totalDepartures} departures
            </div>
          </div>
        </div>

        {/* Month Calendar Heatmap */}
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-6">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Monthly Occupancy Heatmap</h3>
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 bg-gradient-to-br from-green-500 to-green-600 rounded"></div>
                <span className="text-sm text-gray-600 dark:text-gray-400">Low (&lt;50%)</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 bg-gradient-to-br from-red-500 to-red-600 rounded"></div>
                <span className="text-sm text-gray-600 dark:text-gray-400">High (&gt;90%)</span>
              </div>
            </div>
          </div>
          
          <div className="grid grid-cols-7 gap-2">
            {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
              <div key={day} className="text-center text-sm font-medium text-gray-600 dark:text-gray-400 py-2">
                {day}
              </div>
            ))}
            
            {/* Empty cells for days before first day of month */}
            {emptyCells.map((_, index) => (
              <div key={`empty-${index}`} className="h-24 bg-gray-50 dark:bg-gray-900/30 rounded-lg"></div>
            ))}
            
            {/* Days of the month */}
            {daysInMonth.map((day, index) => {
              const occupancy = getOccupancyForDate(day);
              const isToday = day.toDateString() === new Date().toDateString();
              
              return (
                <div 
                  key={index} 
                  className={`${getOccupancyColor(occupancy)} rounded-lg p-3 text-center cursor-pointer hover:opacity-90 transition-opacity relative group`}
                  title={`${day.getDate()} ${day.toLocaleDateString('en-US', { month: 'short' })}: ${occupancy}% occupancy`}
                >
                  <div className={`font-semibold ${isToday ? 'text-white bg-blue-600/30 rounded-full w-6 h-6 flex items-center justify-center mx-auto' : 'text-white'}`}>
                    {day.getDate()}
                  </div>
                  <div className="text-xs text-white/90 mt-1">{occupancy}%</div>
                  
                  {/* Tooltip on hover */}
                  <div className="absolute bottom-full mb-2 left-1/2 transform -translate-x-1/2 opacity-0 group-hover:opacity-100 transition-opacity bg-gray-900 text-white text-xs px-2 py-1 rounded whitespace-nowrap z-10">
                    <div className="font-semibold">{day.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}</div>
                    <div>Occupancy: {occupancy}%</div>
                    <div className="text-blue-400">Click to view details</div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Revenue Trend Chart */}
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-6">Daily Revenue Trend</h3>
          <div className="h-64 flex items-end gap-1 overflow-x-auto">
            {currentMonthData.map((day, index) => {
              const maxRevenue = Math.max(...currentMonthData.map(d => d.revenue));
              const height = (day.revenue / maxRevenue) * 100;
              const date = new Date(day.date);
              
              return (
                <div key={index} className="flex-1 min-w-[30px] flex flex-col items-center group">
                  <div 
                    className="w-full bg-gradient-to-t from-blue-500 to-blue-300 rounded-t-lg transition-all hover:opacity-90 min-h-[2px]"
                    style={{ height: `${height}%` }}
                  ></div>
                  <div className="mt-2 text-xs text-gray-600 dark:text-gray-400">
                    {date.getDate()}
                  </div>
                  <div className="text-xs font-medium mt-1">
                    ${day.revenue.toLocaleString()}
                  </div>
                  <div className="absolute bottom-full mb-2 opacity-0 group-hover:opacity-100 transition-opacity bg-gray-900 text-white text-xs px-2 py-1 rounded whitespace-nowrap">
                    <div>{date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}</div>
                    <div>Revenue: ${day.revenue.toLocaleString()}</div>
                    <div>Occupancy: {day.occupancy}%</div>
                    <div>Arrivals: {day.arrivals}</div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Daily Averages */}
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-6">Daily Averages</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-gray-50 dark:bg-gray-900/30 rounded-lg p-4">
              <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Weekday vs Weekend</h4>
              <div className="space-y-3">
                <div>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-gray-600 dark:text-gray-400">Weekdays</span>
                    <span className="font-medium">82%</span>
                  </div>
                  <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                    <div className="bg-blue-500 h-2 rounded-full" style={{ width: '82%' }}></div>
                  </div>
                </div>
                <div>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-gray-600 dark:text-gray-400">Weekends</span>
                    <span className="font-medium">91%</span>
                  </div>
                  <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                    <div className="bg-green-500 h-2 rounded-full" style={{ width: '91%' }}></div>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="bg-gray-50 dark:bg-gray-900/30 rounded-lg p-4">
              <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Room Type Performance</h4>
              <div className="space-y-3">
                {['Standard', 'Deluxe', 'Suite', 'Presidential'].map(type => {
                  const occupancy = Math.floor(Math.random() * 30) + 70;
                  return (
                    <div key={type} className="flex items-center justify-between">
                      <span className="text-sm text-gray-600 dark:text-gray-400">{type}</span>
                      <span className="text-sm font-medium">{occupancy}%</span>
                    </div>
                  );
                })}
              </div>
            </div>
            
            <div className="bg-gray-50 dark:bg-gray-900/30 rounded-lg p-4">
              <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Revenue Distribution</h4>
              <div className="space-y-3">
                {['Room Revenue', 'F&B', 'Services', 'Other'].map((category, index) => {
                  const percentages = [65, 20, 10, 5];
                  const colors = ['bg-blue-500', 'bg-green-500', 'bg-yellow-500', 'bg-purple-500'];
                  return (
                    <div key={category}>
                      <div className="flex justify-between text-sm mb-1">
                        <span className="text-gray-600 dark:text-gray-400">{category}</span>
                        <span className="font-medium">{percentages[index]}%</span>
                      </div>
                      <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                        <div className={`${colors[index]} h-2 rounded-full`} style={{ width: `${percentages[index]}%` }}></div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Dashboard Header */}
      <div className="mb-6">
        <div className="flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Reservation Dashboard</h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              Manage bookings, check-ins, and room assignments
            </p>
          </div>
          <div className="flex items-center gap-3">
            <button className="btn-secondary flex items-center gap-2">
              <Download size={20} />
              Export Today
            </button>
            <button 
              onClick={() => navigate('/booking-form')}
              className="btn-primary flex items-center gap-2"
            >
              <Plus size={20} />
              New Booking
            </button>
          </div>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
        {stats.map((stat, index) => {
          const Icon = stat.icon;
          return (
            <div key={index} className="group">
              <div className={`card hover:shadow-lg transition-all duration-300 ${stat.bgColor} hover:scale-[1.02]`}>
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-sm text-gray-600 dark:text-gray-400">{stat.label}</p>
                    <p className="text-3xl font-bold mt-2 text-gray-900 dark:text-white">{stat.value}</p>
                    {stat.change && (
                      <div className="flex items-center gap-1 mt-2">
                        {stat.trending === 'up' ? (
                          <TrendingUp size={16} className="text-green-500" />
                        ) : (
                          <TrendingDown size={16} className="text-red-500" />
                        )}
                        <span className={`text-sm ${stat.trending === 'up' ? 'text-green-500' : 'text-red-500'}`}>
                          {stat.change}
                        </span>
                      </div>
                    )}
                  </div>
                  <div className={`p-3 rounded-xl ${stat.bgColor} group-hover:scale-110 transition-transform`}>
                    <Icon size={24} className={stat.color} />
                  </div>
                </div>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-3">{stat.description}</p>
              </div>
            </div>
          );
        })}
      </div>

      {/* View Switcher and Quick Filters */}
      <div className="mb-6">
        <div className="flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setActiveView('calendar')}
              className={`px-4 py-2.5 rounded-lg flex items-center gap-2 transition-all ${
                activeView === 'calendar' 
                  ? 'bg-blue-600 text-white shadow-lg' 
                  : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
              }`}
            >
              <Calendar size={18} />
              Calendar View
            </button>
            <button
              onClick={() => setActiveView('list')}
              className={`px-4 py-2.5 rounded-lg flex items-center gap-2 transition-all ${
                activeView === 'list' 
                  ? 'bg-blue-600 text-white shadow-lg' 
                  : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
              }`}
            >
              <List size={18} />
              List View
            </button>
            <button
              onClick={() => setActiveView('month')}
              className={`px-4 py-2.5 rounded-lg flex items-center gap-2 transition-all ${
                activeView === 'month' 
                  ? 'bg-blue-600 text-white shadow-lg' 
                  : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
              }`}
            >
              <BarChart3 size={18} />
              Month Overview
            </button>
          </div>
          
          {/* Status Quick Filters */}
          <div className="flex items-center gap-2 flex-wrap">
            <button
              onClick={() => setSelectedFilter('all')}
              className={`px-3 py-1.5 rounded-full text-sm font-medium transition-all ${
                selectedFilter === 'all'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
              }`}
            >
              All Reservations
            </button>
            {bookingStatus.map((status, index) => (
              <button
                key={index}
                onClick={() => setSelectedFilter(status.value)}
                className={`px-3 py-1.5 rounded-full text-sm font-medium flex items-center gap-2 transition-all ${
                  selectedFilter === status.value
                    ? 'bg-primary-100 text-primary-800 dark:bg-primary-900/30 dark:text-primary-400 shadow-sm'
                    : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
                }`}
              >
                <div className={`w-2 h-2 rounded-full ${status.color}`}></div>
                {status.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Active View Content */}
      <div className="mb-6">
        {activeView === 'calendar' && <CalendarView />}
        {activeView === 'list' && <ListView />}
        {activeView === 'month' && <MonthView />}
      </div>

      {/* Today's Arrivals & Departures (Only in List View) */}
      {activeView === 'list' && (
        <div className="card">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Today's Operations</h2>
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-600 dark:text-gray-400">
                August 7, 2024
              </span>
              <button 
                onClick={() => navigate('/booking-form')}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition-colors"
              >
                Quick Check-in
              </button>
            </div>
          </div>
          
          {/* Quick Actions */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-blue-50 dark:bg-blue-900/20 rounded-xl p-4 border border-blue-200 dark:border-blue-800">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-semibold text-blue-800 dark:text-blue-300">Check-in Ready</h3>
                  <p className="text-3xl font-bold text-blue-600 dark:text-blue-400 mt-2">12</p>
                  <p className="text-sm text-blue-600/80 dark:text-blue-400/80 mt-1">Expected arrivals</p>
                </div>
                <div className="p-3 bg-blue-100 dark:bg-blue-800/30 rounded-lg">
                  <Check size={24} className="text-blue-600 dark:text-blue-400" />
                </div>
              </div>
              <button 
                onClick={() => navigate('/checkin')}
                className="w-full mt-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition-colors"
              >
                Process Check-ins
              </button>
            </div>
            
            <div className="bg-green-50 dark:bg-green-900/20 rounded-xl p-4 border border-green-200 dark:border-green-800">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-semibold text-green-800 dark:text-green-300">Check-out Today</h3>
                  <p className="text-3xl font-bold text-green-600 dark:text-green-400 mt-2">8</p>
                  <p className="text-sm text-green-600/80 dark:text-green-400/80 mt-1">Expected departures</p>
                </div>
                <div className="p-3 bg-green-100 dark:bg-green-800/30 rounded-lg">
                  <Clock size={24} className="text-green-600 dark:text-green-400" />
                </div>
              </div>
              <button 
                onClick={() => navigate('/checkout')}
                className="w-full mt-4 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm font-medium transition-colors"
              >
                Process Check-outs
              </button>
            </div>
            
            <div className="bg-red-50 dark:bg-red-900/20 rounded-xl p-4 border border-red-200 dark:border-red-800">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-semibold text-red-800 dark:text-red-300">Requires Attention</h3>
                  <p className="text-3xl font-bold text-red-600 dark:text-red-400 mt-2">3</p>
                  <p className="text-sm text-red-600/80 dark:text-red-400/80 mt-1">Late check-outs & issues</p>
                </div>
                <div className="p-3 bg-red-100 dark:bg-red-800/30 rounded-lg">
                  <AlertCircle size={24} className="text-red-600 dark:text-red-400" />
                </div>
              </div>
              <button 
                onClick={() => navigate('/issues')}
                className="w-full mt-4 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium transition-colors"
              >
                View Issues
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Global Filter Panel Modal */}
      {showFilters && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="w-full max-w-4xl max-h-[90vh] overflow-auto">
            <FilterPanel />
          </div>
        </div>
      )}
    </div>
  );
};

export default ReservationBooking;