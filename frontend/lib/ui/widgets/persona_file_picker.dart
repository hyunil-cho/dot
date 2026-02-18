import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';

class PersonaFileData {
  final TextEditingController nameController;
  final PlatformFile file;

  PersonaFileData({required this.nameController, required this.file});

  void dispose() {
    nameController.dispose();
  }
}

class PersonaFilePicker extends StatefulWidget {
  final ValueChanged<List<PersonaFileData>> onFilesChanged;
  final int? maxFiles; // 최대 파일 개수 제한

  const PersonaFilePicker({
    super.key,
    required this.onFilesChanged,
    this.maxFiles,
  });

  @override
  State<PersonaFilePicker> createState() => _PersonaFilePickerState();
}

class _PersonaFilePickerState extends State<PersonaFilePicker> {
  List<PersonaFileData> _personaFiles = [];

  @override
  void dispose() {
    for (var entry in _personaFiles) {
      entry.dispose();
    }
    super.dispose();
  }

  void _addPersonaFile() async {
    // 파일 개수 제한 확인
    if (widget.maxFiles != null && _personaFiles.length >= widget.maxFiles!) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('최대 ${widget.maxFiles}개의 파일만 추가할 수 있습니다.'),
            backgroundColor: Colors.orange,
          ),
        );
      }
      return;
    }

    FilePickerResult? result = await FilePicker.platform.pickFiles(
      allowMultiple: false, // 한 번에 하나의 파일만 선택
      type: FileType.custom,
      allowedExtensions: ['txt'], // 카카오톡 내보내기 파일은 .txt
      withData: true,
    );

    if (result != null && result.files.isNotEmpty) {
      PlatformFile pickedFile = result.files.first;

      const int maxFileSizeInBytes = 1 * 1024 * 1024; // 1MB

      if (pickedFile.size > maxFileSizeInBytes) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('파일 크기가 1MB를 초과합니다. 더 작은 파일을 선택해주세요.'),
              backgroundColor: Colors.red,
            ),
          );
        }
        return;
      }

      setState(() {
        _personaFiles.add(PersonaFileData(
          nameController: TextEditingController(),
          file: pickedFile,
        ));
      });
      widget.onFilesChanged(_personaFiles); // Notify parent
    }
  }

  void _removePersonaFile(PersonaFileData data) {
    setState(() {
      data.dispose();
      _personaFiles.remove(data);
    });
    widget.onFilesChanged(_personaFiles); // Notify parent
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(top: 24.0, bottom: 8.0),
      child: Text(
        title,
        style: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.bold,
          color: Colors.white.withOpacity(0.6),
          letterSpacing: 1.2,
        ),
      ),
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required String labelText,
    required IconData icon,
    TextInputType? keyboardType,
    int? maxLines = 1,
    String? Function(String?)? validator,
  }) {
    return TextFormField(
      controller: controller,
      keyboardType: keyboardType,
      maxLines: maxLines,
      style: const TextStyle(color: Colors.white),
      decoration: InputDecoration(
        labelText: labelText,
        labelStyle: TextStyle(color: Colors.white.withOpacity(0.8)),
        filled: true,
        fillColor: Colors.white.withOpacity(0.1),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide.none,
        ),
        prefixIcon: Icon(icon, color: Colors.white70),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: Colors.white.withOpacity(0.2)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFF6C63FF)),
        ),
      ),
      validator: validator,
    );
  }

  Widget _buildPersonaFileEntry(PersonaFileData data) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.05),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: _buildTextField(
                  controller: data.nameController,
                  labelText: '학습 대상 이름 (카톡 대화명)',
                  icon: Icons.person,
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return '학습 대상 이름을 입력해주세요.';
                    }
                    return null;
                  },
                ),
              ),
              const SizedBox(width: 8),
              IconButton(
                icon: const Icon(Icons.delete, color: Colors.redAccent),
                onPressed: () => _removePersonaFile(data),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            '파일: ${data.file.name}',
            style: TextStyle(color: Colors.white.withOpacity(0.7)),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final bool isLimitReached = widget.maxFiles != null && _personaFiles.length >= widget.maxFiles!;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _buildSectionHeader('페르소나 학습 (선택 사항)'),
        OutlinedButton.icon(
          onPressed: isLimitReached ? null : _addPersonaFile,
          icon: Icon(
            Icons.add,
            color: isLimitReached ? Colors.white24 : Colors.white70,
          ),
          label: Text(
            '새 대화 파일 추가',
            style: TextStyle(
              color: isLimitReached ? Colors.white24 : Colors.white,
            ),
          ),
          style: OutlinedButton.styleFrom(
            minimumSize: const Size(double.infinity, 48),
            side: BorderSide(
              color: isLimitReached ? Colors.white10 : Colors.white.withOpacity(0.3),
            ),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
        ),
        const SizedBox(height: 16),
        Column(
          children: _personaFiles.map((data) => _buildPersonaFileEntry(data)).toList(),
        ),
      ],
    );
  }
}
