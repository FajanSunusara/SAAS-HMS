import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { CheckCircle, Download, Mail, Printer, Home, Edit, Clock, User, CreditCard, Camera, FileText, ShieldCheck } from 'lucide-react';

const CheckInConfirmation = () => {
  const navigate = useNavigate();
  const { bookingId } = useParams();
  const [isCheckingIn, setIsCheckingIn] = useState(false);

  // Enhanced confirmation data with new fields
  const confirmation = {
    bookingId: bookingId || 'BK-847291',
    guestName: 'John Anderson',
    email: 'john.anderson@email.com',
    phone: '+1 (555) 123-4567',
    nationality: 'American',
    address: '123 Main Street, New York, NY 10001',
    room: '301',
    roomType: 'Deluxe Suite',
    floor: 3,
    checkIn: '2024-12-21',
    checkInTime: '02:00 PM',
    checkOut: '2024-12-24',
    checkOutTime: '11:00 AM',
    nights: 3,
    adults: 2,
    children: 1,
    purpose: 'Leisure',
    ratePlan: 'Corporate Flexible Rate',
    ratePerNight: 180,
    roomCharges: 540,
    taxesAndFees: 97.20,
    discounts: 54.00,
    totalAmount: 583.20,
    advancePaid: 200,
    balanceDue: 383.20,
    paymentStatus: 'Payment Complete',
    source: 'Online Travel Agent',
    discountCode: 'WELCOME10',
    idType: 'Passport',
    documentNumber: 'AB123456789',
    idPhoto: 'https://images.unsplash.com/photo-1544717305-99670f9c28f3?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80',
    guestPhoto: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80',
    wifiPassword: 'Hotel@2024',
    amenities: ['Free WiFi', 'Breakfast Included', 'Gym Access', 'Pool Access', 'Room Service'],
    specialRequests: 'High floor, city view',
    verificationStatus: 'Verified',
    // Additional fields for walk-in form
    title: 'Mr.',
    firstName: 'John',
    lastName: 'Anderson',
    dateOfBirth: '1985-06-15',
    gender: 'Male',
    passportExpiry: '2029-12-31',
    country: 'United States',
    state: 'New York',
    city: 'New York',
    zipCode: '10001',
    emergencyContact: '+1 (555) 987-6543',
    company: 'TechCorp Inc.',
    businessEmail: 'john.anderson@techcorp.com',
    vipStatus: 'Gold',
    loyaltyNumber: 'LOY789456',
    arrivalTime: '02:00 PM',
    flightNumber: 'AA123',
    pickupRequired: true,
    pickupTime: '01:30 PM',
    pickupLocation: 'JFK Airport',
    remarks: 'Early check-in requested',
    // Room preferences
    bedPreference: 'King Bed',
    smokingPreference: 'Non-Smoking',
    floorPreference: 'High Floor',
    viewPreference: 'City View',
    // Payment details
    paymentMethod: 'Credit Card',
    cardType: 'Visa',
    cardLastFour: '1234',
    cardHolderName: 'John Anderson',
    cardExpiry: '12/2026',
    billingAddress: 'Same as above',
    // Special services
    extraBed: false,
    crib: false,
    wheelchairAccess: false,
    earlyCheckIn: true,
    lateCheckOut: false
  };

  // API Functions
  const handleCheckIn = async () => {
    setIsCheckingIn(true);
    try {
      // API: POST /api/checkins/confirm - Confirm check-in and update room status
      // API: POST /api/rooms/occupy - Mark room as occupied
      // API: PUT /api/bookings/{bookingId}/status - Update booking status to 'Checked-In'
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      // Navigate to dashboard with success message
      navigate('/dashboard', { 
        state: { 
          message: `Successfully checked in ${confirmation.guestName} to Room ${confirmation.room}`,
          type: 'success'
        }
      });
    } catch (error) {
      console.error('Check-in failed:', error);
      setIsCheckingIn(false);
    }
  };

  const handleEditDetails = () => {
    // Navigate to walk-in page with all the booking data
    navigate('/walk-in', { 
      state: { 
        bookingData: confirmation,
        mode: 'edit',
        source: 'checkin-confirmation'
      }
    });
  };

  const handleSendEmail = async () => {
    try {
      // API: POST /api/checkins/confirmation/send-email
      // Send confirmation email to guest
      alert('Confirmation email sent successfully!');
    } catch (error) {
      console.error('Email sending failed:', error);
    }
  };

  const handleDownloadConfirmation = () => {
    // Generate and download PDF confirmation
    const link = document.createElement('a');
    link.href = `/api/checkins/confirmation/${confirmation.bookingId}/pdf`;
    link.download = `CheckIn-Confirmation-${confirmation.bookingId}.pdf`;
    link.click();
  };

  const handlePrintConfirmation = () => {
    window.print();
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Check-In Confirmation</h1>
        <p className="text-gray-600 dark:text-gray-400">
          Review and confirm guest details before finalizing check-in for Booking ID: {confirmation.bookingId}
        </p>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Guest & Stay Details (2 columns on desktop) */}
        <div className="lg:col-span-2 space-y-6">
          {/* Guest Information Card */}
          <div className="card">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-gray-200 dark:border-gray-700">
              <h2 className="text-xl font-semibold flex items-center gap-2">
                <User size={20} />
                Guest Information
              </h2>
              <div className="flex items-center gap-2">
                <ShieldCheck size={18} className="text-green-500" />
                <span className="text-sm font-medium text-green-600">{confirmation.verificationStatus}</span>
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Full Name</p>
                <p className="text-lg font-semibold">{confirmation.guestName}</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Phone</p>
                <p className="font-medium">{confirmation.phone}</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Email</p>
                <p className="font-medium">{confirmation.email}</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Nationality</p>
                <p className="font-medium">{confirmation.nationality}</p>
              </div>
              <div className="md:col-span-2 space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Address</p>
                <p className="font-medium">{confirmation.address}</p>
              </div>
            </div>
          </div>

          {/* Stay Details Card */}
          <div className="card">
            <h2 className="text-xl font-semibold mb-4 pb-3 border-b border-gray-200 dark:border-gray-700 flex items-center gap-2">
              <Clock size={20} />
              Stay Details
            </h2>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Check-in / Check-out</p>
                <div className="space-y-1">
                  <p className="font-semibold">
                    {confirmation.checkIn} - {confirmation.checkOut}
                  </p>
                  <p className="text-sm text-gray-500">
                    {confirmation.checkInTime} → {confirmation.checkOutTime}
                  </p>
                </div>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Guests</p>
                <p className="font-semibold">{confirmation.adults} Adults, {confirmation.children} Children</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Purpose</p>
                <p className="font-semibold">{confirmation.purpose}</p>
              </div>
            </div>
          </div>

          {/* Room Details Card */}
          <div className="card">
            <h2 className="text-xl font-semibold mb-4 pb-3 border-b border-gray-200 dark:border-gray-700">
              Room Information
            </h2>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Room Number</p>
                <p className="text-3xl font-bold text-primary-600">{confirmation.room}</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Room Type</p>
                <p className="text-lg font-semibold">{confirmation.roomType}</p>
                <p className="text-sm text-gray-500">Floor {confirmation.floor}</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Rate Plan</p>
                <p className="font-semibold">{confirmation.ratePlan}</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Duration</p>
                <p className="font-semibold">{confirmation.nights} Nights</p>
              </div>
            </div>
          </div>

          {/* Guest Documents Card */}
          <div className="card">
            <h2 className="text-xl font-semibold mb-4 pb-3 border-b border-gray-200 dark:border-gray-700 flex items-center gap-2">
              <FileText size={20} />
              Guest Documents
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Document Metadata */}
              <div className="space-y-4">
                <div className="space-y-1">
                  <p className="text-sm text-gray-600 dark:text-gray-400">ID Type</p>
                  <p className="font-semibold">{confirmation.idType}</p>
                </div>
                <div className="space-y-1">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Document Number</p>
                  <p className="font-mono font-semibold text-lg">{confirmation.documentNumber}</p>
                </div>
                <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                  <p className="text-sm font-semibold mb-2">Verification Status</p>
                  <div className="flex items-center gap-2">
                    <CheckCircle size={16} className="text-green-500" />
                    <span className="text-sm text-gray-700 dark:text-gray-300">
                      Document verified and validated
                    </span>
                  </div>
                </div>
              </div>

              {/* Document Images */}
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <Camera size={16} className="text-gray-500" />
                    <p className="text-sm font-medium">ID Photo</p>
                  </div>
                  <div className="relative aspect-square overflow-hidden rounded-lg border border-gray-200 dark:border-gray-700">
                    <img 
                      src={confirmation.idPhoto} 
                      alt="ID Document" 
                      className="w-full h-full object-cover"
                    />
                  </div>
                </div>
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <User size={16} className="text-gray-500" />
                    <p className="text-sm font-medium">Guest Photo</p>
                  </div>
                  <div className="relative aspect-square overflow-hidden rounded-lg border border-gray-200 dark:border-gray-700">
                    <img 
                      src={confirmation.guestPhoto} 
                      alt="Guest" 
                      className="w-full h-full object-cover"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column - Billing & Meta (1 column on desktop) */}
        <div className="space-y-6">
          {/* Billing Summary Card */}
          <div className="card bg-gradient-to-br from-primary-50 to-blue-50 dark:from-primary-900/20 dark:to-blue-900/20 border-2 border-primary-200 dark:border-primary-800">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-primary-300 dark:border-primary-700">
              <h2 className="text-xl font-semibold flex items-center gap-2">
                <CreditCard size={20} />
                Billing Summary
              </h2>
              <div className="px-3 py-1 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center gap-2">
                <CheckCircle size={14} className="text-green-600" />
                <span className="text-sm font-medium text-green-700 dark:text-green-400">
                  {confirmation.paymentStatus}
                </span>
              </div>
            </div>
            
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-600 dark:text-gray-400">Room Charges ({confirmation.nights} nights)</span>
                <span className="font-semibold">${confirmation.roomCharges.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600 dark:text-gray-400">Taxes & Fees (18%)</span>
                <span className="font-semibold">${confirmation.taxesAndFees.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600 dark:text-gray-400">Discounts</span>
                <span className="font-semibold text-green-600">-${confirmation.discounts.toFixed(2)}</span>
              </div>
              
              <div className="pt-3 border-t border-primary-300 dark:border-primary-700 space-y-2">
                <div className="flex justify-between">
                  <span className="font-bold text-lg">Total Amount Due</span>
                  <span className="text-2xl font-bold text-primary-600">${confirmation.totalAmount.toFixed(2)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600 dark:text-gray-400">Advance Paid</span>
                  <span className="font-semibold text-green-600">${confirmation.advancePaid.toFixed(2)}</span>
                </div>
                <div className="flex justify-between pt-2 border-t border-primary-300 dark:border-primary-700">
                  <span className="font-bold">Balance Due</span>
                  <span className={`text-xl font-bold ${confirmation.balanceDue > 0 ? 'text-red-600' : 'text-green-600'}`}>
                    ${confirmation.balanceDue.toFixed(2)}
                  </span>
                </div>
              </div>
              
              <p className="text-xs text-gray-600 dark:text-gray-400 text-center mt-4 pt-3 border-t border-primary-300 dark:border-primary-700">
                {confirmation.balanceDue > 0 
                  ? 'Balance payable at checkout'
                  : 'All payments completed'}
              </p>
            </div>
          </div>

          {/* Discount & Reference Card */}
          <div className="card">
            <h3 className="font-semibold mb-3">Discount & Reference</h3>
            <div className="space-y-4">
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Source</p>
                <p className="font-medium">{confirmation.source}</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-gray-600 dark:text-gray-400">Discount Code</p>
                <div className="flex items-center justify-between">
                  <span className="font-mono font-semibold">{confirmation.discountCode}</span>
                  <span className="px-2 py-1 bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400 text-xs font-medium rounded">
                    Applied
                  </span>
                </div>
              </div>
              <div className="p-3 bg-gray-50 dark:bg-gray-800 rounded-lg">
                <p className="text-sm font-medium mb-1">Revenue Analytics</p>
                <p className="text-xs text-gray-600 dark:text-gray-400">
                  This booking contributes to {confirmation.source} channel performance metrics.
                </p>
              </div>
            </div>
          </div>

          {/* Important Information */}
          <div className="card">
            <h3 className="font-semibold mb-3">Important Information</h3>
            <ul className="space-y-2 text-sm text-gray-600 dark:text-gray-400">
              <li className="flex items-start gap-2">
                <span className="text-primary-500 mt-0.5">•</span>
                <span>Check-out time is 11:00 AM</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-primary-500 mt-0.5">•</span>
                <span>Late checkout subject to availability</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-primary-500 mt-0.5">•</span>
                <span>Please keep your room key card safe</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-primary-500 mt-0.5">•</span>
                <span>Room service available 24/7</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-primary-500 mt-0.5">•</span>
                <span>Breakfast timing: 7:00 AM - 10:30 AM</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-primary-500 mt-0.5">•</span>
                <span>Gym & Pool: 6:00 AM - 10:00 PM</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      {/* Bottom Action Bar */}
      <div className="sticky bottom-6 mt-8 pt-6 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 rounded-lg p-4 shadow-lg">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
            <CheckCircle size={16} className="text-green-500" />
            <span>All required documents verified and payment completed</span>
          </div>
          
          <div className="flex gap-3">
            <button
              onClick={handleEditDetails}
              className="btn-secondary flex items-center gap-2"
            >
              <Edit size={18} />
              Edit Details
            </button>
            
            <button
              onClick={handleCheckIn}
              disabled={isCheckingIn}
              className="btn-primary flex items-center gap-2 min-w-[140px] justify-center"
            >
              {isCheckingIn ? (
                <>
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                  Processing...
                </>
              ) : (
                <>
                  <CheckCircle size={18} />
                  Confirm Check-in
                </>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Quick Actions Bar (Hidden in print) */}
      <div className="print:hidden flex gap-3 mt-6">
        <button 
          onClick={handleDownloadConfirmation}
          className="btn-secondary flex items-center gap-2"
        >
          <Download size={18} />
          Download PDF
        </button>
        <button 
          onClick={handleSendEmail}
          className="btn-secondary flex items-center gap-2"
        >
          <Mail size={18} />
          Send Email
        </button>
        <button 
          onClick={handlePrintConfirmation}
          className="btn-secondary flex items-center gap-2"
        >
          <Printer size={18} />
          Print
        </button>
        <button 
          onClick={() => navigate('/dashboard')}
          className="btn-secondary flex items-center gap-2 ml-auto"
        >
          <Home size={18} />
          Dashboard
        </button>
      </div>
    </div>
  );
};

export default CheckInConfirmation;