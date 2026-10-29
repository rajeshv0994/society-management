import 'package:society_ui/society_ui.dart';
import '../../domain/entities/admin_resource.dart';

class SocietyAdminRemoteDataSource {
  const SocietyAdminRemoteDataSource(this.api);
  final ApiClient api;
  Future<Object?> load(ResourceRequest request) =>
      api.get(pathFor(request.target));
  Future<Object?> execute(ResourceCommand command) => switch (command.method) {
    ResourceMethod.post => api.post(pathFor(command.target), command.body),
    ResourceMethod.put => api.put(pathFor(command.target), command.body),
    ResourceMethod.patch => api.patch(pathFor(command.target), command.body),
    ResourceMethod.delete => api.delete(pathFor(command.target)),
    ResourceMethod.uploadLogo => api.upload(
      '/api/society/profile/logo',
      bytes: command.bytes!,
      filename: command.filename!,
    ),
    ResourceMethod.uploadVisitorPhoto => api.upload(
      '/api/visitors/${command.target.values.single}/photo',
      bytes: command.bytes!,
      filename: command.filename!,
    ),
    ResourceMethod.uploadDocument => api.upload(
      '/api/society/documents?documentType=${command.documentType}',
      bytes: command.bytes!,
      filename: command.filename!,
    ),
    ResourceMethod.download => api.getBytes(pathFor(command.target)),
    ResourceMethod.upload => api.upload(
      pathFor(command.target),
      bytes: command.bytes!,
      filename: command.filename!,
      field: command.field,
    ),
  };
  String absoluteUrl(String path) => '${api.baseUrl}$path';

  static String pathFor(AdminTarget target) {
    String value(Object? item) => Uri.encodeComponent(item.toString());
    final values = target.values;
    final path = switch (target.type) {
      AdminTargetType.visitors =>
        values.isEmpty ? '/api/visitors' : '/api/visitors/${value(values[0])}',
      AdminTargetType.visitorHosts => '/api/visitors/hosts',
      AdminTargetType.visitorTargets => '/api/visitors/targets',
      AdminTargetType.visitorStaffApproval =>
        '/api/visitors/${value(values[0])}/staff-approval',
      AdminTargetType.visitorStatus =>
        '/api/visitors/${value(values[0])}/status',
      AdminTargetType.documents => '/api/society/documents',
      AdminTargetType.staff =>
        values.isEmpty
            ? '/api/society/staff'
            : '/api/society/staff/${value(values[0])}',
      AdminTargetType.staffResetPassword =>
        '/api/society/staff/${value(values[0])}/reset-password',
      AdminTargetType.staffPermissions =>
        '/api/society/staff/${value(values[0])}/permissions',
      AdminTargetType.staffStatus =>
        '/api/society/staff/${value(values[0])}/status',
      AdminTargetType.staffSalaryPayments =>
        '/api/society/staff/${value(values[0])}/salary-payments',
      AdminTargetType.staffEmployment =>
        '/api/society/staff/${value(values[0])}/employment',
      AdminTargetType.staffAttendance =>
        '/api/society/staff/${value(values[0])}/attendance',
      AdminTargetType.societyProfile => '/api/society/profile',
      AdminTargetType.societySubscription => '/api/society/subscription',
      AdminTargetType.subscriptionQuantities =>
        '/api/society/subscription/quantities',
      AdminTargetType.configuration => '/api/society/configuration',
      AdminTargetType.configurationComplete =>
        '/api/society/configuration/complete',
      AdminTargetType.configurationResource =>
        '/api/society/configuration/${value(values[0])}${values.length > 1 ? '/${value(values[1])}' : ''}',
      AdminTargetType.tasks => '/api/tasks',
      AdminTargetType.taskComplete => '/api/tasks/${value(values[0])}/complete',
      AdminTargetType.structure => '/api/society/structure',
      AdminTargetType.structureBlocks =>
        values.isEmpty
            ? '/api/society/structure/blocks'
            : '/api/society/structure/blocks/${value(values[0])}',
      AdminTargetType.structureBlockSections =>
        '/api/society/structure/blocks/${value(values[0])}/sections',
      AdminTargetType.structureSections =>
        '/api/society/structure/sections/${value(values[0])}',
      AdminTargetType.structureSpaces =>
        values.isEmpty
            ? '/api/society/structure/spaces'
            : '/api/society/structure/spaces/${value(values[0])}',
      AdminTargetType.structureSectionSpaces =>
        '/api/society/structure/sections/${value(values[0])}/spaces',
      AdminTargetType.support => '/api/support',
      AdminTargetType.supportMessages =>
        '/api/support/${value(values[0])}/messages',
      AdminTargetType.storage => '/api/society/storage',
      AdminTargetType.storagePurchases => '/api/society/storage/purchases',
      AdminTargetType.storageFile =>
        '/api/society/storage/files/${value(values[0])}',
      AdminTargetType.residents =>
        values.isEmpty
            ? '/api/society/residents'
            : '/api/society/residents/${value(values[0])}',
      AdminTargetType.residentsPending => '/api/society/residents/pending',
      AdminTargetType.residentReview =>
        '/api/society/residents/${value(values[0])}/review',
      AdminTargetType.residentHousehold =>
        '/api/society/residents/${value(values[0])}/household',
      AdminTargetType.residentHouseholdMember =>
        '/api/society/residents/${value(values[0])}/household/${value(values[1])}',
      AdminTargetType.complaint => '/api/complaints/${value(values[0])}',
      AdminTargetType.billingPayments => '/api/billing/payments',
      AdminTargetType.billingInvoices => '/api/billing/invoices',
      AdminTargetType.invoicePayments =>
        '/api/billing/invoices/${value(values[0])}/payments',
      AdminTargetType.audit => '/api/society/audit',
      AdminTargetType.dashboard => '/api/dashboard/society',
      AdminTargetType.external => values.single.toString(),
      AdminTargetType.none => '',
    };
    if (target.query.isEmpty) return path;
    return Uri(path: path, queryParameters: target.query).toString();
  }
}
