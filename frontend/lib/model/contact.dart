class Contact {
  final String id;
  final String name;
  final String phoneNumber;
  final String? avatarUrl; // 이미지 URL (없으면 이니셜 표시)
  final String relationship; // 관계 (예: 친구, 가족, 직장)
  final String memo; // 메모

  Contact({
    required this.id,
    required this.name,
    required this.phoneNumber,
    this.avatarUrl,
    this.relationship = 'Unknown', // 기본값
    this.memo = '', // 기본값
  });

  // 이름의 첫 글자를 가져오는 헬퍼 메서드
  String get initial => name.isNotEmpty ? name[0].toUpperCase() : '?';
}
