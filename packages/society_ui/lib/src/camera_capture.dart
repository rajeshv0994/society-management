import 'dart:typed_data';
import 'package:camera/camera.dart';
import 'package:flutter/material.dart';

class VisitorPhotoCapture {
  const VisitorPhotoCapture({required this.name, required this.bytes});
  final String name;
  final Uint8List bytes;
}

Future<VisitorPhotoCapture?> captureVisitorPhoto(BuildContext context) =>
    showDialog<VisitorPhotoCapture>(
      context: context,
      barrierDismissible: false,
      builder: (_) => const _CameraCaptureDialog(),
    );

class _CameraCaptureDialog extends StatefulWidget {
  const _CameraCaptureDialog();

  @override
  State<_CameraCaptureDialog> createState() => _CameraCaptureDialogState();
}

class _CameraCaptureDialogState extends State<_CameraCaptureDialog>
    with WidgetsBindingObserver {
  CameraController? controller;
  String? error;
  bool capturing = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _initialize();
  }

  Future<void> _initialize() async {
    try {
      final cameras = await availableCameras();
      if (cameras.isEmpty)
        throw Exception('No camera was found on this device.');
      final selected = cameras.firstWhere(
        (camera) => camera.lensDirection == CameraLensDirection.back,
        orElse: () => cameras.first,
      );
      final next = CameraController(
        selected,
        ResolutionPreset.medium,
        enableAudio: false,
      );
      await next.initialize();
      if (!mounted) {
        await next.dispose();
        return;
      }
      setState(() => controller = next);
    } on CameraException catch (exception) {
      _showError(_cameraMessage(exception));
    } catch (exception) {
      _showError(exception.toString().replaceFirst('Exception: ', ''));
    }
  }

  String _cameraMessage(CameraException exception) {
    if (exception.code.toLowerCase().contains('denied')) {
      return 'Camera permission was denied. Allow camera access in the browser address bar, then try again.';
    }
    return 'Unable to start the camera: ${exception.description ?? exception.code}';
  }

  void _showError(String message) {
    if (mounted) setState(() => error = message);
  }

  Future<void> _capture() async {
    final active = controller;
    if (active == null || !active.value.isInitialized || capturing) return;
    setState(() => capturing = true);
    try {
      final photo = await active.takePicture();
      final bytes = await photo.readAsBytes();
      await active.dispose();
      controller = null;
      if (mounted) {
        Navigator.pop(
          context,
          VisitorPhotoCapture(name: photo.name, bytes: bytes),
        );
      }
    } on CameraException catch (exception) {
      _showError(
        'Unable to capture photo: ${exception.description ?? exception.code}',
      );
      if (mounted) setState(() => capturing = false);
    }
  }

  Future<void> _close() async {
    final active = controller;
    controller = null;
    await active?.dispose();
    if (mounted) Navigator.pop(context);
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.inactive) {
      controller?.dispose();
      controller = null;
    } else if (state == AppLifecycleState.resumed && controller == null) {
      _initialize();
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Capture visitor photo'),
    content: SizedBox(
      width: 520,
      child: error != null
          ? Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.no_photography_outlined, size: 52),
                  const SizedBox(height: 12),
                  Text(error!, textAlign: TextAlign.center),
                  const SizedBox(height: 12),
                  OutlinedButton.icon(
                    onPressed: () {
                      setState(() => error = null);
                      _initialize();
                    },
                    icon: const Icon(Icons.refresh),
                    label: const Text('Try again'),
                  ),
                ],
              ),
            )
          : controller == null || !controller!.value.isInitialized
          ? const SizedBox(
              height: 320,
              child: Center(child: CircularProgressIndicator()),
            )
          : AspectRatio(
              aspectRatio: controller!.value.aspectRatio,
              child: ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: CameraPreview(controller!),
              ),
            ),
    ),
    actions: [
      TextButton(onPressed: _close, child: const Text('Cancel')),
      FilledButton.icon(
        onPressed: controller?.value.isInitialized == true && !capturing
            ? _capture
            : null,
        icon: const Icon(Icons.camera_alt),
        label: Text(capturing ? 'Capturing…' : 'Capture'),
      ),
    ],
  );
}
