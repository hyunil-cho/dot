import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:dot_frontend/ui/widgets/phone_text_field.dart';

void main() {
  group('PhoneTextField Formatting Tests', () {
    late TextEditingController controller;

    setUp(() {
      controller = TextEditingController();
    });

    tearDown(() {
      controller.dispose();
    });

    testWidgets('11-digit phone number (010-1234-5678) should be formatted correctly', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: PhoneTextField(
            controller: controller,
            labelText: '전화번호',
          ),
        ),
      ));

      // 11자리 휴대폰 번호 입력
      await tester.enterText(find.byType(TextField), '01012345678');
      await tester.pump();

      expect(controller.text, '010-1234-5678');
    });

    testWidgets('10-digit phone number (011-123-4567) should be formatted correctly', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: PhoneTextField(
            controller: controller,
            labelText: '전화번호',
          ),
        ),
      ));

      // 10자리 휴대폰 번호 입력 (예: 011, 017 등)
      await tester.enterText(find.byType(TextField), '0111234567');
      await tester.pump();

      expect(controller.text, '011-123-4567');
    });

    testWidgets('Entering only prefix (010) should add a hyphen if continuing', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: PhoneTextField(
            controller: controller,
            labelText: '전화번호',
          ),
        ),
      ));

      // 4자리 입력
      await tester.enterText(find.byType(TextField), '0101');
      await tester.pump();

      expect(controller.text, '010-1');
    });

    testWidgets('Entering 8 digits (e.g., landline/partial) should format with hyphen', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: PhoneTextField(
            controller: controller,
            labelText: '전화번호',
          ),
        ),
      ));

      // 8자리 입력
      await tester.enterText(find.byType(TextField), '01012345');
      await tester.pump();

      // 8자리는 else 조건에서 i=2, i=6 에서 하이픈이 추가됨
      expect(controller.text, '010-1234-5');
    });

    testWidgets('Validator should be called', (WidgetTester tester) async {
      final formKey = GlobalKey<FormState>();
      bool validatorCalled = false;

      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: Form(
            key: formKey,
            child: PhoneTextField(
              controller: controller,
              labelText: '전화번호',
              validator: (value) {
                validatorCalled = true;
                return null;
              },
            ),
          ),
        ),
      ));

      formKey.currentState?.validate();
      expect(validatorCalled, isTrue);
    });
  });
}
