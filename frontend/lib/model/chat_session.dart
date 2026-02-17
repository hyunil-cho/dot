import 'package:dot_frontend/model/contact.dart';

class ChatSession {
  final String id;
  final Contact contact; // The person we are chatting with
  final String lastMessage;
  final DateTime lastMessageAt;
  final int unreadCount;

  ChatSession({
    required this.id,
    required this.contact,
    required this.lastMessage,
    required this.lastMessageAt,
    this.unreadCount = 0,
  });
}
