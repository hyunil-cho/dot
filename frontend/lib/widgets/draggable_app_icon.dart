import 'package:flutter/material.dart';

class DraggableAppIcon extends StatelessWidget {
  final double x;
  final double y;
  final IconData icon;
  final String label;
  final Color color;
  final double size; // 아이콘(박스)의 크기
  final Function(double dx, double dy) onDragUpdate;
  final VoidCallback? onDragEnd;
  final VoidCallback? onTap;

  const DraggableAppIcon({
    super.key,
    required this.x,
    required this.y,
    required this.icon,
    required this.label,
    required this.onDragUpdate,
    required this.size,
    this.onDragEnd,
    this.color = Colors.white,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Positioned(
      left: x,
      top: y,
      child: GestureDetector(
        onPanUpdate: (details) {
          onDragUpdate(details.delta.dx, details.delta.dy);
        },
        onPanEnd: (details) {
          if (onDragEnd != null) {
            onDragEnd!();
          }
        },
        onTap: onTap,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: size,
              height: size,
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.2),
                borderRadius: BorderRadius.circular(size * 0.25), // 크기에 비례한 라운드
                border: Border.all(
                  color: Colors.white.withOpacity(0.3),
                  width: 1,
                ),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.1),
                    blurRadius: 8,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Icon(
                icon,
                color: color,
                size: size * 0.5, // 아이콘 심볼 크기도 비례하게 조정
              ),
            ),
            const SizedBox(height: 8),
            SizedBox(
              width: size + 20, // 텍스트 영역 확보
              child: Text(
                label,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                  shadows: [
                    Shadow(
                      color: Colors.black45,
                      blurRadius: 4,
                      offset: Offset(0, 2),
                    ),
                  ],
                ),
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
