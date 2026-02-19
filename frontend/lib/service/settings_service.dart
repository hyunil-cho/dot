import 'dart:convert';
import 'package:dot_frontend/service/api_service.dart';

class SettingsService {
  final ApiService _apiService;

  SettingsService({ApiService? apiService}) : _apiService = apiService ?? ApiService();

  Future<Map<String, dynamic>> getSettings(String token) async {
    try {
      // API endpoint assumption: GET /api/settings
      // If the API doesn't exist yet, we might need to mock it or handle 404.
      // For now, I'll implement the call.
      final response = await _apiService.get('/api/settings', token: token);
      
      // If API is not ready, return default values (mocking for now if needed)
      // But assuming server has it.
      
      final Map<String, dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return data;
    } catch (e) {
      print('Failed to load settings: $e');
      // Return defaults on error for now to keep app working
      return {
        'callTimeout': 30,
        'notificationsEnabled': true,
      };
    }
  }

  Future<void> updateSettings(String token, Map<String, dynamic> settings) async {
    try {
      await _apiService.put('/api/settings', settings, token: token);
    } catch (e) {
      print('Failed to update settings: $e');
      rethrow;
    }
  }
}
