import 'package:flutter/material.dart';

// 조건부 import: 웹일 때는 url_strategy_web.dart, 아닐 때는 url_strategy_noop.dart를 사용
import 'package:dot_frontend/config/url_strategy_noop.dart'
    if (dart.library.html) 'package:dot_frontend/config/url_strategy_web.dart';

import 'package:dot_frontend/entry/slide_to_start_screen.dart';
import 'package:dot_frontend/auth/login_screen.dart';
import 'package:dot_frontend/auth/signup_screen.dart';
import 'package:dot_frontend/home/main_screen.dart';

void main() {
  // 웹/앱 환경에 따라 적절한 URL 설정 함수 실행
  configureUrl();
  
  runApp(const DotApp());
}

class DotApp extends StatelessWidget {
  const DotApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Dot',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        // 어두운 배경에 어울리는 테마 설정
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF6C63FF),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      // 초기 경로 설정
      initialRoute: '/',
      // 라우트 정의
      routes: {
        '/': (context) => const SlideToStartScreen(),
        '/login': (context) => const LoginScreen(),
        '/signup': (context) => const SignUpScreen(),
        '/home': (context) => const MainScreen(),
      },
    );
  }
}
