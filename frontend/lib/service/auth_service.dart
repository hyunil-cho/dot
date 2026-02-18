import 'dart:convert';
import 'package:dot_frontend/service/api_service.dart';

class AuthService {
  final ApiService _apiService = ApiService();

  // 로그인 메서드
  // 성공 시 (accessToken, refreshToken), 실패 시 null 반환
  Future<(String, String)?> login(String email, String password) async {
    try {
      final response = await _apiService.post('/api/auth/login', {
        'email': email,
        'password': password,
      });
      final data = jsonDecode(response.body);
      
      if (data['accessToken'] != null && data['refreshToken'] != null) {
        return (data['accessToken'] as String, data['refreshToken'] as String);
      }
      return null;
    } catch (e) {
      // 개발 중에는 목 데이터를 반환하도록 설정할 수 있습니다.
      // return ("mock_access_token", "mock_refresh_token");
      return null;
    }
  }

  // 회원가입 메서드 (추후 구현)
  Future<bool> signUp(String name, String email, String password) async {
    try {
      final response = await _apiService.post('/api/auth/signup', {
        'name': name,
        'email': email,
        'password': password,
      });
      return response.statusCode == 200;
    } catch (e) {
      print('회원가입 오류: $e');
      return false;
    }

    await Future.delayed(const Duration(seconds: 1));
    return true;
  }
}
