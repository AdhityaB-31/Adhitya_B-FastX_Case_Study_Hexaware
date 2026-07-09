import API from "../api/axiosConfig";

const createBooking = async (bookingData) => {
    const response = await API.post("/bookings/create", bookingData);
    return response.data;
};

const confirmBooking = async (bookingId) => {
    const response = await API.put(`/bookings/${bookingId}/confirm`);
    return response.data;
};

const cancelBooking = async (bookingId, reason) => {
    const url = reason 
        ? `/bookings/${bookingId}/cancel?reason=${encodeURIComponent(reason)}` 
        : `/bookings/${bookingId}/cancel`;
    const response = await API.put(url);
    return response.data;
};

const getBookingById = async (bookingId) => {
    const response = await API.get(`/bookings/${bookingId}`);
    return response.data;
};

const getBookingsByUser = async (userId) => {
    const response = await API.get(`/bookings/user/${userId}`);
    return response.data;
};

const getBookingsByBus = async (busId) => {
    const response = await API.get(`/bookings/bus/${busId}`);
    return response.data;
};

const calculateFare = async (busId, numberOfSeats) => {
    const response = await API.get(`/bookings/fare?busId=${busId}&numberOfSeats=${numberOfSeats}`);
    return response.data;
};

export default {
    createBooking,
    confirmBooking,
    cancelBooking,
    getBookingById,
    getBookingsByUser,
    getBookingsByBus,
    calculateFare,
};
