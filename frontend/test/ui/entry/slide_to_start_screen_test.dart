import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:dot_frontend/ui/entry/slide_to_start_screen.dart'; // 경로 수정
import 'package:dot_frontend/ui/auth/login_screen.dart'; // 경로 수정

void main() {
  // 테스트 환경의 화면 크기 설정 (선택 사항, 기본값 800x600)
  // tester.binding.window.physicalSizeTestValue = Size(400, 800);
  // tester.binding.window.devicePixelRatioTestValue = 1.0;

  Widget createTestWidget() {
    return MaterialApp(
      home: const SlideToStartScreen(),
      routes: {
        '/login': (context) => const LoginScreen(),
      },
    );
  }

  testWidgets('슬라이드를 90% 미만으로 밀면 핸들이 제자리로 돌아온다', (WidgetTester tester) async {
    // 1. 위젯 렌더링
    await tester.pumpWidget(createTestWidget());

    // 2. 슬라이더 핸들 찾기
    final handleFinder = find.byIcon(Icons.arrow_forward);
    expect(handleFinder, findsOneWidget);

    // 3. 초기 위치 저장
    final initialPosition = tester.getCenter(handleFinder);

    // 4. 슬라이더 전체 너비 구하기
    // SlideToStartWidget은 화면 너비 전체를 사용함 (LayoutBuilder)
    // 테스트 환경 기본 너비는 800.
    // 50% 정도만 밀어봄 (약 400px) -> 90% 미만 확실함
    
    // 드래그 제스처 시작
    final gesture = await tester.startGesture(initialPosition);
    await gesture.moveBy(const Offset(200, 0)); // 200px 이동
    await tester.pump(); // 화면 갱신

    // 드래그 중 위치 확인 (이동했는지)
    final draggedPosition = tester.getCenter(handleFinder);
    expect(draggedPosition.dx, greaterThan(initialPosition.dx));

    // 드래그 종료 (손 뗌)
    await gesture.up();
    await tester.pumpAndSettle(); // 애니메이션 및 상태 변경 완료 대기

    // 5. 다시 초기 위치로 돌아왔는지 확인
    final finalPosition = tester.getCenter(handleFinder);
    
    // 오차 범위 0.5 정도로 비교 (부동소수점 연산 고려)
    expect(finalPosition.dx, closeTo(initialPosition.dx, 0.5));
  });

  testWidgets('슬라이드를 90% 이상 밀면 로그인 화면으로 이동한다', (WidgetTester tester) async {
    await tester.pumpWidget(createTestWidget());

    final handleFinder = find.byIcon(Icons.arrow_forward);
    expect(handleFinder, findsOneWidget);

    // 화면 너비(800)의 90% 이상인 750px 정도 드래그
    await tester.drag(handleFinder, const Offset(750, 0));
    
    // 드래그 종료 및 화면 전환 애니메이션 대기
    await tester.pumpAndSettle();

    // 로그인 화면으로 전환되었는지 확인
    expect(find.byType(LoginScreen), findsOneWidget);
  });
}
