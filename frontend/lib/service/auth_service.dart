class AuthService {
  // 로그인 메서드
  // 성공 시 토큰(String), 실패 시 null 반환
  Future<String?> login(String email, String password) async {
    // 임시 로직: 1초 지연 후, 이메일과 비밀번호가 비어있지 않으면 성공
    await Future.delayed(const Duration(seconds: 1));

    if (email.isNotEmpty && password.isNotEmpty) {
      // 실제로는 서버에서 받은 토큰을 반환해야 함
      return "mock_token_12345"; 
    } else {
      return null;
    }
  }

  // 회원가입 메서드 (추후 구현)
  Future<bool> signUp(String name, String email, String password) async {
    await Future.delayed(const Duration(seconds: 1));
    return true;
  }
}
