import Footer from "../components/layout/Footer";

const PublicLayout = ({ children }) => {
    return (
        <div className="public-layout">
            <div style={{ flex: 1 }}>
                {children}
            </div>
            <Footer />
        </div>
    );
};

export default PublicLayout;
