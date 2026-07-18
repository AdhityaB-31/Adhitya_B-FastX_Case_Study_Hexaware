import { useEffect, useState } from "react";
import { Row, Col, Card, Table } from "react-bootstrap";
import { FaUsers, FaUserTie, FaBus, FaTicketAlt } from "react-icons/fa";
import AdminLayout from "../../layouts/AdminLayout";
import adminService from "../../services/adminService";
import userService from "../../services/userService";
import operatorService from "../../services/operatorService";
import busService from "../../services/busService";

const StatCard = ({ icon, label, value, color }) => (
    <div className="stat-card">
        <div className={`stat-icon ${color}`}>{icon}</div>
        <div className="stat-value">{value}</div>
        <div className="stat-label">{label}</div>
    </div>
);

const AdminDashboard = () => {
    const [stats, setStats] = useState({ totalUsers: 0, totalOperators: 0, totalBuses: 0, totalBookings: 0 });
    const [users, setUsers] = useState([]);
    const [operators, setOperators] = useState([]);
    const [buses, setBuses] = useState([]);

    useEffect(() => { loadData(); }, []);

    const loadData = async () => {
        try {
            const [statsData, usersData, operatorsData, busesData] = await Promise.all([
                adminService.getDashboardStats(),
                userService.getAllUsers(),
                operatorService.getAllOperators(),
                busService.getAllBuses()
            ]);
            setStats(statsData);
            setUsers(Array.isArray(usersData) ? usersData : Array.isArray(usersData?.content) ? usersData.content : []);
            setOperators(Array.isArray(operatorsData) ? operatorsData : Array.isArray(operatorsData?.content) ? operatorsData.content : []);
            setBuses(Array.isArray(busesData) ? busesData : Array.isArray(busesData?.content) ? busesData.content : []);
        } catch (error) {
            console.log("Error loading dashboard data");
        }
    };

    return (
        <AdminLayout>
            <div className="page-header">
                <div className="page-header-left">
                    <h4>Admin Dashboard</h4>
                    <p>FastX platform overview and control panel</p>
                </div>
            </div>

            {/* Stats Row */}
            <Row className="g-3 mb-4">
                <Col md={3}>
                    <StatCard icon={<FaUsers />} label="Total Users" value={stats.totalUsers} color="indigo" />
                </Col>
                <Col md={3}>
                    <StatCard icon={<FaUserTie />} label="Total Operators" value={stats.totalOperators} color="emerald" />
                </Col>
                <Col md={3}>
                    <StatCard icon={<FaBus />} label="Total Buses" value={stats.totalBuses} color="amber" />
                </Col>
                <Col md={3}>
                    <StatCard icon={<FaTicketAlt />} label="Total Bookings" value={stats.totalBookings} color="violet" />
                </Col>
            </Row>

            {/* Data Tables - Stacked Down Order */}
            <Row className="g-4">
                <Col xs={12}>
                    <Card>
                        <Card.Header>
                            <FaUsers style={{ marginRight: 8, color: "#4f46e5" }} /> User Details
                        </Card.Header>
                        <div style={{ maxHeight: 270, overflowY: "auto" }}>
                            <Table className="table-admin text-center align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Name</th>
                                        <th>Mobile Number</th>
                                        <th>Email</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {users.length > 0 ? users.map(u => (
                                        <tr key={u.userId}>
                                            <td>{u.userId}</td>
                                            <td>{u.fullName || "—"}</td>
                                            <td>{u.phoneNumber || "—"}</td>
                                            <td style={{ fontSize: "0.82rem", color: "#64748b" }}>{u.email}</td>
                                        </tr>
                                    )) : (
                                        <tr><td colSpan="4" className="text-muted py-4">No users found.</td></tr>
                                    )}
                                </tbody>
                            </Table>
                        </div>
                    </Card>
                </Col>

                <Col xs={12}>
                    <Card>
                        <Card.Header>
                            <FaUserTie style={{ marginRight: 8, color: "#10b981" }} /> Operator Details
                        </Card.Header>
                        <div style={{ maxHeight: 270, overflowY: "auto" }}>
                            <Table className="table-admin text-center align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Company</th>
                                        <th>Number of Buses</th>
                                        <th>Email</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {operators.length > 0 ? operators.map(o => {
                                        const busCount = buses.filter(b => b.operatorId === o.operatorId).length;
                                        return (
                                            <tr key={o.operatorId}>
                                                <td>{o.operatorId}</td>
                                                <td>{o.companyName}</td>
                                                <td>{busCount}</td>
                                                <td style={{ fontSize: "0.82rem", color: "#64748b" }}>{o.email}</td>
                                            </tr>
                                        );
                                    }) : (
                                        <tr><td colSpan="4" className="text-muted py-4">No operators found.</td></tr>
                                    )}
                                </tbody>
                            </Table>
                        </div>
                    </Card>
                </Col>

                <Col xs={12}>
                    <Card>
                        <Card.Header>
                            <FaBus style={{ marginRight: 8 }} /> Bus Details
                        </Card.Header>
                        <div style={{ maxHeight: 270, overflowY: "auto" }}>
                            <Table className="table-admin text-center align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Bus Name</th>
                                        <th>Journey Date</th>
                                        <th>Route</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {buses.length > 0 ? buses.map(b => (
                                        <tr key={b.busId}>
                                            <td>{b.busId}</td>
                                            <td>{b.busName}</td>
                                            <td>{b.journeyDate}</td>
                                            <td style={{ fontSize: "0.82rem", color: "#64748b" }}>{b.origin} → {b.destination}</td>
                                        </tr>
                                    )) : (
                                        <tr><td colSpan="4" className="text-muted py-4">No buses found.</td></tr>
                                    )}
                                </tbody>
                            </Table>
                        </div>
                    </Card>
                </Col>
            </Row>
        </AdminLayout>
    );
};

export default AdminDashboard;
