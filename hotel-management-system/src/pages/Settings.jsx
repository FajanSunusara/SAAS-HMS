import { useState } from 'react';
import { Save, User, Bell, Lock, Globe, Palette } from 'lucide-react';

const Settings = () => {
  const [activeTab, setActiveTab] = useState('profile');

  // API: GET /api/settings/user - Get user settings
  // API: PUT /api/settings/user - Update user settings
  // API: PUT /api/settings/password - Change password
  // API: PUT /api/settings/notifications - Update notification preferences

  const tabs = [
    { id: 'profile', name: 'Profile', icon: User },
    { id: 'notifications', name: 'Notifications', icon: Bell },
    { id: 'security', name: 'Security', icon: Lock },
    { id: 'preferences', name: 'Preferences', icon: Palette },
    { id: 'language', name: 'Language & Region', icon: Globe },
  ];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Settings</h1>
        <p className="text-gray-600 dark:text-gray-400 mt-1">Manage your account settings and preferences</p>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200 dark:border-gray-700">
        <div className="flex gap-4 overflow-x-auto">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-3 border-b-2 transition-colors whitespace-nowrap ${
                activeTab === tab.id
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
              }`}
            >
              <tab.icon size={18} />
              {tab.name}
            </button>
          ))}
        </div>
      </div>

      {/* Tab Content */}
      {activeTab === 'profile' && (
        <div className="card">
          <h2 className="text-xl font-semibold mb-4">Profile Settings</h2>
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-2">First Name</label>
                <input type="text" defaultValue="John" className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">Last Name</label>
                <input type="text" defaultValue="Doe" className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">Email</label>
                <input type="email" defaultValue="john.doe@hotel.com" className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">Phone</label>
                <input type="tel" defaultValue="+91 98765 43210" className="input-field" />
              </div>
            </div>
            <button className="btn-primary flex items-center gap-2">
              <Save size={18} />
              Save Changes
            </button>
          </div>
        </div>
      )}

      {activeTab === 'notifications' && (
        <div className="card">
          <h2 className="text-xl font-semibold mb-4">Notification Preferences</h2>
          <div className="space-y-4">
            {[
              'New booking notifications',
              'Check-in/Check-out alerts',
              'Payment received notifications',
              'System maintenance alerts',
              'Daily summary reports'
            ].map((item, index) => (
              <label key={index} className="flex items-center gap-3 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg cursor-pointer">
                <input type="checkbox" defaultChecked className="w-4 h-4" />
                <span>{item}</span>
              </label>
            ))}
            <button className="btn-primary flex items-center gap-2">
              <Save size={18} />
              Save Preferences
            </button>
          </div>
        </div>
      )}

      {activeTab === 'security' && (
        <div className="card">
          <h2 className="text-xl font-semibold mb-4">Security Settings</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">Current Password</label>
              <input type="password" className="input-field" />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">New Password</label>
              <input type="password" className="input-field" />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Confirm New Password</label>
              <input type="password" className="input-field" />
            </div>
            <button className="btn-primary flex items-center gap-2">
              <Save size={18} />
              Change Password
            </button>
          </div>
        </div>
      )}

      {activeTab === 'preferences' && (
        <div className="card">
          <h2 className="text-xl font-semibold mb-4">Application Preferences</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">Theme</label>
              <select className="input-field">
                <option>Light</option>
                <option>Dark</option>
                <option>System</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Date Format</label>
              <select className="input-field">
                <option>DD/MM/YYYY</option>
                <option>MM/DD/YYYY</option>
                <option>YYYY-MM-DD</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Time Format</label>
              <select className="input-field">
                <option>12 Hour</option>
                <option>24 Hour</option>
              </select>
            </div>
            <button className="btn-primary flex items-center gap-2">
              <Save size={18} />
              Save Preferences
            </button>
          </div>
        </div>
      )}

      {activeTab === 'language' && (
        <div className="card">
          <h2 className="text-xl font-semibold mb-4">Language & Region Settings</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">Language</label>
              <select className="input-field">
                <option>English</option>
                <option>Hindi</option>
                <option>Spanish</option>
                <option>French</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Timezone</label>
              <select className="input-field">
                <option>Asia/Kolkata (IST)</option>
                <option>America/New_York (EST)</option>
                <option>Europe/London (GMT)</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Currency</label>
              <select className="input-field">
                <option>INR (₹)</option>
                <option>USD ($)</option>
                <option>EUR (€)</option>
                <option>GBP (£)</option>
              </select>
            </div>
            <button className="btn-primary flex items-center gap-2">
              <Save size={18} />
              Save Settings
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Settings;
