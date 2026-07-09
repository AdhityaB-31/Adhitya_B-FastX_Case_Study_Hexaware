import AdminSidebar from "../components/sidebars/AdminSidebar";
import Footer from "../components/layout/Footer";

const AdminLayout = ({ children }) => {
    return (
        <div className="app-layout">
            <AdminSidebar />
            <div className="app-content-wrapper">
                <div className="app-content">
                    {children}
                </div>
                <Footer />
            </div>
        </div>
    );
};

export default AdminLayout;
