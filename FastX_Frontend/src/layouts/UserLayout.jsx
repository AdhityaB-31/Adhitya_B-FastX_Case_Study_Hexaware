import UserSidebar from "../components/sidebars/UserSidebar";
import AdminSidebar from "../components/sidebars/AdminSidebar";
import OperatorSidebar from "../components/sidebars/OperatorSidebar";
import Footer from "../components/layout/Footer";
import { useAuth } from "../context/AuthContext";

const UserLayout = ({ children }) => {
    const { user } = useAuth();

    const renderSidebar = () => {
        if (user?.role === "ADMIN") {
            return <AdminSidebar />;
        }
        if (user?.role === "BUS_OPERATOR") {
            return <OperatorSidebar />;
        }
        return <UserSidebar />;
    };

    return (
        <div className="app-layout">
            {renderSidebar()}
            <div className="app-content-wrapper">
                <div className="app-content">
                    {children}
                </div>
                <Footer />
            </div>
        </div>
    );
};

export default UserLayout;
