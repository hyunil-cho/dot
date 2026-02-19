import 'package:dot_frontend/model/chat_session.dart';
import 'package:dot_frontend/model/message.dart';
import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/service/chat_service.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class ChatScreen extends StatefulWidget {
  final String sessionId;

  const ChatScreen({super.key, required this.sessionId});

  // 라우터에서 호출하기 위한 팩토리 메서드
  static Widget fromRoute(RouteSettings settings, String sessionId) {
    return ChatScreen(sessionId: sessionId);
  }

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final _messageController = TextEditingController();
  List<Message> _messages = [];
  bool _isLoading = false;
  ChatSession? _currentSession;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _initializeChat();
    });
  }

  Future<void> _initializeChat() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final chatService = Provider.of<ChatService>(context, listen: false);
      final token = authProvider.accessToken;

      if (token == null) {
        throw Exception('Authentication token not found');
      }

      // 1. 세션 정보 조회 (AppBar 표시용)
      final session = await chatService.getChatSession(token, widget.sessionId);
      
      // 2. 메시지 목록 조회
      final messages = await chatService.getMessages(token, widget.sessionId);

      if (mounted) {
        setState(() {
          _currentSession = session;
          // API returns chronological order (oldest first).
          // We want newest first for reverse ListView.
          _messages = messages.reversed.toList();
        });
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('채팅방 로드 실패: $e')),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  @override
  void dispose() {
    _messageController.dispose();
    super.dispose();
  }

  Future<void> _handleSendMessage() async {
    final text = _messageController.text.trim();
    if (text.isNotEmpty) {
      // 1. 사용자 메시지를 먼저 화면에 표시 (Optimistic Update)
      // 기존 로직에 따라 User 메시지는 왼쪽(isSentByMe: false)에 표시
      final userMessage = Message(
        id: 'temp_${DateTime.now().millisecondsSinceEpoch}',
        text: text,
        timestamp: DateTime.now(),
        isSentByMe: false, 
      );

      setState(() {
        _messages.insert(0, userMessage);
      });
      _messageController.clear();

      try {
        final authProvider = Provider.of<AuthProvider>(context, listen: false);
        final chatService = Provider.of<ChatService>(context, listen: false);
        final token = authProvider.accessToken;

        if (token != null) {
          // 2. 서버로 메시지 전송
          final responseMessage = await chatService.sendMessage(token, widget.sessionId, text);
          
          // 3. 서버 응답(Assistant 메시지)을 화면에 표시
          // API 응답의 isFromUser: false -> Message.fromJson에서 isSentByMe: true로 변환됨 (오른쪽 표시)
          if (mounted) {
            setState(() {
              _messages.insert(0, responseMessage);
            });
          }
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('메시지 전송 실패: $e')),
          );
          // 실패 시 추가했던 사용자 메시지를 제거하거나 재시도 UI를 보여주는 등의 처리가 가능함
          // 여기서는 간단히 에러 메시지만 표시
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          const BackgroundDesign(),
          SafeArea(
            child: Column(
              children: [
                _buildAppBar(context),
                Expanded(
                  child: _isLoading
                      ? const Center(child: CircularProgressIndicator(color: Colors.white))
                      : ListView.builder(
                          reverse: true, // To show latest messages at the bottom
                          padding: const EdgeInsets.all(16),
                          itemCount: _messages.length,
                          itemBuilder: (context, index) {
                            final message = _messages[index];
                            return _buildMessageBubble(message);
                          },
                        ),
                ),
                _buildMessageComposer(),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAppBar(BuildContext context) {
    if (_currentSession == null) {
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 12),
        decoration: BoxDecoration(
          color: Colors.black.withOpacity(0.1),
        ),
        child: Row(
          children: [
            IconButton(
              icon: const Icon(Icons.arrow_back, color: Colors.white),
              onPressed: () => Navigator.pop(context),
            ),
            const Spacer(),
          ],
        ),
      );
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 12),
      decoration: BoxDecoration(
        color: Colors.black.withOpacity(0.1),
      ),
      child: Row(
        children: [
          IconButton(
            icon: const Icon(Icons.arrow_back, color: Colors.white),
            onPressed: () => Navigator.pop(context),
          ),
          const SizedBox(width: 8),
          CircleAvatar(
            child: Text(_currentSession!.contact.initial),
          ),
          const SizedBox(width: 12),
          Text(
            _currentSession!.contact.name,
            style: const TextStyle(
                color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const Spacer(),
          IconButton(
            icon: const Icon(Icons.more_vert, color: Colors.white),
            onPressed: () {},
          ),
        ],
      ),
    );
  }

  Widget _buildMessageBubble(Message message) {
    final isMe = message.isSentByMe;
    return Row(
      mainAxisAlignment: isMe ? MainAxisAlignment.end : MainAxisAlignment.start,
      children: [
        Container(
          constraints: BoxConstraints(
              maxWidth: MediaQuery.of(context).size.width * 0.7),
          margin: const EdgeInsets.symmetric(vertical: 4),
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
          decoration: BoxDecoration(
            color: isMe
                ? const Color(0xFF6C63FF)
                : Colors.white.withOpacity(0.15),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Text(
            message.text,
            style: const TextStyle(color: Colors.white, fontSize: 16),
          ),
        ),
      ],
    );
  }

  Widget _buildMessageComposer() {
    return Container(
      padding: const EdgeInsets.all(8.0),
      decoration: BoxDecoration(
        color: Colors.black.withOpacity(0.2),
        border: Border(top: BorderSide(color: Colors.white.withOpacity(0.1))),
      ),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _messageController,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                hintText: '메시지 입력...',
                hintStyle: TextStyle(color: Colors.white.withOpacity(0.5)),
                filled: true,
                fillColor: Colors.white.withOpacity(0.1),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(24),
                  borderSide: BorderSide.none,
                ),
                contentPadding: const EdgeInsets.symmetric(horizontal: 16),
              ),
              onSubmitted: (_) => _handleSendMessage(),
            ),
          ),
          const SizedBox(width: 8),
          IconButton(
            icon: const Icon(Icons.send, color: Color(0xFF6C63FF)),
            onPressed: _handleSendMessage,
          ),
        ],
      ),
    );
  }
}
