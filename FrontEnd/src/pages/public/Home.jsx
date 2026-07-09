import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Navbar, Container, Nav, Row, Col, Form, Button, Card } from "react-bootstrap";
import { FaBus, FaArrowRight, FaMapMarkerAlt, FaCalendarAlt } from "react-icons/fa";
import { useAuth } from "../../context/AuthContext";

const Home = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [search, setSearch] = useState({ origin: "", destination: "", journeyDate: "" });
    const [errors, setErrors] = useState({});
    const today = new Date().toISOString().split("T")[0];

    const handleSearch = (e) => {
        e.preventDefault();
        const errs = {};
        if (!search.origin.trim()) errs.origin = "Enter origin city";
        if (!search.destination.trim()) errs.destination = "Enter destination city";
        if (!search.journeyDate) errs.journeyDate = "Select a journey date";
        if (search.origin && search.destination && search.origin.toLowerCase() === search.destination.toLowerCase()) {
            errs.destination = "Origin and destination can't be the same";
        }
        if (Object.keys(errs).length) { setErrors(errs); return; }
        navigate("/search", { state: { prefill: search } });
    };

    return (
        <div style={{ minHeight: "100vh", background: "#f8fafc", display: "flex", flexDirection: "column" }}>
            {/* Navbar */}
            <Navbar expand="lg" className="border-bottom" style={{
                background: "rgba(255,255,255,0.95)",
                backdropFilter: "blur(12px)",
                padding: "14px 0",
                boxShadow: "0 1px 3px rgba(0,0,0,0.06)"
            }}>
                <Container>
                    <Navbar.Brand href="/" style={{ display: "flex", alignItems: "center", gap: 10, fontWeight: 800, fontSize: "1.3rem", color: "#4f46e5" }}>
                        <img src="/FastX-Logo.png" alt="FastX" height="32" />
                        FastX
                    </Navbar.Brand>
                    <Navbar.Toggle />
                    <Navbar.Collapse>
                        <Nav className="ms-auto align-items-center gap-2">
                            {user ? (
                                <>
                                    <span style={{ color: "#64748b", fontSize: "0.88rem" }}>{user.email}</span>
                                    <Link to={`/${user.role === "BUS_OPERATOR" ? "operator" : user.role?.toLowerCase()}/dashboard`}
                                        className="btn btn-primary btn-sm">Dashboard</Link>
                                </>
                            ) : (
                                <>
                                    <Link to="/login" className="nav-link" style={{ color: "#64748b", fontWeight: 500 }}>Sign In</Link>
                                    <Link to="/register/user" className="btn btn-primary btn-sm px-4">Register</Link>
                                </>
                            )}
                        </Nav>
                    </Navbar.Collapse>
                </Container>
            </Navbar>

            {/* Hero Section */}
            <div className="hero-section" style={{ padding: "80px 0", position: "relative", flex: 1, display: "flex", alignItems: "center" }}>
                <Container>
                    <Row className="align-items-center g-5">
                        {/* Left Copy */}
                        <Col lg={6}>
                            <div style={{
                                display: "inline-flex", alignItems: "center", gap: 8,
                                background: "#eef2ff", color: "#4f46e5",
                                padding: "6px 16px", borderRadius: 999,
                                fontSize: "0.82rem", fontWeight: 700,
                                marginBottom: 20, letterSpacing: "0.5px"
                            }}>
                                <FaBus style={{ fontSize: "0.8rem" }} />
                                INDIA'S TRUSTED BUS BOOKING PLATFORM
                            </div>
                            <h1 style={{
                                fontSize: "clamp(2.2rem, 5vw, 3.2rem)",
                                fontWeight: 900,
                                lineHeight: 1.15,
                                background: "linear-gradient(135deg, #1e1b4b 0%, #4f46e5 60%, #7c3aed 100%)",
                                WebkitBackgroundClip: "text",
                                WebkitTextFillColor: "transparent",
                                marginBottom: 20,
                            }}>
                                Travel Smarter,<br />Book Faster
                            </h1>
                            <p style={{ fontSize: "1.05rem", color: "#64748b", lineHeight: 1.75, marginBottom: 32, maxWidth: 480 }}>
                                Search hundreds of bus routes, choose your perfect seat, and get your ticket instantly — all in one place.
                            </p>
                            <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
                                <Link to="/register/user" className="btn btn-primary" style={{ padding: "12px 28px", fontSize: "0.95rem" }}>
                                    Get Started Free
                                </Link>
                                <Link to="/search" className="btn btn-outline-primary" style={{ padding: "12px 28px", fontSize: "0.95rem" }}>
                                    Browse Buses <FaArrowRight style={{ marginLeft: 6, fontSize: "0.8rem" }} />
                                </Link>
                            </div>
                            {/* Trust Badges */}
                            <div style={{ display: "flex", gap: 24, marginTop: 36, flexWrap: "wrap" }}>
                                {[["500+", "Routes"], ["10k+", "Bookings"], ["99%", "Satisfaction"]].map(([val, label]) => (
                                    <div key={label}>
                                        <div style={{ fontSize: "1.4rem", fontWeight: 800, color: "#4f46e5" }}>{val}</div>
                                        <div style={{ fontSize: "0.78rem", color: "#94a3b8", fontWeight: 600 }}>{label}</div>
                                    </div>
                                ))}
                            </div>
                        </Col>

                        {/* Search Card */}
                        <Col lg={6}>
                            <Card className="shadow-lg" style={{ border: "none", borderRadius: 20, overflow: "visible" }}>
                                <Card.Body style={{ padding: 32 }}>
                                    <div style={{ marginBottom: 24 }}>
                                        <h4 style={{ fontWeight: 800, marginBottom: 4 }}>Find Your Bus</h4>
                                        <p className="text-muted" style={{ fontSize: "0.88rem", margin: 0 }}>
                                            Compare fares and book instantly
                                        </p>
                                    </div>

                                    <Form onSubmit={handleSearch}>
                                        <Form.Group className="mb-3">
                                            <Form.Label>
                                                <FaMapMarkerAlt style={{ color: "#4f46e5", marginRight: 6 }} />
                                                From
                                            </Form.Label>
                                            <Form.Control
                                                type="text"
                                                placeholder="Enter origin city"
                                                value={search.origin}
                                                onChange={e => { setSearch(p => ({ ...p, origin: e.target.value })); setErrors(p => ({ ...p, origin: "" })); }}
                                                isInvalid={!!errors.origin}
                                            />
                                            <Form.Control.Feedback type="invalid">{errors.origin}</Form.Control.Feedback>
                                        </Form.Group>

                                        <Form.Group className="mb-3">
                                            <Form.Label>
                                                <FaMapMarkerAlt style={{ color: "#ef4444", marginRight: 6 }} />
                                                To
                                            </Form.Label>
                                            <Form.Control
                                                type="text"
                                                placeholder="Enter destination city"
                                                value={search.destination}
                                                onChange={e => { setSearch(p => ({ ...p, destination: e.target.value })); setErrors(p => ({ ...p, destination: "" })); }}
                                                isInvalid={!!errors.destination}
                                            />
                                            <Form.Control.Feedback type="invalid">{errors.destination}</Form.Control.Feedback>
                                        </Form.Group>

                                        <Form.Group className="mb-4">
                                            <Form.Label>
                                                <FaCalendarAlt style={{ color: "#f59e0b", marginRight: 6 }} />
                                                Journey Date
                                            </Form.Label>
                                            <Form.Control
                                                type="date"
                                                min={today}
                                                value={search.journeyDate}
                                                onChange={e => { setSearch(p => ({ ...p, journeyDate: e.target.value })); setErrors(p => ({ ...p, journeyDate: "" })); }}
                                                isInvalid={!!errors.journeyDate}
                                            />
                                            <Form.Control.Feedback type="invalid">{errors.journeyDate}</Form.Control.Feedback>
                                        </Form.Group>

                                        <Button type="submit" variant="primary" className="w-100" style={{ padding: "13px", fontSize: "1rem" }}>
                                            <FaBus style={{ marginRight: 8 }} /> Search Buses
                                        </Button>
                                    </Form>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </Container>
            </div>
        </div>
    );
};

export default Home;
