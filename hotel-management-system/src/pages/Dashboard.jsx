import { useNavigate } from 'react-router-dom';
import { 
  Users, 
  DoorOpen, 
  Calendar, 
  DollarSign, 
  TrendingUp, 
  TrendingDown,
  AlertCircle,
  CreditCard
} from 'lucide-react';

const Dashboard = () => {
  const navigate = useNavigate();

  // API: GET /api/dashboard/stats - Get dashboard statistics
  // API: GET /api/dashboard/arrivals/today - Get today's arrivals
  // API: GET /api/dashboard/departures/today - Get today's departures
  // API: GET /api/dashboard/pending-payments - Get pending payments
  // API: GET /api/dashboard/alerts - Get alerts and notifications

  const stats = [
    { 
      title: 'Total Occupancy', 
      value: '82%', 
      change: '+2%', 
      trending: 'up', 
      icon: DoorOpen,
      color: 'blue' 
    },
    { 
      title: 'Arrivals', 
      value: '25', 
      change: '-5%', 
      trending: 'down', 
      icon: Users,
      color: 'green' 
    },
    { 
      title: 'Departures', 
      value: '18', 
      change: '+1%', 
      trending: 'up', 
      icon: Users,
      color: 'orange' 
    },
    { 
      title: 'Exp. Revenue', 
      value: '$12,450', 
      change: 'Today', 
      icon: DollarSign,
      color: 'purple' 
    },
  ];

  const roomStatus = [
    { status: 'Available', count: 125, color: 'bg-green-500' },
    { status: 'Occupied', count: 68, color: 'bg-blue-500' },
    { status: 'Cleaning', count: 12, color: 'bg-yellow-500' },
    { status: 'Maintenance', count: 3, color: 'bg-red-500' },
  ];

  const pendingPayments = [
    { guest: 'David Lee', room: '302', amount: '$255.00', dueDate: 'Oct 28, 2023', status: 'Overdue by 1 day' },
    { guest: 'Maria Garcia', room: '110', amount: '$120.50', dueDate: 'Oct 29, 2023', status: 'Due Today' },
    { guest: 'James Smith', room: '501', amount: '$450.00', dueDate: 'Oct 30, 2023', status: 'Due in 1 day' },
  ];

  const quickActions = [
    { label: 'New Check-In', action: () => navigate('/checkin'), icon: Users, color: 'blue' },
    { label: 'Walk-In Guest', action: () => navigate('/walk-in'), icon: Users, color: 'green' },
    { label: 'New Booking', action: () => navigate('/booking-form'), icon: Calendar, color: 'purple' },
    { label: 'Check-Out', action: () => navigate('/checkout'), icon: DoorOpen, color: 'orange' },
    { label: 'View Rooms', action: () => navigate('/rooms'), icon: DoorOpen, color: 'pink' },
    { label: 'Invoices', action: () => navigate('/invoices'), icon: CreditCard, color: 'indigo' },
  ];

  const todayArrivals = [
    { guest: 'Alex Johnson', room: '201', status: 'Payment Due' },
    { guest: 'S. Williams', room: '305', status: 'Paid' },
    { guest: 'Michael Brown', room: '112', status: 'Checked In' },
  ];

  const todayDepartures = [
    { guest: 'Emily Carter', room: '401', status: 'Balanced' },
    { guest: 'D. Rodriguez', room: '215', status: 'Owed: $45' },
    { guest: 'Jessica Chen', room: '602', status: 'Checked Out' },
  ];

  const alerts = [
    {
      title: 'Overdue Payment: Room 410',
      message: 'Ms. Davis, Balance: $128.50. Folio has outstanding charges.',
      type: 'error'
    },
    {
      title: 'Credit Card Expiring: Room 205',
      message: "Mr. Chen's card on file expires this month. Confirm at check-out.",
      type: 'warning'
    },
    {
      title: 'Large group check-in pending',
      message: '"Innovate Corp" (15 rooms) arriving at 4:00 PM.',
      type: 'info'
    },
  ];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Reception Dashboard</h1>
        <p className="text-gray-600 dark:text-gray-400 mt-1">Welcome back! Here's what's happening today.</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat, index) => (
          <div key={index} className="card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">{stat.title}</p>
                <p className="text-2xl font-bold mt-1">{stat.value}</p>
                <div className="flex items-center gap-1 mt-2">
                  {stat.trending === 'up' ? (
                    <TrendingUp size={16} className="text-green-500" />
                  ) : stat.trending === 'down' ? (
                    <TrendingDown size={16} className="text-red-500" />
                  ) : null}
                  <span className={`text-sm ${
                    stat.trending === 'up' ? 'text-green-500' : 
                    stat.trending === 'down' ? 'text-red-500' : 
                    'text-gray-500'
                  }`}>
                    {stat.change}
                  </span>
                </div>
              </div>
              <div className={`p-3 rounded-full bg-${stat.color}-100 dark:bg-${stat.color}-900/20`}>
                <stat.icon className={`text-${stat.color}-600 dark:text-${stat.color}-400`} size={24} />
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Room Status & Pending Payments */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Room Status Overview */}
        <div className="card">
          <h2 className="text-xl font-semibold mb-4">Room Status Overview</h2>
          <div className="grid grid-cols-2 gap-4">
            {roomStatus.map((room, index) => (
              <div key={index} className="flex items-center gap-3 p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                <div className={`w-3 h-3 rounded-full ${room.color}`}></div>
                <div>
                  <p className="text-2xl font-bold">{room.count}</p>
                  <p className="text-sm text-gray-600 dark:text-gray-400">{room.status}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Pending Payments */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold">Pending Payments</h2>
            <span className="text-sm text-gray-600 dark:text-gray-400">$1,820 • 5 guests</span>
          </div>
          <div className="space-y-3">
            {pendingPayments.map((payment, index) => (
              <div key={index} className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                <div>
                  <p className="font-medium">{payment.guest}</p>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Room {payment.room}</p>
                </div>
                <div className="text-right">
                  <p className="font-semibold">{payment.amount}</p>
                  <p className="text-xs text-red-500">{payment.status}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card">
        <h2 className="text-xl font-semibold mb-4">Quick Actions</h2>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          {quickActions.map((action, index) => (
            <button
              key={index}
              onClick={action.action}
              className="flex flex-col items-center gap-2 p-4 rounded-lg bg-gray-50 dark:bg-gray-700/50 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            >
              <div className={`p-3 rounded-full bg-${action.color}-100 dark:bg-${action.color}-900/20`}>
                <action.icon className={`text-${action.color}-600 dark:text-${action.color}-400`} size={24} />
              </div>
              <span className="text-sm font-medium text-center">{action.label}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Alerts & Notifications */}
      <div className="card">
        <h2 className="text-xl font-semibold mb-4">Alerts & Notifications</h2>
        <div className="space-y-3">
          {alerts.map((alert, index) => (
            <div
              key={index}
              className={`flex gap-3 p-4 rounded-lg ${
                alert.type === 'error' ? 'bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800' :
                alert.type === 'warning' ? 'bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800' :
                'bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800'
              }`}
            >
              <AlertCircle 
                className={
                  alert.type === 'error' ? 'text-red-600' :
                  alert.type === 'warning' ? 'text-yellow-600' :
                  'text-blue-600'
                } 
                size={20} 
              />
              <div>
                <p className="font-semibold">{alert.title}</p>
                <p className="text-sm text-gray-600 dark:text-gray-400">{alert.message}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Today's Arrivals & Departures */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Arrivals */}
        <div className="card">
          <h2 className="text-xl font-semibold mb-4">Today's Arrivals (25)</h2>
          <div className="space-y-3">
            {todayArrivals.map((arrival, index) => (
              <div key={index} className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                <div>
                  <p className="font-medium">{arrival.guest}</p>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Room {arrival.room}</p>
                </div>
                <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                  arrival.status === 'Payment Due' ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400' :
                  arrival.status === 'Paid' ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400' :
                  'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
                }`}>
                  {arrival.status}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Departures */}
        <div className="card">
          <h2 className="text-xl font-semibold mb-4">Today's Departures (18)</h2>
          <div className="space-y-3">
            {todayDepartures.map((departure, index) => (
              <div key={index} className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                <div>
                  <p className="font-medium">{departure.guest}</p>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Room {departure.room}</p>
                </div>
                <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                  departure.status === 'Balanced' ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400' :
                  departure.status.includes('Owed') ? 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400' :
                  'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
                }`}>
                  {departure.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
