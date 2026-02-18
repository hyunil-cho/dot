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

class FakeNavigatorObserver extends NavigatorObserver {
  Route<dynamic>? pushedRoute;

  @override
  void didPush(Route<dynamic> route, Route<dynamic>? previousRoute) {
    pushedRoute = route;
    super.didPush(route, previousRoute);
  }
}

// Helper function to create a testable widget with all necessary providers and the app router.
Widget createTestApp({
  required String initialRoute,
  List<NavigatorObserver> observers = const [],
}) {
  return MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) {
        final auth = AuthProvider();
        auth.login('mock_token','refresh token'); // Assume authenticated
        return auth;
      }),
      ChangeNotifierProvider(create: (_) => ContactsProvider()),
      ChangeNotifierProvider(create: (_) => SettingsProvider()),
      ChangeNotifierProvider(create: (_) => ChatProvider()),
    ],
    child: MaterialApp(
      initialRoute: initialRoute,
      onGenerateRoute: generateRoute,
      navigatorObservers: observers,
    ),
  );
}

void main() {
  group('SessionsScreen Navigation Test', () {
    late FakeNavigatorObserver fakeObserver;

    setUp(() {
      fakeObserver = FakeNavigatorObserver();
    });

    testWidgets('Tapping "Alice" session navigates to route "/chat/1"',
        (WidgetTester tester) async {
      // Alice's ID is '1' in ChatProvider dummy data
      await tester.pumpWidget(createTestApp(
        initialRoute: '/messages',
        observers: [fakeObserver],
      ));

      // Ensure Alice's session is visible
      expect(find.text('Alice'), findsOneWidget);

      // Tap Alice
      await tester.tap(find.text('Alice'));
      await tester.pumpAndSettle();

      // Check if the pushed route has the correct name
      expect(fakeObserver.pushedRoute, isNotNull);
      expect(fakeObserver.pushedRoute!.settings.name, equals('/chat/1'));
    });

    testWidgets('Searching sessions filters the list',
        (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(initialRoute: '/messages'));

      // Initial state: Alice and Bob should be visible
      expect(find.text('Alice'), findsOneWidget);
      expect(find.text('Bob'), findsOneWidget);

      // Search for 'Alice'
      await tester.enterText(find.byType(TextField), 'Alice');
      await tester.pump();

      // Alice should be visible in the list, Bob should not
      // Use find.widgetWithText to find the ListTile or check descendant of ListView
      expect(find.descendant(of: find.byType(ListTile), matching: find.text('Alice')), findsOneWidget);
      expect(find.text('Bob'), findsNothing);

      // Search for something that doesn't exist
      await tester.enterText(find.byType(TextField), 'NonExistent');
      await tester.pump();

      expect(find.text('Alice'), findsNothing);
      expect(find.text('Bob'), findsNothing);
    });

    testWidgets(
        'Tapping message icon on home screen navigates to sessions, then to chat screen',
        (WidgetTester tester) async {
      await tester.pumpWidget(createTestApp(initialRoute: '/home'));

      expect(find.byType(HomeScreen), findsOneWidget);
      final messageIcon = find.widgetWithText(HomeAppIcon, '메시지');
      expect(messageIcon, findsOneWidget);

      await tester.tap(messageIcon);
      await tester.pumpAndSettle();

      expect(find.byType(SessionsScreen), findsOneWidget);
      
      await tester.tap(find.text('Alice'));
      await tester.pumpAndSettle();

      expect(find.byType(ChatScreen), findsOneWidget);
    });
  });
}
