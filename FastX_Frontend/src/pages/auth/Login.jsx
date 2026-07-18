import { useState } from "react";
import { useFormik } from "formik";
import * as Yup from "yup";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { toast } from "react-toastify";
import { Container, Form, Button, Row, Col, Card, InputGroup } from "react-bootstrap";
import { GoogleLogin } from "@react-oauth/google";
import { FaEnvelope, FaLock, FaEye, FaEyeSlash } from "react-icons/fa";
import authService from "../../services/authService";
import API from "../../api/axiosConfig";
import { useAuth } from "../../context/AuthContext";

export const Login = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { login } = useAuth();
    const [showPassword, setShowPassword] = useState(false);

    const redirectAfterLogin = (token) => {
        login(token);
        const decoded = JSON.parse(atob(token.split(".")[1]));
        const role = decoded.role;
        const redirectTo = location.state?.redirectTo;
        if (redirectTo && role === "ROLE_USER") {
            navigate(redirectTo, { state: location.state?.searchParams ? { searchParams: location.state.searchParams } : undefined });
        } else if (role === "ROLE_ADMIN") navigate("/admin/dashboard");
        else if (role === "ROLE_BUS_OPERATOR") navigate("/operator/dashboard");
        else navigate("/user/dashboard");
    };

    const formik = useFormik({
        initialValues: { email: "", password: "" },
        validationSchema: Yup.object({
            email: Yup.string().email("Invalid email address").required("Email is required"),
            password: Yup.string().required("Password is required"),
        }),
        onSubmit: async (values) => {
            try {
                const token = await authService.login(values);
                toast.success("Login successful!");
                redirectAfterLogin(token);
            } catch (error) {
                const errMsg = typeof error.response?.data === "string"
                    ? error.response.data
                    : (error.response?.data?.message || "Invalid credentials. Please try again.");
                toast.error(errMsg);
            }
        },
    });

    const handleGoogleSuccess = async (credentialResponse) => {
        try {
            const idToken = credentialResponse.credential;
            const token = await authService.googleLogin(idToken);
            toast.success("Signed in with Google successfully!");
            login(token);
            const decoded = JSON.parse(atob(token.split(".")[1]));
            if (decoded.role === "ROLE_USER") {
                try {
                    const response = await API.get(`/users/${decoded.entityId}`);
                    const profile = response.data;
                    if (profile.phoneNumber === "0000000000" || !profile.gender || profile.address === "Added via Google Sign-In") {
                        toast.info("Please complete your profile details to continue.");
                        navigate("/user/profile");
                        return;
                    }
                } catch (err) {
                    console.error("Failed to check profile completeness", err);
                }
            }
            const redirectTo = location.state?.redirectTo;
            if (redirectTo && decoded.role === "ROLE_USER") {
                navigate(redirectTo, { state: location.state?.searchParams ? { searchParams: location.state.searchParams } : undefined });
            } else if (decoded.role === "ROLE_ADMIN") navigate("/admin/dashboard");
            else if (decoded.role === "ROLE_BUS_OPERATOR") navigate("/operator/dashboard");
            else navigate("/user/dashboard");
        } catch (error) {
            const errMsg = typeof error.response?.data === "string"
                ? error.response.data
                : (error.response?.data?.message || "Google sign-in failed. Please try again.");
            toast.error(errMsg);
        }
    };

    return (
        <div style={{ minHeight: "100vh", background: "linear-gradient(135deg, #f8faff 0%, #eef2ff 50%, #faf5ff 100%)", display: "flex", flexDirection: "column" }}>
            {/* Top Bar */}
            <div style={{ padding: "16px 24px", display: "flex", alignItems: "center", gap: 10 }}>
                <Link to="/" style={{ display: "flex", alignItems: "center", gap: 8, textDecoration: "none", color: "#4f46e5", fontWeight: 800, fontSize: "1.2rem" }}>
                    <img src="/FastX-Logo.png" alt="FastX Logo" style={{ height: "32px", width: "auto" }} />
                    FastX
                </Link>
            </div>

            <Container style={{ flex: 1, display: "flex", alignItems: "center", justifyContent: "center", padding: "32px 16px" }}>
                <Row className="w-100 justify-content-center">
                    <Col xs={12} sm={10} md={7} lg={5} xl={4}>
                        <Card className="shadow-lg" style={{ border: "none", borderRadius: 20 }}>
                            <Card.Body style={{ padding: "40px 32px" }}>
                                <div className="text-center mb-4">
                                    <img src="/FastX-Logo.png" alt="FastX Logo" style={{ height: "48px", width: "auto", marginBottom: 12 }} />
                                    <h4 style={{ fontWeight: 800, marginBottom: 4 }}>Sign In</h4>
                                    <p className="text-muted" style={{ fontSize: "0.88rem" }}>Enter your credentials to continue</p>
                                </div>

                                <Form onSubmit={formik.handleSubmit}>
                                    <Form.Group className="mb-3">
                                        <Form.Label>
                                            <FaEnvelope style={{ marginRight: 6, color: "#4f46e5" }} />Email
                                        </Form.Label>
                                        <Form.Control type="email" name="email" placeholder="you@example.com"
                                            value={formik.values.email} onChange={formik.handleChange}
                                            onBlur={formik.handleBlur}
                                            isInvalid={formik.touched.email && formik.errors.email} />
                                        <Form.Control.Feedback type="invalid">{formik.errors.email}</Form.Control.Feedback>
                                    </Form.Group>

                                    <Form.Group className="mb-2">
                                        <div className="d-flex justify-content-between">
                                            <Form.Label>
                                                <FaLock style={{ marginRight: 6, color: "#4f46e5" }} />Password
                                            </Form.Label>
                                            <Link to="/forgot-password" style={{ fontSize: "0.82rem", color: "#4f46e5", textDecoration: "none" }}>
                                                Forgot password?
                                            </Link>
                                        </div>
                                        <InputGroup>
                                            <Form.Control type={showPassword ? "text" : "password"} name="password" placeholder="Enter your password"
                                                value={formik.values.password} onChange={formik.handleChange}
                                                onBlur={formik.handleBlur}
                                                isInvalid={formik.touched.password && formik.errors.password} />
                                            <Button variant="outline-secondary" onClick={() => setShowPassword(!showPassword)}
                                                style={{ border: "1.5px solid #cbd5e1", borderLeft: "none", color: "#64748b", background: "white", display: "flex", alignItems: "center", justifyContent: "center", padding: "0 14px" }}>
                                                {showPassword ? <FaEyeSlash /> : <FaEye />}
                                            </Button>
                                            <Form.Control.Feedback type="invalid">{formik.errors.password}</Form.Control.Feedback>
                                        </InputGroup>
                                    </Form.Group>

                                    <Button type="submit" variant="primary" className="w-100 mt-4" style={{ padding: "11px" }}
                                        disabled={formik.isSubmitting}>
                                        {formik.isSubmitting ? "Signing in..." : "Sign In"}
                                    </Button>
                                </Form>

                                <div className="divider-text my-3">or continue with</div>

                                <div className="d-flex justify-content-center mb-3">
                                    <GoogleLogin onSuccess={handleGoogleSuccess}
                                        onError={() => toast.error("Google sign-in failed.")}
                                        useOneTap={false} width="280" text="continue_with" shape="rectangular" />
                                </div>

                                <div style={{ background: "#f8fafc", borderRadius: 10, padding: "14px 16px", textAlign: "center", marginTop: 24 }}>
                                    <p style={{ fontSize: "0.85rem", color: "#64748b", margin: "0 0 8px" }}>Don't have an account?</p>
                                    <div style={{ display: "flex", gap: 8, justifyContent: "center" }}>
                                        <Button variant="outline-primary" size="sm" onClick={() => navigate("/register/user")}>
                                            Register as User
                                        </Button>
                                        <Button variant="outline-secondary" size="sm" onClick={() => navigate("/register/operator")}>
                                            Register as Operator
                                        </Button>
                                    </div>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </div>
    );
};
