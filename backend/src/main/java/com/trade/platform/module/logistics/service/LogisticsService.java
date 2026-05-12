package com.trade.platform.module.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.logistics.dto.LogisticsQueryDTO;
import com.trade.platform.module.logistics.entity.Logistics;
import com.trade.platform.module.logistics.mapper.LogisticsMapper;
import com.trade.platform.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service
public class LogisticsService {

    @Resource
    private LogisticsMapper logisticsMapper;

    public PageResult<Logistics> getList(LogisticsQueryDTO dto) {
        LambdaQueryWrapper<Logistics> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getLogisticsNo())) {
            wrapper.like(Logistics::getLogisticsNo, dto.getLogisticsNo());
        }
        if (StringUtils.hasText(dto.getTransportMode())) {
            wrapper.eq(Logistics::getTransportMode, dto.getTransportMode());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Logistics::getStatus, dto.getStatus());
        }
        if (dto.getOrderId() != null) {
            wrapper.eq(Logistics::getOrderId, dto.getOrderId());
        }
        wrapper.orderByDesc(Logistics::getCreateTime);

        Page<Logistics> page = new Page<>(dto.getCurrent(), dto.getSize());
        logisticsMapper.selectPage(page, wrapper);
        return PageResult.from(page);
    }

    public Logistics getById(Long id) {
        return logisticsMapper.selectById(id);
    }

    public void create(Logistics entity) {
        entity.setTenantId(UserContext.getTenantId());
        logisticsMapper.insert(entity);
    }

    public void update(Logistics entity) {
        Logistics existing = logisticsMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException("物流记录不存在");
        }
        logisticsMapper.updateById(entity);
    }

    public void delete(Long id) {
        logisticsMapper.deleteById(id);
    }

    public void updateStatus(Long id, String status) {
        Logistics entity = logisticsMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("物流记录不存在");
        }
        entity.setStatus(status);
        logisticsMapper.updateById(entity);
    }
}
