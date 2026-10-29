import 'package:flutter/material.dart';

abstract final class SocietyColors {
  static const navy = Color(0xff17213f);
  static const navyDark = Color(0xff0e1530);
  static const blue = Color(0xff3157d5);
  static const teal = Color(0xff12a594);
  static const canvas = Color(0xfffffbf5);
  static const surfaceMuted = Color(0xfff1f5ff);
  static const border = Color(0xffe5e8f1);
  static const textMuted = Color(0xff66708c);
  static const success = Color(0xff14866d);
  static const warning = Color(0xffb66500);
  static const danger = Color(0xffbd3551);
}

class SocietyTheme {
  const SocietyTheme._();

  static ThemeData light({Color accent = SocietyColors.teal}) {
    final scheme = ColorScheme.fromSeed(
      seedColor: SocietyColors.blue,
      brightness: Brightness.light,
      primary: SocietyColors.blue,
      secondary: accent,
      surface: Colors.white,
      error: SocietyColors.danger,
    );
    final baseText = ThemeData.light().textTheme.apply(
      bodyColor: SocietyColors.navy,
      displayColor: SocietyColors.navy,
    );
    final text = baseText.copyWith(
      displayLarge: baseText.displayLarge?.copyWith(
        fontWeight: FontWeight.w800,
        letterSpacing: -1.6,
      ),
      displayMedium: baseText.displayMedium?.copyWith(
        fontWeight: FontWeight.w800,
        letterSpacing: -1.2,
      ),
      headlineLarge: baseText.headlineLarge?.copyWith(
        fontWeight: FontWeight.w800,
        letterSpacing: -.8,
      ),
      headlineMedium: baseText.headlineMedium?.copyWith(
        fontWeight: FontWeight.w800,
        letterSpacing: -.6,
      ),
      headlineSmall: baseText.headlineSmall?.copyWith(
        fontWeight: FontWeight.w700,
        letterSpacing: -.3,
      ),
      titleLarge: baseText.titleLarge?.copyWith(fontWeight: FontWeight.w700),
      titleMedium: baseText.titleMedium?.copyWith(fontWeight: FontWeight.w700),
      labelLarge: baseText.labelLarge?.copyWith(fontWeight: FontWeight.w700),
      bodyMedium: baseText.bodyMedium?.copyWith(height: 1.45),
    );

    final rounded = RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(14),
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      textTheme: text,
      scaffoldBackgroundColor: SocietyColors.canvas,
      canvasColor: SocietyColors.canvas,
      dividerColor: SocietyColors.border,
      appBarTheme: AppBarTheme(
        elevation: 0,
        scrolledUnderElevation: 1,
        backgroundColor: SocietyColors.canvas,
        foregroundColor: SocietyColors.navy,
        surfaceTintColor: Colors.transparent,
        centerTitle: false,
        titleTextStyle: text.titleLarge?.copyWith(
          color: SocietyColors.navy,
          fontWeight: FontWeight.w800,
        ),
        toolbarHeight: 70,
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: Colors.white,
        surfaceTintColor: Colors.transparent,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
          side: const BorderSide(color: SocietyColors.border),
        ),
      ),
      dialogTheme: DialogThemeData(
        backgroundColor: SocietyColors.canvas,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 16,
          vertical: 16,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: SocietyColors.border),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: SocietyColors.border),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: SocietyColors.blue, width: 1.6),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: SocietyColors.danger),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: SocietyColors.blue,
          foregroundColor: Colors.white,
          minimumSize: const Size(48, 48),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 15),
          shape: rounded,
          textStyle: const TextStyle(fontWeight: FontWeight.w700),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: SocietyColors.navy,
          side: const BorderSide(color: SocietyColors.border),
          minimumSize: const Size(48, 48),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 15),
          shape: rounded,
          textStyle: const TextStyle(fontWeight: FontWeight.w700),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: SocietyColors.blue,
          shape: rounded,
          textStyle: const TextStyle(fontWeight: FontWeight.w700),
        ),
      ),
      iconButtonTheme: IconButtonThemeData(
        style: IconButton.styleFrom(
          foregroundColor: SocietyColors.navy,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: SocietyColors.surfaceMuted,
        side: BorderSide.none,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(100)),
        labelStyle: const TextStyle(
          color: SocietyColors.navy,
          fontWeight: FontWeight.w600,
        ),
      ),
      listTileTheme: ListTileThemeData(
        iconColor: SocietyColors.textMuted,
        textColor: SocietyColors.navy,
        selectedColor: SocietyColors.blue,
        selectedTileColor: SocietyColors.blue.withValues(alpha: .09),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 3),
      ),
      drawerTheme: const DrawerThemeData(
        backgroundColor: SocietyColors.canvas,
        surfaceTintColor: Colors.transparent,
      ),
      navigationRailTheme: NavigationRailThemeData(
        backgroundColor: Colors.white,
        indicatorColor: SocietyColors.blue.withValues(alpha: .1),
        selectedIconTheme: const IconThemeData(color: SocietyColors.blue),
        selectedLabelTextStyle: const TextStyle(
          color: SocietyColors.blue,
          fontWeight: FontWeight.w700,
        ),
      ),
      progressIndicatorTheme: const ProgressIndicatorThemeData(
        color: SocietyColors.blue,
        linearTrackColor: SocietyColors.surfaceMuted,
      ),
      snackBarTheme: SnackBarThemeData(
        backgroundColor: SocietyColors.navy,
        contentTextStyle: const TextStyle(color: Colors.white),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: SocietyColors.blue,
        foregroundColor: Colors.white,
      ),
      extensions: [SocietyRoleTheme(accent: accent)],
    );
  }
}

