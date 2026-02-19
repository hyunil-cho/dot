import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/service/chat_service.dart';
import 'package:dot_frontend/service/contact_service.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/custom_app_bar.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class SelectContactScreen extends StatefulWidget {
  const SelectContactScreen({super.key});

  @override
  State<SelectContactScreen> createState() => _SelectContactScreenState();
}

class _SelectContactScreenState extends State<SelectContactScreen> {
  List<Contact> _contacts = [];
  List<Contact> _filteredContacts = [];
  bool _isLoading = false;
  String? _errorMessage;
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _fetchContacts();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _fetchContacts() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final contactService = Provider.of<ContactService>(context, listen: false);
      
      if (authProvider.accessToken != null) {
        final contacts = await contactService.getContacts(authProvider.accessToken!);
        if (mounted) {
          setState(() {
            _contacts = contacts;
            _filterContacts(_searchController.text);
            _isLoading = false;
          });
        }
      } else {
        throw Exception('로그인이 필요합니다.');
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _errorMessage = '연락처를 불러오는 데 실패했습니다: $e';
          _isLoading = false;
        });
      }
    }
  }

  void _filterContacts(String query) {
    if (query.isEmpty) {
      setState(() {
        _filteredContacts = _contacts;
      });
    } else {
      setState(() {
        _filteredContacts = _contacts.where((contact) {
          final nameLower = contact.name.toLowerCase();
          final queryLower = query.toLowerCase();
          return nameLower.contains(queryLower) || contact.phoneNumber.contains(query);
        }).toList();
      });
    }
  }

  Future<void> _handleContactSelection(BuildContext context, Contact contact) async {
    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final chatService = Provider.of<ChatService>(context, listen: false);
      final token = authProvider.accessToken;

      if (token == null) {
        throw Exception('Authentication token not found');
      }

      // Show loading indicator
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => const Center(child: CircularProgressIndicator()),
      );

      // Create or get chat session
      final session = await chatService.createChatSession(token, contact.id);

      if (context.mounted) {
        Navigator.pop(context); // Dismiss loading indicator
        // Navigate to chat screen
        Navigator.pushReplacementNamed(
          context, 
          '/chat/${contact.id}',
          arguments: session, // Pass session object
        );
      }
    } catch (e) {
      if (context.mounted) {
        Navigator.pop(context); // Dismiss loading indicator if showing
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('채팅방 생성 실패: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      extendBodyBehindAppBar: true,
      appBar: const CustomAppBar(
        title: Text('대화 상대 선택'),
      ),
      body: Stack(
        children: [
          const BackgroundDesign(),
          SafeArea(
            child: Column(
              children: [
                // 검색창
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
                  child: TextField(
                    controller: _searchController,
                    style: const TextStyle(color: Colors.white),
                    onChanged: _filterContacts,
                    decoration: InputDecoration(
                      hintText: '이름 또는 전화번호 검색',
                      hintStyle: TextStyle(color: Colors.white.withOpacity(0.5)),
                      prefixIcon:
                          Icon(Icons.search, color: Colors.white.withOpacity(0.5)),
                      filled: true,
                      fillColor: Colors.white.withOpacity(0.1),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: BorderSide.none,
                      ),
                      contentPadding:
                          const EdgeInsets.symmetric(vertical: 0, horizontal: 16),
                    ),
                  ),
                ),

                // 연락처 목록
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
                                    onPressed: _fetchContacts,
                                    child: const Text('다시 시도'),
                                  ),
                                ],
                              ),
                            )
                          : _filteredContacts.isEmpty
                              ? Center(
                                  child: Text(
                                    '검색 결과가 없습니다.',
                                    style: TextStyle(color: Colors.white.withOpacity(0.5)),
                                  ),
                                )
                              : ListView.builder(
                                  padding: const EdgeInsets.symmetric(horizontal: 16),
                                  itemCount: _filteredContacts.length,
                                  itemBuilder: (context, index) {
                                    final contact = _filteredContacts[index];
                                    return _buildContactTile(context, contact);
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

  Widget _buildContactTile(BuildContext context, Contact contact) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.05),
        borderRadius: BorderRadius.circular(12),
      ),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: const Color(0xFF6C63FF),
          foregroundColor: Colors.white,
          child: Text(contact.initial),
        ),
        title: Text(
          contact.name,
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.w600,
          ),
        ),
        subtitle: Text(
          contact.phoneNumber,
          style: TextStyle(
            color: Colors.white.withOpacity(0.6),
            fontSize: 12,
          ),
        ),
        onTap: () => _handleContactSelection(context, contact),
      ),
    );
  }
}
