import 'package:flutter/material.dart';

class AuthProvider extends ChangeNotifier {
  String? _accessToken;
  String? _refreshToken;

  // 로그인 상태 확인 (accessToken이 있으면 true)
  bool get isAuthenticated => _accessToken != null;

  // 토큰 가져오기
  String? get accessToken => _accessToken;
  String? get refreshToken => _refreshToken;

  // 로그인 처리 (토큰 저장)
  void login(String accessToken, String refreshToken) {
    _accessToken = accessToken;
    _refreshToken = refreshToken;
    notifyListeners(); // 상태 변경 알림 (UI 업데이트)
  }

  // 로그아웃 처리 (토큰 삭제)
  void logout() {
    _accessToken = null;
    _refreshToken = null;
    notifyListeners(); // 상태 변경 알림
  }
}
