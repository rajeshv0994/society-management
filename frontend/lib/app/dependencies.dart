import '../features/public_portal/data/api_public_repository.dart';
import '../features/public_portal/domain/public_repository.dart';
import '../src/api.dart';

PublicRepository createPublicRepository() => ApiPublicRepository(ApiClient());
