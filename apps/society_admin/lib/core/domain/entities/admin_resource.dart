import 'dart:collection';

sealed class AdminResource {
  const AdminResource();
}

/// A semantic resource identifier. URL construction belongs exclusively to
/// the data layer; presentation BLoCs only select a resource and its values.
enum AdminTargetType {
  visitors,
  visitorHosts,
  visitorTargets,
  visitorStaffApproval,
  visitorStatus,
  documents,
  staff,
  staffResetPassword,
  staffPermissions,
  staffStatus,
  staffSalaryPayments,
  staffEmployment,
  staffAttendance,
  societyProfile,
  societySubscription,
  subscriptionQuantities,
  configuration,
  configurationComplete,
  configurationResource,
  tasks,
  taskComplete,
  structure,
  structureBlocks,
  structureBlockSections,
  structureSections,
  structureSpaces,
  structureSectionSpaces,
  support,
  supportMessages,
  storage,
  storagePurchases,
  storageFile,
  residents,
  residentsPending,
  residentReview,
  residentHousehold,
  residentHouseholdMember,
  complaint,
  billingPayments,
  billingInvoices,
  invoicePayments,
  audit,
  dashboard,
  external,
  none,
}

final class AdminTarget {
  const AdminTarget(this.type, [this.values = const [], this.query = const {}]);

  final AdminTargetType type;
  final List<Object> values;
  final Map<String, String> query;

  String get cacheKey => type == AdminTargetType.external
      ? values.single.toString()
      : '${type.name}:${values.join(':')}:${Uri(queryParameters: query).query}';
}

abstract final class AdminTargets {
  static const visitors = AdminTarget(AdminTargetType.visitors);
  static const visitorHosts = AdminTarget(AdminTargetType.visitorHosts);
  static const visitorTargets = AdminTarget(AdminTargetType.visitorTargets);
  static const documents = AdminTarget(AdminTargetType.documents);
  static const staff = AdminTarget(AdminTargetType.staff);
  static const profile = AdminTarget(AdminTargetType.societyProfile);
  static const subscription = AdminTarget(AdminTargetType.societySubscription);
  static const subscriptionQuantities = AdminTarget(
    AdminTargetType.subscriptionQuantities,
  );
  static const configuration = AdminTarget(AdminTargetType.configuration);
  static const configurationComplete = AdminTarget(
    AdminTargetType.configurationComplete,
  );
  static const tasks = AdminTarget(AdminTargetType.tasks);
  static const structure = AdminTarget(AdminTargetType.structure);
  static const structureBlocks = AdminTarget(AdminTargetType.structureBlocks);
  static const structureSpaces = AdminTarget(AdminTargetType.structureSpaces);
  static const support = AdminTarget(AdminTargetType.support);
  static const storage = AdminTarget(AdminTargetType.storage);
  static const storagePurchases = AdminTarget(AdminTargetType.storagePurchases);
  static const residents = AdminTarget(AdminTargetType.residents);
  static const residentsPending = AdminTarget(AdminTargetType.residentsPending);
  static const billingPayments = AdminTarget(AdminTargetType.billingPayments);
  static const billingInvoices = AdminTarget(AdminTargetType.billingInvoices);
  static const dashboard = AdminTarget(AdminTargetType.dashboard);
  static const none = AdminTarget(AdminTargetType.none);

