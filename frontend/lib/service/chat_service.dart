import 'dart:convert';
import 'package:dot_frontend/model/chat_session.dart';
import 'package:dot_frontend/model/message.dart';
import 'package:dot_frontend/service/api_service.dart';

class ChatService {
  final ApiService _apiService;

  ChatService({ApiService? apiService}) : _apiService = apiService ?? ApiService();

  Future<List<Message>> getMessages(String token, String sessionId) async {
    try {
      final response = await _apiService.get(
        '/api/chat-sessions/$sessionId/messages',
        token: token,
      );

      // Log the raw response body
      print('API Response: ${response.body}');

      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      final messages = data.map((json) => Message.fromJson(json)).toList();
      
      return messages;
    } catch (e) {
      print('Failed to load messages: $e');
      rethrow;
    }
  }

  Future<List<ChatSession>> getChatSessions(String token) async {
    try {
      final response = await _apiService.get(
        '/api/chat-sessions',
        token: token,
      );

      // Log the raw response body
      print('API Response (Sessions): ${response.body}');

      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      final sessions = data.map((json) => ChatSession.fromJson(json)).toList();
      
      return sessions;
    } catch (e) {
      print('Failed to load chat sessions: $e');
      rethrow;
    }
  }

  Future<ChatSession> getChatSession(String token, String sessionId) async {
    try {
      final response = await _apiService.get(
        '/api/chat-sessions/$sessionId',
        token: token,
      );

      // Log the raw response body
      print('API Response (Get Session): ${response.body}');

      final Map<String, dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return ChatSession.fromJson(data);
    } catch (e) {
      print('Failed to load chat session: $e');
      rethrow;
    }
  }

  Future<ChatSession> createChatSession(String token, String personaId) async {
    try {
      final response = await _apiService.post(
        '/api/chat-sessions?personaId=$personaId',
        {}, // Empty body
        token: token,
      );

      // Log the raw response body
      print('API Response (Create Session): ${response.body}');

      final Map<String, dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return ChatSession.fromJson(data);
    } catch (e) {
      print('Failed to create chat session: $e');
      rethrow;
    }
  }

  Future<Message> sendMessage(String token, String sessionId, String content) async {
    try {
      final response = await _apiService.post(
        '/api/chat-sessions/$sessionId/messages',
        {'content': content},
        token: token,
      );

      // Log the raw response body
      print('API Response (Send Message): ${response.body}');

      final Map<String, dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return Message.fromJson(data);
    } catch (e) {
      print('Failed to send message: $e');
      rethrow;
    }
  }
}
