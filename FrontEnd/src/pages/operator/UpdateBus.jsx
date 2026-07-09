import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useFormik } from "formik";
import * as Yup from "yup";
import { toast } from "react-toastify";
import { Form, Row, Col, Card, Button } from "react-bootstrap";

import OperatorLayout from "../../layouts/OperatorLayout";
import busService from "../../services/busService";
import operatorService from "../../services/operatorService";
import { useAuth } from "../../context/AuthContext";

const UpdateBus = () => {
    const { user } = useAuth();
    const { busId } = useParams();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [amenities, setAmenities] = useState([]);

    useEffect(() => {
        busService
            .getAllAmenities()
            .then((data) => setAmenities(Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : []))
            .catch(() => toast.warn("Could not load amenities"));
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
            operatorId: "",
            amenityIds: [],
        },
        enableReinitialize: true,
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
            journeyDate: Yup.date().required("Journey date is required"),
            totalSeats: Yup.number().min(1).required("Total seats required"),
            fare: Yup.number().min(1).required("Fare is required"),
        }),
        onSubmit: async (values) => {
            try {
                let operatorId = values.operatorId;
                if (!operatorId && user?.entityId) operatorId = Number(user.entityId);
                if (!operatorId && user?.email) {
                    const operator = await operatorService.getOperatorByEmail(user.email);
                    operatorId = operator?.operatorId;
                }
                if (!operatorId) { toast.error("Could not determine operator ID."); return; }

                const formattedValues = {
                    ...values,
                    busNumber: values.busNumber.toUpperCase(),
                };
                await busService.updateBus(busId, { ...formattedValues, operatorId });
                toast.success("Bus updated successfully!");
                navigate("/operator/buses");
            } catch (error) {
                toast.error(error.response?.data?.message || "Failed to update bus");
            }
        },
    });

    useEffect(() => {
        const loadBus = async () => {
            try {
                const bus = await busService.getBusDetails(busId);
                formik.setValues({
                    busName: bus.busName || "",
                    busNumber: bus.busNumber || "",
                    busType: bus.busType || "",
                    origin: bus.origin || "",
                    destination: bus.destination || "",
                    departureTime: bus.departureTime || "",
                    arrivalTime: bus.arrivalTime || "",
                    journeyDate: bus.journeyDate || "",
                    totalSeats: bus.totalSeats || "",
                    fare: bus.fare || "",
                    operatorId: bus.operatorId || "",
                    amenityIds: bus.amenityIds || [],
                });
            } catch (error) {
                toast.error("Failed to load bus details");
                navigate("/operator/buses");
            } finally {
                setLoading(false);
            }
        };
        loadBus();
    }, [busId]);

    const toggleAmenity = (id) => {
        const current = formik.values.amenityIds;
        formik.setFieldValue(
            "amenityIds",
            current.includes(id) ? current.filter((x) => x !== id) : [...current, id],
        );
    };

    if (loading)
        return <OperatorLayout><p className="text-muted">Loading bus details...</p></OperatorLayout>;

    return (
        <OperatorLayout>
            <Button variant="secondary" size="sm" className="mb-3" onClick={() => navigate(-1)}>Back</Button>

            <h4>Update Bus</h4>
            <p className="text-muted">Modify bus details below</p>
            <hr />

            <Card>
                <Card.Header>Edit Bus {busId}</Card.Header>
                <Card.Body>
                    <Form onSubmit={formik.handleSubmit}>
                        <Row className="g-3">
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Bus Name</Form.Label>
                                    <Form.Control name="busName" value={formik.values.busName} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.busName && formik.errors.busName} />
                                    <Form.Control.Feedback type="invalid">{formik.errors.busName}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Bus Number</Form.Label>
                                    <Form.Control name="busNumber" value={formik.values.busNumber} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.busNumber && formik.errors.busNumber} />
                                    <Form.Control.Feedback type="invalid">{formik.errors.busNumber}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Bus Type</Form.Label>
                                    <Form.Select name="busType" value={formik.values.busType} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.busType && formik.errors.busType}>
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
                                    <Form.Control name="origin" value={formik.values.origin} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.origin && formik.errors.origin} />
                                    <Form.Control.Feedback type="invalid">{formik.errors.origin}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Destination</Form.Label>
                                    <Form.Control name="destination" value={formik.values.destination} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.destination && formik.errors.destination} />
                                    <Form.Control.Feedback type="invalid">{formik.errors.destination}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Journey Date</Form.Label>
                                    <Form.Control type="date" name="journeyDate" value={formik.values.journeyDate} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.journeyDate && formik.errors.journeyDate} />
                                    <Form.Control.Feedback type="invalid">{formik.errors.journeyDate}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Departure Time</Form.Label>
                                    <Form.Control type="time" name="departureTime" value={formik.values.departureTime} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.departureTime && formik.errors.departureTime} />
                                    <Form.Control.Feedback type="invalid">{formik.errors.departureTime}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Arrival Time</Form.Label>
                                    <Form.Control type="time" name="arrivalTime" value={formik.values.arrivalTime} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.arrivalTime && formik.errors.arrivalTime} />
                                    <Form.Control.Feedback type="invalid">{formik.errors.arrivalTime}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Total Seats</Form.Label>
                                    <Form.Control type="number" name="totalSeats" value={formik.values.totalSeats} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.totalSeats && formik.errors.totalSeats} />
                                    <Form.Control.Feedback type="invalid">{formik.errors.totalSeats}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Fare per Seat (Rs.)</Form.Label>
                                    <Form.Control type="number" name="fare" value={formik.values.fare} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.fare && formik.errors.fare} />
                                    <Form.Control.Feedback type="invalid">{formik.errors.fare}</Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={12}>
                                <Form.Group>
                                    <Form.Label>Amenities (select all that apply)</Form.Label>
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
                                </Form.Group>
                            </Col>
                        </Row>

                        <div className="d-flex gap-2 mt-4">
                            <Button type="submit" variant="primary" disabled={formik.isSubmitting}>
                                {formik.isSubmitting ? "Saving..." : "Save Changes"}
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

export default UpdateBus;
