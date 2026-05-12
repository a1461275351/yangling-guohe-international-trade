package com.trade.platform.module.settlement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.settlement.dto.SettlementQueryDTO;
import com.trade.platform.module.settlement.entity.Settlement;
import com.trade.platform.module.settlement.mapper.SettlementMapper;
import com.trade.platform.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service
public class SettlementService {

    @Resource
    private SettlementMapper settlementMapper;

    public PageResult<Settlement> getList(SettlementQueryDTO dto) {
        LambdaQueryWrapper<Settlement> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getSettlementNo())) {
            wrapper.like(Settlement::getSettlementNo, dto.getSettlementNo());
        }
        if (StringUtils.hasText(dto.getSettlementType())) {
            wrapper.eq(Settlement::getSettlementType, dto.getSettlementType());
        }
        if (dto.getEnterpriseId() != null) {
            wrapper.eq(Settlement::getEnterpriseId, dto.getEnterpriseId());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Settlement::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(Settlement::getCreateTime);

        Page<Settlement> page = new Page<>(dto.getCurrent(), dto.getSize());
        settlementMapper.selectPage(page, wrapper);
        return PageResult.from(page);
    }

    public Settlement getById(Long id) {
        return settlementMapper.selectById(id);
    }

    public void create(Settlement entity) {
        entity.setTenantId(UserContext.getTenantId());
        settlementMapper.insert(entity);
    }

    public void update(Settlement entity) {
        Settlement existing = settlementMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException("结算记录不存在");
        }
        settlementMapper.updateById(entity);
    }

    public void delete(Long id) {
        settlementMapper.deleteById(id);
    }
}
