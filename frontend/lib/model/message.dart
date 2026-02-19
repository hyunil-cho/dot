class Message {
  final String id;
  final String text;
  final DateTime timestamp;
  final bool isSentByMe;

  Message({
    required this.id,
    required this.text,
    required this.timestamp,
    required this.isSentByMe,
  });

  factory Message.fromJson(Map<String, dynamic> json) {
    return Message(
      id: json['messageId'].toString(),
      text: json['content'] ?? '',
      timestamp: DateTime.parse(json['createdAt']),
      // isFromUser가 true이면 왼쪽(상대방), false이면 오른쪽(나)
      // 기존 로직: isSentByMe가 true이면 오른쪽, false이면 왼쪽
      // 요구사항: isFromUser가 true이면 왼쪽 -> isSentByMe = false
      //          isFromUser가 false이면 오른쪽 -> isSentByMe = true
      isSentByMe: !(json['isFromUser'] as bool),
    );
  }
}
