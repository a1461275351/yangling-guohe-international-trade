package com.trade.platform.config;

import com.trade.platform.security.JwtFilter;
import com.trade.platform.security.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtFilter)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/templates/onlyoffice/callback",
                        "/api/templates/preview/**"
                );

        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/templates/onlyoffice/callback",
                        "/api/templates/preview/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 不再公开映射，文件下载通过 FileController 鉴权接口
        // 仅保留 OnlyOffice 内部访问需要的路径（通过内网访问，非公网暴露）
    }
}
