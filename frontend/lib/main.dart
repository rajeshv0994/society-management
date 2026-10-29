import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:url_launcher/url_launcher.dart';
import 'app/dependencies.dart';
import 'features/public_portal/domain/public_models.dart';
import 'features/public_portal/domain/public_repository.dart';
import 'features/public_portal/presentation/bloc/public_cubits.dart';

void main() => runApp(const SocietyApp());

class SocietyApp extends StatelessWidget {
  const SocietyApp({super.key});
  @override
  Widget build(BuildContext context) => RepositoryProvider<PublicRepository>(
    create: (_) => createPublicRepository(),
    child: MaterialApp(
      title: 'SocietyHub',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xff3157d5),
          primary: const Color(0xff3157d5),
          secondary: const Color(0xff12a594),
          surface: const Color(0xfffffbf5),
        ),
        useMaterial3: true,
        scaffoldBackgroundColor: const Color(0xfffffbf5),
        textTheme: ThemeData.light().textTheme.apply(
          bodyColor: const Color(0xff17213f),
          displayColor: const Color(0xff17213f),
        ),
        cardTheme: const CardThemeData(
          elevation: 0,
          color: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(22)),
            side: BorderSide(color: Color(0xffe8eaf1)),
          ),
        ),
        inputDecorationTheme: const InputDecorationTheme(
          border: OutlineInputBorder(),
        ),
      ),
      home: const HomePage(),
    ),
  );
}

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) => const _Landing();
}

class PortalUrls {
  static const member = String.fromEnvironment(
    'MEMBER_APP_URL',
    defaultValue: 'http://localhost:8084',
  );
  static const societyAdmin = String.fromEnvironment(
    'SOCIETY_ADMIN_APP_URL',
    defaultValue: 'http://localhost:8082',
  );
  static const staff = String.fromEnvironment(
    'STAFF_APP_URL',
    defaultValue: 'http://localhost:8083',
  );
  static const superAdmin = String.fromEnvironment(
    'SUPER_ADMIN_APP_URL',
    defaultValue: 'http://localhost:8081',
  );
}

class _Landing extends StatefulWidget {
  const _Landing();

  @override
  State<_Landing> createState() => _LandingState();
}

class _LandingState extends State<_Landing> {
  final servicesKey = GlobalKey();
  final portalsKey = GlobalKey();
  final plansKey = GlobalKey();

  void open(Widget page) {
    Navigator.push(context, MaterialPageRoute(builder: (_) => page));
  }

  void scrollTo(GlobalKey key) {
    final target = key.currentContext;
    if (target != null) {
      Scrollable.ensureVisible(
        target,
        duration: const Duration(milliseconds: 550),
        curve: Curves.easeOutCubic,
      );
    }
  }

  Future<void> launchPortal(String url) async {
    final uri = Uri.tryParse(url);
    final launched =
        uri != null &&
        await launchUrl(
          uri,
          mode: LaunchMode.platformDefault,
          webOnlyWindowName: '_self',
        );
    if (!launched && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Unable to open this portal right now.')),
      );
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    backgroundColor: const Color(0xfffffbf5),
    body: SelectionArea(
      child: CustomScrollView(
        slivers: [
          SliverAppBar(
            pinned: true,
            backgroundColor: const Color(0xfffffbf5).withValues(alpha: .96),
            surfaceTintColor: Colors.transparent,
            toolbarHeight: 76,
            titleSpacing: 24,
            title: _BrandMark(compact: MediaQuery.sizeOf(context).width < 460),
            actions: [
              if (MediaQuery.sizeOf(context).width >= 760) ...[
                TextButton(
                  onPressed: () => scrollTo(servicesKey),
                  child: const Text('Services'),
                ),
                TextButton(
                  onPressed: () => scrollTo(plansKey),
                  child: const Text('Plans'),
                ),
                TextButton(
                  onPressed: () => scrollTo(portalsKey),
                  child: const Text('Open portal'),
                ),
                const SizedBox(width: 8),
              ],
              FilledButton(
                onPressed: () => open(const SocietyRegistrationPage()),
                child: Text(
                  MediaQuery.sizeOf(context).width >= 520
                      ? 'Register society'
                      : 'Register',
                ),
              ),
              const SizedBox(width: 20),
            ],
          ),
          SliverToBoxAdapter(
            child: _Hero(
              onRegister: () => open(const SocietyRegistrationPage()),
              onPortals: () => scrollTo(portalsKey),
            ),
          ),
          const SliverToBoxAdapter(child: _TrustStrip()),
          SliverToBoxAdapter(
            child: _Section(
              key: servicesKey,
              eyebrow: 'EVERYDAY SOCIETY MANAGEMENT',
              title: 'Everything your community needs, in one place.',
              subtitle:
                  'Replace registers, spreadsheets, and scattered chats with connected workflows built for every role.',
              child: const _ServicesGrid(),
            ),
          ),
          SliverToBoxAdapter(
            child: Container(
              color: const Color(0xfff1f5ff),
              child: _Section(
                key: plansKey,
                eyebrow: 'FLEXIBLE PLANS',
                title: 'Start small. Scale with your society.',
                subtitle:
                    'Explore the plans currently available from SocietyHub. Custom plans are available for unique workflows.',
                child: _PlansSection(
                  onCustomPlan: () => open(const CustomRequirementPage()),
                ),
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: _Section(
              key: portalsKey,
              eyebrow: 'ONE PLATFORM, FOUR EXPERIENCES',
              title: 'Choose your SocietyHub workspace.',
              subtitle:
                  'Each role gets a focused application with the information and tools it needs.',
              child: _PortalGrid(onOpen: launchPortal),
            ),
          ),
          SliverToBoxAdapter(
            child: _GettingStarted(
              onActivate: () => open(const ActivationPage()),
              onResidentRegister: () => open(const ResidentRegistrationPage()),
            ),
          ),
          SliverToBoxAdapter(
            child: _FinalCta(
              onRegister: () => open(const SocietyRegistrationPage()),
              onCustomPlan: () => open(const CustomRequirementPage()),
            ),
          ),
          const SliverToBoxAdapter(child: _Footer()),
        ],
      ),
    ),
  );
}

class _BrandMark extends StatelessWidget {
  const _BrandMark({this.compact = false});
  final bool compact;

  @override
  Widget build(BuildContext context) => Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      Container(
        width: 38,
        height: 38,
        decoration: BoxDecoration(
          color: const Color(0xff3157d5),
          borderRadius: BorderRadius.circular(12),
        ),
        child: const Icon(
          Icons.apartment_rounded,
          color: Colors.white,
          size: 22,
        ),
      ),
      if (!compact) ...[
        const SizedBox(width: 10),
        const Text(
          'SocietyHub',
          style: TextStyle(fontWeight: FontWeight.w800, letterSpacing: -.4),
        ),
      ],
    ],
  );
}

