import 'package:dot_frontend/ui/contacts/contacts_screen.dart'; // ContactsScreen import
import 'package:dot_frontend/ui/settings/settings_screen.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:flutter/material.dart';

class MainScreen extends StatefulWidget {
  final int selectedIndex;

  const MainScreen({super.key, this.selectedIndex = 0});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
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
    // 현재 페이지와 같은 탭을 누르면 아무것도 하지 않음
    if (index == widget.selectedIndex) return;

    switch (index) {
      case 0:
        Navigator.pushReplacementNamed(context, '/home');
        break;
      case 1:
        Navigator.pushReplacementNamed(context, '/contacts');
        break;
      case 2:
        Navigator.pushReplacementNamed(context, '/settings');
        break;
    }
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
            child: _widgetOptions.elementAt(widget.selectedIndex),
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
              icon: Icon(Icons.message),
              label: '메시지',
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
          currentIndex: widget.selectedIndex,
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
