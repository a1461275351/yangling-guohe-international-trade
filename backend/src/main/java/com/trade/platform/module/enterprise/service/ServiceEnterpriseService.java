package com.trade.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.enterprise.dto.EnterpriseQueryDTO;
import com.trade.platform.module.enterprise.entity.ServiceEnterprise;
import com.trade.platform.module.enterprise.mapper.ServiceEnterpriseMapper;
import com.trade.platform.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ServiceEnterpriseService {

    @Resource
    private ServiceEnterpriseMapper enterpriseMapper;

    public PageResult<ServiceEnterprise> getList(EnterpriseQueryDTO dto) {
        LambdaQueryWrapper<ServiceEnterprise> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getEnterpriseName())) {
            wrapper.like(ServiceEnterprise::getEnterpriseName, dto.getEnterpriseName());
        }
        if (StringUtils.hasText(dto.getCreditCode())) {
            wrapper.like(ServiceEnterprise::getCreditCode, dto.getCreditCode());
        }
        if (StringUtils.hasText(dto.getAgreementStatus())) {
            wrapper.eq(ServiceEnterprise::getAgreementStatus, dto.getAgreementStatus());
        }
        if (StringUtils.hasText(dto.getRiskLevel())) {
            wrapper.eq(ServiceEnterprise::getRiskLevel, dto.getRiskLevel());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(ServiceEnterprise::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(ServiceEnterprise::getCreateTime);

        Page<ServiceEnterprise> page = new Page<>(dto.getCurrent(), dto.getSize());
        enterpriseMapper.selectPage(page, wrapper);
        return PageResult.from(page);
    }

    public ServiceEnterprise getById(Long id) {
        return enterpriseMapper.selectById(id);
    }

    public void create(ServiceEnterprise entity) {
        entity.setTenantId(UserContext.getTenantId());
        enterpriseMapper.insert(entity);
    }

    public void update(ServiceEnterprise entity) {
        ServiceEnterprise existing = enterpriseMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException("服务企业不存在");
        }
        enterpriseMapper.updateById(entity);
    }

    public void delete(Long id) {
        enterpriseMapper.deleteById(id);
    }

    public void batchDelete(List<Long> ids) {
        enterpriseMapper.deleteBatchIds(ids);
    }

    public void updateStatus(Long id, Integer status) {
        ServiceEnterprise entity = enterpriseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("服务企业不存在");
        }
        entity.setStatus(status);
        enterpriseMapper.updateById(entity);
    }

    public List<ServiceEnterprise> getActiveList() {
        LambdaQueryWrapper<ServiceEnterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceEnterprise::getStatus, 1);
        wrapper.orderByAsc(ServiceEnterprise::getEnterpriseName);
        return enterpriseMapper.selectList(wrapper);
    }
}
