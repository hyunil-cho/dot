package com.dot.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Dot API",
        version = "v1.0.0",
        description = """
            Dot Backend API - 감성 채팅 시스템
            
            사용자가 사랑하는 사람의 성향을 AI에게 학습시켜 
            실제 채팅과 유사한 경험을 제공하는 텍스트 기반 감성 채팅 시스템입니다.
            
            ## 인증 방법
            - JWT Bearer Token 방식 사용
            - 로그인 후 발급받은 Access Token을 헤더에 포함하여 요청
            - 형식: `Authorization: Bearer {accessToken}`
            
            ## 주요 기능
            - 회원 관리 (가입, 로그인, 탈퇴)
            - Persona 관리 (연락처 형태)
            - 학습 데이터 업로드
            - AI 채팅
            
            ## 문서
            - 상세 가이드: /backend/docs/SWAGGER_GUIDE.md
            - Auth API: /backend/docs/AUTH_SWAGGER_DOCUMENTATION.md
            """,
        contact = @Contact(
            name = "Dot Development Team",
            email = "dev@dot-project.com"
        )
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT 인증 토큰을 입력하세요. 로그인 API를 통해 발급받을 수 있습니다."
)
public class OpenApiConfig {
}

