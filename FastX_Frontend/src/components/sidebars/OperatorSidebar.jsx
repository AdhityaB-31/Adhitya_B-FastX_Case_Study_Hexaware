import { Nav } from "react-bootstrap";
import { NavLink } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { FaBus, FaCompass, FaPlusCircle, FaSlidersH, FaSignOutAlt, FaUser, FaMoneyBillWave } from "react-icons/fa";

const OperatorSidebar = () => {
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
                    <small>Operator Portal</small>
                </div>
            </div>

            <div className="sidebar-section-label">Fleet</div>
            <Nav className="flex-column">
                <NavLink to="/operator/dashboard" className="nav-link">
                    <FaCompass /> Dashboard
                </NavLink>
                <NavLink to="/operator/buses" className="nav-link">
                    <FaBus /> My Buses
                </NavLink>
                <NavLink to="/operator/add-bus" className="nav-link">
                    <FaPlusCircle /> Add New Bus
                </NavLink>
                <NavLink to="/operator/seats" className="nav-link">
                    <FaSlidersH /> Seat Management
                </NavLink>
                <NavLink to="/operator/refunds" className="nav-link">
                    <FaMoneyBillWave /> Refund Approvals
                </NavLink>
            </Nav>

            <div className="sidebar-section-label">Account</div>
            <Nav className="flex-column">
                <NavLink to="/operator/profile" className="nav-link">
                    <FaUser /> Profile
                </NavLink>
            </Nav>

            <button onClick={handleLogout} className="logout-btn">
                <FaSignOutAlt /> Sign Out
            </button>
        </div>
    );
};

export default OperatorSidebar;
