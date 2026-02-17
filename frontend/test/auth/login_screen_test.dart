import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:dot_frontend/ui/auth/login_screen.dart';
import 'package:dot_frontend/service/auth_service.dart';
import 'package:dot_frontend/ui/home/main_screen.dart';

// MockAuthService 클래스를 생성하기 위한 어노테이션
@GenerateMocks([AuthService])
import 'login_screen_test.mocks.dart';

void main() {
  // Mock 객체 생성
  late MockAuthService mockAuthService;

  setUp(() {
    // 각 테스트 전에 Mock 객체 초기화
    mockAuthService = MockAuthService();
  });

  Widget createTestWidget({required AuthService authService}) {
    return MaterialApp(
      home: LoginScreen(authService: authService),
      routes: {
        '/home': (context) => const MainScreen(),
      },
    );
  }

  testWidgets('로그인 성공 시 홈 화면으로 이동한다', (WidgetTester tester) async {
    // 1. Mock 설정: login 메서드가 항상 true를 반환하도록 설정
    when(mockAuthService.login(any, any)).thenAnswer((_) async => true);

    // 2. 위젯 렌더링 (MockAuthService 주입)
    await tester.pumpWidget(createTestWidget(authService: mockAuthService));

    // 3. 이메일과 비밀번호 입력
    await tester.enterText(find.widgetWithText(TextField, 'Email'), 'test@example.com');
    await tester.enterText(find.widgetWithText(TextField, 'Password'), 'password');

    // 4. 로그인 버튼 클릭
    await tester.tap(find.widgetWithText(ElevatedButton, 'Login'));
    
    // 5. 로딩 인디케이터가 나타나는지 확인
    await tester.pump(); // 로딩 상태 변경 반영
    expect(find.byType(CircularProgressIndicator), findsOneWidget);

    // 6. 비동기 작업(로그인) 및 화면 전환 완료 대기
    await tester.pumpAndSettle();

    // 7. 홈 화면으로 이동했는지 확인
    expect(find.byType(MainScreen), findsOneWidget);
    expect(find.byType(LoginScreen), findsNothing); // 로그인 화면은 사라져야 함
  });
}
