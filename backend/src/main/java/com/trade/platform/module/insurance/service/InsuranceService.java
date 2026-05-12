package com.trade.platform.module.insurance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.insurance.dto.InsuranceQueryDTO;
import com.trade.platform.module.insurance.entity.Insurance;
import com.trade.platform.module.insurance.mapper.InsuranceMapper;
import com.trade.platform.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service
public class InsuranceService {

    @Resource
    private InsuranceMapper insuranceMapper;

    public PageResult<Insurance> getList(InsuranceQueryDTO dto) {
        LambdaQueryWrapper<Insurance> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getPolicyNo())) {
            wrapper.like(Insurance::getPolicyNo, dto.getPolicyNo());
        }
        if (dto.getEnterpriseId() != null) {
            wrapper.eq(Insurance::getEnterpriseId, dto.getEnterpriseId());
        }
        if (StringUtils.hasText(dto.getBuyerCountry())) {
            wrapper.eq(Insurance::getBuyerCountry, dto.getBuyerCountry());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Insurance::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(Insurance::getCreateTime);

        Page<Insurance> page = new Page<>(dto.getCurrent(), dto.getSize());
        insuranceMapper.selectPage(page, wrapper);
        return PageResult.from(page);
    }

    public Insurance getById(Long id) {
        return insuranceMapper.selectById(id);
    }

    public void create(Insurance entity) {
        entity.setTenantId(UserContext.getTenantId());
        insuranceMapper.insert(entity);
    }

    public void update(Insurance entity) {
        Insurance existing = insuranceMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException("信保记录不存在");
        }
        insuranceMapper.updateById(entity);
    }

    public void delete(Long id) {
        insuranceMapper.deleteById(id);
    }
}
