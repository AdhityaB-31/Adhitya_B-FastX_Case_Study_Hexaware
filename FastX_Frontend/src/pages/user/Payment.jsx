import { useEffect, useState } from "react";
import { useParams, useLocation, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { Card, Row, Col, Button, Alert, Badge } from "react-bootstrap";
import { FaCreditCard, FaMobileAlt, FaUniversity, FaShieldAlt, FaCheckCircle, FaDownload } from "react-icons/fa";
import UserLayout from "../../layouts/UserLayout";
import razorpayService from "../../services/razorpayService";
import bookingService from "../../services/bookingService";
import { useAuth } from "../../context/AuthContext";
import DownloadTicketButton from "../../components/ticket/DownloadTicketButton";
import API from "../../api/axiosConfig";

const PAYMENT_METHODS = [
    { value: "UPI", label: "UPI", hint: "Google Pay, PhonePe, Paytm", icon: <FaMobileAlt /> },
    { value: "CREDIT_CARD", label: "Credit Card", hint: "Visa, Mastercard, Rupay", icon: <FaCreditCard /> },
    { value: "DEBIT_CARD", label: "Debit Card", hint: "All major bank cards", icon: <FaCreditCard /> },
    { value: "NET_BANKING", label: "Net Banking", hint: "SBI, HDFC, ICICI & more", icon: <FaUniversity /> },
];

const Payment = () => {
    const { bookingId } = useParams();
    const { state } = useLocation();
    const navigate = useNavigate();
    const { user } = useAuth();

    const [booking, setBooking] = useState(state?.booking || null);
    const [bus] = useState(state?.bus || null);
    const [selectedSeats] = useState(state?.selectedSeats || []);
    const [paymentMethod, setMethod] = useState("");
    const [processing, setProcessing] = useState(false);
    const [paymentDone, setPaymentDone] = useState(false);
    const [paymentFailed, setFailed] = useState(false);
    const [failReason, setFailReason] = useState("");
    const [txnId, setTxnId] = useState("");
    const [timeLeft, setTimeLeft] = useState(null);

    useEffect(() => {
        const fetchDetails = async () => {
            try {
                const data = await bookingService.getBookingById(bookingId);
                setBooking(data);
                if (data.bookingStatus === "CONFIRMED") {
                    try {
                        const payRes = await API.get(`/payments/booking/${bookingId}`);
                        if (payRes.data) {
                            setTxnId(payRes.data.transactionId || "—");
                            setMethod(payRes.data.paymentMethod || "UPI");
                        }
                    } catch (e) {
                        console.log("Could not load payment details", e);
                    }
                    setPaymentDone(true);
                }
            } catch (err) {
                toast.error("Could not load booking details");
                navigate(user?.role === "ADMIN" ? "/admin/dashboard" : "/user/bookings");
            }
        };

        if (!booking) {
            fetchDetails();
        } else if (booking.bookingStatus === "CONFIRMED" && !paymentDone) {
            API.get(`/payments/booking/${bookingId}`)
                .then(payRes => {
                    if (payRes.data) {
                        setTxnId(payRes.data.transactionId || "—");
                        setMethod(payRes.data.paymentMethod || "UPI");
                    }
                })
                .catch(e => console.log(e))
                .finally(() => setPaymentDone(true));
        }
    }, [bookingId, booking, navigate, paymentDone, user]);

    useEffect(() => {
        if (!booking?.reservationExpiresAt) return;
        const tick = () => {
            let expiryStr = booking.reservationExpiresAt;
            if (expiryStr) {
                const timePart = expiryStr.split("T")[1] || "";
                if (!timePart.includes("+") && !timePart.includes("-") && !timePart.endsWith("Z")) {
                    expiryStr = expiryStr + "Z";
                }
            }
            const diff = new Date(expiryStr) - Date.now();
            setTimeLeft(diff <= 0 ? 0 : Math.ceil(diff / 1000));
        };
        tick();
        const id = setInterval(tick, 1000);
        return () => clearInterval(id);
    }, [booking]);

    useEffect(() => {
        if (timeLeft === 0 && booking?.bookingStatus === "PENDING") {
            bookingService.cancelBooking(bookingId).catch(() => { });
        }
    }, [timeLeft, booking, bookingId]);

    const formatTime = (secs) => {
        if (secs == null) return "--:--";
        const m = Math.floor(secs / 60).toString().padStart(2, "0");
        const s = (secs % 60).toString().padStart(2, "0");
        return `${m}:${s}`;
    };

    const totalAmount = booking?.totalAmount ?? 0;
    const isExpired = timeLeft === 0;
    const isUrgent = timeLeft != null && timeLeft < 300;

    const handlePay = async () => {
        if (!paymentMethod) { toast.warning("Please select a payment method"); return; }
        if (isExpired) { toast.error("Reservation expired. Please book again."); navigate(user?.role === "ADMIN" ? "/admin/dashboard" : "/user/search"); return; }

        setProcessing(true);
        setFailed(false);

        try {
            const orderData = await razorpayService.createOrder(Number(bookingId));
            const result = await razorpayService.openCheckout(
                orderData,
                { name: user?.name || user?.email, email: user?.email },
                paymentMethod,
            );
            setTxnId(result.transactionId || result.razorpayPaymentId);
            setBooking(prev => prev ? { ...prev, bookingStatus: "CONFIRMED" } : null);
            setPaymentDone(true);
            toast.success("Payment successful. Your booking is confirmed.");
        } catch (err) {
            if (err.message === "PAYMENT_CANCELLED") {
                toast.info("Payment cancelled.");
            } else {
                setFailed(true);
                setFailReason(err.response?.data?.error || err.message || "Payment failed");
                toast.error("Payment failed. Please try again.");
            }
        } finally {
            setProcessing(false);
        }
    };

    if (paymentDone) {
        return (
            <UserLayout>
                <div style={{ maxWidth: 560, margin: "40px auto", textAlign: "center" }}>
                    <div style={{ width: 80, height: 80, background: "#d1fae5", borderRadius: "50%", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 24px", fontSize: "2rem", color: "#059669" }}>
                        <FaCheckCircle />
                    </div>
                    <h3 style={{ fontWeight: 800, color: "#059669", marginBottom: 8 }}>Payment Successful!</h3>
                    <p className="text-muted" style={{ marginBottom: 28 }}>Your booking is confirmed and your seat is reserved.</p>

                    <Card style={{ marginBottom: 24, textAlign: "left" }}>
                        <Card.Body>
                            {[
                                ["Booking ID", bookingId],
                                ["Payment Method", (paymentMethod || "Razorpay").replace(/_/g, " ")],
                                ["Transaction ID", txnId || "—"],
                                ["Amount Paid", `₹${totalAmount}`],
                                ["Status", "CONFIRMED"],
                            ].map(([label, val]) => (
                                <div key={label} style={{ display: "flex", justifyContent: "space-between", padding: "8px 0", borderBottom: "1px solid #f1f5f9" }}>
                                    <span style={{ color: "#64748b", fontSize: "0.88rem" }}>{label}</span>
                                    <strong style={{ fontSize: "0.9rem", color: label === "Status" ? "#059669" : "#0f172a" }}>{val}</strong>
                                </div>
                            ))}
                        </Card.Body>
                    </Card>

                    <div style={{ display: "flex", gap: 10, justifyContent: "center", flexWrap: "wrap" }}>
                        <DownloadTicketButton bookingId={Number(bookingId)} />
                        <Button variant="primary" onClick={() => navigate(user?.role === "ADMIN" ? "/admin/dashboard" : "/user/bookings")}>View My Bookings</Button>
                        <Button variant="secondary" onClick={() => navigate(user?.role === "ADMIN" ? "/admin/dashboard" : "/user/search")}>Book Another Bus</Button>
                    </div>
                </div>
            </UserLayout>
        );
    }

    return (
        <UserLayout>
            <div className="page-header">
                <div className="page-header-left">
                    <h4>Complete Payment</h4>
                    <p>Booking {bookingId} · Secured by Razorpay</p>
                </div>
                {timeLeft != null && (
                    <div style={{
                        background: isUrgent ? "#fee2e2" : "#d1fae5",
                        color: isUrgent ? "#991b1b" : "#065f46",
                        border: `1px solid ${isUrgent ? "#fca5a5" : "#6ee7b7"}`,
                        borderRadius: 10, padding: "10px 18px", textAlign: "center", minWidth: 100
                    }}>
                        <div style={{ fontSize: "1.4rem", fontWeight: 800, lineHeight: 1 }}>{formatTime(timeLeft)}</div>
                        <div style={{ fontSize: "0.72rem", fontWeight: 600, marginTop: 2 }}>Seat reserved</div>
                    </div>
                )}
            </div>

            {isExpired && (
                <Alert variant="danger">
                    Reservation expired — seats have been released.{" "}
                    <Button variant="link" className="p-0" onClick={() => navigate(user?.role === "ADMIN" ? "/admin/dashboard" : "/user/search")}>Search again</Button>
                </Alert>
            )}

            {paymentFailed && (
                <Alert variant="danger">
                    <strong>Payment failed:</strong> {failReason}. Please try a different method or retry.
                </Alert>
            )}

            <Row className="g-4">
                {/* Payment Methods */}
                <Col lg={7}>
                    <Card>
                        <Card.Header>
                            <FaCreditCard style={{ marginRight: 8, color: "#4f46e5" }} />Select Payment Method
                        </Card.Header>
                        <Card.Body>
                            <Row className="g-3 mb-4">
                                {PAYMENT_METHODS.map(m => (
                                    <Col xs={6} key={m.value}>
                                        <div className={`payment-method-card ${paymentMethod === m.value ? "selected" : ""}`}
                                            onClick={() => setMethod(m.value)}>
                                            <div className="method-icon">{m.icon}</div>
                                            <strong style={{ display: "block", fontSize: "0.9rem" }}>{m.label}</strong>
                                            <div style={{ fontSize: "0.78rem", color: "#64748b", marginTop: 2 }}>{m.hint}</div>
                                        </div>
                                    </Col>
                                ))}
                            </Row>

                            {paymentMethod && (
                                <Alert variant="info" style={{ fontSize: "0.87rem" }}>
                                    <FaShieldAlt style={{ marginRight: 6 }} />
                                    Clicking <strong>Pay Now</strong> will open a secure Razorpay checkout popup.
                                </Alert>
                            )}

                            <Button variant="primary" className="w-100" style={{ padding: "14px", fontSize: "1rem" }}
                                disabled={!paymentMethod || processing || isExpired}
                                onClick={handlePay}>
                                {processing ? "Opening Razorpay..." : `Pay ₹${totalAmount}`}
                            </Button>

                            {paymentFailed && (
                                <Button variant="outline-danger" className="w-100 mt-2"
                                    disabled={processing || isExpired} onClick={handlePay}>
                                    Retry Payment
                                </Button>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                {/* Order Summary */}
                <Col lg={5}>
                    <Card className="position-sticky" style={{ top: 20 }}>
                        <Card.Header>Order Summary</Card.Header>
                        <Card.Body>
                            {bus && (
                                <div style={{ marginBottom: 20 }}>
                                    <h6 style={{ fontWeight: 700, marginBottom: 12 }}>{bus.busName}</h6>
                                    {[
                                        ["Route", `${bus.origin} → ${bus.destination}`],
                                        ["Timing", `${bus.departureTime} → ${bus.arrivalTime}`],
                                        ["Date", bus.journeyDate],
                                        ["Type", bus.busType?.replace(/_/g, " ")],
                                    ].map(([label, val]) => (
                                        <div key={label} style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
                                            <span style={{ color: "#64748b", fontSize: "0.85rem" }}>{label}</span>
                                            <span style={{ fontSize: "0.85rem", fontWeight: 500 }}>{val}</span>
                                        </div>
                                    ))}
                                </div>
                            )}

                            {selectedSeats.length > 0 && (
                                <div style={{ marginBottom: 20, paddingTop: 16, borderTop: "1px solid #e2e8f0" }}>
                                    <span style={{ fontSize: "0.82rem", color: "#64748b" }}>Selected Seats: </span>
                                    <strong>{selectedSeats.map(s => s.seatNumber).join(", ")}</strong>
                                </div>
                            )}

                            <div style={{ background: "#f8fafc", borderRadius: 10, padding: "16px", borderTop: "2px solid #e2e8f0" }}>
                                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                                    <span style={{ color: "#64748b" }}>Total Amount</span>
                                    <strong style={{ fontSize: "1.4rem", color: "#4f46e5" }}>₹{totalAmount}</strong>
                                </div>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </UserLayout>
    );
};

export default Payment;
