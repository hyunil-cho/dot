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

  factory ChatSession.fromJson(Map<String, dynamic> json) {
    return ChatSession(
      id: json['sessionId'].toString(),
      contact: Contact(
        id: json['personaId'].toString(),
        name: json['personaName'] ?? 'Unknown',
        phoneNumber: '', // Not provided in this API
        relationship: 'Unknown', // Not provided
      ),
      lastMessage: json['lastMessage'] ?? '',
      // Use updatedAt if available, otherwise use startedAt, otherwise current time
      lastMessageAt: json['updatedAt'] != null 
          ? DateTime.parse(json['updatedAt']) 
          : (json['startedAt'] != null ? DateTime.parse(json['startedAt']) : DateTime.now()),
      unreadCount: 0, // Not provided in this API, default to 0
    );
  }
}
