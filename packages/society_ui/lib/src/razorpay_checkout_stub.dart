class RazorpayCheckoutResult {
  const RazorpayCheckoutResult({
    required this.orderId,
    required this.paymentId,
    required this.signature,
  });
  final String orderId;
  final String paymentId;
  final String signature;
}

Future<RazorpayCheckoutResult> openRazorpayCheckout(
  Map<String, dynamic> options,
) async => throw UnsupportedError(
  'Razorpay checkout is supported on Android, iOS, and web. Open the Society Admin web app on this device.',
);
