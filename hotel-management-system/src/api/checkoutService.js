import apiService from './apiService';

export const checkoutService = {
  async getTodayCheckOuts() {
    return apiService.get('/v1/bookings/checkouts/today');
  },

  async getExpectedDepartures(date) {
    return apiService.get(`/v1/bookings/departures/today?date=${date}`);
  },

  async processCheckOut(bookingId, checkoutData) {
    return apiService.post(`/v1/bookings/${bookingId}/checkout`, checkoutData);
  },

  async getBookingBill(bookingId) {
    return apiService.get(`/v1/bookings/${bookingId}/bill`);
  },

  async processPayment(paymentData) {
    return apiService.post('/v1/payments', paymentData);
  },

  async updateBookingStatus(bookingId, status) {
    return apiService.patch(`/v1/bookings/${bookingId}/status?status=${status}`);
  },

  async searchDepartures(keyword, filters = {}) {
    const params = new URLSearchParams({ keyword, ...filters });
    return apiService.get(`/v1/bookings/search/departures?${params}`);
  },
};