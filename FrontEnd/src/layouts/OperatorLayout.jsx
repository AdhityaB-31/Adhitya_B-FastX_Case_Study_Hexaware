import OperatorSidebar from "../components/sidebars/OperatorSidebar";
import Footer from "../components/layout/Footer";

const OperatorLayout = ({ children }) => {
    return (
        <div className="app-layout">
            <OperatorSidebar />
            <div className="app-content-wrapper">
                <div className="app-content">
                    {children}
                </div>
                <Footer />
            </div>
        </div>
    );
};

export default OperatorLayout;
