import React, { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft, Download, Mail, Printer, CreditCard,
  Edit, Save, X, Check, Copy, FileSignature,
  Building, User, Calendar, DollarSign, FileText,
  Trash2, Plus, Type, PenTool, Upload, Eye, EyeOff
} from 'lucide-react';

const InvoiceView = () => {
  const { invoiceId } = useParams();
  const navigate = useNavigate();
  const printRef = useRef(null);
  const canvasRef = useRef(null);
  
  const [editMode, setEditMode] = useState(false);
  const [showEmailModal, setShowEmailModal] = useState(false);
  const [signatureMode, setSignatureMode] = useState('digital'); // 'digital' or 'manual'
  const [isDrawing, setIsDrawing] = useState(false);
  const [lastX, setLastX] = useState(0);
  const [lastY, setLastY] = useState(0);
  
  const [invoice, setInvoice] = useState({
    id: invoiceId,
    invoiceNumber: 'INV-2024-00128',
    statementType: 'Corporate Client Statement',
    statementDate: '21 May 2024',
    periodCovered: '01 May 2024 - 20 May 2024',
    logoText: 'HotelIN',
    tagline: 'Official Corporate Statement for Services Rendered.',
    
    hotel: {
      name: 'The Grand Hotel Corporation',
      address: '123 Corporate Plaza, Metropolis, 10101',
      vatId: 'VAT123456789',
      registration: 'GRND123',
      phone: '+1 (555) 123-4567',
      email: 'accounts@grandhotel.com'
    },
    
    client: {
      name: 'Acme Corp. / John Doe',
      address: '456 Executive Boulevard, Gotham City, 20202',
      email: 'john.doe@email.com',
      phone: '+1 (555) 987-6543',
      company: 'Acme Corp.'
    },
    
    items: [
      {
        id: 1,
        description: 'Deluxe Room Accommodation (3 nights)',
        quantity: 3,
        rate: 250.00,
        amount: 750.00
      },
      {
        id: 2,
        description: 'Executive Dining Services',
        quantity: 1,
        rate: 120.50,
        amount: 120.50
      },
      {
        id: 3,
        description: 'Premium Spa & Wellness Package',
        quantity: 2,
        rate: 80.00,
        amount: 160.00
      },
      {
        id: 4,
        description: 'Executive Mini-bar Consumption',
        quantity: 1,
        rate: 45.00,
        amount: 45.00
      }
    ],
    
    summary: {
      subtotal: 1075.50,
      taxRate: 18,
      sgst: 96.80,
      cgst: 96.80,
      total: 1269.10,
      discount: 0,
      discountType: 'amount' // 'amount' or 'percentage'
    },
    
    payment: {
      status: 'PAID IN FULL',
      method: 'Corporate Credit Card (**** **** **** 4242)',
      transactionId: 'TXN_582B9E0A1C4F',
      date: '21 May 2024',
      badgeColor: 'bg-green-100 text-green-800'
    },
    
    signature: {
      signed: false,
      type: null, // 'digital' or 'manual'
      digital: {
        name: '',
        title: '',
        date: '',
        time: ''
      },
      manual: {
        image: null, // Base64 string for manual signature
        date: '',
        time: ''
      }
    },
    
    disclaimer: 'This is an official Corporate Client Statement. For any discrepancies, please contact our accounting department within 30 days of the statement date.',
    copyright: '© 2024 The Grand Hotel Corporation. All rights reserved.'
  });

  // Initialize canvas for manual signature
  useEffect(() => {
    if (canvasRef.current && editMode && signatureMode === 'manual') {
      const canvas = canvasRef.current;
      const ctx = canvas.getContext('2d');
      
      // Set canvas background
      ctx.fillStyle = 'white';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      
      // Set drawing style
      ctx.lineWidth = 2;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.strokeStyle = 'black';
      
      // Load existing signature if available
      if (invoice.signature.manual.image) {
        const img = new Image();
        img.onload = () => {
          ctx.drawImage(img, 0, 0);
        };
        img.src = invoice.signature.manual.image;
      }
    }
  }, [editMode, signatureMode, invoice.signature.manual.image]);

  // Calculate totals
  const calculateTotals = () => {
    const subtotal = invoice.items.reduce((sum, item) => sum + (item.quantity * item.rate), 0);
    const taxAmount = subtotal * (invoice.summary.taxRate / 100);
    const discount = invoice.summary.discountType === 'percentage' 
      ? subtotal * (invoice.summary.discount / 100)
      : invoice.summary.discount;
    const total = subtotal + taxAmount - discount;
    
    setInvoice(prev => ({
      ...prev,
      summary: {
        ...prev.summary,
        subtotal: parseFloat(subtotal.toFixed(2)),
        sgst: parseFloat((taxAmount / 2).toFixed(2)),
        cgst: parseFloat((taxAmount / 2).toFixed(2)),
        total: parseFloat(total.toFixed(2))
      }
    }));
  };

  // Handle field changes
  const handleFieldChange = (field, value) => {
    setInvoice(prev => ({ ...prev, [field]: value }));
  };

  const handleHotelFieldChange = (field, value) => {
    setInvoice(prev => ({
      ...prev,
      hotel: { ...prev.hotel, [field]: value }
    }));
  };

  const handleClientFieldChange = (field, value) => {
    setInvoice(prev => ({
      ...prev,
      client: { ...prev.client, [field]: value }
    }));
  };

  const handleItemChange = (index, field, value) => {
    const updatedItems = [...invoice.items];
    updatedItems[index] = { ...updatedItems[index], [field]: value };
    
    // Recalculate amount if quantity or rate changes
    if (field === 'quantity' || field === 'rate') {
      updatedItems[index].amount = updatedItems[index].quantity * updatedItems[index].rate;
    }
    
    setInvoice(prev => ({ ...prev, items: updatedItems }));
    calculateTotals();
  };

  const addNewItem = () => {
    const newItem = {
      id: Date.now(),
      description: 'New Item',
      quantity: 1,
      rate: 0,
      amount: 0
    };
    
    setInvoice(prev => ({ ...prev, items: [...prev.items, newItem] }));
    setTimeout(calculateTotals, 100);
  };

  const removeItem = (index) => {
    const updatedItems = invoice.items.filter((_, i) => i !== index);
    setInvoice(prev => ({ ...prev, items: updatedItems }));
    setTimeout(calculateTotals, 100);
  };

  const handleSummaryChange = (field, value) => {
    setInvoice(prev => ({
      ...prev,
      summary: { ...prev.summary, [field]: parseFloat(value) || 0 }
    }));
    setTimeout(calculateTotals, 100);
  };

  // Manual Signature Drawing Functions
  const startDrawing = (e) => {
    if (!editMode || signatureMode !== 'manual') return;
    
    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    setLastX(x);
    setLastY(y);
    setIsDrawing(true);
    
    const ctx = canvas.getContext('2d');
    ctx.beginPath();
    ctx.moveTo(x, y);
  };

  const draw = (e) => {
    if (!isDrawing) return;
    
    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    const ctx = canvas.getContext('2d');
    ctx.lineTo(x, y);
    ctx.stroke();
    
    setLastX(x);
    setLastY(y);
  };

  const stopDrawing = () => {
    setIsDrawing(false);
  };

  const clearManualSignature = () => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = 'white';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Reset stroke style
    ctx.lineWidth = 2;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.strokeStyle = 'black';
  };

  const saveManualSignature = () => {
    const canvas = canvasRef.current;
    const imageData = canvas.toDataURL('image/png');
    
    const now = new Date();
    const signatureData = {
      signed: true,
      type: 'manual',
      manual: {
        image: imageData,
        date: now.toISOString().split('T')[0],
        time: now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }
    };
    
    setInvoice(prev => ({
      ...prev,
      signature: signatureData
    }));
    
    alert('Manual signature saved successfully!');
  };

  // Digital Signature Functions
  const saveDigitalSignature = () => {
    if (!invoice.signature.digital.name || !invoice.signature.digital.title) {
      alert('Please enter name and title for digital signature');
      return;
    }
    
    const now = new Date();
    const signatureData = {
      signed: true,
      type: 'digital',
      digital: {
        ...invoice.signature.digital,
        date: now.toISOString().split('T')[0],
        time: now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }
    };
    
    setInvoice(prev => ({
      ...prev,
      signature: signatureData
    }));
    
    alert('Digital signature saved successfully!');
  };

  const handleDigitalSignatureChange = (field, value) => {
    setInvoice(prev => ({
      ...prev,
      signature: {
        ...prev.signature,
        digital: {
          ...prev.signature.digital,
          [field]: value
        }
      }
    }));
  };

  const clearSignature = () => {
    if (invoice.signature.type === 'manual') {
      clearManualSignature();
    }
    
    setInvoice(prev => ({
      ...prev,
      signature: {
        signed: false,
        type: null,
        digital: { name: '', title: '', date: '', time: '' },
        manual: { image: null, date: '', time: '' }
      }
    }));
  };

  // Action Functions
  const handleDownloadPDF = () => {
    alert('Downloading PDF...');
  };

  const handleSendEmail = () => {
    setShowEmailModal(true);
  };

  const handlePrint = () => {
    window.print();
  };

  const toggleEditMode = () => {
    if (editMode) {
      // Save changes
      alert('Invoice saved successfully!');
    }
    setEditMode(!editMode);
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    alert('Copied to clipboard!');
  };

  // Render editable field
  const EditableField = ({ value, onChange, type = 'text', className = '', placeholder = '' }) => {
    if (editMode) {
      return (
        <input
          type={type}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          className={`border border-blue-300 rounded px-2 py-1 focus:outline-none focus:ring-2 focus:ring-blue-500 ${className}`}
        />
      );
    }
    return <span className={className}>{value || placeholder}</span>;
  };

  const EditableTextarea = ({ value, onChange, className = '', placeholder = '' }) => {
    if (editMode) {
      return (
        <textarea
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          className={`border border-blue-300 rounded px-2 py-1 focus:outline-none focus:ring-2 focus:ring-blue-500 w-full ${className}`}
          rows="3"
        />
      );
    }
    return <p className={className}>{value || placeholder}</p>;
  };

  // Status Badge Component
  const StatusBadge = ({ status }) => {
    const statusConfig = {
      'PAID IN FULL': { color: 'bg-green-100 text-green-800', icon: '✓' },
      'PENDING': { color: 'bg-yellow-100 text-yellow-800', icon: '⏱' },
      'OVERDUE': { color: 'bg-red-100 text-red-800', icon: '⚠' },
      'DRAFT': { color: 'bg-gray-100 text-gray-800', icon: '📝' }
    };
    
    const config = statusConfig[status] || statusConfig.DRAFT;
    
    return (
      <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${config.color}`}>
        <span className="mr-1">{config.icon}</span>
        {status}
      </span>
    );
  };

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      {/* Email Modal */}
      {showEmailModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-md">
            <div className="p-6 border-b">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-semibold">Send Invoice</h3>
                <button
                  onClick={() => setShowEmailModal(false)}
                  className="text-gray-500 hover:text-gray-700"
                >
                  <X size={20} />
                </button>
              </div>
            </div>
            <div className="p-6">
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1">Recipient Email</label>
                <input
                  type="email"
                  defaultValue={invoice.client.email}
                  className="w-full border rounded-lg px-3 py-2"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1">Message</label>
                <textarea
                  className="w-full border rounded-lg px-3 py-2 h-32"
                  defaultValue={`Dear ${invoice.client.name},\n\nPlease find attached your invoice ${invoice.invoiceNumber}.\n\nTotal Amount: $${invoice.summary.total.toFixed(2)}\n\nBest regards,\n${invoice.hotel.name}`}
                />
              </div>
              <div className="flex justify-end gap-3">
                <button
                  onClick={() => setShowEmailModal(false)}
                  className="px-4 py-2 border rounded-lg hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  onClick={() => {
                    alert('Email sent!');
                    setShowEmailModal(false);
                  }}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  Send Email
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Back Button */}
      <button
        onClick={() => navigate('/invoices')}
        className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6"
      >
        <ArrowLeft size={20} />
        Back to Invoices
      </button>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Corporate Client Statement #
            <EditableField
              value={invoice.invoiceNumber}
              onChange={(value) => handleFieldChange('invoiceNumber', value)}
              className="ml-2 w-48"
              placeholder="Invoice Number"
            />
          </h1>
          <p className="text-gray-600">View and manage corporate invoice</p>
        </div>
        
        {/* Action Buttons */}
        <div className="flex flex-wrap gap-2">
          <button
            onClick={toggleEditMode}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg ${
              editMode
                ? 'bg-green-600 hover:bg-green-700 text-white'
                : 'bg-blue-600 hover:bg-blue-700 text-white'
            }`}
          >
            {editMode ? <Save size={18} /> : <Edit size={18} />}
            {editMode ? 'Save Changes' : 'Edit Statement'}
          </button>
          
          <button
            onClick={handlePrint}
            className="flex items-center gap-2 px-4 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded-lg"
          >
            <Printer size={18} />
            Print
          </button>
          
          <button
            onClick={handleSendEmail}
            className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg"
          >
            <Mail size={18} />
            Email
          </button>
          
          <button
            onClick={handleDownloadPDF}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg"
          >
            <Download size={18} />
            PDF
          </button>
        </div>
      </div>

      {/* Main Invoice Container */}
      <div ref={printRef} className="bg-white rounded-xl shadow-lg border border-gray-200 p-8">
        {/* Hotel Logo & Statement Header */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 pb-8 border-b">
          {/* Left: Hotel Logo */}
          <div className="mb-6 md:mb-0">
            <div className="flex items-center gap-3 mb-2">
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <Building className="text-blue-600" size={24} />
              </div>
              <div>
                <h2 className="text-3xl font-bold text-gray-900">
                  <EditableField
                    value={invoice.logoText}
                    onChange={(value) => handleFieldChange('logoText', value)}
                    className="text-3xl font-bold"
                    placeholder="Company Name"
                  />
                </h2>
                <p className="text-sm text-gray-600 mt-1">
                  <EditableField
                    value={invoice.tagline}
                    onChange={(value) => handleFieldChange('tagline', value)}
                    className="w-64"
                    placeholder="Company tagline"
                  />
                </p>
              </div>
            </div>
          </div>
          
          {/* Right: Statement Info */}
          <div className="text-right">
            <h3 className="text-2xl font-bold text-gray-900 mb-2">
              <EditableField
                value={invoice.statementType}
                onChange={(value) => handleFieldChange('statementType', value)}
                className="text-2xl font-bold"
                placeholder="Statement Type"
              />
            </h3>
            <div className="space-y-1 text-sm">
              <p>
                <span className="font-semibold">Statement Date:</span>{' '}
                <EditableField
                  value={invoice.statementDate}
                  onChange={(value) => handleFieldChange('statementDate', value)}
                  className="w-32"
                  placeholder="Date"
                />
              </p>
              <p>
                <span className="font-semibold">Period Covered:</span>{' '}
                <EditableField
                  value={invoice.periodCovered}
                  onChange={(value) => handleFieldChange('periodCovered', value)}
                  className="w-48"
                  placeholder="Period"
                />
              </p>
              <p>
                <span className="font-semibold">Account Reference:</span>{' '}
                <span className="font-mono font-bold">{invoice.invoiceNumber}</span>
              </p>
            </div>
          </div>
        </div>

        {/* Billed From / Billed To */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
          {/* Billed From */}
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <Building size={18} className="text-gray-400" />
              <h4 className="text-sm font-semibold text-gray-700 uppercase tracking-wide">
                BILLED FROM
              </h4>
            </div>
            <div className="space-y-1">
              <p className="font-semibold text-lg text-gray-900">
                <EditableField
                  value={invoice.hotel.name}
                  onChange={(value) => handleHotelFieldChange('name', value)}
                  className="font-semibold text-lg w-full"
                  placeholder="Company Name"
                />
              </p>
              <p className="text-gray-600">
                <EditableField
                  value={invoice.hotel.address}
                  onChange={(value) => handleHotelFieldChange('address', value)}
                  className="w-full"
                  placeholder="Address"
                />
              </p>
              <p className="text-sm text-gray-600">
                <span className="font-medium">VAT ID:</span>{' '}
                <EditableField
                  value={invoice.hotel.vatId}
                  onChange={(value) => handleHotelFieldChange('vatId', value)}
                  className="w-32"
                  placeholder="VAT ID"
                />{' '}
                |{' '}
                <span className="font-medium">Company Reg:</span>{' '}
                <EditableField
                  value={invoice.hotel.registration}
                  onChange={(value) => handleHotelFieldChange('registration', value)}
                  className="w-32"
                  placeholder="Registration"
                />
              </p>
              <p className="text-sm text-gray-600">
                <span className="font-medium">Phone:</span>{' '}
                <EditableField
                  value={invoice.hotel.phone}
                  onChange={(value) => handleHotelFieldChange('phone', value)}
                  className="w-48"
                  placeholder="Phone"
                />
              </p>
              <p className="text-sm text-gray-600">
                <span className="font-medium">Email:</span>{' '}
                <EditableField
                  value={invoice.hotel.email}
                  onChange={(value) => handleHotelFieldChange('email', value)}
                  className="w-64"
                  type="email"
                  placeholder="Email"
                />
              </p>
            </div>
          </div>
          
          {/* Billed To */}
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <User size={18} className="text-gray-400" />
              <h4 className="text-sm font-semibold text-gray-700 uppercase tracking-wide">
                BILLED TO
              </h4>
            </div>
            <div className="space-y-1">
              <p className="font-semibold text-lg text-gray-900">
                <EditableField
                  value={invoice.client.name}
                  onChange={(value) => handleClientFieldChange('name', value)}
                  className="font-semibold text-lg w-full"
                  placeholder="Client Name"
                />
              </p>
              <p className="text-sm text-gray-600">
                <EditableField
                  value={invoice.client.company}
                  onChange={(value) => handleClientFieldChange('company', value)}
                  className="w-full"
                  placeholder="Company"
                />
              </p>
              <p className="text-gray-600">
                <EditableField
                  value={invoice.client.address}
                  onChange={(value) => handleClientFieldChange('address', value)}
                  className="w-full"
                  placeholder="Address"
                />
              </p>
              <p className="text-sm text-gray-600">
                <EditableField
                  value={invoice.client.email}
                  onChange={(value) => handleClientFieldChange('email', value)}
                  className="w-64"
                  type="email"
                  placeholder="Email"
                />{' '}
                | <span className="font-medium">Tel:</span>{' '}
                <EditableField
                  value={invoice.client.phone}
                  onChange={(value) => handleClientFieldChange('phone', value)}
                  className="w-32"
                  placeholder="Phone"
                />
              </p>
            </div>
          </div>
        </div>

        {/* Divider */}
        <div className="border-t border-gray-300 my-8"></div>

        {/* Items Table */}
        <div className="mb-8">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold">Invoice Items</h3>
            {editMode && (
              <button
                onClick={addNewItem}
                className="flex items-center gap-2 px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                <Plus size={16} />
                Add Item
              </button>
            )}
          </div>
          <table className="w-full">
            <thead className="border-b border-gray-300">
              <tr>
                <th className="text-left py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">
                  SERVICE / ITEM DESCRIPTION
                </th>
                <th className="text-center py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">
                  QTY
                </th>
                <th className="text-right py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">
                  RATE
                </th>
                <th className="text-right py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">
                  AMOUNT
                </th>
                {editMode && (
                  <th className="text-center py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">
                    ACTIONS
                  </th>
                )}
              </tr>
            </thead>
            <tbody>
              {invoice.items.map((item, index) => (
                <tr key={item.id} className="border-b border-gray-200">
                  <td className="py-4">
                    <EditableField
                      value={item.description}
                      onChange={(value) => handleItemChange(index, 'description', value)}
                      className="w-full"
                      placeholder="Item description"
                    />
                  </td>
                  <td className="text-center py-4">
                    <EditableField
                      value={item.quantity}
                      onChange={(value) => handleItemChange(index, 'quantity', parseFloat(value) || 0)}
                      type="number"
                      className="w-20 text-center"
                      placeholder="Qty"
                    />
                  </td>
                  <td className="text-right py-4">
                    <div className="flex items-center justify-end gap-1">
                      <span className="text-gray-600">$</span>
                      <EditableField
                        value={item.rate}
                        onChange={(value) => handleItemChange(index, 'rate', parseFloat(value) || 0)}
                        type="number"
                        className="w-32 text-right"
                        placeholder="0.00"
                      />
                    </div>
                  </td>
                  <td className="text-right py-4">
                    <span className="font-medium text-gray-900">
                      ${item.amount.toFixed(2)}
                    </span>
                  </td>
                  {editMode && (
                    <td className="text-center py-4">
                      <button
                        onClick={() => removeItem(index)}
                        className="text-red-600 hover:text-red-800 p-1"
                        title="Remove item"
                      >
                        <Trash2 size={16} />
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Totals Section */}
        <div className="flex justify-end mb-12">
          <div className="w-full md:w-2/3 lg:w-1/2">
            <div className="space-y-3">
              {/* Subtotal */}
              <div className="flex justify-between items-center py-2 border-b border-gray-200">
                <span className="text-gray-600">Subtotal</span>
                <span className="font-medium text-gray-900">${invoice.summary.subtotal.toFixed(2)}</span>
              </div>
              
              {/* Tax Rate */}
              {editMode && (
                <div className="flex justify-between items-center py-2 border-b border-gray-200">
                  <span className="text-gray-600">
                    Tax Rate:
                    <EditableField
                      value={invoice.summary.taxRate}
                      onChange={(value) => handleSummaryChange('taxRate', value)}
                      type="number"
                      className="w-20 ml-2"
                      placeholder="18"
                    />%
                  </span>
                  <span className="font-medium text-gray-900">
                    ${(invoice.summary.sgst + invoice.summary.cgst).toFixed(2)}
                  </span>
                </div>
              )}
              
              {/* SGST */}
              <div className="flex justify-between items-center py-2 border-b border-gray-200">
                <span className="text-gray-600">SGST ({invoice.summary.taxRate / 2}%)</span>
                <span className="font-medium text-gray-900">${invoice.summary.sgst.toFixed(2)}</span>
              </div>
              
              {/* CGST */}
              <div className="flex justify-between items-center py-2 border-b border-gray-200">
                <span className="text-gray-600">CGST ({invoice.summary.taxRate / 2}%)</span>
                <span className="font-medium text-gray-900">${invoice.summary.cgst.toFixed(2)}</span>
              </div>
              
              {/* Discount */}
              <div className="flex justify-between items-center py-2 border-b border-gray-200">
                <span className="text-gray-600">
                  Discount
                  {editMode && (
                    <>
                      {' '}
                      <select
                        value={invoice.summary.discountType}
                        onChange={(e) => setInvoice(prev => ({
                          ...prev,
                          summary: { ...prev.summary, discountType: e.target.value }
                        }))}
                        className="ml-2 border rounded px-2 py-1"
                      >
                        <option value="amount">Amount</option>
                        <option value="percentage">Percentage</option>
                      </select>
                      <EditableField
                        value={invoice.summary.discount}
                        onChange={(value) => handleSummaryChange('discount', value)}
                        type="number"
                        className="w-20 ml-2"
                        placeholder="0"
                      />
                      {invoice.summary.discountType === 'percentage' && '%'}
                    </>
                  )}
                </span>
                <span className="font-medium text-green-600">
                  -${invoice.summary.discount.toFixed(2)}
                </span>
              </div>
              
              {/* Total Due - Highlighted */}
              <div className="flex justify-between items-center py-4 border-t-2 border-gray-900 mt-2">
                <span className="text-lg font-bold text-gray-900">TOTAL DUE</span>
                <span className="text-2xl font-bold text-gray-900">
                  ${invoice.summary.total.toFixed(2)}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Payment Status & Details */}
        <div className="mb-12">
          <h4 className="text-lg font-semibold text-gray-900 mb-4">
            PAYMENT STATUS & DETAILS
          </h4>
          
          <div className="bg-green-50 border border-green-200 rounded-lg p-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                  <Check className="text-green-600" size={20} />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-gray-900">Payment Status:</span>
                    {editMode ? (
                      <select
                        value={invoice.payment.status}
                        onChange={(e) => setInvoice(prev => ({
                          ...prev,
                          payment: { ...prev.payment, status: e.target.value }
                        }))}
                        className="border rounded px-3 py-1"
                      >
                        <option value="PAID IN FULL">PAID IN FULL</option>
                        <option value="PENDING">PENDING</option>
                        <option value="OVERDUE">OVERDUE</option>
                        <option value="DRAFT">DRAFT</option>
                      </select>
                    ) : (
                      <StatusBadge status={invoice.payment.status} />
                    )}
                  </div>
                  <p className="text-sm text-gray-600 mt-1">Fully paid and processed</p>
                </div>
              </div>
              
              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="font-medium text-gray-700">Method:</span>
                  {editMode ? (
                    <EditableField
                      value={invoice.payment.method}
                      onChange={(value) => setInvoice(prev => ({
                        ...prev,
                        payment: { ...prev.payment, method: value }
                      }))}
                      className="w-64"
                      placeholder="Payment method"
                    />
                  ) : (
                    <span className="text-gray-900">{invoice.payment.method}</span>
                  )}
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="font-medium text-gray-700">Transaction ID:</span>
                  <div className="flex items-center gap-2">
                    {editMode ? (
                      <EditableField
                        value={invoice.payment.transactionId}
                        onChange={(value) => setInvoice(prev => ({
                          ...prev,
                          payment: { ...prev.payment, transactionId: value }
                        }))}
                        className="font-mono w-48"
                        placeholder="Transaction ID"
                      />
                    ) : (
                      <span className="font-mono text-gray-900">{invoice.payment.transactionId}</span>
                    )}
                    <button
                      onClick={() => copyToClipboard(invoice.payment.transactionId)}
                      className="text-gray-500 hover:text-gray-700"
                    >
                      <Copy size={14} />
                    </button>
                  </div>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="font-medium text-gray-700">Payment Date:</span>
                  {editMode ? (
                    <EditableField
                      value={invoice.payment.date}
                      onChange={(value) => setInvoice(prev => ({
                        ...prev,
                        payment: { ...prev.payment, date: value }
                      }))}
                      className="w-32"
                      placeholder="Date"
                    />
                  ) : (
                    <span className="text-gray-900">{invoice.payment.date}</span>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Digital Signature */}
        <div className="mb-12">
          <h4 className="text-lg font-semibold text-gray-900 mb-4">
            OFFICIAL DIGITAL SIGNATURE
          </h4>
          
          {invoice.signature.signed ? (
            <div className="border-2 border-green-300 bg-green-50 rounded-lg p-6">
              <div className="flex items-center gap-2 text-green-600 mb-4">
                <Check size={20} />
                <span className="font-medium">Digitally Signed by Authorized Representative</span>
              </div>
              
              {invoice.signature.type === 'digital' ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-600">Name</p>
                    <p className="font-semibold text-gray-900">{invoice.signature.digital.name}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Title</p>
                    <p className="font-semibold text-gray-900">{invoice.signature.digital.title}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Date</p>
                    <p className="font-semibold text-gray-900">{invoice.signature.digital.date}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Time</p>
                    <p className="font-semibold text-gray-900">{invoice.signature.digital.time}</p>
                  </div>
                </div>
              ) : (
                <div className="text-center">
                  <div className="mb-4">
                    <img 
                      src={invoice.signature.manual.image} 
                      alt="Manual Signature" 
                      className="max-w-xs mx-auto border border-gray-300 rounded"
                    />
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm text-gray-600">Signed on</p>
                      <p className="font-semibold text-gray-900">{invoice.signature.manual.date}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">at</p>
                      <p className="font-semibold text-gray-900">{invoice.signature.manual.time}</p>
                    </div>
                  </div>
                </div>
              )}
              
              <div className="flex gap-3 mt-6">
                <button
                  onClick={clearSignature}
                  className="px-4 py-2 border border-red-300 text-red-600 rounded-lg hover:bg-red-50"
                >
                  Clear Signature
                </button>
              </div>
            </div>
          ) : (
            <div className={`border-2 ${editMode ? 'border-blue-300 bg-blue-50' : 'border-dashed border-gray-300 bg-gray-50'} rounded-lg p-6`}>
              {editMode ? (
                <div className="space-y-6">
                  {/* Signature Type Selection */}
                  <div className="flex gap-4 mb-4">
                    <button
                      onClick={() => setSignatureMode('digital')}
                      className={`flex-1 flex flex-col items-center justify-center p-4 border-2 rounded-lg ${
                        signatureMode === 'digital' 
                          ? 'border-blue-500 bg-blue-50' 
                          : 'border-gray-300 hover:border-gray-400'
                      }`}
                    >
                      <Type size={24} className="mb-2 text-gray-600" />
                      <span className="font-medium">Digital Signature</span>
                      <span className="text-sm text-gray-500">Type name and title</span>
                    </button>
                    <button
                      onClick={() => setSignatureMode('manual')}
                      className={`flex-1 flex flex-col items-center justify-center p-4 border-2 rounded-lg ${
                        signatureMode === 'manual' 
                          ? 'border-blue-500 bg-blue-50' 
                          : 'border-gray-300 hover:border-gray-400'
                      }`}
                    >
                      <PenTool size={24} className="mb-2 text-gray-600" />
                      <span className="font-medium">Manual Signature</span>
                      <span className="text-sm text-gray-500">Draw your signature</span>
                    </button>
                  </div>

                  {/* Digital Signature Form */}
                  {signatureMode === 'digital' ? (
                    <div className="space-y-4">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                          <label className="block text-sm font-medium mb-1">Name *</label>
                          <input
                            type="text"
                            value={invoice.signature.digital.name}
                            onChange={(e) => handleDigitalSignatureChange('name', e.target.value)}
                            className="w-full border rounded px-3 py-2"
                            placeholder="Signatory Name"
                          />
                        </div>
                        <div>
                          <label className="block text-sm font-medium mb-1">Title *</label>
                          <input
                            type="text"
                            value={invoice.signature.digital.title}
                            onChange={(e) => handleDigitalSignatureChange('title', e.target.value)}
                            className="w-full border rounded px-3 py-2"
                            placeholder="e.g., Accounts Manager"
                          />
                        </div>
                      </div>
                      <div className="flex justify-end">
                        <button
                          onClick={saveDigitalSignature}
                          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center gap-2"
                          disabled={!invoice.signature.digital.name || !invoice.signature.digital.title}
                        >
                          <FileSignature size={18} />
                          Sign Document
                        </button>
                      </div>
                    </div>
                  ) : (
                    /* Manual Signature Canvas */
                    <div className="space-y-4">
                      <div className="border border-gray-300 rounded-lg p-4 bg-white">
                        <div className="flex justify-between items-center mb-2">
                          <label className="text-sm font-medium">Draw your signature:</label>
                          <button
                            onClick={clearManualSignature}
                            className="text-sm text-red-600 hover:text-red-800"
                          >
                            Clear Canvas
                          </button>
                        </div>
                        <div className="border-2 border-dashed border-gray-300 rounded">
                          <canvas
                            ref={canvasRef}
                            width={600}
                            height={200}
                            className="w-full h-50 cursor-crosshair"
                            onMouseDown={startDrawing}
                            onMouseMove={draw}
                            onMouseUp={stopDrawing}
                            onMouseLeave={stopDrawing}
                            onTouchStart={(e) => {
                              e.preventDefault();
                              startDrawing(e.touches[0]);
                            }}
                            onTouchMove={(e) => {
                              e.preventDefault();
                              draw(e.touches[0]);
                            }}
                            onTouchEnd={stopDrawing}
                          />
                        </div>
                        <p className="text-xs text-gray-500 mt-2">
                          Click and drag to draw your signature
                        </p>
                      </div>
                      <div className="flex justify-end gap-3">
                        <button
                          onClick={clearManualSignature}
                          className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                        >
                          Clear
                        </button>
                        <button
                          onClick={saveManualSignature}
                          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center gap-2"
                        >
                          <FileSignature size={18} />
                          Save Signature
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-8">
                  <div className="w-16 h-16 border-2 border-dashed border-gray-400 rounded-full mx-auto mb-4 flex items-center justify-center">
                    <FileSignature size={24} className="text-gray-400" />
                  </div>
                  <p className="text-gray-600 mb-2">This document is ready for digital signature</p>
                  <p className="text-sm text-gray-500 mb-6">
                    Enable edit mode to add your signature
                  </p>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Divider */}
        <div className="border-t border-gray-300 my-8"></div>

        {/* Footer */}
        <div className="text-center">
          <div className="mb-4">
            <EditableTextarea
              value={invoice.disclaimer}
              onChange={(value) => handleFieldChange('disclaimer', value)}
              className="text-sm text-gray-600"
              placeholder="Disclaimer text"
            />
          </div>
          <p className="text-sm text-gray-500">
            <EditableField
              value={invoice.copyright}
              onChange={(value) => handleFieldChange('copyright', value)}
              className="text-sm text-gray-500"
              placeholder="Copyright notice"
            />
          </p>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-lg border border-gray-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Invoice Status</p>
              <p className="font-semibold text-green-600">{invoice.payment.status}</p>
            </div>
            <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
              <Check className="text-green-600" size={20} />
            </div>
          </div>
        </div>
        
        <div className="bg-white p-4 rounded-lg border border-gray-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Total Amount</p>
              <p className="font-semibold text-gray-900">${invoice.summary.total.toFixed(2)}</p>
            </div>
            <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
              <DollarSign className="text-blue-600" size={20} />
            </div>
          </div>
        </div>
        
        <div className="bg-white p-4 rounded-lg border border-gray-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Period</p>
              <p className="font-semibold text-gray-900">{invoice.periodCovered}</p>
            </div>
            <div className="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center">
              <Calendar className="text-purple-600" size={20} />
            </div>
          </div>
        </div>
      </div>

      {/* Print Styles */}
      <style jsx>{`
        @media print {
          body * {
            visibility: hidden;
          }
          .print-area, .print-area * {
            visibility: visible;
          }
          .print-area {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            box-shadow: none;
            border: none;
          }
          .no-print {
            display: none !important;
          }
        }
      `}</style>
    </div>
  );
};

export default InvoiceView;