import { useState, useEffect } from "react";
import { Card, Form, Button, Table, Row, Col } from "react-bootstrap";
import { toast } from "react-toastify";

import { useAuth } from "../../context/AuthContext";
import AdminLayout from "../../layouts/AdminLayout";
import OperatorLayout from "../../layouts/OperatorLayout";
import amenityService from "../../services/amenityService";

const ManageAmenities = () => {
    const { user } = useAuth();
    const [amenities, setAmenities] = useState([]);
    const [newAmenityName, setNewAmenityName] = useState("");
    const [loading, setLoading] = useState(true);

    const isAdmin = user?.role === "ADMIN";
    const Layout = isAdmin ? AdminLayout : OperatorLayout;

    useEffect(() => {
        loadAmenities();
    }, []);

    const loadAmenities = async () => {
        try {
            const data = await amenityService.getAllAmenities();
            const arr = Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : [];
            setAmenities(arr);
        } catch (error) {
            toast.error("Failed to load amenities");
        } finally {
            setLoading(false);
        }
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        if (!newAmenityName.trim()) {
            toast.warning("Please enter an amenity name");
            return;
        }
        try {
            await amenityService.createAmenity({ amenityName: newAmenityName.trim() });
            toast.success("Amenity created successfully!");
            setNewAmenityName("");
            loadAmenities();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to create amenity");
        }
    };

    const handleDelete = async (id, name) => {
        if (!window.confirm(`Delete amenity "${name}"?`)) return;
        try {
            await amenityService.deleteAmenity(id);
            toast.success("Amenity deleted successfully");
            loadAmenities();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to delete amenity");
        }
    };

    return (
        <Layout>
            <h4>Manage Amenities</h4>
            <p className="text-muted">Create and manage amenities available for buses</p>
            <hr />

            <Row>
                <Col md={5} className="mb-4">
                    <Card>
                        <Card.Header>Add New Amenity</Card.Header>
                        <Card.Body>
                            <Form onSubmit={handleCreate}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Amenity Name</Form.Label>
                                    <Form.Control
                                        type="text"
                                        placeholder="e.g., WiFi, AC, Sleeper, Charging Point"
                                        value={newAmenityName}
                                        onChange={(e) => setNewAmenityName(e.target.value)}
                                    />
                                </Form.Group>
                                <Button type="submit" variant="primary" className="w-100">
                                    Create Amenity
                                </Button>
                            </Form>
                        </Card.Body>
                    </Card>
                </Col>

                <Col md={7}>
                    {loading ? (
                        <p className="text-muted">Loading amenities...</p>
                    ) : amenities.length === 0 ? (
                        <p className="text-muted">No amenities found. Create one to get started.</p>
                    ) : (
                        <Table responsive className={`${isAdmin ? "table-admin" : "table-operator"} text-center align-middle`}>
                            <thead className="text-center">
                                <tr>
                                    <th>ID</th>
                                    <th>Name</th>
                                    {isAdmin && <th>Actions</th>}
                                </tr>
                            </thead>
                            <tbody>
                                {amenities.map(amenity => (
                                    <tr key={amenity.amenityId}>
                                        <td>{amenity.amenityId}</td>
                                        <td>{amenity.amenityName}</td>
                                        {isAdmin && (
                                            <td>
                                                <Button variant="danger" size="sm" onClick={() => handleDelete(amenity.amenityId, amenity.amenityName)}>
                                                    Delete
                                                </Button>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    )}
                </Col>
            </Row>
        </Layout>
    );
};

export default ManageAmenities;
