package com.trade.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trade.platform.common.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Check method-level annotation first, then class-level
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if (requireRole == null) {
            return true;
        }

        String currentRole = UserContext.getRole();
        if (currentRole == null) {
            writeError(response, 403, "无权访问");
            return false;
        }

        String[] allowedRoles = requireRole.value();
        boolean hasPermission = Arrays.asList(allowedRoles).contains(currentRole);

        if (!hasPermission) {
            writeError(response, 403, "无权访问");
            return false;
        }

        return true;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        Result<Void> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
