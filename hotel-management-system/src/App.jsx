// src/App.jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

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
import Payment from './pages/Payment';
import Reports from './pages/Reports';
import Settings from './pages/Settings';
import GuestSearch from './pages/GuestSearch';
import DailySheet from './pages/DailySheet';

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 300000, // 5 minutes
    },
  },
});

function App() {
  const [darkMode, setDarkMode] = useState(false);

  return (
    <QueryClientProvider client={queryClient}>
      <div className={darkMode ? 'dark' : ''}>
        <BrowserRouter>
          <Routes>
            <Route
              path="/"
              element={
                <Layout
                  darkMode={darkMode}
                  setDarkMode={setDarkMode}
                />
              }
            >
              <Route index element={<Navigate to="/dashboard" replace />} />

              <Route path="dashboard" element={<Dashboard />} />
              <Route path="rooms" element={<Rooms />} />
              <Route path="reservations" element={<ReservationBooking />} />
              <Route path="guests" element={<GuestSearch />} />
              <Route path="guest-detail/:guestId" element={<GuestDetail />} />

              <Route path="checkin" element={<CheckIn />} />
              <Route path="checkin-confirmation" element={<CheckInConfirmation />} />
              <Route path="checkout" element={<CheckOut />} />

              <Route path="invoices" element={<InvoiceDashboard />} />
              <Route path="invoice/:invoiceId" element={<InvoiceView />} />
              
              <Route path="payments" element={<Payment />} />
              <Route path="payments/new" element={<Payment />} />
              <Route path="payments/:paymentId" element={<Payment />} />
              <Route path="daily-sheet" element={<DailySheet />} />
              
              <Route path="invoice/:invoiceId/pay" element={<Payment />} />

              <Route path="walk-in" element={<WalkIn />} />
              <Route path="booking-form" element={<BookingForm />} />
              <Route path="new-booking" element={<NewBooking />} />

              <Route path="reports" element={<Reports />} />
              <Route path="settings" element={<Settings />} />
            </Route>

            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </BrowserRouter>
      </div>
    </QueryClientProvider>
  );
}

export default App;