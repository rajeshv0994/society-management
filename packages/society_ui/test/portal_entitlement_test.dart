import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  const modules = [
    PortalModule('Home', 'Core', Icons.home),
    PortalModule('Amenities', 'Paid', Icons.pool, serviceCode: 'AMENITIES'),
    PortalModule(
      'Visitors',
      'Staff permission',
      Icons.badge,
      serviceCode: 'VISITORS',
      permissionCode: 'VISITORS',
    ),
  ];

  test('member and staff navigation follows society service entitlements', () {
    final visible = portalModulesForAccess(
      modules,
      enabledServices: {'VISITORS'},
      permissions: {'VISITORS'},
    );

    expect(visible.map((module) => module.label), ['Home', 'Visitors']);
  });

  test('staff permission remains required inside an enabled service', () {
    final visible = portalModulesForAccess(
      modules,
      enabledServices: {'VISITORS'},
      permissions: {},
    );

    expect(visible.map((module) => module.label), ['Home']);
  });

  test('catalog lookup failure preserves backward-compatible navigation', () {
    final visible = portalModulesForAccess(
      modules,
      enabledServices: null,
      permissions: {'VISITORS'},
    );

    expect(visible.map((module) => module.label), [
      'Home',
      'Amenities',
      'Visitors',
    ]);
  });
}
