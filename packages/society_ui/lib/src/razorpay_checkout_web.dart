import 'dart:async';
import 'dart:js_interop';
import 'dart:js_interop_unsafe';

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

@JS('Razorpay')
extension type _Razorpay._(JSObject _) implements JSObject {
  external factory _Razorpay(JSObject options);
  external void open();
  external void on(String event, JSFunction handler);
}

Future<RazorpayCheckoutResult> openRazorpayCheckout(
  Map<String, dynamic> values,
) {
  final result = Completer<RazorpayCheckoutResult>();
  final options = JSObject();
  void set(String key, Object? value) =>
      options.setProperty(key.toJS, value.jsify());
  set('key', values['key_id']);
  set('amount', values['amount_paise']);
  set('currency', values['currency']);
  set('order_id', values['provider_order_id']);
  set('name', values['name']);
  set('description', values['description']);
  set('prefill', values['prefill']);
  set('theme', {'color': '#1E40AF'});
  options.setProperty(
    'handler'.toJS,
    ((JSObject response) {
      if (result.isCompleted) return;
      result.complete(
        RazorpayCheckoutResult(
          orderId: response
              .getProperty<JSString>('razorpay_order_id'.toJS)
              .toDart,
          paymentId: response
              .getProperty<JSString>('razorpay_payment_id'.toJS)
              .toDart,
          signature: response
              .getProperty<JSString>('razorpay_signature'.toJS)
              .toDart,
        ),
      );
    }).toJS,
  );
  final modal = JSObject();
  modal.setProperty(
    'ondismiss'.toJS,
    (() {
      if (!result.isCompleted)
        result.completeError(StateError('Payment cancelled'));
    }).toJS,
  );
  options.setProperty('modal'.toJS, modal);
  final checkout = _Razorpay(options);
  checkout.on(
    'payment.failed',
    ((JSObject response) {
      if (!result.isCompleted)
        result.completeError(StateError('Razorpay payment failed'));
    }).toJS,
  );
  checkout.open();
  return result.future.timeout(
    const Duration(minutes: 5),
    onTimeout: () => throw TimeoutException(
      'Razorpay checkout did not finish. Refresh the invoice before trying again.',
    ),
  );
}
