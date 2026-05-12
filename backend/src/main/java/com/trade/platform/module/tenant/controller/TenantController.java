package com.trade.platform.module.tenant.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.security.RequireRole;
import com.trade.platform.common.Result;
import com.trade.platform.module.tenant.dto.TenantQueryDTO;
import com.trade.platform.module.tenant.entity.Tenant;
import com.trade.platform.module.tenant.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequireRole({"ADMIN", "GUOHE"})
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @PostMapping("/list")
    public Result<PageResult<Tenant>> list(@RequestBody TenantQueryDTO dto) {
        PageResult<Tenant> result = tenantService.getTenantList(dto);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Tenant> getById(@PathVariable Long id) {
        Tenant tenant = tenantService.getTenantById(id);
        return Result.success(tenant);
    }

    @PostMapping
    public Result<Void> create(@RequestBody Tenant tenant) {
        tenantService.createTenant(tenant);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Tenant tenant) {
        tenantService.updateTenant(tenant);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        tenantService.updateTenantStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return Result.success();
    }

    @GetMapping("/all")
    public Result<List<Tenant>> getAll() {
        List<Tenant> tenants = tenantService.getAllActiveTenants();
        return Result.success(tenants);
    }
}
