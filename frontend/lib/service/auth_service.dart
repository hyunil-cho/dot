class AuthService {
  // 로그인 메서드
  // 성공 시 true, 실패 시 false 반환 (또는 구체적인 에러 메시지 반환 가능)
  // 실제로는 비동기 통신이 필요하므로 Future<bool>을 반환
  Future<bool> login(String email, String password) async {
    // 임시 로직: 1초 지연 후, 이메일과 비밀번호가 비어있지 않으면 성공
    await Future.delayed(const Duration(seconds: 1));

    if (email.isNotEmpty && password.isNotEmpty) {
      return true;
    } else {
      return false;
    }
  }

  // 회원가입 메서드 (추후 구현)
  Future<bool> signUp(String name, String email, String password) async {
    await Future.delayed(const Duration(seconds: 1));
    return true;
  }
}
