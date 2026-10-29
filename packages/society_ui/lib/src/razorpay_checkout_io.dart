import 'dart:async';
import 'dart:io';

import 'package:razorpay_flutter/razorpay_flutter.dart';

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
  Map<String, dynamic> values,
) async {
  if (!Platform.isAndroid && !Platform.isIOS) {
    throw UnsupportedError(
      'Razorpay checkout is supported on Android, iOS, and web. Open the Society Admin web app on this device.',
    );
  }

  final checkout = Razorpay();
  final result = Completer<RazorpayCheckoutResult>();

  void fail(Object error) {
    if (!result.isCompleted) result.completeError(error);
  }

  checkout.on(Razorpay.EVENT_PAYMENT_SUCCESS, (PaymentSuccessResponse value) {
    final orderId = value.orderId ?? values['provider_order_id']?.toString();
    final paymentId = value.paymentId;
    final signature = value.signature;
    if (orderId == null || paymentId == null || signature == null) {
      fail(StateError('Razorpay returned an incomplete payment confirmation.'));
      return;
    }
    if (!result.isCompleted) {
      result.complete(
        RazorpayCheckoutResult(
          orderId: orderId,
          paymentId: paymentId,
          signature: signature,
        ),
      );
    }
  });
  checkout.on(Razorpay.EVENT_PAYMENT_ERROR, (PaymentFailureResponse value) {
    fail(StateError(value.message ?? 'Razorpay payment failed'));
  });
  checkout.on(Razorpay.EVENT_EXTERNAL_WALLET, (ExternalWalletResponse value) {
    fail(
      StateError(
        'External wallet ${value.walletName ?? ''} was selected. Return to the app and refresh the invoice after payment.',
      ),
    );
  });

  try {
    checkout.open({
      'key': values['key_id'],
      'amount': values['amount_paise'],
      'currency': values['currency'],
      'order_id': values['provider_order_id'],
      'name': values['name'],
      'description': values['description'],
      'prefill': values['prefill'],
      'theme': {'color': '#1E40AF'},
    });
    return await result.future.timeout(
      const Duration(minutes: 5),
      onTimeout: () => throw TimeoutException(
        'Razorpay checkout did not finish. Refresh the invoice before trying again.',
      ),
    );
  } finally {
    checkout.clear();
  }
}