  static AdminTarget visitorStaffApproval(Object id) =>
      AdminTarget(AdminTargetType.visitorStaffApproval, [id]);
  static AdminTarget visitorStatus(Object id) =>
      AdminTarget(AdminTargetType.visitorStatus, [id]);
  static AdminTarget staffMember(Object id) =>
      AdminTarget(AdminTargetType.staff, [id]);
  static AdminTarget staffResetPassword(Object id) =>
      AdminTarget(AdminTargetType.staffResetPassword, [id]);
  static AdminTarget staffPermissions(Object id) =>
      AdminTarget(AdminTargetType.staffPermissions, [id]);
  static AdminTarget staffStatus(Object id) =>
      AdminTarget(AdminTargetType.staffStatus, [id]);
  static AdminTarget staffSalaryPayments(Object id) =>
      AdminTarget(AdminTargetType.staffSalaryPayments, [id]);
  static AdminTarget staffEmployment(Object id) =>
      AdminTarget(AdminTargetType.staffEmployment, [id]);
  static AdminTarget staffAttendance(Object id) =>
      AdminTarget(AdminTargetType.staffAttendance, [id]);
  static AdminTarget taskComplete(Object id) =>
      AdminTarget(AdminTargetType.taskComplete, [id]);
  static AdminTarget structureBlock(Object id) =>
      AdminTarget(AdminTargetType.structureBlocks, [id]);
  static AdminTarget structureBlockSections(Object id) =>
      AdminTarget(AdminTargetType.structureBlockSections, [id]);
  static AdminTarget structureSection(Object id) =>
      AdminTarget(AdminTargetType.structureSections, [id]);
  static AdminTarget structureSpace(Object id) =>
      AdminTarget(AdminTargetType.structureSpaces, [id]);
  static AdminTarget structureSectionSpaces(Object id) =>
      AdminTarget(AdminTargetType.structureSectionSpaces, [id]);
  static AdminTarget supportMessages(Object id) =>
      AdminTarget(AdminTargetType.supportMessages, [id]);
  static AdminTarget storageFile(Object id) =>
      AdminTarget(AdminTargetType.storageFile, [id]);
  static AdminTarget resident(Object id) =>
      AdminTarget(AdminTargetType.residents, [id]);
  static AdminTarget residentReview(Object id) =>
      AdminTarget(AdminTargetType.residentReview, [id]);
  static AdminTarget residentHousehold(Object id) =>
      AdminTarget(AdminTargetType.residentHousehold, [id]);
  static AdminTarget residentHouseholdMember(
    Object residentId,
    Object memberId,
  ) => AdminTarget(AdminTargetType.residentHouseholdMember, [
    residentId,
    memberId,
  ]);
  static AdminTarget complaint(Object id) =>
      AdminTarget(AdminTargetType.complaint, [id]);
  static AdminTarget invoicePayments(Object id) =>
      AdminTarget(AdminTargetType.invoicePayments, [id]);
  static AdminTarget configurationResource(String segment, [Object? id]) =>
      AdminTarget(AdminTargetType.configurationResource, [segment, ?id]);
  static AdminTarget audit(Map<String, String> query) =>
      AdminTarget(AdminTargetType.audit, const [], query);
  static AdminTarget external(String path) =>
      AdminTarget(AdminTargetType.external, [path]);
}

final class AdminRecord extends MapBase<String, dynamic> {
  AdminRecord._(this._values);

  factory AdminRecord.from(Map value) => AdminRecord._(
    Map.unmodifiable(
      value.map(
        (key, item) => MapEntry(key.toString(), _freezeAdminValue(item)),
      ),
    ),
  );

  static final empty = AdminRecord._(const {});
  final Map<String, dynamic> _values;

  @override
  dynamic operator [](Object? key) => _values[key];
  @override
  void operator []=(String key, dynamic value) =>
      throw UnsupportedError('AdminRecord is immutable.');
  @override
  void clear() => throw UnsupportedError('AdminRecord is immutable.');
  @override
  Iterable<String> get keys => _values.keys;
  @override
  dynamic remove(Object? key) =>
      throw UnsupportedError('AdminRecord is immutable.');
}

Object? _freezeAdminValue(Object? value) => switch (value) {
  Map value => AdminRecord.from(value),
  List value => List.unmodifiable(value.map(_freezeAdminValue)),
  _ => value,
};

class RawResource extends AdminResource {
  const RawResource(this.value);
  final Object? value;
}

class ResourceDocument extends AdminResource {
  const ResourceDocument(this.data);
  final AdminRecord data;
}

class ResourceCollection extends AdminResource {
  const ResourceCollection(this.items);
  final List<AdminRecord> items;
}

enum ResourceKind { document, collection, raw }

enum ResourceMethod {
  post,
  put,
  patch,
  delete,
  uploadLogo,
  uploadVisitorPhoto,
  uploadDocument,
  download,
  upload,
}

class ResourceRequest {
  const ResourceRequest(this.target, this.kind);
  final AdminTarget target;
  final ResourceKind kind;
}

class ResourceCommand {
  ResourceCommand(
    this.method,
    this.target, {
    Map<String, dynamic> body = const {},
    this.bytes,
    this.filename,
    this.documentType,
    this.field = 'file',
  }) : body = AdminRecord.from(body);
  final ResourceMethod method;
  final AdminTarget target;
  final AdminRecord body;
  final List<int>? bytes;
  final String? filename;
  final String? documentType;
  final String field;
}
