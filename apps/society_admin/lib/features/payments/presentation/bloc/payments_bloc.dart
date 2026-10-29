// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

class PaymentsBloc extends AdminResourceBloc {
  PaymentsBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadPayments() => load(
    'payments',
    const ResourceRequest(
      AdminTargets.billingPayments,
      ResourceKind.collection,
    ),
  );

  Future<List<Map<String, dynamic>>> loadUnpaidInvoices() async =>
      (await queryCollection(AdminTargets.billingInvoices)).items
          .where((invoice) => invoice['status'] != 'PAID')
          .toList(growable: false);

  Future<Object?> recordPayment(
    Object invoiceId,
    Map<String, dynamic> payment,
  ) => execute(
    ResourceCommand(
      ResourceMethod.post,
      AdminTargets.invoicePayments(invoiceId),
      body: payment,
    ),
  );
}
