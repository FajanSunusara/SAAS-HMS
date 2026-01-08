// src/api/dashboardService.js
import apiService from './apiService';

export const dashboardService = {
  async getDashboardStats() {
    return apiService.get('/v1/dashboard/stats');
  },

  async getHealthCheck() {
    return apiService.get('/v1/dashboard/health');
  },
};