class _Hero extends StatelessWidget {
  const _Hero({required this.onRegister, required this.onPortals});
  final VoidCallback onRegister;
  final VoidCallback onPortals;

  @override
  Widget build(BuildContext context) => Container(
    margin: const EdgeInsets.fromLTRB(20, 16, 20, 0),
    decoration: BoxDecoration(
      gradient: const LinearGradient(
        colors: [Color(0xff111a3a), Color(0xff253f94)],
        begin: Alignment.topLeft,
        end: Alignment.bottomRight,
      ),
      borderRadius: BorderRadius.circular(32),
    ),
    child: Stack(
      children: [
        const Positioned(
          right: -90,
          top: -110,
          child: _Glow(size: 330, color: Color(0xff5e7dff)),
        ),
        const Positioned(
          left: -90,
          bottom: -150,
          child: _Glow(size: 300, color: Color(0xff12a594)),
        ),
        Padding(
          padding: EdgeInsets.symmetric(
            horizontal: MediaQuery.sizeOf(context).width >= 900 ? 72 : 24,
            vertical: MediaQuery.sizeOf(context).width >= 700 ? 84 : 56,
          ),
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 1120),
              child: LayoutBuilder(
                builder: (context, constraints) {
                  final wide = constraints.maxWidth >= 820;
                  final copy = _HeroCopy(
                    onRegister: onRegister,
                    onPortals: onPortals,
                  );
                  if (!wide) return copy;
                  return Row(
                    children: [
                      Expanded(flex: 6, child: copy),
                      const SizedBox(width: 56),
                      const Expanded(flex: 4, child: _HeroDashboard()),
                    ],
                  );
                },
              ),
            ),
          ),
        ),
      ],
    ),
  );
}

class _Glow extends StatelessWidget {
  const _Glow({required this.size, required this.color});
  final double size;
  final Color color;

  @override
  Widget build(BuildContext context) => Container(
    width: size,
    height: size,
    decoration: BoxDecoration(
      shape: BoxShape.circle,
      color: color.withValues(alpha: .18),
    ),
  );
}

class _HeroCopy extends StatelessWidget {
  const _HeroCopy({required this.onRegister, required this.onPortals});
  final VoidCallback onRegister;
  final VoidCallback onPortals;

  @override
  Widget build(BuildContext context) => Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
        decoration: BoxDecoration(
          color: Colors.white.withValues(alpha: .1),
          borderRadius: BorderRadius.circular(100),
          border: Border.all(color: Colors.white24),
        ),
        child: const Text(
          'SMARTER COMMUNITIES START HERE',
          style: TextStyle(
            color: Color(0xffbfcaff),
            fontSize: 12,
            fontWeight: FontWeight.w700,
            letterSpacing: 1.2,
          ),
        ),
      ),
      const SizedBox(height: 24),
      Text(
        'Your entire society,\nworking beautifully together.',
        style: TextStyle(
          color: Colors.white,
          fontSize: MediaQuery.sizeOf(context).width >= 700 ? 54 : 38,
          height: 1.05,
          fontWeight: FontWeight.w800,
          letterSpacing: -1.6,
        ),
      ),
      const SizedBox(height: 20),
      ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 670),
        child: const Text(
          'From visitor security and maintenance billing to resident communication and staff duties—SocietyHub keeps every role connected.',
          style: TextStyle(
            color: Color(0xffd9def4),
            fontSize: 18,
            height: 1.55,
          ),
        ),
      ),
      const SizedBox(height: 32),
      Wrap(
        spacing: 12,
        runSpacing: 12,
        children: [
          FilledButton.icon(
            style: FilledButton.styleFrom(
              backgroundColor: Colors.white,
              foregroundColor: const Color(0xff17213f),
              padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 18),
            ),
            onPressed: onRegister,
            icon: const Icon(Icons.arrow_forward_rounded),
            label: const Text('Start your society'),
          ),
          OutlinedButton.icon(
            style: OutlinedButton.styleFrom(
              foregroundColor: Colors.white,
              side: const BorderSide(color: Colors.white38),
              padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 18),
            ),
            onPressed: onPortals,
            icon: const Icon(Icons.grid_view_rounded),
            label: const Text('Open your portal'),
          ),
        ],
      ),
    ],
  );
}

class _HeroDashboard extends StatelessWidget {
  const _HeroDashboard();

