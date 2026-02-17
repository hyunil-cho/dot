import 'package:flutter/material.dart';
import 'package:dot_frontend/entry/slide_to_start_screen.dart';

void main() {
  runApp(const DotApp());
}

class DotApp extends StatelessWidget {
  const DotApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Dot',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        // 어두운 배경에 어울리는 테마 설정
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF6C63FF),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      home: const SlideToStartScreen(),
    );
  }
}
