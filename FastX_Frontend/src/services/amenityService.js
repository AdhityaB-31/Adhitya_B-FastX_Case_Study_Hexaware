import API from "../api/axiosConfig";

const getAllAmenities = async () => {
	const response = await API.get("/amenities");
	return response.data;
};

const createAmenity = async (amenityData) => {
	const response = await API.post("/amenities/create", amenityData);
	return response.data;
};

const deleteAmenity = async (amenityId) => {
	const response = await API.delete(`/amenities/delete/${amenityId}`);
	return response.data;
};

export default {
	getAllAmenities,
	createAmenity,
	deleteAmenity,
};
