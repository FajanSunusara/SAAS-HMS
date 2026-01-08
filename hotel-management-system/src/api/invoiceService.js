// src/api/invoiceService.js
import apiService from './apiService';

export const invoiceService = {
  async generateInvoice(bookingId) {
    return apiService.post(`/v1/invoices/generate/${bookingId}`);
  },

  async getInvoiceById(invoiceId) {
    return apiService.get(`/v1/invoices/${invoiceId}`);
  },

  async getInvoiceByBooking(bookingId) {
    return apiService.get(`/v1/invoices/booking/${bookingId}`);
  },

  async getInvoicesByGuest(guestId) {
    return apiService.get(`/v1/invoices/guest/${guestId}`);
  },

  async getAllInvoices(page = 0, size = 20) {
    return apiService.get(`/v1/invoices?page=${page}&size=${size}`);
  },

  async getPendingInvoices() {
    return apiService.get('/v1/invoices/pending');
  },

  async updateInvoiceStatus(invoiceId, status) {
    return apiService.patch(`/v1/invoices/${invoiceId}/status?status=${status}`);
  },

  async getTotalPendingAmount() {
    return apiService.get('/v1/invoices/pending/total');
  },

  async getInvoiceSummary(invoiceId) {
    const invoice = await this.getInvoiceById(invoiceId);
    const payments = await paymentService.getPaymentsByInvoice(invoiceId);
    
    return {
      invoice: invoice.data,
      payments: payments.data || [],
      totalPaid: payments.data?.reduce((sum, p) => sum + parseFloat(p.amount || 0), 0) || 0,
      balance: parseFloat(invoice.data?.totalAmount || 0) - (payments.data?.reduce((sum, p) => sum + parseFloat(p.amount || 0), 0) || 0)
    };
  },

  async downloadInvoice(invoiceId, format = 'pdf') {
    return apiService.get(`/v1/invoices/${invoiceId}/download?format=${format}`, {
      responseType: 'blob'
    });
  },

  async sendInvoiceEmail(invoiceId, emailData = {}) {
    const defaultEmail = {
      to: '',
      subject: 'Your Invoice from Hotel',
      message: 'Please find your invoice attached.',
      sendCopyToHotel: true
    };
    
    return apiService.post(`/v1/invoices/${invoiceId}/send`, { ...defaultEmail, ...emailData });
  },

  async printInvoice(invoiceId) {
    return apiService.get(`/v1/invoices/${invoiceId}/print`);
  },

  async getInvoiceItems(invoiceId) {
    return apiService.get(`/v1/invoices/${invoiceId}/items`);
  },

  async addInvoiceItem(invoiceId, item) {
    return apiService.post(`/v1/invoices/${invoiceId}/items`, item);
  },

  async updateInvoiceItem(invoiceId, itemId, itemData) {
    return apiService.put(`/v1/invoices/${invoiceId}/items/${itemId}`, itemData);
  },

  async deleteInvoiceItem(invoiceId, itemId) {
    return apiService.delete(`/v1/invoices/${invoiceId}/items/${itemId}`);
  },

  async applyDiscount(invoiceId, discountData) {
    return apiService.post(`/v1/invoices/${invoiceId}/discount`, discountData);
  },

  async getInvoiceHistory(invoiceId) {
    return apiService.get(`/v1/invoices/${invoiceId}/history`);
  },

  async voidInvoice(invoiceId, reason) {
    return apiService.post(`/v1/invoices/${invoiceId}/void`, { reason });
  },

  async duplicateInvoice(originalInvoiceId, newBookingId = null) {
    return apiService.post(`/v1/invoices/${originalInvoiceId}/duplicate`, { newBookingId });
  },

  async getInvoiceTemplate(invoiceId) {
    return apiService.get(`/v1/invoices/${invoiceId}/template`);
  },

  async updateInvoiceTemplate(invoiceId, templateData) {
    return apiService.put(`/v1/invoices/${invoiceId}/template`, templateData);
  }
};

// For circular dependency, import paymentService here
import { paymentService } from './paymentService';