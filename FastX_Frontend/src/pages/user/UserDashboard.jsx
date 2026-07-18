import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Row, Col, Button, Table, Badge } from "react-bootstrap";
import { FaTicketAlt, FaCheckCircle, FaTimesCircle, FaBus, FaArrowRight } from "react-icons/fa";
import UserLayout from "../../layouts/UserLayout";
import bookingService from "../../services/bookingService";
import { useAuth } from "../../context/AuthContext";

const StatCard = ({ icon, label, value, color }) => (
    <div className="stat-card">
        <div className={`stat-icon ${color}`}>{icon}</div>
        <div className="stat-value">{value}</div>
        <div className="stat-label">{label}</div>
    </div>
);

const UserDashboard = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (user?.entityId) loadBookings();
    }, [user]);

    const loadBookings = async () => {
        try {
            const data = await bookingService.getBookingsByUser(user.entityId);
            const arr = Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : [];
            setBookings(arr);
        } catch (error) {
            console.log("Error loading bookings");
        } finally {
            setLoading(false);
        }
    };

    const confirmed = bookings.filter(b => b.bookingStatus === "CONFIRMED");
    const cancelled = bookings.filter(b => b.bookingStatus === "CANCELLED");

    const getStatusBadge = (status) => {
        const map = { CONFIRMED: "success", PENDING: "warning", CANCELLED: "danger", EXPIRED: "secondary" };
        return <Badge bg={map[status] || "secondary"}>{status}</Badge>;
    };

    return (
        <UserLayout>
            <div className="page-header">
                <div className="page-header-left">
                    <h4>Welcome back, {user?.name || user?.email?.split("@")[0]}! 👋</h4>
                    <p>Here's an overview of your travel activity</p>
                </div>
                <Button variant="primary" onClick={() => navigate("/user/search")}>
                    <FaBus style={{ marginRight: 6 }} /> Book a Bus
                </Button>
            </div>

            {/* Stats */}
            <Row className="g-3 mb-4">
                <Col md={4}>
                    <StatCard icon={<FaTicketAlt />} label="Total Bookings" value={bookings.length} color="indigo" />
                </Col>
                <Col md={4}>
                    <StatCard icon={<FaCheckCircle />} label="Confirmed" value={confirmed.length} color="emerald" />
                </Col>
                <Col md={4}>
                    <StatCard icon={<FaTimesCircle />} label="Cancelled" value={cancelled.length} color="rose" />
                </Col>
            </Row>

            {/* Quick Actions */}
            <div className="section-title" style={{ marginBottom: 16 }}>Quick Actions</div>
            <div style={{ display: "flex", gap: 12, marginBottom: 32, flexWrap: "wrap" }}>
                {[
                    { label: "Search Buses", path: "/user/search", variant: "primary" },
                    { label: "My Bookings", path: "/user/bookings", variant: "secondary" },
                    { label: "My Profile", path: "/user/profile", variant: "secondary" },
                ].map(a => (
                    <Button key={a.label} variant={a.variant} onClick={() => navigate(a.path)}>
                        {a.label} {a.variant === "primary" && <FaArrowRight style={{ marginLeft: 6, fontSize: "0.8rem" }} />}
                    </Button>
                ))}
            </div>

            {/* Recent Bookings */}
            {loading ? (
                <p className="text-muted">Loading bookings...</p>
            ) : bookings.length > 0 ? (
                <>
                    <div className="section-title" style={{ marginBottom: 16 }}>Recent Bookings</div>
                    <div className="table-responsive">
                        <Table className="table-user text-center align-middle mb-0">
                            <thead>
                                <tr>
                                    <th>Booking ID</th>
                                    <th>Bus</th>
                                    <th>Date</th>
                                    <th>Seats</th>
                                    <th>Amount</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {bookings.slice(0, 5).map(b => (
                                    <tr key={b.bookingId} onClick={() => navigate("/user/bookings")}
                                        style={{ cursor: "pointer" }}>
                                        <td><strong>{b.bookingId}</strong></td>
                                        <td>{b.busName || `Bus ${b.busId}`}</td>
                                        <td>{b.bookingDate?.substring(0, 10)}</td>
                                        <td>{b.numberOfSeats}</td>
                                        <td><strong>₹{b.totalAmount}</strong></td>
                                        <td>{getStatusBadge(b.bookingStatus)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    </div>
                </>
            ) : (
                <div className="empty-state">
                    <div className="empty-state-icon">🎫</div>
                    <h6>No bookings yet</h6>
                    <p>Start planning your journey today.</p>
                    <Button variant="primary" onClick={() => navigate("/user/search")}>
                        <FaBus style={{ marginRight: 6 }} /> Search Buses
                    </Button>
                </div>
            )}
        </UserLayout>
    );
};

export default UserDashboard;