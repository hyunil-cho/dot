class Contact {
  final String id;
  final String name;
  final String phoneNumber;
  final String? profileImageUrl; 
  final String relationship; 
  final String memo; 

  Contact({
    required this.id,
    required this.name,
    required this.phoneNumber,
    this.profileImageUrl,
    this.relationship = 'Unknown',
    this.memo = '',
  });

  factory Contact.fromJson(Map<String, dynamic> json) {
    return Contact(
      id: json['id'].toString(),
      name: json['name'] ?? '',
      phoneNumber: json['phoneNumber'] ?? '',
      profileImageUrl: json['profileImageUrl'],
      relationship: json['relationship'] ?? 'Unknown',
      memo: json['memo'] ?? '', // API 응답에 memo가 없을 수 있으므로 기본값 설정
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': int.tryParse(id),
      'name': name,
      'phoneNumber': phoneNumber,
      'profileImageUrl': profileImageUrl,
      'relationship': relationship,
      'memo': memo,
    };
  }

  String get initial => name.isNotEmpty ? name[0].toUpperCase() : '?';
}
