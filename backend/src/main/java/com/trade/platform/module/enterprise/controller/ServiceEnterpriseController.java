package com.trade.platform.module.enterprise.controller;

import com.trade.platform.common.OpLog;
import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.enterprise.dto.EnterpriseQueryDTO;
import com.trade.platform.module.enterprise.entity.ServiceEnterprise;
import com.trade.platform.module.enterprise.service.ServiceEnterpriseService;
import com.trade.platform.security.RequireRole;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enterprises")
public class ServiceEnterpriseController {

    @Resource
    private ServiceEnterpriseService enterpriseService;

    @PostMapping("/list")
    public Result<PageResult<ServiceEnterprise>> list(@RequestBody EnterpriseQueryDTO dto) {
        return Result.success(enterpriseService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<ServiceEnterprise> getById(@PathVariable Long id) {
        return Result.success(enterpriseService.getById(id));
    }

    @OpLog(module = "服务企业", action = "CREATE", description = "新增服务企业")
    @PostMapping
    public Result<Void> create(@RequestBody ServiceEnterprise entity) {
        enterpriseService.create(entity);
        return Result.success();
    }

    @OpLog(module = "服务企业", action = "UPDATE", description = "编辑服务企业")
    @PutMapping
    public Result<Void> update(@RequestBody ServiceEnterprise entity) {
        enterpriseService.update(entity);
        return Result.success();
    }

    @OpLog(module = "服务企业", action = "DELETE", description = "删除服务企业")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        enterpriseService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        enterpriseService.batchDelete(body.get("ids"));
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        enterpriseService.updateStatus(id, status);
        return Result.success();
    }

    @GetMapping("/active")
    public Result<List<ServiceEnterprise>> getActiveList() {
        return Result.success(enterpriseService.getActiveList());
    }
}
