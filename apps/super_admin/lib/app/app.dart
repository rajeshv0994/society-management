import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import '../features/authentication/presentation/bloc/auth_bloc.dart';
import '../features/authentication/presentation/pages/auth_gate.dart';
import 'dependency_injection.dart';

class SuperAdminApp extends StatefulWidget {
  const SuperAdminApp({super.key});
  @override
  State<SuperAdminApp> createState() => _SuperAdminAppState();
}

class _SuperAdminAppState extends State<SuperAdminApp> {
  late final dependencies = AppDependencies.bootstrap();
  @override
  void dispose() {
    dependencies.dispose();
    super.dispose();
  }

  @override
  Widget build(c) => MaterialApp(
    title: 'SocietyHub Platform Control',
    debugShowCheckedModeBanner: false,
    theme: SocietyTheme.light(accent: const Color(0xff6e42bd)),
    home: BlocProvider(
      create: (_) =>
          AuthBloc(dependencies.authentication)..add(const AuthRestored()),
      child: AuthGate(dependencies: dependencies),
    ),
  );
}
