import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';
import 'package:dot_frontend/ui/auth/login_screen.dart';
import 'package:dot_frontend/service/auth_service.dart';
import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/ui/home/main_screen.dart';

// MockAuthService 클래스를 생성하기 위한 어노테이션
@GenerateMocks([AuthService])
import 'login_screen_test.mocks.dart';

void main() {
  late MockAuthService mockAuthService;
  late AuthProvider authProvider;

  setUp(() {
    mockAuthService = MockAuthService();
    authProvider = AuthProvider();
  });

  Widget createTestWidget() {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider<AuthProvider>.value(value: authProvider),
      ],
      child: MaterialApp(
        // Consumer를 사용하여 로그인 상태에 따라 화면 전환 (main.dart와 동일한 구조)
        home: Consumer<AuthProvider>(
          builder: (context, auth, child) {
            return auth.isAuthenticated 
                ? const MainScreen() 
                : LoginScreen(authService: mockAuthService);
          },
        ),
        routes: {
          '/home': (context) => const MainScreen(),
        },
      ),
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

  testWidgets('로그인 성공 시 Provider 상태 업데이트 및 홈 화면으로 이동한다', (WidgetTester tester) async {
    // 1. Mock 설정: login 메서드가 지연 후 토큰을 반환하도록 설정
    when(mockAuthService.login(any, any)).thenAnswer((_) async {
      await Future.delayed(const Duration(milliseconds: 100)); // 지연 시간 추가
      return 'mock_token';
    });

    // 2. 위젯 렌더링
    await tester.pumpWidget(createTestWidget());

    // 3. 이메일과 비밀번호 입력
    await tester.enterText(find.widgetWithText(TextField, 'Email'), 'test@example.com');
    await tester.enterText(find.widgetWithText(TextField, 'Password'), 'password');

    // 4. 로그인 버튼 클릭
    await tester.tap(find.widgetWithText(ElevatedButton, 'Login'));
    
    // 5. 로딩 인디케이터 확인 (지연 시간 덕분에 로딩 상태가 유지됨)
    await tester.pump(); // 첫 번째 프레임 렌더링 (로딩 시작)
    expect(find.byType(CircularProgressIndicator), findsOneWidget);

    // 6. 비동기 작업 완료 대기 (지연 시간만큼 기다림)
    await tester.pumpAndSettle();

    // 7. 검증
    // - AuthProvider 상태가 업데이트 되었는지 확인
    expect(authProvider.isAuthenticated, isTrue);
    expect(authProvider.token, 'mock_token');
    
    // - 홈 화면으로 이동했는지 확인
    expect(find.byType(MainScreen), findsOneWidget);
    expect(find.byType(LoginScreen), findsNothing);
  });
}
