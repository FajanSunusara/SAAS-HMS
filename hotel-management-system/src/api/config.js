// src/api/config.js
const API_BASE_URL = 'http://localhost:8080/api';

export const apiConfig = {
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
};

export default API_BASE_URL;
