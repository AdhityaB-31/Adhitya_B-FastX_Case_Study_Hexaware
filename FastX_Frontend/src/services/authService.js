import API from "../api/axiosConfig";

const login = async (credentials) => {
	const response = await API.post("/auth/login", credentials);

	return response.data;
};

const registerUser = async (userData) => {
	const response = await API.post("/auth/register/user", userData);

	return response.data;
};

const registerOperator = async (operatorData) => {
	const response = await API.post("/auth/register/operator", operatorData);

	return response.data;
};

const forgotPassword = async (email) => {
	const response = await API.post("/auth/forgot-password", { email });
	return response.data;
};

const resetPassword = async ({ token, newPassword }) => {
	const response = await API.post("/auth/reset-password", { token, newPassword });
	return response.data;
};

const googleLogin = async (idToken) => {
	const response = await API.post("/auth/google", { idToken });
	return response.data;
};

export default {
	login,
	registerUser,
	registerOperator,
	forgotPassword,
	resetPassword,
	googleLogin,
};
