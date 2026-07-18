import { Navbar, Container, Nav, Button } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

const CustomNavbar = () => {
	const { user, logout } = useAuth();
	const navigate = useNavigate();

	const handleLogout = () => {
		logout();
		window.location.href = "/";
	};

	return (
		<Navbar bg="light" variant="light" expand="lg" className="border-bottom shadow-sm mb-4">
			<Container>
				<Navbar.Brand className="d-flex align-items-center gap-2">
					<img src="/FastX-Logo.png" alt="FastX Logo" style={{ height: "32px", objectFit: "contain" }} />
				</Navbar.Brand>

				<Nav className="ms-auto align-items-center">
					<Navbar.Text className="me-3">
						Welcome, {user?.fullName}
					</Navbar.Text>

					<Button variant="danger" onClick={handleLogout}>
						Logout
					</Button>
				</Nav>
			</Container>
		</Navbar>
	);
};

export default CustomNavbar;
