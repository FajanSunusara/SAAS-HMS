import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const invoiceApi = {
  // Get all invoices with pagination
  getAllInvoices: async (page = 0, size = 20) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices`, {
        params: { page, size }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching invoices:', error);
      throw error;
    }
  },

  // Get invoice by ID
  getInvoiceById: async (id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching invoice:', error);
      throw error;
    }
  },

  // Get pending invoices
  getPendingInvoices: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/pending`);
      return response.data;
    } catch (error) {
      console.error('Error fetching pending invoices:', error);
      throw error;
    }
  },

  // Get total pending amount
  getTotalPendingAmount: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/pending/total`);
      return response.data;
    } catch (error) {
      console.error('Error fetching total pending amount:', error);
      throw error;
    }
  },

  

  // Update invoice status
  updateInvoiceStatus: async (id, status) => {
    try {
      const response = await axios.patch(
        `${API_BASE_URL}/v1/invoices/${id}/status`,
        null,
        { params: { status } }
      );
      return response.data;
    } catch (error) {
      console.error('Error updating invoice status:', error);
      throw error;
    }
  },

  // Mark multiple invoices as paid
  markInvoicesAsPaid: async (invoiceIds) => {
    try {
      // Since we don't have a bulk update endpoint, update one by one
      const promises = invoiceIds.map(id => 
        invoiceApi.updateInvoiceStatus(id, 'PAID')
      );
      return await Promise.all(promises);
    } catch (error) {
      console.error('Error marking invoices as paid:', error);
      throw error;
    }
  },

  // Get invoices by date range (custom implementation needed in backend)
  getInvoicesByDateRange: async (startDate, endDate) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/search/by-date`, {
        params: { startDate, endDate }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching invoices by date range:', error);
      throw error;
    }
  },
  // Get invoice by ID with detailed data
  getInvoiceById: async (id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching invoice details:', error);
      throw error;
    }
  },

   // Get invoice by invoice number
  getInvoiceByNumber: async (invoiceNumber) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/by-number/${invoiceNumber}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching invoice by number:', error);
      throw error;
    }
  },


    // Update invoice
  updateInvoice: async (id, invoiceData) => {
    try {
      const response = await axios.put(`${API_BASE_URL}/v1/invoices/${id}`, invoiceData);
      return response.data;
    } catch (error) {
      console.error('Error updating invoice:', error);
      throw error;
    }
  },

    // Send invoice email
  sendInvoiceEmail: async (id, emailData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/v1/invoices/${id}/send-email`, emailData);
      return response.data;
    } catch (error) {
      console.error('Error sending invoice email:', error);
      throw error;
    }
  },


 generateInvoicePDF: async (id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/${id}/pdf`, {
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      console.error('Error generating PDF:', error);
      throw error;
    }
  },

  //  getAllInvoicesOptimized: async (page = 0, size = 20) => {
  //   try {
  //     const response = await axios.get(`${API_BASE_URL}/v1/invoices/optimized`, {
  //       params: { page, size }
  //     });
  //     return response.data;
  //   } catch (error) {
  //     console.error('Error fetching invoices:', error);
  //     throw error;
  //   }
  // },

  // getInvoiceByIdOptimized: async (id) => {
  //   try {
  //     const response = await axios.get(`${API_BASE_URL}/v1/invoices/${id}/optimized`);
  //     return response.data;
  //   } catch (error) {
  //     console.error('Error fetching invoice details:', error);
  //     throw error;
  //   }
  // },

  
  // Generate PDF from backend
  generateInvoicePDF: async (id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/${id}/pdf`, {
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      console.error('Error generating PDF from backend:', error);
      throw error;
    }
  },

  // Send email via backend
  sendInvoiceEmail: async (id, emailData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/v1/invoices/${id}/send-email`, emailData);
      return response.data;
    } catch (error) {
      console.error('Error sending email via backend:', error);
      throw error;
    }
  },

    // Generate and download PDF from backend
  generateInvoicePDF: async (id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/${id}/pdf`, {
        responseType: 'blob',
        timeout: 30000 // 30 second timeout
      });
      return response.data;
    } catch (error) {
      console.error('Error generating PDF from backend:', error);
      throw error;
    }
  },

  // Download PDF directly (alternative method)
  downloadInvoicePDF: async (id, invoiceNumber) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/${id}/pdf`, {
        responseType: 'blob',
        timeout: 30000
      });
      
      // Create blob URL
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      
      // Create download link
      const link = document.createElement('a');
      link.href = url;
      link.download = `invoice_${invoiceNumber || id}.pdf`;
      document.body.appendChild(link);
      link.click();
      
      // Cleanup
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      return true;
    } catch (error) {
      console.error('Error downloading PDF:', error);
      throw error;
    }
  },

  // Send invoice email
  sendInvoiceEmail: async (id, emailData) => {
    try {
      const response = await axios.post(
        `${API_BASE_URL}/v1/invoices/${id}/send-email`, 
        emailData,
        {
          timeout: 30000
        }
      );
      return response.data;
    } catch (error) {
      console.error('Error sending email via backend:', error);
      throw error;
    }
  },

  // Get invoices by status
  getInvoicesByStatus: async (status) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/v1/invoices/search/by-status`, {
        params: { status }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching invoices by status:', error);
      throw error;
    }
  }
};





export default invoiceApi;