  @override
  Widget build(BuildContext context) => Transform.rotate(
    angle: .025,
    child: Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xfff8faff),
        borderRadius: BorderRadius.circular(24),
        boxShadow: const [
          BoxShadow(
            color: Colors.black26,
            blurRadius: 40,
            offset: Offset(0, 20),
          ),
        ],
      ),
      child: Column(
        children: [
          Row(
            children: [
              const CircleAvatar(
                backgroundColor: Color(0xffe6ebff),
                child: Icon(Icons.domain_rounded, color: Color(0xff3157d5)),
              ),
              const SizedBox(width: 10),
              const Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Good morning',
                      style: TextStyle(fontSize: 12, color: Color(0xff6b7390)),
                    ),
                    Text(
                      'Green Valley Society',
                      style: TextStyle(fontWeight: FontWeight.w700),
                    ),
                  ],
                ),
              ),
              IconButton(
                onPressed: null,
                icon: const Icon(Icons.notifications_none_rounded),
              ),
            ],
          ),
          const SizedBox(height: 18),
          const Row(
            children: [
              Expanded(
                child: _MiniMetric(
                  '248',
                  'Homes',
                  Icons.home_work_outlined,
                  Color(0xff3157d5),
                ),
              ),
              SizedBox(width: 10),
              Expanded(
                child: _MiniMetric(
                  '12',
                  'Visitors',
                  Icons.badge_outlined,
                  Color(0xff12a594),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: const Color(0xfffff5e6),
              borderRadius: BorderRadius.circular(16),
            ),
            child: const Row(
              children: [
                CircleAvatar(
                  backgroundColor: Color(0xffffe1ab),
                  child: Icon(
                    Icons.campaign_outlined,
                    color: Color(0xffa55d00),
                  ),
                ),
                SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Community notice',
                        style: TextStyle(fontWeight: FontWeight.w700),
                      ),
                      Text(
                        'Water tank cleaning · Sunday',
                        style: TextStyle(fontSize: 12),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    ),
  );
}

class _MiniMetric extends StatelessWidget {
  const _MiniMetric(this.value, this.label, this.icon, this.color);
  final String value;
  final String label;
  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(14),
    decoration: BoxDecoration(
      color: color.withValues(alpha: .08),
      borderRadius: BorderRadius.circular(16),
    ),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, color: color),
        const SizedBox(height: 16),
        Text(
          value,
          style: const TextStyle(fontSize: 25, fontWeight: FontWeight.w800),
        ),
        Text(label, style: const TextStyle(color: Color(0xff6b7390))),
      ],
    ),
  );
}

class _TrustStrip extends StatelessWidget {
  const _TrustStrip();

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 34),
    child: Wrap(
      alignment: WrapAlignment.center,
      spacing: 34,
      runSpacing: 16,
      children: const [
        _TrustItem(Icons.lock_outline_rounded, 'Role-based access'),
        _TrustItem(Icons.shield_outlined, 'Tenant-isolated data'),
        _TrustItem(Icons.devices_rounded, 'Web & mobile ready'),
        _TrustItem(Icons.support_agent_rounded, 'Connected support'),
      ],
    ),
  );
}

class _TrustItem extends StatelessWidget {
  const _TrustItem(this.icon, this.text);
  final IconData icon;
  final String text;

  @override
  Widget build(BuildContext context) => Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      Icon(icon, size: 20, color: const Color(0xff3157d5)),
      const SizedBox(width: 8),
      Text(text, style: const TextStyle(fontWeight: FontWeight.w600)),
    ],
  );
}

class _Section extends StatelessWidget {
  const _Section({
    super.key,
    required this.eyebrow,
    required this.title,
    required this.subtitle,
    required this.child,
  });
  final String eyebrow;
  final String title;
  final String subtitle;
  final Widget child;

  @override
  Widget build(BuildContext context) => Padding(
    padding: EdgeInsets.symmetric(
      horizontal: MediaQuery.sizeOf(context).width >= 700 ? 40 : 20,
      vertical: MediaQuery.sizeOf(context).width >= 700 ? 82 : 56,
    ),
    child: Center(
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 1180),
        child: Column(
          children: [
            Text(
              eyebrow,
              style: const TextStyle(
                color: Color(0xff3157d5),
                fontSize: 12,
                fontWeight: FontWeight.w800,
                letterSpacing: 1.5,
              ),
            ),
            const SizedBox(height: 12),
            Text(
              title,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: MediaQuery.sizeOf(context).width >= 700 ? 38 : 29,
                height: 1.12,
                fontWeight: FontWeight.w800,
                letterSpacing: -1,
              ),
            ),
            const SizedBox(height: 14),
            ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 720),
              child: Text(
                subtitle,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  color: Color(0xff66708c),
                  fontSize: 16,
                  height: 1.55,
                ),
              ),
            ),
            const SizedBox(height: 42),
            child,
          ],
        ),
      ),
    ),
  );
}

class _ServicesGrid extends StatelessWidget {
  const _ServicesGrid();

  static const services = [
    (
      Icons.badge_outlined,
      'Visitor management',
      'Pre-approve guests and manage secure check-in and check-out from the gate.',
      Color(0xffe9edff),
      Color(0xff3157d5),
    ),
    (
      Icons.receipt_long_outlined,
      'Billing & collections',
      'Create maintenance invoices, track balances, and maintain a transparent payment ledger.',
      Color(0xfffff1df),
      Color(0xffb66500),
    ),
    (
      Icons.support_agent_rounded,
      'Complaints & helpdesk',
      'Route resident requests, assign staff, and follow every issue through resolution.',
      Color(0xffffe9ec),
      Color(0xffbd3551),
    ),
    (
      Icons.groups_2_outlined,
      'Residents & staff',
      'Manage approvals, occupancy, staff profiles, permissions, shifts, and duties.',
      Color(0xffe5f8f4),
      Color(0xff087e70),
    ),
    (
      Icons.pool_outlined,
      'Amenities & bookings',
      'Publish facilities, prevent schedule collisions, and calculate booking charges.',
      Color(0xffeee9ff),
      Color(0xff6e42bd),
    ),
    (
      Icons.campaign_outlined,
      'Notices & communication',
      'Keep owners, tenants, and staff informed with audience-aware announcements.',
      Color(0xffe7f4ff),
      Color(0xff1873a8),
    ),
  ];

