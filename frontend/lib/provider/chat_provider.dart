import 'package:dot_frontend/model/chat_session.dart';
import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/model/message.dart';
import 'package:flutter/material.dart';

class ChatProvider extends ChangeNotifier {
  final List<ChatSession> _sessions = [
    // Dummy Data
    ChatSession(
      id: 'session1',
      contact: Contact(id: '1', name: 'Alice', phoneNumber: '010-1234-5678', relationship: 'Friend'),
      lastMessage: 'Hey, how are you?',
      lastMessageAt: DateTime.now().subtract(const Duration(minutes: 5)),
      unreadCount: 2,
    ),
    ChatSession(
      id: 'session2',
      contact: Contact(id: '2', name: 'Bob', phoneNumber: '010-2345-6789', relationship: 'Colleague'),
      lastMessage: 'See you at the meeting tomorrow.',
      lastMessageAt: DateTime.now().subtract(const Duration(hours: 1)),
    ),
    ChatSession(
      id: 'session3',
      contact: Contact(id: '3', name: 'Charlie', phoneNumber: '010-3456-7890', relationship: 'Family'),
      lastMessage: 'Happy birthday! 🎉',
      lastMessageAt: DateTime.now().subtract(const Duration(days: 1)),
      unreadCount: 1,
    ),
     ChatSession(
      id: 'session4',
      contact: Contact(id: '9', name: 'Ian', phoneNumber: '010-9012-3456', relationship: 'Colleague'),
      lastMessage: 'Can you check the PR?',
      lastMessageAt: DateTime.now().subtract(const Duration(days: 2)),
    ),
  ];

  final Map<String, List<Message>> _messages = {
    'session1': [
      Message(id: 'm1', text: 'Hey, how are you?', timestamp: DateTime.now().subtract(const Duration(minutes: 5)), isSentByMe: false),
      Message(id: 'm2', text: 'I am good, thanks! What about you?', timestamp: DateTime.now().subtract(const Duration(minutes: 4)), isSentByMe: true),
      Message(id: 'm3', text: 'Doing great. Just working on the new project.', timestamp: DateTime.now().subtract(const Duration(minutes: 3)), isSentByMe: false),
      Message(id: 'm4', text: 'Sounds exciting!', timestamp: DateTime.now().subtract(const Duration(minutes: 2)), isSentByMe: true),
    ],
    'session2': [
      Message(id: 'm5', text: 'See you at the meeting tomorrow.', timestamp: DateTime.now().subtract(const Duration(hours: 1)), isSentByMe: false),
    ],
    'session3': [
       Message(id: 'm6', text: 'Happy birthday! 🎉', timestamp: DateTime.now().subtract(const Duration(days: 1)), isSentByMe: false),
    ]
  };

  List<ChatSession> get sessions => _sessions;

  List<Message> getMessages(String sessionId) {
    return _messages[sessionId] ?? [];
  }

  ChatSession? getSessionById(String sessionId) {
    try {
      return _sessions.firstWhere((s) => s.id == sessionId);
    } catch (e) {
      return null;
    }
  }

  void sendMessage(String sessionId, String text) {
    final newMessage = Message(
      id: 'msg_${DateTime.now().millisecondsSinceEpoch}',
      text: text,
      timestamp: DateTime.now(),
      isSentByMe: true,
    );
    
    if (_messages.containsKey(sessionId)) {
      _messages[sessionId]!.add(newMessage);
    } else {
      _messages[sessionId] = [newMessage];
    }
    
    // Update the last message in the session list
    final sessionIndex = _sessions.indexWhere((s) => s.id == sessionId);
    if (sessionIndex != -1) {
      // _sessions[sessionIndex].lastMessage = text; // This won't work as ChatSession is not mutable
      // Instead, create a new ChatSession object
    }

    notifyListeners();
  }
}
