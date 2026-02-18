import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/provider/chat_provider.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/provider/settings_provider.dart';
import 'package:dot_frontend/router.dart';
import 'package:dot_frontend/ui/home/home_screen.dart';
import 'package:dot_frontend/ui/message/chat_screen.dart';
import 'package:dot_frontend/ui/message/sessions_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

// Helper function to create a testable widget with all necessary providers and the app router.
Widget createTestApp({required String initialRoute}) {
  return MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) {
        final auth = AuthProvider();
        auth.login('mock_token'); // Assume authenticated
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
  group('Message Navigation Flow Test', () {
    testWidgets(
        'Tapping message icon on home screen navigates to sessions, then to chat screen',
        (WidgetTester tester) async {
      // 1. Build the widget tree, starting at the '/home' route.
      await tester.pumpWidget(createTestApp(initialRoute: '/home'));

      // 2. Verify the initial screen is the HomeScreen and find the '메시지' icon.
      expect(find.byType(HomeScreen), findsOneWidget);
      final messageIcon = find.widgetWithText(HomeAppIcon, '메시지');
      expect(messageIcon, findsOneWidget);

      // 3. Tap the '메시지' icon to navigate to the sessions screen.
      await tester.tap(messageIcon);
      await tester.pumpAndSettle();

      // 4. Verify that the app has navigated to the SessionsScreen.
      final sessionsScreenFinder = find.byType(SessionsScreen);
      expect(sessionsScreenFinder, findsOneWidget);
      expect(find.widgetWithText(AppBar, '메시지'), findsOneWidget); // Check AppBar title

      // 5. Find and tap the first session entry ('Alice').
      await tester.tap(find.text('Alice'));
      await tester.pumpAndSettle();

      // 6. Verify that the app has navigated to the ChatScreen.
      final chatScreenFinder = find.byType(ChatScreen);
      expect(chatScreenFinder, findsOneWidget);

      // 7. Verify the data on the chat screen is correct.
      expect(
        find.descendant(
          of: chatScreenFinder,
          matching: find.text('Alice'),
        ),
        findsOneWidget, // Name in the AppBar
      );
      expect(
        find.descendant(
          of: chatScreenFinder,
          matching: find.text('Hey, how are you?'),
        ),
        findsOneWidget, // First message in the history
      );
    });
  });
}
