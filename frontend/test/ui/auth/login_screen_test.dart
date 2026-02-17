import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:dot_frontend/ui/auth/login_screen.dart'; // 경로 수정

void main() {
  Widget createTestWidget() {
    return const MaterialApp(
      home: LoginScreen(),
    );
  }

  testWidgets('이메일 미입력 시 에러 메시지 표시 및 화면 유지', (WidgetTester tester) async {
    await tester.pumpWidget(createTestWidget());
    await tester.enterText(find.widgetWithText(TextField, 'Password'), 'password123');
    await tester.tap(find.widgetWithText(ElevatedButton, 'Login'));
    await tester.pump();

    expect(find.text('필수 입력정보를 모두 입력해주세요.'), findsOneWidget);
    expect(find.byType(LoginScreen), findsOneWidget);
  });

  testWidgets('비밀번호 미입력 시 에러 메시지 표시 및 화면 유지', (WidgetTester tester) async {
    await tester.pumpWidget(createTestWidget());
    await tester.enterText(find.widgetWithText(TextField, 'Email'), 'test@example.com');
    await tester.tap(find.widgetWithText(ElevatedButton, 'Login'));
    await tester.pump();

    expect(find.text('필수 입력정보를 모두 입력해주세요.'), findsOneWidget);
    expect(find.byType(LoginScreen), findsOneWidget);
  });

  testWidgets('둘 다 미입력 시 에러 메시지 표시 및 화면 유지', (WidgetTester tester) async {
    await tester.pumpWidget(createTestWidget());
    await tester.tap(find.widgetWithText(ElevatedButton, 'Login'));
    await tester.pump();

    expect(find.text('필수 입력정보를 모두 입력해주세요.'), findsOneWidget);
    expect(find.byType(LoginScreen), findsOneWidget);
  });

  testWidgets('에러 메시지는 3초 후 자동으로 사라진다', (WidgetTester tester) async {
    await tester.pumpWidget(createTestWidget());
    await tester.tap(find.widgetWithText(ElevatedButton, 'Login'));
    await tester.pump();

    expect(find.text('필수 입력정보를 모두 입력해주세요.'), findsOneWidget);
    await tester.pump(const Duration(seconds: 3, milliseconds: 100));
    expect(find.text('필수 입력정보를 모두 입력해주세요.'), findsNothing);
  });
}
