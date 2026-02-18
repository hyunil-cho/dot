import 'package:flutter/material.dart';
import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/service/contact_service.dart';

class ContactsProvider extends ChangeNotifier {
  final ContactService _contactService = ContactService();
  
  List<Contact> _contacts = [];
  bool _isLoading = false;
  String? _errorMessage;
  String _searchQuery = '';

  List<Contact> get contacts => _contacts;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  // 연락처 목록 가져오기
  Future<void> fetchContacts(String token) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      _contacts = await _contactService.getContacts(token);
    } catch (e) {
      _errorMessage = '연락처를 불러오는 데 실패했습니다.';
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  // 검색어 설정
  void setSearchQuery(String query) {
    _searchQuery = query;
    notifyListeners();
  }

  // 필터링된 연락처 목록 반환
  List<Contact> get filteredContacts {
    if (_searchQuery.isEmpty) {
      return _contacts;
    }
    return _contacts.where((contact) {
      final nameLower = contact.name.toLowerCase();
      final queryLower = _searchQuery.toLowerCase();
      return nameLower.contains(queryLower) || contact.phoneNumber.contains(_searchQuery);
    }).toList();
  }

  // 새로운 연락처 추가
  void addContact(Contact contact) {
    _contacts.insert(0, contact);
    notifyListeners();
  }

  // 연락처 정보 업데이트
  void updateContact(Contact updatedContact) {
    final index = _contacts.indexWhere((c) => c.id == updatedContact.id);
    if (index != -1) {
      _contacts[index] = updatedContact;
      notifyListeners();
    }
  }

  // ID로 연락처 찾기
  Contact? getContactById(String id) {
    try {
      return _contacts.firstWhere((c) => c.id == id);
    } catch (e) {
      return null;
    }
  }
}
