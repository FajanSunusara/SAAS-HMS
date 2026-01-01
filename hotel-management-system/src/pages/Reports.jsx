import { useState } from 'react';
import { BarChart3, Download, Calendar, Filter } from 'lucide-react';

const Reports = () => {
  const [reportType, setReportType] = useState('occupancy');
  const [dateRange, setDateRange] = useState('today');

  // API: GET /api/reports/occupancy - Get occupancy report
  // API: GET /api/reports/revenue - Get revenue report
  // API: GET /api/reports/bookings - Get bookings report
  // API: GET /api/reports/guests - Get guest report
  // API: POST /api/reports/export - Export report to PDF/Excel

  const reportTypes = [
    { id: 'occupancy', name: 'Occupancy Report', icon: BarChart3 },
    { id: 'revenue', name: 'Revenue Report', icon: BarChart3 },
    { id: 'bookings', name: 'Booking Report', icon: BarChart3 },
    { id: 'guests', name: 'Guest Report', icon: BarChart3 },
    { id: 'payments', name: 'Payment Report', icon: BarChart3 },
  ];

  const dateRanges = [
    { id: 'today', name: 'Today' },
    { id: 'yesterday', name: 'Yesterday' },
    { id: 'last7days', name: 'Last 7 Days' },
    { id: 'last30days', name: 'Last 30 Days' },
    { id: 'thismonth', name: 'This Month' },
    { id: 'lastmonth', name: 'Last Month' },
    { id: 'custom', name: 'Custom Range' },
  ];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Reports & Analytics</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Generate and view various hotel reports</p>
        </div>
        <button className="btn-primary flex items-center gap-2">
          <Download size={20} />
          Export Report
        </button>
      </div>

      {/* Report Type Selection */}
      <div className="card">
        <h2 className="text-xl font-semibold mb-4">Select Report Type</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4">
          {reportTypes.map((type) => (
            <button
              key={type.id}
              onClick={() => setReportType(type.id)}
              className={`p-4 rounded-lg border-2 transition-all ${
                reportType === type.id
                  ? 'border-primary-600 bg-primary-50 dark:bg-primary-900/20'
                  : 'border-gray-300 dark:border-gray-600 hover:border-primary-400'
              }`}
            >
              <type.icon className="mx-auto mb-2 text-primary-600" size={32} />
              <p className="text-sm font-medium text-center">{type.name}</p>
            </button>
          ))}
        </div>
      </div>

      {/* Date Range Selection */}
      <div className="card">
        <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
          <Calendar size={24} />
          Select Date Range
        </h2>
        <div className="flex flex-wrap gap-2">
          {dateRanges.map((range) => (
            <button
              key={range.id}
              onClick={() => setDateRange(range.id)}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                dateRange === range.id
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600'
              }`}
            >
              {range.name}
            </button>
          ))}
        </div>

        {dateRange === 'custom' && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
            <div>
              <label className="block text-sm font-medium mb-2">Start Date</label>
              <input type="date" className="input-field" />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">End Date</label>
              <input type="date" className="input-field" />
            </div>
          </div>
        )}
      </div>

      {/* Report Display */}
      <div className="card">
        <div className="text-center py-16">
          <BarChart3 className="mx-auto text-gray-400 mb-4" size={64} />
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
            {reportTypes.find(rt => rt.id === reportType)?.name}
          </h3>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            Report for {dateRanges.find(dr => dr.id === dateRange)?.name}
          </p>
          <p className="text-sm text-gray-500">
            This section will display the selected report with charts and data tables
          </p>
        </div>
      </div>
    </div>
  );
};

export default Reports;
