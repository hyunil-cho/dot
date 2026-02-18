import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/ui/contacts/edit_contact_screen.dart';
import 'package:flutter/material.dart';
import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/action_button.dart';
import 'package:provider/provider.dart';

class ContactDetailScreen extends StatelessWidget {
  final Contact contact;

  const ContactDetailScreen({super.key, required this.contact});

  Future<void> _handleDelete(BuildContext context) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: const Color(0xFF4A148C),
        title: const Text('연락처 삭제', style: TextStyle(color: Colors.white)),
        content: Text('${contact.name} 연락처를 삭제하시겠습니까?',
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

    if (confirmed == true && context.mounted) {
      try {
        final authProvider = Provider.of<AuthProvider>(context, listen: false);
        final contactsProvider =
            Provider.of<ContactsProvider>(context, listen: false);

        final token = authProvider.accessToken;
        if (token == null) throw Exception('인증 토큰이 없습니다.');

        await contactsProvider.deleteContact(token, contact.id);

        if (context.mounted) {
          Navigator.pop(context); // 연락처 목록으로 돌아가기
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('연락처가 삭제되었습니다.')),
          );
        }
      } catch (e) {
        if (context.mounted) {
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
                    contact.initial,
                    style: const TextStyle(fontSize: 48, fontWeight: FontWeight.bold),
                  ),
                ),

                const SizedBox(height: 24),

                // 이름
                Text(
                  contact.name,
                  style: const TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),

                const SizedBox(height: 8),

                // 전화번호
                Text(
                  contact.phoneNumber,
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
                                _buildDetailItem(Icons.people, 'Relationship', contact.relationship),
                                const SizedBox(height: 24),
                                _buildDetailItem(Icons.note, 'Memo', contact.memo.isNotEmpty ? contact.memo : 'No memo'),
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
                                      context, '/chat/${contact.id}');
                                },
                              ),
                              ActionButton(
                                icon: Icons.edit,
                                label: 'Edit',
                                color: Colors.orangeAccent,
                                onTap: () {
                                  Navigator.pushNamed(
                                      context, '/contact/${contact.id}/edit');
                                },
                              ),
                              ActionButton(
                                icon: Icons.delete,
                                label: 'Delete',
                                color: Colors.redAccent,
                                onTap: () => _handleDelete(context),
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
