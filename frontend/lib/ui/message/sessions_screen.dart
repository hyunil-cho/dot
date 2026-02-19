import 'package:dot_frontend/model/chat_session.dart';
import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/service/chat_service.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/custom_app_bar.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:timeago/timeago.dart' as timeago;

class SessionsScreen extends StatefulWidget {
  const SessionsScreen({super.key});

  @override
  State<SessionsScreen> createState() => _SessionsScreenState();
}

class _SessionsScreenState extends State<SessionsScreen> {
  List<ChatSession> _sessions = [];
  List<ChatSession> _filteredSessions = [];
  bool _isLoading = false;
  String? _errorMessage;
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _fetchSessions();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _fetchSessions() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final chatService = Provider.of<ChatService>(context, listen: false);
      final token = authProvider.accessToken;

      if (token != null) {
        final sessions = await chatService.getChatSessions(token);
        if (mounted) {
          setState(() {
            _sessions = sessions;
            _filterSessions(_searchController.text);
            _isLoading = false;
          });
        }
      } else {
        throw Exception('로그인이 필요합니다.');
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _errorMessage = '대화 목록을 불러오는 데 실패했습니다: $e';
          _isLoading = false;
        });
      }
    }
  }

  void _filterSessions(String query) {
    if (query.isEmpty) {
      setState(() {
        _filteredSessions = _sessions;
      });
    } else {
      setState(() {
        _filteredSessions = _sessions.where((session) {
          final nameLower = session.contact.name.toLowerCase();
          final messageLower = session.lastMessage.toLowerCase();
          final queryLower = query.toLowerCase();
          return nameLower.contains(queryLower) || messageLower.contains(queryLower);
        }).toList();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    // Set the timeago locale to Korean
    timeago.setLocaleMessages('ko', timeago.KoMessages());

    return Scaffold(
      backgroundColor: Colors.transparent,
      extendBodyBehindAppBar: true,
      appBar: const CustomAppBar(
        title: Text('메시지'),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          await Navigator.pushNamed(context, '/select_contact');
          _fetchSessions(); // Refresh list after returning
        },
        backgroundColor: const Color(0xFF6C63FF),
        child: const Icon(Icons.add, color: Colors.white),
      ),
      body: Stack(
        children: [
          const BackgroundDesign(),
          SafeArea(
            child: Column(
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
                  child: TextField(
                    controller: _searchController,
                    style: const TextStyle(color: Colors.white),
                    onChanged: _filterSessions,
                    decoration: InputDecoration(
                      hintText: '대화 검색',
                      hintStyle: TextStyle(color: Colors.white.withOpacity(0.5)),
                      prefixIcon: Icon(Icons.search,
                          color: Colors.white.withOpacity(0.5)),
                      filled: true,
                      fillColor: Colors.white.withOpacity(0.1),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: BorderSide.none,
                      ),
                      contentPadding: const EdgeInsets.symmetric(
                          vertical: 0, horizontal: 16),
                    ),
                  ),
                ),
                Expanded(
                  child: _isLoading
                      ? const Center(child: CircularProgressIndicator(color: Colors.white))
                      : _errorMessage != null
                          ? Center(
                              child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Text(
                                    _errorMessage!,
                                    style: const TextStyle(color: Colors.white),
                                    textAlign: TextAlign.center,
                                  ),
                                  const SizedBox(height: 16),
                                  ElevatedButton(
                                    onPressed: _fetchSessions,
                                    child: const Text('다시 시도'),
                                  ),
                                ],
                              ),
                            )
                          : _filteredSessions.isEmpty
                              ? Center(
                                  child: Text(
                                    '대화 목록이 없습니다.',
                                    style: TextStyle(color: Colors.white.withOpacity(0.5)),
                                  ),
                                )
                              : ListView.builder(
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 8, vertical: 16),
                                  itemCount: _filteredSessions.length,
                                  itemBuilder: (context, index) {
                                    final session = _filteredSessions[index];
                                    return _buildSessionTile(context, session);
                                  },
                                ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSessionTile(BuildContext context, ChatSession session) {
    return ListTile(
      leading: CircleAvatar(
        radius: 28,
        backgroundColor: const Color(0xFF6C63FF),
        foregroundColor: Colors.white,
        child: Text(session.contact.initial),
      ),
      title: Text(
        session.contact.name,
        style: const TextStyle(
          color: Colors.white,
          fontWeight: FontWeight.w600,
          fontSize: 16,
        ),
      ),
      subtitle: Text(
        session.lastMessage,
        style: TextStyle(
          color: Colors.white.withOpacity(0.7),
          fontSize: 14,
        ),
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      trailing: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          Text(
            timeago.format(session.lastMessageAt, locale: 'ko'),
            style: TextStyle(
              color: Colors.white.withOpacity(0.5),
              fontSize: 12,
            ),
          ),
          const SizedBox(height: 4),
          if (session.unreadCount > 0)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
              decoration: BoxDecoration(
                color: Colors.redAccent,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                '${session.unreadCount}',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
            )
          else
            const SizedBox(height: 18), // To align with the unread count badge
        ],
      ),
      onTap: () async {
        // Navigate to chat screen with the session ID
        await Navigator.pushNamed(
          context, 
          '/chat/${session.id}', // Using session ID
        );
        _fetchSessions();
      },
    );
  }
}
