import API from "../api/axiosConfig";

const getDashboardStats = async () => {
	const response = await API.get("/admin/dashboard/stats");

	return response.data;
};

const approveOperator = async (operatorId) => {
	const response = await API.put(`/admin/operators/${operatorId}/approve`);
	return response.data;
};

const toggleUserStatus = async (userId, reason) => {
	const response = await API.put(`/admin/users/${userId}/toggle-status`, null, {
		params: reason ? { reason } : {},
	});
	return response.data;
};

const toggleOperatorStatus = async (operatorId, reason) => {
	const response = await API.put(`/admin/operators/${operatorId}/toggle-status`, null, {
		params: reason ? { reason } : {},
	});
	return response.data;
};

export default {
	getDashboardStats,
	approveOperator,
	toggleUserStatus,
	toggleOperatorStatus,
};
