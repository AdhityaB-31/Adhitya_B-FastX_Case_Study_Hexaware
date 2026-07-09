import { useFormik } from "formik";
import * as Yup from "yup";
import { useNavigate, Link } from "react-router-dom";
import { toast } from "react-toastify";
import { Container, Card, Form, Button, Row, Col } from "react-bootstrap";
import authService from "../../services/authService";
import PasswordRequirements from "../../components/auth/PasswordRequirements";

const RegisterOperator = () => {
    const navigate = useNavigate();

    const formik = useFormik({
        initialValues: { companyName: "", email: "", password: "", phoneNumber: "", address: "" },
        validationSchema: Yup.object({
            companyName: Yup.string().min(3, "Company name must be at least 3 characters").required("Company name is required"),
            email: Yup.string().email("Invalid email address").required("Email is required"),
            password: Yup.string()
                .min(8, "Password must be at least 8 characters")
                .matches(/[A-Z]/, "Password must contain at least one uppercase letter")
                .matches(/[a-z]/, "Password must contain at least one lowercase letter")
                .matches(/\d/, "Password must contain at least one number")
                .matches(/[@$!%*?&]/, "Password must contain at least one special character")
                .required("Password is required"),
            phoneNumber: Yup.string().matches(/^[6-9]\d{9}$/, "Enter a valid 10-digit phone number").required("Phone number is required"),
            address: Yup.string().min(10, "Address must be at least 10 characters").required("Address is required"),
        }),
        onSubmit: async (values) => {
            try {
                await authService.registerOperator(values);
                toast.success("Operator registered successfully! Please login.");
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
                    <Col md={7} lg={6}>
                        <div className="text-center mb-4">
                            <img src="/FastX-Logo.png" alt="FastX Logo" height="48" className="mb-3" />
                            <h2>Become an Operator</h2>
                            <p className="text-muted">Register your bus company on FastX</p>
                        </div>

                        <Card>
                            <Card.Body className="p-4">
                                <Form onSubmit={formik.handleSubmit}>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Company Name</Form.Label>
                                        <Form.Control type="text" name="companyName" placeholder="Enter company name" value={formik.values.companyName} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.companyName && formik.errors.companyName} />
                                        <Form.Control.Feedback type="invalid">{formik.errors.companyName}</Form.Control.Feedback>
                                    </Form.Group>

                                    <Row>
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Email Address</Form.Label>
                                                <Form.Control type="email" name="email" placeholder="Business email" value={formik.values.email} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.email && formik.errors.email} />
                                                <Form.Control.Feedback type="invalid">{formik.errors.email}</Form.Control.Feedback>
                                            </Form.Group>
                                        </Col>
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Password</Form.Label>
                                                <Form.Control type="password" name="password" placeholder="Create password" value={formik.values.password} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.password && formik.errors.password} />
                                                <Form.Control.Feedback type="invalid">{formik.errors.password}</Form.Control.Feedback>
                                                <PasswordRequirements password={formik.values.password} />
                                            </Form.Group>
                                        </Col>
                                    </Row>

                                    <Form.Group className="mb-3">
                                        <Form.Label>Phone Number</Form.Label>
                                        <Form.Control type="text" name="phoneNumber" placeholder="10-digit phone number" value={formik.values.phoneNumber} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.phoneNumber && formik.errors.phoneNumber} />
                                        <Form.Control.Feedback type="invalid">{formik.errors.phoneNumber}</Form.Control.Feedback>
                                    </Form.Group>

                                    <Form.Group className="mb-3">
                                        <Form.Label>Business Address</Form.Label>
                                        <Form.Control as="textarea" rows={2} name="address" placeholder="Enter your business address" value={formik.values.address} onChange={formik.handleChange} onBlur={formik.handleBlur} isInvalid={formik.touched.address && formik.errors.address} />
                                        <Form.Control.Feedback type="invalid">{formik.errors.address}</Form.Control.Feedback>
                                    </Form.Group>

                                    <Button type="submit" variant="primary" className="w-100" disabled={formik.isSubmitting}>
                                        {formik.isSubmitting ? "Registering..." : "Register as Operator"}
                                    </Button>
                                </Form>

                                <hr />

                                <div className="text-center">
                                    <p className="text-muted mb-1">Already registered? <Link to="/login">Login here</Link></p>
                                    <p className="text-muted mb-0">Passenger? <Link to="/register/user">Register as User</Link></p>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </div>
    );
};

export default RegisterOperator;
