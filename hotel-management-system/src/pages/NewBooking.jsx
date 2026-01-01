import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar, Search, Filter, Plus } from 'lucide-react';

const NewBooking = () => {
  const navigate = useNavigate();
  const [viewMode, setViewMode] = useState('calendar'); // 'calendar' or 'list'
  const [selectedDate, setSelectedDate] = useState(new Date());

  // API: GET /api/bookings/calendar - Get calendar view bookings
  // API: GET /api/bookings/availability - Check room availability
  // API: POST /api/bookings/quick - Create quick booking

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">New Booking</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Quick booking creation</p>
        </div>
        <button
          onClick={() => navigate('/booking-form')}
          className="btn-primary flex items-center gap-2"
        >
          <Plus size={20} />
          Detailed Booking Form
        </button>
      </div>

      {/* View Toggle */}
      <div className="card">
        <div className="flex gap-2">
          <button
            onClick={() => setViewMode('calendar')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              viewMode === 'calendar'
                ? 'bg-primary-600 text-white'
                : 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600'
            }`}
          >
            Calendar View
          </button>
          <button
            onClick={() => setViewMode('list')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              viewMode === 'list'
                ? 'bg-primary-600 text-white'
                : 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600'
            }`}
          >
            List View
          </button>
        </div>
      </div>

      {/* Content based on view mode */}
      <div className="card">
        <div className="text-center py-12">
          <Calendar className="mx-auto text-gray-400 mb-4" size={64} />
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
            {viewMode === 'calendar' ? 'Calendar View' : 'List View'}
          </h3>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            This view will show {viewMode === 'calendar' ? 'a calendar interface' : 'a list of bookings'} for quick booking management
          </p>
          <button onClick={() => navigate('/booking-form')} className="btn-primary">
            Create New Booking
          </button>
        </div>
      </div>
    </div>
  );
};

export default NewBooking;
