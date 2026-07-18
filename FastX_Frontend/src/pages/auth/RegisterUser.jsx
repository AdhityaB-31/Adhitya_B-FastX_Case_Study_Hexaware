import { useFormik } from "formik";
import * as Yup from "yup";
import { useNavigate, Link } from "react-router-dom";
import { toast } from "react-toastify";
import { Container, Card, Form, Button, Row, Col } from "react-bootstrap";
import authService from "../../services/authService";
import PasswordRequirements from "../../components/auth/PasswordRequirements";

const RegisterUser = () => {
    const navigate = useNavigate();

    const formik = useFormik({
        initialValues: { fullName: "", email: "", password: "", phoneNumber: "", dateOfBirth: "", gender: "", address: "" },
        validationSchema: Yup.object({
            fullName: Yup.string().min(3, "Name must be at least 3 characters").required("Full name is required"),
            email: Yup.string().email("Invalid email address").required("Email is required"),
            password: Yup.string()
                .min(8, "Password must be at least 8 characters")
                .matches(/[A-Z]/, "Password must contain at least one uppercase letter")
                .matches(/[a-z]/, "Password must contain at least one lowercase letter")
                .matches(/\d/, "Password must contain at least one number")
                .matches(/[@$!%*?&]/, "Password must contain at least one special character")
                .required("Password is required"),
            phoneNumber: Yup.string().matches(/^[6-9]\d{9}$/, "Enter a valid 10-digit phone number").required("Phone number is required"),
            dateOfBirth: Yup.date().max(new Date(), "Date of birth cannot be in the future").required("Date of birth is required"),
            gender: Yup.string().oneOf(["Male", "Female", "Other"], "Please select a valid gender").required("Gender is required"),
            address: Yup.string().min(10, "Address must be at least 10 characters").required("Address is required"),
        }),
        onSubmit: async (values) => {
            try {
                await authService.registerUser(values);
                toast.success("Registration successful! Please login.");
                navigate("/login");
            } catch (error) {
                toast.error(error.response?.data?.message || "Registration failed. Please try again.");
            }
        },
    });

    return (
        <div className="py-5">
            <Container>
                <Row className="justify-content-center">
                    <Col md={8} lg={6}>
                        <div className="text-center mb-4">
                            <img src="/FastX-Logo.png" alt="FastX Logo" height="48" className="mb-3" />
                            <h2>Create an Account</h2>
                            <p className="text-muted">Register as a passenger</p>
                        </div>

                        <Card>
                            <Card.Body className="p-4">
                                <Form onSubmit={formik.handleSubmit}>
                                    <Row>
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Full Name</Form.Label>
                                                <Form.Control type="text" name="fullName" placeholder="Enter full name" value={formik.values.fullName} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.fullName && formik.errors.fullName} />
                                                <Form.Control.Feedback type="invalid">{formik.errors.fullName}</Form.Control.Feedback>
                                            </Form.Group>
                                        </Col>
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Email Address</Form.Label>
                                                <Form.Control type="email" name="email" placeholder="Enter email" value={formik.values.email} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.email && formik.errors.email} />
                                                <Form.Control.Feedback type="invalid">{formik.errors.email}</Form.Control.Feedback>
                                            </Form.Group>
                                        </Col>
                                    </Row>

                                    <Row>
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Password</Form.Label>
                                                <Form.Control type="password" name="password" placeholder="Create password" value={formik.values.password} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.password && formik.errors.password} />
                                                <Form.Control.Feedback type="invalid">{formik.errors.password}</Form.Control.Feedback>
                                                <PasswordRequirements password={formik.values.password} />
                                            </Form.Group>
                                        </Col>
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Phone Number</Form.Label>
                                                <Form.Control type="text" name="phoneNumber" placeholder="10-digit phone number" value={formik.values.phoneNumber} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.phoneNumber && formik.errors.phoneNumber} />
                                                <Form.Control.Feedback type="invalid">{formik.errors.phoneNumber}</Form.Control.Feedback>
                                            </Form.Group>
                                        </Col>
                                    </Row>

                                    <Row>
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Date of Birth</Form.Label>
                                                <Form.Control type="date" name="dateOfBirth" value={formik.values.dateOfBirth} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.dateOfBirth && formik.errors.dateOfBirth} />
                                                <Form.Control.Feedback type="invalid">{formik.errors.dateOfBirth}</Form.Control.Feedback>
                                            </Form.Group>
                                        </Col>
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Gender</Form.Label>
                                                <Form.Select name="gender" value={formik.values.gender} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.gender && formik.errors.gender}>
                                                    <option value="">Select gender</option>
                                                    <option value="Male">Male</option>
                                                    <option value="Female">Female</option>
                                                    <option value="Other">Other</option>
                                                </Form.Select>
                                                <Form.Control.Feedback type="invalid">{formik.errors.gender}</Form.Control.Feedback>
                                            </Form.Group>
                                        </Col>
                                    </Row>

                                    <Form.Group className="mb-3">
                                        <Form.Label>Address</Form.Label>
                                        <Form.Control as="textarea" rows={2} name="address" placeholder="Enter your address" value={formik.values.address} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.address && formik.errors.address} />
                                        <Form.Control.Feedback type="invalid">{formik.errors.address}</Form.Control.Feedback>
                                    </Form.Group>

                                    <Button type="submit" variant="primary" className="w-100" disabled={formik.isSubmitting}>
                                        {formik.isSubmitting ? "Registering..." : "Create Account"}
                                    </Button>
                                </Form>

                                <hr />

                                <div className="text-center">
                                    <p className="text-muted mb-1">Already have an account? <Link to="/login">Login here</Link></p>
                                    <p className="text-muted mb-0">Bus Operator? <Link to="/register/operator">Register as Operator</Link></p>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </div>
    );
};

export default RegisterUser;
