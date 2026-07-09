import { useFormik } from "formik";
import * as Yup from "yup";
import { useNavigate, Link } from "react-router-dom";
import { toast } from "react-toastify";
import { Container, Card, Form, Button, Row, Col } from "react-bootstrap";
import authService from "../../services/authService";

export const ForgotPassword = () => {
    const navigate = useNavigate();

    const formik = useFormik({
        initialValues: { email: "" },
        validationSchema: Yup.object({
            email: Yup.string().email("Invalid email address").required("Email is required"),
        }),
        onSubmit: async (values, { setSubmitting }) => {
            try {
                await authService.forgotPassword(values.email);
                toast.success("If an account exists for that email, a reset link has been sent.");
                setSubmitting(false);
                setTimeout(() => {
                    navigate("/login");
                }, 2000);
            } catch (error) {
                toast.error(error.response?.data?.message || "An error occurred. Please try again.");
                setSubmitting(false);
            }
        },
    });

    return (
        <div className="py-5">
            <Container>
                <Row className="justify-content-center">
                    <Col md={5}>
                        <div className="text-center mb-4">
                            <img src="/FastX-Logo.png" alt="FastX Logo" height="48" className="mb-3" />
                            <h2>Reset Password</h2>
                            <p className="text-muted">Enter your email and we'll send you a reset link</p>
                        </div>

                        <Card>
                            <Card.Body className="p-4">
                                <Form onSubmit={formik.handleSubmit}>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Email Address</Form.Label>
                                        <Form.Control
                                            type="email"
                                            name="email"
                                            placeholder="Enter your email"
                                            value={formik.values.email}
                                            onChange={formik.handleChange}
                                            onBlur={formik.handleBlur}
                                            isInvalid={formik.touched.email && formik.errors.email}
                                        />
                                        <Form.Control.Feedback type="invalid">{formik.errors.email}</Form.Control.Feedback>
                                    </Form.Group>

                                    <Button type="submit" variant="primary" className="w-100" disabled={formik.isSubmitting}>
                                        {formik.isSubmitting ? "Sending..." : "Send Reset Link"}
                                    </Button>
                                </Form>

                                <hr />

                                <div className="text-center">
                                    <Link to="/login">Back to Login</Link>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </div>
    );
};
