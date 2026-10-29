import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import '../features/auth/presentation/bloc/auth_bloc.dart';
import '../features/auth/presentation/pages/auth_gate.dart';
import 'dependency_injection.dart';

class SocietyAdminApp extends StatefulWidget {
  const SocietyAdminApp({super.key});

  @override
  State<SocietyAdminApp> createState() => _SocietyAdminAppState();
}

class _SocietyAdminAppState extends State<SocietyAdminApp> {
  late final dependencies = AppDependencies.bootstrap();

  @override
  Widget build(c) => MaterialApp(
    title: 'SocietyHub Administration',
    debugShowCheckedModeBanner: false,
    theme: SocietyTheme.light(accent: const Color(0xff3157d5)),
    home: BlocProvider(
      create: (_) => AuthBloc(dependencies.auth)..add(const RestoreAuth()),
      child: AuthGate(dependencies: dependencies.societyAdmin),
    ),
  );
}
