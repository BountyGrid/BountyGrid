import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';

void main() {
  runApp(const BountyGridApp());
}

class AppConfig {
  static const configuredApiBase = String.fromEnvironment('API_BASE_URL');
  static const appEnvironment = String.fromEnvironment('APP_ENV', defaultValue: 'local');

  static String get defaultApiBase {
    if (configuredApiBase.isNotEmpty) return configuredApiBase;
    if (Platform.isIOS || Platform.isMacOS) return 'http://localhost:8080/api';
    return 'http://10.0.2.2:8080/api';
  }

  static bool get isStoreBuild => appEnvironment == 'production';
}

class BountyGridApp extends StatelessWidget {
  const BountyGridApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'BountyGrid',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xff0f766e),
          primary: const Color(0xff0f766e),
          secondary: const Color(0xffb45309),
          surface: Colors.white,
        ),
        scaffoldBackgroundColor: const Color(0xfff7f8fb),
        useMaterial3: true,
      ),
      home: const BountyGridHome(),
    );
  }
}

class BountyGridHome extends StatefulWidget {
  const BountyGridHome({super.key});

  @override
  State<BountyGridHome> createState() => _BountyGridHomeState();
}

class _BountyGridHomeState extends State<BountyGridHome> {
  final ApiClient api = ApiClient();
  int tab = 0;
  String token = '';
  String status = '';

  @override
  Widget build(BuildContext context) {
    final pages = <Widget>[
      AlertsPage(api: api, requireAuth: requireAuth),
      PostAlertPage(api: api, requireAuth: requireAuth),
      WalletPage(api: api, requireAuth: requireAuth),
      MorePage(api: api, requireAuth: requireAuth),
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('BountyGrid'),
        actions: [
          IconButton(
            tooltip: token.isEmpty ? 'Login' : 'Logout',
            onPressed: token.isEmpty ? showAuth : logout,
            icon: Icon(token.isEmpty ? Icons.login : Icons.logout),
          ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            if (status.isNotEmpty)
              MaterialBanner(
                content: Text(status),
                actions: [
                  TextButton(
                    onPressed: () => setState(() => status = ''),
                    child: const Text('Dismiss'),
                  ),
                ],
              ),
            Expanded(
              child: token.isEmpty ? AuthPanel(api: api, onToken: saveToken) : pages[tab],
            ),
          ],
        ),
      ),
      bottomNavigationBar: token.isEmpty
          ? null
          : NavigationBar(
              selectedIndex: tab,
              onDestinationSelected: (index) => setState(() => tab = index),
              destinations: const [
                NavigationDestination(icon: Icon(Icons.radar), label: 'Alerts'),
                NavigationDestination(icon: Icon(Icons.add_location_alt), label: 'Post'),
                NavigationDestination(icon: Icon(Icons.account_balance_wallet), label: 'Wallet'),
                NavigationDestination(icon: Icon(Icons.grid_view), label: 'More'),
              ],
            ),
    );
  }

  Future<void> requireAuth(Future<void> Function() action) async {
    if (token.isEmpty) {
      showAuth();
      return;
    }
    try {
      await action();
    } catch (error) {
      setState(() => status = error.toString());
    }
  }

  void saveToken(String nextToken) {
    setState(() {
      token = nextToken;
      api.token = nextToken;
      status = 'Signed in';
    });
  }

  void logout() {
    setState(() {
      token = '';
      api.token = '';
      status = 'Signed out';
    });
  }

  void showAuth() {
    setState(() {
      token = '';
      api.token = '';
      status = '';
    });
  }
}

class AuthPanel extends StatefulWidget {
  const AuthPanel({super.key, required this.api, required this.onToken});

  final ApiClient api;
  final ValueChanged<String> onToken;

  @override
  State<AuthPanel> createState() => _AuthPanelState();
}

