// src/api/paymentService.js
import apiService from './apiService';

export const paymentService = {
  async processPayment(paymentData) {
    return apiService.post('/v1/payments', paymentData);
  },

  async getPaymentById(paymentId) {
    return apiService.get(`/v1/payments/${paymentId}`);
  },

  async getPaymentsByInvoice(invoiceId) {
    return apiService.get(`/v1/payments/invoice/${invoiceId}`);
  },

  async getPaymentsByBooking(bookingId) {
    return apiService.get(`/v1/payments/booking/${bookingId}`);
  },

  async getAllPayments(page = 0, size = 20) {
    return apiService.get(`/v1/payments?page=${page}&size=${size}`);
  },

  async getTodayCollection() {
    return apiService.get('/v1/payments/collection/today');
  },

  async getPaymentMethodBreakdown(startDate, endDate) {
    return apiService.get(`/v1/payments/breakdown?startDate=${startDate}&endDate=${endDate}`);
  },

  async getPaymentsBetweenDates(startDate, endDate) {
    return apiService.get(`/v1/payments/range?start=${startDate}&end=${endDate}`);
  },

  async processCheckoutPayment(bookingId, paymentData) {
    // Custom payment for checkout with additional details
    const checkoutPayment = {
      bookingId: bookingId,
      amount: paymentData.amount,
      paymentMethod: paymentData.paymentMethod,
      currency: paymentData.currency || 'USD',
      status: 'COMPLETED',
      transactionId: `TXN-${Date.now()}`,
      referenceNumber: paymentData.referenceNumber,
      notes: paymentData.notes || 'Checkout payment',
      paymentDate: new Date().toISOString()
    };
    
    return this.processPayment(checkoutPayment);
  },

  async splitPayment(bookingId, splitData) {
    // Handle split payments (multiple payment methods)
    const promises = splitData.payments.map(payment => 
      this.processPayment({
        bookingId,
        amount: payment.amount,
        paymentMethod: payment.method,
        currency: payment.currency || 'USD',
        status: 'COMPLETED',
        notes: `Split payment - ${payment.method}`
      })
    );
    
    return Promise.all(promises);
  },

  async refundPayment(paymentId, refundData) {
    return apiService.post(`/v1/payments/${paymentId}/refund`, refundData);
  },

  async getPaymentSummary(bookingId) {
    const payments = await this.getPaymentsByBooking(bookingId);
    const totalPaid = payments.data?.reduce((sum, payment) => 
      sum + parseFloat(payment.amount || 0), 0) || 0;
    
    return {
      totalPaid,
      payments: payments.data || [],
      lastPayment: payments.data?.[payments.data.length - 1]
    };
  }
};