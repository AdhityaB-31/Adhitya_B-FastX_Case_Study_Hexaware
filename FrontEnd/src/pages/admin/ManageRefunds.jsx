import { useEffect, useState } from "react";
import { Button, Table, Badge } from "react-bootstrap";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import AdminLayout from "../../layouts/AdminLayout";
import OperatorLayout from "../../layouts/OperatorLayout";
import refundService from "../../services/refundService";

const ManageRefunds = () => {
    const { user } = useAuth();
    const [refunds, setRefunds] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState("ALL");
    const [approving, setApproving] = useState(null);

    const isAdmin = user?.role === "ADMIN";
    const Layout = isAdmin ? AdminLayout : OperatorLayout;

    useEffect(() => {
        if (user?.entityId) {
            loadRefunds();
        }
    }, [user]);

    const loadRefunds = async () => {
        try {
            let data;
            if (isAdmin) {
                data = await refundService.getAllRefunds();
            } else {
                data = await refundService.getRefundsByOperator(user.entityId);
            }
            const arr = Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : [];
            setRefunds(arr);
        } catch {
            toast.error("Failed to load refunds");
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (id) => {
        if (approving === id) return;
        setApproving(id);
        try {
            await refundService.approveRefund(id);
            toast.success("Refund approved!");
            loadRefunds();
        } catch (error) {
            toast.error(error.response?.data?.message || "Unable to approve refund");
        } finally {
            setApproving(null);
        }
    };

    const handleReject = async (id) => {
        if (!window.confirm("Reject this refund request?")) return;
        try {
            await refundService.rejectRefund(id);
            toast.success("Refund rejected");
            loadRefunds();
        } catch (error) {
            toast.error(error.response?.data?.message || "Unable to reject refund");
        }
    };

    const filtered = filter === "ALL" ? refunds : refunds.filter(r => r.refundStatus === filter);
    const pending = refunds.filter(r => r.refundStatus === "PENDING").length;

    const getStatusBadge = (status) => {
        const map = { PENDING: "warning", APPROVED: "success", REJECTED: "danger", PROCESSED: "info" };
        return <Badge bg={map[status] || "secondary"}>{status}</Badge>;
    };

    const FILTERS = ["ALL", "PENDING", "APPROVED", "REJECTED", "PROCESSED"];

    return (
        <Layout>
            <h4>{isAdmin ? "Refund History" : "Refund Approvals"}</h4>
            <p className="text-muted">{pending > 0 ? `${pending} pending approvals` : `${refunds.length} total refunds`}</p>
            <hr />

            <div className="mb-3">
                {FILTERS.map(s => (
                    <Button
                        key={s}
                        variant={filter === s ? "primary" : "outline-secondary"}
                        size="sm"
                        className="me-2 mb-1"
                        onClick={() => setFilter(s)}
                    >
                        {s} ({s === "ALL" ? refunds.length : refunds.filter(r => r.refundStatus === s).length})
                    </Button>
                ))}
            </div>

            {loading ? (
                <p className="text-muted">Loading refunds...</p>
            ) : filtered.length === 0 ? (
                <p className="text-muted">No refunds found.</p>
            ) : (
                <Table responsive className={`${isAdmin ? "table-admin" : "table-operator"} text-center align-middle`}>
                    <thead className="text-center">
                        <tr>
                            <th>Refund ID</th>
                            <th>Booking ID</th>
                            <th>Amount</th>
                            <th>Date</th>
                            <th>Reason</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtered.map(r => (
                            <tr key={r.refundId}>
                                <td>{r.refundId}</td>
                                <td>{r.bookingId}</td>
                                <td>Rs. {r.refundAmount}</td>
                                <td>{r.refundDate ? r.refundDate.replace("T", " ").substring(0, 16) : "-"}</td>
                                <td>{r.reason || "-"}</td>
                                <td>{getStatusBadge(r.refundStatus)}</td>
                                <td>
                                    {r.refundStatus === "PENDING" && !isAdmin ? (
                                        <div className="d-flex gap-1 justify-content-center">
                                            <Button variant="success" size="sm" disabled={approving === r.refundId} onClick={() => handleApprove(r.refundId)}>
                                                {approving === r.refundId ? "Approving..." : "Approve"}
                                            </Button>
                                            <Button variant="danger" size="sm" onClick={() => handleReject(r.refundId)}>
                                                Reject
                                            </Button>
                                        </div>
                                    ) : (
                                        <span className="text-muted small">
                                            {r.refundStatus === "PENDING" ? "Pending Operator" : "No action"}
                                        </span>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </Table>
            )}
        </Layout>
    );
};

export default ManageRefunds;
