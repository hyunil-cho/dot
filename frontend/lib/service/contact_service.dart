import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/service/api_service.dart';
import 'package:dot_frontend/config/constants.dart';

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

  Future<Contact> createContact({
    required String token,
    required String name,
    required String phoneNumber,
    required String relationship,
    required String memo,
    String? personaTargetName,
    List<int>? fileBytes,
    String? fileName,
  }) async {
    try {
      final uri = Uri.parse('${ApiConstants.baseUrl}/api/personas');
      final request = http.MultipartRequest('POST', uri);

      // 헤더 설정
      request.headers['accept'] = 'application/json';
      request.headers['Authorization'] = 'Bearer $token';

      // 일반 필드 추가
      request.fields['name'] = name;
      request.fields['phoneNumber'] = phoneNumber;
      request.fields['relationship'] = relationship;
      request.fields['memo'] = memo;
      request.fields['speakerName'] = personaTargetName ?? '';

      // 프로필 이미지/학습 파일 추가
      if (fileBytes != null && fileName != null) {
        request.files.add(http.MultipartFile.fromBytes(
          'profileImage',
          fileBytes,
          filename: fileName,
        ));
      }

      print('Request URI: ${request.url}');
      print('Request Fields: ${request.fields}');

      final streamedResponse = await request.send();
      final response = await http.Response.fromStream(streamedResponse);

      if (response.statusCode == 200 || response.statusCode == 201) {
        final data = jsonDecode(utf8.decode(response.bodyBytes));
        return Contact.fromJson(data);
      } else {
        throw Exception('Failed to create contact: ${response.statusCode} - ${response.body}');
      }
          } catch (e) {
          print('연락처 생성 오류: $e');
          rethrow;
        }
      }
    
      Future<void> deleteContact(String token, String contactId) async {
        try {
          final response = await _apiService.delete('/api/personas/$contactId', token: token);
          if (response.statusCode != 200 && response.statusCode != 204) {
            throw Exception('Failed to delete contact: ${response.statusCode} - ${response.body}');
          }
        } catch (e) {
          print('연락처 삭제 오류: $e');
          rethrow;
        }
      }
    }
