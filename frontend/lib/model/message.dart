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
}
