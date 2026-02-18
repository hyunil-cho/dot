import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/custom_app_bar.dart';
import 'package:dot_frontend/ui/widgets/persona_file_picker.dart'; // Add PersonaFilePicker import
import 'package:dot_frontend/ui/widgets/custom_text_field.dart'; // Add CustomTextField import
import 'package:dot_frontend/ui/widgets/phone_text_field.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class EditContactScreen extends StatefulWidget {
  final Contact contact;

  const EditContactScreen({super.key, required this.contact});

  @override
  State<EditContactScreen> createState() => _EditContactScreenState();
}

class _EditContactScreenState extends State<EditContactScreen> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _phoneController;
  late TextEditingController _relationshipController;
  late TextEditingController _memoController;

  List<PersonaFileData> _personaFiles = []; // Changed type

  @override
  void initState() {
    super.initState();
    // 컨트롤러를 기존 연락처 정보로 초기화
    _nameController = TextEditingController(text: widget.contact.name);
    _phoneController = TextEditingController(text: widget.contact.phoneNumber);
    _relationshipController =
        TextEditingController(text: widget.contact.relationship);
    _memoController = TextEditingController(text: widget.contact.memo);
    // TODO: 기존 페르소나 파일 및 이름 데이터를 로드하는 로직 구현
  }

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _relationshipController.dispose();
    _memoController.dispose();
    // PersonaFilePicker will manage its own controllers disposal
    super.dispose();
  }

  void _handleUpdateContact() {
    if (_formKey.currentState!.validate()) {
      final updatedContact = Contact(
        id: widget.contact.id, // 기존 ID 사용
        name: _nameController.text,
        phoneNumber: _phoneController.text,
        relationship: _relationshipController.text,
        memo: _memoController.text,
        profileImageUrl: widget.contact.profileImageUrl, // 기존 이미지 URL 유지
      );

      // Provider를 통해 연락처 업데이트
      Provider.of<ContactsProvider>(context, listen: false)
          .updateContact(updatedContact);

      // 페르소나 데이터 처리
      if (_personaFiles.isNotEmpty) {
        for (var entry in _personaFiles) {
          if (entry.nameController.text.isNotEmpty) {
            print('Persona Target Name: ${entry.nameController.text}');
            print('File: ${entry.file.name}');
            // TODO: 실제 파일 업로드 및 페르소나 데이터 저장 로직 구현
          }
        }
      }

      // 상세 화면으로 돌아가기 (두 번 pop)
      int count = 0;
      Navigator.of(context).popUntil((_) => count++ >= 2);

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('연락처가 수정되었습니다.')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: const CustomAppBar(
        title: Text('연락처 수정', style: TextStyle(color: Colors.white)),
      ),
      body: Stack(
        children: [
          const BackgroundDesign(),
          SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24.0),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const SizedBox(height: 40),
                    CustomTextField(
                      controller: _nameController,
                      labelText: '이름',
                      icon: Icons.person,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return '이름을 입력해주세요.';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    PhoneTextField(
                      controller: _phoneController,
                      labelText: '전화번호',
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return '전화번호를 입력해주세요.';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    CustomTextField(
                      controller: _relationshipController,
                      labelText: '관계',
                      icon: Icons.people,
                    ),
                    const SizedBox(height: 16),
                    CustomTextField(
                      controller: _memoController,
                      labelText: '메모',
                      icon: Icons.note,
                      maxLines: 3,
                    ),

                    PersonaFilePicker(
                      onFilesChanged: (newFiles) {
                        setState(() {
                          _personaFiles = newFiles;
                        });
                      },
                    ),

                    const SizedBox(height: 40),
                    ElevatedButton.icon(
                      onPressed: _handleUpdateContact,
                      icon: const Icon(Icons.save),
                      label: const Text('수정 완료'),
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        backgroundColor: Colors.white,
                        foregroundColor: const Color(0xFF4A148C),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                        textStyle: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
