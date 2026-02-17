import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/provider/chat_provider.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/provider/settings_provider.dart';
import 'package:dot_frontend/router.dart';
import 'package:dot_frontend/ui/message/chat_screen.dart';
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
  group('SessionsScreen Navigation Test', () {
    testWidgets(
        'Tapping a session entry navigates to the chat screen with correct data',
        (WidgetTester tester) async {
      // 1. Build the widget tree, starting at the '/home' route (Messages tab).
      await tester.pumpWidget(createTestApp(initialRoute: '/home'));

      // 2. Verify the initial screen is the sessions list.
      final titleFinder = find.byWidgetPredicate(
        (widget) => widget is Text && widget.data == '메시지' && widget.style?.fontSize == 32,
      );
      expect(titleFinder, findsOneWidget);
      expect(find.text('Alice'), findsOneWidget);

      // 3. Find and tap the first session entry ('Alice').
      await tester.tap(find.text('Alice'));
      await tester.pumpAndSettle(); // Wait for navigation animation.

      // 4. Verify that the app has navigated to the ChatScreen.
      final chatScreenFinder = find.byType(ChatScreen);
      expect(chatScreenFinder, findsOneWidget);

      // 5. Verify the data on the chat screen is correct.
      //    - Check for the correct AppBar title.
      //    - Check for a message from the chat history.
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
