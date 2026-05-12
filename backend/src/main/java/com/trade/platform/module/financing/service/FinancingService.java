package com.trade.platform.module.financing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.financing.dto.FinancingQueryDTO;
import com.trade.platform.module.financing.entity.Financing;
import com.trade.platform.module.financing.mapper.FinancingMapper;
import com.trade.platform.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service
public class FinancingService {

    @Resource
    private FinancingMapper financingMapper;

    public PageResult<Financing> getList(FinancingQueryDTO dto) {
        LambdaQueryWrapper<Financing> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getFinancingNo())) {
            wrapper.like(Financing::getFinancingNo, dto.getFinancingNo());
        }
        if (dto.getEnterpriseId() != null) {
            wrapper.eq(Financing::getEnterpriseId, dto.getEnterpriseId());
        }
        if (StringUtils.hasText(dto.getFinancingType())) {
            wrapper.eq(Financing::getFinancingType, dto.getFinancingType());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Financing::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(Financing::getCreateTime);

        Page<Financing> page = new Page<>(dto.getCurrent(), dto.getSize());
        financingMapper.selectPage(page, wrapper);
        return PageResult.from(page);
    }

    public Financing getById(Long id) {
        return financingMapper.selectById(id);
    }

    public void create(Financing entity) {
        entity.setTenantId(UserContext.getTenantId());
        financingMapper.insert(entity);
    }

    public void update(Financing entity) {
        Financing existing = financingMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException("融资记录不存在");
        }
        financingMapper.updateById(entity);
    }

    public void delete(Long id) {
        financingMapper.deleteById(id);
    }
}
