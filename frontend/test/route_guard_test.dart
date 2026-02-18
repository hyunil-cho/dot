import 'package:dot_frontend/provider/chat_provider.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/router.dart';
import 'package:dot_frontend/ui/home/home_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/ui/entry/slide_to_start_screen.dart';
import 'package:dot_frontend/ui/auth/login_screen.dart';

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
      // 라우터가 의존하는 모든 Provider를 추가합니다.
      ChangeNotifierProvider(create: (_) => ContactsProvider()),
      ChangeNotifierProvider(create: (_) => ChatProvider()),
    ],
    child: MaterialApp(
      initialRoute: initialRoute,
      onGenerateRoute: generateRoute,
    ),
  );
}

void main() {
  group('Route Guard Tests', () {
    testWidgets('비로그인 상태에서 /home 접근 시 LoginScreen으로 리다이렉트', (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(initialRoute: '/home', isAuthenticated: false));
      await tester.pumpAndSettle();

      expect(find.byType(LoginScreen), findsOneWidget);
      expect(find.byType(HomeScreen), findsNothing);
    });

    testWidgets('비로그인 상태에서 / 접근 시 SlideToStartScreen 표시', (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(initialRoute: '/', isAuthenticated: false));
      await tester.pumpAndSettle();

      expect(find.byType(SlideToStartScreen), findsOneWidget);
    });

    testWidgets('로그인 상태에서 /home 접근 시 HomeScreen 표시', (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(initialRoute: '/home', isAuthenticated: true));
      await tester.pumpAndSettle();

      expect(find.byType(HomeScreen), findsOneWidget);
    });

    testWidgets('로그인 상태에서 /login 접근 시 HomeScreen으로 리다이렉트', (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(initialRoute: '/login', isAuthenticated: true));
      await tester.pumpAndSettle();

      expect(find.byType(HomeScreen), findsOneWidget);
      expect(find.byType(LoginScreen), findsNothing);
    });
  });
}
