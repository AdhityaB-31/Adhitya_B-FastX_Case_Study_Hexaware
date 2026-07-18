import { Nav } from "react-bootstrap";
import { NavLink } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { FaBus, FaCompass, FaHistory, FaUser, FaSignOutAlt } from "react-icons/fa";

const UserSidebar = () => {
    const { logout } = useAuth();

    const handleLogout = () => {
        logout();
        window.location.href = "/";
    };

    return (
        <div className="sidebar">
            <div className="sidebar-brand">
                <img src="/FastX-Logo.png" alt="FastX" style={{ height: "30px", width: "auto" }} />
                <div>
                    <h5 style={{ fontSize: "1rem", margin: 0 }}>FastX</h5>
                    <small>Passenger Portal</small>
                </div>
            </div>

            <div className="sidebar-section-label">Navigation</div>
            <Nav className="flex-column">
                <NavLink to="/user/dashboard" className="nav-link">
                    <FaCompass /> Dashboard
                </NavLink>
                <NavLink to="/user/search" className="nav-link">
                    <FaBus /> Search Buses
                </NavLink>
                <NavLink to="/user/bookings" className="nav-link">
                    <FaHistory /> My Bookings
                </NavLink>
                <NavLink to="/user/profile" className="nav-link">
                    <FaUser /> My Profile
                </NavLink>
            </Nav>

            <button onClick={handleLogout} className="logout-btn">
                <FaSignOutAlt /> Sign Out
            </button>
        </div>
    );
};

export default UserSidebar;