  @override
  Widget build(BuildContext context) => LayoutBuilder(
    builder: (context, constraints) {
      final columns = constraints.maxWidth >= 960
          ? 3
          : constraints.maxWidth >= 620
          ? 2
          : 1;
      final width = (constraints.maxWidth - (columns - 1) * 16) / columns;
      return Wrap(
        spacing: 16,
        runSpacing: 16,
        children: [
          for (final service in services)
            SizedBox(
              width: width,
              child: _ServiceCard(
                icon: service.$1,
                title: service.$2,
                description: service.$3,
                background: service.$4,
                foreground: service.$5,
              ),
            ),
        ],
      );
    },
  );
}

class _ServiceCard extends StatelessWidget {
  const _ServiceCard({
    required this.icon,
    required this.title,
    required this.description,
    required this.background,
    required this.foreground,
  });
  final IconData icon;
  final String title;
  final String description;
  final Color background;
  final Color foreground;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 50,
            height: 50,
            decoration: BoxDecoration(
              color: background,
              borderRadius: BorderRadius.circular(15),
            ),
            child: Icon(icon, color: foreground),
          ),
          const SizedBox(height: 22),
          Text(
            title,
            style: const TextStyle(fontSize: 19, fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 9),
          Text(
            description,
            style: const TextStyle(color: Color(0xff66708c), height: 1.5),
          ),
        ],
      ),
    ),
  );
}

class _PlansSection extends StatelessWidget {
  const _PlansSection({required this.onCustomPlan});
  final VoidCallback onCustomPlan;

  @override
  Widget build(BuildContext context) => BlocProvider(
    create: (context) => PlansCubit(context.read<PublicRepository>())..load(),
    child: BlocBuilder<PlansCubit, PublicState<List<PublicRecord>>>(
      builder: (context, state) {
        final plans = state.data ?? const <PublicRecord>[];
        if (state.status == PublicStatus.initial ||
            state.status == PublicStatus.loading) {
          return const SizedBox(
            height: 180,
            child: Center(child: CircularProgressIndicator()),
          );
        }
        if (plans.isEmpty) {
          return _PlansFallback(onCustomPlan: onCustomPlan);
        }
        return LayoutBuilder(
          builder: (context, constraints) {
            final columns = constraints.maxWidth >= 900
                ? 3
                : constraints.maxWidth >= 580
                ? 2
                : 1;
            final shown = plans.take(5).toList();
            final width = (constraints.maxWidth - (columns - 1) * 16) / columns;
            return Wrap(
              spacing: 16,
              runSpacing: 16,
              alignment: WrapAlignment.center,
              children: [
                for (final plan in shown)
                  SizedBox(
                    width: width,
                    child: _PlanCard(
                      plan: plan,
                      onStart: () => Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => const SocietyRegistrationPage(),
                        ),
                      ),
                    ),
                  ),
                SizedBox(
                  width: width,
                  child: _CustomPlanCard(onPressed: onCustomPlan),
                ),
              ],
            );
          },
        );
      },
    ),
  );
}

class _PlansFallback extends StatelessWidget {
  const _PlansFallback({required this.onCustomPlan});
  final VoidCallback onCustomPlan;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(28),
      child: Row(
        children: [
          const Icon(Icons.cloud_off_outlined, color: Color(0xff66708c)),
          const SizedBox(width: 14),
          const Expanded(
            child: Text(
              'Plan pricing is temporarily unavailable. Tell us what your society needs.',
            ),
          ),
          TextButton(
            onPressed: onCustomPlan,
            child: const Text('Request a plan'),
          ),
        ],
      ),
    ),
  );
}

class _PlanCard extends StatelessWidget {
  const _PlanCard({required this.plan, required this.onStart});
  final Map<String, dynamic> plan;
  final VoidCallback onStart;

  @override
  Widget build(BuildContext context) {
    final name = (plan['name'] ?? plan['code'] ?? 'Plan').toString();
    final rawPrice =
        plan['price_per_space'] ??
        plan['pricePerSpace'] ??
        plan['price_monthly'] ??
        0;
    final price = num.tryParse(rawPrice.toString()) ?? 0;
    final maxFlats =
        plan['max_spaces'] ??
        plan['max_units'] ??
        plan['max_flats'] ??
        plan['maxFlats'];
    final trialDuration =
        plan['trial_duration_value'] ??
        plan['trialDurationValue'] ??
        plan['trial_duration_months'] ??
        plan['trialDurationMonths'];
    final trialUnit =
        (plan['trial_duration_unit'] ?? plan['trialDurationUnit'] ?? 'MONTHS')
            .toString()
            .toLowerCase();
    final staffPrice =
        num.tryParse(
          (plan['price_per_staff'] ?? plan['pricePerStaff'] ?? 0).toString(),
        ) ??
        0;
    final maxStaff = plan['max_staff'] ?? plan['maxStaff'];
    final isTrial = (plan['code'] ?? '').toString().toUpperCase() == 'TRIAL';
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(name, style: const TextStyle(fontWeight: FontWeight.w700)),
            const SizedBox(height: 12),
            Text(
              price == 0
                  ? isTrial
                        ? 'Free trial'
                        : 'Let’s talk'
                  : '₹${price.toStringAsFixed(0)} / space / month',
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w800),
            ),
            const SizedBox(height: 10),
            Text(
              maxFlats == null
                  ? 'Flexible society size'
                  : 'Up to $maxFlats spaces',
              style: const TextStyle(color: Color(0xff66708c)),
            ),
            if (isTrial && trialDuration != null) ...[
              const SizedBox(height: 6),
              Text(
                '$trialDuration ${trialDuration == 1 ? trialUnit.replaceAll('s', '') : trialUnit} trial • up to ${maxFlats ?? 'configured'} spaces',
                style: const TextStyle(color: Color(0xff66708c)),
              ),
            ],
            const SizedBox(height: 6),
            Text(
              staffPrice == 0
                  ? 'Includes up to ${maxStaff ?? 'configured'} staff'
                  : '₹${staffPrice.toStringAsFixed(0)} / staff / month • up to ${maxStaff ?? 'custom'}',
              style: const TextStyle(color: Color(0xff66708c)),
            ),
            const SizedBox(height: 22),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton(
                onPressed: onStart,
                child: const Text('Get started'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _CustomPlanCard extends StatelessWidget {
  const _CustomPlanCard({required this.onPressed});
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) => Card(
    color: const Color(0xff17213f),
    child: Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(Icons.tune_rounded, color: Color(0xffaebcff)),
          const SizedBox(height: 18),
          const Text(
            'Need something custom?',
            style: TextStyle(
              color: Colors.white,
              fontSize: 20,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 9),
          const Text(
            'Choose your modules, capacity, and workflow.',
            style: TextStyle(color: Color(0xffcbd1e8)),
          ),
          const SizedBox(height: 22),
          TextButton(
            style: TextButton.styleFrom(foregroundColor: Colors.white),
            onPressed: onPressed,
            child: const Text('Build a custom plan →'),
          ),
        ],
      ),
    ),
  );
}

