import { createContext, useContext, useEffect, useState } from "react";
import { jwtDecode } from "jwt-decode";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
	const [user, setUser] = useState(null);
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		const token = localStorage.getItem("token");

		if (token) {
			try {
				const decoded = jwtDecode(token);

				const isExpired =
					decoded.exp && decoded.exp * 1000 < Date.now();
				if (isExpired) {
					console.warn("Stored token is expired — clearing session.");
					localStorage.removeItem("token");
					setLoading(false);
					return;
				}

				setUser({
					email: decoded.sub,
					role: decoded.role.replace("ROLE_", ""),
					entityId: decoded.entityId || null,
					fullName: decoded.displayName || decoded.sub,
				});
			} catch (error) {
				console.warn("Failed to decode stored token:", error);
				localStorage.removeItem("token");
			}
		}
		setLoading(false);
	}, []);

	const login = (token) => {
		localStorage.setItem("token", token);
		const decoded = jwtDecode(token);
		setUser({
			email: decoded.sub,
			role: decoded.role.replace("ROLE_", ""),
			entityId: decoded.entityId || null,
			fullName: decoded.displayName || decoded.sub,
		});
	};

	const logout = () => {
		localStorage.removeItem("token");
		setUser(null);
	};

	return (
		<AuthContext.Provider
			value={{
				user,
				login,
				logout,
				loading,
			}}
		>
			{children}
		</AuthContext.Provider>
	);
};

export const useAuth = () => {
	return useContext(AuthContext);
};
