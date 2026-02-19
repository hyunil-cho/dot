import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/ui/auth/login_screen.dart';
import 'package:dot_frontend/ui/auth/signup_screen.dart';
import 'package:dot_frontend/ui/contacts/add_contact_screen.dart';
import 'package:dot_frontend/ui/contacts/contact_detail_screen.dart';
import 'package:dot_frontend/ui/contacts/contacts_screen.dart';
import 'package:dot_frontend/ui/contacts/edit_contact_screen.dart';
import 'package:dot_frontend/ui/entry/slide_to_start_screen.dart';
import 'package:dot_frontend/ui/home/home_screen.dart';
import 'package:dot_frontend/ui/message/chat_screen.dart';
import 'package:dot_frontend/ui/message/sessions_screen.dart';
import 'package:dot_frontend/ui/message/select_contact_screen.dart';
import 'package:dot_frontend/ui/settings/settings_screen.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

Route<dynamic>? generateRoute(RouteSettings settings) {
  // 1. 로그인 없이 접근 가능한 경로 (화이트리스트)
  const publicRoutes = ['/', '/login', '/signup'];

  // 2. 현재 요청된 경로가 공개 경로인지 확인
  final uri = Uri.parse(settings.name ?? '');
  var isPublicRoute = publicRoutes.contains(uri.path);

  return MaterialPageRoute(
    builder: (context) {
      // 3. 현재 인증 상태 확인
      final auth = Provider.of<AuthProvider>(context, listen: false);
      final isAuthenticated = auth.isAuthenticated;

      // 4. 라우팅 로직 (가드)
      if (!isAuthenticated && !isPublicRoute) {
        return const LoginScreen();
      }

      if (isAuthenticated && isPublicRoute) {
        return const HomeScreen();
      }

      // 5. 동적 경로 처리 (위젯에게 파싱 위임)
      
      // /chat/:sessionId
      if (uri.pathSegments.length == 2 && uri.pathSegments.first == 'chat') {
        final sessionId = uri.pathSegments[1];
        return ChatScreen.fromRoute(settings, sessionId);
      }

      // /contact/:id/edit
      if (uri.pathSegments.length == 3 &&
          uri.pathSegments.first == 'contact' &&
          uri.pathSegments.last == 'edit') {
        final id = uri.pathSegments[1];
        return EditContactScreen.fromRoute(settings, id);
      }

      // /contact/:id
      if (uri.pathSegments.length == 2 &&
          uri.pathSegments.first == 'contact') {
        final id = uri.pathSegments[1];
        return ContactDetailScreen.fromRoute(settings, id);
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
          return const HomeScreen();
        case '/messages':
          return const SessionsScreen();
        case '/contacts':
          return const ContactsScreen();
        case '/settings':
          return const SettingsScreen();
        case '/add_contact':
          return const AddContactScreen();
        case '/select_contact':
          return const SelectContactScreen();
        default:
          return isAuthenticated
              ? const HomeScreen()
              : const SlideToStartScreen();
      }
    },
    settings: settings,
  );
}
