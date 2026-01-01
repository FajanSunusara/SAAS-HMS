// src/App.jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useState } from 'react';

// Layout
import Layout from './components/Layout';

// Pages
import Dashboard from './pages/Dashboard';
import Rooms from './pages/Rooms';
import ReservationBooking from './pages/ReservationBooking';
import GuestDetail from './pages/GuestDetail';
import CheckIn from './pages/CheckIn';
import CheckOut from './pages/CheckOut';
import InvoiceDashboard from './pages/InvoiceDashboard';
import InvoiceView from './pages/InvoiceView';
import WalkIn from './pages/WalkIn';
import BookingForm from './pages/BookingForm';
import CheckInConfirmation from './pages/CheckInConfirmation';
import NewBooking from './pages/NewBooking';
import Payment from './pages/Payment'; // Payment module (Dashboard + Form)
import Reports from './pages/Reports';
import Settings from './pages/Settings';
import GuestSearch from './pages/GuestSearch';
import DailySheet from './pages/DailySheet';

function App() {
  const [darkMode, setDarkMode] = useState(false);

  return (
    <div className={darkMode ? 'dark' : ''}>
      <BrowserRouter>
        <Routes>
          {/* Root layout */}
          <Route
            path="/"
            element={
              <Layout
                darkMode={darkMode}
                setDarkMode={setDarkMode}
              />
            }
          >
            {/* Default redirect */}
            <Route index element={<Navigate to="/dashboard" replace />} />

            {/* Main routes */}
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="rooms" element={<Rooms />} />
            <Route path="reservations" element={<ReservationBooking />} />
            <Route path="guests" element={<GuestSearch />} />
            <Route path="guest-detail/:guestId" element={<GuestDetail />} />

            {/* Check-in / Check-out */}
            <Route path="checkin" element={<CheckIn />} />
            <Route path="checkin-confirmation" element={<CheckInConfirmation />} />
            <Route path="checkout" element={<CheckOut />} />

            {/* Billing */}
            <Route path="invoices" element={<InvoiceDashboard />} />
            <Route path="invoice/:invoiceId" element={<InvoiceView />} />
            
            {/* Payment Module */}
            <Route path="payments" element={<Payment />} /> {/* Dashboard */}
            <Route path="payments/new" element={<Payment />} /> {/* Add Payment */}
            <Route path="payments/:paymentId" element={<Payment />} /> {/* View/Edit Payment */}
            <Route path="DailySheet" element={<DailySheet />} /> {/* Add Payment */}
            
            {/* Quick Payment from Invoice */}
            <Route path="invoice/:invoiceId/pay" element={<Payment />} />

            {/* Booking */}
            <Route path="walk-in" element={<WalkIn />} />
            <Route path="booking-form" element={<BookingForm />} />
            <Route path="new-booking" element={<NewBooking />} />

            {/* Reports & Settings */}
            <Route path="reports" element={<Reports />} />
            <Route path="settings" element={<Settings />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;