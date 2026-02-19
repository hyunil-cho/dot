import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/service/contact_service.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/custom_app_bar.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class ContactsScreen extends StatefulWidget {
  const ContactsScreen({super.key});

  @override
  State<ContactsScreen> createState() => _ContactsScreenState();
}

class _ContactsScreenState extends State<ContactsScreen> {
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      extendBodyBehindAppBar: true,
      appBar: const CustomAppBar(
        title: Text('연락처'),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          // 연락처 추가 후 돌아왔을 때 목록 갱신
          await Navigator.pushNamed(context, '/add_contact');
          _fetchContacts();
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
                // 검색창
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
                  child: TextField(
                    controller: _searchController,
                    style: const TextStyle(color: Colors.white),
                    onChanged: _filterContacts,
                    decoration: InputDecoration(
                      hintText: '검색',
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
                                    '연락처가 없습니다.',
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
        leading: contact.profileImageUrl != null
            ? CircleAvatar(
                backgroundImage: NetworkImage(contact.profileImageUrl!),
              )
            : CircleAvatar(
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
        trailing:
            Icon(Icons.message, color: Colors.greenAccent.withOpacity(0.8)),
        onTap: () async {
          // 상세 화면으로 이동 시 contact 객체 전달
          // 상세 화면에서 돌아올 때 변경 사항이 있을 수 있으므로 목록 갱신
          await Navigator.pushNamed(
            context, 
            '/contact/${contact.id}',
            arguments: contact,
          );
          _fetchContacts();
        },
      ),
    );
  }
}
