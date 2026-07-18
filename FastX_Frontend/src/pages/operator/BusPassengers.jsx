import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button, Table, Badge } from "react-bootstrap";
import { toast } from "react-toastify";

import { useAuth } from "../../context/AuthContext";
import OperatorLayout from "../../layouts/OperatorLayout";
import bookingService from "../../services/bookingService";
import busService from "../../services/busService";

const BusPassengers = () => {
    const { busId } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();

    const [passengers, setPassengers] = useState([]);
    const [busDetails, setBusDetails] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadData();
    }, [busId]);

    const loadData = async () => {
        try {
            const [bus, bookings] = await Promise.all([
                busService.getBusDetails(busId),
                bookingService.getBookingsByBus(busId)
            ]);

            setBusDetails(bus);

            const bookingsArr = Array.isArray(bookings) ? bookings : Array.isArray(bookings?.content) ? bookings.content : [];
            const confirmedBookings = bookingsArr.filter(b => b.bookingStatus === "CONFIRMED");
            const allPassengers = confirmedBookings.flatMap(b =>
                b.passengers.map(p => ({
                    ...p,
                    bookingId: b.bookingId,
                    bookingStatus: b.bookingStatus,
                    bookingDate: b.bookingDate
                }))
            );

            allPassengers.sort((a, b) => a.bookingId - b.bookingId);
            setPassengers(allPassengers);
        } catch (error) {
            toast.error("Failed to load passenger details");
        } finally {
            setLoading(false);
        }
    };

    return (
        <OperatorLayout>
            <Button variant="secondary" size="sm" className="mb-3" onClick={() => navigate(-1)}>
                Back
            </Button>

            <h4>Passenger Manifest</h4>
            {busDetails && (
                <p className="text-muted">
                    {busDetails.busName} ({busDetails.busNumber}) - {busDetails.origin} to {busDetails.destination} | Journey: {busDetails.journeyDate}
                </p>
            )}
            <hr />

            <p className="mb-3"><strong>Total Confirmed Passengers: {passengers.length}</strong></p>

            {loading ? (
                <p className="text-muted">Loading passengers...</p>
            ) : passengers.length === 0 ? (
                <p className="text-muted">No confirmed passengers for this bus yet.</p>
            ) : (
                <Table responsive className="table-operator text-center align-middle">
                    <thead className="text-center">
                        <tr>
                            <th>Seat</th>
                            <th>Passenger Name</th>
                            <th>Age</th>
                            <th>Gender</th>
                            <th>Booking ID</th>
                        </tr>
                    </thead>
                    <tbody>
                        {passengers.map((p, idx) => (
                            <tr key={idx}>
                                <td><Badge bg="primary">{p.seatNumber}</Badge></td>
                                <td>{p.name}</td>
                                <td>{p.age}</td>
                                <td>{p.gender}</td>
                                <td>{p.bookingId}</td>
                            </tr>
                        ))}
                    </tbody>
                </Table>
            )}
        </OperatorLayout>
    );
};

export default BusPassengers;
