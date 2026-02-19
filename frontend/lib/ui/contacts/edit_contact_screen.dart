import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/service/contact_service.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/custom_app_bar.dart';
import 'package:dot_frontend/ui/widgets/persona_file_picker.dart';
import 'package:dot_frontend/ui/widgets/custom_text_field.dart';
import 'package:dot_frontend/ui/widgets/phone_text_field.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class EditContactScreen extends StatefulWidget {
  final Contact? contact;
  final String? contactId;

  const EditContactScreen({super.key, this.contact, this.contactId});

  // 라우터에서 호출하기 위한 팩토리 메서드
  static Widget fromRoute(RouteSettings settings, String contactId) {
    if (settings.arguments is Contact) {
      return EditContactScreen(contact: settings.arguments as Contact);
    }
    return EditContactScreen(contactId: contactId);
  }

  @override
  State<EditContactScreen> createState() => _EditContactScreenState();
}

class _EditContactScreenState extends State<EditContactScreen> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _phoneController;
  late TextEditingController _relationshipController;
  late TextEditingController _memoController;

  List<PersonaFileData> _personaFiles = [];
  bool _isLoading = false;
  String? _errorMessage;
  Contact? _contact;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController();
    _phoneController = TextEditingController();
    _relationshipController = TextEditingController();
    _memoController = TextEditingController();

    _contact = widget.contact;
    if (_contact != null) {
      _initializeControllers(_contact!);
    } else if (widget.contactId != null) {
      _fetchContact();
    }
  }

  void _initializeControllers(Contact contact) {
    _nameController.text = contact.name;
    _phoneController.text = contact.phoneNumber;
    _relationshipController.text = contact.relationship;
    _memoController.text = contact.memo;
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
            _initializeControllers(contact);
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

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _relationshipController.dispose();
    _memoController.dispose();
    super.dispose();
  }

  Future<void> _handleUpdateContact() async {
    if (_contact == null) return;

    if (_formKey.currentState!.validate()) {
      setState(() {
        _isLoading = true;
      });

      try {
        final authProvider = Provider.of<AuthProvider>(context, listen: false);
        final contactService = Provider.of<ContactService>(context, listen: false);
        final token = authProvider.accessToken;

        if (token == null) {
          throw Exception('인증 토큰이 없습니다. 다시 로그인해주세요.');
        }

        String? targetName;
        List<int>? fileBytes;
        String? fileName;

        if (_personaFiles.isNotEmpty) {
          final personaData = _personaFiles.first;
          targetName = personaData.nameController.text;
          fileBytes = personaData.file.bytes?.toList();
          fileName = personaData.file.name;
        }

        await contactService.updateContact(
          token: token,
          contactId: _contact!.id,
          name: _nameController.text,
          phoneNumber: _phoneController.text,
          relationship: _relationshipController.text,
          memo: _memoController.text,
          personaTargetName: targetName,
          fileBytes: fileBytes,
          fileName: fileName,
        );

        if (mounted) {
          int count = 0;
          Navigator.of(context).popUntil((_) => count++ >= 2);

          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('연락처가 수정되었습니다.')),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('수정 실패: $e'),
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
    if (_isLoading && _contact == null) {
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
                      onPressed: _isLoading ? null : _handleUpdateContact,
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
                      label: Text(_isLoading ? '수정 중...' : '수정 완료'),
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
