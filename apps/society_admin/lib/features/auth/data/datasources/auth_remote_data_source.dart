import 'package:society_ui/society_ui.dart';

class AuthRemoteDataSource {
  const AuthRemoteDataSource(this.client);

  final ApiClient client;

  Future<List<dynamic>> loadPublicSocieties() async =>
      (await client.get('/api/public/societies') as List).cast<dynamic>();
}
