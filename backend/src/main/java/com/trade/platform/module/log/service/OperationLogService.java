package com.trade.platform.module.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.log.dto.LogQueryDTO;
import com.trade.platform.module.log.entity.OperationLog;
import com.trade.platform.module.log.mapper.OperationLogMapper;
import com.trade.platform.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class OperationLogService {

    @Resource
    private OperationLogMapper logMapper;

    public PageResult<OperationLog> getList(LogQueryDTO dto) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like(OperationLog::getUsername, dto.getUsername());
        }
        if (StringUtils.hasText(dto.getModule())) {
            wrapper.eq(OperationLog::getModule, dto.getModule());
        }
        if (StringUtils.hasText(dto.getAction())) {
            wrapper.eq(OperationLog::getAction, dto.getAction());
        }
        if (StringUtils.hasText(dto.getStartDate())) {
            wrapper.ge(OperationLog::getCreateTime, LocalDate.parse(dto.getStartDate()).atStartOfDay());
        }
        if (StringUtils.hasText(dto.getEndDate())) {
            wrapper.le(OperationLog::getCreateTime, LocalDate.parse(dto.getEndDate()).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> page = new Page<>(dto.getCurrent(), dto.getSize());
        logMapper.selectPage(page, wrapper);

        PageResult<OperationLog> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    public void log(String module, String action, String targetType, Long targetId, String description) {
        OperationLog log = new OperationLog();
        try {
            log.setTenantId(UserContext.getTenantId());
            log.setUserId(UserContext.getUserId());
            log.setUsername(UserContext.getUsername());
        } catch (Exception ignored) {
        }
        log.setModule(module);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDescription(description);
        log.setStatus(1);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }

    public void logWithRequest(String module, String action, String targetType, Long targetId,
                               String description, String method, String url, String ip) {
        OperationLog log = new OperationLog();
        try {
            log.setTenantId(UserContext.getTenantId());
            log.setUserId(UserContext.getUserId());
            log.setUsername(UserContext.getUsername());
        } catch (Exception ignored) {
        }
        log.setModule(module);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDescription(description);
        log.setRequestMethod(method);
        log.setRequestUrl(url);
        log.setRequestIp(ip);
        log.setStatus(1);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }

    public void logError(String module, String action, String targetType, Long targetId,
                         String description, String method, String url, String ip, String errorMsg) {
        OperationLog log = new OperationLog();
        try {
            log.setTenantId(UserContext.getTenantId());
            log.setUserId(UserContext.getUserId());
            log.setUsername(UserContext.getUsername());
        } catch (Exception ignored) {
        }
        log.setModule(module);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDescription(description);
        log.setRequestMethod(method);
        log.setRequestUrl(url);
        log.setRequestIp(ip);
        log.setStatus(0);
        log.setErrorMsg(errorMsg != null && errorMsg.length() > 500 ? errorMsg.substring(0, 500) : errorMsg);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }
}
