import 'package:flutter_web_plugins/url_strategy.dart';

void configureUrl() {
  // 웹에서는 PathUrlStrategy를 사용하여 #을 제거합니다.
  usePathUrlStrategy();
}
