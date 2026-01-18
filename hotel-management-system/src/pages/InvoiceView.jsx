import React, { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft, Download, Mail, Printer, CreditCard,
  Edit, Save, X, Check, Copy, FileSignature,
  Building, User, Calendar, DollarSign, FileText,
  Trash2, Plus, Type, PenTool, Upload, Eye, EyeOff,
  Loader2, AlertCircle, Signature, Camera, Image as ImageIcon
} from 'lucide-react';
import invoiceApi from '../api/invoiceApi';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

// Fixed hotel details - use these as default
const FIXED_HOTEL = {
  name: 'Hotel Grand Plaza',
  address: '123 Luxury Street, City, State 12345',
  vatId: 'VAT123456789',
  registration: 'HGP2024',
  phone: '+1 (555) 123-4567',
  email: 'accounts@hotelgrandplaza.com'
};

const InvoiceView = () => {
  const { invoiceId } = useParams();
  const navigate = useNavigate();
  const printRef = useRef(null);
  const canvasRef = useRef(null);
  const signatureCanvasRef = useRef(null);
  
  const [editMode, setEditMode] = useState(false);
  const [showEmailModal, setShowEmailModal] = useState(false);
  const [signatureMode, setSignatureMode] = useState('digital');
  const [isDrawing, setIsDrawing] = useState(false);
  const [lastX, setLastX] = useState(0);
  const [lastY, setLastY] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [downloadingPDF, setDownloadingPDF] = useState(false);
  const [sendingEmail, setSendingEmail] = useState(false);
  const [signatureImage, setSignatureImage] = useState(null);
  
  const [invoice, setInvoice] = useState({
    id: '',
    invoiceNumber: '',
    invoiceId: '',
    statementType: 'Hotel Invoice',
    statementDate: '',
    periodCovered: '',
    
    hotel: FIXED_HOTEL,
    
    client: {
      name: '',
      address: '',
      email: '',
      phone: '',
      company: ''
    },
    
    booking: {
      checkInDate: '',
      checkOutDate: '',
      roomNumber: '',
      roomType: '',
      nights: 0
    },
    
    items: [],
    
    summary: {
      roomCharges: 0,
      serviceCharges: 0,
      foodCharges: 0,
      otherCharges: 0,
      subtotal: 0,
      discountAmount: 0,
      taxAmount: 0,
      totalAmount: 0,
      amountPaid: 0,
      balanceDue: 0,
      taxRate: 18
    },
    
    payment: {
      status: 'PENDING',
      method: '',
      transactionId: '',
      date: '',
      badgeColor: 'bg-yellow-100 text-yellow-800'
    },
    
    signature: {
      signed: false,
      type: null,
      digital: { name: '', title: '', date: '', time: '' },
      manual: { image: null, date: '', time: '' }
    },
    
    notes: '',
    disclaimer: 'This is an official invoice. For any discrepancies, please contact our accounts department within 30 days.',
    copyright: `© ${new Date().getFullYear()} Hotel Grand Plaza. All rights reserved.`
  });

  // Helper function to safely format currency
  const formatCurrency = (amount) => {
    if (amount === undefined || amount === null || isNaN(amount)) {
      return '0.00';
    }
    return parseFloat(amount).toFixed(2);
  };

  // Format date function
  const formatDate = (dateString) => {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    } catch (e) {
      return dateString || '';
    }
  };

  // Map backend status to display status
  const mapStatusToDisplay = (status) => {
    const statusMap = {
      'PENDING': 'PENDING',
      'PAID': 'PAID IN FULL',
      'PARTIAL': 'PARTIALLY PAID',
      'OVERDUE': 'OVERDUE',
      'CANCELLED': 'CANCELLED'
    };
    return statusMap[status] || status || 'PENDING';
  };

  // Get status badge color
  const getStatusBadgeColor = (status) => {
    switch (status) {
      case 'PAID': return 'bg-green-100 text-green-800';
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'OVERDUE': return 'bg-red-100 text-red-800';
      case 'CANCELLED': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  // Transform backend data to frontend format
  const transformBackendData = (backendData) => {
    // Handle null or undefined backendData
    if (!backendData) {
      console.error('Backend data is null or undefined');
      return getMockInvoice();
    }
    
    const booking = backendData.booking || {};
    const guest = backendData.guest || {};
    const payments = backendData.payments || [];
    const latestPayment = payments.length > 0 ? payments[payments.length - 1] : null;
    
    // Create items array from charges
    const items = [];
    if (backendData.roomCharges && backendData.roomCharges > 0) {
      items.push({
        id: 1,
        description: 'Room Charges',
        quantity: booking.nights || 1,
        rate: backendData.roomCharges / (booking.nights || 1),
        amount: backendData.roomCharges
      });
    }
    if (backendData.serviceCharges && backendData.serviceCharges > 0) {
      items.push({
        id: 2,
        description: 'Service Charges',
        quantity: 1,
        rate: backendData.serviceCharges,
        amount: backendData.serviceCharges
      });
    }
    if (backendData.foodCharges && backendData.foodCharges > 0) {
      items.push({
        id: 3,
        description: 'Food & Beverage',
        quantity: 1,
        rate: backendData.foodCharges,
        amount: backendData.foodCharges
      });
    }
    if (backendData.otherCharges && backendData.otherCharges > 0) {
      items.push({
        id: 4,
        description: 'Other Charges',
        quantity: 1,
        rate: backendData.otherCharges,
        amount: backendData.otherCharges
      });
    }
    
    const transformedInvoice = {
      id: backendData.invoiceNumber || `INV-${backendData.invoiceId || 'N/A'}`,
      invoiceId: backendData.invoiceId || '',
      invoiceNumber: backendData.invoiceNumber || 'N/A',
      statementDate: formatDate(backendData.issueDate),
      dueDate: formatDate(backendData.dueDate),
      periodCovered: booking.checkInDate && booking.checkOutDate ? 
        `${formatDate(booking.checkInDate)} - ${formatDate(booking.checkOutDate)}` : 
        'N/A',
      
      hotel: FIXED_HOTEL,
      
      client: {
        name: (guest.firstName && guest.lastName) ? 
          `${guest.firstName} ${guest.lastName}`.trim() : 
          guest.company || guest.email || 'Guest',
        address: guest.address || '',
        email: guest.email || '',
        phone: guest.phone || '',
        company: guest.company || ''
      },
      
      booking: {
        checkInDate: formatDate(booking.checkInDate),
        checkOutDate: formatDate(booking.checkOutDate),
        roomNumber: booking.rooms?.[0]?.roomNumber || booking.roomNumber || 'N/A',
        roomType: booking.rooms?.[0]?.roomType || booking.roomType || 'N/A',
        nights: booking.nights || 0
      },
      
      items: items,
      
      summary: {
        roomCharges: backendData.roomCharges || 0,
        serviceCharges: backendData.serviceCharges || 0,
        foodCharges: backendData.foodCharges || 0,
        otherCharges: backendData.otherCharges || 0,
        subtotal: backendData.subtotal || 0,
        discountAmount: backendData.discountAmount || 0,
        taxAmount: backendData.taxAmount || 0,
        totalAmount: backendData.totalAmount || 0,
        amountPaid: backendData.amountPaid || 0,
        balanceDue: backendData.balanceDue || 0,
        taxRate: booking.taxPercentage ? parseFloat(booking.taxPercentage) : 18
      },
      
      payment: {
        status: mapStatusToDisplay(backendData.status || 'PENDING'),
        method: latestPayment?.paymentMethod || 'Not specified',
        transactionId: latestPayment?.transactionId || 'N/A',
        date: latestPayment ? formatDate(latestPayment.paymentDate) : 'N/A',
        badgeColor: getStatusBadgeColor(backendData.status || 'PENDING')
      },
      
      signature: {
        signed: false,
        type: null,
        digital: { name: '', title: '', date: '', time: '' },
        manual: { image: null, date: '', time: '' }
      },
      
      notes: backendData.notes || '',
      createdAt: backendData.createdAt,
      updatedAt: backendData.updatedAt
    };
    
    return transformedInvoice;
  };

  // Get mock invoice data (fallback)
  const getMockInvoice = () => {
    return {
      id: 'INV-2024-00128',
      invoiceNumber: 'INV-2024-00128',
      invoiceId: invoiceId || 1,
      statementType: 'Hotel Invoice',
      statementDate: '21 May 2024',
      periodCovered: '01 May 2024 - 20 May 2024',
      
      hotel: FIXED_HOTEL,
      
      client: {
        name: 'John Doe',
        address: '456 Guest Avenue, Guest City, GC 67890',
        email: 'john.doe@email.com',
        phone: '+1 (555) 987-6543',
        company: 'Acme Corp.'
      },
      
      booking: {
        checkInDate: '01 May 2024',
        checkOutDate: '20 May 2024',
        roomNumber: 'Deluxe 201',
        roomType: 'Deluxe Room',
        nights: 19
      },
      
      items: [
        {
          id: 1,
          description: 'Deluxe Room Accommodation (19 nights)',
          quantity: 19,
          rate: 250.00,
          amount: 4750.00
        },
        {
          id: 2,
          description: 'Room Service & Dining',
          quantity: 1,
          rate: 320.50,
          amount: 320.50
        }
      ],
      
      summary: {
        roomCharges: 4750.00,
        serviceCharges: 320.50,
        foodCharges: 0,
        otherCharges: 0,
        subtotal: 5070.50,
        discountAmount: 0,
        taxAmount: 912.69,
        totalAmount: 5983.19,
        amountPaid: 5983.19,
        balanceDue: 0,
        taxRate: 18
      },
      
      payment: {
        status: 'PAID IN FULL',
        method: 'Credit Card (**** **** **** 4242)',
        transactionId: 'TXN_582B9E0A1C4F',
        date: '21 May 2024',
        badgeColor: 'bg-green-100 text-green-800'
      },
      
      signature: {
        signed: false,
        type: null,
        digital: { name: '', title: '', date: '', time: '' },
        manual: { image: null, date: '', time: '' }
      },
      
      notes: '',
      disclaimer: 'This is an official invoice. For any discrepancies, please contact our accounts department within 30 days.',
      copyright: `© ${new Date().getFullYear()} Hotel Grand Plaza. All rights reserved.`
    };
  };

  // Fetch invoice data from backend
  const fetchInvoiceData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      console.log(`Fetching invoice data for ID: ${invoiceId}`);
      
      // Try to fetch from backend
      const response = await invoiceApi.getInvoiceById(invoiceId);
      
      if (response && response.data) {
        console.log('Received invoice data:', response.data);
        const transformed = transformBackendData(response.data);
        setInvoice(transformed);
        
        // Load signature if exists
        if (transformed.signature.signed && transformed.signature.type === 'manual') {
          setSignatureImage(transformed.signature.manual.image);
        }
        
        return;
      }
      
      // If no data from endpoint
      console.warn('No data received from backend, using mock data');
      setError('Could not load invoice data. Using demo data.');
      setInvoice(getMockInvoice());
      
    } catch (err) {
      console.error('Error fetching invoice:', err);
      
      // More specific error messages
      if (err.response) {
        if (err.response.status === 404) {
          setError(`Invoice with ID ${invoiceId} not found in the system.`);
        } else if (err.response.status === 500) {
          setError('Server error. Please try again later.');
        } else {
          setError(`Error ${err.response.status}: ${err.response.data?.message || 'Unknown error'}`);
        }
      } else if (err.request) {
        setError('Cannot connect to server. Please check your network connection.');
      } else {
        setError('An unexpected error occurred.');
      }
      
      // Use mock data as fallback
      setInvoice(getMockInvoice());
    } finally {
      setLoading(false);
    }
  };

  // Generate PDF using Frontend (fallback method)
  const generatePDFFrontend = async () => {
    setDownloadingPDF(true);
    try {
      const invoiceElement = printRef.current;
      
      // Create a clone of the invoice element for PDF generation
      const element = invoiceElement.cloneNode(true);
      
      // Remove action buttons and non-printable elements
      const buttons = element.querySelectorAll('button, .no-print, .action-button');
      buttons.forEach(button => button.style.display = 'none');
      
      // Set proper styles for PDF
      element.style.width = '210mm';
      element.style.minHeight = '297mm';
      element.style.padding = '20mm';
      element.style.margin = '0';
      element.style.backgroundColor = 'white';
      element.style.color = 'black';
      
      // Append to document temporarily
      document.body.appendChild(element);
      element.style.position = 'absolute';
      element.style.left = '-9999px';
      
      // Use html2canvas to capture the element
      const canvas = await html2canvas(element, {
        scale: 2, // Higher quality
        useCORS: true,
        logging: false,
        backgroundColor: '#ffffff'
      });
      
      // Remove the temporary element
      document.body.removeChild(element);
      
      // Convert canvas to image
      const imgData = canvas.toDataURL('image/png');
      const imgWidth = 210; // A4 width in mm
      const pageHeight = 297; // A4 height in mm
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      
      // Create PDF
      const pdf = new jsPDF('p', 'mm', 'a4');
      let position = 0;
      
      // Add image to PDF
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
      
      // Add new page if content is too long
      const heightLeft = imgHeight;
      while (heightLeft > 0) {
        position = heightLeft - pageHeight;
        pdf.addPage();
        pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
      }
      
      // Save PDF
      pdf.save(`invoice_${invoice.invoiceNumber || invoiceId}.pdf`);
      
      setDownloadingPDF(false);
      return true;
    } catch (error) {
      console.error('Error generating PDF:', error);
      setDownloadingPDF(false);
      return false;
    }
  };

  // Download PDF - Try backend first, then frontend
  const handleDownloadPDF = async () => {
    try {
      setDownloadingPDF(true);
      
      // Try backend endpoint first
      try {
        const response = await invoiceApi.generateInvoicePDF(invoice.invoiceId);
        if (response) {
          // Create blob from response
          const blob = new Blob([response], { type: 'application/pdf' });
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `invoice_${invoice.invoiceNumber || invoiceId}.pdf`;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
          
          setDownloadingPDF(false);
          return;
        }
      } catch (backendError) {
        console.log('Backend PDF generation failed, using frontend fallback:', backendError);
      }
      
      // Fallback to frontend PDF generation
      const success = await generatePDFFrontend();
      if (!success) {
        throw new Error('PDF generation failed');
      }
      
    } catch (error) {
      console.error('Error downloading PDF:', error);
      alert('Failed to download PDF. Please try again.');
      setDownloadingPDF(false);
    }
  };

  // Send Email - Try backend first, then frontend
  const handleSendEmail = async (emailData) => {
    try {
      setSendingEmail(true);
      
      // Try backend endpoint first
      try {
        await invoiceApi.sendInvoiceEmail(invoice.invoiceId, {
          recipientEmail: emailData.recipientEmail,
          subject: emailData.subject,
          message: emailData.message,
          includePDF: true
        });
        
        alert('Email sent successfully via backend!');
        setShowEmailModal(false);
        return;
      } catch (backendError) {
        console.log('Backend email failed, using frontend fallback:', backendError);
      }
      
      // Fallback to frontend email (mailto link)
      const subject = encodeURIComponent(emailData.subject);
      const body = encodeURIComponent(emailData.message);
      const mailtoLink = `mailto:${emailData.recipientEmail}?subject=${subject}&body=${body}`;
      
      window.location.href = mailtoLink;
      
      setSendingEmail(false);
      setShowEmailModal(false);
      alert('Email client opened. Please send the email manually.');
      
    } catch (error) {
      console.error('Error sending email:', error);
      alert('Failed to send email. Please try again.');
      setSendingEmail(false);
    }
  };

  // Handle print
  const handlePrint = () => {
    // Add print-specific class to show signature area for print
    const printArea = printRef.current;
    printArea.classList.add('print-mode');
    
    setTimeout(() => {
      window.print();
      setTimeout(() => {
        printArea.classList.remove('print-mode');
      }, 100);
    }, 100);
  };

  // Initialize canvas for manual signature
  useEffect(() => {
    if (signatureCanvasRef.current && editMode && signatureMode === 'manual') {
      const canvas = signatureCanvasRef.current;
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
      if (signatureImage) {
        const img = new Image();
        img.onload = () => {
          ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
        };
        img.src = signatureImage;
      }
    }
  }, [editMode, signatureMode, signatureImage]);

  // Manual Signature Drawing Functions
  const startDrawing = (e) => {
    if (!editMode || signatureMode !== 'manual') return;
    
    const canvas = signatureCanvasRef.current;
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
    
    const canvas = signatureCanvasRef.current;
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
    const canvas = signatureCanvasRef.current;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = 'white';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Reset stroke style
    ctx.lineWidth = 2;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.strokeStyle = 'black';
    
    setSignatureImage(null);
  };

  const saveManualSignature = () => {
    const canvas = signatureCanvasRef.current;
    const imageData = canvas.toDataURL('image/png');
    setSignatureImage(imageData);
    
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

  const handleSignatureUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        const img = new Image();
        img.onload = () => {
          const canvas = signatureCanvasRef.current;
          const ctx = canvas.getContext('2d');
          
          // Clear canvas
          ctx.fillStyle = 'white';
          ctx.fillRect(0, 0, canvas.width, canvas.height);
          
          // Draw uploaded image
          ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
          
          // Save to state
          setSignatureImage(event.target.result);
        };
        img.src = event.target.result;
      };
      reader.readAsDataURL(file);
    }
  };

  // Calculate totals
  const calculateTotals = () => {
    const subtotal = invoice.items.reduce((sum, item) => sum + (item.quantity * item.rate), 0);
    const taxAmount = subtotal * (invoice.summary.taxRate / 100);
    const discount = invoice.summary.discountAmount;
    const total = subtotal + taxAmount - discount;
    
    setInvoice(prev => ({
      ...prev,
      summary: {
        ...prev.summary,
        subtotal: parseFloat(subtotal.toFixed(2)),
        taxAmount: parseFloat(taxAmount.toFixed(2)),
        totalAmount: parseFloat(total.toFixed(2))
      }
    }));
  };

  // Handle field changes
  const handleFieldChange = (field, value) => {
    setInvoice(prev => ({ ...prev, [field]: value }));
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
    
    if (field === 'quantity' || field === 'rate') {
      updatedItems[index].amount = updatedItems[index].quantity * updatedItems[index].rate;
    }
    
    setInvoice(prev => ({ ...prev, items: updatedItems }));
    setTimeout(calculateTotals, 100);
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

  // Save invoice to backend
  const saveInvoice = async () => {
    try {
      setSaving(true);
      
      const updateData = {
        dueDate: new Date().toISOString().split('T')[0],
        notes: invoice.notes,
        status: invoice.payment.status === 'PAID IN FULL' ? 'PAID' : 'PENDING',
        discountAmount: invoice.summary.discountAmount,
        taxAmount: invoice.summary.taxAmount,
        otherCharges: invoice.summary.otherCharges
      };
      
      await invoiceApi.updateInvoice(invoice.invoiceId, updateData);
      
      alert('Invoice updated successfully!');
      setEditMode(false);
      fetchInvoiceData(); // Refresh data
    } catch (error) {
      console.error('Error saving invoice:', error);
      alert('Failed to save invoice. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const toggleEditMode = () => {
    if (editMode) {
      saveInvoice();
    } else {
      setEditMode(true);
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    alert('Copied to clipboard!');
  };

  // Render editable field
  const EditableField = ({ value, onChange, type = 'text', className = '', placeholder = '', disabled = false }) => {
    if (editMode && !disabled) {
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
      'PAID IN FULL': { color: 'bg-green-100 text-green-800', icon: <Check size={14} /> },
      'PENDING': { color: 'bg-yellow-100 text-yellow-800', icon: <AlertCircle size={14} /> },
      'OVERDUE': { color: 'bg-red-100 text-red-800', icon: <AlertCircle size={14} /> },
      'PARTIALLY PAID': { color: 'bg-blue-100 text-blue-800', icon: <DollarSign size={14} /> },
      'CANCELLED': { color: 'bg-gray-100 text-gray-800', icon: <X size={14} /> }
    };
    
    const config = statusConfig[status] || statusConfig.PENDING;
    
    return (
      <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium ${config.color}`}>
        {config.icon}
        {status}
      </span>
    );
  };

  // Fetch data on component mount
  useEffect(() => {
    fetchInvoiceData();
  }, [invoiceId]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Loader2 className="w-12 h-12 animate-spin text-blue-600 mx-auto" />
          <p className="mt-4 text-gray-600">Loading invoice...</p>
        </div>
      </div>
    );
  }

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
                  id="emailRecipient"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1">Message</label>
                <textarea
                  className="w-full border rounded-lg px-3 py-2 h-32"
                  defaultValue={`Dear ${invoice.client.name},\n\nPlease find attached your invoice ${invoice.invoiceNumber}.\n\nTotal Amount: $${formatCurrency(invoice.summary.totalAmount)}\n\nBest regards,\n${invoice.hotel.name}`}
                  id="emailMessage"
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
                    const emailData = {
                      recipientEmail: document.getElementById('emailRecipient').value,
                      subject: `Invoice ${invoice.invoiceNumber}`,
                      message: document.getElementById('emailMessage').value
                    };
                    handleSendEmail(emailData);
                  }}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                  disabled={sendingEmail}
                >
                  {sendingEmail ? 'Sending...' : 'Send Email'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Error Display */}
      {error && (
        <div className="mb-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4">
          <div className="flex items-center">
            <AlertCircle className="w-5 h-5 text-yellow-600 mr-2" />
            <p className="text-yellow-700">{error}</p>
          </div>
        </div>
      )}

      {/* Back Button */}
      <button
        onClick={() => navigate('/invoices')}
        className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 no-print"
      >
        <ArrowLeft size={20} />
        Back to Invoices
      </button>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6 no-print">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Invoice #{invoice.invoiceNumber}
            <span className="ml-3">
              <StatusBadge status={invoice.payment.status} />
            </span>
          </h1>
          <p className="text-gray-600">View and manage invoice details</p>
        </div>
        
        {/* Action Buttons */}
        <div className="flex flex-wrap gap-2">
          <button
            onClick={toggleEditMode}
            disabled={saving}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg ${
              editMode
                ? 'bg-green-600 hover:bg-green-700 text-white'
                : 'bg-blue-600 hover:bg-blue-700 text-white'
            } ${saving ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            {saving ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : editMode ? (
              <Save size={18} />
            ) : (
              <Edit size={18} />
            )}
            {saving ? 'Saving...' : editMode ? 'Save Changes' : 'Edit Invoice'}
          </button>
          
          <button
            onClick={handlePrint}
            className="flex items-center gap-2 px-4 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded-lg no-print"
          >
            <Printer size={18} />
            Print
          </button>
          
          <button
            onClick={() => setShowEmailModal(true)}
            className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg no-print"
          >
            <Mail size={18} />
            Email
          </button>
          
          <button
            onClick={handleDownloadPDF}
            disabled={downloadingPDF}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg no-print"
          >
            {downloadingPDF ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Download size={18} />
            )}
            {downloadingPDF ? 'Generating PDF...' : 'Download PDF'}
          </button>
        </div>
      </div>

      {/* Invoice Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6 no-print">
        <div className="bg-white p-4 rounded-lg border border-gray-200 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Total Amount</p>
              <p className="text-2xl font-bold text-gray-900">
                ${formatCurrency(invoice.summary.totalAmount)}
              </p>
            </div>
            <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
              <DollarSign className="text-blue-600" size={20} />
            </div>
          </div>
          <div className="mt-2 text-sm text-gray-600">
            <p>Balance Due: <span className="font-semibold">${formatCurrency(invoice.summary.balanceDue)}</span></p>
            <p>Amount Paid: <span className="font-semibold">${formatCurrency(invoice.summary.amountPaid)}</span></p>
          </div>
        </div>
        
        <div className="bg-white p-4 rounded-lg border border-gray-200 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Guest</p>
              <p className="text-lg font-semibold text-gray-900">{invoice.client.name}</p>
              <p className="text-sm text-gray-600">{invoice.client.company}</p>
            </div>
            <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
              <User className="text-green-600" size={20} />
            </div>
          </div>
          <div className="mt-2 text-sm text-gray-600">
            <p>{invoice.client.email}</p>
            <p>{invoice.client.phone}</p>
          </div>
        </div>
        
        <div className="bg-white p-4 rounded-lg border border-gray-200 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Stay Details</p>
              <p className="text-lg font-semibold text-gray-900">{invoice.booking.roomNumber}</p>
              <p className="text-sm text-gray-600">{invoice.booking.roomType}</p>
            </div>
            <div className="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center">
              <Building className="text-purple-600" size={20} />
            </div>
          </div>
          <div className="mt-2 text-sm text-gray-600">
            <p>{invoice.booking.checkInDate} - {invoice.booking.checkOutDate}</p>
            <p>{invoice.booking.nights} night(s)</p>
          </div>
        </div>
      </div>

      {/* Main Invoice Container - A4 Size for Print */}
      <div 
        ref={printRef} 
        className="bg-white rounded-xl shadow-lg border border-gray-200 p-8 mx-auto"
        style={{
          maxWidth: '210mm',
          minHeight: '297mm',
          margin: '0 auto'
        }}
      >
        {/* Hotel Logo & Header */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 pb-8 border-b">
          <div className="mb-6 md:mb-0">
            <div className="flex items-center gap-3 mb-2">
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <Building className="text-blue-600" size={24} />
              </div>
              <div>
                <h2 className="text-3xl font-bold text-gray-900">
                  {invoice.hotel?.name || FIXED_HOTEL.name}
                </h2>
                <p className="text-sm text-gray-600 mt-1">
                  Official Invoice for Services Rendered
                </p>
              </div>
            </div>
          </div>
          
          <div className="text-right">
            <h3 className="text-2xl font-bold text-gray-900 mb-2">INVOICE</h3>
            <div className="space-y-1 text-sm">
              <p>
                <span className="font-semibold">Invoice Date:</span>{' '}
                {invoice.statementDate}
              </p>
              <p>
                <span className="font-semibold">Due Date:</span>{' '}
                <EditableField
                  value={invoice.dueDate}
                  onChange={(value) => handleFieldChange('dueDate', value)}
                  className="w-32"
                  placeholder="Due Date"
                />
              </p>
              <p>
                <span className="font-semibold">Invoice Number:</span>{' '}
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
                {invoice.hotel?.name || FIXED_HOTEL.name}
              </p>
              <p className="text-gray-600">{invoice.hotel?.address || FIXED_HOTEL.address}</p>
              <p className="text-sm text-gray-600">
                <span className="font-medium">Phone:</span> {invoice.hotel?.phone || FIXED_HOTEL.phone}
              </p>
              <p className="text-sm text-gray-600">
                <span className="font-medium">Email:</span> {invoice.hotel?.email || FIXED_HOTEL.email}
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

        {/* Stay Details */}
        <div className="mb-8 p-4 bg-gray-50 rounded-lg">
          <h4 className="text-lg font-semibold text-gray-900 mb-4">STAY DETAILS</h4>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <p className="text-sm text-gray-600">Check-in</p>
              <p className="font-medium">{invoice.booking.checkInDate}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Check-out</p>
              <p className="font-medium">{invoice.booking.checkOutDate}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Room</p>
              <p className="font-medium">{invoice.booking.roomNumber} ({invoice.booking.roomType})</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Nights</p>
              <p className="font-medium">{invoice.booking.nights}</p>
            </div>
          </div>
        </div>

        {/* Items Table */}
        <div className="mb-8">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold">INVOICE ITEMS</h3>
            {editMode && (
              <button
                onClick={addNewItem}
                className="flex items-center gap-2 px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 no-print"
              >
                <Plus size={16} />
                Add Item
              </button>
            )}
          </div>
          {invoice.items.length === 0 ? (
            <div className="text-center py-8 border border-dashed border-gray-300 rounded-lg">
              <p className="text-gray-500">No items added to this invoice</p>
              {editMode && (
                <button
                  onClick={addNewItem}
                  className="mt-2 text-blue-600 hover:text-blue-800"
                >
                  Add your first item
                </button>
              )}
            </div>
          ) : (
            <table className="w-full border-collapse">
              <thead className="border-b-2 border-gray-800">
                <tr>
                  <th className="text-left py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">
                    DESCRIPTION
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
                    <th className="text-center py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider no-print">
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
                        ${formatCurrency(item.amount)}
                      </span>
                    </td>
                    {editMode && (
                      <td className="text-center py-4 no-print">
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
          )}
        </div>

        {/* Totals Section */}
        <div className="flex justify-end mb-12">
          <div className="w-full md:w-2/3 lg:w-1/2">
            <div className="space-y-3">
              {/* Subtotal */}
              <div className="flex justify-between items-center py-2 border-b border-gray-200">
                <span className="text-gray-600">Subtotal</span>
                <span className="font-medium text-gray-900">
                  ${formatCurrency(invoice.summary.subtotal)}
                </span>
              </div>
              
              {/* Discount */}
              <div className="flex justify-between items-center py-2 border-b border-gray-200">
                <span className="text-gray-600">
                  Discount
                  {editMode && (
                    <EditableField
                      value={invoice.summary.discountAmount}
                      onChange={(value) => handleSummaryChange('discountAmount', value)}
                      type="number"
                      className="w-20 ml-2 no-print"
                      placeholder="0"
                    />
                  )}
                </span>
                <span className="font-medium text-green-600">
                  -${formatCurrency(invoice.summary.discountAmount)}
                </span>
              </div>
              
              {/* Tax */}
              <div className="flex justify-between items-center py-2 border-b border-gray-200">
                <span className="text-gray-600">
                  Tax ({invoice.summary.taxRate}%)
                  {editMode && (
                    <EditableField
                      value={invoice.summary.taxAmount}
                      onChange={(value) => handleSummaryChange('taxAmount', value)}
                      type="number"
                      className="w-20 ml-2 no-print"
                      placeholder="0"
                    />
                  )}
                </span>
                <span className="font-medium text-gray-900">
                  ${formatCurrency(invoice.summary.taxAmount)}
                </span>
              </div>
              
              {/* Amount Paid */}
              <div className="flex justify-between items-center py-2 border-b border-gray-200">
                <span className="text-gray-600">Amount Paid</span>
                <span className="font-medium text-blue-600">
                  ${formatCurrency(invoice.summary.amountPaid)}
                </span>
              </div>
              
              {/* Balance Due - Highlighted */}
              <div className="flex justify-between items-center py-4 border-t-2 border-gray-900 mt-2">
                <span className="text-lg font-bold text-gray-900">
                  {invoice.summary.balanceDue > 0 ? 'BALANCE DUE' : 'TOTAL PAID'}
                </span>
                <span className={`text-2xl font-bold ${
                  invoice.summary.balanceDue > 0 ? 'text-red-600' : 'text-green-600'
                }`}>
                  ${formatCurrency(Math.abs(invoice.summary.balanceDue))}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Payment Status */}
        <div className="mb-8">
          <h4 className="text-lg font-semibold text-gray-900 mb-4">
            PAYMENT STATUS
          </h4>
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                  invoice.payment.status === 'PAID IN FULL' ? 'bg-green-100' :
                  invoice.payment.status === 'PENDING' ? 'bg-yellow-100' :
                  invoice.payment.status === 'OVERDUE' ? 'bg-red-100' : 'bg-gray-100'
                }`}>
                  {invoice.payment.status === 'PAID IN FULL' && <Check className="text-green-600" size={20} />}
                  {invoice.payment.status === 'PENDING' && <AlertCircle className="text-yellow-600" size={20} />}
                  {invoice.payment.status === 'OVERDUE' && <AlertCircle className="text-red-600" size={20} />}
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
                        className="border rounded px-3 py-1 no-print"
                      >
                        <option value="PAID IN FULL">PAID IN FULL</option>
                        <option value="PENDING">PENDING</option>
                        <option value="OVERDUE">OVERDUE</option>
                        <option value="PARTIALLY PAID">PARTIALLY PAID</option>
                      </select>
                    ) : (
                      <StatusBadge status={invoice.payment.status} />
                    )}
                  </div>
                  <p className="text-sm text-gray-600 mt-1">
                    {invoice.payment.status === 'PAID IN FULL' ? 'Fully paid and processed' :
                     invoice.payment.status === 'PENDING' ? 'Payment pending' :
                     invoice.payment.status === 'OVERDUE' ? 'Payment overdue' : 'Payment in progress'}
                  </p>
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
                      className="text-gray-500 hover:text-gray-700 no-print"
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

        {/* Signature Section */}
        <div className="mb-8">
          <h4 className="text-lg font-semibold text-gray-900 mb-4">
            SIGNATURE
          </h4>
          
          {editMode ? (
            <div className="border-2 border-blue-300 bg-blue-50 rounded-lg p-6">
              <div className="mb-4">
                <h5 className="font-medium text-gray-900 mb-2">Add Signature</h5>
                <div className="flex gap-4 mb-4">
                  <button
                    onClick={() => setSignatureMode('digital')}
                    className={`flex-1 flex flex-col items-center justify-center p-4 border-2 rounded-lg ${
                      signatureMode === 'digital' 
                        ? 'border-blue-500 bg-blue-100' 
                        : 'border-gray-300 hover:border-gray-400'
                    }`}
                  >
                    <Type size={24} className="mb-2 text-gray-600" />
                    <span className="font-medium">Type Signature</span>
                    <span className="text-sm text-gray-500">Enter name and title</span>
                  </button>
                  <button
                    onClick={() => setSignatureMode('manual')}
                    className={`flex-1 flex flex-col items-center justify-center p-4 border-2 rounded-lg ${
                      signatureMode === 'manual' 
                        ? 'border-blue-500 bg-blue-100' 
                        : 'border-gray-300 hover:border-gray-400'
                    }`}
                  >
                    <PenTool size={24} className="mb-2 text-gray-600" />
                    <span className="font-medium">Draw Signature</span>
                    <span className="text-sm text-gray-500">Draw your signature</span>
                  </button>
                  <label className="flex-1 flex flex-col items-center justify-center p-4 border-2 border-gray-300 rounded-lg hover:border-gray-400 cursor-pointer">
                    <ImageIcon size={24} className="mb-2 text-gray-600" />
                    <span className="font-medium">Upload Signature</span>
                    <span className="text-sm text-gray-500">Upload signature image</span>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleSignatureUpload}
                      className="hidden"
                    />
                  </label>
                </div>
              </div>

              {signatureMode === 'digital' ? (
                <div className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-1">Name *</label>
                      <input
                        type="text"
                        value={invoice.signature.digital.name}
                        onChange={(e) => setInvoice(prev => ({
                          ...prev,
                          signature: {
                            ...prev.signature,
                            digital: { ...prev.signature.digital, name: e.target.value }
                          }
                        }))}
                        className="w-full border rounded px-3 py-2"
                        placeholder="Signatory Name"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-1">Title *</label>
                      <input
                        type="text"
                        value={invoice.signature.digital.title}
                        onChange={(e) => setInvoice(prev => ({
                          ...prev,
                          signature: {
                            ...prev.signature,
                            digital: { ...prev.signature.digital, title: e.target.value }
                          }
                        }))}
                        className="w-full border rounded px-3 py-2"
                        placeholder="e.g., Accounts Manager"
                      />
                    </div>
                  </div>
                  <div className="flex justify-end gap-3">
                    <button
                      onClick={() => {
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
                        setInvoice(prev => ({ ...prev, signature: signatureData }));
                        alert('Digital signature saved!');
                      }}
                      disabled={!invoice.signature.digital.name || !invoice.signature.digital.title}
                      className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <FileSignature size={18} />
                      Save Signature
                    </button>
                  </div>
                </div>
              ) : (
                <div className="space-y-4">
                  <div className="border border-gray-300 rounded-lg p-4 bg-white">
                    <div className="flex justify-between items-center mb-2">
                      <label className="text-sm font-medium">Draw your signature below:</label>
                      <button
                        onClick={clearManualSignature}
                        className="text-sm text-red-600 hover:text-red-800 no-print"
                      >
                        Clear Canvas
                      </button>
                    </div>
                    <div className="border-2 border-dashed border-gray-300 rounded">
                      <canvas
                        ref={signatureCanvasRef}
                        width={600}
                        height={200}
                        className="w-full h-50 cursor-crosshair"
                        onMouseDown={startDrawing}
                        onMouseMove={draw}
                        onMouseUp={stopDrawing}
                        onMouseLeave={stopDrawing}
                      />
                    </div>
                    <p className="text-xs text-gray-500 mt-2">
                      Click and drag to draw your signature. For print, this area will be used for manual signing.
                    </p>
                  </div>
                  <div className="flex justify-end gap-3">
                    <button
                      onClick={clearManualSignature}
                      className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 no-print"
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
          ) : invoice.signature.signed ? (
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
              ) : signatureImage ? (
                <div className="text-center">
                  <div className="mb-4">
                    <img 
                      src={signatureImage} 
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
              ) : (
                <div className="text-center py-8">
                  <div className="w-16 h-16 border-2 border-dashed border-gray-400 rounded-full mx-auto mb-4 flex items-center justify-center">
                    <Signature size={24} className="text-gray-400" />
                  </div>
                  <p className="text-gray-600 mb-2">This document is ready for signature</p>
                  <p className="text-sm text-gray-500">
                    Enable edit mode to add your signature
                  </p>
                </div>
              )}
            </div>
          ) : (
            <div className="border-2 border-dashed border-gray-300 bg-gray-50 rounded-lg p-6">
              <div className="text-center py-8">
                <div className="w-16 h-16 border-2 border-dashed border-gray-400 rounded-full mx-auto mb-4 flex items-center justify-center">
                  <Signature size={24} className="text-gray-400" />
                </div>
                <p className="text-gray-600 mb-2">This document is ready for signature</p>
                <p className="text-sm text-gray-500">
                  Enable edit mode to add your signature
                </p>
              </div>
              
              {/* Signature Area for Print (only shows when printing) */}
              <div className="print-only mt-8 border-t-2 border-dashed border-gray-400 pt-8">
                <p className="text-sm text-gray-600 mb-4">For physical signing:</p>
                <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
                  <p className="font-medium text-gray-900">SIGNATURE AREA</p>
                  <p className="text-sm text-gray-500 mt-2">Please sign here when printing this document</p>
                  <div className="mt-4 h-32 border-b-2 border-gray-400"></div>
                  <div className="flex justify-between mt-4 text-sm text-gray-600">
                    <span>Name: _________________________</span>
                    <span>Date: _________________________</span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Notes Section */}
        <div className="mb-8">
          <h4 className="text-lg font-semibold text-gray-900 mb-4">
            NOTES
          </h4>
          <EditableTextarea
            value={invoice.notes}
            onChange={(value) => handleFieldChange('notes', value)}
            className="border border-gray-300 rounded-lg p-4 bg-gray-50 w-full"
            placeholder="Add any notes or special instructions..."
          />
        </div>

        {/* Footer */}
        <div className="text-center pt-8 border-t border-gray-300">
          <div className="mb-4">
            <p className="text-sm text-gray-600">{invoice.disclaimer}</p>
          </div>
          <p className="text-sm text-gray-500">
            {invoice.copyright}
          </p>
          <p className="text-xs text-gray-400 mt-2">
            Generated on: {new Date().toLocaleDateString()} {new Date().toLocaleTimeString()}
          </p>
        </div>
      </div>

      {/* Print Styles */}
      <style jsx>{`
        @media print {
          /* Hide everything except the invoice */
          body * {
            visibility: hidden;
          }
          
          #root, #root > div, .invoice-container, .invoice-container * {
            visibility: visible;
          }
          
          /* Invoice container styling for print */
          .invoice-container {
            position: absolute;
            left: 0;
            top: 0;
            width: 210mm;
            min-height: 297mm;
            margin: 0;
            padding: 15mm;
            background: white;
            box-shadow: none;
            border: none;
          }
          
          /* Hide non-printable elements */
          .no-print {
            display: none !important;
          }
          
          /* Show print-only elements */
          .print-only {
            display: block !important;
          }
          
          /* Ensure proper page breaks */
          .page-break {
            page-break-before: always;
          }
          
          /* Adjust font sizes for print */
          body {
            font-size: 12pt;
          }
          
          h1, h2, h3, h4 {
            font-size: 14pt !important;
            margin-bottom: 10pt !important;
          }
          
          /* Remove backgrounds for better print */
          .bg-gray-50, .bg-green-50, .bg-blue-50, .bg-yellow-50, .bg-red-50 {
            background: transparent !important;
          }
          
          /* Ensure text is black for print */
          .text-gray-900, .text-gray-700, .text-gray-600, .text-gray-500 {
            color: black !important;
          }
          
          /* Ensure borders are visible */
          .border-gray-300, .border-gray-200 {
            border-color: #999 !important;
          }
          
          /* Signature area styling for print */
          .signature-print-area {
            border: 2px dashed #ccc;
            padding: 20pt;
            margin-top: 20pt;
            min-height: 100pt;
          }
        }
        
        @page {
          size: A4;
          margin: 15mm;
        }
        
        /* Print mode class for showing signature area */
        .print-mode .print-only {
          display: block;
        }
      `}</style>
    </div>
  );
};

export default InvoiceView;