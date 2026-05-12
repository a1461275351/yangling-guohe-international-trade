package com.trade.platform.module.taxrefund.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.taxrefund.dto.TaxRefundQueryDTO;
import com.trade.platform.module.taxrefund.entity.TaxRefund;
import com.trade.platform.module.taxrefund.mapper.TaxRefundMapper;
import com.trade.platform.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service
public class TaxRefundService {

    @Resource
    private TaxRefundMapper taxRefundMapper;

    public PageResult<TaxRefund> getList(TaxRefundQueryDTO dto) {
        LambdaQueryWrapper<TaxRefund> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getRefundNo())) {
            wrapper.like(TaxRefund::getRefundNo, dto.getRefundNo());
        }
        if (dto.getEnterpriseId() != null) {
            wrapper.eq(TaxRefund::getEnterpriseId, dto.getEnterpriseId());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(TaxRefund::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getRiskFlag())) {
            wrapper.eq(TaxRefund::getRiskFlag, dto.getRiskFlag());
        }
        wrapper.orderByDesc(TaxRefund::getCreateTime);

        Page<TaxRefund> page = new Page<>(dto.getCurrent(), dto.getSize());
        taxRefundMapper.selectPage(page, wrapper);
        return PageResult.from(page);
    }

    public TaxRefund getById(Long id) {
        return taxRefundMapper.selectById(id);
    }

    public void create(TaxRefund entity) {
        entity.setTenantId(UserContext.getTenantId());
        taxRefundMapper.insert(entity);
    }

    public void update(TaxRefund entity) {
        TaxRefund existing = taxRefundMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException("退税记录不存在");
        }
        taxRefundMapper.updateById(entity);
    }

    public void delete(Long id) {
        taxRefundMapper.deleteById(id);
    }

    public void updateStatus(Long id, String status) {
        TaxRefund entity = taxRefundMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("退税记录不存在");
        }
        entity.setStatus(status);
        taxRefundMapper.updateById(entity);
    }
}
