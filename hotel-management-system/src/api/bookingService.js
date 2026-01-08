// src/api/bookingService.js
import apiService from './apiService';

export const bookingService = {
  async getAllBookings() {
    return apiService.get('/v1/bookings');
  },

  async getBookingById(id) {
    return apiService.get(`/v1/bookings/${id}`);
  },

  async getBookingByCode(code) {
    return apiService.get(`/v1/bookings/code/${code}`);
  },

  async getTodayCheckIns() {
    return apiService.get('/v1/bookings/checkins/today');
  },

  async getTodayCheckOuts() {
    return apiService.get('/v1/bookings/checkouts/today');
  },

  async createBooking(bookingData) {
    return apiService.post('/v1/bookings', bookingData);
  },

  async updateBookingStatus(id, status) {
    return apiService.patch(`/v1/bookings/${id}/status?status=${status}`);
  },

  async cancelBooking(id, reason) {
    const params = reason ? `?reason=${encodeURIComponent(reason)}` : '';
    return apiService.delete(`/v1/bookings/${id}${params}`);
  },

  async searchBookings(keyword, page = 0, size = 20) {
    return apiService.get(`/v1/bookings/search?keyword=${keyword}&page=${page}&size=${size}`);
  },
};
