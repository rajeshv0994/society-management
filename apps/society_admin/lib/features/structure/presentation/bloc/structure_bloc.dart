// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

class StructureBloc extends AdminResourceBloc {
  StructureBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadStructure() => load(
    'structure',
    const ResourceRequest(AdminTargets.structure, ResourceKind.document),
  );

  Future<Object?> saveBlock({Object? id, required String name}) => execute(
    ResourceCommand(
      id == null ? ResourceMethod.post : ResourceMethod.put,
      id == null
          ? AdminTargets.structureBlocks
          : AdminTargets.structureBlock(id),
      body: {'name': name},
    ),
  );

  Future<Object?> saveSection({
    required Object blockId,
    Object? sectionId,
    required String name,
    required int order,
  }) => execute(
    ResourceCommand(
      sectionId == null ? ResourceMethod.post : ResourceMethod.put,
      sectionId == null
          ? AdminTargets.structureBlockSections(blockId)
          : AdminTargets.structureSection(sectionId),
      body: {'name': name, 'sectionOrder': order},
    ),
  );

  Future<Object?> saveSpace({
    Object? sectionId,
    Object? spaceId,
    required String number,
    double? areaSqft,
  }) => execute(
    ResourceCommand(
      spaceId == null ? ResourceMethod.post : ResourceMethod.put,
      spaceId != null
          ? AdminTargets.structureSpace(spaceId)
          : sectionId == null
          ? AdminTargets.structureSpaces
          : AdminTargets.structureSectionSpaces(sectionId),
      body: {'spaceNumber': number, 'areaSqft': areaSqft},
    ),
  );

  Future<Object?> deleteStructureItem(String type, Object id) {
    final target = switch (type) {
      'block' => AdminTargets.structureBlock(id),
      'section' => AdminTargets.structureSection(id),
      'space' => AdminTargets.structureSpace(id),
      _ => throw ArgumentError.value(type, 'type'),
    };
    return execute(ResourceCommand(ResourceMethod.delete, target));
  }
}
