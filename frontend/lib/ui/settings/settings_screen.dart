import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/service/settings_service.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/custom_app_bar.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  int _callTimeout = 30;
  bool _notificationsEnabled = true;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _fetchSettings();
  }

  Future<void> _fetchSettings() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final settingsService = Provider.of<SettingsService>(context, listen: false);
      final token = authProvider.accessToken;

      if (token != null) {
        final settings = await settingsService.getSettings(token);
        if (mounted) {
          setState(() {
            _callTimeout = settings['callTimeout'] ?? 30;
            _notificationsEnabled = settings['notificationsEnabled'] ?? true;
          });
        }
      }
    } catch (e) {
      // Handle error or use defaults
      print('Error fetching settings: $e');
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _updateSettings() async {
    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final settingsService = Provider.of<SettingsService>(context, listen: false);
      final token = authProvider.accessToken;

      if (token != null) {
        await settingsService.updateSettings(token, {
          'callTimeout': _callTimeout,
          'notificationsEnabled': _notificationsEnabled,
        });
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('설정 저장 실패: $e')),
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
        title: Text('설정'),
      ),
      body: Stack(
        children: [
          const BackgroundDesign(),
          SafeArea(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator(color: Colors.white))
                : ListView(
                    padding: const EdgeInsets.all(16.0),
                    children: [
                      // 섹션 1: 일반 설정
                      _buildSectionHeader('일반'),
                      _buildListTile(
                        icon: Icons.timer,
                        title: '전화 연결 타임아웃',
                        subtitle: '$_callTimeout초',
                        onTap: () => _showTimeoutDialog(context),
                      ),
                      
                      // 알림 설정 (SwitchListTile 사용)
                      Container(
                        margin: const EdgeInsets.only(bottom: 8.0),
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.1),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: SwitchListTile(
                          secondary: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: Colors.white.withOpacity(0.1),
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.notifications, color: Colors.white),
                          ),
                          title: const Text(
                            '알림',
                            style: TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          subtitle: Text(
                            _notificationsEnabled ? '켜짐' : '꺼짐',
                            style: TextStyle(
                              color: Colors.white.withOpacity(0.6),
                              fontSize: 12,
                            ),
                          ),
                          value: _notificationsEnabled,
                          onChanged: (bool value) {
                            setState(() {
                              _notificationsEnabled = value;
                            });
                            _updateSettings();
                          },
                          activeColor: const Color(0xFF6C63FF),
                        ),
                      ),
                    ],
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(top: 16.0, bottom: 8.0),
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

  Widget _buildListTile({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
    Color textColor = Colors.white,
    Color iconColor = Colors.white,
  }) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8.0),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
      ),
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.1),
            shape: BoxShape.circle,
          ),
          child: Icon(icon, color: iconColor),
        ),
        title: Text(
          title,
          style: TextStyle(
            color: textColor,
            fontWeight: FontWeight.w500,
          ),
        ),
        subtitle: Text(
          subtitle,
          style: TextStyle(
            color: textColor.withOpacity(0.6),
            fontSize: 12,
          ),
        ),
        trailing: Icon(Icons.chevron_right, color: textColor.withOpacity(0.4)),
        onTap: onTap,
      ),
    );
  }

  void _showTimeoutDialog(BuildContext context) {
    final TextEditingController controller = TextEditingController(
      text: _callTimeout.toString(),
    );

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          backgroundColor: const Color(0xFF2E1A47),
          title: const Text('전화 연결 타임아웃 설정', style: TextStyle(color: Colors.white)),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text(
                '타임아웃 시간을 초 단위로 입력해주세요.',
                style: TextStyle(color: Colors.white70, fontSize: 14),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: controller,
                keyboardType: TextInputType.number,
                style: const TextStyle(color: Colors.white),
                decoration: InputDecoration(
                  suffixText: '초',
                  suffixStyle: const TextStyle(color: Colors.white70),
                  enabledBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Colors.white.withOpacity(0.3)),
                  ),
                  focusedBorder: const OutlineInputBorder(
                    borderSide: BorderSide(color: Color(0xFF6C63FF)),
                  ),
                ),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('취소', style: TextStyle(color: Colors.white70)),
            ),
            TextButton(
              onPressed: () {
                final int? newValue = int.tryParse(controller.text);
                if (newValue != null && newValue > 0) {
                  setState(() {
                    _callTimeout = newValue;
                  });
                  _updateSettings();
                  Navigator.pop(context);
                } else {
                  // 유효하지 않은 입력 처리
                }
              },
              child: const Text('저장', style: TextStyle(color: Color(0xFF6C63FF))),
            ),
          ],
        );
      },
    );
  }
}
