import 'package:flutter/material.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/settings/settings_screen.dart';
import 'package:dot_frontend/ui/contacts/contacts_screen.dart'; // ContactsScreen import

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  int _selectedIndex = 0; // 현재 선택된 탭의 인덱스

  // 각 탭에 해당하는 페이지
  static const List<Widget> _widgetOptions = <Widget>[
    // 1. 전화 (Phone)
    Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.phone, size: 80, color: Colors.white),
          SizedBox(height: 16),
          Text('Phone', style: TextStyle(color: Colors.white, fontSize: 24)),
        ],
      ),
    ),
    // 2. 연락처 (Contacts)
    ContactsScreen(), // ContactsScreen 연결
    // 3. 설정 (Settings)
    SettingsScreen(),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // 배경이 전체 화면을 덮도록 Stack 사용
      body: Stack(
        children: [
          // 배경 디자인 재사용
          const BackgroundDesign(),
          
          // 선택된 탭에 해당하는 페이지 표시
          // SafeArea를 사용하여 상단 상태바 영역 침범 방지
          SafeArea(
            child: _widgetOptions.elementAt(_selectedIndex),
          ),
        ],
      ),
      bottomNavigationBar: Container(
        // 네비게이션 바 배경을 투명하게 하거나 그라데이션을 줄 수 있음
        decoration: BoxDecoration(
          color: Colors.black.withOpacity(0.2), // 반투명 배경
          border: Border(
            top: BorderSide(
              color: Colors.white.withOpacity(0.1),
              width: 0.5,
            ),
          ),
        ),
        child: BottomNavigationBar(
          items: const <BottomNavigationBarItem>[
            BottomNavigationBarItem(
              icon: Icon(Icons.phone),
              label: '전화',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.contacts),
              label: '연락처',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.settings),
              label: '설정',
            ),
          ],
          currentIndex: _selectedIndex,
          onTap: _onItemTapped,
          // --- 스타일링 ---
          backgroundColor: Colors.transparent, // Container 색상을 따름
          elevation: 0, // 그림자 제거 (깔끔하게)
          selectedItemColor: Colors.white, // 선택된 아이콘 색상
          unselectedItemColor: Colors.white.withOpacity(0.5), // 선택되지 않은 아이콘 색상
          showUnselectedLabels: true,
          type: BottomNavigationBarType.fixed,
        ),
      ),
    );
  }
}