class _PortalGrid extends StatelessWidget {
  const _PortalGrid({required this.onOpen});
  final Future<void> Function(String) onOpen;

  @override
  Widget build(BuildContext context) => LayoutBuilder(
    builder: (context, constraints) {
      final columns = constraints.maxWidth >= 980
          ? 4
          : constraints.maxWidth >= 600
          ? 2
          : 1;
      final width = (constraints.maxWidth - (columns - 1) * 16) / columns;
      final portals = [
        (
          'Member',
          'For residents',
          'Payments, notices, visitors, amenities, complaints, and household.',
          Icons.home_rounded,
          const Color(0xffd14d25),
          PortalUrls.member,
        ),
        (
          'Society Admin',
          'For committee & managers',
          'Residents, structure, staff, billing, operations, and support.',
          Icons.apartment_rounded,
          const Color(0xff3157d5),
          PortalUrls.societyAdmin,
        ),
        (
          'Staff',
          'For security & operations',
          'Assigned duties, visitor desk, and complaint progress.',
          Icons.badge_rounded,
          const Color(0xff087e8b),
          PortalUrls.staff,
        ),
        (
          'Platform Admin',
          'For SocietyHub operations',
          'Societies, plans, approvals, support, and audit.',
          Icons.admin_panel_settings_rounded,
          const Color(0xff6e42bd),
          PortalUrls.superAdmin,
        ),
      ];
      return Wrap(
        spacing: 16,
        runSpacing: 16,
        children: [
          for (final portal in portals)
            SizedBox(
              width: width,
              child: _PortalCard(
                title: portal.$1,
                audience: portal.$2,
                description: portal.$3,
                icon: portal.$4,
                color: portal.$5,
                onPressed: () => onOpen(portal.$6),
              ),
            ),
        ],
      );
    },
  );
}

class _PortalCard extends StatelessWidget {
  const _PortalCard({
    required this.title,
    required this.audience,
    required this.description,
    required this.icon,
    required this.color,
    required this.onPressed,
  });
  final String title;
  final String audience;
  final String description;
  final IconData icon;
  final Color color;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) => Card(
    clipBehavior: Clip.antiAlias,
    child: InkWell(
      onTap: onPressed,
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              width: 50,
              height: 50,
              decoration: BoxDecoration(
                color: color.withValues(alpha: .11),
                borderRadius: BorderRadius.circular(15),
              ),
              child: Icon(icon, color: color),
            ),
            const SizedBox(height: 22),
            Text(
              audience.toUpperCase(),
              style: TextStyle(
                color: color,
                fontSize: 10,
                fontWeight: FontWeight.w800,
                letterSpacing: 1,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              title,
              style: const TextStyle(fontSize: 21, fontWeight: FontWeight.w800),
            ),
            const SizedBox(height: 10),
            Text(
              description,
              style: const TextStyle(color: Color(0xff66708c), height: 1.45),
            ),
            const SizedBox(height: 22),
            Row(
              children: [
                Expanded(
                  child: Text(
                    'Open application',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(color: color, fontWeight: FontWeight.w700),
                  ),
                ),
                const SizedBox(width: 8),
                Icon(Icons.arrow_outward_rounded, color: color, size: 20),
              ],
            ),
          ],
        ),
      ),
    ),
  );
}

class _GettingStarted extends StatelessWidget {
  const _GettingStarted({
    required this.onActivate,
    required this.onResidentRegister,
  });
  final VoidCallback onActivate;
  final VoidCallback onResidentRegister;

  @override
  Widget build(BuildContext context) => Container(
    color: const Color(0xff17213f),
    padding: EdgeInsets.symmetric(
      horizontal: MediaQuery.sizeOf(context).width >= 700 ? 40 : 20,
      vertical: 70,
    ),
    child: Center(
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 1120),
        child: LayoutBuilder(
          builder: (context, constraints) {
            final wide = constraints.maxWidth >= 800;
            final intro = const Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'GETTING STARTED',
                  style: TextStyle(
                    color: Color(0xffaebcff),
                    fontWeight: FontWeight.w800,
                    fontSize: 12,
                    letterSpacing: 1.4,
                  ),
                ),
                SizedBox(height: 12),
                Text(
                  'Already invited to SocietyHub?',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 32,
                    height: 1.15,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                SizedBox(height: 14),
                Text(
                  'Activate a newly approved society or submit your resident registration.',
                  style: TextStyle(color: Color(0xffcbd1e8), height: 1.5),
                ),
              ],
            );
            final actions = Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                FilledButton.icon(
                  style: FilledButton.styleFrom(
                    backgroundColor: Colors.white,
                    foregroundColor: const Color(0xff17213f),
                  ),
                  onPressed: onActivate,
                  icon: const Icon(Icons.key_rounded),
                  label: const Text('Activate society'),
                ),
                OutlinedButton.icon(
                  style: OutlinedButton.styleFrom(
                    foregroundColor: Colors.white,
                    side: const BorderSide(color: Colors.white38),
                  ),
                  onPressed: onResidentRegister,
                  icon: const Icon(Icons.person_add_alt_1_rounded),
                  label: const Text('Register as resident'),
                ),
              ],
            );
            if (!wide) {
              return Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [intro, const SizedBox(height: 28), actions],
              );
            }
            return Row(
              children: [
                Expanded(child: intro),
                const SizedBox(width: 40),
                actions,
              ],
            );
          },
        ),
      ),
    ),
  );
}

