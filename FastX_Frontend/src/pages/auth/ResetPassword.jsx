import { useState } from "react";
import { useFormik } from "formik";
import * as Yup from "yup";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { toast } from "react-toastify";
import { Container, Card, Form, Button, Row, Col, Alert } from "react-bootstrap";
import authService from "../../services/authService";
import PasswordRequirements from "../../components/auth/PasswordRequirements";

export const ResetPassword = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token") || "";
    const [done, setDone] = useState(false);

    const formik = useFormik({
        initialValues: { newPassword: "", confirmPassword: "" },
        validationSchema: Yup.object({
            newPassword: Yup.string()
                .min(8, "Password must be at least 8 characters")
                .matches(/[A-Z]/, "Password must contain at least one uppercase letter")
                .matches(/[a-z]/, "Password must contain at least one lowercase letter")
                .matches(/\d/, "Password must contain at least one number")
                .matches(/[@$!%*?&]/, "Password must contain at least one special character")
                .required("Password is required"),
            confirmPassword: Yup.string()
                .oneOf([Yup.ref("newPassword")], "Passwords must match")
                .required("Please confirm your password"),
        }),
        onSubmit: async (values, { setSubmitting }) => {
            if (!token) {
                toast.error("This reset link is missing its token. Please request a new one.");
                setSubmitting(false);
                return;
            }
            try {
                await authService.resetPassword({ token, newPassword: values.newPassword });
                toast.success("Password reset successfully! Please sign in.");
                setDone(true);
                setTimeout(() => navigate("/login"), 2000);
            } catch (error) {
                toast.error(error.response?.data?.message || "This reset link is invalid or has expired.");
            } finally {
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
                            <h2>Set a New Password</h2>
                            <p className="text-muted">Choose a strong password for your account</p>
                        </div>

                        <Card>
                            <Card.Body className="p-4">
                                {!token && (
                                    <Alert variant="danger">
                                        This link is missing a reset token. Please use the link from your email, or request a new one.
                                    </Alert>
                                )}

                                {done ? (
                                    <p className="text-center text-muted">Redirecting you to sign in...</p>
                                ) : (
                                    <Form onSubmit={formik.handleSubmit}>
                                        <Form.Group className="mb-3">
                                            <Form.Label>New Password</Form.Label>
                                            <Form.Control
                                                type="password"
                                                name="newPassword"
                                                placeholder="Enter new password"
                                                value={formik.values.newPassword}
                                                onChange={formik.handleChange}
                                                onBlur={formik.handleBlur}
                                                isInvalid={formik.touched.newPassword && formik.errors.newPassword}
                                            />
                                            <Form.Control.Feedback type="invalid">{formik.errors.newPassword}</Form.Control.Feedback>
                                            <PasswordRequirements password={formik.values.newPassword} />
                                        </Form.Group>

                                        <Form.Group className="mb-3">
                                            <Form.Label>Confirm Password</Form.Label>
                                            <Form.Control
                                                type="password"
                                                name="confirmPassword"
                                                placeholder="Re-enter new password"
                                                value={formik.values.confirmPassword}
                                                onChange={formik.handleChange}
                                                onBlur={formik.handleBlur}
                                                isInvalid={formik.touched.confirmPassword && formik.errors.confirmPassword}
                                            />
                                            <Form.Control.Feedback type="invalid">{formik.errors.confirmPassword}</Form.Control.Feedback>
                                        </Form.Group>

                                        <Button type="submit" variant="primary" className="w-100" disabled={formik.isSubmitting || !token}>
                                            {formik.isSubmitting ? "Resetting..." : "Reset Password"}
                                        </Button>
                                    </Form>
                                )}

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
