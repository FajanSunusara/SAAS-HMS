import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Calendar, User, Mail, Phone, MapPin, CreditCard, Search, 
  Save, Eye, RefreshCw, ChevronRight, Check,  Bed,
  DollarSign,  Printer, Send, 
  Users, X, AlertCircle, CheckCircle, 
  ChevronLeft, Download, HelpCircle, 
  Info,  Wifi as WifiIcon,
  Coffee as CoffeeIcon, Car as CarIcon, Sun as SunIcon,
  Wind as WindIcon,
  Tv as TvIcon, 
  Smartphone as Mobile,
  HardDrive as Disk,
  Download as DownloadIcon,
  Eye as EyeIcon,
  Printer as PrinterIcon,
  Battery as BatteryIcon
} from 'lucide-react';
import axios from 'axios';

const BookingForm = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [formData, setFormData] = useState({
    // Guest Information
    guestSearch: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    nationality: '',
    address: '',
    idType: '',
    idNumber: '',
    contactType: 'same',
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    dateOfBirth: '',
    gender: '',
    passportNumber: '',
    passportExpiry: '',
    country: '',
    state: '',
    city: '',
    zipCode: '',
    emergencyContact: '',
    company: '',
    businessEmail: '',
    loyaltyNumber: '',
    
    // Stay Details
    checkInDate: '',
    checkOutDate: '',
    adults: 1,
    children: 0,
    infants: 0,
    arrivalTime: '14:00',
    departureTime: '12:00',
    purposeOfVisit: '',
    specialInstructions: '',
    
    // Room Selection
    roomSelectionMode: 'type',
    selectedRoomTypes: [],
    selectedRooms: [],
    totalRooms: 0,
    
    // Pricing & Offers
    ratePlan: '',
    basePrice: 0,
    discountType: 'percentage',
    discountValue: 0,
    promoCode: '',
    taxPercentage: 10,
    extraServices: [],
    
    // Payment
    paymentType: 'partial',
    paymentMethod: '',
    advancePayment: 0,
    transactionId: '',
    paymentDate: new Date().toISOString().split('T')[0],
    paymentRemarks: '',
    
    // Additional Information
    specialRequests: '',
    bookingSource: 'walk-in',
    remarks: '',
    marketingOptIn: false,
    termsAccepted: false,
    
    // Room Preferences
    bedPreference: '',
    smokingPreference: 'non-smoking',
    floorPreference: '',
    viewPreference: '',
    
    // Special Services
    extraBed: false,
    crib: false,
    wheelchairAccess: false,
    earlyCheckIn: false,
    lateCheckOut: false,
  });

  const [searchResults, setSearchResults] = useState([]);
  const [draftSaved, setDraftSaved] = useState(false);
  const [showAvailability, setShowAvailability] = useState(false);
  const [availableRooms, setAvailableRooms] = useState([]);
  const [bookingId, setBookingId] = useState('');
  const [validationErrors, setValidationErrors] = useState({});
  const [showRoomGrid, setShowRoomGrid] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [toasts, setToasts] = useState([]);
  const [formDataArray, setFormDataArray] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [guestSearchLoading, setGuestSearchLoading] = useState(false);
  const [rooms, setRooms] = useState([]);

  // API base URL
  const API_BASE_URL = 'http://localhost:8080/api';

  // Custom toast notification system
  const showToast = (message, type = 'success') => {
    const id = Date.now();
    const newToast = { id, message, type };
    setToasts(prev => [...prev, newToast]);
    
    setTimeout(() => {
      setToasts(prev => prev.filter(toast => toast.id !== id));
    }, 3000);
  };

  // Fetch rooms on component mount
  useEffect(() => {
    fetchRooms();
  }, []);

  const fetchRooms = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/rooms`);
      console.log('Rooms API Response:', response.data);
      
      if (response.data && response.data.success) {
        setRooms(response.data.data || []);
        showToast('Rooms loaded successfully', 'success');
      }
    } catch (error) {
      console.error('Error fetching rooms:', error);
      showToast('Error loading rooms. Using mock data.', 'error');
      // Use mock rooms as fallback
      setRooms(createMockRooms());
    }
  };

  // Create mock rooms for fallback
  const createMockRooms = () => {
    return [
      { id: 1, roomNumber: '101', roomType: 'Standard Room', baseRate: 120, capacity: 2, floorNumber: 1, status: 'AVAILABLE', features: ['WiFi', 'TV', 'AC'], description: 'Comfortable room with basic amenities' },
      { id: 2, roomNumber: '102', roomType: 'Standard Room', baseRate: 120, capacity: 2, floorNumber: 1, status: 'AVAILABLE', features: ['WiFi', 'TV', 'AC'], description: 'Comfortable room with basic amenities' },
      { id: 3, roomNumber: '201', roomType: 'Deluxe Room', baseRate: 180, capacity: 2, floorNumber: 2, status: 'AVAILABLE', features: ['WiFi', 'TV', 'AC', 'Mini-bar', 'Coffee maker'], description: 'Spacious room with premium amenities' },
      { id: 4, roomNumber: '202', roomType: 'Deluxe Room', baseRate: 180, capacity: 2, floorNumber: 2, status: 'AVAILABLE', features: ['WiFi', 'TV', 'AC', 'Mini-bar', 'Coffee maker'], description: 'Spacious room with premium amenities' },
      { id: 5, roomNumber: '301', roomType: 'Executive Suite', baseRate: 280, capacity: 4, floorNumber: 3, status: 'AVAILABLE', features: ['WiFi', 'TV', 'AC', 'Mini-bar', 'Coffee maker', 'Work desk'], description: 'Luxury suite with separate living area' },
      { id: 6, roomNumber: '401', roomType: 'Presidential Suite', baseRate: 450, capacity: 4, floorNumber: 4, status: 'AVAILABLE', features: ['WiFi', 'TV', 'AC', 'Mini-bar', 'Coffee maker', 'Jacuzzi', 'Butler service'], description: 'Ultimate luxury with panoramic views' },
    ];
  };

  const roomTypes = [
    { id: 1, name: 'Standard Room', price: 120, capacity: 2, available: 5, floor: '1st', description: 'Comfortable room with basic amenities', features: ['WiFi', 'TV', 'AC', 'Mini-fridge'] },
    { id: 2, name: 'Deluxe Room', price: 180, capacity: 2, available: 3, floor: '2nd', description: 'Spacious room with premium amenities', features: ['WiFi', 'TV', 'AC', 'Mini-bar', 'Coffee maker'] },
    { id: 3, name: 'Executive Suite', price: 280, capacity: 4, available: 2, floor: '3rd', description: 'Luxury suite with separate living area', features: ['WiFi', 'TV', 'AC', 'Mini-bar', 'Coffee maker', 'Work desk'] },
    { id: 4, name: 'Presidential Suite', price: 450, capacity: 4, available: 1, floor: '4th', description: 'Ultimate luxury with panoramic views', features: ['WiFi', 'TV', 'AC', 'Mini-bar', 'Coffee maker', 'Jacuzzi', 'Butler service'] },
  ];

  const ratePlans = [
    { id: 1, name: 'Standard Rate', description: 'Best Available Rate', discount: 0, cancellationPolicy: 'Free cancellation 48 hours before arrival' },
    { id: 2, name: 'Corporate Rate', description: 'Special rate for corporate bookings', discount: 15, cancellationPolicy: 'Free cancellation 24 hours before arrival' },
    { id: 3, name: 'Early Bird', description: 'Book 30 days in advance', discount: 20, cancellationPolicy: 'Non-refundable' },
    { id: 4, name: 'Weekend Special', description: 'Friday to Sunday stays', discount: 10, cancellationPolicy: 'Free cancellation 72 hours before arrival' },
  ];

  const extraServicesList = [
    { id: 1, name: 'Breakfast Buffet', price: 25, per: 'per person per night', category: 'dining' },
    { id: 2, name: 'Airport Pickup', price: 50, per: 'one time', category: 'transport' },
    { id: 3, name: 'Parking', price: 15, per: 'per night', category: 'parking' },
    { id: 4, name: 'Late Check-out (until 6 PM)', price: 35, per: 'one time', category: 'extras' },
    { id: 5, name: 'Early Check-in (from 10 AM)', price: 35, per: 'one time', category: 'extras' },
    { id: 6, name: 'Spa Access', price: 40, per: 'per person', category: 'wellness' },
    { id: 7, name: 'Premium WiFi', price: 10, per: 'per stay', category: 'internet' },
    { id: 8, name: 'Minibar Restock', price: 30, per: 'per day', category: 'dining' },
    { id: 9, name: 'Laundry Service', price: 20, per: 'per item', category: 'services' },
  ];

  const nationalityOptions = ['Indian', 'American', 'British', 'Canadian', 'Australian', 'German', 'French', 'Japanese', 'Chinese', 'Brazilian'];
  const idTypes = ['Passport', 'Driving License', 'National ID', 'Aadhaar Card', 'Voter ID'];
  const paymentMethods = ['Credit Card', 'Debit Card', 'Cash', 'Bank Transfer', 'UPI', 'PayPal', 'Cheque', 'Corporate Account'];
  const bookingSources = ['Walk-in', 'Phone', 'Website', 'Booking.com', 'Expedia', 'MakeMyTrip', 'Corporate Portal', 'Travel Agent'];
  const genderOptions = ['Male', 'Female', 'Other', 'Prefer not to say'];
  const purposeOptions = ['Leisure', 'Business', 'Conference', 'Wedding', 'Medical', 'Education', 'Family', 'Other'];

  const steps = [
    { id: 1, name: 'Guest Details', icon: <User size={16} />, description: 'Identify guest information' },
    { id: 2, name: 'Stay Details', icon: <Calendar size={16} />, description: 'Define stay duration & purpose' },
    { id: 3, name: 'Room Selection', icon: <Bed size={16} />, description: 'Choose rooms or room types' },
    { id: 4, name: 'Pricing & Payment', icon: <DollarSign size={16} />, description: 'Set rates & collect payment' },
    { id: 5, name: 'Confirmation', icon: <Check size={16} />, description: 'Review & confirm booking' },
  ];

  const validateStep = (step) => {
    const errors = {};
    
    switch(step) {
      case 1:
        if (!formData.firstName) errors.firstName = 'First name is required';
        if (!formData.lastName) errors.lastName = 'Last name is required';
        if (!formData.phone) errors.phone = 'Phone number is required';
        if (!formData.nationality) errors.nationality = 'Nationality is required';
        if (!formData.idType && formData.idNumber) errors.idType = 'ID type is required when ID number is provided';
        break;
        
      case 2:
        if (!formData.checkInDate) errors.checkInDate = 'Check-in date is required';
        if (!formData.checkOutDate) errors.checkOutDate = 'Check-out date is required';
        if (formData.checkInDate && formData.checkOutDate) {
          const checkIn = new Date(formData.checkInDate);
          const checkOut = new Date(formData.checkOutDate);
          if (checkOut <= checkIn) errors.checkOutDate = 'Check-out date must be after check-in date';
        }
        if (!formData.adults || formData.adults < 1) errors.adults = 'At least 1 adult is required';
        break;
        
      case 3:
        if (formData.roomSelectionMode === 'type') {
          if (formData.selectedRoomTypes.length === 0) errors.roomSelection = 'Please select at least one room type';
        } else {
          if (formData.selectedRooms.length === 0) errors.roomSelection = 'Please select at least one room';
        }
        break;
        
      case 4:
        if (!formData.ratePlan) errors.ratePlan = 'Rate plan is required';
        if (formData.paymentType !== 'none' && !formData.paymentMethod) errors.paymentMethod = 'Payment method is required';
        if (formData.paymentType === 'partial' && formData.advancePayment < calculateTotal() * 0.2) {
          errors.advancePayment = 'Minimum 20% advance required';
        }
        if (!formData.termsAccepted) errors.termsAccepted = 'You must accept the terms and conditions';
        break;
    }
    
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    
    if (type === 'checkbox') {
      if (name === 'extraServices') {
        setFormData(prev => {
          const services = [...prev.extraServices];
          if (checked) {
            services.push(value);
          } else {
            const index = services.indexOf(value);
            if (index > -1) services.splice(index, 1);
          }
          return { ...prev, extraServices: services };
        });
      } else {
        setFormData(prev => ({ ...prev, [name]: checked }));
      }
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
    
    if (validationErrors[name]) {
      setValidationErrors(prev => ({ ...prev, [name]: undefined }));
    }
  };

  const handleRoomTypeChange = (roomTypeId, numberOfRooms) => {
    setFormData(prev => {
      const existingIndex = prev.selectedRoomTypes.findIndex(rt => rt.typeId === roomTypeId);
      let updatedRoomTypes;
      
      if (existingIndex > -1) {
        if (numberOfRooms === 0) {
          updatedRoomTypes = prev.selectedRoomTypes.filter(rt => rt.typeId !== roomTypeId);
        } else {
          updatedRoomTypes = [...prev.selectedRoomTypes];
          updatedRoomTypes[existingIndex] = { ...updatedRoomTypes[existingIndex], numberOfRooms };
        }
      } else if (numberOfRooms > 0) {
        updatedRoomTypes = [...prev.selectedRoomTypes, { typeId: roomTypeId, numberOfRooms }];
      } else {
        updatedRoomTypes = prev.selectedRoomTypes;
      }
      
      const newBasePrice = updatedRoomTypes.reduce((total, rt) => {
        const roomType = roomTypes.find(r => r.id === rt.typeId);
        return total + (roomType.price * rt.numberOfRooms * calculateNights());
      }, 0);
      
      return { 
        ...prev, 
        selectedRoomTypes: updatedRoomTypes,
        basePrice: newBasePrice
      };
    });
  };

  const handleRoomSelect = (roomNumber) => {
    setFormData(prev => {
      const rooms = [...prev.selectedRooms];
      const index = rooms.indexOf(roomNumber.toString());
      
      if (index > -1) {
        rooms.splice(index, 1);
      } else {
        rooms.push(roomNumber.toString());
      }
      
      return { 
        ...prev, 
        selectedRooms: rooms,
        totalRooms: rooms.length
      };
    });
  };

  const calculateNights = () => {
    if (formData.checkInDate && formData.checkOutDate) {
      const checkIn = new Date(formData.checkInDate);
      const checkOut = new Date(formData.checkOutDate);
      const nights = Math.ceil((checkOut - checkIn) / (1000 * 60 * 60 * 24));
      return nights > 0 ? nights : 0;
    }
    return 0;
  };

  const calculateDiscount = () => {
    if (formData.discountType === 'percentage') {
      return (formData.basePrice * formData.discountValue) / 100;
    } else {
      return parseFloat(formData.discountValue) || 0;
    }
  };

  const calculateExtraServicesTotal = () => {
    return formData.extraServices.reduce((total, serviceId) => {
      const service = extraServicesList.find(s => s.id === parseInt(serviceId));
      const nights = calculateNights();
      const guests = formData.adults + formData.children;
      
      if (service) {
        if (service.per.includes('per night')) {
          return total + (service.price * nights * guests);
        } else if (service.per.includes('per person')) {
          return total + (service.price * guests);
        } else {
          return total + service.price;
        }
      }
      return total;
    }, 0);
  };

  const calculateSubtotal = () => {
    return formData.basePrice + calculateExtraServicesTotal();
  };

  const calculateTax = () => {
    const subtotal = calculateSubtotal();
    return (subtotal * formData.taxPercentage) / 100;
  };

  const calculateTotal = () => {
    const subtotal = calculateSubtotal();
    const discount = calculateDiscount();
    const tax = calculateTax();
    return subtotal - discount + tax;
  };

  const calculateBalance = () => {
    const total = calculateTotal();
    const advance = parseFloat(formData.advancePayment) || 0;
    return total - advance;
  };

  // Search guests from backend
  const handleSearchGuest = async () => {
    if (formData.guestSearch.length > 2) {
      try {
        setGuestSearchLoading(true);
        const response = await axios.get(`${API_BASE_URL}/v1/guests/search`, {
          params: { keyword: formData.guestSearch }
        });
        
        console.log('Guest Search Response:', response.data);
        
        if (response.data && response.data.success) {
          setSearchResults(response.data.data || []);
          if (response.data.data && response.data.data.length > 0) {
            showToast(`${response.data.data.length} guest(s) found`, 'success');
          } else {
            showToast('No guests found', 'info');
          }
        } else {
          setSearchResults([]);
        }
      } catch (error) {
        console.error('Error searching guests:', error);
        showToast('Error searching guests', 'error');
        // Fallback to mock data
        setSearchResults([
          { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@example.com', phone: '+1234567890', previousStays: 3, loyaltyTier: 'Gold' },
          { id: 2, firstName: 'Jane', lastName: 'Smith', email: 'jane@example.com', phone: '+0987654321', previousStays: 1, loyaltyTier: 'Silver' },
          { id: 3, firstName: 'Robert', lastName: 'Johnson', email: 'robert@example.com', phone: '+1122334455', previousStays: 5, loyaltyTier: 'Platinum' },
        ]);
      } finally {
        setGuestSearchLoading(false);
      }
    }
  };

  const handleSelectGuest = (guest) => {
    setFormData(prev => ({
      ...prev,
      firstName: guest.firstName || '',
      lastName: guest.lastName || '',
      email: guest.email || '',
      phone: guest.phone || '',
      guestSearch: `${guest.firstName} ${guest.lastName}`,
      nationality: guest.nationality || '',
      loyaltyNumber: guest.loyaltyNumber || '',
      address: guest.address || '',
    }));
    setSearchResults([]);
    showToast(`Guest ${guest.firstName} ${guest.lastName} loaded`, 'success');
  };

  // Add form data to array function
  const addFormDataToArray = () => {
    const formDataCopy = { ...formData };
    
    const dataToStore = {
      id: Date.now(),
      timestamp: new Date().toISOString(),
      step: currentStep,
      data: { ...formDataCopy }
    };

    setFormDataArray(prev => [...prev, dataToStore]);
    showToast('Form data saved to array', 'success');
  };

  const handleSaveDraft = () => {
    addFormDataToArray();
    
    const drafts = JSON.parse(localStorage.getItem('bookingDrafts') || '[]');
    drafts.push({
      ...formData,
      draftId: Date.now(),
      savedAt: new Date().toISOString(),
      progress: currentStep,
      formDataArray: formDataArray
    });
    localStorage.setItem('bookingDrafts', JSON.stringify(drafts));
    setDraftSaved(true);
    showToast('Draft saved successfully', 'success');
    setTimeout(() => setDraftSaved(false), 3000);
  };

  const handleResetForm = () => {
    if (window.confirm('Are you sure? All unsaved changes will be lost.')) {
      setFormData({
        guestSearch: '',
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        nationality: '',
        address: '',
        idType: '',
        idNumber: '',
        contactType: 'same',
        contactName: '',
        contactPhone: '',
        contactEmail: '',
        dateOfBirth: '',
        gender: '',
        passportNumber: '',
        passportExpiry: '',
        country: '',
        state: '',
        city: '',
        zipCode: '',
        emergencyContact: '',
        company: '',
        businessEmail: '',
        loyaltyNumber: '',
        checkInDate: '',
        checkOutDate: '',
        adults: 1,
        children: 0,
        infants: 0,
        arrivalTime: '14:00',
        departureTime: '12:00',
        purposeOfVisit: '',
        specialInstructions: '',
        roomSelectionMode: 'type',
        selectedRoomTypes: [],
        selectedRooms: [],
        totalRooms: 0,
        ratePlan: '',
        basePrice: 0,
        discountType: 'percentage',
        discountValue: 0,
        promoCode: '',
        taxPercentage: 10,
        extraServices: [],
        paymentType: 'partial',
        paymentMethod: '',
        advancePayment: 0,
        transactionId: '',
        paymentDate: new Date().toISOString().split('T')[0],
        paymentRemarks: '',
        specialRequests: '',
        bookingSource: 'walk-in',
        remarks: '',
        marketingOptIn: false,
        termsAccepted: false,
        bedPreference: '',
        smokingPreference: 'non-smoking',
        floorPreference: '',
        viewPreference: '',
        extraBed: false,
        crib: false,
        wheelchairAccess: false,
        earlyCheckIn: false,
        lateCheckOut: false,
      });
      setFormDataArray([]);
      setCurrentStep(1);
      setValidationErrors({});
      showToast('Form reset successfully', 'info');
    }
  };

  const handleViewAvailability = async () => {
    if (formData.checkInDate && formData.checkOutDate) {
      setShowAvailability(true);
      
      try {
        setIsLoading(true);
        const response = await axios.get(`${API_BASE_URL}/v1/rooms/available`, {
          params: {
            checkIn: formData.checkInDate,
            checkOut: formData.checkOutDate,
            roomType: formData.roomSelectionMode === 'type' ? formData.selectedRoomTypes[0]?.typeId : ''
          }
        });
        
        console.log('Available Rooms Response:', response.data);
        
        if (response.data && response.data.success) {
          const roomsData = response.data.data || [];
          
          if (Array.isArray(roomsData)) {
            const transformedRooms = roomsData.map(room => ({
              id: room.id,
              number: room.roomNumber || room.id.toString(),
              type: room.roomType || 'Standard Room',
              floor: room.floorNumber || 1,
              price: room.baseRate || 100,
              status: room.status || 'AVAILABLE',
              features: room.features || ['WiFi', 'TV']
            }));
            setAvailableRooms(transformedRooms);
          } else {
            createMockAvailableRooms();
          }
        } else {
          createMockAvailableRooms();
        }
      } catch (error) {
        console.error('Error fetching available rooms:', error);
        createMockAvailableRooms();
        showToast('Using mock data (API unavailable)', 'info');
      } finally {
        setIsLoading(false);
      }
    } else {
      showToast('Please select check-in and check-out dates first.', 'error');
    }
  };

  const createMockAvailableRooms = () => {
    const available = [];
    for (let i = 101; i <= 110; i++) {
      available.push({
        id: i,
        number: i.toString(),
        type: i <= 104 ? 'Standard Room' : i <= 108 ? 'Deluxe Room' : 'Executive Suite',
        floor: Math.floor(i / 100),
        price: i <= 104 ? 120 : i <= 108 ? 180 : 280,
        status: Math.random() > 0.3 ? 'AVAILABLE' : 'OCCUPIED',
        features: i <= 104 ? ['WiFi', 'TV'] : i <= 108 ? ['WiFi', 'TV', 'AC', 'Mini-bar'] : ['WiFi', 'TV', 'AC', 'Mini-bar', 'Coffee maker'],
      });
    }
    setAvailableRooms(available);
  };

  const handleNextStep = () => {
    if (validateStep(currentStep)) {
      addFormDataToArray();
      setCurrentStep(prev => Math.min(prev + 1, 5));
    } else {
      showToast('Please fix the validation errors before proceeding.', 'error');
    }
  };

  const handlePrevStep = () => {
    setCurrentStep(prev => Math.max(prev - 1, 1));
  };

  // Prepare data for backend
  const prepareBackendData = () => {
    // Transform selected room types to room numbers if in type mode
    const selectedRoomsToSend = formData.roomSelectionMode === 'type' 
      ? [] // Will be assigned by backend
      : formData.selectedRooms;

    // Transform selected room types for backend
    const selectedRoomTypesForBackend = formData.selectedRoomTypes.map(rt => ({
      typeId: rt.typeId,
      numberOfRooms: rt.numberOfRooms
    }));

    return {
      // Guest Information
      firstName: formData.firstName,
      lastName: formData.lastName,
      email: formData.email || null,
      phone: formData.phone,
      nationality: formData.nationality,
      address: formData.address || null,
      idType: formData.idType || null,
      idNumber: formData.idNumber || null,
      dateOfBirth: formData.dateOfBirth || null,
      gender: formData.gender || null,
      passportNumber: formData.passportNumber || null,
      passportExpiry: formData.passportExpiry || null,
      country: formData.country || null,
      state: formData.state || null,
      city: formData.city || null,
      zipCode: formData.zipCode || null,
      emergencyContact: formData.emergencyContact || null,
      company: formData.company || null,
      businessEmail: formData.businessEmail || null,
      loyaltyNumber: formData.loyaltyNumber || null,
      
      // Stay Details
      checkInDate: formData.checkInDate,
      checkOutDate: formData.checkOutDate,
      adults: parseInt(formData.adults) || 1,
      children: parseInt(formData.children) || 0,
      infants: parseInt(formData.infants) || 0,
      arrivalTime: formData.arrivalTime || null,
      departureTime: formData.departureTime || null,
      purposeOfVisit: formData.purposeOfVisit || null,
      specialInstructions: formData.specialInstructions || null,
      
      // Room Selection
      roomSelectionMode: formData.roomSelectionMode,
      selectedRoomTypes: selectedRoomTypesForBackend,
      selectedRooms: selectedRoomsToSend,
      totalRooms: formData.totalRooms || selectedRoomTypesForBackend.reduce((sum, rt) => sum + rt.numberOfRooms, 0),
      
      // Pricing & Offers
      ratePlan: formData.ratePlan || null,
      basePrice: parseFloat(formData.basePrice) || 0,
      discountType: formData.discountType,
      discountValue: parseFloat(formData.discountValue) || 0,
      promoCode: formData.promoCode || null,
      taxPercentage: parseFloat(formData.taxPercentage) || 10,
      extraServices: formData.extraServices.map(id => parseInt(id)),
      
      // Payment
      paymentType: formData.paymentType,
      paymentMethod: formData.paymentMethod || null,
      advancePayment: parseFloat(formData.advancePayment) || 0,
      transactionId: formData.transactionId || null,
      paymentDate: formData.paymentDate || null,
      paymentRemarks: formData.paymentRemarks || null,
      
      // Additional Information
      specialRequests: formData.specialRequests || null,
      bookingSource: formData.bookingSource || 'walk-in',
      remarks: formData.remarks || null,
      marketingOptIn: formData.marketingOptIn,
      termsAccepted: formData.termsAccepted,
      
      // Room Preferences
      bedPreference: formData.bedPreference || null,
      smokingPreference: formData.smokingPreference || 'non-smoking',
      floorPreference: formData.floorPreference || null,
      viewPreference: formData.viewPreference || null,
      
      // Special Services
      extraBed: formData.extraBed,
      crib: formData.crib,
      wheelchairAccess: formData.wheelchairAccess,
      earlyCheckIn: formData.earlyCheckIn,
      lateCheckOut: formData.lateCheckOut,

      // Calculated totals
      totalAmount: calculateTotal(),
      balanceDue: calculateBalance(),
      nights: calculateNights()
    };
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateStep(4)) {
      return;
    }
    
    // Add final form data to array
    addFormDataToArray();
    
    // Prepare data for backend
    const backendData = prepareBackendData();
    
    // Log complete booking details to console
    console.log('========== COMPLETE BOOKING DETAILS ==========');
    console.log('Form Data:', formData);
    console.log('Form Data Array:', formDataArray);
    console.log('Backend Data (to be sent):', backendData);
    console.log('Calculated Totals:', {
      nights: calculateNights(),
      subtotal: calculateSubtotal(),
      discount: calculateDiscount(),
      tax: calculateTax(),
      total: calculateTotal(),
      balance: calculateBalance()
    });
    console.log('=============================================');
    
    try {
      setIsSubmitting(true);
      showToast('Sending booking data to server...', 'info');
      
      // Send data to backend
      console.log('Sending POST request to:', `${API_BASE_URL}/v1/booking-form`);
      console.log('Request data:', JSON.stringify(backendData, null, 2));
      
      const response = await axios.post(`${API_BASE_URL}/v1/booking-form`, backendData, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      console.log('Backend Response:', response.data);
      
      if (response.data && response.data.success) {
        const newBookingId = response.data.data.bookingId || response.data.data.id || `BK${Date.now().toString().slice(-8)}`;
        setBookingId(newBookingId);
        
        // Clear drafts
        const drafts = JSON.parse(localStorage.getItem('bookingDrafts') || '[]');
        const updatedDrafts = drafts.filter(d => 
          !(d.firstName === formData.firstName && 
            d.lastName === formData.lastName && 
            d.checkInDate === formData.checkInDate)
        );
        localStorage.setItem('bookingDrafts', JSON.stringify(updatedDrafts));
        
        // Save to local storage as backup
        const bookings = JSON.parse(localStorage.getItem('bookings') || '[]');
        bookings.push({
          ...formData,
          bookingId: newBookingId,
          createdAt: new Date().toISOString(),
          status: 'confirmed',
          totalAmount: calculateTotal(),
          balanceDue: calculateBalance(),
          backendResponse: response.data
        });
        localStorage.setItem('bookings', JSON.stringify(bookings));
        
        setCurrentStep(5);
        showToast('Booking confirmed and saved to database!', 'success');
        
        // Log success
        console.log('✅ Booking successfully created with ID:', newBookingId);
        console.log('✅ Backend response:', response.data);
      } else {
        throw new Error(response.data.message || 'Booking creation failed');
      }
    } catch (error) {
      console.error('❌ Booking creation failed:', error);
      
      // Show detailed error
      let errorMessage = 'Unknown error';
      if (error.response) {
        console.error('❌ Error response data:', error.response.data);
        console.error('❌ Error response status:', error.response.status);
        console.error('❌ Error response headers:', error.response.headers);
        
        errorMessage = error.response.data?.message || 
                      error.response.data?.error || 
                      `HTTP ${error.response.status}: ${error.response.statusText}`;
      } else if (error.request) {
        console.error('❌ No response received:', error.request);
        errorMessage = 'No response from server. Please check if backend is running.';
      } else {
        console.error('❌ Error setting up request:', error.message);
        errorMessage = error.message;
      }
      
      showToast(`Failed to create booking: ${errorMessage}`, 'error');
      
      // Fallback: Save to localStorage
      const newBookingId = `BK${Date.now().toString().slice(-8)}`;
      setBookingId(newBookingId);
      
      const bookings = JSON.parse(localStorage.getItem('bookings') || '[]');
      bookings.push({
        ...formData,
        bookingId: newBookingId,
        createdAt: new Date().toISOString(),
        status: 'confirmed',
        totalAmount: calculateTotal(),
        balanceDue: calculateBalance(),
        error: errorMessage
      });
      localStorage.setItem('bookings', JSON.stringify(bookings));
      
      setCurrentStep(5);
      showToast('Booking saved locally (backend error)', 'warning');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handlePrintConfirmation = () => {
    window.print();
  };

  const handleSendConfirmation = () => {
    showToast(`Confirmation sent to ${formData.email}`, 'success');
  };

  const handleExportDetails = () => {
    const exportData = {
      formData: formData,
      formDataArray: formDataArray,
      calculated: {
        nights: calculateNights(),
        subtotal: calculateSubtotal(),
        discount: calculateDiscount(),
        tax: calculateTax(),
        total: calculateTotal(),
        balance: calculateBalance()
      },
      backendData: prepareBackendData()
    };
    
    const dataStr = JSON.stringify(exportData, null, 2);
    const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr);
    const exportFileDefaultName = `booking-details-${Date.now()}.json`;
    
    const linkElement = document.createElement('a');
    linkElement.setAttribute('href', dataUri);
    linkElement.setAttribute('download', exportFileDefaultName);
    linkElement.click();
    
    showToast('Booking details exported successfully', 'success');
  };

  const getStepClass = (stepId) => {
    if (stepId === currentStep) return 'bg-primary-600 text-white border-primary-600';
    if (stepId < currentStep) return 'bg-green-500 text-white border-green-500';
    return 'bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-400 border-gray-300 dark:border-gray-600';
  };

  useEffect(() => {
    const newBasePrice = formData.selectedRoomTypes.reduce((total, rt) => {
      const roomType = roomTypes.find(r => r.id === rt.typeId);
      return total + (roomType?.price * rt.numberOfRooms * calculateNights());
    }, 0);
    
    setFormData(prev => ({ ...prev, basePrice: newBasePrice }));
  }, [formData.checkInDate, formData.checkOutDate, formData.selectedRoomTypes]);

  // Toast styling
  const getToastColor = (type) => {
    switch(type) {
      case 'success': return 'bg-green-100 border-green-400 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case 'error': return 'bg-red-100 border-red-400 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      case 'info': return 'bg-blue-100 border-blue-400 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      default: return 'bg-gray-100 border-gray-400 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    }
  };

  const getToastIcon = (type) => {
    switch(type) {
      case 'success': return <CheckCircle className="text-green-500" size={20} />;
      case 'error': return <AlertCircle className="text-red-500" size={20} />;
      case 'info': return <Info className="text-blue-500" size={20} />;
      default: return <Info className="text-gray-500" size={20} />;
    }
  };

  // Function to view array data
  const viewArrayData = () => {
    console.log('Current form data array:', formDataArray);
    alert(`Form data array has ${formDataArray.length} entries. Check console for details.`);
  };

  // Test API connection
  const testAPIConnection = async () => {
    try {
      showToast('Testing API connection...', 'info');
      const response = await axios.get(`${API_BASE_URL}/v1/rooms/health`);
      console.log('API Health Check:', response.data);
      showToast(`API Connection: ${response.data}`, 'success');
    } catch (error) {
      console.error('API Connection Test Failed:', error);
      showToast('API Connection Failed. Check if backend is running.', 'error');
    }
  };

  return (
    <div className="space-y-6 p-4 md:p-6">
      {/* Custom Toast Container */}
      <div className="fixed top-4 right-4 z-50 space-y-2">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`p-4 rounded-lg border shadow-lg flex items-center gap-3 animate-fade-in ${getToastColor(toast.type)}`}
            style={{ minWidth: '300px' }}
          >
            {getToastIcon(toast.type)}
            <span className="font-medium">{toast.message}</span>
          </div>
        ))}
      </div>

      {/* Page Header */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">New Booking</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Create a new reservation for future check-in</p>
          <div className="flex items-center gap-2 mt-1">
            <p className="text-sm text-blue-600 dark:text-blue-400">
              Data array entries: {formDataArray.length}
            </p>
            <button 
              onClick={viewArrayData}
              className="ml-2 text-xs bg-blue-100 hover:bg-blue-200 text-blue-800 px-2 py-1 rounded"
            >
              View Array
            </button>
            <button 
              onClick={testAPIConnection}
              className="ml-2 text-xs bg-green-100 hover:bg-green-200 text-green-800 px-2 py-1 rounded"
            >
              Test API
            </button>
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={handleResetForm}
            className="btn-secondary flex items-center gap-2"
          >
            <RefreshCw size={16} />
            Reset Form
          </button>
          <button
            type="button"
            onClick={handleViewAvailability}
            className="btn-secondary flex items-center gap-2"
          >
            <Eye size={16} />
            View Availability
          </button>
          <button
            type="button"
            onClick={handleSaveDraft}
            className="btn-secondary flex items-center gap-2"
            disabled={draftSaved}
          >
            {draftSaved ? <Check size={16} /> : <Save size={16} />}
            {draftSaved ? 'Draft Saved' : 'Save as Draft'}
          </button>
          <button
            type="submit"
            form="booking-form"
            className="btn-primary bg-green-600 hover:bg-green-700 flex items-center gap-2"
            disabled={currentStep !== 4 || isSubmitting}
          >
            {isSubmitting ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                Processing...
              </>
            ) : (
              <>
                <Check size={16} />
                Confirm Booking
              </>
            )}
          </button>
        </div>
      </div>

      {/* Progress Indicator */}
      <div className="card">
        <div className="flex flex-col md:flex-row items-center justify-between">
          {steps.map((step, index) => (
            <div key={step.id} className="flex items-center flex-1">
              <div className="flex flex-col items-center">
                <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold border-2 ${getStepClass(step.id)}`}>
                  {step.id < currentStep ? <Check size={16} /> : step.id === currentStep ? step.id : step.id}
                </div>
                <span className="text-xs mt-2 text-gray-600 dark:text-gray-400">{step.name}</span>
                <span className="text-xs text-gray-500 hidden md:block">{step.description}</span>
              </div>
              {index < steps.length - 1 && (
                <div className={`flex-1 h-1 mx-4 hidden md:block ${step.id < currentStep ? 'bg-green-500' : 'bg-gray-300 dark:bg-gray-700'}`} />
              )}
            </div>
          ))}
        </div>
        <div className="mt-4 text-center text-sm text-gray-600 dark:text-gray-400">
          Step {currentStep} of {steps.length}: {steps[currentStep-1].name}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Form */}
        <div className="lg:col-span-2 space-y-6">
          <form id="booking-form" onSubmit={handleSubmit} className="space-y-6">
            {/* Step 1: Guest Details */}
            {currentStep >= 1 && (
              <div className="card">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-xl font-semibold flex items-center gap-2">
                    <User size={24} />
                    Guest Details
                  </h2>
                  <span className="text-sm text-gray-500">Step 1 of 5</span>
                </div>

                {/* Guest Search */}
                <div className="mb-8">
                  <label className="block text-sm font-medium mb-2">Search Existing Guest</label>
                  <div className="flex gap-2">
                    <div className="relative flex-1">
                      <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                      <input
                        type="text"
                        name="guestSearch"
                        value={formData.guestSearch}
                        onChange={handleInputChange}
                        onKeyUp={(e) => e.key === 'Enter' && handleSearchGuest()}
                        className="input-field pl-10"
                        placeholder="Search by name, phone, or email..."
                      />
                    </div>
                    <button
                      type="button"
                      onClick={handleSearchGuest}
                      className="btn-secondary"
                      disabled={guestSearchLoading}
                    >
                      {guestSearchLoading ? (
                        <>
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-600"></div>
                          Searching...
                        </>
                      ) : 'Search'}
                    </button>
                    <button
                      type="button"
                      className="btn-primary"
                      onClick={() => {
                        setFormData(prev => ({ ...prev, guestSearch: '' }));
                        setSearchResults([]);
                      }}
                    >
                      New Guest
                    </button>
                  </div>
                  
                  {searchResults.length > 0 && (
                    <div className="mt-3 border rounded-lg overflow-hidden">
                      {searchResults.map(guest => (
                        <div
                          key={guest.id}
                          className="p-3 border-b last:border-b-0 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer"
                          onClick={() => handleSelectGuest(guest)}
                        >
                          <div className="flex justify-between items-start">
                            <div>
                              <div className="font-medium">{guest.firstName} {guest.lastName}</div>
                              <div className="text-sm text-gray-600 dark:text-gray-400">
                                {guest.email} • {guest.phone}
                              </div>
                              <div className="text-xs text-gray-500">
                                Nationality: {guest.nationality || 'Not specified'}
                              </div>
                            </div>
                            <span className={`px-2 py-1 text-xs rounded ${
                              guest.loyaltyTier === 'Platinum' 
                                ? 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400'
                                : guest.loyaltyTier === 'Gold'
                                ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400'
                                : 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400'
                            }`}>
                              {guest.loyaltyTier || 'Member'}
                            </span>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Guest Information */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-2">First Name *</label>
                    <input
                      type="text"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleInputChange}
                      className={`input-field ${validationErrors.firstName ? 'border-red-500' : ''}`}
                      required
                    />
                    {validationErrors.firstName && (
                      <p className="text-red-500 text-sm mt-1">{validationErrors.firstName}</p>
                    )}
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Last Name *</label>
                    <input
                      type="text"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleInputChange}
                      className={`input-field ${validationErrors.lastName ? 'border-red-500' : ''}`}
                      required
                    />
                    {validationErrors.lastName && (
                      <p className="text-red-500 text-sm mt-1">{validationErrors.lastName}</p>
                    )}
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Email</label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                      <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleInputChange}
                        className="input-field pl-10"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Phone *</label>
                    <div className="relative">
                      <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                      <input
                        type="tel"
                        name="phone"
                        value={formData.phone}
                        onChange={handleInputChange}
                        className={`input-field pl-10 ${validationErrors.phone ? 'border-red-500' : ''}`}
                        required
                      />
                    </div>
                    {validationErrors.phone && (
                      <p className="text-red-500 text-sm mt-1">{validationErrors.phone}</p>
                    )}
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Date of Birth</label>
                    <input
                      type="date"
                      name="dateOfBirth"
                      value={formData.dateOfBirth}
                      onChange={handleInputChange}
                      className="input-field"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Gender</label>
                    <select
                      name="gender"
                      value={formData.gender}
                      onChange={handleInputChange}
                      className="input-field"
                    >
                      <option value="">Select Gender</option>
                      {genderOptions.map(option => (
                        <option key={option} value={option}>{option}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Nationality *</label>
                    <select
                      name="nationality"
                      value={formData.nationality}
                      onChange={handleInputChange}
                      className={`input-field ${validationErrors.nationality ? 'border-red-500' : ''}`}
                      required
                    >
                      <option value="">Select Nationality</option>
                      {nationalityOptions.map(nat => (
                        <option key={nat} value={nat}>{nat}</option>
                      ))}
                    </select>
                    {validationErrors.nationality && (
                      <p className="text-red-500 text-sm mt-1">{validationErrors.nationality}</p>
                    )}
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">ID Type</label>
                    <select
                      name="idType"
                      value={formData.idType}
                      onChange={handleInputChange}
                      className="input-field"
                    >
                      <option value="">Select ID Type</option>
                      {idTypes.map(type => (
                        <option key={type} value={type}>{type}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">ID Number</label>
                    <input
                      type="text"
                      name="idNumber"
                      value={formData.idNumber}
                      onChange={handleInputChange}
                      className="input-field"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium mb-2">Address</label>
                    <div className="relative">
                      <MapPin className="absolute left-3 top-3 text-gray-400" size={18} />
                      <textarea
                        name="address"
                        value={formData.address}
                        onChange={handleInputChange}
                        className="input-field pl-10"
                        rows="2"
                      />
                    </div>
                  </div>
                </div>

                {/* Additional Information */}
                <div className="mt-6">
                  <h3 className="text-lg font-semibold mb-4">Additional Information</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-2">Company</label>
                      <input
                        type="text"
                        name="company"
                        value={formData.company}
                        onChange={handleInputChange}
                        className="input-field"
                        placeholder="Company name"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Business Email</label>
                      <input
                        type="email"
                        name="businessEmail"
                        value={formData.businessEmail}
                        onChange={handleInputChange}
                        className="input-field"
                        placeholder="company@example.com"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Loyalty Number</label>
                      <input
                        type="text"
                        name="loyaltyNumber"
                        value={formData.loyaltyNumber}
                        onChange={handleInputChange}
                        className="input-field"
                        placeholder="LOY123456"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Emergency Contact</label>
                      <input
                        type="text"
                        name="emergencyContact"
                        value={formData.emergencyContact}
                        onChange={handleInputChange}
                        className="input-field"
                        placeholder="+1 (555) 123-4567"
                      />
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Step 2: Stay Details */}
            {currentStep >= 2 && (
              <div className="card">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-xl font-semibold flex items-center gap-2">
                    <Calendar size={24} />
                    Stay Details
                  </h2>
                  <span className="text-sm text-gray-500">Step 2 of 5</span>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-2">Check-in Date *</label>
                    <input
                      type="date"
                      name="checkInDate"
                      value={formData.checkInDate}
                      onChange={handleInputChange}
                      className={`input-field ${validationErrors.checkInDate ? 'border-red-500' : ''}`}
                      required
                    />
                    {validationErrors.checkInDate && (
                      <p className="text-red-500 text-sm mt-1">{validationErrors.checkInDate}</p>
                    )}
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Check-out Date *</label>
                    <input
                      type="date"
                      name="checkOutDate"
                      value={formData.checkOutDate}
                      onChange={handleInputChange}
                      className={`input-field ${validationErrors.checkOutDate ? 'border-red-500' : ''}`}
                      required
                    />
                    {validationErrors.checkOutDate && (
                      <p className="text-red-500 text-sm mt-1">{validationErrors.checkOutDate}</p>
                    )}
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Adults *</label>
                    <input
                      type="number"
                      name="adults"
                      min="1"
                      max="10"
                      value={formData.adults}
                      onChange={handleInputChange}
                      className={`input-field ${validationErrors.adults ? 'border-red-500' : ''}`}
                      required
                    />
                    {validationErrors.adults && (
                      <p className="text-red-500 text-sm mt-1">{validationErrors.adults}</p>
                    )}
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Children</label>
                    <input
                      type="number"
                      name="children"
                      min="0"
                      max="10"
                      value={formData.children}
                      onChange={handleInputChange}
                      className="input-field"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Arrival Time</label>
                    <input
                      type="time"
                      name="arrivalTime"
                      value={formData.arrivalTime}
                      onChange={handleInputChange}
                      className="input-field"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Departure Time</label>
                    <input
                      type="time"
                      name="departureTime"
                      value={formData.departureTime}
                      onChange={handleInputChange}
                      className="input-field"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Purpose of Visit</label>
                    <select
                      name="purposeOfVisit"
                      value={formData.purposeOfVisit}
                      onChange={handleInputChange}
                      className="input-field"
                    >
                      <option value="">Select Purpose</option>
                      {purposeOptions.map(purpose => (
                        <option key={purpose} value={purpose}>{purpose}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2">Booking Source</label>
                    <select
                      name="bookingSource"
                      value={formData.bookingSource}
                      onChange={handleInputChange}
                      className="input-field"
                    >
                      {bookingSources.map(source => (
                        <option key={source} value={source.toLowerCase()}>{source}</option>
                      ))}
                    </select>
                  </div>
                </div>

                {/* Special Instructions */}
                <div className="mt-6">
                  <label className="block text-sm font-medium mb-2">Special Instructions</label>
                  <textarea
                    name="specialInstructions"
                    value={formData.specialInstructions}
                    onChange={handleInputChange}
                    className="input-field"
                    rows="3"
                    placeholder="Any special requests or instructions..."
                  />
                </div>

                {/* Stay Summary */}
                <div className="mt-6 p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
                  <div className="flex items-center justify-between">
                    <div className="space-y-1">
                      <p className="text-sm font-medium">Stay Duration</p>
                      <p className="text-2xl font-bold">{calculateNights()} Nights</p>
                      <p className="text-sm text-gray-600 dark:text-gray-400">
                        {formData.checkInDate && new Date(formData.checkInDate).toLocaleDateString()} - {' '}
                        {formData.checkOutDate && new Date(formData.checkOutDate).toLocaleDateString()}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium">Guests</p>
                      <p className="text-xl font-bold">{formData.adults + formData.children} Total</p>
                      <p className="text-sm text-gray-600 dark:text-gray-400">
                        {formData.adults} Adult(s), {formData.children} Child(ren)
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Step 3: Room Selection */}
            {currentStep >= 3 && (
              <div className="card">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-xl font-semibold flex items-center gap-2">
                    <Bed size={24} />
                    Room Selection
                  </h2>
                  <span className="text-sm text-gray-500">Step 3 of 5</span>
                </div>

                {/* Selection Mode */}
                <div className="mb-6">
                  <label className="block text-sm font-medium mb-2">Selection Mode</label>
                  <div className="flex gap-4">
                    <label className="inline-flex items-center">
                      <input
                        type="radio"
                        name="roomSelectionMode"
                        value="type"
                        checked={formData.roomSelectionMode === 'type'}
                        onChange={handleInputChange}
                        className="mr-2"
                      />
                      Room Type Wise
                    </label>
                    <label className="inline-flex items-center">
                      <input
                        type="radio"
                        name="roomSelectionMode"
                        value="number"
                        checked={formData.roomSelectionMode === 'number'}
                        onChange={handleInputChange}
                        className="mr-2"
                      />
                      Room Number Wise
                    </label>
                  </div>
                  {validationErrors.roomSelection && (
                    <p className="text-red-500 text-sm mt-1">{validationErrors.roomSelection}</p>
                  )}
                </div>

                {formData.roomSelectionMode === 'type' ? (
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead className="bg-gray-50 dark:bg-gray-700/50">
                        <tr>
                          <th className="px-4 py-3 text-left text-sm font-semibold">Select</th>
                          <th className="px-4 py-3 text-left text-sm font-semibold">Room Type</th>
                          <th className="px-4 py-3 text-left text-sm font-semibold">Max Occupancy</th>
                          <th className="px-4 py-3 text-left text-sm font-semibold">Price/Night</th>
                          <th className="px-4 py-3 text-left text-sm font-semibold">Available</th>
                          <th className="px-4 py-3 text-left text-sm font-semibold">Rooms Required</th>
                          <th className="px-4 py-3 text-left text-sm font-semibold">Total</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                        {roomTypes.map(rt => {
                          const selectedRoom = formData.selectedRoomTypes.find(r => r.typeId === rt.id);
                          const roomsSelected = selectedRoom ? selectedRoom.numberOfRooms : 0;
                          const roomTotal = rt.price * roomsSelected * calculateNights();
                          
                          return (
                            <tr key={rt.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                              <td className="px-4 py-3">
                                <input
                                  type="checkbox"
                                  checked={roomsSelected > 0}
                                  onChange={(e) => handleRoomTypeChange(rt.id, e.target.checked ? 1 : 0)}
                                  className="rounded"
                                />
                              </td>
                              <td className="px-4 py-3">
                                <div>
                                  <p className="font-medium">{rt.name}</p>
                                  <p className="text-xs text-gray-500">{rt.description}</p>
                                </div>
                              </td>
                              <td className="px-4 py-3">
                                <div className="flex items-center gap-1">
                                  <Users size={14} className="text-gray-500" />
                                  <span>{rt.capacity} guests</span>
                                </div>
                              </td>
                              <td className="px-4 py-3">
                                <span className="font-semibold">${rt.price}</span>
                              </td>
                              <td className="px-4 py-3">
                                <span className={`px-2 py-1 text-xs rounded ${
                                  rt.available > 3 
                                    ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'
                                    : rt.available > 0
                                    ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400'
                                    : 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400'
                                }`}>
                                  {rt.available} rooms
                                </span>
                              </td>
                              <td className="px-4 py-3">
                                <div className="flex items-center gap-2">
                                  <button
                                    type="button"
                                    onClick={() => handleRoomTypeChange(rt.id, Math.max(0, roomsSelected - 1))}
                                    className="w-8 h-8 rounded-full border flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-700"
                                    disabled={roomsSelected === 0}
                                  >
                                    <span className="sr-only">Decrease</span>
                                    -
                                  </button>
                                  <input
                                    type="number"
                                    min="0"
                                    max={rt.available}
                                    value={roomsSelected}
                                    onChange={(e) => handleRoomTypeChange(rt.id, parseInt(e.target.value) || 0)}
                                    className="w-16 text-center border rounded py-1"
                                  />
                                  <button
                                    type="button"
                                    onClick={() => handleRoomTypeChange(rt.id, Math.min(rt.available, roomsSelected + 1))}
                                    className="w-8 h-8 rounded-full border flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-700"
                                    disabled={roomsSelected >= rt.available}
                                  >
                                    <span className="sr-only">Increase</span>
                                    +
                                  </button>
                                </div>
                              </td>
                              <td className="px-4 py-3">
                                <span className="font-bold">${roomTotal.toFixed(2)}</span>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <div>
                    <div className="flex justify-between items-center mb-4">
                      <div>
                        <p className="font-medium">Available Rooms for Selected Dates</p>
                        <p className="text-sm text-gray-500">Click to select/deselect rooms</p>
                      </div>
                      <div className="flex items-center gap-4">
                        <div className="flex items-center gap-2">
                          <div className="w-3 h-3 bg-green-500 rounded"></div>
                          <span className="text-xs">Available</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <div className="w-3 h-3 bg-red-500 rounded"></div>
                          <span className="text-xs">Occupied</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <div className="w-3 h-3 bg-blue-500 rounded"></div>
                          <span className="text-xs">Selected</span>
                        </div>
                      </div>
                    </div>
                    
                    {isLoading ? (
                      <div className="flex justify-center items-center py-8">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
                        <span className="ml-3">Loading available rooms...</span>
                      </div>
                    ) : (
                      <>
                        {Array.isArray(availableRooms) && availableRooms.length > 0 ? (
                          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                            {availableRooms.map(room => (
                              <button
                                key={room.id || room.number}
                                type="button"
                                onClick={() => handleRoomSelect(room.number || room.id)}
                                className={`p-4 rounded-lg border-2 transition-all ${
                                  formData.selectedRooms.includes((room.number || room.id).toString())
                                    ? 'border-primary-600 bg-primary-50 dark:bg-primary-900/20'
                                    : (room.status || '').toUpperCase() === 'AVAILABLE'
                                    ? 'border-gray-300 dark:border-gray-600 hover:border-primary-500'
                                    : 'border-red-300 dark:border-red-600 opacity-50 cursor-not-allowed'
                                }`}
                                disabled={(room.status || '').toUpperCase() !== 'AVAILABLE'}
                              >
                                <div className="text-center">
                                  <div className="font-bold text-lg">{room.number || room.id || 'N/A'}</div>
                                  <div className="text-xs text-gray-600 dark:text-gray-400 mt-1">
                                    {room.type || 'Unknown Room Type'}
                                  </div>
                                  <div className="text-sm font-medium mt-2">${room.price || 0}</div>
                                  <div className="text-xs text-gray-500">Floor {room.floor || 'N/A'}</div>
                                  <div className="text-xs text-gray-500 mt-1">
                                    Status: {(room.status || 'unknown').toUpperCase()}
                                  </div>
                                </div>
                              </button>
                            ))}
                          </div>
                        ) : (
                          <div className="text-center py-8 border rounded-lg">
                            <p className="text-gray-500">No rooms available for selected dates</p>
                            <button
                              type="button"
                              onClick={createMockAvailableRooms}
                              className="mt-2 btn-secondary text-sm"
                            >
                              Load Mock Data
                            </button>
                          </div>
                        )}
                      </>
                    )}
                    
                    <div className="mt-4 text-sm">
                      Selected Rooms: {formData.selectedRooms.length} room(s)
                      {formData.selectedRooms.length > 0 && (
                        <span className="ml-2 text-primary-600 font-medium">
                          {formData.selectedRooms.join(', ')}
                        </span>
                      )}
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Step 4: Pricing & Payment */}
            {currentStep >= 4 && (
              <>
                {/* Pricing & Offers */}
                <div className="card">
                  <div className="flex items-center justify-between mb-6">
                    <h2 className="text-xl font-semibold flex items-center gap-2">
                      <DollarSign size={24} />
                      Pricing & Offers
                    </h2>
                    <span className="text-sm text-gray-500">Step 4 of 5</span>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-2">Rate Plan *</label>
                      <select
                        name="ratePlan"
                        value={formData.ratePlan}
                        onChange={handleInputChange}
                        className={`input-field ${validationErrors.ratePlan ? 'border-red-500' : ''}`}
                        required
                      >
                        <option value="">Select Rate Plan</option>
                        {ratePlans.map(rp => (
                          <option key={rp.id} value={rp.name}>
                            {rp.name} - {rp.description} {rp.discount > 0 ? `(${rp.discount}% off)` : ''}
                          </option>
                        ))}
                      </select>
                      {validationErrors.ratePlan && (
                        <p className="text-red-500 text-sm mt-1">{validationErrors.ratePlan}</p>
                      )}
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Promo Code</label>
                      <div className="flex gap-2">
                        <input
                          type="text"
                          name="promoCode"
                          value={formData.promoCode}
                          onChange={handleInputChange}
                          className="input-field flex-1"
                          placeholder="Enter promo code"
                        />
                        <button type="button" className="btn-secondary">
                          Apply
                        </button>
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Discount Type</label>
                      <select
                        name="discountType"
                        value={formData.discountType}
                        onChange={handleInputChange}
                        className="input-field"
                      >
                        <option value="percentage">Percentage (%)</option>
                        <option value="flat">Flat Amount</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">
                        {formData.discountType === 'percentage' ? 'Discount %' : 'Discount Amount'}
                      </label>
                      <input
                        type="number"
                        name="discountValue"
                        value={formData.discountValue}
                        onChange={handleInputChange}
                        className="input-field"
                        min="0"
                        max={formData.discountType === 'percentage' ? 100 : calculateSubtotal()}
                      />
                    </div>
                  </div>

                  {/* Extra Services */}
                  <div className="mt-6">
                    <label className="block text-sm font-medium mb-2">Extra Services</label>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                      {extraServicesList.map(service => (
                        <label
                          key={service.id}
                          className={`flex items-center justify-between p-3 border rounded-lg cursor-pointer transition-all ${
                            formData.extraServices.includes(service.id.toString())
                              ? 'border-primary-600 bg-primary-50 dark:bg-primary-900/20'
                              : 'border-gray-300 dark:border-gray-600 hover:border-primary-500'
                          }`}
                        >
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              name="extraServices"
                              value={service.id}
                              checked={formData.extraServices.includes(service.id.toString())}
                              onChange={handleInputChange}
                              className="mr-3"
                            />
                            <div>
                              <div className="font-medium">{service.name}</div>
                              <div className="text-xs text-gray-500">{service.per}</div>
                            </div>
                          </div>
                          <div className="font-bold">${service.price}</div>
                        </label>
                      ))}
                    </div>
                  </div>
                </div>

                {/* Payment Information */}
                <div className="card">
                  <div className="flex items-center justify-between mb-6">
                    <h2 className="text-xl font-semibold flex items-center gap-2">
                      <CreditCard size={24} />
                      Payment Information
                    </h2>
                    <span className="text-sm text-gray-500">Step 4 of 5</span>
                  </div>

                  <div className="mb-4">
                    <label className="block text-sm font-medium mb-2">Payment Type</label>
                    <div className="flex gap-4">
                      <label className="inline-flex items-center">
                        <input
                          type="radio"
                          name="paymentType"
                          value="none"
                          checked={formData.paymentType === 'none'}
                          onChange={handleInputChange}
                          className="mr-2"
                        />
                        No Payment
                      </label>
                      <label className="inline-flex items-center">
                        <input
                          type="radio"
                          name="paymentType"
                          value="partial"
                          checked={formData.paymentType === 'partial'}
                          onChange={handleInputChange}
                          className="mr-2"
                        />
                        Partial Payment
                      </label>
                      <label className="inline-flex items-center">
                        <input
                          type="radio"
                          name="paymentType"
                          value="full"
                          checked={formData.paymentType === 'full'}
                          onChange={handleInputChange}
                          className="mr-2"
                        />
                        Full Payment
                      </label>
                    </div>
                  </div>

                  {formData.paymentType !== 'none' && (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium mb-2">Payment Method *</label>
                        <select
                          name="paymentMethod"
                          value={formData.paymentMethod}
                          onChange={handleInputChange}
                          className={`input-field ${validationErrors.paymentMethod ? 'border-red-500' : ''}`}
                          required
                        >
                          <option value="">Select Payment Method</option>
                          {paymentMethods.map(method => (
                            <option key={method} value={method}>{method}</option>
                          ))}
                        </select>
                        {validationErrors.paymentMethod && (
                          <p className="text-red-500 text-sm mt-1">{validationErrors.paymentMethod}</p>
                        )}
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-2">Paid Amount *</label>
                        <input
                          type="number"
                          name="advancePayment"
                          value={formData.advancePayment}
                          onChange={handleInputChange}
                          className={`input-field ${validationErrors.advancePayment ? 'border-red-500' : ''}`}
                          min="0"
                          max={calculateTotal()}
                          required={formData.paymentType !== 'none'}
                        />
                        {formData.paymentType === 'partial' && (
                          <p className="text-xs text-gray-500 mt-1">
                            Minimum {Math.ceil(calculateTotal() * 0.2)} required (20%)
                          </p>
                        )}
                        {validationErrors.advancePayment && (
                          <p className="text-red-500 text-sm mt-1">{validationErrors.advancePayment}</p>
                        )}
                      </div>
                      {formData.paymentMethod && !['Cash', 'Cheque'].includes(formData.paymentMethod) && (
                        <div>
                          <label className="block text-sm font-medium mb-2">Transaction ID</label>
                          <input
                            type="text"
                            name="transactionId"
                            value={formData.transactionId}
                            onChange={handleInputChange}
                            className="input-field"
                            placeholder="Transaction reference number"
                          />
                        </div>
                      )}
                      <div>
                        <label className="block text-sm font-medium mb-2">Payment Date</label>
                        <input
                          type="date"
                          name="paymentDate"
                          value={formData.paymentDate}
                          onChange={handleInputChange}
                          className="input-field"
                        />
                      </div>
                      <div className="md:col-span-2">
                        <label className="block text-sm font-medium mb-2">Payment Remarks</label>
                        <input
                          type="text"
                          name="paymentRemarks"
                          value={formData.paymentRemarks}
                          onChange={handleInputChange}
                          className="input-field"
                          placeholder="Any remarks about this payment..."
                        />
                      </div>
                    </div>
                  )}
                </div>

                {/* Terms & Conditions */}
                <div className="card">
                  <div className="flex items-start gap-3">
                    <input
                      type="checkbox"
                      name="termsAccepted"
                      checked={formData.termsAccepted}
                      onChange={handleInputChange}
                      className="mt-1"
                      required
                    />
                    <div>
                      <label className="block text-sm font-medium">
                        I accept the terms and conditions *
                      </label>
                      <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        By accepting, you agree to our cancellation policy and hotel regulations.
                      </p>
                      {validationErrors.termsAccepted && (
                        <p className="text-red-500 text-sm mt-1">{validationErrors.termsAccepted}</p>
                      )}
                    </div>
                  </div>
                </div>
              </>
            )}

            {/* Step Navigation Buttons */}
            <div className="flex justify-between pt-4">
              <div>
                {currentStep > 1 && (
                  <button
                    type="button"
                    onClick={handlePrevStep}
                    className="btn-secondary flex items-center gap-2"
                  >
                    <ChevronLeft size={16} />
                    Previous
                  </button>
                )}
              </div>
              <div className="flex gap-3">
                {currentStep < 4 && (
                  <button
                    type="button"
                    onClick={handleNextStep}
                    className="btn-primary flex items-center gap-2"
                  >
                    Next Step
                    <ChevronRight size={16} />
                  </button>
                )}
              </div>
            </div>
          </form>
        </div>

        {/* Sticky Booking Summary Panel */}
        <div className="lg:col-span-1">
          <div className="sticky top-6 space-y-6">
            <div className="card bg-gradient-to-r from-primary-50 to-blue-50 dark:from-primary-900/20 dark:to-blue-900/20 border-2 border-primary-200 dark:border-primary-800">
              <h2 className="text-xl font-semibold mb-4">Booking Summary</h2>
              <div className="space-y-4">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">Guest</p>
                  <p className="font-semibold">
                    {formData.firstName || 'Guest'} {formData.lastName || ''}
                  </p>
                  {formData.email && (
                    <p className="text-sm text-gray-600 dark:text-gray-400">{formData.email}</p>
                  )}
                </div>
                
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">Stay Duration</p>
                  <p className="font-semibold">{calculateNights()} Night(s)</p>
                  {formData.checkInDate && formData.checkOutDate && (
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {new Date(formData.checkInDate).toLocaleDateString()} - {' '}
                      {new Date(formData.checkOutDate).toLocaleDateString()}
                    </p>
                  )}
                </div>
                
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">Rooms Selected</p>
                  {formData.roomSelectionMode === 'type' ? (
                    <div>
                      {formData.selectedRoomTypes.map(rt => {
                        const roomType = roomTypes.find(r => r.id === rt.typeId);
                        return (
                          <div key={rt.typeId} className="flex justify-between text-sm">
                            <span>{roomType?.name} × {rt.numberOfRooms}</span>
                            <span>${(roomType?.price * rt.numberOfRooms * calculateNights()).toFixed(2)}</span>
                          </div>
                        );
                      })}
                      {formData.selectedRoomTypes.length === 0 && (
                        <p className="text-sm text-gray-500">No rooms selected</p>
                      )}
                    </div>
                  ) : (
                    <p className="font-semibold">{formData.selectedRooms.length} room(s)</p>
                  )}
                </div>
                
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">Guests</p>
                  <p className="font-semibold">
                    {formData.adults + formData.children} Total
                  </p>
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    {formData.adults} Adult(s), {formData.children} Child(ren)
                  </p>
                </div>
                
                <div className="pt-4 border-t border-primary-300 dark:border-primary-700">
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span>Base Price:</span>
                      <span>${formData.basePrice.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span>Extra Services:</span>
                      <span>${calculateExtraServicesTotal().toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span>Subtotal:</span>
                      <span>${calculateSubtotal().toFixed(2)}</span>
                    </div>
                    {calculateDiscount() > 0 && (
                      <div className="flex justify-between text-sm text-green-600">
                        <span>Discount:</span>
                        <span>-${calculateDiscount().toFixed(2)}</span>
                      </div>
                    )}
                    <div className="flex justify-between text-sm">
                      <span>Tax ({formData.taxPercentage}%):</span>
                      <span>${calculateTax().toFixed(2)}</span>
                    </div>
                  </div>
                  
                  <div className="pt-4 border-t-2 border-primary-300 dark:border-primary-700">
                    <div className="flex justify-between">
                      <span className="text-lg font-bold">Grand Total:</span>
                      <span className="text-2xl font-bold text-primary-600">
                        ${calculateTotal().toFixed(2)}
                      </span>
                    </div>
                    <div className="flex justify-between mt-2">
                      <span className="text-gray-600 dark:text-gray-400">Advance Paid:</span>
                      <span className="font-semibold text-green-600">
                        ${(parseFloat(formData.advancePayment) || 0).toFixed(2)}
                      </span>
                    </div>
                    <div className="flex justify-between pt-2">
                      <span className="font-bold">Balance Due:</span>
                      <span className={`text-xl font-bold ${calculateBalance() > 0 ? 'text-red-600' : 'text-green-600'}`}>
                        ${calculateBalance().toFixed(2)}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="card">
              <h3 className="font-semibold mb-3">Quick Actions</h3>
              <div className="space-y-2">
                <button
                  type="button"
                  onClick={handleNextStep}
                  className="w-full btn-primary flex items-center justify-center gap-2"
                >
                  Continue to Next Step
                  <ChevronRight size={16} />
                </button>
                <button
                  type="button"
                  onClick={() => {
                    addFormDataToArray();
                    showToast('Current step saved to array', 'success');
                  }}
                  className="w-full btn-secondary flex items-center justify-center gap-2"
                >
                  <Save size={16} />
                  Save Current Step to Array
                </button>
                <button
                  type="button"
                  onClick={handleExportDetails}
                  className="w-full btn-outline flex items-center justify-center gap-2"
                >
                  <Download size={16} />
                  Export Details
                </button>
              </div>
            </div>

            {/* Array Data Status */}
            <div className="card bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800">
              <h3 className="font-semibold mb-3 flex items-center gap-2">
                <Disk size={18} />
                Data Array Status
              </h3>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-sm">Entries:</span>
                  <span className="font-bold">{formDataArray.length}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm">Current Step:</span>
                  <span className="font-bold">Step {currentStep}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm">Last Saved:</span>
                  <span className="text-sm">
                    {formDataArray.length > 0 
                      ? new Date(formDataArray[formDataArray.length - 1].timestamp).toLocaleTimeString()
                      : 'Never'
                    }
                  </span>
                </div>
                <button
                  onClick={() => {
                    console.log('Form Data Array:', formDataArray);
                    alert(`Check console for ${formDataArray.length} array entries`);
                  }}
                  className="w-full mt-2 btn-outline flex items-center justify-center gap-2"
                >
                  <EyeIcon size={16} />
                  View Array in Console
                </button>
              </div>
            </div>

            {/* Help & Support */}
            <div className="card">
              <h3 className="font-semibold mb-3 flex items-center gap-2">
                <HelpCircle size={18} />
                Need Help?
              </h3>
              <div className="space-y-2">
                <button className="w-full text-left p-3 border rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
                  <div className="font-medium">View Pricing Policy</div>
                  <div className="text-sm text-gray-500">Understand our rates and charges</div>
                </button>
                <button className="w-full text-left p-3 border rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
                  <div className="font-medium">Check Cancellation Policy</div>
                  <div className="text-sm text-gray-500">Review booking modifications</div>
                </button>
                <button className="w-full text-left p-3 border rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
                  <div className="font-medium">Contact Support</div>
                  <div className="text-sm text-gray-500">Get help from our team</div>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Step 5: Confirmation */}
      {currentStep === 5 && (
        <div className="card">
          <div className="text-center py-8">
            <div className="w-16 h-16 bg-green-100 dark:bg-green-900 rounded-full flex items-center justify-center mx-auto mb-4">
              <Check className="text-green-600 dark:text-green-400" size={32} />
            </div>
            <h2 className="text-2xl font-bold mb-2">Booking Confirmed!</h2>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              Your booking has been successfully created and confirmed.
            </p>
            
            <div className="bg-gradient-to-r from-green-50 to-blue-50 dark:from-green-900/20 dark:to-blue-900/20 border border-green-200 dark:border-green-800 rounded-xl p-6 max-w-md mx-auto mb-6">
              <div className="text-3xl font-bold text-primary-600 mb-2">{bookingId}</div>
              <div className="text-sm text-gray-600 dark:text-gray-400">Booking Reference Number</div>
              <div className="text-xs text-gray-500 mt-2">
                Created on {new Date().toLocaleDateString()} at {new Date().toLocaleTimeString()}
              </div>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 max-w-2xl mx-auto mb-8">
              <div className="text-center p-4 border rounded-lg">
                <div className="font-bold text-lg">{formData.firstName} {formData.lastName}</div>
                <div className="text-sm text-gray-600">Guest</div>
              </div>
              <div className="text-center p-4 border rounded-lg">
                <div className="font-bold text-lg">{formData.roomSelectionMode === 'type' 
                  ? `${formData.selectedRoomTypes.reduce((sum, rt) => sum + rt.numberOfRooms, 0)} Rooms`
                  : `${formData.selectedRooms.length} Rooms`
                }</div>
                <div className="text-sm text-gray-600">Room Count</div>
              </div>
              <div className="text-center p-4 border rounded-lg">
                <div className="font-bold text-lg">${calculateTotal().toFixed(2)}</div>
                <div className="text-sm text-gray-600">Total Amount</div>
              </div>
            </div>
            
            <div className="flex flex-wrap justify-center gap-3">
              <button
                onClick={handlePrintConfirmation}
                className="btn-primary flex items-center gap-2"
              >
                <Printer size={16} />
                Print Confirmation
              </button>
              <button
                onClick={handleSendConfirmation}
                className="btn-secondary flex items-center gap-2"
              >
                <Send size={16} />
                Send Email & SMS
              </button>
              <button
                onClick={() => navigate(`/bookings/${bookingId}`)}
                className="btn-secondary flex items-center gap-2"
              >
                <Eye size={16} />
                View Booking Details
              </button>
              <button
                onClick={() => navigate('/reservations')}
                className="btn-secondary flex items-center gap-2"
              >
                Back to Reservations
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Room Availability Modal */}
      {showAvailability && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-auto">
            <div className="p-6">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-bold">Room Availability</h2>
                <button
                  onClick={() => setShowAvailability(false)}
                  className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300"
                >
                  <X size={24} />
                </button>
              </div>
              
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-lg">
                  <div className="text-2xl font-bold text-primary-600">5</div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">Available Rooms</div>
                </div>
                <div className="bg-green-50 dark:bg-green-900/20 p-4 rounded-lg">
                  <div className="text-2xl font-bold text-green-600">8</div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">Occupied</div>
                </div>
                <div className="bg-yellow-50 dark:bg-yellow-900/20 p-4 rounded-lg">
                  <div className="text-2xl font-bold text-yellow-600">2</div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">Reserved</div>
                </div>
                <div className="bg-red-50 dark:bg-red-900/20 p-4 rounded-lg">
                  <div className="text-2xl font-bold text-red-600">1</div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">Out of Service</div>
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h3 className="font-semibold mb-3">Room Types Availability</h3>
                  <div className="space-y-3">
                    {roomTypes.map(rt => (
                      <div key={rt.id} className="flex justify-between items-center p-3 border rounded-lg">
                        <div>
                          <div className="font-medium">{rt.name}</div>
                          <div className="text-sm text-gray-600">${rt.price}/night</div>
                          <div className="text-xs text-gray-500">{rt.capacity} guests max</div>
                        </div>
                        <div className="text-right">
                          <div className="font-bold">{rt.available} available</div>
                          <div className="text-sm text-gray-600">Floor {rt.floor}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
                
                <div>
                  <h3 className="font-semibold mb-3">Quick Actions</h3>
                  <div className="space-y-3">
                    <button
                      onClick={() => {
                        setFormData(prev => ({ ...prev, roomSelectionMode: 'number' }));
                        setShowAvailability(false);
                        setCurrentStep(3);
                      }}
                      className="w-full btn-primary text-left p-4"
                    >
                      <div className="font-bold">Select Specific Rooms</div>
                      <div className="text-sm">Choose exact room numbers from available list</div>
                    </button>
                    <button
                      onClick={() => {
                        setFormData(prev => ({ ...prev, roomSelectionMode: 'type' }));
                        setShowAvailability(false);
                        setCurrentStep(3);
                      }}
                      className="w-full btn-secondary text-left p-4"
                    >
                      <div className="font-bold">Book by Room Type</div>
                      <div className="text-sm">Let system assign rooms automatically</div>
                    </button>
                    <button
                      onClick={() => setShowRoomGrid(!showRoomGrid)}
                      className="w-full btn-outline text-left p-4"
                    >
                      <div className="font-bold">View Floor Plan</div>
                      <div className="text-sm">Interactive room layout view</div>
                    </button>
                  </div>
                </div>
              </div>
              
              <div className="mt-6 flex justify-end">
                <button
                  onClick={() => setShowAvailability(false)}
                  className="btn-secondary"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BookingForm;