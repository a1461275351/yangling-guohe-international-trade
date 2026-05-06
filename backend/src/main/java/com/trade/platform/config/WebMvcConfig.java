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
        // 映射 /uploads/** 到文件系统，让 ONLYOFFICE 可通过 URL 访问文件
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().normalize().toString();
        if (!absolutePath.endsWith("/") && !absolutePath.endsWith("\\")) {
            absolutePath += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
