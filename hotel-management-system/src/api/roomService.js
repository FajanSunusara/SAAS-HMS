// src/api/roomService.js
import apiService from './apiService';

export const roomService = {
  async getAllRooms() {
    return apiService.get('/v1/rooms');
  },

  async getRoomById(id) {
    return apiService.get(`/v1/rooms/${id}`);
  },

  async getRoomsByStatus(status) {
    return apiService.get(`/v1/rooms/status/${status}`);
  },

  async getRoomStatusSummary() {
    return apiService.get('/v1/rooms/status/summary');
  },

  async getAvailableRooms(checkIn, checkOut, roomType) {
    const params = new URLSearchParams({
      checkIn,
      checkOut,
      ...(roomType && { roomType }),
    });
    return apiService.get(`/v1/rooms/available?${params}`);
  },

  async createRoom(roomData) {
    return apiService.post('/v1/rooms', roomData);
  },

  async updateRoom(id, roomData) {
    return apiService.put(`/v1/rooms/${id}`, roomData);
  },

  async updateRoomStatus(id, status) {
    return apiService.patch(`/v1/rooms/${id}/status?status=${status}`);
  },

  async deleteRoom(id) {
    return apiService.delete(`/v1/rooms/${id}`);
  },
};
