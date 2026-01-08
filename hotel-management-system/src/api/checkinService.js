import apiService from './apiService';

export const checkinService = {
  async getTodayCheckIns() {
    return apiService.get('/v1/bookings/checkins/today');
  },

  async getTodayCheckOuts() {
    return apiService.get('/v1/bookings/checkouts/today');
  },

  async processCheckIn(bookingId, checkinData = {}) {
    return apiService.patch(`/v1/bookings/${bookingId}/checkin`, checkinData);
  },

  async getExpectedArrivals() {
    // You'll need to create this endpoint in backend
    return apiService.get('/v1/bookings/arrivals/today');
  },

  async cancelCheckIn(bookingId, reason) {
    return apiService.patch(`/v1/bookings/${bookingId}/checkin/cancel`, { reason });
  },

  async searchBookingsForCheckIn(keyword, filters = {}) {
    const params = new URLSearchParams({ keyword, ...filters });
    return apiService.get(`/v1/bookings/search?${params}`);
  }
};