@immutable
class SocietyRoleTheme extends ThemeExtension<SocietyRoleTheme> {
  const SocietyRoleTheme({required this.accent});
  final Color accent;

  @override
  SocietyRoleTheme copyWith({Color? accent}) =>
      SocietyRoleTheme(accent: accent ?? this.accent);

  @override
  SocietyRoleTheme lerp(SocietyRoleTheme? other, double t) =>
      SocietyRoleTheme(accent: Color.lerp(accent, other?.accent, t) ?? accent);
}

class SocietyBrandMark extends StatelessWidget {
  const SocietyBrandMark({
    super.key,
    this.compact = false,
    this.onDark = false,
  });
  final bool compact;
  final bool onDark;

  @override
  Widget build(BuildContext context) => Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: onDark ? Colors.white : SocietyColors.blue,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Icon(
          Icons.apartment_rounded,
          size: 23,
          color: onDark ? SocietyColors.blue : Colors.white,
        ),
      ),
      if (!compact) ...[
        const SizedBox(width: 10),
        Flexible(
          child: Text(
            'SocietyHub',
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: TextStyle(
              color: onDark ? Colors.white : SocietyColors.navy,
              fontSize: 20,
              fontWeight: FontWeight.w800,
              letterSpacing: -.4,
            ),
          ),
        ),
      ],
    ],
  );
}

class SocietyLoginBackground extends StatelessWidget {
  const SocietyLoginBackground({super.key, required this.child});
  final Widget child;

  @override
  Widget build(BuildContext context) => DecoratedBox(
    decoration: const BoxDecoration(
      gradient: LinearGradient(
        colors: [SocietyColors.canvas, Color(0xffeef2ff)],
        begin: Alignment.topLeft,
        end: Alignment.bottomRight,
      ),
    ),
    child: Stack(
      children: [
        Positioned(
          right: -120,
          top: -150,
          child: _ThemeGlow(color: SocietyColors.blue, size: 360),
        ),
        Positioned(
          left: -120,
          bottom: -170,
          child: _ThemeGlow(color: SocietyColors.teal, size: 340),
        ),
        child,
      ],
    ),
  );
}

class _ThemeGlow extends StatelessWidget {
  const _ThemeGlow({required this.color, required this.size});
  final Color color;
  final double size;

  @override
  Widget build(BuildContext context) => Container(
    width: size,
    height: size,
    decoration: BoxDecoration(
      color: color.withValues(alpha: .08),
      shape: BoxShape.circle,
    ),
  );
}
