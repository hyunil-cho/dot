import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/ui/auth/login_screen.dart';
import 'package:dot_frontend/ui/auth/signup_screen.dart';
import 'package:dot_frontend/ui/contacts/add_contact_screen.dart';
import 'package:dot_frontend/ui/contacts/contact_detail_screen.dart';
import 'package:dot_frontend/ui/contacts/edit_contact_screen.dart';
import 'package:dot_frontend/ui/entry/slide_to_start_screen.dart';
import 'package:dot_frontend/ui/home/main_screen.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

Route<dynamic>? generateRoute(RouteSettings settings) {
  // 1. 로그인 없이 접근 가능한 경로 (화이트리스트)
  const publicRoutes = ['/', '/login', '/signup'];

  // 2. 현재 요청된 경로가 공개 경로인지 확인
  final uri = Uri.parse(settings.name ?? '');
  var isPublicRoute = publicRoutes.contains(uri.path);

  // /contact/:id 와 /contact/:id/edit는 인증이 필요한 비공개 경로지만, publicRoutes에 포함시키지 않음
  // 아래 로직에서 직접 처리

  return MaterialPageRoute(
    builder: (context) {
      // 3. 현재 인증 상태 확인
      final auth = Provider.of<AuthProvider>(context, listen: false);
      final isAuthenticated = auth.isAuthenticated;

      // 4. 라우팅 로직 (가드)

      // Case A: 비로그인 상태인데 비공개 경로에 접근하려는 경우 -> 로그인 화면으로
      // 동적 경로도 고려해야 하므로, public이 아닌 모든 경로는 비공개로 간주
      if (!isAuthenticated && !isPublicRoute) {
        // /contact/:id 형태의 경로도 비공개 경로에 포함하여 처리
        final isDynamicContactRoute =
            uri.pathSegments.length > 1 && uri.pathSegments.first == 'contact';
        if (!isDynamicContactRoute && uri.path != '/login') {
          return const LoginScreen();
        } else if (isDynamicContactRoute) {
          return const LoginScreen();
        }
      }

      // Case B: 이미 로그인했는데 공개 경로에 접근하려는 경우 -> 홈으로
      if (isAuthenticated && isPublicRoute) {
        return const MainScreen(selectedIndex: 0);
      }

      // 5. 각 경로에 맞는 위젯 반환 (동적 경로 우선 처리)
      final contactsProvider =
          Provider.of<ContactsProvider>(context, listen: false);

      // /contact/:id/edit
      if (uri.pathSegments.length == 3 &&
          uri.pathSegments.first == 'contact' &&
          uri.pathSegments.last == 'edit') {
        final id = uri.pathSegments[1];
        final contact = contactsProvider.getContactById(id);
        return contact != null
            ? EditContactScreen(contact: contact)
            : const MainScreen(selectedIndex: 0); // 혹은 404 페이지
      }

      // /contact/:id
      if (uri.pathSegments.length == 2 &&
          uri.pathSegments.first == 'contact') {
        final id = uri.pathSegments[1];
        final contact = contactsProvider.getContactById(id);
        return contact != null
            ? ContactDetailScreen(contact: contact)
            : const MainScreen(selectedIndex: 0); // 혹은 404 페이지
      }

      // 정적 경로 처리
      switch (settings.name) {
        case '/':
          return const SlideToStartScreen();
        case '/login':
          return const LoginScreen();
        case '/signup':
          return const SignUpScreen();
        case '/home':
          return const MainScreen(selectedIndex: 0);
        case '/contacts':
          return const MainScreen(selectedIndex: 1);
        case '/settings':
          return const MainScreen(selectedIndex: 2);
        case '/add_contact':
          return const AddContactScreen();
        default:
          // 알 수 없는 경로는 에러 페이지나 홈으로 (로그인 상태에 따라)
          return isAuthenticated
              ? const MainScreen(selectedIndex: 0)
              : const SlideToStartScreen();
      }
    },
    settings: settings,
  );
}
