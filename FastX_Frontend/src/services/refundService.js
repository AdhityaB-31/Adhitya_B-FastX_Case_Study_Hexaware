import API from "../api/axiosConfig";

const getAllRefunds = async () => {
    const response = await API.get("/refunds/getall");
    return response.data;
};

const approveRefund = async (id) => {
    const response = await API.put(`/refunds/${id}/approve`);
    return response.data;
};

const rejectRefund = async (id) => {
    const response = await API.put(`/refunds/${id}/reject`);
    return response.data;
};

const getRefundsByOperator = async (operatorId) => {
    const response = await API.get(`/refunds/operator/${operatorId}`);
    return response.data;
};

export default {
    getAllRefunds,
    approveRefund,
    rejectRefund,
    getRefundsByOperator,
};
