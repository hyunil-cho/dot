import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/ui/entry/slide_to_start_screen.dart';
import 'package:dot_frontend/ui/auth/login_screen.dart';
import 'package:dot_frontend/ui/home/main_screen.dart';

// main.dart의 라우팅 로직을 테스트하기 위해 유사한 구조를 만듭니다.
// 실제 main.dart를 import해서 쓸 수도 있지만, 테스트 환경 구성을 위해 별도로 정의하는 것이 좋습니다.
Widget createTestApp({required String initialRoute, required bool isAuthenticated}) {
  return MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) {
        final auth = AuthProvider();
        if (isAuthenticated) {
          auth.login('mock_token');
        }
        return auth;
      }),
    ],
    child: MaterialApp(
      initialRoute: initialRoute,
      onGenerateRoute: (settings) {
        // main.dart의 로직 복사 (테스트 대상)
        const publicRoutes = ['/', '/login', '/signup'];
        final isPublicRoute = publicRoutes.contains(settings.name);

        return MaterialPageRoute(
          builder: (context) {
            final auth = Provider.of<AuthProvider>(context, listen: false);
            final isAuthenticated = auth.isAuthenticated;

            // Case A: 비로그인 & 비공개 경로 -> 로그인 화면
            if (!isAuthenticated && !isPublicRoute) {
              return const LoginScreen();
            }

            // Case B: 로그인 & 공개 경로 -> 홈 화면
            if (isAuthenticated && isPublicRoute) {
              return const MainScreen();
            }

            // 정상적인 경우
            switch (settings.name) {
              case '/':
                return const SlideToStartScreen();
              case '/login':
                return const LoginScreen();
              case '/home':
                return const MainScreen();
              default:
                return isAuthenticated ? const MainScreen() : const SlideToStartScreen();
            }
          },
          settings: settings,
        );
      },
    ),
  );
}

void main() {
  group('Route Guard Tests', () {
    testWidgets('비로그인 상태에서 /home 접근 시 LoginScreen으로 리다이렉트', (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(initialRoute: '/home', isAuthenticated: false));
      await tester.pumpAndSettle();

      expect(find.byType(LoginScreen), findsOneWidget);
      expect(find.byType(MainScreen), findsNothing);
    });

    testWidgets('비로그인 상태에서 / 접근 시 SlideToStartScreen 표시', (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(initialRoute: '/', isAuthenticated: false));
      await tester.pumpAndSettle();

      expect(find.byType(SlideToStartScreen), findsOneWidget);
    });

    testWidgets('로그인 상태에서 /home 접근 시 MainScreen 표시', (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(initialRoute: '/home', isAuthenticated: true));
      await tester.pumpAndSettle();

      expect(find.byType(MainScreen), findsOneWidget);
    });

    testWidgets('로그인 상태에서 /login 접근 시 MainScreen으로 리다이렉트', (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(initialRoute: '/login', isAuthenticated: true));
      await tester.pumpAndSettle();

      expect(find.byType(MainScreen), findsOneWidget);
      expect(find.byType(LoginScreen), findsNothing);
    });
  });
}