class _AuthPanelState extends State<AuthPanel> {
  late final TextEditingController base = TextEditingController(text: widget.api.baseUrl);
  final name = TextEditingController();
  final email = TextEditingController();
  final password = TextEditingController();
  final city = TextEditingController();
  bool loading = false;
  String message = '';

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Text('Find what matters', style: Theme.of(context).textTheme.headlineMedium),
        const SizedBox(height: 8),
        const Text('Lost and found alerts, rewards, tips, SOS, and recovery stories.'),
        const SizedBox(height: 20),
        if (!AppConfig.isStoreBuild) AppTextField(controller: base, label: 'API base URL'),
        AppTextField(controller: email, label: 'Email', keyboardType: TextInputType.emailAddress),
        AppTextField(controller: password, label: 'Password', obscureText: true),
        AppTextField(controller: name, label: 'Name for registration'),
        AppTextField(controller: city, label: 'City'),
        const SizedBox(height: 12),
        FilledButton.icon(
          onPressed: loading ? null : login,
          icon: const Icon(Icons.login),
          label: const Text('Log in'),
        ),
        OutlinedButton.icon(
          onPressed: loading ? null : register,
          icon: const Icon(Icons.person_add),
          label: const Text('Register'),
        ),
        if (message.isNotEmpty) Padding(padding: const EdgeInsets.only(top: 12), child: Text(message)),
      ],
    );
  }

  Future<void> login() async {
    await auth(() => widget.api.post('/auth/login', {
          'email': email.text.trim(),
          'password': password.text,
        }));
  }

  Future<void> register() async {
    await auth(() => widget.api.post('/auth/register', {
          'name': name.text.trim(),
          'email': email.text.trim(),
          'password': password.text,
          'city': city.text.trim(),
        }));
  }

  Future<void> auth(Future<dynamic> Function() request) async {
    setState(() {
      loading = true;
      message = '';
      widget.api.baseUrl = base.text.trim();
    });
    try {
      final json = await request() as Map<String, dynamic>;
      widget.onToken(json['token'] as String);
    } catch (error) {
      setState(() => message = error.toString());
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }
}

class AlertsPage extends StatefulWidget {
  const AlertsPage({super.key, required this.api, required this.requireAuth});

  final ApiClient api;
  final AuthRunner requireAuth;

  @override
  State<AlertsPage> createState() => _AlertsPageState();
}

class _AlertsPageState extends State<AlertsPage> {
  final lat = TextEditingController(text: '19.07');
  final lng = TextEditingController(text: '72.88');
  List<dynamic> alerts = [];
  bool loading = false;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text('Nearby alerts', style: Theme.of(context).textTheme.headlineSmall),
        Row(
          children: [
            Expanded(child: AppTextField(controller: lat, label: 'Latitude', keyboardType: TextInputType.number)),
            const SizedBox(width: 12),
            Expanded(child: AppTextField(controller: lng, label: 'Longitude', keyboardType: TextInputType.number)),
          ],
        ),
        FilledButton.icon(
          onPressed: loading ? null : load,
          icon: const Icon(Icons.refresh),
          label: const Text('Load alerts'),
        ),
        const SizedBox(height: 12),
        if (alerts.isEmpty && !loading) const Text('No alerts loaded yet.'),
        for (final item in alerts) AlertCard(alert: item as Map<String, dynamic>),
      ],
    );
  }

  Future<void> load() async {
    await widget.requireAuth(() async {
      setState(() => loading = true);
      final result = await widget.api.getList('/alerts/nearby?lat=${lat.text}&lng=${lng.text}&radius=25');
      setState(() {
        alerts = result;
        loading = false;
      });
    });
  }
}

class PostAlertPage extends StatefulWidget {
  const PostAlertPage({super.key, required this.api, required this.requireAuth});

  final ApiClient api;
  final AuthRunner requireAuth;

  @override
  State<PostAlertPage> createState() => _PostAlertPageState();
}