class _FinalCta extends StatelessWidget {
  const _FinalCta({required this.onRegister, required this.onCustomPlan});
  final VoidCallback onRegister;
  final VoidCallback onCustomPlan;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 74),
    child: Center(
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 900),
        child: Column(
          children: [
            const Text(
              'Ready for a calmer, better-connected society?',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 34,
                height: 1.15,
                fontWeight: FontWeight.w800,
                letterSpacing: -.8,
              ),
            ),
            const SizedBox(height: 14),
            const Text(
              'Register today or tell us about the workflow your community needs.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Color(0xff66708c), fontSize: 16),
            ),
            const SizedBox(height: 28),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                FilledButton(
                  onPressed: onRegister,
                  child: const Text('Register your society'),
                ),
                OutlinedButton(
                  onPressed: onCustomPlan,
                  child: const Text('Request custom plan'),
                ),
              ],
            ),
          ],
        ),
      ),
    ),
  );
}

class _Footer extends StatelessWidget {
  const _Footer();

  @override
  Widget build(BuildContext context) => Container(
    color: const Color(0xff0e1530),
    padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 30),
    child: const Center(
      child: Wrap(
        alignment: WrapAlignment.center,
        crossAxisAlignment: WrapCrossAlignment.center,
        spacing: 28,
        runSpacing: 12,
        children: [
          _FooterBrand(),
          Text(
            'Society operations, thoughtfully connected.',
            style: TextStyle(color: Color(0xffaab2d0)),
          ),
          Text('© 2026 SocietyHub', style: TextStyle(color: Color(0xff7f89aa))),
        ],
      ),
    ),
  );
}

class _FooterBrand extends StatelessWidget {
  const _FooterBrand();

  @override
  Widget build(BuildContext context) => Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      Icon(Icons.apartment_rounded, color: Color(0xffaebcff)),
      SizedBox(width: 8),
      Text(
        'SocietyHub',
        style: TextStyle(color: Colors.white, fontWeight: FontWeight.w800),
      ),
    ],
  );
}

class SocietyRegistrationPage extends StatefulWidget {
  const SocietyRegistrationPage({super.key});
  @override
  State<SocietyRegistrationPage> createState() => _SocietyRegState();
}

class _SocietyRegState extends State<SocietyRegistrationPage> {
  final ctrls = List.generate(11, (_) => TextEditingController());
  final staffCount = TextEditingController();
  final selectedServices = <String>{};
  String plan = 'TRIAL';
  String? validationMessage;
  late final RegistrationCubit _cubit;
  bool _defaultsApplied = false;

  @override
  void initState() {
    super.initState();
    _cubit = RegistrationCubit(context.read<PublicRepository>())..load();
  }

  @override
  void dispose() {
    _cubit.close();
    super.dispose();
  }

  void _applyDefaults(List<PublicRecord> services) {
    if (_defaultsApplied) return;
    _defaultsApplied = true;
    selectedServices.addAll(
      services
          .where(
            (service) =>
                service['status'] == 'ACTIVE' &&
                service['availability_type'] == 'DEFAULT_FREE',
          )
          .map((service) => service['code'].toString()),
    );
  }

  String? _registrationMessage(PublicState<List<PublicRecord>> state) {
    if (state.status == PublicStatus.failure) return state.message;
    final result = state.result;
    if (result == null) return validationMessage;
    final estimate = result['estimatedMonthlyPrice'];
    final code = result['societyCode'];
    final registrationNumber = result['registrationNumber'];
    final pricing = estimate == null ? '' : ' Estimated ₹$estimate/month.';
    final loginCode = code == null ? '' : ' Society code: $code.';
    final registration = registrationNumber == null
        ? ''
        : ' Registration number: $registrationNumber.';
    return result['setupCode'] == null
        ? 'Submitted for verification.$registration$loginCode$pricing'
        : 'Approved. Setup code: ${result['setupCode']}.$registration$loginCode$pricing';
  }

  Future<void> _submit() async {
    final totalBlocks = int.tryParse(ctrls[2].text);
    final totalSpaces = int.tryParse(ctrls[3].text);
    if (totalBlocks == null || totalBlocks < 1) {
      setState(() => validationMessage = 'Enter at least 1 block.');
      return;
    }
    if (totalSpaces == null || totalSpaces < 1) {
      setState(() => validationMessage = 'Enter at least 1 unit space.');
      return;
    }
    setState(() => validationMessage = null);
    await _cubit.submit(
      SocietyRegistrationCommand(
        name: ctrls[0].text,
        societyType: ctrls[1].text,
        totalBlocks: totalBlocks,
        totalSpaces: totalSpaces,
        plannedStaff: int.tryParse(staffCount.text) ?? 0,
        approximateResidents: int.tryParse(ctrls[4].text),
        address: ctrls[5].text,
        city: ctrls[6].text,
        state: ctrls[7].text,
        pinCode: ctrls[8].text,
        contactPerson: ctrls[9].text,
        email: ctrls[10].text,
        planCode: plan,
        enabledModules: selectedServices.toList(growable: false),
      ),
    );
  }

