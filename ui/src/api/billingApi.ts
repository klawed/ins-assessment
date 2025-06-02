import axios from 'axios';

const api = axios.create({
  baseURL: '/api/billing'
});

export const billingApi = {
  getCustomerBillings: async (customerId: string) => {
    const response = await api.get(`/customer/${customerId}`);
    return response.data;
  },

  submitPayment: async (request: PaymentRequest) => {
    const response = await api.post('/payments', request);
    return response.data;
  }
};