class _PostAlertPageState extends State<PostAlertPage> {
  final title = TextEditingController();
  final description = TextEditingController();
  final type = TextEditingController(text: 'LOST');
  final category = TextEditingController(text: 'OTHER');
  final lat = TextEditingController(text: '19.07');
  final lng = TextEditingController(text: '72.88');
  final city = TextEditingController(text: 'Mumbai');
  final reward = TextEditingController(text: '0');
  String message = '';

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text('Post alert', style: Theme.of(context).textTheme.headlineSmall),
        AppTextField(controller: title, label: 'Title'),
        AppTextField(controller: description, label: 'Description', maxLines: 3),
        AppTextField(controller: type, label: 'LOST or FOUND'),
        AppTextField(controller: category, label: 'Category'),
        AppTextField(controller: lat, label: 'Latitude', keyboardType: TextInputType.number),
        AppTextField(controller: lng, label: 'Longitude', keyboardType: TextInputType.number),
        AppTextField(controller: city, label: 'City'),
        AppTextField(controller: reward, label: 'Reward', keyboardType: TextInputType.number),
        FilledButton.icon(
          onPressed: submit,
          icon: const Icon(Icons.add_location_alt),
          label: const Text('Create alert'),
        ),
        if (message.isNotEmpty) Text(message),
      ],
    );
  }

  Future<void> submit() async {
    await widget.requireAuth(() async {
      await widget.api.post('/alerts', {
        'title': title.text.trim(),
        'description': description.text.trim(),
        'alertType': type.text.trim().toUpperCase(),
        'category': category.text.trim().toUpperCase(),
        'latitude': double.tryParse(lat.text) ?? 0,
        'longitude': double.tryParse(lng.text) ?? 0,
        'city': city.text.trim(),
        'radiusKm': 5,
        'rewardAmount': double.tryParse(reward.text) ?? 0,
      });
      setState(() => message = 'Alert created');
    });
  }
}

class WalletPage extends StatefulWidget {
  const WalletPage({super.key, required this.api, required this.requireAuth});

  final ApiClient api;
  final AuthRunner requireAuth;

  @override
  State<WalletPage> createState() => _WalletPageState();
}

class _WalletPageState extends State<WalletPage> {
  final amount = TextEditingController(text: '100');
  double balance = 0;
  List<dynamic> transactions = [];

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text('Wallet', style: Theme.of(context).textTheme.headlineSmall),
        Text('\$${balance.toStringAsFixed(2)}', style: Theme.of(context).textTheme.displaySmall),
        AppTextField(controller: amount, label: 'Amount', keyboardType: TextInputType.number),
        FilledButton(onPressed: () => walletAction('deposit'), child: const Text('Deposit')),
        OutlinedButton(onPressed: () => walletAction('withdraw'), child: const Text('Withdraw')),
        OutlinedButton(onPressed: load, child: const Text('Refresh wallet')),
        for (final tx in transactions)
          AppCard(
            title: '${tx['type']}',
            body: '${tx['description'] ?? ''}\nAmount: ${tx['amount']}',
          ),
      ],
    );
  }

  Future<void> load() async {
    await widget.requireAuth(() async {
      final wallet = await widget.api.getMap('/wallet');
      final list = await widget.api.getList('/wallet/transactions');
      setState(() {
        balance = (wallet['balance'] as num?)?.toDouble() ?? 0;
        transactions = list;
      });
    });
  }

  Future<void> walletAction(String action) async {
    await widget.requireAuth(() async {
      await widget.api.post('/wallet/$action?amount=${amount.text}', null);
      await load();
    });
  }
}

class MorePage extends StatefulWidget {
  const MorePage({super.key, required this.api, required this.requireAuth});

  final ApiClient api;
  final AuthRunner requireAuth;

  @override
  State<MorePage> createState() => _MorePageState();
}

