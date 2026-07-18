import { Nav } from "react-bootstrap";
import { NavLink } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { FaBus, FaCompass, FaUsers, FaUserTie, FaCog, FaMoneyBillWave, FaHistory, FaTicketAlt, FaSignOutAlt, FaUser } from "react-icons/fa";

const AdminSidebar = () => {
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
                    <small>Admin Control</small>
                </div>
            </div>

            <div className="sidebar-section-label">Management</div>
            <Nav className="flex-column">
                <NavLink to="/admin/dashboard" className="nav-link">
                    <FaCompass /> Dashboard
                </NavLink>
                <NavLink to="/admin/users" className="nav-link">
                    <FaUsers /> Manage Users
                </NavLink>
                <NavLink to="/admin/operators" className="nav-link">
                    <FaUserTie /> Manage Operators
                </NavLink>
                <NavLink to="/admin/buses" className="nav-link">
                    <FaBus /> Bus Manager
                </NavLink>
                <NavLink to="/admin/amenities" className="nav-link">
                    <FaCog /> Amenities
                </NavLink>
                <NavLink to="/admin/refunds" className="nav-link">
                    <FaMoneyBillWave /> Refund History
                </NavLink>
            </Nav>

            <div className="sidebar-section-label">Tickets</div>
            <Nav className="flex-column">
                <NavLink to="/user/search" className="nav-link">
                    <FaBus /> Book Ticket
                </NavLink>
                <NavLink to="/user/bookings" className="nav-link">
                    <FaHistory /> My Bookings
                </NavLink>
            </Nav>

            <div className="sidebar-section-label">Account</div>
            <Nav className="flex-column">
                <NavLink to="/admin/profile" className="nav-link">
                    <FaUser /> Profile
                </NavLink>
            </Nav>

            <button onClick={handleLogout} className="logout-btn">
                <FaSignOutAlt /> Sign Out
            </button>
        </div>
    );
};

export default AdminSidebar;
