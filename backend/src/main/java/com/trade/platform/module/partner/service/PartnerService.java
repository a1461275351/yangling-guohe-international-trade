package com.trade.platform.module.partner.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.partner.dto.PartnerQueryDTO;
import com.trade.platform.module.partner.entity.Partner;
import com.trade.platform.module.partner.mapper.PartnerMapper;
import com.trade.platform.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PartnerService {

    @Resource
    private PartnerMapper partnerMapper;

    public PageResult<Partner> getList(PartnerQueryDTO dto) {
        LambdaQueryWrapper<Partner> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getType())) {
            wrapper.eq(Partner::getType, dto.getType());
        }
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Partner::getName, dto.getName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Partner::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(Partner::getCreateTime);

        Page<Partner> page = new Page<>(dto.getCurrent(), dto.getSize());
        partnerMapper.selectPage(page, wrapper);

        PageResult<Partner> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    public Partner getById(Long id) {
        Partner partner = partnerMapper.selectById(id);
        if (partner == null) {
            throw new BusinessException("合作伙伴不存在");
        }
        return partner;
    }

    public void create(Partner partner) {
        partner.setTenantId(UserContext.getTenantId());
        partnerMapper.insert(partner);
    }

    public void update(Partner partner) {
        Partner existing = partnerMapper.selectById(partner.getId());
        if (existing == null) {
            throw new BusinessException("合作伙伴不存在");
        }
        partnerMapper.updateById(partner);
    }

    public void delete(Long id) {
        Partner existing = partnerMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("合作伙伴不存在");
        }
        partnerMapper.deleteById(id);
    }

    public void updateStatus(Long id, Integer status) {
        Partner partner = partnerMapper.selectById(id);
        if (partner == null) {
            throw new BusinessException("合作伙伴不存在");
        }
        partner.setStatus(status);
        partnerMapper.updateById(partner);
    }

    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要操作的合作伙伴");
        }
        for (Long id : ids) {
            Partner partner = partnerMapper.selectById(id);
            if (partner != null) {
                partner.setStatus(status);
                partnerMapper.updateById(partner);
            }
        }
    }

    public List<Partner> getActiveByType(String type) {
        LambdaQueryWrapper<Partner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Partner::getType, type);
        wrapper.eq(Partner::getStatus, 1);
        wrapper.orderByAsc(Partner::getName);
        return partnerMapper.selectList(wrapper);
    }
}
