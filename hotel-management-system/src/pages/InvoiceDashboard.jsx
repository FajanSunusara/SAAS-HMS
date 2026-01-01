import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Search, Filter, Download, TrendingUp, TrendingDown, 
  Plus, Calendar, CreditCard, DollarSign, FileText,
  CheckCircle, XCircle, AlertTriangle, MoreVertical,
  ChevronDown, ChevronUp, Check, X, File
} from 'lucide-react';

const InvoiceDashboard = () => {
  const navigate = useNavigate();
  
  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [selectedInvoices, setSelectedInvoices] = useState([]);
  const [filtersExpanded, setFiltersExpanded] = useState(true);
  const [dateRange, setDateRange] = useState({ start: '', end: '' });
  const [paymentTypeFilter, setPaymentTypeFilter] = useState('all');
  const [showBulkBar, setShowBulkBar] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showMarkPaidModal, setShowMarkPaidModal] = useState(false);

  // Mock stats data
  const stats = [
    { 
      label: 'Total Revenue', 
      value: '$1,250,450', 
      subtext: 'Total Invoices: 789',
      change: '+2.5%', 
      trending: 'up',
      icon: <CreditCard className="text-blue-500" size={20} />
    },
    { 
      label: 'Amount Unpaid', 
      value: '$85,230', 
      change: '+8.1%', 
      trending: 'up',
      icon: <AlertTriangle className="text-yellow-500" size={20} />
    },
    { 
      label: 'Invoices Paid', 
      value: '692', 
      change: '-1.2%', 
      trending: 'down',
      icon: <CheckCircle className="text-green-500" size={20} />
    },
    { 
      label: 'Avg. Invoice Value', 
      value: '$1,807', 
      change: '+0.5%', 
      trending: 'up',
      icon: <FileText className="text-purple-500" size={20} />
    },
  ];

  // Mock invoices data with additional fields
  const invoices = [
    {
      id: 'INV-0764',
      customer: 'Liam Johnson',
      issueDate: '2024-05-15',
      dueDate: '2024-05-20',
      amount: 1250.00,
      formattedAmount: '$1,250.00',
      status: 'Paid',
      email: 'liam.johnson@email.com',
      paymentType: 'Credit Card',
      room: 'Deluxe Suite 201',
      stayDuration: '3 nights',
      selected: false
    },
    {
      id: 'INV-0763',
      customer: 'Olivia Smith',
      issueDate: '2024-05-14',
      dueDate: '2024-05-19',
      amount: 875.50,
      formattedAmount: '$875.50',
      status: 'Unpaid',
      email: 'olivia.smith@email.com',
      paymentType: 'Bank Transfer',
      room: 'Standard 105',
      stayDuration: '2 nights',
      selected: false
    },
    {
      id: 'INV-0762',
      customer: 'Noah Williams',
      issueDate: '2024-05-12',
      dueDate: '2024-05-17',
      amount: 2400.00,
      formattedAmount: '$2,400.00',
      status: 'Paid',
      email: 'noah.williams@email.com',
      paymentType: 'Credit Card',
      room: 'Executive Suite 301',
      stayDuration: '5 nights',
      selected: false
    },
    {
      id: 'INV-0761',
      customer: 'Emma Brown',
      issueDate: '2024-05-10',
      dueDate: '2024-05-15',
      amount: 320.00,
      formattedAmount: '$320.00',
      status: 'Overdue',
      email: 'emma.brown@email.com',
      paymentType: 'Cash',
      room: 'Standard 108',
      stayDuration: '1 night',
      selected: false
    },
    {
      id: 'INV-0760',
      customer: 'Ava Jones',
      issueDate: '2024-05-09',
      dueDate: '2024-05-14',
      amount: 5130.25,
      formattedAmount: '$5,130.25',
      status: 'Paid',
      email: 'ava.jones@email.com',
      paymentType: 'Credit Card',
      room: 'Penthouse 401',
      stayDuration: '7 nights',
      selected: false
    },
    {
      id: 'INV-0759',
      customer: 'William Garcia',
      issueDate: '2024-05-08',
      dueDate: '2024-05-13',
      amount: 1890.00,
      formattedAmount: '$1,890.00',
      status: 'Unpaid',
      email: 'william.garcia@email.com',
      paymentType: 'Bank Transfer',
      room: 'Deluxe Suite 205',
      stayDuration: '4 nights',
      selected: false
    },
    {
      id: 'INV-0758',
      customer: 'Sophia Martinez',
      issueDate: '2024-05-07',
      dueDate: '2024-05-12',
      amount: 745.50,
      formattedAmount: '$745.50',
      status: 'Paid',
      email: 'sophia.martinez@email.com',
      paymentType: 'Credit Card',
      room: 'Standard 102',
      stayDuration: '2 nights',
      selected: false
    },
    {
      id: 'INV-0757',
      customer: 'James Anderson',
      issueDate: '2024-05-06',
      dueDate: '2024-05-11',
      amount: 3250.00,
      formattedAmount: '$3,250.00',
      status: 'Overdue',
      email: 'james.anderson@email.com',
      paymentType: 'Cash',
      room: 'Executive Suite 305',
      stayDuration: '6 nights',
      selected: false
    },
  ];

  // Get status color
  const getStatusColor = (status) => {
    switch (status) {
      case 'Paid':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300 border border-green-200 dark:border-green-800';
      case 'Unpaid':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300 border border-yellow-200 dark:border-yellow-800';
      case 'Overdue':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300 border border-red-200 dark:border-red-800';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300 border border-gray-200 dark:border-gray-800';
    }
  };

  // Filter invoices
  const filteredInvoices = invoices.filter(invoice => {
    const matchesSearch = invoice.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          invoice.customer.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          invoice.email.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesStatus = statusFilter === 'all' || 
                         invoice.status.toLowerCase() === statusFilter;
    
    const matchesPaymentType = paymentTypeFilter === 'all' || 
                              invoice.paymentType.toLowerCase().replace(' ', '_') === paymentTypeFilter;
    
    const matchesDateRange = !dateRange.start || !dateRange.end || 
                           (invoice.issueDate >= dateRange.start && invoice.issueDate <= dateRange.end);
    
    return matchesSearch && matchesStatus && matchesPaymentType && matchesDateRange;
  });

  // Handle checkbox selection
  const handleSelectInvoice = (invoiceId) => {
    setSelectedInvoices(prev => {
      if (prev.includes(invoiceId)) {
        return prev.filter(id => id !== invoiceId);
      } else {
        return [...prev, invoiceId];
      }
    });
  };

  // Handle select all
  const handleSelectAll = () => {
    if (selectedInvoices.length === filteredInvoices.length) {
      setSelectedInvoices([]);
    } else {
      setSelectedInvoices(filteredInvoices.map(invoice => invoice.id));
    }
  };

  // Bulk actions
  const handleMarkAsPaid = () => {
    console.log('Marking as paid:', selectedInvoices);
    setShowMarkPaidModal(true);
  };

  const handleDeleteInvoices = () => {
    console.log('Deleting invoices:', selectedInvoices);
    setShowDeleteModal(true);
  };

  const handleExportSelected = () => {
    console.log('Exporting:', selectedInvoices);
    // Implement export logic
  };

  const clearSelection = () => {
    setSelectedInvoices([]);
  };

  // Update showBulkBar when selection changes
  useEffect(() => {
    setShowBulkBar(selectedInvoices.length > 0);
  }, [selectedInvoices]);

  return (
    <div className="space-y-6 pb-20">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Invoice Management</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">{invoices.length} Invoices</p>
        </div>
        <div className="flex gap-3">
          <button 
            onClick={handleExportSelected}
            disabled={selectedInvoices.length === 0}
            className={`btn-secondary flex items-center gap-2 ${selectedInvoices.length === 0 ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            <Download size={20} />
            Export Selected
          </button>
          <button 
            onClick={() => navigate('/invoice/create')}
            className="btn-primary flex items-center gap-2"
          >
            <Plus size={20} />
            Add Invoice
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat, index) => (
          <div key={index} className="card">
            <div className="flex items-start justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">{stat.label}</p>
                <p className="text-2xl font-bold mt-1">{stat.value}</p>
                {stat.subtext && (
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">{stat.subtext}</p>
                )}
              </div>
              <div className="p-2 bg-gray-100 dark:bg-gray-800 rounded-lg">
                {stat.icon}
              </div>
            </div>
            {stat.change && (
              <div className="flex items-center gap-1 mt-4">
                {stat.trending === 'up' ? (
                  <TrendingUp size={16} className="text-green-500" />
                ) : (
                  <TrendingDown size={16} className="text-red-500" />
                )}
                <span className={`text-sm font-medium ${stat.trending === 'up' ? 'text-green-500' : 'text-red-500'}`}>
                  {stat.change}
                </span>
                <span className="text-xs text-gray-500 dark:text-gray-400 ml-1">
                  vs last month
                </span>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Filters Panel */}
      <div className="card">
        <div 
          className="flex items-center justify-between cursor-pointer"
          onClick={() => setFiltersExpanded(!filtersExpanded)}
        >
          <div className="flex items-center gap-2">
            <Filter size={20} className="text-gray-500" />
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Filters</h3>
          </div>
          <button className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">
            {filtersExpanded ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
          </button>
        </div>
        
        {filtersExpanded && (
          <div className="mt-6 space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              {/* Date Range */}
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Date Range
                </label>
                <div className="flex gap-2">
                  <input
                    type="date"
                    value={dateRange.start}
                    onChange={(e) => setDateRange(prev => ({ ...prev, start: e.target.value }))}
                    className="input-field"
                  />
                  <input
                    type="date"
                    value={dateRange.end}
                    onChange={(e) => setDateRange(prev => ({ ...prev, end: e.target.value }))}
                    className="input-field"
                  />
                </div>
              </div>

              {/* Payment Type */}
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Payment Type
                </label>
                <select
                  value={paymentTypeFilter}
                  onChange={(e) => setPaymentTypeFilter(e.target.value)}
                  className="input-field"
                >
                  <option value="all">All</option>
                  <option value="credit_card">Credit Card</option>
                  <option value="bank_transfer">Bank Transfer</option>
                  <option value="cash">Cash</option>
                </select>
              </div>

              {/* Status Filter */}
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Status
                </label>
                <div className="flex gap-2">
                  {['all', 'paid', 'unpaid', 'overdue'].map((status) => (
                    <button
                      key={status}
                      onClick={() => setStatusFilter(status)}
                      className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                        statusFilter === status
                          ? status === 'all'
                            ? 'bg-primary-600 text-white'
                            : status === 'paid'
                            ? 'bg-green-600 text-white'
                            : status === 'unpaid'
                            ? 'bg-yellow-600 text-white'
                            : 'bg-red-600 text-white'
                          : 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600'
                      }`}
                    >
                      {status.charAt(0).toUpperCase() + status.slice(1)}
                    </button>
                  ))}
                </div>
              </div>

              {/* Search */}
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Search
                </label>
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                  <input
                    type="text"
                    placeholder="Invoice ID, customer, email..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="input-field pl-10"
                  />
                </div>
              </div>
            </div>

            {/* Apply Filters Button */}
            <div className="flex justify-end border-t dark:border-gray-700 pt-4">
              <button
                onClick={() => {
                  // Reset filters
                  setSearchQuery('');
                  setStatusFilter('all');
                  setDateRange({ start: '', end: '' });
                  setPaymentTypeFilter('all');
                }}
                className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white"
              >
                Clear All
              </button>
              <button
                onClick={() => console.log('Applying filters...')}
                className="ml-3 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium"
              >
                Apply Filters
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Invoices Table */}
      <div className="card">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-semibold">Invoice List</h2>
          <div className="text-sm text-gray-600 dark:text-gray-400">
            Showing {filteredInvoices.length} of {invoices.length} invoices
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 dark:bg-gray-700/50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-semibold w-12">
                  <input
                    type="checkbox"
                    checked={selectedInvoices.length === filteredInvoices.length && filteredInvoices.length > 0}
                    onChange={handleSelectAll}
                    className="rounded border-gray-300 dark:border-gray-600"
                  />
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Invoice ID</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Customer</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Issue Date</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Due Date</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Amount</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Status</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Payment Type</th>
                <th className="px-4 py-3 text-left text-sm font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
              {filteredInvoices.map((invoice) => (
                <tr 
                  key={invoice.id} 
                  className={`hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer ${
                    selectedInvoices.includes(invoice.id) ? 'bg-blue-50 dark:bg-blue-900/20' : ''
                  }`}
                >
                  <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
                    <input
                      type="checkbox"
                      checked={selectedInvoices.includes(invoice.id)}
                      onChange={() => handleSelectInvoice(invoice.id)}
                      className="rounded border-gray-300 dark:border-gray-600"
                    />
                  </td>
                  <td 
                    className="px-4 py-3 text-sm font-mono font-medium text-primary-600 dark:text-primary-400 hover:underline"
                    onClick={() => navigate(`/invoice/${invoice.id}`)}
                  >
                    {invoice.id}
                  </td>
                  <td className="px-4 py-3">
                    <div>
                      <p className="font-medium">{invoice.customer}</p>
                      <p className="text-xs text-gray-600 dark:text-gray-400">{invoice.email}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">{invoice.room} • {invoice.stayDuration}</p>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <div className="flex items-center gap-1">
                      <Calendar size={14} className="text-gray-400" />
                      {invoice.issueDate}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <div className="flex items-center gap-1">
                      <Calendar size={14} className="text-gray-400" />
                      {invoice.dueDate}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm font-semibold text-right">{invoice.formattedAmount}</td>
                  <td className="px-4 py-3">
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(invoice.status)}`}>
                      {invoice.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <div className="flex items-center gap-1">
                      {invoice.paymentType === 'Credit Card' && <CreditCard size={14} />}
                      {invoice.paymentType === 'Bank Transfer' && <FileText size={14} />}
                      {invoice.paymentType === 'Cash' && <DollarSign size={14} />}
                      {invoice.paymentType}
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2" onClick={(e) => e.stopPropagation()}>
                      <button
                        onClick={() => navigate(`/invoice/${invoice.id}`)}
                        className="text-blue-600 hover:text-blue-800 dark:text-blue-400 text-sm font-medium px-2 py-1 hover:bg-blue-50 dark:hover:bg-blue-900/30 rounded"
                      >
                        View
                      </button>
                      <button className="text-gray-600 hover:text-gray-800 dark:text-gray-400 text-sm font-medium px-2 py-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded">
                        <MoreVertical size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filteredInvoices.length === 0 && (
          <div className="text-center py-12">
            <div className="text-gray-400 mb-2">
              <Search size={48} className="mx-auto opacity-50" />
            </div>
            <p className="text-gray-500 dark:text-gray-400">No invoices found matching your criteria</p>
            <button
              onClick={() => {
                setSearchQuery('');
                setStatusFilter('all');
                setDateRange({ start: '', end: '' });
                setPaymentTypeFilter('all');
              }}
              className="mt-3 text-primary-600 hover:text-primary-800 dark:text-primary-400 text-sm font-medium"
            >
              Clear all filters
            </button>
          </div>
        )}
      </div>

      {/* Bulk Action Floating Bar */}
      {showBulkBar && (
        <div className="fixed bottom-6 left-1/2 transform -translate-x-1/2 bg-white dark:bg-gray-800 shadow-xl rounded-lg border border-gray-200 dark:border-gray-700 px-6 py-4 flex items-center justify-between min-w-[400px] z-50">
          <div className="flex items-center gap-4">
            <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
              {selectedInvoices.length} item{selectedInvoices.length !== 1 ? 's' : ''} selected
            </span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={handleMarkAsPaid}
              className="px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 flex items-center gap-2"
            >
              <CheckCircle size={16} />
              Mark as Paid
            </button>
            <button
              onClick={handleExportSelected}
              className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 flex items-center gap-2"
            >
              <Download size={16} />
              Export
            </button>
            <button
              onClick={handleDeleteInvoices}
              className="px-4 py-2 bg-red-600 text-white text-sm font-medium rounded-lg hover:bg-red-700 flex items-center gap-2"
            >
              <XCircle size={16} />
              Delete
            </button>
            <button
              onClick={clearSelection}
              className="ml-4 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            >
              <X size={20} />
            </button>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full">
            <div className="p-6">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                Delete Invoices
              </h3>
              <p className="text-gray-600 dark:text-gray-400 mb-6">
                Are you sure you want to delete {selectedInvoices.length} selected invoice{selectedInvoices.length !== 1 ? 's' : ''}? 
                This action cannot be undone.
              </p>
              <div className="flex justify-end gap-3">
                <button
                  onClick={() => setShowDeleteModal(false)}
                  className="px-4 py-2 text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium"
                >
                  Cancel
                </button>
                <button
                  onClick={() => {
                    console.log('Deleting invoices:', selectedInvoices);
                    setSelectedInvoices([]);
                    setShowDeleteModal(false);
                  }}
                  className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium"
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Mark as Paid Confirmation Modal */}
      {showMarkPaidModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full">
            <div className="p-6">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                Mark as Paid
              </h3>
              <p className="text-gray-600 dark:text-gray-400 mb-6">
                Mark {selectedInvoices.length} selected invoice{selectedInvoices.length !== 1 ? 's' : ''} as paid?
                This will update their status and trigger payment reconciliation.
              </p>
              <div className="flex justify-end gap-3">
                <button
                  onClick={() => setShowMarkPaidModal(false)}
                  className="px-4 py-2 text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium"
                >
                  Cancel
                </button>
                <button
                  onClick={() => {
                    console.log('Marking as paid:', selectedInvoices);
                    setSelectedInvoices([]);
                    setShowMarkPaidModal(false);
                  }}
                  className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium flex items-center gap-2"
                >
                  <Check size={16} />
                  Mark as Paid
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default InvoiceDashboard;