import 'package:flutter/material.dart';

// 배경을 그리는 위젯
class BackgroundDesign extends StatelessWidget {
  const BackgroundDesign({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            Color(0xFF2E1A47), // 깊은 보라색
            Color(0xFF4A148C), // 보라색
            Color(0xFF7B1FA2), // 밝은 보라색
          ],
        ),
      ),
      child: Stack(
        children: [
          // 장식용 원 1 (우측 상단)
          Positioned(
            top: -100,
            right: -100,
            child: Container(
              width: 300,
              height: 300,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.white.withOpacity(0.05),
              ),
            ),
          ),
          // 장식용 원 2 (좌측 하단)
          Positioned(
            bottom: 150,
            left: -50,
            child: Container(
              width: 200,
              height: 200,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.white.withOpacity(0.05),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
