import { useNavigate } from "react-router-dom";
import { Container, Button } from "react-bootstrap";

const Unauthorized = () => {
    const navigate = useNavigate();

    return (
        <div style={{ minHeight: "100vh", backgroundColor: "#e8f4f8", padding: "40px 0" }}>
            <Container className="text-center mt-5">
                <h1>403</h1>
                <h3 className="text-danger">Access Denied</h3>
                <p className="text-muted mb-4">
                    You don't have permission to access this page. Please login with the correct account.
                </p>
                <Button variant="secondary" className="me-2" onClick={() => navigate(-1)}>Go Back</Button>
                <Button variant="primary" onClick={() => navigate("/login")}>Login Again</Button>
            </Container>
        </div>
    );
};

export default Unauthorized;