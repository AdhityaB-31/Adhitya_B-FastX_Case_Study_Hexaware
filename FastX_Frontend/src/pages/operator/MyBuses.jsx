import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { Button, Badge, Row, Col, Card } from "react-bootstrap";
import { FaPlus, FaEdit, FaChair, FaUsers, FaTrash } from "react-icons/fa";
import OperatorLayout from "../../layouts/OperatorLayout";
import busService from "../../services/busService";
import { useAuth } from "../../context/AuthContext";

const MyBuses = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [buses, setBuses] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => { loadBuses(); }, []);

    const loadBuses = async () => {
        try {
            if (!user?.entityId) return;
            const data = await busService.getBusesByOperator(user.entityId);
            const busArray = Array.isArray(data)
                ? data
                : Array.isArray(data?.content)
                ? data.content
                : [];
            setBuses(busArray);
        } catch {
            toast.error("Failed to load buses");
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (busId, busName) => {
        if (!window.confirm(`Delete "${busName}"? This action cannot be undone.`)) return;
        try {
            await busService.deleteBus(busId);
            toast.success("Bus deleted successfully");
            loadBuses();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to delete bus");
        }
    };

    return (
        <OperatorLayout>
            <div className="page-header">
                <div className="page-header-left">
                    <h4>My Buses</h4>
                    <p>{buses.length} buses in your fleet</p>
                </div>
                <Button variant="primary" onClick={() => navigate("/operator/add-bus")}>
                    <FaPlus style={{ marginRight: 6 }} /> Add New Bus
                </Button>
            </div>

            {loading ? (
                <p className="text-muted">Loading buses...</p>
            ) : buses.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-state-icon">🚌</div>
                    <h6>No buses added yet</h6>
                    <p>Start by adding your first bus to the fleet.</p>
                    <Button variant="primary" onClick={() => navigate("/operator/add-bus")}>
                        <FaPlus style={{ marginRight: 6 }} /> Add Bus
                    </Button>
                </div>
            ) : (
                <Row className="g-4">
                    {buses.map(bus => {
                        const isUpcoming = new Date(bus.journeyDate) >= new Date(new Date().toDateString());
                        return (
                            <Col key={bus.busId} xs={12} md={6} xl={4}>
                                <Card className="h-100 shadow-sm border-0" style={{ borderRadius: 12, overflow: "hidden" }}>
                                    {/* Card Header */}
                                    <div style={{ background: "#f8fafc", padding: "16px 20px", borderBottom: "1px solid #e2e8f0" }}>
                                        <div className="d-flex justify-content-between align-items-start">
                                            <div>
                                                <h6 className="mb-1" style={{ fontWeight: 700, color: "#1e293b" }}>{bus.busName}</h6>
                                                <span style={{ fontSize: "0.75rem", fontFamily: "monospace", color: "#64748b", background: "#f1f5f9", padding: "2px 6px", borderRadius: 4 }}>
                                                    {bus.busNumber}
                                                </span>
                                            </div>
                                            <Badge bg={isUpcoming ? "success" : "secondary"}>
                                                {isUpcoming ? "Upcoming" : "Completed"}
                                            </Badge>
                                        </div>
                                    </div>

                                    {/* Card Body */}
                                    <Card.Body style={{ padding: "20px" }}>
                                        {/* Route details */}
                                        <div className="mb-3">
                                            <div className="d-flex justify-content-between align-items-center">
                                                <div>
                                                    <span style={{ fontSize: "0.75rem", color: "#94a3b8", fontWeight: 700, textTransform: "uppercase" }}>Origin</span>
                                                    <div style={{ fontWeight: 600, color: "#334155" }}>{bus.origin}</div>
                                                    <div style={{ fontSize: "0.85rem", color: "#64748b" }}>{bus.departureTime}</div>
                                                </div>
                                                <div className="text-center px-2" style={{ color: "#94a3b8" }}>
                                                    <span>→</span>
                                                </div>
                                                <div className="text-end">
                                                    <span style={{ fontSize: "0.75rem", color: "#94a3b8", fontWeight: 700, textTransform: "uppercase" }}>Destination</span>
                                                    <div style={{ fontWeight: 600, color: "#334155" }}>{bus.destination}</div>
                                                    <div style={{ fontSize: "0.85rem", color: "#64748b" }}>{bus.arrivalTime}</div>
                                                </div>
                                            </div>
                                        </div>

                                        {/* Details row */}
                                        <Row className="g-2 text-center mb-3 py-2 bg-light rounded" style={{ fontSize: "0.8rem" }}>
                                            <Col xs={4}>
                                                <div className="text-muted" style={{ fontSize: "0.7rem", fontWeight: 700, textTransform: "uppercase" }}>Date</div>
                                                <div style={{ fontWeight: 600, color: "#334155" }}>{bus.journeyDate}</div>
                                            </Col>
                                            <Col xs={4}>
                                                <div className="text-muted" style={{ fontSize: "0.7rem", fontWeight: 700, textTransform: "uppercase" }}>Seats</div>
                                                <div style={{ fontWeight: 600, color: "#334155" }}>{bus.totalSeats}</div>
                                            </Col>
                                            <Col xs={4}>
                                                <div className="text-muted" style={{ fontSize: "0.7rem", fontWeight: 700, textTransform: "uppercase" }}>Fare</div>
                                                <div style={{ fontWeight: 700, color: "#059669" }}>₹{bus.fare}</div>
                                            </Col>
                                        </Row>

                                        {/* Bus type & amenities */}
                                        <div className="mb-2">
                                            <Badge bg="info" className="me-1" style={{ fontSize: "0.72rem" }}>
                                                {bus.busType?.replace(/_/g, " ")}
                                            </Badge>
                                        </div>

                                        {bus.amenityNames?.length > 0 && (
                                            <div style={{ fontSize: "0.75rem", color: "#64748b", textOverflow: "ellipsis", overflow: "hidden", whiteSpace: "nowrap" }} title={bus.amenityNames.join(", ")}>
                                                <strong>Amenities:</strong> {bus.amenityNames.join(", ")}
                                            </div>
                                        )}
                                    </Card.Body>

                                    {/* Card Footer Actions */}
                                    <div style={{ background: "#f8fafc", padding: "12px 20px", borderTop: "1px solid #e2e8f0" }} className="d-flex gap-2">
                                        <Button variant="outline-primary" size="sm" className="flex-grow-1"
                                            onClick={() => navigate(`/operator/seats/${bus.busId}`)}>
                                            <FaChair style={{ marginRight: 6 }} /> Seats
                                        </Button>
                                        <Button variant="outline-info" size="sm" className="flex-grow-1"
                                            onClick={() => navigate(`/operator/bus/${bus.busId}/passengers`)}>
                                            <FaUsers style={{ marginRight: 6 }} /> Passengers
                                        </Button>
                                        <Button variant="outline-secondary" size="sm"
                                            onClick={() => navigate(`/operator/update-bus/${bus.busId}`)}>
                                            <FaEdit />
                                        </Button>
                                        <Button variant="outline-danger" size="sm"
                                            onClick={() => handleDelete(bus.busId, bus.busName)}>
                                            <FaTrash />
                                        </Button>
                                    </div>
                                </Card>
                            </Col>
                        );
                    })}
                </Row>
            )}
        </OperatorLayout>
    );
};

export default MyBuses;
