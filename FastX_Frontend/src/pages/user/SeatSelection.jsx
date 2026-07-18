import { useEffect, useState } from "react";
import { useParams, useLocation, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { Card, Button, Form, Row, Col, Badge } from "react-bootstrap";

import UserLayout from "../../layouts/UserLayout";
import busService from "../../services/busService";
import bookingService from "../../services/bookingService";
import { useAuth } from "../../context/AuthContext";

const SeatSelection = () => {
    const { busId } = useParams();
    const { state } = useLocation();
    const navigate = useNavigate();
    const { user } = useAuth();

    const [bus, setBus] = useState(state?.bus || null);
    const [seats, setSeats] = useState([]);
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [passengerDetails, setPassengerDetails] = useState({});
    const [loading, setLoading] = useState(true);
    const [booking, setBooking] = useState(false);

    useEffect(() => {
        loadData();
    }, [busId]);

    const loadData = async () => {
        try {
            const [busData, seatData] = await Promise.all([
                busService.getBusDetails(busId),
                busService.getSeatsByBus(busId),
            ]);
            setBus(busData);
            setSeats(Array.isArray(seatData) ? seatData : Array.isArray(seatData?.content) ? seatData.content : []);
        } catch (error) {
            toast.error("Failed to load bus details");
        } finally {
            setLoading(false);
        }
    };

    const getSeatBg = (seat) => {
        const isSelected = selectedSeats.some((s) => s.seatId === seat.seatId);
        if (isSelected) return "#4f46e5"; // Primary brand color
        switch (seat.seatStatus) {
            case "AVAILABLE": return "#d1fae5";
            case "BOOKED": return "#fee2e2";
            case "RESERVED": return "#fef3c7";
            default: return "#e5e7eb";
        }
    };

    const getSeatColor = (seat) => {
        const isSelected = selectedSeats.some((s) => s.seatId === seat.seatId);
        if (isSelected) return "white";
        switch (seat.seatStatus) {
            case "AVAILABLE": return "#065f46";
            case "BOOKED": return "#991b1b";
            case "RESERVED": return "#92400e";
            default: return "#1e293b";
        }
    };

    const toggleSeat = (seat) => {
        if (seat.seatStatus !== "AVAILABLE") return;
        setSelectedSeats((prev) => {
            const isSelected = prev.some((s) => s.seatId === seat.seatId);
            if (isSelected) {
                setPassengerDetails((curr) => {
                    const next = { ...curr };
                    delete next[seat.seatId];
                    return next;
                });
                return prev.filter((s) => s.seatId !== seat.seatId);
            } else {
                setPassengerDetails((curr) => ({
                    ...curr,
                    [seat.seatId]: { name: "", age: "", gender: "" },
                }));
                return [...prev, seat];
            }
        });
    };

    const handlePassengerChange = (seatId, field, value) => {
        setPassengerDetails((prev) => ({
            ...prev,
            [seatId]: {
                ...prev[seatId],
                [field]: value,
            },
        }));
    };

    const handleProceedToBook = async () => {
        if (selectedSeats.length === 0) {
            toast.warning("Please select at least one seat");
            return;
        }

        const passengers = selectedSeats.map((seat) => {
            const pd = passengerDetails[seat.seatId] || {};
            return {
                seatId: seat.seatId,
                name: pd.name || "",
                age: pd.age ? parseInt(pd.age) : 0,
                gender: pd.gender || "",
            };
        });

        for (const p of passengers) {
            if (!p.name.trim() || p.age < 1 || !p.gender) {
                toast.warning("Please fill out passenger details (Name, Age, Gender) for all selected seats");
                return;
            }
        }

        setBooking(true);
        try {
            const bookingData = {
                userId: user.entityId,
                busId: parseInt(busId),
                numberOfSeats: selectedSeats.length,
                seatIds: selectedSeats.map((s) => s.seatId),
                passengers: passengers,
            };
            const created = await bookingService.createBooking(bookingData);
            toast.success("Booking created! Proceeding to payment...");
            navigate(`/user/payment/${created.bookingId}`, {
                state: { booking: created, bus, selectedSeats },
            });
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to create booking");
        } finally {
            setBooking(false);
        }
    };

    const totalFare = selectedSeats.length * (bus?.fare || 0);
    const seatRows = seats.reduce((rows, seat, idx) => {
        if (idx % 4 === 0) rows.push([]);
        rows[rows.length - 1].push(seat);
        return rows;
    }, []);

    return (
        <UserLayout>
            <div className="page-header">
                <div className="page-header-left">
                    <h4>Select Seats</h4>
                    {bus && <p className="text-muted">{bus.busName} — {bus.origin} to {bus.destination}</p>}
                </div>
                <Button variant="secondary" size="sm" onClick={() => navigate(-1)}>Back</Button>
            </div>

            {/* Bus Details Header */}
            {bus && (
                <Card className="mb-4">
                    <Card.Body style={{ padding: "16px 24px" }}>
                        <Row className="align-items-center">
                            <Col md={4}>
                                <strong style={{ fontSize: "1.1rem" }}>{bus.busName}</strong>
                                <div className="text-muted small" style={{ marginTop: 2 }}>{bus.busType?.replace(/_/g, " ")}</div>
                            </Col>
                            <Col md={4} className="text-center">
                                <span style={{ fontWeight: 600, fontSize: "1.1rem" }}>{bus.departureTime}</span>
                                <span style={{ color: "#94a3b8", margin: "0 10px" }}>→</span>
                                <span style={{ fontWeight: 600, fontSize: "1.1rem" }}>{bus.arrivalTime}</span>
                            </Col>
                            <Col md={4} className="text-end">
                                <span className="text-muted small">Fare: </span>
                                <strong style={{ fontSize: "1.2rem", color: "#4f46e5" }}>₹{bus.fare}</strong> <span className="text-muted small">/ seat</span>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>
            )}

            <Row className="g-4">
                {/* Seat Grid */}
                <Col lg={7}>
                    <Card>
                        <Card.Body>
                            <div className="d-flex flex-wrap gap-2 mb-4 justify-content-center" style={{ fontSize: "0.82rem" }}>
                                <span style={{ background: "#d1fae5", color: "#065f46", fontWeight: "600", padding: "6px 12px", borderRadius: 8 }}>Available</span>
                                <span style={{ background: "#fee2e2", color: "#991b1b", fontWeight: "600", padding: "6px 12px", borderRadius: 8 }}>Booked</span>
                                <span style={{ background: "#fef3c7", color: "#92400e", fontWeight: "600", padding: "6px 12px", borderRadius: 8 }}>Reserved</span>
                                <span style={{ background: "#4f46e5", color: "white", fontWeight: "600", padding: "6px 12px", borderRadius: 8 }}>Selected</span>
                            </div>

                            {loading ? (
                                <p className="text-muted text-center py-4">Loading seats...</p>
                            ) : (
                                <div style={{ maxWidth: 360, margin: "0 auto" }}>
                                    <div style={{ display: "flex", flexDirection: "column", gap: "10px", alignItems: "center" }}>
                                        {seatRows.map((row, rowIdx) => (
                                            <div key={rowIdx} style={{ display: "flex", gap: "10px", alignItems: "center" }}>
                                                <span className="text-muted small text-end" style={{ width: "20px", fontWeight: 600 }}>{rowIdx + 1}</span>
                                                {row.slice(0, 2).map((seat) => {
                                                    const isSelected = selectedSeats.some(s => s.seatId === seat.seatId);
                                                    return (
                                                        <button
                                                            key={seat.seatId}
                                                            onClick={() => toggleSeat(seat)}
                                                            title={`${seat.seatNumber} (${seat.seatStatus})`}
                                                            style={{
                                                                width: "48px",
                                                                height: "44px",
                                                                background: getSeatBg(seat),
                                                                border: "1.5px solid rgba(0,0,0,0.08)",
                                                                borderRadius: "10px 10px 6px 6px",
                                                                fontSize: "0.75rem",
                                                                fontWeight: "bold",
                                                                color: getSeatColor(seat),
                                                                cursor: seat.seatStatus === "AVAILABLE" ? "pointer" : "not-allowed",
                                                                transition: "all 0.15s ease",
                                                                padding: "4px 2px",
                                                                display: "flex",
                                                                flexDirection: "column",
                                                                justifyContent: "space-between",
                                                                boxShadow: "0 2px 4px rgba(0,0,0,0.04)"
                                                            }}
                                                        >
                                                            <div style={{
                                                                height: "5px",
                                                                width: "80%",
                                                                margin: "0 auto",
                                                                background: isSelected ? "rgba(255,255,255,0.4)" : "rgba(0,0,0,0.08)",
                                                                borderRadius: "4px"
                                                            }} />
                                                            <span>{seat.seatNumber}</span>
                                                        </button>
                                                    );
                                                })}
                                                <div style={{ width: "32px" }} />
                                                {row.slice(2).map((seat) => {
                                                    const isSelected = selectedSeats.some(s => s.seatId === seat.seatId);
                                                    return (
                                                        <button
                                                            key={seat.seatId}
                                                            onClick={() => toggleSeat(seat)}
                                                            title={`${seat.seatNumber} (${seat.seatStatus})`}
                                                            style={{
                                                                width: "48px",
                                                                height: "44px",
                                                                background: getSeatBg(seat),
                                                                border: "1.5px solid rgba(0,0,0,0.08)",
                                                                borderRadius: "10px 10px 6px 6px",
                                                                fontSize: "0.75rem",
                                                                fontWeight: "bold",
                                                                color: getSeatColor(seat),
                                                                cursor: seat.seatStatus === "AVAILABLE" ? "pointer" : "not-allowed",
                                                                transition: "all 0.15s ease",
                                                                padding: "4px 2px",
                                                                display: "flex",
                                                                flexDirection: "column",
                                                                justifyContent: "space-between",
                                                                boxShadow: "0 2px 4px rgba(0,0,0,0.04)"
                                                            }}
                                                        >
                                                            <div style={{
                                                                height: "5px",
                                                                width: "80%",
                                                                margin: "0 auto",
                                                                background: isSelected ? "rgba(255,255,255,0.4)" : "rgba(0,0,0,0.08)",
                                                                borderRadius: "4px"
                                                            }} />
                                                            <span>{seat.seatNumber}</span>
                                                        </button>
                                                    );
                                                })}
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                {/* Booking Summary */}
                <Col lg={5}>
                    <Card className="position-sticky" style={{ top: 20 }}>
                        <Card.Header>Booking Summary</Card.Header>
                        <Card.Body>
                            <div className="mb-3">
                                <span className="text-muted small d-block mb-2">Selected Seats</span>
                                {selectedSeats.length === 0 ? (
                                    <span className="text-muted small">No seats selected</span>
                                ) : (
                                    <div className="d-flex flex-wrap gap-1">
                                        {selectedSeats.map(s => (
                                            <Badge bg="primary" key={s.seatId}>{s.seatNumber}</Badge>
                                        ))}
                                    </div>
                                )}
                            </div>

                            {selectedSeats.length > 0 && (
                                <div className="mb-4">
                                    <span className="text-muted small d-block mb-2">Passenger Details</span>
                                    <div style={{ maxHeight: "280px", overflowY: "auto", paddingRight: 4 }}>
                                        {selectedSeats.map(seat => (
                                            <div key={seat.seatId} className="bg-light p-2 mb-2 rounded border">
                                                <p className="text-primary fw-bold small mb-2">Seat {seat.seatNumber}</p>
                                                <Form.Control
                                                    type="text"
                                                    placeholder="Full Name"
                                                    size="sm"
                                                    className="mb-2"
                                                    value={passengerDetails[seat.seatId]?.name || ""}
                                                    onChange={(e) => handlePassengerChange(seat.seatId, "name", e.target.value)}
                                                />
                                                <div className="d-flex gap-2">
                                                    <Form.Control
                                                        type="number"
                                                        placeholder="Age"
                                                        min="1"
                                                        max="120"
                                                        size="sm"
                                                        value={passengerDetails[seat.seatId]?.age || ""}
                                                        onChange={(e) => handlePassengerChange(seat.seatId, "age", e.target.value)}
                                                    />
                                                    <Form.Select
                                                        size="sm"
                                                        value={passengerDetails[seat.seatId]?.gender || ""}
                                                        onChange={(e) => handlePassengerChange(seat.seatId, "gender", e.target.value)}
                                                    >
                                                        <option value="">Gender</option>
                                                        <option value="MALE">Male</option>
                                                        <option value="FEMALE">Female</option>
                                                        <option value="OTHER">Other</option>
                                                    </Form.Select>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            <hr />
                            
                            <div className="d-flex justify-content-between mb-2">
                                <span className="text-muted">Seats × Fare</span>
                                <span>{selectedSeats.length} × ₹{bus?.fare || 0}</span>
                            </div>
                            <div className="d-flex justify-content-between fw-bold mb-3">
                                <span>Total Amount</span>
                                <span className="text-primary fs-5">₹{totalFare}</span>
                            </div>

                            <Button 
                                variant="primary" 
                                className="w-100" 
                                disabled={selectedSeats.length === 0 || booking}
                                onClick={handleProceedToBook}
                            >
                                {booking ? "Processing..." : "Proceed to Payment"}
                            </Button>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </UserLayout>
    );
};

export default SeatSelection;
