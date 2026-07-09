import { useEffect, useState } from "react";
import { useFormik } from "formik";
import * as Yup from "yup";
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";
import { Form, Row, Col, Card, Button } from "react-bootstrap";

import OperatorLayout from "../../layouts/OperatorLayout";
import busService from "../../services/busService";
import operatorService from "../../services/operatorService";
import { useAuth } from "../../context/AuthContext";

const AddBus = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [amenities, setAmenities] = useState([]);

    useEffect(() => {
        busService
            .getAllAmenities()
            .then((data) => setAmenities(Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : []))
            .catch(() => toast.warn("Could not load amenities list"));
    }, []);

    const formik = useFormik({
        initialValues: {
            busName: "",
            busNumber: "",
            busType: "",
            origin: "",
            destination: "",
            departureTime: "",
            arrivalTime: "",
            journeyDate: "",
            totalSeats: "",
            fare: "",
            amenityIds: [],
        },
        validationSchema: Yup.object({
            busName: Yup.string().required("Bus name is required"),
            busNumber: Yup.string()
                .required("Bus number is required")
                .matches(/^[A-Za-z]{2}\d{2}[A-Za-z]{2}\d{4}$/, "Bus number must be in the format 'TN07CM2026' (10 characters, e.g. 2 letters, 2 digits, 2 letters, 4 digits)"),
            busType: Yup.string().required("Bus type is required"),
            origin: Yup.string().required("Origin is required"),
            destination: Yup.string().required("Destination is required"),
            departureTime: Yup.string().required("Departure time is required"),
            arrivalTime: Yup.string().required("Arrival time is required"),
            journeyDate: Yup.date().min(new Date(), "Date must be in future").required("Journey date is required"),
            totalSeats: Yup.number().min(1).max(60).required("Total seats required"),
            fare: Yup.number().min(1).required("Fare is required"),
        }),
        onSubmit: async (values) => {
            try {
                let operatorId = user?.entityId ? Number(user.entityId) : null;
                if (!operatorId) {
                    const operator = await operatorService.getOperatorByEmail(user.email);
                    operatorId = operator.operatorId;
                }
                if (!operatorId) {
                    toast.error("Could not determine operator ID. Please log out and log in again.");
                    return;
                }
                const formattedValues = {
                    ...values,
                    busNumber: values.busNumber.toUpperCase(),
                };
                await busService.addBus({ ...formattedValues, operatorId });
                toast.success("Bus added successfully!");
                navigate("/operator/buses");
            } catch (error) {
                toast.error(error.response?.data?.message || "Failed to add bus");
            }
        },
    });

    const toggleAmenity = (id) => {
        const current = formik.values.amenityIds;
        formik.setFieldValue(
            "amenityIds",
            current.includes(id) ? current.filter((x) => x !== id) : [...current, id],
        );
    };

    return (
        <OperatorLayout>
            <Button variant="secondary" size="sm" className="mb-3" onClick={() => navigate(-1)}>Back</Button>

            <h4>Add New Bus</h4>
            <p className="text-muted">Register a new bus to your fleet</p>
            <hr />

            <Card>
                <Card.Header>Bus Details</Card.Header>
                <Card.Body>
                    <Form onSubmit={formik.handleSubmit}>
                        <Row className="g-3">
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Bus Name</Form.Label>
                                    <Form.Control
                                        name="busName"
                                        placeholder="e.g. Royal Cruiser"
                                        value={formik.values.busName}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        isInvalid={formik.touched.busName && formik.errors.busName}
                                    />
                                    <Form.Control.Feedback type="invalid">{formik.errors.busName}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Bus Number</Form.Label>
                                    <Form.Control
                                        name="busNumber"
                                        placeholder="e.g. TN01AB1234"
                                        value={formik.values.busNumber}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        isInvalid={formik.touched.busNumber && formik.errors.busNumber}
                                    />
                                    <Form.Control.Feedback type="invalid">{formik.errors.busNumber}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Bus Type</Form.Label>
                                    <Form.Select
                                        name="busType"
                                        value={formik.values.busType}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        isInvalid={formik.touched.busType && formik.errors.busType}
                                    >
                                        <option value="">Select type</option>
                                        <option value="AC_SEATER">AC Seater</option>
                                        <option value="NON_AC_SEATER">Non-AC Seater</option>
                                        <option value="AC_SLEEPER">AC Sleeper</option>
                                        <option value="NON_AC_SLEEPER">Non-AC Sleeper</option>
                                        <option value="VOLVO">Volvo</option>
                                    </Form.Select>
                                    <Form.Control.Feedback type="invalid">{formik.errors.busType}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Origin</Form.Label>
                                    <Form.Control
                                        name="origin"
                                        placeholder="From city"
                                        value={formik.values.origin}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        isInvalid={formik.touched.origin && formik.errors.origin}
                                    />
                                    <Form.Control.Feedback type="invalid">{formik.errors.origin}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Destination</Form.Label>
                                    <Form.Control
                                        name="destination"
                                        placeholder="To city"
                                        value={formik.values.destination}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        isInvalid={formik.touched.destination && formik.errors.destination}
                                    />
                                    <Form.Control.Feedback type="invalid">{formik.errors.destination}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Journey Date</Form.Label>
                                    <Form.Control
                                        type="date"
                                        name="journeyDate"
                                        value={formik.values.journeyDate}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        isInvalid={formik.touched.journeyDate && formik.errors.journeyDate}
                                    />
                                    <Form.Control.Feedback type="invalid">{formik.errors.journeyDate}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Departure Time</Form.Label>
                                    <Form.Control
                                        type="time"
                                        name="departureTime"
                                        value={formik.values.departureTime}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        isInvalid={formik.touched.departureTime && formik.errors.departureTime}
                                    />
                                    <Form.Control.Feedback type="invalid">{formik.errors.departureTime}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Arrival Time</Form.Label>
                                    <Form.Control
                                        type="time"
                                        name="arrivalTime"
                                        value={formik.values.arrivalTime}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        isInvalid={formik.touched.arrivalTime && formik.errors.arrivalTime}
                                    />
                                    <Form.Control.Feedback type="invalid">{formik.errors.arrivalTime}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Total Seats</Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="totalSeats"
                                        placeholder="e.g. 40"
                                        value={formik.values.totalSeats}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        isInvalid={formik.touched.totalSeats && formik.errors.totalSeats}
                                    />
                                    <Form.Control.Feedback type="invalid">{formik.errors.totalSeats}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Fare per Seat (Rs.)</Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="fare"
                                        placeholder="e.g. 450"
                                        value={formik.values.fare}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        isInvalid={formik.touched.fare && formik.errors.fare}
                                    />
                                    <Form.Control.Feedback type="invalid">{formik.errors.fare}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={12}>
                                <Form.Group>
                                    <Form.Label>Amenities (select all that apply)</Form.Label>
                                    {amenities.length === 0 ? (
                                        <p className="text-muted">No amenities available. Contact admin to add amenities.</p>
                                    ) : (
                                        <div className="d-flex flex-wrap gap-2">
                                            {amenities.map((a) => {
                                                const selected = formik.values.amenityIds.includes(a.amenityId);
                                                return (
                                                    <Button
                                                        type="button"
                                                        key={a.amenityId}
                                                        variant={selected ? "primary" : "outline-secondary"}
                                                        size="sm"
                                                        onClick={() => toggleAmenity(a.amenityId)}
                                                    >
                                                        {selected ? "✓ " : ""}{a.amenityName}
                                                    </Button>
                                                );
                                            })}
                                        </div>
                                    )}
                                    {formik.values.amenityIds.length > 0 && (
                                        <small className="text-muted mt-1 d-block">{formik.values.amenityIds.length} amenity(ies) selected</small>
                                    )}
                                </Form.Group>
                            </Col>
                        </Row>

                        <div className="d-flex gap-2 mt-4">
                            <Button type="submit" variant="primary" disabled={formik.isSubmitting}>
                                {formik.isSubmitting ? "Adding Bus..." : "Add Bus"}
                            </Button>
                            <Button type="button" variant="secondary" onClick={() => navigate("/operator/buses")}>
                                Cancel
                            </Button>
                        </div>
                    </Form>
                </Card.Body>
            </Card>
        </OperatorLayout>
    );
};

export default AddBus;
