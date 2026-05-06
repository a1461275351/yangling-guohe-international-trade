package com.trade.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trade.platform.common.Result;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, 401, "未登录或token已过期");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            if (jwtUtil.isTokenExpired(claims)) {
                writeError(response, 401, "token已过期");
                return false;
            }

            UserContext.UserInfo userInfo = new UserContext.UserInfo(
                    jwtUtil.getUserId(claims),
                    jwtUtil.getUsername(claims),
                    jwtUtil.getRole(claims),
                    jwtUtil.getTenantId(claims)
            );
            UserContext.set(userInfo);
            return true;
        } catch (Exception e) {
            writeError(response, 401, "无效的token");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result<Void> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
