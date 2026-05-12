package com.trade.platform.common;

import com.trade.platform.module.log.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class OpLogAspect {

    @Resource
    private OperationLogService operationLogService;

    @Around("@annotation(com.trade.platform.common.OpLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OpLog opLog = method.getAnnotation(OpLog.class);

        String reqMethod = "";
        String reqUrl = "";
        String reqIp = "";
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                reqMethod = request.getMethod();
                reqUrl = request.getRequestURI();
                reqIp = getClientIp(request);
            }
        } catch (Exception ignored) {
        }

        Long targetId = extractTargetId(point.getArgs());

        try {
            Object result = point.proceed();
            operationLogService.logWithRequest(
                    opLog.module(), opLog.action(), opLog.module(), targetId,
                    opLog.description(), reqMethod, reqUrl, reqIp);
            return result;
        } catch (Throwable e) {
            operationLogService.logError(
                    opLog.module(), opLog.action(), opLog.module(), targetId,
                    opLog.description(), reqMethod, reqUrl, reqIp, e.getMessage());
            throw e;
        }
    }

    private Long extractTargetId(Object[] args) {
        if (args == null || args.length == 0) return null;
        for (Object arg : args) {
            if (arg instanceof Long) return (Long) arg;
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
