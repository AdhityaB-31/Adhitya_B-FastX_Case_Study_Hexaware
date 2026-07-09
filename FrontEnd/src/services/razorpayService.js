import API from "../api/axiosConfig";

const createOrder = async (bookingId) => {
	const response = await API.post("/razorpay/create-order", { bookingId });
	return response.data;
};

const verifyPayment = async ({
	razorpayOrderId,
	razorpayPaymentId,
	razorpaySignature,
	bookingId,
	paymentMethod,
}) => {
	const response = await API.post("/razorpay/verify-payment", {
		razorpayOrderId,
		razorpayPaymentId,
		razorpaySignature,
		bookingId,
		paymentMethod,
	});
	return response.data;
};

const openCheckout = (orderData, userInfo, paymentMethod) => {
	return new Promise((resolve, reject) => {
		if (!window.Razorpay) {
			reject(
				new Error(
					"Razorpay SDK not loaded. Check your internet connection.",
				),
			);
			return;
		}

		const options = {
			key: orderData.keyId,
			amount: orderData.amountInPaise,
			currency: orderData.currency || "INR",
			name: "FastX",
			description: `Bus Booking #${orderData.bookingId}`,
			image: "/favicon.svg",
			order_id: orderData.razorpayOrderId,
			prefill: {
				name: userInfo?.name || "",
				email: userInfo?.email || "",
				contact: userInfo?.contact || "",
			},
			notes: {
				bookingId: String(orderData.bookingId),
			},
			theme: {
				color: "#e94560",
				backdrop_color: "rgba(13,13,26,0.85)",
			},
			modal: {
				backdropclose: false,
				escape: false,
				ondismiss: () => {
					reject(new Error("PAYMENT_CANCELLED"));
				},
			},
			handler: async (response) => {
				try {
					const result = await verifyPayment({
						razorpayOrderId: response.razorpay_order_id,
						razorpayPaymentId: response.razorpay_payment_id,
						razorpaySignature: response.razorpay_signature,
						bookingId: orderData.bookingId,
						paymentMethod,
					});
					resolve({
						...result,
						razorpayPaymentId: response.razorpay_payment_id,
					});
				} catch (err) {
					reject(err);
				}
			},
		};

		const rzp = new window.Razorpay(options);

		rzp.on("payment.failed", (resp) => {
			reject(new Error(resp.error?.description || "Payment failed"));
		});

		rzp.open();
	});
};

export default { createOrder, verifyPayment, openCheckout };
