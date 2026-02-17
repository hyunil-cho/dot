import 'package:flutter/material.dart';
import 'package:dot_frontend/model/contact.dart';

class ContactsProvider extends ChangeNotifier {
  // 더미 데이터
  final List<Contact> _contacts = [
    Contact(id: '1', name: 'Alice', phoneNumber: '010-1234-5678', relationship: 'Friend', memo: 'Met at conference'),
    Contact(id: '2', name: 'Bob', phoneNumber: '010-2345-6789', relationship: 'Colleague', memo: 'Project manager'),
    Contact(id: '3', name: 'Charlie', phoneNumber: '010-3456-7890', relationship: 'Family', memo: 'Brother'),
    Contact(id: '4', name: 'David', phoneNumber: '010-4567-8901', relationship: 'Friend', memo: 'High school friend'),
    Contact(id: '5', name: 'Eve', phoneNumber: '010-5678-9012', relationship: 'Colleague', memo: 'Designer'),
    Contact(id: '6', name: 'Frank', phoneNumber: '010-6789-0123', relationship: 'Unknown', memo: ''),
    Contact(id: '7', name: 'Grace', phoneNumber: '010-7890-1234', relationship: 'Family', memo: 'Cousin'),
    Contact(id: '8', name: 'Hannah', phoneNumber: '010-8901-2345', relationship: 'Friend', memo: 'Gym buddy'),
    Contact(id: '9', name: 'Ian', phoneNumber: '010-9012-3456', relationship: 'Colleague', memo: 'Developer'),
    Contact(id: '10', name: 'Jack', phoneNumber: '010-0123-4567', relationship: 'Unknown', memo: ''),
  ];

  String _searchQuery = '';

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
}
