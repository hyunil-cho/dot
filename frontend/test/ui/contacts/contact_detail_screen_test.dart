import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/provider/chat_provider.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/provider/settings_provider.dart';
import 'package:dot_frontend/router.dart';
import 'package:dot_frontend/ui/contacts/contact_detail_screen.dart';
import 'package:dot_frontend/ui/message/chat_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

Widget createTestApp({
  required Widget child,
  required Contact testContact,
}) {
  return MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) {
        final auth = AuthProvider();
        auth.login('mock_token', 'refresh_token');
        return auth;
      }),
      ChangeNotifierProvider(create: (_) {
        final contacts = ContactsProvider();
        contacts.addContact(testContact);
        return contacts;
      }),
      ChangeNotifierProvider(create: (_) => SettingsProvider()),
      ChangeNotifierProvider(create: (_) => ChatProvider()),
    ],
    child: MaterialApp(
      onGenerateRoute: generateRoute,
      home: child,
    ),
  );
}

void main() {
  group('ContactDetailScreen Navigation Test', () {
    final testContact = Contact(
      id: 'test_1',
      name: 'Test User',
      phoneNumber: '010-1111-2222',
      relationship: 'Friend',
      memo: 'This is a test memo',
    );

    testWidgets('Tapping "Message" button navigates to ChatScreen', (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(
        child: ContactDetailScreen(contact: testContact),
        testContact: testContact,
      ));

      // 1. 상세 화면 요소 확인
      expect(find.text('Test User'), findsOneWidget);
      expect(find.text('Message'), findsOneWidget);

      // 2. 메시지 버튼 클릭
      await tester.tap(find.text('Message'));
      await tester.pumpAndSettle();

      // 3. ChatScreen으로 이동했는지 확인
      // router.dart에서 /chat/test_1 경로를 통해 ChatScreen이 빌드됨
      expect(find.byType(ChatScreen), findsOneWidget);
      expect(find.text('Test User'), findsWidgets); // 앱바나 헤더 등에 이름이 표시될 것임
    });
  });
}
