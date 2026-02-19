import 'package:dot_frontend/router.dart';
import 'package:dot_frontend/service/chat_service.dart';
import 'package:dot_frontend/service/contact_service.dart';
import 'package:dot_frontend/service/settings_service.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:dot_frontend/provider/auth_provider.dart';

import 'package:dot_frontend/config/url_strategy_noop.dart'
    if (dart.library.html) 'package:dot_frontend/config/url_strategy_web.dart';

void main() {
  configureUrl();

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        Provider(create: (_) => SettingsService()), // SettingsService 직접 주입
        Provider(create: (_) => ContactService()),
        Provider(create: (_) => ChatService()),
      ],
      child: const DotApp(),
    ),
  );
}

class DotApp extends StatelessWidget {
  const DotApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Dot',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF6C63FF),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      initialRoute: '/',
      onGenerateRoute: generateRoute,
    );
  }
}
