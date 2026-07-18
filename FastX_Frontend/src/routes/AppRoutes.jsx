import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from "../pages/public/Home";
import { Login } from "../pages/auth/Login";
import RegisterUser from "../pages/auth/RegisterUser";
import RegisterOperator from "../pages/auth/RegisterOperator";
import { ForgotPassword } from "../pages/auth/ForgotPassword";
import { ResetPassword } from "../pages/auth/ResetPassword";
import Unauthorized from "../pages/public/Unauthorized";

import AdminDashboard from "../pages/admin/AdminDashboard";
import ManageOperators from "../pages/admin/ManageOperators";
import ManageUsers from "../pages/admin/ManageUsers";
import ManageRefunds from "../pages/admin/ManageRefunds";
import ManageBuses from "../pages/admin/ManageBuses";
import AdminProfile from "../pages/admin/AdminProfile";
import ManageAmenities from "../pages/admin/ManageAmenities";

import OperatorDashboard from "../pages/operator/OperatorDashboard";
import MyBuses from "../pages/operator/MyBuses";
import AddBus from "../pages/operator/AddBus";
import UpdateBus from "../pages/operator/UpdateBus";
import SeatManagement from "../pages/operator/SeatManagement";
import BusPassengers from "../pages/operator/BusPassengers";
import OperatorProfile from "../pages/operator/OperatorProfile";

import UserDashboard from "../pages/user/UserDashboard";
import SearchBus from "../pages/user/SearchBus";
import SeatSelection from "../pages/user/SeatSelection";
import BookingHistory from "../pages/user/BookingHistory";
import Payment from "../pages/user/Payment";
import UserProfile from "../pages/user/UserProfile";

import ProtectedRoute from "./ProtectedRoute";
import PublicLayout from "../layouts/PublicLayout";

export const AppRoutes = () => {
	return (
		<BrowserRouter>
			<Routes>
				<Route path="/" element={<PublicLayout><Home /></PublicLayout>} />
				<Route path="/login" element={<PublicLayout><Login /></PublicLayout>} />
				<Route path="/register/user" element={<PublicLayout><RegisterUser /></PublicLayout>} />
				<Route
					path="/register/operator"
					element={<PublicLayout><RegisterOperator /></PublicLayout>}
				/>
				<Route path="/forgot-password" element={<PublicLayout><ForgotPassword /></PublicLayout>} />
				<Route path="/reset-password" element={<PublicLayout><ResetPassword /></PublicLayout>} />
				<Route path="/unauthorized" element={<PublicLayout><Unauthorized /></PublicLayout>} />
				<Route path="/search" element={<PublicLayout><SearchBus /></PublicLayout>} />

				<Route
					path="/admin/dashboard"
					element={
						<ProtectedRoute roles={["ADMIN"]}>
							<AdminDashboard />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/admin/users"
					element={
						<ProtectedRoute roles={["ADMIN"]}>
							<ManageUsers />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/admin/operators"
					element={
						<ProtectedRoute roles={["ADMIN"]}>
							<ManageOperators />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/admin/refunds"
					element={
						<ProtectedRoute roles={["ADMIN"]}>
							<ManageRefunds />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/admin/buses"
					element={
						<ProtectedRoute roles={["ADMIN"]}>
							<ManageBuses />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/admin/profile"
					element={
						<ProtectedRoute roles={["ADMIN"]}>
							<AdminProfile />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/admin/amenities"
					element={
						<ProtectedRoute roles={["ADMIN"]}>
							<ManageAmenities />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/operator/dashboard"
					element={
						<ProtectedRoute roles={["BUS_OPERATOR"]}>
							<OperatorDashboard />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/operator/refunds"
					element={
						<ProtectedRoute roles={["BUS_OPERATOR"]}>
							<ManageRefunds />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/operator/amenities"
					element={
						<ProtectedRoute roles={["BUS_OPERATOR"]}>
							<ManageAmenities />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/operator/bus/:busId/passengers"
					element={
						<ProtectedRoute roles={["BUS_OPERATOR"]}>
							<BusPassengers />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/operator/buses"
					element={
						<ProtectedRoute roles={["BUS_OPERATOR"]}>
							<MyBuses />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/operator/add-bus"
					element={
						<ProtectedRoute roles={["BUS_OPERATOR"]}>
							<AddBus />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/operator/update-bus/:busId"
					element={
						<ProtectedRoute roles={["BUS_OPERATOR"]}>
							<UpdateBus />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/operator/seats/:busId"
					element={
						<ProtectedRoute roles={["BUS_OPERATOR"]}>
							<SeatManagement />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/operator/seats"
					element={
						<ProtectedRoute roles={["BUS_OPERATOR"]}>
							<MyBuses />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/operator/profile"
					element={
						<ProtectedRoute roles={["BUS_OPERATOR"]}>
							<OperatorProfile />
						</ProtectedRoute>
					}
				/>

				<Route
					path="/user/dashboard"
					element={
						<ProtectedRoute roles={["USER"]}>
							<UserDashboard />
						</ProtectedRoute>
					}
				/>
				<Route path="/user/search" element={<SearchBus />} />
				<Route
					path="/user/bus/:busId"
					element={
						<ProtectedRoute roles={["USER", "ADMIN"]}>
							<SeatSelection />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/user/bookings"
					element={
						<ProtectedRoute roles={["USER", "ADMIN"]}>
							<BookingHistory />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/user/payment/:bookingId"
					element={
						<ProtectedRoute roles={["USER", "ADMIN"]}>
							<Payment />
						</ProtectedRoute>
					}
				/>
				<Route
					path="/user/profile"
					element={
						<ProtectedRoute roles={["USER"]}>
							<UserProfile />
						</ProtectedRoute>
					}
				/>
			</Routes>
		</BrowserRouter>
	);
};
