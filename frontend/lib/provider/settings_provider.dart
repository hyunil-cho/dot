import 'package:flutter/material.dart';

class SettingsProvider extends ChangeNotifier {
  // 전화 연결 타임아웃 (기본값: 30초)
  int _callTimeout = 30;
  
  // 알림 설정 (기본값: 켜짐)
  bool _notificationsEnabled = true;

  int get callTimeout => _callTimeout;
  bool get notificationsEnabled => _notificationsEnabled;

  void setCallTimeout(int seconds) {
    _callTimeout = seconds;
    notifyListeners();
  }

  void toggleNotifications(bool value) {
    _notificationsEnabled = value;
    notifyListeners();
  }
}
