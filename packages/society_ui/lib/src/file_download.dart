import 'dart:typed_data';

import 'package:file_saver/file_saver.dart';

Future<String?> savePdfFile(String filename, List<int> bytes) =>
    FileSaver.instance.saveFile(
      name: filename.replaceAll(RegExp(r'[^A-Za-z0-9_-]'), '_'),
      bytes: Uint8List.fromList(bytes),
      fileExtension: 'pdf',
      mimeType: MimeType.pdf,
    );
