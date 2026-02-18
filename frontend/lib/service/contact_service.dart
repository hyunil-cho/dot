import 'dart:convert';
import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/service/api_service.dart';

class ContactService {
  final ApiService _apiService = ApiService();

  Future<List<Contact>> getContacts(String token) async {
    try {
      final response = await _apiService.get('/api/personas', token: token);
      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((json) => Contact.fromJson(json)).toList();
    } catch (e) {
      print('연락처 가져오기 오류: $e');
      rethrow;
    }
  }
}
