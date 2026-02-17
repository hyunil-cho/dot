import 'package:flutter/material.dart';
import 'package:dot_frontend/widgets/background_design.dart';
import 'package:dot_frontend/auth/login_screen.dart';

class SlideToStartScreen extends StatelessWidget {
  const SlideToStartScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          // 1. 배경: 그라데이션 및 장식 (이미지 대신 코드 구현)
          const BackgroundDesign(),

          // 2. 로고 및 텍스트 (화면 중앙 상단)
          Positioned(
            top: 0,
            bottom: 200, // 슬라이더 공간 확보
            left: 0,
            right: 0,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: const [
                Icon(
                  Icons.circle, // 임시 로고 아이콘
                  size: 80,
                  color: Colors.white,
                ),
                SizedBox(height: 24),
                Text(
                  'DOT',
                  style: TextStyle(
                    fontSize: 48,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                    letterSpacing: 4,
                  ),
                ),
                SizedBox(height: 8),
                Text(
                  'Connect the world',
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.white70,
                    letterSpacing: 1.2,
                  ),
                ),
              ],
            ),
          ),

          // 3. 슬라이드 위젯 (화면 하단 배치)
          const Positioned(
            bottom: 80,
            left: 40,
            right: 40,
            child: SlideToStartWidget(),
          ),
        ],
      ),
    );
  }
}

class SlideToStartWidget extends StatefulWidget {
  const SlideToStartWidget({super.key});

  @override
  State<SlideToStartWidget> createState() => _SlideToStartWidgetState();
}

class _SlideToStartWidgetState extends State<SlideToStartWidget> {
  double _dragValue = 0.0;
  final double _handleSize = 56.0; // 핸들 크기

  void _onDragUpdate(DragUpdateDetails details, double maxWidth) {
    setState(() {
      double dragRange = maxWidth - _handleSize;
      if (dragRange <= 0) return;

      double delta = details.delta.dx / dragRange;
      _dragValue = (_dragValue + delta).clamp(0.0, 1.0);
    });
  }

  void _onDragEnd(DragEndDetails details) {
    if (_dragValue > 0.9) {
      // 성공 시
      print("앱 시작! (Slide Completed)");
      // 화면 전환 로직 추가
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (context) => const LoginScreen()),
      );
    } else {
      // 실패 시 원위치
      setState(() {
        _dragValue = 0.0;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final maxWidth = constraints.maxWidth;

        return Container(
          width: maxWidth,
          height: _handleSize,
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.2), // 슬라이더 트랙 배경
            borderRadius: BorderRadius.circular(_handleSize / 2),
            border: Border.all(
              color: Colors.white.withOpacity(0.3),
              width: 1,
            ),
          ),
          child: Stack(
            children: [
              // 안내 텍스트
              const Center(
                child: Text(
                  '밀어서 시작하기',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 16,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
              // 드래그 핸들
              Align(
                alignment: Alignment.centerLeft,
                child: Transform.translate(
                  offset: Offset(_dragValue * (maxWidth - _handleSize), 0),
                  child: GestureDetector(
                    onHorizontalDragUpdate: (details) => _onDragUpdate(details, maxWidth),
                    onHorizontalDragEnd: _onDragEnd,
                    child: Container(
                      width: _handleSize,
                      height: _handleSize,
                      decoration: BoxDecoration(
                        color: Colors.white,
                        shape: BoxShape.circle,
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.2),
                            blurRadius: 6,
                            offset: const Offset(0, 2),
                          ),
                        ],
                      ),
                      child: const Icon(
                        Icons.arrow_forward,
                        color: Color(0xFF4A148C), // 아이콘 색상
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
