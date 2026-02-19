import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/service/contact_service.dart';
import 'package:flutter/material.dart';
import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/action_button.dart';
import 'package:provider/provider.dart';

class ContactDetailScreen extends StatefulWidget {
  final Contact? contact;
  final String? contactId;

  const ContactDetailScreen({super.key, this.contact, this.contactId});

  // 라우터에서 호출하기 위한 팩토리 메서드
  static Widget fromRoute(RouteSettings settings, String contactId) {
    if (settings.arguments is Contact) {
      return ContactDetailScreen(contact: settings.arguments as Contact);
    }
    return ContactDetailScreen(contactId: contactId);
  }

  @override
  State<ContactDetailScreen> createState() => _ContactDetailScreenState();
}

class _ContactDetailScreenState extends State<ContactDetailScreen> {
  Contact? _contact;
  bool _isLoading = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _contact = widget.contact;
    if (_contact == null && widget.contactId != null) {
      _fetchContact();
    }
  }

  Future<void> _fetchContact() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final contactService = Provider.of<ContactService>(context, listen: false);
      final token = authProvider.accessToken;

      if (token != null) {
        final contact = await contactService.getContact(token, widget.contactId!);
        if (mounted) {
          setState(() {
            _contact = contact;
            _isLoading = false;
          });
        }
      } else {
        throw Exception('로그인이 필요합니다.');
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _errorMessage = '연락처 정보를 불러오는 데 실패했습니다: $e';
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _handleDelete() async {
    if (_contact == null) return;

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: const Color(0xFF4A148C),
        title: const Text('연락처 삭제', style: TextStyle(color: Colors.white)),
        content: Text('${_contact!.name} 연락처를 삭제하시겠습니까?',
            style: const TextStyle(color: Colors.white70)),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('취소', style: TextStyle(color: Colors.white70)),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('삭제', style: TextStyle(color: Colors.redAccent)),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      try {
        final authProvider = Provider.of<AuthProvider>(context, listen: false);
        final contactService =
            Provider.of<ContactService>(context, listen: false);

        final token = authProvider.accessToken;
        if (token == null) throw Exception('인증 토큰이 없습니다.');

        await contactService.deleteContact(token, _contact!.id);

        if (mounted) {
          Navigator.pop(context); // 연락처 목록으로 돌아가기
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('연락처가 삭제되었습니다.')),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('삭제 실패: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(
        body: Stack(
          children: [
            BackgroundDesign(),
            Center(child: CircularProgressIndicator(color: Colors.white)),
          ],
        ),
      );
    }

    if (_errorMessage != null) {
      return Scaffold(
        body: Stack(
          children: [
            const BackgroundDesign(),
            Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(_errorMessage!, style: const TextStyle(color: Colors.white)),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: _fetchContact,
                    child: const Text('다시 시도'),
                  ),
                ],
              ),
            ),
          ],
        ),
      );
    }

    if (_contact == null) {
      return const Scaffold(
        body: Stack(
          children: [
            BackgroundDesign(),
            Center(child: Text('연락처를 찾을 수 없습니다.', style: TextStyle(color: Colors.white))),
          ],
        ),
      );
    }

    return Scaffold(
      body: Stack(
        children: [
          // 배경 디자인 재사용
          const BackgroundDesign(),
          
          // 상세 정보
          SafeArea(
            child: Column(
              children: [
                // 상단 앱바 (뒤로가기 버튼)
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8.0),
                  child: Row(
                    children: [
                      IconButton(
                        icon: const Icon(Icons.arrow_back, color: Colors.white),
                        onPressed: () => Navigator.pop(context),
                      ),
                      const Spacer(),
                    ],
                  ),
                ),

                const SizedBox(height: 20),

                // 프로필 이미지 (큰 원형)
                CircleAvatar(
                  radius: 60,
                  backgroundColor: const Color(0xFF6C63FF),
                  foregroundColor: Colors.white,
                  child: Text(
                    _contact!.initial,
                    style: const TextStyle(fontSize: 48, fontWeight: FontWeight.bold),
                  ),
                ),

                const SizedBox(height: 24),

                // 이름
                Text(
                  _contact!.name,
                  style: const TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),

                const SizedBox(height: 8),

                // 전화번호
                Text(
                  _contact!.phoneNumber,
                  style: TextStyle(
                    fontSize: 18,
                    color: Colors.white.withOpacity(0.7),
                  ),
                ),

                const SizedBox(height: 40),

                // 상세 정보 리스트 (관계, 메모 등)
                Expanded(
                  child: Container(
                    width: double.infinity,
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.1),
                      borderRadius: const BorderRadius.only(
                        topLeft: Radius.circular(32),
                        topRight: Radius.circular(32),
                      ),
                    ),
                    child: Column(
                      children: [
                        Expanded(
                          child: SingleChildScrollView(
                            padding: const EdgeInsets.all(24),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                _buildDetailItem(Icons.people, 'Relationship', _contact!.relationship),
                                const SizedBox(height: 24),
                                _buildDetailItem(Icons.note, 'Memo', _contact!.memo.isNotEmpty ? _contact!.memo : 'No memo'),
                              ],
                            ),
                          ),
                        ),
                        Padding(
                          padding: const EdgeInsets.fromLTRB(24, 0, 24, 20),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                            children: [
                              ActionButton(
                                icon: Icons.message,
                                label: 'Message',
                                color: Colors.blueAccent,
                                onTap: () {
                                  Navigator.pushNamed(
                                      context, '/chat/${_contact!.id}');
                                },
                              ),
                              ActionButton(
                                icon: Icons.edit,
                                label: 'Edit',
                                color: Colors.orangeAccent,
                                onTap: () async {
                                  await Navigator.pushNamed(
                                      context, '/contact/${_contact!.id}/edit',
                                      arguments: _contact);
                                  // 수정 후 돌아왔을 때 데이터 갱신
                                  if (widget.contactId != null) {
                                    _fetchContact();
                                  }
                                },
                              ),
                              ActionButton(
                                icon: Icons.delete,
                                label: 'Delete',
                                color: Colors.redAccent,
                                onTap: _handleDelete,
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDetailItem(IconData icon, String label, String value) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, color: Colors.white.withOpacity(0.6), size: 24),
        const SizedBox(width: 16),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              label,
              style: TextStyle(
                color: Colors.white.withOpacity(0.6),
                fontSize: 14,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              value,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ],
    );
  }
}
