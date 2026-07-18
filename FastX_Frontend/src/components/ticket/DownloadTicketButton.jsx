import { useState } from "react";
import { toast } from "react-toastify";
import { generateTicketsPDF } from "../../utils/ticketGenerator";
import bookingService from "../../services/bookingService";

const DownloadTicketButton = ({
	bookingId,
	booking: initialBooking = null,
	variant = "button",
	disabled = false,
}) => {
	const [loading, setLoading] = useState(false);

	const handleDownload = async () => {
		if (disabled || loading) return;
		setLoading(true);
		try {
			let booking = initialBooking;
			if (!booking || !booking.passengers?.length) {
				booking = await bookingService.getBookingById(bookingId);
			}

			if (!booking) throw new Error("Booking not found");
			if (booking.bookingStatus !== "CONFIRMED") {
				toast.warning(
					"Tickets are only available for confirmed bookings.",
				);
				return;
			}
			if (!booking.passengers?.length) {
				toast.error("No passenger information found for this booking.");
				return;
			}

			const fileName = await generateTicketsPDF(booking);
			toast.success(
				`${booking.passengers.length} ticket(s) downloaded as ${fileName}`,
			);
		} catch {
			toast.error("Failed to generate tickets. Please try again.");
		} finally {
			setLoading(false);
		}
	};

	if (variant === "icon") {
		return (
			<button
				onClick={handleDownload}
				disabled={disabled || loading}
				title="Download Tickets (PDF)"
				style={{
					background: "#ffffff",
					border: "1px solid #cbd5e1",
					color: "#2563eb",
					borderRadius: 8,
					padding: "8px 14px",
					cursor: disabled || loading ? "not-allowed" : "pointer",
					fontSize: "0.84rem",
					fontWeight: 600,
					opacity: disabled ? 0.6 : 1,
				}}
			>
				{loading ? "Loading..." : "PDF"}
			</button>
		);
	}

	return (
		<button
			onClick={handleDownload}
			disabled={disabled || loading}
			style={{
				background: "#2563eb",
				border: "none",
				borderRadius: 8,
				padding: "10px 18px",
				color: "#fff",
				cursor: disabled || loading ? "not-allowed" : "pointer",
				fontWeight: 600,
				fontSize: "0.9rem",
				opacity: disabled ? 0.6 : 1,
			}}
		>
			{loading ? "Generating..." : "Download Ticket (PDF)"}
		</button>
	);
};

export default DownloadTicketButton;