class _MorePageState extends State<MorePage> {
  String heading = 'More';
  List<dynamic> items = [];

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text(heading, style: Theme.of(context).textTheme.headlineSmall),
        Wrap(
          spacing: 8,
          children: [
            ActionChip(label: const Text('Leaderboard'), onPressed: () => load('/leaderboard', 'Leaderboard')),
            ActionChip(label: const Text('SOS'), onPressed: () => load('/sos/active', 'SOS broadcasts')),
            ActionChip(label: const Text('Stories'), onPressed: () => load('/stories', 'Recovery stories')),
          ],
        ),
        const SizedBox(height: 12),
        for (final item in items) AppCard(title: itemTitle(item), body: itemBody(item)),
      ],
    );
  }

  Future<void> load(String path, String nextHeading) async {
    await widget.requireAuth(() async {
      final result = await widget.api.getList(path);
      setState(() {
        heading = nextHeading;
        items = result;
      });
    });
  }

  String itemTitle(dynamic item) {
    if (item is! Map<String, dynamic>) return 'Item';
    return '${item['name'] ?? item['title'] ?? item['alert']?['title'] ?? 'Item'}';
  }

  String itemBody(dynamic item) {
    if (item is! Map<String, dynamic>) return '$item';
    if (item.containsKey('points')) return 'Points: ${item['points']}\nBadge: ${item['currentBadge']}';
    if (item.containsKey('story')) return '${item['story']}\nHearts: ${item['hearts']} Claps: ${item['claps']}';
    if (item.containsKey('radiusKm')) return 'Radius: ${item['radiusKm']} km';
    return jsonEncode(item);
  }
}

class AlertCard extends StatelessWidget {
  const AlertCard({super.key, required this.alert});

  final Map<String, dynamic> alert;

  @override
  Widget build(BuildContext context) {
    return AppCard(
      title: '${alert['title'] ?? 'Alert'}',
      body: '${alert['description'] ?? ''}\n'
          'Category: ${alert['category'] ?? ''}\n'
          'Reward: ${alert['rewardAmount'] ?? 0}\n'
          'Distance: ${((alert['distanceKm'] as num?)?.toDouble() ?? 0).toStringAsFixed(2)} km',
    );
  }
}

class AppCard extends StatelessWidget {
  const AppCard({super.key, required this.title, required this.body});

  final String title;
  final String body;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 8),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(body),
          ],
        ),
      ),
    );
  }
}

class AppTextField extends StatelessWidget {
  const AppTextField({
    super.key,
    required this.controller,
    required this.label,
    this.keyboardType,
    this.obscureText = false,
    this.maxLines = 1,
  });

  final TextEditingController controller;
  final String label;
  final TextInputType? keyboardType;
  final bool obscureText;
  final int maxLines;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 7),
      child: TextField(
        controller: controller,
        decoration: InputDecoration(border: const OutlineInputBorder(), labelText: label),
        keyboardType: keyboardType,
        obscureText: obscureText,
        maxLines: maxLines,
      ),
    );
  }
}

class ApiClient {
  String baseUrl = AppConfig.defaultApiBase;
  String token = '';

  Future<Map<String, dynamic>> getMap(String path) async {
    final value = await request('GET', path, null);
    return value as Map<String, dynamic>;
  }

  Future<List<dynamic>> getList(String path) async {
    final value = await request('GET', path, null);
    return value as List<dynamic>;
  }

  Future<dynamic> post(String path, Map<String, dynamic>? body) {
    return request('POST', path, body);
  }

  Future<dynamic> request(String method, String path, Map<String, dynamic>? body) async {
    final client = HttpClient();
    try {
      final uri = Uri.parse('$baseUrl$path');
      final request = await client.openUrl(method, uri);
      request.headers.set(HttpHeaders.acceptHeader, 'application/json');
      if (token.isNotEmpty) request.headers.set(HttpHeaders.authorizationHeader, 'Bearer $token');
      if (body != null) {
        final bytes = utf8.encode(jsonEncode(body));
        request.headers.set(HttpHeaders.contentTypeHeader, 'application/json');
        request.contentLength = bytes.length;
        request.add(bytes);
      }
      final response = await request.close().timeout(const Duration(seconds: 20));
      final text = await utf8.decoder.bind(response).join();
      if (response.statusCode >= 400) {
        throw Exception(text.isEmpty ? 'HTTP ${response.statusCode}' : text);
      }
      if (text.isEmpty) return <String, dynamic>{};
      return jsonDecode(text);
    } on TimeoutException {
      throw Exception('Request timed out');
    } finally {
      client.close(force: true);
    }
  }
}

typedef AuthRunner = Future<void> Function(Future<void> Function() action);
