import { useEffect, useState } from "react";
import { Badge, Form, Button, Row, Col, Card } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { FaUsers, FaTrash } from "react-icons/fa";

import AdminLayout from "../../layouts/AdminLayout";
import busService from "../../services/busService";

const ManageBuses = () => {
    const navigate = useNavigate();
    const [buses, setBuses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState("");
    const [typeFilter, setTypeFilter] = useState("ALL");

    useEffect(() => {
        loadBuses();
    }, []);

    const loadBuses = async () => {
        try {
            const data = await busService.getAllBuses();
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

    const handleDelete = async (busId, busName) => {
        if (!window.confirm(`Delete bus "${busName}"? All associated seats will also be removed.`)) return;
        try {
            await busService.deleteBus(busId);
            toast.success("Bus deleted successfully");
            loadBuses();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to delete bus");
        }
    };

    const filtered = buses.filter((b) => {
        const q = search.toLowerCase();
        const matchSearch = !q || b.busName?.toLowerCase().includes(q) || b.origin?.toLowerCase().includes(q) || b.destination?.toLowerCase().includes(q) || b.busNumber?.toLowerCase().includes(q);
        const matchType = typeFilter === "ALL" || b.busType === typeFilter;
        return matchSearch && matchType;
    });

    const isUpcoming = (bus) => bus.journeyDate && new Date(bus.journeyDate) >= new Date(new Date().toDateString());

    return (
        <AdminLayout>
            <h4>Bus Manager</h4>
            <p className="text-muted">{buses.length} total buses across all operators</p>
            <hr />

            <Row className="mb-3 g-2">
                <Col md={5}>
                    <Form.Control
                        placeholder="Search by name, route, number..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </Col>
                <Col md={4}>
                    <Form.Select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
                        <option value="ALL">All Types</option>
                        <option value="AC_SEATER">AC Seater</option>
                        <option value="NON_AC_SEATER">Non-AC Seater</option>
                        <option value="AC_SLEEPER">AC Sleeper</option>
                        <option value="NON_AC_SLEEPER">Non-AC Sleeper</option>
                        <option value="VOLVO">Volvo</option>
                    </Form.Select>
                </Col>
                <Col md={3} className="d-flex align-items-center">
                    <span className="text-muted">Showing {filtered.length} of {buses.length}</span>
                </Col>
            </Row>

            {loading ? (
                <p className="text-muted">Loading buses...</p>
            ) : filtered.length === 0 ? (
                <p className="text-muted">No buses found.</p>
            ) : (
                <Row className="g-4">
                    {filtered.map(bus => (
                        <Col key={bus.busId} xs={12} md={6} xl={4}>
                            <Card className="h-100 shadow-sm border-0" style={{ borderRadius: 12, overflow: "hidden" }}>
                                {/* Card Header */}
                                <div style={{ background: "#f8fafc", padding: "16px 20px", borderBottom: "1px solid #e2e8f0" }}>
                                    <div className="d-flex justify-content-between align-items-start">
                                        <div>
                                            <h6 className="mb-1" style={{ fontWeight: 700, color: "#1e293b" }}>{bus.busName}</h6>
                                            <span style={{ fontSize: "0.75rem", color: "#64748b", background: "#f1f5f9", padding: "2px 6px", borderRadius: 4 }}>
                                                ID: {bus.busId} | {bus.busNumber}
                                            </span>
                                        </div>
                                        <Badge bg={isUpcoming(bus) ? "success" : "secondary"}>
                                            {isUpcoming(bus) ? "Upcoming" : "Completed"}
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
                                            </div>
                                            <div className="text-center px-2" style={{ color: "#94a3b8" }}>
                                                <span>→</span>
                                            </div>
                                            <div className="text-end">
                                                <span style={{ fontSize: "0.75rem", color: "#94a3b8", fontWeight: 700, textTransform: "uppercase" }}>Destination</span>
                                                <div style={{ fontWeight: 600, color: "#334155" }}>{bus.destination}</div>
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
                                            <div style={{ fontWeight: 700, color: "#4f46e5" }}>₹{bus.fare}</div>
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
                                    <Button variant="outline-info" size="sm" className="flex-grow-1"
                                        onClick={() => navigate(`/admin/bus/${bus.busId}/passengers`)}>
                                        <FaUsers style={{ marginRight: 6 }} /> Passengers
                                    </Button>
                                    <Button variant="outline-danger" size="sm"
                                        onClick={() => handleDelete(bus.busId, bus.busName)}>
                                        <FaTrash style={{ marginRight: 6 }} /> Delete
                                    </Button>
                                </div>
                            </Card>
                        </Col>
                    ))}
                </Row>
            )}
        </AdminLayout>
    );
};

export default ManageBuses;
