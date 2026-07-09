import API from "../api/axiosConfig";

const getAllUsers = async () => {
	const response = await API.get("/users");

	return response.data;
};

const deleteUser = async (id) => {
	const response = await API.delete(`/users/delete/${id}`);

	return response.data;
};

export default {
	getAllUsers,
	deleteUser,
};
