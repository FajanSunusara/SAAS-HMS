import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Search, Filter, Download, TrendingUp, TrendingDown, 
  Plus, Calendar, CreditCard, DollarSign, FileText,
  CheckCircle, XCircle, AlertTriangle, MoreVertical,
  ChevronDown, ChevronUp, Check, X, File, Loader2
} from 'lucide-react';
import invoiceApi from '../api/invoiceApi';

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
  const [invoices, setInvoices] = useState([]);
  const [filteredInvoices, setFilteredInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [stats, setStats] = useState({
    totalRevenue: { value: '$0', change: '+0.0%', trending: 'up' },
    amountUnpaid: { value: '$0', change: '+0.0%', trending: 'up' },
    invoicesPaid: { value: '0', change: '+0.0%', trending: 'up' },
    avgInvoiceValue: { value: '$0', change: '+0.0%', trending: 'up' }
  });

  // Mock stats data structure (you can replace with real calculations)
  const statsConfig = [
    { 
      label: 'Total Revenue', 
      key: 'totalRevenue',
      icon: <CreditCard className="text-blue-500" size={20} />
    },
    { 
      label: 'Amount Unpaid', 
      key: 'amountUnpaid',
      icon: <AlertTriangle className="text-yellow-500" size={20} />
    },
    { 
      label: 'Invoices Paid', 
      key: 'invoicesPaid',
      icon: <CheckCircle className="text-green-500" size={20} />
    },
    { 
      label: 'Avg. Invoice Value', 
      key: 'avgInvoiceValue',
      icon: <FileText className="text-purple-500" size={20} />
    },
  ];

  // Get status color
  const getStatusColor = (status) => {
    switch (status) {
      case 'PAID':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300 border border-green-200 dark:border-green-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300 border border-yellow-200 dark:border-yellow-800';
      case 'OVERDUE':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300 border border-red-200 dark:border-red-800';
      case 'CANCELLED':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300 border border-gray-200 dark:border-gray-800';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300 border border-gray-200 dark:border-gray-800';
    }
  };

  // Format backend status to frontend display
  const formatStatus = (status) => {
    switch (status) {
      case 'PAID': return 'Paid';
      case 'PENDING': return 'Pending';
      case 'OVERDUE': return 'Overdue';
      case 'CANCELLED': return 'Cancelled';
      default: return status;
    }
  };

  // Format amount to currency
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount || 0);
  };

  // Fetch invoices from backend
  const fetchInvoices = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await invoiceApi.getAllInvoices(0, 100);
      
      if (response && response.data) {
        // Transform backend data to frontend format
        const transformedInvoices = response.data.content.map(invoice => ({
          id: invoice.invoiceNumber,
          invoiceId: invoice.invoiceId,
          customer: `${invoice.guest?.firstName || ''} ${invoice.guest?.lastName || ''}`.trim(),
          issueDate: invoice.issueDate,
          dueDate: invoice.dueDate,
          amount: invoice.totalAmount,
          formattedAmount: formatCurrency(invoice.totalAmount),
          status: invoice.status,
          formattedStatus: formatStatus(invoice.status),
          email: invoice.guest?.email || '',
          paymentType: invoice.payments?.[0]?.paymentMethod || 'Unknown',
          room: invoice.booking?.rooms?.[0]?.roomNumber || 'N/A',
          stayDuration: `${invoice.booking?.nights || 0} nights`,
          selected: false
        }));
        
        setInvoices(transformedInvoices);
        setFilteredInvoices(transformedInvoices);
        calculateStats(transformedInvoices);
      }
    } catch (err) {
      console.error('Error fetching invoices:', err);
      setError('Failed to load invoices. Please try again.');
      // Fallback to mock data for demo
      setInvoices(getMockInvoices());
      setFilteredInvoices(getMockInvoices());
      calculateStats(getMockInvoices());
    } finally {
      setLoading(false);
    }
  };

  // Calculate statistics from invoices
  const calculateStats = (invoiceList) => {
    const totalRevenue = invoiceList.reduce((sum, inv) => sum + (inv.amount || 0), 0);
    const paidInvoices = invoiceList.filter(inv => inv.status === 'PAID');
    const unpaidInvoices = invoiceList.filter(inv => inv.status === 'PENDING' || inv.status === 'OVERDUE');
    const amountUnpaid = unpaidInvoices.reduce((sum, inv) => sum + (inv.amount || 0), 0);
    const avgInvoiceValue = invoiceList.length > 0 ? totalRevenue / invoiceList.length : 0;

    setStats({
      totalRevenue: {
        value: formatCurrency(totalRevenue),
        change: '+2.5%',
        trending: 'up'
      },
      amountUnpaid: {
        value: formatCurrency(amountUnpaid),
        change: '+8.1%',
        trending: 'up'
      },
      invoicesPaid: {
        value: paidInvoices.length.toString(),
        change: '-1.2%',
        trending: 'down'
      },
      avgInvoiceValue: {
        value: formatCurrency(avgInvoiceValue),
        change: '+0.5%',
        trending: 'up'
      }
    });
  };

  // Filter invoices based on filters
  useEffect(() => {
    if (!invoices.length) return;

    const filtered = invoices.filter(invoice => {
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
    
    setFilteredInvoices(filtered);
  }, [searchQuery, statusFilter, paymentTypeFilter, dateRange, invoices]);

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
  const handleMarkAsPaid = async () => {
    try {
      // Get actual invoice IDs from selected invoice numbers
      const selectedInvoiceObjects = filteredInvoices.filter(inv => 
        selectedInvoices.includes(inv.id)
      );
      const invoiceIds = selectedInvoiceObjects.map(inv => inv.invoiceId);
      
      await invoiceApi.markInvoicesAsPaid(invoiceIds);
      
      // Refresh data
      fetchInvoices();
      setSelectedInvoices([]);
      setShowMarkPaidModal(false);
    } catch (error) {
      console.error('Error marking invoices as paid:', error);
      alert('Failed to mark invoices as paid. Please try again.');
    }
  };

  const handleDeleteInvoices = async () => {
    try {
      // Note: Your backend doesn't have delete endpoint yet
      // You'll need to implement DELETE /v1/invoices/{id} endpoint
      console.log('Would delete invoices:', selectedInvoices);
      setSelectedInvoices([]);
      setShowDeleteModal(false);
      alert('Delete functionality not implemented in backend yet');
    } catch (error) {
      console.error('Error deleting invoices:', error);
      alert('Failed to delete invoices. Please try again.');
    }
  };

  const handleExportSelected = () => {
    console.log('Exporting:', selectedInvoices);
    // Implement export logic
    alert('Export functionality to be implemented');
  };

  const clearSelection = () => {
    setSelectedInvoices([]);
  };

  // Apply filters
  const handleApplyFilters = async () => {
    try {
      setLoading(true);
      
      if (dateRange.start && dateRange.end) {
        // Fetch by date range if implemented
        // const response = await invoiceApi.getInvoicesByDateRange(dateRange.start, dateRange.end);
        // Process response
      } else if (statusFilter !== 'all') {
        // Fetch by status if implemented
        // const response = await invoiceApi.getInvoicesByStatus(statusFilter.toUpperCase());
        // Process response
      }
      
    } catch (error) {
      console.error('Error applying filters:', error);
    } finally {
      setLoading(false);
    }
  };

  // Clear all filters
  const handleClearFilters = () => {
    setSearchQuery('');
    setStatusFilter('all');
    setDateRange({ start: '', end: '' });
    setPaymentTypeFilter('all');
    fetchInvoices(); // Reload all invoices
  };

  // Update showBulkBar when selection changes
  useEffect(() => {
    setShowBulkBar(selectedInvoices.length > 0);
  }, [selectedInvoices]);

  // Load invoices on component mount
  useEffect(() => {
    fetchInvoices();
  }, []);

  // Mock data fallback
  const getMockInvoices = () => {
    return [
      {
        id: 'INV-0764',
        invoiceId: 1,
        customer: 'Liam Johnson',
        issueDate: '2024-05-15',
        dueDate: '2024-05-20',
        amount: 1250.00,
        formattedAmount: '$1,250.00',
        status: 'PAID',
        formattedStatus: 'Paid',
        email: 'liam.johnson@email.com',
        paymentType: 'Credit Card',
        room: 'Deluxe Suite 201',
        stayDuration: '3 nights',
        selected: false
      },
      // ... rest of mock data
    ];
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Loader2 className="w-12 h-12 animate-spin text-primary-600 mx-auto" />
          <p className="mt-4 text-gray-600 dark:text-gray-400">Loading invoices...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 pb-20">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Invoice Management</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            {error ? 'Using demo data' : `${invoices.length} Invoices`}
          </p>
          {error && (
            <p className="text-red-500 text-sm mt-1">{error}</p>
          )}
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
        {statsConfig.map((stat, index) => (
          <div key={index} className="card">
            <div className="flex items-start justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">{stat.label}</p>
                <p className="text-2xl font-bold mt-1">{stats[stat.key].value}</p>
              </div>
              <div className="p-2 bg-gray-100 dark:bg-gray-800 rounded-lg">
                {stat.icon}
              </div>
            </div>
            {stats[stat.key].change && (
              <div className="flex items-center gap-1 mt-4">
                {stats[stat.key].trending === 'up' ? (
                  <TrendingUp size={16} className="text-green-500" />
                ) : (
                  <TrendingDown size={16} className="text-red-500" />
                )}
                <span className={`text-sm font-medium ${stats[stat.key].trending === 'up' ? 'text-green-500' : 'text-red-500'}`}>
                  {stats[stat.key].change}
                </span>
                <span className="text-xs text-gray-500 dark:text-gray-400 ml-1">
                  vs last month
                </span>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Filters Panel - Same as before but with updated handlers */}
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
                <div className="flex gap-2 flex-wrap">
                  {['all', 'PAID', 'PENDING', 'OVERDUE'].map((status) => (
                    <button
                      key={status}
                      onClick={() => setStatusFilter(status.toLowerCase())}
                      className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                        statusFilter === status.toLowerCase()
                          ? status === 'all'
                            ? 'bg-primary-600 text-white'
                            : status === 'PAID'
                            ? 'bg-green-600 text-white'
                            : status === 'PENDING'
                            ? 'bg-yellow-600 text-white'
                            : 'bg-red-600 text-white'
                          : 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600'
                      }`}
                    >
                      {formatStatus(status)}
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
                onClick={handleClearFilters}
                className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white"
              >
                Clear All
              </button>
              <button
                onClick={handleApplyFilters}
                className="ml-3 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium"
              >
                Apply Filters
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Invoices Table - Updated to show real data */}
      <div className="card">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-semibold">Invoice List</h2>
          <div className="text-sm text-gray-600 dark:text-gray-400">
            Showing {filteredInvoices.length} of {invoices.length} invoices
          </div>
        </div>

        {loading ? (
          <div className="text-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-primary-600 mx-auto" />
            <p className="mt-2 text-gray-600 dark:text-gray-400">Loading invoices...</p>
          </div>
        ) : (
          <>
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
                          <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                            {invoice.room} • {invoice.stayDuration}
                          </p>
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
                      <td className="px-4 py-3 text-sm font-semibold text-right">
                        {invoice.formattedAmount}
                      </td>
                      <td className="px-4 py-3">
                        <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(invoice.status)}`}>
                          {invoice.formattedStatus}
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
  onClick={() => navigate(`/invoice/${invoice.invoiceId}`)}
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
                  onClick={handleClearFilters}
                  className="mt-3 text-primary-600 hover:text-primary-800 dark:text-primary-400 text-sm font-medium"
                >
                  Clear all filters
                </button>
              </div>
            )}
          </>
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
              onClick={() => setShowMarkPaidModal(true)}
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
              onClick={() => setShowDeleteModal(true)}
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
                  onClick={handleDeleteInvoices}
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
                  onClick={handleMarkAsPaid}
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