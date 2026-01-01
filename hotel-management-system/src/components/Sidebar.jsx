import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  DoorOpen,
  Calendar,
  Users,
  LogIn,
  LogOut,
  FileText,
  CreditCard,
  BarChart3,
  Settings,
} from 'lucide-react';

const menuSections = [
  {
    title: 'Main',
    items: [
      { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
      { path: '/rooms', label: 'Rooms', icon: DoorOpen },
      { path: '/reservations', label: 'Reservations', icon: Calendar },
      { path: '/guests', label: 'Guests', icon: Users },
    ],
  },
  {
    title: 'Operations',
    items: [
      { path: '/checkin', label: 'Check In', icon: LogIn },
      { path: '/checkout', label: 'Check Out', icon: LogOut },
      { path: '/invoices', label: 'Invoices', icon: FileText },
      { path: '/payments', label: 'Payments', icon: CreditCard },
    ],
  },
  {
    title: 'Admin',
    items: [
      { path: '/reports', label: 'Reports', icon: BarChart3 },
      { path: '/settings', label: 'Settings', icon: Settings },
    ],
  },
];

const Sidebar = ({ isOpen, setIsOpen }) => {
  return (
    <>
      <aside
        className={`fixed top-16 left-0 z-40 h-[calc(100vh-4rem)]
        bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-800
        transition-all duration-300 ${isOpen ? 'w-60' : 'w-16'}`}
      >
        <div className="h-full flex flex-col justify-between p-2">

          {/* Menu */}
          <div className="space-y-4 overflow-y-auto">
            {menuSections.map(section => (
              <div key={section.title}>
                {isOpen && (
                  <p className="px-3 text-xs font-semibold text-gray-400 uppercase mb-2">
                    {section.title}
                  </p>
                )}

                {section.items.map(({ path, label, icon: Icon }) => (
                  <NavLink
                    key={path}
                    to={path}
                    className={({ isActive }) =>
                      `flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition
                      ${isActive
                        ? 'bg-blue-600 text-white'
                        : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800'}`
                    }
                  >
                    <Icon size={18} />
                    {isOpen && <span>{label}</span>}
                  </NavLink>
                ))}
              </div>
            ))}
          </div>

          {/* Quick Stats */}
          {isOpen && (
            <div className="mb-3 p-3 rounded-lg bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800">
              <p className="text-xs text-blue-700 dark:text-blue-300">
                🏨 12 Active Rooms
              </p>
              <p className="text-xs text-blue-700 dark:text-blue-300 mt-1">
                👤 5 Check-ins Today
              </p>
            </div>
          )}

          {/* Logout */}
          <button className="flex items-center gap-3 px-3 py-2 rounded-lg text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20">
            <LogOut size={18} />
            {isOpen && <span>Logout</span>}
          </button>
        </div>
      </aside>

      {/* Mobile Overlay */}
      {isOpen && (
        <div
          onClick={() => setIsOpen(false)}
          className="lg:hidden fixed inset-0 z-30 bg-black/40"
        />
      )}
    </>
  );
};

export default Sidebar;
