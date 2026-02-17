import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/provider/chat_provider.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/provider/settings_provider.dart';
import 'package:dot_frontend/router.dart';
import 'package:dot_frontend/ui/contacts/contact_detail_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

// main.dart의 전체 라우팅 로직을 포함하는 테스트용 위젯 래퍼입니다.
Widget createTestApp({required String initialRoute}) {
  return MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) {
        final auth = AuthProvider();
        // 이 테스트에서는 인증된 상태를 가정합니다.
        auth.login('mock_token');
        return auth;
      }),
      ChangeNotifierProvider(create: (_) => ContactsProvider()),
      ChangeNotifierProvider(create: (_) => SettingsProvider()),
      ChangeNotifierProvider(create: (_) => ChatProvider()),
    ],
    child: MaterialApp(
      initialRoute: initialRoute,
      onGenerateRoute: generateRoute,
    ),
  );
}

void main() {
  group('Contacts Screen Navigation & Data Verification Test', () {
    testWidgets(
        'Tapping a contact entry navigates to the detail screen with correct data',
        (WidgetTester tester) async {
      // 1. 테스트 위젯을 빌드합니다. '/contacts' 경로에서 시작합니다.
      await tester.pumpWidget(createTestApp(initialRoute: '/contacts'));

      // 2. 초기 화면에 리스트 타일이 있는지 확인합니다.
      expect(find.byType(ListTile), findsWidgets);

      // 3. 첫 번째 연락처 타일을 탭합니다.
      await tester.tap(find.byType(ListTile).first);
      await tester.pumpAndSettle(); // 화면 전환 애니메이션이 끝날 때까지 기다립니다.

      // 4. 네비게이션 후 상세 화면이 올바르게 표시되는지 확인합니다.
      final detailScreenFinder = find.byType(ContactDetailScreen);
      expect(detailScreenFinder, findsOneWidget);

      // 5. 상세 화면의 데이터가 탭한 연락처의 정보와 일치하는지 확인합니다.
      //    모든 finders의 범위를 ContactDetailScreen 내부로 한정합니다.
      expect(
        find.descendant(
            of: detailScreenFinder, matching: find.text('Alice')),
        findsOneWidget,
      );
      expect(
        find.descendant(
            of: detailScreenFinder, matching: find.text('010-1234-5678')),
        findsOneWidget,
      );
      expect(
        find.descendant(
            of: detailScreenFinder, matching: find.text('Friend')),
        findsOneWidget,
      );
    });
  });
}
