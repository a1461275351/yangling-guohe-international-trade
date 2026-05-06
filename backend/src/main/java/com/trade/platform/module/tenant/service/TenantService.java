package com.trade.platform.module.tenant.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.Constants;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.tenant.dto.TenantQueryDTO;
import com.trade.platform.module.tenant.entity.Tenant;
import com.trade.platform.module.tenant.mapper.TenantMapper;
import com.trade.platform.module.user.mapper.UserMapper;
import com.trade.platform.module.user.entity.User;
import com.trade.platform.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantService {

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    /**
     * 分页查询租户列表
     */
    public PageResult<Tenant> getTenantList(TenantQueryDTO dto) {
        Page<Tenant> page = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<Tenant> wrapper = new QueryWrapper<>();

        if (StrUtil.isNotBlank(dto.getName())) {
            wrapper.like("name", dto.getName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq("status", dto.getStatus());
        }
        if (StrUtil.isNotBlank(dto.getStartDate())) {
            wrapper.ge("create_time", dto.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(dto.getEndDate())) {
            wrapper.le("create_time", dto.getEndDate() + " 23:59:59");
        }
        wrapper.orderByDesc("create_time");

        IPage<Tenant> result = tenantMapper.selectPage(page, wrapper);
        return PageResult.from(result);
    }

    /**
     * 根据ID获取租户
     */
    public Tenant getTenantById(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        return tenant;
    }

    /**
     * 创建租户
     */
    public void createTenant(Tenant tenant) {
        // 检查租户编码唯一性
        Long count = tenantMapper.selectCount(
                new QueryWrapper<Tenant>().eq("tenant_code", tenant.getTenantCode())
        );
        if (count > 0) {
            throw new BusinessException("租户编码已存在");
        }
        if (tenant.getStatus() == null) {
            tenant.setStatus(Constants.STATUS_ACTIVE);
        }
        tenantMapper.insert(tenant);
    }

    /**
     * 更新租户
     */
    public void updateTenant(Tenant tenant) {
        Tenant existing = tenantMapper.selectById(tenant.getId());
        if (existing == null) {
            throw new BusinessException("租户不存在");
        }
        // 如果修改了租户编码，需要检查唯一性
        if (StrUtil.isNotBlank(tenant.getTenantCode()) && !tenant.getTenantCode().equals(existing.getTenantCode())) {
            Long count = tenantMapper.selectCount(
                    new QueryWrapper<Tenant>().eq("tenant_code", tenant.getTenantCode())
            );
            if (count > 0) {
                throw new BusinessException("租户编码已存在");
            }
        }
        tenantMapper.updateById(tenant);
    }

    /**
     * 更新租户状态，同时更新该租户下所有用户的状态
     */
    @Transactional
    public void updateTenantStatus(Long id, Integer status) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        tenant.setStatus(status);
        tenantMapper.updateById(tenant);

        // 同步更新该租户下所有用户的状态
        userService.updateUserStatusByTenantId(id, status);
    }

    /**
     * 逻辑删除租户
     */
    public void deleteTenant(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }

        // 检查是否有关联的用户
        Long userCount = userMapper.selectCount(
                new QueryWrapper<User>().eq("tenant_id", id)
        );
        if (userCount > 0) {
            throw new BusinessException("该租户下存在用户，无法删除");
        }

        tenantMapper.deleteById(id);
    }

    /**
     * 获取所有启用的租户（用于下拉选择）
     */
    public List<Tenant> getAllActiveTenants() {
        return tenantMapper.selectList(
                new QueryWrapper<Tenant>().eq("status", Constants.STATUS_ACTIVE).orderByAsc("name")
        );
    }
}
