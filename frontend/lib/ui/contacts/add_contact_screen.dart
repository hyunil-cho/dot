import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/custom_app_bar.dart';
import 'package:dot_frontend/ui/widgets/persona_file_picker.dart'; // New import
import 'package:dot_frontend/ui/widgets/custom_text_field.dart'; // Add CustomTextField import
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class AddContactScreen extends StatefulWidget {
  const AddContactScreen({super.key});

  @override
  State<AddContactScreen> createState() => _AddContactScreenState();
}

class _AddContactScreenState extends State<AddContactScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _relationshipController = TextEditingController();
  final _memoController = TextEditingController();

  List<PersonaFileData> _personaFiles = []; // Changed type and removed private

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _relationshipController.dispose();
    _memoController.dispose();
    // PersonaFilePicker will manage its own controllers disposal
    super.dispose();
  }

  void _handleSaveContact() {
    if (_formKey.currentState!.validate()) {
      final newContact = Contact(
        id: DateTime
            .now()
            .millisecondsSinceEpoch
            .toString(),
        name: _nameController.text,
        phoneNumber: _phoneController.text,
        relationship: _relationshipController.text,
        memo: _memoController.text,
      );

      Provider.of<ContactsProvider>(context, listen: false)
          .addContact(newContact);

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

      Navigator.of(context).pop();

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('새로운 연락처가 저장되었습니다.')),
      );
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
                    CustomTextField(
                      controller: _phoneController,
                      labelText: '전화번호',
                      icon: Icons.phone,
                      keyboardType: TextInputType.phone,
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
                      onPressed: _handleSaveContact,
                      icon: const Icon(Icons.save),
                      label: const Text('저장하기'),
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