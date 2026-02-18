import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:dot_frontend/config/constants.dart';

class ApiService {
  final String _baseUrl = ApiConstants.baseUrl;

  // 공통 헤더 설정
  Map<String, String> _headers(String? token) {
    final headers = {
      'Content-Type': 'application/json; charset=UTF-8',
    };
    if (token != null) {
      headers['Authorization'] = 'Bearer $token';
    }
    return headers;
  }

  // GET 요청
  Future<http.Response> get(String endpoint, {String? token}) async {
    final response = await http.get(
      Uri.parse('$_baseUrl$endpoint'),
      headers: _headers(token),
    );
    return _handleResponse(response);
  }

  // POST 요청
  Future<http.Response> post(String endpoint, Map<String, dynamic> body, {String? token}) async {
    final response = await http.post(
      Uri.parse('$_baseUrl$endpoint'),
      headers: _headers(token),
      body: jsonEncode(body),
    );
    return _handleResponse(response);
  }

  // PUT 요청
  Future<http.Response> put(String endpoint, Map<String, dynamic> body, {String? token}) async {
    final response = await http.put(
      Uri.parse('$_baseUrl$endpoint'),
      headers: _headers(token),
      body: jsonEncode(body),
    );
    return _handleResponse(response);
  }

  // DELETE 요청
  Future<http.Response> delete(String endpoint, {String? token}) async {
    final response = await http.delete(
      Uri.parse('$_baseUrl$endpoint'),
      headers: _headers(token),
    );
    return _handleResponse(response);
  }

  // 응답 처리 및 에러 핸들링
  http.Response _handleResponse(http.Response response) {
    if (response.statusCode >= 200 && response.statusCode < 300) {
      return response;
    } else {
      // 에러 발생 시 예외 던짐 (추후 상세 에러 처리 로직 추가 가능)
      throw Exception('Failed to load data: ${response.statusCode} - ${response.body}');
    }
  }
}
