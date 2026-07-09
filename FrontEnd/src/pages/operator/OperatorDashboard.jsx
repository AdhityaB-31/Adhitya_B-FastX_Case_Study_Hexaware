import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Row, Col, Card, Button, Table, Badge } from "react-bootstrap";
import { FaBus, FaRoute, FaCalendarCheck, FaPlus } from "react-icons/fa";
import { toast } from "react-toastify";
import OperatorLayout from "../../layouts/OperatorLayout";
import busService from "../../services/busService";
import { useAuth } from "../../context/AuthContext";

const StatCard = ({ icon, label, value, color }) => (
    <div className="stat-card">
        <div className={`stat-icon ${color}`}>{icon}</div>
        <div className="stat-value">{value}</div>
        <div className="stat-label">{label}</div>
    </div>
);

const OperatorDashboard = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [buses, setBuses] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => { loadBuses(); }, []);

    const loadBuses = async () => {
        try {
            if (!user?.entityId) return;
            const data = await busService.getBusesByOperator(user.entityId);
            // Normalise: handle array, paginated { content: [] }, or null
            const busArray = Array.isArray(data)
                ? data
                : Array.isArray(data?.content)
                ? data.content
                : [];
            setBuses(busArray);
        } catch (error) {
            toast.error("Failed to load buses");
        } finally {
            setLoading(false);
        }
    };

    const activeBuses = buses.filter(b => new Date(b.journeyDate) >= new Date(new Date().toDateString()));
    const uniqueRoutes = new Set(buses.map(b => b.origin + "-" + b.destination)).size;

    return (
        <OperatorLayout>
            <div className="page-header">
                <div className="page-header-left">
                    <h4>Operator Dashboard</h4>
                    <p>Manage your fleet and track performance</p>
                </div>
                <Button variant="primary" onClick={() => navigate("/operator/add-bus")}>
                    <FaPlus style={{ marginRight: 6 }} /> Add New Bus
                </Button>
            </div>

            <Row className="g-3 mb-4">
                <Col md={4}>
                    <StatCard icon={<FaBus />} label="Total Buses" value={buses.length} color="indigo" />
                </Col>
                <Col md={4}>
                    <StatCard icon={<FaCalendarCheck />} label="Upcoming Buses" value={activeBuses.length} color="emerald" />
                </Col>
                <Col md={4}>
                    <StatCard icon={<FaRoute />} label="Unique Routes" value={uniqueRoutes} color="amber" />
                </Col>
            </Row>

            <div className="section-title">
                <img src="/FastX-Logo.png" alt="FastX" style={{ height: "18px", width: "auto", marginRight: 8, verticalAlign: "middle" }} /> My Fleet
            </div>

            {loading ? (
                <p className="text-muted">Loading buses...</p>
            ) : buses.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-state-icon">🚌</div>
                    <h6>No buses in your fleet yet</h6>
                    <p>Add your first bus to start accepting bookings.</p>
                    <Button variant="primary" onClick={() => navigate("/operator/add-bus")}>
                        <FaPlus style={{ marginRight: 6 }} /> Add Your First Bus
                    </Button>
                </div>
            ) : (
                <div className="table-responsive">
                    <Table className="table-operator text-center align-middle mb-0">
                        <thead>
                            <tr>
                                <th>Bus Name</th>
                                <th>Route</th>
                                <th>Type</th>
                                <th>Seats</th>
                                <th>Fare</th>
                                <th>Journey Date</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {buses.map(bus => {
                                const isUpcoming = new Date(bus.journeyDate) >= new Date(new Date().toDateString());
                                return (
                                    <tr key={bus.busId}>
                                        <td style={{ fontWeight: 600 }}>{bus.busName}</td>
                                        <td>{bus.origin} → {bus.destination}</td>
                                        <td>{bus.busType?.replace(/_/g, " ")}</td>
                                        <td>{bus.totalSeats}</td>
                                        <td><strong>₹{bus.fare}</strong></td>
                                        <td>{bus.journeyDate}</td>
                                        <td>
                                            <Badge bg={isUpcoming ? "success" : "secondary"}>
                                                {isUpcoming ? "Upcoming" : "Completed"}
                                            </Badge>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </Table>
                </div>
            )}
        </OperatorLayout>
    );
};

export default OperatorDashboard;