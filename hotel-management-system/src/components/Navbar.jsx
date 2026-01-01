import { Menu, Bell, Moon, Sun, Search, User } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Navbar = ({ darkMode, setDarkMode, onToggleSidebar }) => {
  const [search, setSearch] = useState('');
  const [profileOpen, setProfileOpen] = useState(false);
  const navigate = useNavigate();

  return (
    <header className="fixed top-0 left-0 right-0 z-30 h-16 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800">
      <div className="h-full flex items-center justify-between px-4 gap-4">

        {/* Left */}
        <div className="flex items-center gap-3">
          <button
            onClick={onToggleSidebar}
            className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800"
          >
            <Menu size={20} />
          </button>
          <span className="hidden md:block font-semibold text-gray-800 dark:text-white">
            Hotel Management System
          </span>
        </div>

        {/* Search */}
        <div className="flex-1 max-w-xl hidden sm:block">
          <div className="relative">
            <Search
              size={18}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search bookings, rooms, guests..."
              className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-300 dark:border-gray-700
              bg-gray-50 dark:bg-gray-800 text-sm focus:ring-2 focus:ring-blue-500 outline-none"
            />
          </div>
        </div>

        {/* Right */}
        <div className="flex items-center gap-2">
          <button className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800">
            <Bell size={18} />
          </button>

          <button
            onClick={() => setDarkMode(!darkMode)}
            className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800"
          >
            {darkMode ? <Sun size={18} /> : <Moon size={18} />}
          </button>

          {/* Profile */}
          <div className="relative">
            <button
              onClick={() => setProfileOpen(v => !v)}
              className="flex items-center gap-2 p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              <div className="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center text-sm font-semibold">
                JD
              </div>
            </button>

            {profileOpen && (
              <div className="absolute right-0 mt-2 w-44 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg">
                <button
                  onClick={() => navigate('/profile')}
                  className="w-full px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  Profile
                </button>
                <button
                  onClick={() => navigate('/settings')}
                  className="w-full px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  Settings
                </button>
                <button className="w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20">
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>

      </div>
    </header>
  );
};

export default Navbar;
