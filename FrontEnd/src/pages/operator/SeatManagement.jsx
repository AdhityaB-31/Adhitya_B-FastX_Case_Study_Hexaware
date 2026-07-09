import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { Card, Button, Badge, Row } from "react-bootstrap";

import OperatorLayout from "../../layouts/OperatorLayout";
import busService from "../../services/busService";

const SeatManagement = () => {
    const { busId } = useParams();
    const navigate = useNavigate();
    const [seats, setSeats] = useState([]);
    const [bus, setBus] = useState(null);
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [loading, setLoading] = useState(true);

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
            toast.error("Failed to load seat data");
        } finally {
            setLoading(false);
        }
    };

    const getSeatBg = (seat) => {
        if (selectedSeats.includes(seat.seatId)) return "#4f46e5";
        switch (seat.seatStatus) {
            case "AVAILABLE": return "#d1fae5";
            case "BOOKED": return "#fee2e2";
            case "RESERVED": return "#fef3c7";
            default: return "#e5e7eb";
        }
    };

    const getSeatColor = (seat) => {
        if (selectedSeats.includes(seat.seatId)) return "white";
        switch (seat.seatStatus) {
            case "AVAILABLE": return "#065f46";
            case "BOOKED": return "#991b1b";
            case "RESERVED": return "#92400e";
            default: return "#1e293b";
        }
    };

    const toggleSeat = (seat) => {
        if (seat.seatStatus === "AVAILABLE") return;
        setSelectedSeats((prev) =>
            prev.includes(seat.seatId)
                ? prev.filter((id) => id !== seat.seatId)
                : [...prev, seat.seatId],
        );
    };

    const handleReleaseSeats = async () => {
        if (selectedSeats.length === 0) { toast.warning("Please select seats to release"); return; }
        if (!window.confirm(`Release ${selectedSeats.length} seats?`)) return;
        try {
            await busService.releaseSeats(selectedSeats);
            toast.success(`${selectedSeats.length} seats released successfully`);
            setSelectedSeats([]);
            loadData();
        } catch (error) {
            toast.error("Failed to release seats");
        }
    };

    const bookedCount = seats.filter((s) => s.seatStatus === "BOOKED").length;
    const availableCount = seats.filter((s) => s.seatStatus === "AVAILABLE").length;
    const reservedCount = seats.filter((s) => s.seatStatus === "RESERVED").length;

    const seatRows = seats.reduce((rows, seat, idx) => {
        if (idx % 4 === 0) rows.push([]);
        rows[rows.length - 1].push(seat);
        return rows;
    }, []);

    return (
        <OperatorLayout>
            <div className="page-header">
                <div className="page-header-left">
                    <h4>Seat Management</h4>
                    {bus && <p className="text-muted">{bus.busName} — {bus.origin} to {bus.destination}</p>}
                </div>
                <Button variant="secondary" size="sm" onClick={() => navigate(-1)}>Back</Button>
            </div>

            <div className="d-flex gap-3 mb-3 flex-wrap">
                <span className="badge bg-success fs-6">Available: {availableCount}</span>
                <span className="badge bg-danger fs-6">Booked: {bookedCount}</span>
                <span className="badge bg-warning text-dark fs-6">Reserved: {reservedCount}</span>
                <span className="badge bg-primary fs-6">Total: {seats.length}</span>
            </div>

            <p className="text-muted small mb-4">
                Legend: <span style={{ background: "#d1fae5", color: "#15803d", fontWeight: "600", padding: "4px 10px", borderRadius: 6 }}>Available</span>{" "}
                <span style={{ background: "#fee2e2", color: "#b91c1c", fontWeight: "600", padding: "4px 10px", borderRadius: 6 }}>Booked</span>{" "}
                <span style={{ background: "#fef3c7", color: "#b45309", fontWeight: "600", padding: "4px 10px", borderRadius: 6 }}>Reserved</span>{" "}
                <span style={{ background: "#4f46e5", color: "white", fontWeight: "600", padding: "4px 10px", borderRadius: 6 }}>Selected</span>
            </p>

            <Card style={{ maxWidth: 450, margin: "0 auto" }}>
                <Card.Body>
                    {loading ? (
                        <p className="text-muted text-center py-4">Loading seats...</p>
                    ) : (
                        <>
                            <div style={{ display: "flex", flexDirection: "column", gap: "10px", alignItems: "center" }}>
                                {seatRows.map((row, rowIdx) => (
                                    <div key={rowIdx} style={{ display: "flex", gap: "10px", alignItems: "center" }}>
                                        <span className="text-muted small text-end" style={{ width: "20px", fontWeight: 600 }}>{rowIdx + 1}</span>
                                        {row.slice(0, 2).map((seat) => {
                                            const isSelected = selectedSeats.includes(seat.seatId);
                                            return (
                                                <button
                                                    key={seat.seatId}
                                                    onClick={() => toggleSeat(seat)}
                                                    title={`${seat.seatNumber} - ${seat.seatStatus}`}
                                                    style={{
                                                        width: "48px",
                                                        height: "44px",
                                                        background: getSeatBg(seat),
                                                        border: "1.5px solid rgba(0,0,0,0.08)",
                                                        borderRadius: "10px 10px 6px 6px",
                                                        fontSize: "0.75rem",
                                                        fontWeight: 600,
                                                        color: getSeatColor(seat),
                                                        cursor: seat.seatStatus === "AVAILABLE" ? "default" : "pointer",
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
                                            const isSelected = selectedSeats.includes(seat.seatId);
                                            return (
                                                <button
                                                    key={seat.seatId}
                                                    onClick={() => toggleSeat(seat)}
                                                    title={`${seat.seatNumber} - ${seat.seatStatus}`}
                                                    style={{
                                                        width: "48px",
                                                        height: "44px",
                                                        background: getSeatBg(seat),
                                                        border: "1.5px solid rgba(0,0,0,0.08)",
                                                        borderRadius: "10px 10px 6px 6px",
                                                        fontSize: "0.75rem",
                                                        fontWeight: 600,
                                                        color: getSeatColor(seat),
                                                        cursor: seat.seatStatus === "AVAILABLE" ? "default" : "pointer",
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

                            {selectedSeats.length > 0 && (
                                <div className="mt-4 border-top pt-3 text-center">
                                    <p className="text-muted small">{selectedSeats.length} seat(s) selected for release</p>
                                    <Button variant="danger" onClick={handleReleaseSeats} className="w-100">
                                        Release Selected Seats
                                    </Button>
                                </div>
                            )}
                        </>
                    )}
                </Card.Body>
            </Card>
        </OperatorLayout>
    );
};

export default SeatManagement;
