import { jsPDF } from "jspdf";

export async function generateTicketsPDF(booking) {
	if (!booking || !booking.passengers || booking.passengers.length === 0) {
		throw new Error("No passenger data available to generate tickets");
	}

	// Load the logo
	const logoUrl = "/FastX-Logo.png";
	const img = new Image();
	img.src = logoUrl;
	await new Promise((resolve, reject) => {
		img.onload = resolve;
		img.onerror = () => {
			console.warn("Failed to load logo for PDF");
			resolve(); // continue even if logo fails
		};
	});

	const imgRatio = img.width && img.height ? img.width / img.height : 1;
	const imgH = 12;
	const imgW = imgH * imgRatio;

	const doc = new jsPDF({
		orientation: "portrait",
		unit: "mm",
		format: "a5",
	});
	const W = doc.internal.pageSize.getWidth();
	const total = booking.passengers.length;

	const {
		bookingId,
		bookingDate,
		totalAmount,
		farePerSeat,
		busName,
		busNumber,
		busType,
		origin,
		destination,
		journeyDate,
		departureTime,
		arrivalTime,
	} = booking;

	booking.passengers.forEach((passenger, index) => {
		if (index > 0) doc.addPage();

		const fareAmt = farePerSeat ?? totalAmount / total;
		const ticketNo = `FX${String(bookingId).padStart(6, "0")}-P${index + 1}`;

		let y = 14;

		if (img.width) {
			doc.addImage(img, "PNG", 12, 6, imgW, imgH);
		}

		doc.setTextColor(17, 24, 39);
		doc.setFont("helvetica", "bold");
		doc.setFontSize(15);
		doc.text("FastX Bus Ticket", W / 2, 12, { align: "center" });
		doc.setFont("helvetica", "normal");
		doc.setFontSize(8.5);
		doc.text("Simple e-ticket for your journey", W / 2, 17, {
			align: "center",
		});

		y = 24;

		doc.setDrawColor(209, 213, 219);
		doc.setLineWidth(0.3);
		doc.line(12, y, W - 12, y);

		y += 8;
		doc.setFont("helvetica", "bold");
		doc.setFontSize(9);
		doc.text("Ticket No:", 12, y);
		doc.setFont("helvetica", "normal");
		doc.text(ticketNo, 40, y);

		doc.setFont("helvetica", "bold");
		doc.text("Status:", W - 60, y);
		doc.text("CONFIRMED", W - 40, y);

		y += 6;
		doc.setDrawColor(209, 213, 219);
		doc.line(12, y, W - 12, y);

		y += 7;
		doc.setFont("helvetica", "bold");
		doc.setFontSize(10);
		doc.text("Journey Details", 12, y);

		y += 6;
		doc.setFontSize(14);
		doc.setFont("helvetica", "bold");
		doc.text(`${origin || "-"}  ->  ${destination || "-"}`, W / 2, y, {
			align: "center",
		});

		y += 8;
		doc.setFontSize(9);
		doc.setFont("helvetica", "normal");

		const col1 = 12,
			col2 = W / 2 + 4;

		doc.setFont("helvetica", "bold");
		doc.text("Journey Date:", col1, y);
		doc.setFont("helvetica", "normal");
		doc.text(String(journeyDate || bookingDate || "-"), col1 + 30, y);

		doc.setFont("helvetica", "bold");
		doc.text("Booking Date:", col2, y);
		doc.setFont("helvetica", "normal");
		doc.text(String(bookingDate || "-").substring(0, 10), col2 + 30, y);

		y += 7;
		doc.setFont("helvetica", "bold");
		doc.text("Departure:", col1, y);
		doc.setFont("helvetica", "normal");
		doc.text(String(departureTime || "-").substring(0, 5), col1 + 22, y);

		doc.setFont("helvetica", "bold");
		doc.text("Arrival:", col2, y);
		doc.setFont("helvetica", "normal");
		doc.text(String(arrivalTime || "-").substring(0, 5), col2 + 17, y);

		y += 7;
		doc.setFont("helvetica", "bold");
		doc.text("Bus Name:", col1, y);
		doc.setFont("helvetica", "normal");
		doc.text(busName || "-", col1 + 22, y);

		doc.setFont("helvetica", "bold");
		doc.text("Bus No:", col2, y);
		doc.setFont("helvetica", "normal");
		doc.text(busNumber || "-", col2 + 17, y);

		y += 7;
		doc.setFont("helvetica", "bold");
		doc.text("Bus Type:", col1, y);
		doc.setFont("helvetica", "normal");
		doc.text((busType || "-").replace(/_/g, " "), col1 + 22, y);

		doc.setFont("helvetica", "bold");
		doc.text("Booking ID:", col2, y);
		doc.setFont("helvetica", "normal");
		doc.text(`${bookingId}`, col2 + 25, y);

		y += 7;
		doc.setDrawColor(200, 200, 200);
		doc.line(12, y, W - 12, y);

		y += 7;
		doc.setFont("helvetica", "bold");
		doc.setFontSize(10);
		doc.setTextColor(33, 33, 33);
		doc.text("Passenger Details", 12, y);
		doc.setFontSize(8);
		doc.setFont("helvetica", "normal");
		doc.text(`(${index + 1} of ${total})`, 56, y);

		y += 7;
		doc.setFontSize(9);
		doc.setFont("helvetica", "bold");
		doc.text("Name:", col1, y);
		doc.setFont("helvetica", "normal");
		doc.text((passenger.name || "-").toUpperCase(), col1 + 15, y);

		y += 7;
		doc.setFont("helvetica", "bold");
		doc.text("Age:", col1, y);
		doc.setFont("helvetica", "normal");
		doc.text(String(passenger.age ?? "-"), col1 + 13, y);

		doc.setFont("helvetica", "bold");
		doc.text("Gender:", col2, y);
		doc.setFont("helvetica", "normal");
		doc.text(passenger.gender || "-", col2 + 18, y);

		y += 7;
		doc.setFont("helvetica", "bold");
		doc.text("Seat No:", col1, y);
		doc.setFont("helvetica", "normal");
		doc.text(passenger.seatNumber || "-", col1 + 20, y);

		doc.setFont("helvetica", "bold");
		doc.text("Seat Type:", col2, y);
		doc.setFont("helvetica", "normal");
		doc.text((passenger.seatType || "-").replace(/_/g, " "), col2 + 24, y);

		y += 7;
		doc.setFont("helvetica", "bold");
		doc.text("Fare Paid:", col1, y);
		doc.setFont("helvetica", "normal");
		doc.text(`Rs. ${Number(fareAmt).toFixed(0)}`, col1 + 22, y);

		y += 7;
		doc.setDrawColor(209, 213, 219);
		doc.line(12, y, W - 12, y);

		y += 7;
		doc.setFontSize(7.5);
		doc.setFont("helvetica", "normal");
		doc.setTextColor(75, 85, 99);
		doc.text(
			"This is a valid e-ticket. Please carry a digital or printed copy.",
			W / 2,
			y,
			{ align: "center" },
		);
		y += 5;
		doc.text(
			"Subject to FastX terms and conditions. fastx.in | support@fastx.in",
			W / 2,
			y,
			{ align: "center" },
		);
	});

	const fileName = `FastX_Tickets_Booking ${booking.bookingId}.pdf`;
	doc.save(fileName);
	return fileName;
}
