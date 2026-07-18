import API from "../api/axiosConfig";

const getAllOperators = async () => {
    const response = await API.get("/operators");
    return response.data;
};

const deleteOperator = async (id) => {
    const response = await API.delete(`/operators/delete/${id}`);
    return response.data;
};

const getOperatorById = async (id) => {
    const response = await API.get(`/operators/${id}`);
    return response.data;
};

const getOperatorByEmail = async (email) => {
    const response = await API.get(`/operators/email/${email}`);
    return response.data;
};

const updateOperator = async (id, data) => {
    const response = await API.put(`/operators/update/${id}`, data);
    return response.data;
};

export default {
    getAllOperators,
    deleteOperator,
    getOperatorById,
    getOperatorByEmail,
    updateOperator,
};
