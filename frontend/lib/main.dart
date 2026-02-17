import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:dot_frontend/provider/auth_provider.dart';

import 'package:dot_frontend/config/url_strategy_noop.dart'
    if (dart.library.html) 'package:dot_frontend/config/url_strategy_web.dart';

import 'package:dot_frontend/ui/entry/slide_to_start_screen.dart';
import 'package:dot_frontend/ui/auth/login_screen.dart';
import 'package:dot_frontend/ui/auth/signup_screen.dart';
import 'package:dot_frontend/ui/home/main_screen.dart';

void main() {
  configureUrl();
  
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
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
      onGenerateRoute: (settings) {
        // 1. 로그인 없이 접근 가능한 경로 (화이트리스트)
        const publicRoutes = ['/', '/login', '/signup'];
        
        // 2. 현재 요청된 경로가 공개 경로인지 확인
        final isPublicRoute = publicRoutes.contains(settings.name);

        return MaterialPageRoute(
          builder: (context) {
            // 3. 현재 인증 상태 확인
            final auth = Provider.of<AuthProvider>(context, listen: false);
            final isAuthenticated = auth.isAuthenticated;

            // 4. 라우팅 로직 (가드)
            
            // Case A: 비로그인 상태인데 비공개 경로(예: /home)에 접근하려는 경우 -> 로그인 화면으로
            if (!isAuthenticated && !isPublicRoute) {
              // 로그인 후 원래 가려던 곳으로 보내주려면 arguments 등을 활용할 수 있음
              // 여기서는 단순히 로그인 화면으로 리다이렉트
              // (주의: 이미 로그인 화면이면 리다이렉트 안 함)
              if (settings.name != '/login') {
                 // Future.microtask로 감싸서 빌드 중 네비게이션 오류 방지할 수도 있지만,
                 // 여기서는 builder 내부이므로 위젯 반환으로 처리하는 게 안전함.
                 return const LoginScreen();
              }
            }

            // Case B: 이미 로그인했는데 로그인/회원가입/진입 화면에 접근하려는 경우 -> 홈으로
            if (isAuthenticated && isPublicRoute) {
              // 단, 로그아웃 기능이 있다면 예외 처리가 필요할 수 있음.
              // 여기서는 로그인 상태면 무조건 홈으로 보냄.
              return const MainScreen();
            }

            // 5. 각 경로에 맞는 위젯 반환 (정상적인 경우)
            switch (settings.name) {
              case '/':
                return const SlideToStartScreen();
              case '/login':
                return const LoginScreen();
              case '/signup':
                return const SignUpScreen();
              case '/home':
                return const MainScreen();
              default:
                // 알 수 없는 경로는 에러 페이지나 홈으로 (로그인 상태에 따라)
                return isAuthenticated ? const MainScreen() : const SlideToStartScreen();
            }
          },
          settings: settings,
        );
      },
    );
  }
}