  @override
  Widget build(c) {
    final labels = [
      'Society name',
      'Society type',
      'Total blocks',
      'Number of unit spaces',
      'Approx. residents',
      'Address',
      'City',
      'State',
      'PIN code',
      'Contact person',
      'Email',
    ];
    return BlocProvider.value(
      value: _cubit,
      child: BlocBuilder<RegistrationCubit, PublicState<List<PublicRecord>>>(
        builder: (context, state) {
          final serviceCatalog = state.data ?? const <PublicRecord>[];
          _applyDefaults(serviceCatalog);
          final loadingServices =
              state.status == PublicStatus.initial ||
              state.status == PublicStatus.loading;
          final message = _registrationMessage(state);
          return FormShell(
            title: 'Register your society',
            children: [
              for (int i = 0; i < labels.length; i++)
                field(ctrls[i], labels[i], number: [2, 3, 4, 8].contains(i)),
              field(staffCount, 'Expected staff count', number: true),
              DropdownButtonFormField(
                initialValue: plan,
                decoration: const InputDecoration(
                  labelText: 'Billing type',
                  border: OutlineInputBorder(),
                ),
                items:
                    const {'TRIAL': 'Trial', 'STANDARD': 'Usage-based billing'}
                        .entries
                        .map(
                          (entry) => DropdownMenuItem(
                            value: entry.key,
                            child: Text(entry.value),
                          ),
                        )
                        .toList(),
                onChanged: (x) => setState(() => plan = x!),
              ),
              Text(
                'Select services',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              Wrap(
                spacing: 6,
                runSpacing: 6,
                children: [
                  for (final service in serviceCatalog.where(
                    (service) => service['status'] == 'ACTIVE',
                  ))
                    FilterChip(
                      label: Text(
                        '${service['name']}${_registrationServicePrice(service)}',
                      ),
                      selected: selectedServices.contains(service['code']),
                      onSelected: service['availability_type'] == 'DEFAULT_FREE'
                          ? null
                          : (selected) => setState(
                              () => selected
                                  ? selectedServices.add(
                                      service['code'].toString(),
                                    )
                                  : selectedServices.remove(service['code']),
                            ),
                    ),
                ],
              ),
              if (loadingServices) const LinearProgressIndicator(),
              if (serviceCatalog.any(
                (service) => service['status'] == 'COMING_SOON',
              )) ...[
                Text(
                  'Coming soon',
                  style: Theme.of(context).textTheme.titleSmall,
                ),
                Wrap(
                  spacing: 6,
                  runSpacing: 6,
                  children: [
                    for (final service in serviceCatalog.where(
                      (service) => service['status'] == 'COMING_SOON',
                    ))
                      Chip(label: Text(service['name'].toString())),
                  ],
                ),
              ],
              FilledButton(
                onPressed: loadingServices || state.busy ? null : _submit,
                child: const Text('Submit registration'),
              ),
              if (message != null) Text(message),
            ],
          );
        },
      ),
    );
  }
}

String _registrationServicePrice(Map<String, dynamic> service) =>
    switch (service['availability_type']?.toString()) {
      'DEFAULT_FREE' => ' · Included',
      'PAID_PER_SPACE' => ' · ₹${service['price_per_space'] ?? 0}/space/month',
      _ => ' · Free',
    };

class ActivationPage extends StatefulWidget {
  const ActivationPage({super.key});
  @override
  State<ActivationPage> createState() => _ActivationState();
}

class _ActivationState extends State<ActivationPage> {
  final c = List.generate(5, (_) => TextEditingController());
  late final ActivationCubit _cubit;

  @override
  void initState() {
    super.initState();
    _cubit = ActivationCubit(context.read<PublicRepository>());
  }

  @override
  void dispose() {
    _cubit.close();
    super.dispose();
  }

  @override
  Widget build(x) => BlocProvider.value(
    value: _cubit,
    child: BlocBuilder<ActivationCubit, PublicState<void>>(
      builder: (context, state) => FormShell(
        title: 'Activate society',
        children: [
          field(c[0], 'Setup code'),
          field(c[1], 'Admin full name'),
          field(c[2], 'Email'),
          field(c[3], 'Mobile'),
          field(c[4], 'Password', secret: true),
          FilledButton(
            onPressed: state.busy
                ? null
                : () => _cubit.submit(
                    SocietyActivationCommand(
                      setupCode: c[0].text,
                      fullName: c[1].text,
                      email: c[2].text,
                      mobile: c[3].text,
                      password: c[4].text,
                    ),
                  ),
            child: Text(state.busy ? 'Activating…' : 'Activate'),
          ),
          if (state.message != null) Text(state.message!),
        ],
      ),
    ),
  );
}

class CustomRequirementPage extends StatefulWidget {
  const CustomRequirementPage({super.key});
  @override
  State<CustomRequirementPage> createState() => _CustomRequirementPageState();
}

class _CustomRequirementPageState extends State<CustomRequirementPage> {
  final controls = {
    for (final key in [
      'societyName',
      'contactPerson',
      'email',
      'mobile',
      'city',
      'estimatedFlats',
      'estimatedStaff',
      'budgetMonthly',
      'requirementDetails',
    ])
      key: TextEditingController(),
  };
  final modules = <String>{};
  String? validationMessage;
  late final RequirementCubit _cubit;
  final available = [
    'RESIDENTS',
    'STAFF',
    'VISITORS',
    'COMPLAINTS',
    'BILLING',
    'PAYMENTS',
    'AMENITIES',
    'NOTICES',
    'DOCUMENTS',
    'PARKING',
    'VENDORS',
    'CUSTOM_REPORTS',
    'API_ACCESS',
  ];

  @override
  void initState() {
    super.initState();
    _cubit = RequirementCubit(context.read<PublicRepository>());
  }

  @override
  void dispose() {
    _cubit.close();
    super.dispose();
  }

  Future<void> submit() async {
    if (controls.values.any((c) => c.text.trim().isEmpty) || modules.isEmpty) {
      setState(
        () => validationMessage =
            'Complete all fields and select at least one module.',
      );
      return;
    }
    setState(() => validationMessage = null);
    await _cubit.submit(
      RequirementCommand(
        societyName: controls['societyName']!.text,
        contactPerson: controls['contactPerson']!.text,
        email: controls['email']!.text,
        mobile: controls['mobile']!.text,
        city: controls['city']!.text,
        estimatedSpaces: int.tryParse(controls['estimatedFlats']!.text),
        estimatedStaff: int.tryParse(controls['estimatedStaff']!.text),
        requestedModules: modules.toList(growable: false),
        requirementDetails: controls['requirementDetails']!.text,
        budgetMonthly: double.tryParse(controls['budgetMonthly']!.text),
      ),
    );
  }

