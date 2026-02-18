package com.dot.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 정적 리소스 핸들러 설정 (업로드된 파일 서빙)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // uploads/** 경로로 요청이 오면 실제 파일 시스템의 uploadDir에서 찾음
        String path = new File(uploadDir).getAbsolutePath();
        
        registry.addResourceHandler("/" + uploadDir + "/**")
                .addResourceLocations("file:" + path + File.separator);
    }
}
