import API from "../api/axiosConfig";

const getPaymentByBookingId = async (bookingId) => {
    const response = await API.get(`/payments/booking/${bookingId}`);
    return response.data;
};

const processPayment = async (bookingId, paymentMethod) => {
    const response = await API.post(`/payments/process/${bookingId}?paymentMethod=${paymentMethod}`);
    return response.data;
};

const verifyPayment = async (paymentId) => {
    const response = await API.get(`/payments/${paymentId}/verify`);
    return response.data;
};

const refundPayment = async (paymentId) => {
    const response = await API.put(`/payments/${paymentId}/refund`);
    return response.data;
};

export default {
    getPaymentByBookingId,
    processPayment,
    verifyPayment,
    refundPayment,
};
