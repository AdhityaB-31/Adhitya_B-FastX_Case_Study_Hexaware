import { useState, useEffect, useRef } from "react";
import { toast } from "react-toastify";
import { Card, Row, Col, Form, Button, Badge, Modal, InputGroup } from "react-bootstrap";
import { FaUser, FaPhone, FaMapMarkerAlt, FaEnvelope, FaSave, FaLock, FaKey, FaBuilding, FaEye, FaEyeSlash } from "react-icons/fa";
import API from "../../api/axiosConfig";
import OperatorLayout from "../../layouts/OperatorLayout";
import { useAuth } from "../../context/AuthContext";
import authService from "../../services/authService";

const OperatorProfile = () => {
    const { user } = useAuth();
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    // Change Password Modal State
    const [showPwModal, setShowPwModal] = useState(false);
    const [pwData, setPwData] = useState({ oldPassword: "", newPassword: "", confirmPassword: "" });
    const [pwLoading, setPwLoading] = useState(false);
    const [showOldPw, setShowOldPw] = useState(false);
    const [showNewPw, setShowNewPw] = useState(false);
    const [showConfirmPw, setShowConfirmPw] = useState(false);

    const companyNameRef = useRef();
    const phoneRef = useRef();
    const addressRef = useRef();

    useEffect(() => {
        if (user?.entityId) loadProfile();
    }, [user]);

    const loadProfile = async () => {
        try {
            const response = await API.get(`/operators/${user.entityId}`);
            setProfile(response.data);
        } catch {
            toast.error("Failed to load profile");
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const companyName = companyNameRef.current?.value?.trim();
        const phoneNumber = phoneRef.current?.value?.trim();
        const address = addressRef.current?.value?.trim();

        if (!companyName) { toast.error("Company name is required"); return; }
        if (!phoneNumber || !/^[0-9]{10}$/.test(phoneNumber)) { toast.error("Enter valid 10-digit phone number"); return; }
        if (!address || address.length < 5) { toast.error("Address must be at least 5 characters"); return; }

        setSaving(true);
        try {
            const updated = { ...profile, companyName, phoneNumber, address };
            const response = await API.put(`/operators/update/${user.entityId}`, updated);
            setProfile(response.data);
            toast.success("Profile updated successfully!");
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to update profile");
        } finally {
            setSaving(false);
        }
    };

    const handlePwChange = async (e) => {
        e.preventDefault();
        const { oldPassword, newPassword, confirmPassword } = pwData;

        if (!oldPassword) { toast.error("Old password is required"); return; }
        if (oldPassword === newPassword) {
            toast.error("New password cannot be the same as the old password");
            return;
        }

        const pwRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;
        if (!newPassword || !pwRegex.test(newPassword)) {
            toast.error("Password must be at least 8 characters and include an uppercase letter, a lowercase letter, a number, and a special character (@$!%*?&)");
            return;
        }

        if (newPassword !== confirmPassword) { toast.error("Passwords do not match"); return; }

        setPwLoading(true);
        try {
            // Step 1: Verify old password by attempting to log in
            try {
                await authService.login({ email: profile.email, password: oldPassword });
            } catch (err) {
                toast.error("Verification failed: Incorrect old password");
                setPwLoading(false);
                return;
            }

            // Step 2: Update the operator's password in the profile
            const updated = { ...profile, password: newPassword };
            await API.put(`/operators/update/${user.entityId}`, updated);
            toast.success("Password changed successfully!");
            setShowPwModal(false);
            setPwData({ oldPassword: "", newPassword: "", confirmPassword: "" });
            setShowOldPw(false);
            setShowNewPw(false);
            setShowConfirmPw(false);
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to change password");
        } finally {
            setPwLoading(false);
        }
    };

    if (loading) {
        return <OperatorLayout><div className="text-center py-5 text-muted">Loading profile...</div></OperatorLayout>;
    }

    const initials = profile?.companyName?.[0]?.toUpperCase() || "O";

    return (
        <OperatorLayout>
            <div className="page-header">
                <div className="page-header-left">
                    <h4>My Profile</h4>
                    <p>Manage your operator account and travel details</p>
                </div>
            </div>

            <Row className="g-4">
                {/* Profile Card */}
                <Col md={4}>
                    <Card style={{ textAlign: "center" }}>
                        <Card.Body style={{ padding: "32px 24px" }}>
                            <div className="avatar-circle" style={{ background: "#10b981" }}>{initials}</div>
                            <h5 style={{ fontWeight: 700, marginBottom: 4 }}>{profile?.companyName || "—"}</h5>
                            <p style={{ color: "#64748b", fontSize: "0.85rem", marginBottom: 16 }}>{profile?.email}</p>
                            <Badge bg="success" style={{ marginBottom: 20 }}>Bus Operator</Badge>

                            <div style={{ textAlign: "left", borderTop: "1px solid #e2e8f0", paddingTop: 20, marginBottom: 20 }}>
                                {[
                                    { icon: <FaPhone />, label: "Phone", value: profile?.phoneNumber || "—" },
                                    { icon: <FaMapMarkerAlt />, label: "Address", value: profile?.address || "—" },
                                ].map(item => (
                                    <div key={item.label} style={{ display: "flex", gap: 12, marginBottom: 14, alignItems: "flex-start" }}>
                                        <div style={{ width: 32, height: 32, background: "#d1fae5", borderRadius: 8, display: "flex", alignItems: "center", justifyContent: "center", color: "#059669", fontSize: "0.8rem", flexShrink: 0 }}>
                                            {item.icon}
                                        </div>
                                        <div>
                                            <div style={{ fontSize: "0.72rem", color: "#94a3b8", fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.5px" }}>{item.label}</div>
                                            <div style={{ fontSize: "0.88rem", color: "#334155", fontWeight: 500 }}>{item.value}</div>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            <Button variant="outline-primary" className="w-100" onClick={() => setShowPwModal(true)}>
                                <FaKey style={{ marginRight: 8 }} /> Change Password
                            </Button>
                        </Card.Body>
                    </Card>
                </Col>

                {/* Edit Form */}
                <Col md={8}>
                    <Card>
                        <Card.Header>Edit Profile Details</Card.Header>
                        <Card.Body>
                            <form onSubmit={handleSubmit}>
                                <Row className="g-3">
                                    <Col md={6}>
                                        <Form.Label>
                                            <FaBuilding style={{ marginRight: 6, color: "#10b981" }} />Company Name
                                        </Form.Label>
                                        <input ref={companyNameRef} defaultValue={profile?.companyName || ""} placeholder="Enter company name" className="form-control" />
                                    </Col>
                                    <Col md={6}>
                                        <Form.Label>
                                            <FaPhone style={{ marginRight: 6, color: "#4f46e5" }} />Phone Number
                                        </Form.Label>
                                        <input ref={phoneRef} defaultValue={profile?.phoneNumber || ""} placeholder="10-digit phone number" className="form-control" />
                                    </Col>
                                    <Col md={6}>
                                        <Form.Label>
                                            <FaEnvelope style={{ marginRight: 6, color: "#64748b" }} />Email (Read-only)
                                        </Form.Label>
                                        <input value={profile?.email || ""} readOnly disabled className="form-control bg-light" />
                                    </Col>
                                    <Col md={12}>
                                        <Form.Label>
                                            <FaMapMarkerAlt style={{ marginRight: 6, color: "#ef4444" }} />Address
                                        </Form.Label>
                                        <textarea ref={addressRef} defaultValue={profile?.address || ""} placeholder="Enter company headquarters address" rows={3} className="form-control" />
                                    </Col>
                                </Row>

                                <Button type="submit" variant="primary" className="mt-4" disabled={saving} style={{ minWidth: 140 }}>
                                    <FaSave style={{ marginRight: 7 }} />
                                    {saving ? "Saving..." : "Save Changes"}
                                </Button>
                            </form>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            {/* Change Password Modal */}
            <Modal show={showPwModal} onHide={() => {
                setShowPwModal(false);
                setShowOldPw(false);
                setShowNewPw(false);
                setShowConfirmPw(false);
            }} centered>
                <Modal.Header closeButton>
                    <Modal.Title style={{ fontSize: "1.1rem" }}>
                        <FaLock style={{ marginRight: 8, color: "#4f46e5" }} />
                        Change Password
                    </Modal.Title>
                </Modal.Header>
                <form onSubmit={handlePwChange}>
                    <Modal.Body>
                        <div className="mb-3">
                            <Form.Label>Old Password</Form.Label>
                            <InputGroup>
                                <Form.Control
                                    type={showOldPw ? "text" : "password"}
                                    placeholder="Enter old password"
                                    value={pwData.oldPassword}
                                    onChange={e => setPwData(prev => ({ ...prev, oldPassword: e.target.value }))}
                                />
                                <Button variant="outline-secondary" onClick={() => setShowOldPw(!showOldPw)}>
                                    {showOldPw ? <FaEyeSlash /> : <FaEye />}
                                </Button>
                            </InputGroup>
                        </div>
                        <div className="mb-3">
                            <Form.Label>New Password</Form.Label>
                            <InputGroup>
                                <Form.Control
                                    type={showNewPw ? "text" : "password"}
                                    placeholder="Min 8 chars, uppercase, lowercase, number & special char"
                                    value={pwData.newPassword}
                                    onChange={e => setPwData(prev => ({ ...prev, newPassword: e.target.value }))}
                                />
                                <Button variant="outline-secondary" onClick={() => setShowNewPw(!showNewPw)}>
                                    {showNewPw ? <FaEyeSlash /> : <FaEye />}
                                </Button>
                            </InputGroup>
                            <Form.Text className="text-muted" style={{ fontSize: "0.75rem", display: "block", marginTop: 4 }}>
                                Must be at least 8 characters and include an uppercase letter, a lowercase letter, a number, and a special character (@$!%*?&).
                            </Form.Text>
                        </div>
                        <div className="mb-3">
                            <Form.Label>Confirm New Password</Form.Label>
                            <InputGroup>
                                <Form.Control
                                    type={showConfirmPw ? "text" : "password"}
                                    placeholder="Confirm new password"
                                    value={pwData.confirmPassword}
                                    onChange={e => setPwData(prev => ({ ...prev, confirmPassword: e.target.value }))}
                                />
                                <Button variant="outline-secondary" onClick={() => setShowConfirmPw(!showConfirmPw)}>
                                    {showConfirmPw ? <FaEyeSlash /> : <FaEye />}
                                </Button>
                            </InputGroup>
                        </div>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={() => setShowPwModal(false)}>Cancel</Button>
                        <Button type="submit" variant="primary" disabled={pwLoading}>
                            {pwLoading ? "Updating..." : "Update Password"}
                        </Button>
                    </Modal.Footer>
                </form>
            </Modal>
        </OperatorLayout>
    );
};

export default OperatorProfile;