  @override
  Widget build(context) => BlocProvider.value(
    value: _cubit,
    child: BlocBuilder<RequirementCubit, PublicState<void>>(
      builder: (context, state) => FormShell(
        title: 'Request a custom plan',
        children: [
          for (final entry in {
            'societyName': 'Society name',
            'contactPerson': 'Contact person',
            'email': 'Email',
            'mobile': 'Mobile',
            'city': 'City',
            'estimatedFlats': 'Estimated spaces',
            'estimatedStaff': 'Estimated staff',
            'budgetMonthly': 'Monthly budget (₹)',
            'requirementDetails': 'Describe your requirements',
          }.entries)
            TextField(
              controller: controls[entry.key],
              maxLines: entry.key == 'requirementDetails' ? 4 : 1,
              keyboardType:
                  [
                    'estimatedFlats',
                    'estimatedStaff',
                    'budgetMonthly',
                  ].contains(entry.key)
                  ? TextInputType.number
                  : null,
              decoration: InputDecoration(
                labelText: entry.value,
                border: const OutlineInputBorder(),
              ),
            ),
          Text(
            'Required modules',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          Wrap(
            spacing: 6,
            children: [
              for (final module in available)
                FilterChip(
                  label: Text(module.replaceAll('_', ' ')),
                  selected: modules.contains(module),
                  onSelected: (selected) => setState(
                    () =>
                        selected ? modules.add(module) : modules.remove(module),
                  ),
                ),
            ],
          ),
          FilledButton.icon(
            onPressed: state.busy ? null : submit,
            icon: const Icon(Icons.send),
            label: Text(state.busy ? 'Submitting…' : 'Submit requirement'),
          ),
          if (state.message != null || validationMessage != null)
            Text(state.message ?? validationMessage!),
        ],
      ),
    ),
  );
}

class ResidentRegistrationPage extends StatelessWidget {
  const ResidentRegistrationPage({super.key});
  @override
  Widget build(c) => const FormShell(
    title: 'Resident self-registration',
    children: [
      Text(
        'Select a society and space after the society admin has configured it. A space may be a house number, apartment, entire level, shop, office, or other rental.',
      ),
      Card(
        child: ListTile(
          leading: Icon(Icons.verified_user),
          title: Text('Admin verification required'),
          subtitle: Text(
            'Owner/tenant documents and occupancy are verified before activation.',
          ),
        ),
      ),
    ],
  );
}

class FormShell extends StatelessWidget {
  final String title;
  final List<Widget> children;
  const FormShell({super.key, required this.title, required this.children});
  @override
  Widget build(c) => Scaffold(
    appBar: AppBar(title: Text(title)),
    body: Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 560),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              ...children.expand((w) => [w, const SizedBox(height: 14)]),
            ],
          ),
        ),
      ),
    ),
  );
}

Widget field(
  TextEditingController c,
  String label, {
  bool secret = false,
  bool number = false,
}) => _PublicFormField(
  controller: c,
  label: label,
  secret: secret,
  number: number,
);

class _PublicFormField extends StatefulWidget {
  const _PublicFormField({
    required this.controller,
    required this.label,
    required this.secret,
    required this.number,
  });
  final TextEditingController controller;
  final String label;
  final bool secret;
  final bool number;

  @override
  State<_PublicFormField> createState() => _PublicFormFieldState();
}

class _PublicFormFieldState extends State<_PublicFormField> {
  bool hidden = true;

  IconData get icon => switch (widget.label) {
    'Email' => Icons.mail_outline_rounded,
    'Password' => Icons.lock_outline_rounded,
    'Mobile' => Icons.phone_outlined,
    'Address' => Icons.location_on_outlined,
    'City' || 'State' => Icons.location_city_outlined,
    'PIN code' => Icons.pin_drop_outlined,
    'Setup code' => Icons.key_outlined,
    'Contact person' || 'Admin full name' => Icons.person_outline_rounded,
    'Society name' => Icons.apartment_outlined,
    'Society type' => Icons.category_outlined,
    'Total blocks' => Icons.domain_outlined,
    'Number of unit spaces' ||
    'Approx. residents' ||
    'Expected staff count' => Icons.numbers_rounded,
    _ => Icons.edit_outlined,
  };

  @override
  Widget build(BuildContext context) => TextField(
    controller: widget.controller,
    obscureText: widget.secret && hidden,
    keyboardType: widget.number
        ? TextInputType.number
        : widget.label == 'Email'
        ? TextInputType.emailAddress
        : widget.label == 'Mobile'
        ? TextInputType.phone
        : null,
    autofillHints: widget.label == 'Email'
        ? const [AutofillHints.email]
        : widget.secret
        ? const [AutofillHints.newPassword]
        : null,
    decoration: InputDecoration(
      labelText: widget.label,
      border: const OutlineInputBorder(),
      prefixIcon: Icon(
        icon,
        color: Theme.of(context).colorScheme.primary,
        semanticLabel: widget.label,
      ),
      suffixIcon: widget.secret
          ? IconButton(
              tooltip: hidden ? 'Show password' : 'Hide password',
              onPressed: () => setState(() => hidden = !hidden),
              icon: Icon(
                hidden
                    ? Icons.visibility_outlined
                    : Icons.visibility_off_outlined,
              ),
            )
          : null,
    ),
  );
}

class ErrorText extends StatelessWidget {
  final String text;
  const ErrorText(this.text, {super.key});
  @override
  Widget build(c) =>
      Text(text, style: TextStyle(color: Theme.of(c).colorScheme.error));
}
