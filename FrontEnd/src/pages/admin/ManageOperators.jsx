import { useEffect, useState } from "react";
import { Form, Button, Table, Badge, Modal } from "react-bootstrap";
import { toast } from "react-toastify";

import AdminLayout from "../../layouts/AdminLayout";
import operatorService from "../../services/operatorService";
import adminService from "../../services/adminService";

const ManageOperators = () => {
    const [operators, setOperators] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState("");
    const [approvingId, setApprovingId] = useState(null);

    // Modal states
    const [showModal, setShowModal] = useState(false);
    const [modalAction, setModalAction] = useState(""); // "approve" or "deactivate"
    const [targetOperator, setTargetOperator] = useState(null);
    const [deactivateReason, setDeactivateReason] = useState("");

    useEffect(() => { loadOperators(); }, []);

    const loadOperators = async () => {
        try {
            const data = await operatorService.getAllOperators();
            const arr = Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : [];
            setOperators(arr);
        } catch (error) {
            toast.error("Failed to load operators");
        } finally {
            setLoading(false);
        }
    };

    const handleToggleStatus = (id, name, isActive) => {
        setTargetOperator({ id, companyName: name });
        setModalAction(isActive ? "deactivate" : "approve");
        setDeactivateReason("");
        setShowModal(true);
    };

    const confirmToggleStatus = async () => {
        if (!targetOperator) return;

        if (modalAction === "approve") {
            setApprovingId(targetOperator.id);
            setShowModal(false);
            try {
                await adminService.approveOperator(targetOperator.id);
                toast.success("Operator approved and notification email sent!");
                loadOperators();
            } catch (error) {
                toast.error(error.response?.data?.message || "Failed to approve operator");
            } finally {
                setApprovingId(null);
            }
        } else {
            const reason = deactivateReason.trim();
            if (!reason) {
                toast.error("Reason is mandatory to deactivate an operator.");
                return;
            }
            try {
                await adminService.toggleOperatorStatus(targetOperator.id, reason);
                toast.success("Operator deactivated successfully");
                setShowModal(false);
                loadOperators();
            } catch (error) {
                toast.error(error.response?.data?.message || "Unable to deactivate operator");
            }
        }
    };

    const handleDelete = async (id, name) => {
        if (!window.confirm(`Are you sure you want to permanently delete operator "${name || id}"? All their buses and schedules will also be removed.`)) return;
        try {
            await operatorService.deleteOperator(id);
            toast.success("Operator deleted successfully");
            loadOperators();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to delete operator");
        }
    };

    const filtered = operators.filter(op => {
        const q = search.toLowerCase();
        return !q || op.companyName?.toLowerCase().includes(q) || op.email?.toLowerCase().includes(q) || op.phoneNumber?.includes(q);
    });

    return (
        <AdminLayout>
            <h4>Manage Operators</h4>
            <p className="text-muted">{operators.length} registered operators</p>
            <hr />

            <Form.Control
                placeholder="Search by company, email, phone..."
                value={search}
                onChange={e => setSearch(e.target.value)}
                className="mb-3"
                style={{ maxWidth: 340 }}
            />

            {loading ? (
                <p className="text-muted">Loading operators...</p>
            ) : filtered.length === 0 ? (
                <p className="text-muted">No operators found.</p>
            ) : (
                <Table responsive className="table-admin text-center align-middle">
                    <thead className="text-center">
                        <tr>
                            <th>ID</th>
                            <th>Company Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Address</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtered.map(op => (
                            <tr key={op.operatorId}>
                                <td>{op.operatorId}</td>
                                <td>{op.companyName || "—"}</td>
                                <td>{op.email}</td>
                                <td>{op.phoneNumber || "—"}</td>
                                <td>{op.address ? op.address.substring(0, 30) + (op.address.length > 30 ? "..." : "") : "—"}</td>
                                <td>
                                    {op.isActive ? (
                                        <Badge bg="success">Approved</Badge>
                                    ) : (
                                        <Badge bg="warning">Pending Approval</Badge>
                                    )}
                                </td>
                                <td>
                                    <div className="d-flex gap-1 justify-content-center">
                                        <Button
                                            variant={op.isActive ? "warning" : "success"}
                                            size="sm"
                                            disabled={approvingId === op.operatorId}
                                            onClick={() => handleToggleStatus(op.operatorId, op.companyName, op.isActive)}
                                        >
                                            {op.isActive ? "Deactivate" : (approvingId === op.operatorId ? "Approving..." : "Approve")}
                                        </Button>
                                        <Button
                                            variant="danger"
                                            size="sm"
                                            onClick={() => handleDelete(op.operatorId, op.companyName)}
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
                    <Modal.Title>{modalAction === "deactivate" ? "Deactivate Operator Account" : "Approve Operator Account"}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {modalAction === "deactivate" ? (
                        <>
                            <p>Are you sure you want to deactivate operator <strong>{targetOperator?.companyName}</strong>? They will not be able to log in, and their buses/schedules will be suspended.</p>
                            <Form.Group className="mt-3">
                                <Form.Label><strong>Reason for Deactivation (Mandatory)</strong></Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    placeholder="Please provide the reason for deactivating this operator account..."
                                    value={deactivateReason}
                                    onChange={(e) => setDeactivateReason(e.target.value)}
                                />
                            </Form.Group>
                        </>
                    ) : (
                        <p>Are you sure you want to approve operator <strong>{targetOperator?.companyName}</strong>? They will be notified by email and allowed to log in and set up schedules.</p>
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
                        {modalAction === "deactivate" ? "Deactivate Operator" : "Approve Operator"}
                    </Button>
                </Modal.Footer>
            </Modal>
        </AdminLayout>
    );
};

export default ManageOperators;
