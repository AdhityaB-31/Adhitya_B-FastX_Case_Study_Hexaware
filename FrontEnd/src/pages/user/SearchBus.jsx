import { useState, useEffect } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { toast } from "react-toastify";
import { Card, Form, Button, Container, Row, Col, Navbar, Nav, Badge, Table } from "react-bootstrap";
import { FaBus, FaMapMarkerAlt, FaCalendarAlt, FaSearch, FaArrowRight } from "react-icons/fa";
import busService from "../../services/busService";
import { useAuth } from "../../context/AuthContext";
import UserLayout from "../../layouts/UserLayout";

const SearchBus = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuth();
    const prefill = location.state?.prefill || {};
    const [formData, setFormData] = useState({
        origin: prefill.origin || "",
        destination: prefill.destination || "",
        journeyDate: prefill.journeyDate || ""
    });
    const [buses, setBuses] = useState([]);
    const [busTypeFilter, setBusTypeFilter] = useState("ALL");
    const [searched, setSearched] = useState(false);
    const [loading, setLoading] = useState(false);
    const [errors, setErrors] = useState({});

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: "" }));
    };

    const validate = () => {
        const newErrors = {};
        if (!formData.origin.trim()) newErrors.origin = "Origin is required";
        if (!formData.destination.trim()) newErrors.destination = "Destination is required";
        if (!formData.journeyDate) newErrors.journeyDate = "Journey date is required";
        if (formData.origin && formData.destination && formData.origin.toLowerCase() === formData.destination.toLowerCase()) {
            newErrors.destination = "Origin and destination cannot be the same";
        }
        return newErrors;
    };

    const filterExpired = (results) => results.filter(bus => {
        if (!bus.journeyDate || !bus.departureTime) return true;
        const [year, month, day] = bus.journeyDate.split("-").map(Number);
        const [hours, minutes] = bus.departureTime.split(":").map(Number);
        return new Date(year, month - 1, day, hours || 0, minutes || 0) >= new Date();
    });

    const filteredBuses = buses.filter(bus => {
        return busTypeFilter === "ALL" || bus.busType === busTypeFilter;
    });

    const performSearch = async (searchData) => {
        setLoading(true);
        try {
            const raw = await busService.searchBuses(searchData.origin, searchData.destination, searchData.journeyDate);
            const results = Array.isArray(raw) ? raw : Array.isArray(raw?.content) ? raw.content : [];
            const filtered = filterExpired(results);
            setBuses(filtered);
            setSearched(true);
            if (filtered.length === 0) toast.info("No buses found for this route and date");
        } catch {
            toast.error("Search failed. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (prefill.origin && prefill.destination && prefill.journeyDate) {
            performSearch(prefill);
        } else {
            loadAllBuses();
        }
        // eslint-disable-next-line
    }, []);

    const loadAllBuses = async () => {
        setLoading(true);
        try {
            const raw = await busService.getAllBuses();
            const results = Array.isArray(raw) ? raw : Array.isArray(raw?.content) ? raw.content : [];
            setBuses(filterExpired(results));
            setSearched(true);
        } catch {
            toast.error("Failed to load buses.");
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = async (e) => {
        e.preventDefault();
        const validationErrors = validate();
        if (Object.keys(validationErrors).length > 0) { setErrors(validationErrors); return; }
        await performSearch(formData);
    };

    const handleBookNow = (bus) => {
        if (!user) {
            navigate("/login", { state: { redirectTo: `/user/bus/${bus.busId}`, searchParams: { bus, journeyDate: formData.journeyDate } } });
        } else {
            navigate(`/user/bus/${bus.busId}`, { state: { bus, journeyDate: formData.journeyDate } });
        }
    };

    const isUserRoute = location.pathname.includes("/user/");

    const publicNavbar = !isUserRoute && (
        <Navbar expand="lg" style={{ background: "rgba(255,255,255,0.95)", backdropFilter: "blur(12px)", borderBottom: "1px solid #e2e8f0", padding: "14px 0" }}>
            <Container>
                <Navbar.Brand href="/" style={{ display: "flex", alignItems: "center", gap: 8, fontWeight: 800, color: "#4f46e5", fontSize: "1.3rem" }}>
                    <img src="/FastX-Logo.png" alt="FastX" height="30" />FastX
                </Navbar.Brand>
                <Navbar.Toggle />
                <Navbar.Collapse>
                    <Nav className="ms-auto align-items-center gap-2">
                        {user ? (
                            <>
                                <span style={{ color: "#64748b", fontSize: "0.88rem" }}>{user.email}</span>
                                <Link to={`/${user.role === "BUS_OPERATOR" ? "operator" : user.role?.toLowerCase()}/dashboard`} className="btn btn-primary btn-sm">Dashboard</Link>
                            </>
                        ) : (
                            <>
                                <Link to="/login" style={{ color: "#64748b", fontWeight: 500, textDecoration: "none", padding: "6px 12px" }}>Sign In</Link>
                                <Link to="/register/user" className="btn btn-primary btn-sm px-4">Register</Link>
                            </>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );

    const content = (
        <Container className={isUserRoute ? "" : "py-4"}>
            {!isUserRoute && (
                <div style={{ marginBottom: 28 }}>
                    <h4>Search Buses</h4>
                    <p className="text-muted">Find the best bus for your journey</p>
                </div>
            )}

            {isUserRoute && (
                <div className="page-header">
                    <div className="page-header-left">
                        <h4>Search Buses</h4>
                        <p>Find and book your next bus journey</p>
                    </div>
                </div>
            )}

            {/* Search Form */}
            <Card className="mb-4">
                <Card.Body>
                    <Form onSubmit={handleSearch}>
                        <Row className="g-3 align-items-end">
                            <Col md={3}>
                                <Form.Group>
                                    <Form.Label>
                                        <FaMapMarkerAlt style={{ color: "#4f46e5", marginRight: 5 }} />From
                                    </Form.Label>
                                    <Form.Control name="origin" placeholder="Origin city" value={formData.origin}
                                        onChange={handleChange} isInvalid={!!errors.origin} />
                                    <Form.Control.Feedback type="invalid">{errors.origin}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={3}>
                                <Form.Group>
                                    <Form.Label>
                                        <FaMapMarkerAlt style={{ color: "#ef4444", marginRight: 5 }} />To
                                    </Form.Label>
                                    <Form.Control name="destination" placeholder="Destination city" value={formData.destination}
                                        onChange={handleChange} isInvalid={!!errors.destination} />
                                    <Form.Control.Feedback type="invalid">{errors.destination}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={2}>
                                <Form.Group>
                                    <Form.Label>
                                        <FaCalendarAlt style={{ color: "#f59e0b", marginRight: 5 }} />Date
                                    </Form.Label>
                                    <Form.Control type="date" name="journeyDate" min={new Date().toISOString().split("T")[0]}
                                        value={formData.journeyDate} onChange={handleChange} isInvalid={!!errors.journeyDate} />
                                    <Form.Control.Feedback type="invalid">{errors.journeyDate}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={2}>
                                <Form.Group>
                                    <Form.Label>
                                        <FaBus style={{ color: "#10b981", marginRight: 5 }} />Bus Type
                                    </Form.Label>
                                    <Form.Select value={busTypeFilter} onChange={(e) => setBusTypeFilter(e.target.value)}>
                                        <option value="ALL">All Types</option>
                                        <option value="AC_SEATER">AC Seater</option>
                                        <option value="NON_AC_SEATER">Non-AC Seater</option>
                                        <option value="AC_SLEEPER">AC Sleeper</option>
                                        <option value="NON_AC_SLEEPER">Non-AC Sleeper</option>
                                        <option value="VOLVO">Volvo</option>
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                            <Col md={2}>
                                <Button type="submit" variant="primary" className="w-100" disabled={loading}>
                                    <FaSearch style={{ marginRight: 7 }} />
                                    {loading ? "Searching..." : "Search"}
                                </Button>
                            </Col>
                        </Row>
                    </Form>
                </Card.Body>
            </Card>

            {/* Results */}
            {searched && (
                <>
                    <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 16 }}>
                        <h5 style={{ margin: 0 }}>{filteredBuses.length} Buses Found</h5>
                        {filteredBuses.length > 0 && <Badge bg="primary">{filteredBuses.length} available</Badge>}
                    </div>

                    {filteredBuses.length === 0 ? (
                        <div className="empty-state">
                            <div className="empty-state-icon">🚌</div>
                            <h6>No buses available</h6>
                            <p>Try a different date or route.</p>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <Table className="table-user text-center align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th>Bus</th>
                                        <th>Type</th>
                                        <th>From</th>
                                        <th>To</th>
                                        <th>Date</th>
                                        <th>Departure</th>
                                        <th>Arrival</th>
                                        <th>Fare</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredBuses.map(bus => (
                                        <tr key={bus.busId}>
                                            <td style={{ textAlign: "left" }}>
                                                <strong style={{ display: "block" }}>{bus.busName}</strong>
                                                <small style={{ color: "#94a3b8" }}>{bus.busNumber}</small>
                                            </td>
                                            <td>
                                                <Badge bg="secondary">{bus.busType?.replace(/_/g, " ")}</Badge>
                                            </td>
                                            <td>{bus.origin}</td>
                                            <td>{bus.destination}</td>
                                            <td>{bus.journeyDate}</td>
                                            <td><strong>{bus.departureTime}</strong></td>
                                            <td>{bus.arrivalTime}</td>
                                            <td><strong style={{ color: "#4f46e5" }}>₹{bus.fare}</strong></td>
                                            <td>
                                                <Button variant="primary" size="sm" onClick={() => handleBookNow(bus)}>
                                                    {user ? "Book Now" : "Login to Book"}
                                                    <FaArrowRight style={{ marginLeft: 5, fontSize: "0.75rem" }} />
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    )}
                </>
            )}
        </Container>
    );

    return isUserRoute ? <UserLayout>{content}</UserLayout> : <><div className="public-layout">{publicNavbar}{content}</div></>;
};

export default SearchBus;
