import { useEffect, useState } from "react";
import { Form, Button, Table, Badge, Modal } from "react-bootstrap";
import { toast } from "react-toastify";

import AdminLayout from "../../layouts/AdminLayout";
import userService from "../../services/userService";
import adminService from "../../services/adminService";

const ManageUsers = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState("");

    // Modal states
    const [showModal, setShowModal] = useState(false);
    const [modalAction, setModalAction] = useState(""); // "activate" or "deactivate"
    const [targetUser, setTargetUser] = useState(null);
    const [deactivateReason, setDeactivateReason] = useState("");

    useEffect(() => { loadUsers(); }, []);

    const loadUsers = async () => {
        try {
            const data = await userService.getAllUsers();
            const arr = Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : [];
            setUsers(arr);
        } catch (error) {
            toast.error("Failed to load users");
        } finally {
            setLoading(false);
        }
    };

    const handleToggleStatus = (id, name, isActive) => {
        setTargetUser({ id, fullName: name });
        setModalAction(isActive ? "deactivate" : "activate");
        setDeactivateReason("");
        setShowModal(true);
    };

    const confirmToggleStatus = async () => {
        if (!targetUser) return;
        const isDeactivate = modalAction === "deactivate";
        const reason = isDeactivate ? deactivateReason.trim() : "";

        if (isDeactivate && !reason) {
            toast.error("Reason is mandatory to deactivate a user.");
            return;
        }

        try {
            await adminService.toggleUserStatus(targetUser.id, reason);
            toast.success(`User ${isDeactivate ? "deactivated" : "activated"} successfully`);
            setShowModal(false);
            loadUsers();
        } catch (error) {
            toast.error(error.response?.data?.message || `Unable to ${modalAction} user`);
        }
    };

    const handleDelete = async (id, name) => {
        if (!window.confirm(`Are you sure you want to permanently delete user "${name || id}"?`)) return;
        try {
            await userService.deleteUser(id);
            toast.success("User deleted successfully");
            loadUsers();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to delete user");
        }
    };

    const filtered = users.filter(u => {
        const q = search.toLowerCase();
        return !q || u.fullName?.toLowerCase().includes(q) || u.email?.toLowerCase().includes(q) || u.phoneNumber?.includes(q);
    });

    return (
        <AdminLayout>
            <h4>Manage Users</h4>
            <p className="text-muted">{users.length} registered users</p>
            <hr />

            <Form.Control
                placeholder="Search by name, email, phone..."
                value={search}
                onChange={e => setSearch(e.target.value)}
                className="mb-3"
                style={{ maxWidth: 340 }}
            />

            {loading ? (
                <p className="text-muted">Loading users...</p>
            ) : filtered.length === 0 ? (
                <p className="text-muted">No users found.</p>
            ) : (
                <Table responsive className="table-admin text-center align-middle">
                    <thead className="text-center">
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Gender</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtered.map(user => (
                            <tr key={user.userId}>
                                <td>{user.userId}</td>
                                <td>{user.fullName || "—"}</td>
                                <td>{user.email}</td>
                                <td>{user.phoneNumber || "—"}</td>
                                <td>{user.gender || "—"}</td>
                                <td>
                                    <Badge bg={user.isActive ? "success" : "danger"}>
                                        {user.isActive ? "Active" : "Inactive"}
                                    </Badge>
                                </td>
                                <td>
                                    <div className="d-flex gap-1 justify-content-center">
                                        <Button 
                                            variant={user.isActive ? "warning" : "success"} 
                                            size="sm" 
                                            onClick={() => handleToggleStatus(user.userId, user.fullName, user.isActive)}
                                        >
                                            {user.isActive ? "Deactivate" : "Activate"}
                                        </Button>
                                        <Button 
                                            variant="danger" 
                                            size="sm" 
                                            onClick={() => handleDelete(user.userId, user.fullName)}
                                        >
                                            Delete
                                        </Button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </Table>
            )}

            <Modal show={showModal} onHide={() => setShowModal(false)} centered>
                <Modal.Header closeButton className={modalAction === "deactivate" ? "bg-danger text-white" : "bg-success text-white"}>
                    <Modal.Title>{modalAction === "deactivate" ? "Deactivate User Account" : "Activate User Account"}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {modalAction === "deactivate" ? (
                        <>
                            <p>Are you sure you want to deactivate user <strong>{targetUser?.fullName || targetUser?.email}</strong>? They will not be able to log in.</p>
                            <Form.Group className="mt-3">
                                <Form.Label><strong>Reason for Deactivation (Mandatory)</strong></Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    placeholder="Please provide the reason for deactivating this user account..."
                                    value={deactivateReason}
                                    onChange={(e) => setDeactivateReason(e.target.value)}
                                />
                            </Form.Group>
                        </>
                    ) : (
                        <p>Are you sure you want to activate user <strong>{targetUser?.fullName || targetUser?.email}</strong>? They will be notified by email and allowed to log in.</p>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowModal(false)}>
                        Cancel
                    </Button>
                    <Button 
                        variant={modalAction === "deactivate" ? "danger" : "success"}
                        onClick={confirmToggleStatus}
                        disabled={modalAction === "deactivate" && !deactivateReason.trim()}
                    >
                        {modalAction === "deactivate" ? "Deactivate User" : "Activate User"}
                    </Button>
                </Modal.Footer>
            </Modal>
        </AdminLayout>
    );
};

export default ManageUsers;
