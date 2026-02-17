import 'package:flutter/material.dart';

class AuthProvider extends ChangeNotifier {
  String? _token; // 로그인 토큰 (null이면 비로그인)

  // 로그인 상태 확인 (토큰이 있으면 true)
  bool get isAuthenticated => _token != null;

  // 토큰 가져오기
  String? get token => _token;

  // 로그인 처리 (토큰 저장)
  void login(String token) {
    _token = token;
    notifyListeners(); // 상태 변경 알림 (UI 업데이트)
  }

  // 로그아웃 처리 (토큰 삭제)
  void logout() {
    _token = null;
    notifyListeners(); // 상태 변경 알림
  }
}
