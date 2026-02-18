import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/service/contact_service.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/custom_app_bar.dart';
import 'package:dot_frontend/ui/widgets/persona_file_picker.dart';
import 'package:dot_frontend/ui/widgets/custom_text_field.dart';
import 'package:dot_frontend/ui/widgets/phone_text_field.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class AddContactScreen extends StatefulWidget {
  const AddContactScreen({super.key});

  @override
  State<AddContactScreen> createState() => _AddContactScreenState();
}

class _AddContactScreenState extends State<AddContactScreen> {
  final _formKey = GlobalKey<FormState>();
  final _contactService = ContactService();
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _relationshipController = TextEditingController();
  final _memoController = TextEditingController();

  List<PersonaFileData> _personaFiles = [];
  bool _isLoading = false;

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _relationshipController.dispose();
    _memoController.dispose();
    super.dispose();
  }

  Future<void> _handleSaveContact() async {
    if (_formKey.currentState!.validate()) {
      setState(() {
        _isLoading = true;
      });

      try {
        final authProvider = Provider.of<AuthProvider>(context, listen: false);
        final token = authProvider.accessToken;

        if (token == null) {
          throw Exception('인증 토큰이 없습니다. 다시 로그인해주세요.');
        }

        // 첫 번째 페르소나 파일 정보 가져오기 (최대 1개로 제한됨)
        String? targetName;
        List<int>? fileBytes;
        String? fileName;

        if (_personaFiles.isNotEmpty) {
          final personaData = _personaFiles.first;
          targetName = personaData.nameController.text;
          fileBytes = personaData.file.bytes?.toList();
          fileName = personaData.file.name;
        }

        final newContact = await _contactService.createContact(
          token: token,
          name: _nameController.text,
          phoneNumber: _phoneController.text,
          relationship: _relationshipController.text,
          memo: _memoController.text,
          personaTargetName: targetName,
          fileBytes: fileBytes,
          fileName: fileName,
        );

        if (mounted) {
          Provider.of<ContactsProvider>(context, listen: false)
              .addContact(newContact);

          Navigator.of(context).pop();

          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('새로운 연락처가 저장되었습니다.')),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('저장 실패: $e'),
              backgroundColor: Colors.red,
            ),
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
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: const CustomAppBar(
        title: Text('새 연락처 추가', style: TextStyle(color: Colors.white)),
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
                      maxFiles: 1,
                      onFilesChanged: (newFiles) {
                        setState(() {
                          _personaFiles = newFiles;
                        });
                      },
                    ),

                    const SizedBox(height: 40),
                    ElevatedButton.icon(
                      onPressed: _isLoading ? null : _handleSaveContact,
                      icon: _isLoading
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Color(0xFF4A148C),
                              ),
                            )
                          : const Icon(Icons.save),
                      label: Text(_isLoading ? '저장 중...' : '저장하기'),
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