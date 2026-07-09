import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { Button, Table, Badge } from "react-bootstrap";
import { FaBus, FaPlus } from "react-icons/fa";
import UserLayout from "../../layouts/UserLayout";
import bookingService from "../../services/bookingService";
import DownloadTicketButton from "../../components/ticket/DownloadTicketButton";
import { useAuth } from "../../context/AuthContext";

const FILTERS = ["ALL", "PENDING", "CONFIRMED", "CANCELLED", "EXPIRED"];

const getStatusBadge = (status) => {
    const map = { CONFIRMED: "success", PENDING: "warning", CANCELLED: "danger", EXPIRED: "secondary" };
    return <Badge bg={map[status] || "secondary"}>{status}</Badge>;
};

const getRefundBadge = (refundStatus) => {
    const map = { PENDING: "warning", APPROVED: "info", REJECTED: "danger", PROCESSED: "success" };
    return <Badge bg={map[refundStatus] || "secondary"}>Refund: {refundStatus}</Badge>;
};

const BookingHistory = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState("ALL");
    const [cancelling, setCancelling] = useState(null);

    useEffect(() => {
        if (user?.entityId) loadBookings();
    }, [user]);

    const loadBookings = async () => {
        try {
            const data = await bookingService.getBookingsByUser(user.entityId);
            const arr = Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : [];
            setBookings(arr.sort((a, b) => b.bookingId - a.bookingId));
        } catch {
            toast.error("Failed to load bookings");
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = async (bookingId, isConfirmed) => {
        let reason = "";
        if (isConfirmed) {
            reason = window.prompt("Please enter the reason for cancelling your booking and requesting a refund:");
            if (reason === null) return; // user cancelled the prompt
            if (!reason.trim()) {
                toast.warning("Cancellation aborted: A reason is required for refunds.");
                return;
            }
        } else {
            const msg = "Cancel this booking? No payment was made, so no refund is needed.";
            if (!window.confirm(msg)) return;
        }

        setCancelling(bookingId);
        try {
            await bookingService.cancelBooking(bookingId, reason);
            toast.success(isConfirmed
                ? "Booking cancelled! Refund request raised."
                : "Booking cancelled successfully.");
            loadBookings();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to cancel booking");
        } finally {
            setCancelling(null);
        }
    };

    const count = (s) => s === "ALL" ? bookings.length : bookings.filter(b => b.bookingStatus === s).length;
    const filtered = filter === "ALL" ? bookings : bookings.filter(b => b.bookingStatus === filter);

    return (
        <UserLayout>
            <div className="page-header">
                <div className="page-header-left">
                    <h4>My Bookings</h4>
                    <p>{bookings.length} total bookings found</p>
                </div>
                <Button variant="primary" onClick={() => navigate("/user/search")}>
                    <FaPlus style={{ marginRight: 6 }} /> Book New Trip
                </Button>
            </div>

            {/* Filter Tabs */}
            <div className="filter-tabs">
                {FILTERS.map(s => (
                    <button key={s} className={`filter-tab ${filter === s ? "active" : ""}`}
                        onClick={() => setFilter(s)}>
                        {s} <span style={{ opacity: 0.7 }}>({count(s)})</span>
                    </button>
                ))}
            </div>

            {loading ? (
                <p className="text-muted">Loading bookings...</p>
            ) : filtered.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-state-icon">🎫</div>
                    <h6>No bookings found</h6>
                    <p>Try a different filter or book a new trip.</p>
                    <Button variant="primary" onClick={() => navigate("/user/search")}>
                        <FaBus style={{ marginRight: 6 }} /> Search Buses
                    </Button>
                </div>
            ) : (
                <div className="table-responsive">
                    <Table className="table-user text-center align-middle mb-0">
                        <thead>
                            <tr>
                                <th>Booking ID</th>
                                <th>Bus</th>
                                <th>Booked On</th>
                                <th>Seats</th>
                                <th>Amount</th>
                                <th>Status</th>
                                <th>Refund</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filtered.map(booking => {
                                const isCancellable = booking.bookingStatus === "PENDING" || booking.bookingStatus === "CONFIRMED";
                                const isConfirmedBooking = booking.bookingStatus === "CONFIRMED";
                                const isCancellingThis = cancelling === booking.bookingId;

                                return (
                                    <tr key={booking.bookingId}>
                                        <td><strong>{booking.bookingId}</strong></td>
                                        <td>{booking.busName || `Bus ${booking.busId}`}</td>
                                        <td style={{ color: "#64748b" }}>{booking.bookingDate?.substring(0, 10)}</td>
                                        <td>{booking.numberOfSeats}</td>
                                        <td><strong>₹{booking.totalAmount}</strong></td>
                                        <td>{getStatusBadge(booking.bookingStatus)}</td>
                                        <td>
                                            {booking.refundStatus ? (
                                                <div>
                                                    {getRefundBadge(booking.refundStatus)}
                                                    <div style={{ fontSize: "0.75rem", color: "#64748b", marginTop: 4 }}>
                                                        ₹{booking.refundAmount}
                                                        {booking.refundStatus === "PENDING" && <div>Pending approval (1–3 days)</div>}
                                                        {booking.refundStatus === "APPROVED" && <div>Will reflect in 5–7 days</div>}
                                                    </div>
                                                </div>
                                            ) : booking.bookingStatus === "CANCELLED" ? (
                                                <span style={{ fontSize: "0.8rem", color: "#94a3b8" }}>No refund</span>
                                            ) : "—"}
                                        </td>
                                        <td>
                                            <div style={{ display: "flex", gap: 6, justifyContent: "center", flexWrap: "wrap" }}>
                                                {booking.bookingStatus === "CONFIRMED" && (
                                                    <DownloadTicketButton bookingId={booking.bookingId} booking={booking} variant="icon" />
                                                )}
                                                {booking.bookingStatus === "PENDING" && (
                                                    <Button variant="success" size="sm"
                                                        onClick={() => navigate(`/user/payment/${booking.bookingId}`)}>
                                                        Pay Now
                                                    </Button>
                                                )}
                                                {isCancellable && (
                                                    <Button variant="danger" size="sm" disabled={isCancellingThis}
                                                        onClick={() => handleCancel(booking.bookingId, isConfirmedBooking)}>
                                                        {isCancellingThis ? "..." : "Cancel"}
                                                    </Button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </Table>
                </div>
            )}
        </UserLayout>
    );
};

export default BookingHistory;
