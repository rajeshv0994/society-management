import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  const modules = [
    PortalModule('My profile', 'Always available', Icons.person),
    PortalModule(
      'Visitor desk',
      'Permission protected',
      Icons.badge,
      serviceCode: 'VISITORS',
      permissionCode: 'VISITORS',
    ),
    PortalModule(
      'Payments',
      'Permission protected',
      Icons.payments,
      serviceCode: 'PAYMENTS',
      permissionCode: 'PAYMENTS',
    ),
  ];

  test('staff navigation requires both service and permission', () {
    final visible = portalModulesForAccess(
      modules,
      permissions: {'VISITORS'},
      enabledServices: {'VISITORS', 'PAYMENTS'},
    );

    expect(visible.map((module) => module.label), [
      'My profile',
      'Visitor desk',
    ]);
  });

  test('a permission cannot expose a disabled society service', () {
    final visible = portalModulesForAccess(
      modules,
      permissions: {'VISITORS', 'PAYMENTS'},
      enabledServices: {'PAYMENTS'},
    );

    expect(visible.map((module) => module.label), ['My profile', 'Payments']);
  });
}
