import API from "../api/axiosConfig";

const searchBuses = async (origin, destination, journeyDate) => {
	const response = await API.get(
		`/buses/search?origin=${origin}&destination=${destination}&journeyDate=${journeyDate}`,
	);
	return response.data;
};

const getBusDetails = async (busId) => {
	const response = await API.get(`/buses/${busId}`);
	return response.data;
};

const getAvailableSeats = async (busId) => {
	const response = await API.get(`/buses/${busId}/available-seats`);
	return response.data;
};

const getBusesByOperator = async (operatorId) => {
	const response = await API.get(`/buses/operator/${operatorId}`);
	return response.data;
};

const addBus = async (busData) => {
	const response = await API.post("/buses/create", busData);
	return response.data;
};

const updateBus = async (busId, busData) => {
	const response = await API.put(`/buses/update/${busId}`, busData);
	return response.data;
};

const deleteBus = async (busId) => {
	const response = await API.delete(`/buses/delete/${busId}`);
	return response.data;
};

const getSeatsByBus = async (busId) => {
	const response = await API.get(`/seats/bus/${busId}`);
	return response.data;
};

const reserveSeats = async (seatIds, bookingId) => {
	const response = await API.post(`/seats/reserve/${bookingId}`, seatIds);
	return response.data;
};

const releaseSeats = async (seatIds) => {
	const response = await API.post("/seats/release", seatIds);
	return response.data;
};

const getAllBuses = async () => {
	const response = await API.get("/buses/all");
	return response.data;
};

const getAllAmenities = async () => {
	const response = await API.get("/amenities");
	return response.data;
};

const searchBusesPublic = async (origin, destination, journeyDate) => {
	const response = await API.get(
		`/buses/search?origin=${origin}&destination=${destination}&journeyDate=${journeyDate}`,
	);
	return response.data;
};

const getCheapestBuses = async () => {
	const response = await API.get("/buses/cheapest");
	return response.data;
};

export default {
	searchBuses,
	getBusDetails,
	getAvailableSeats,
	getBusesByOperator,
	addBus,
	updateBus,
	deleteBus,
	getSeatsByBus,
	reserveSeats,
	releaseSeats,
	getAllBuses,
	getAllAmenities,
	searchBusesPublic,
	getCheapestBuses,
};
