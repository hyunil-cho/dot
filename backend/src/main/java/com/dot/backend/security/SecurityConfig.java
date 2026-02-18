package com.dot.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 설정
 *
 * - JWT 기반 인증
 * - Session 사용 안 함 (Stateless)
 * - 회원가입/로그인은 인증 없이 접근 가능
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용)
            .csrf(csrf -> csrf.disable())

            // CORS 설정 활성화 (Flutter Web 연동)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // Session 사용 안 함
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 인증 규칙
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/api/auth/**",      // 회원가입, 로그인
                                "/h2-console/**",    // H2 Console (개발용)
                                "/error",            // 에러 페이지
                                "/swagger-ui/**",    // Swagger UI
                                "/swagger-ui.html",
                                "/v3/api-docs/**",   // OpenAPI JSON
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

            // H2 Console 사용을 위한 설정
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )

            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

