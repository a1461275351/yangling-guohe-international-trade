package com.trade.platform.module.logistics.controller;

import com.trade.platform.common.OpLog;
import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.logistics.dto.LogisticsQueryDTO;
import com.trade.platform.module.logistics.entity.Logistics;
import com.trade.platform.module.logistics.service.LogisticsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    @Resource
    private LogisticsService logisticsService;

    @PostMapping("/list")
    public Result<PageResult<Logistics>> list(@RequestBody LogisticsQueryDTO dto) {
        return Result.success(logisticsService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Logistics> getById(@PathVariable Long id) {
        return Result.success(logisticsService.getById(id));
    }

    @OpLog(module = "物流服务", action = "CREATE", description = "新增物流服务")
    @PostMapping
    public Result<Void> create(@RequestBody Logistics entity) {
        logisticsService.create(entity);
        return Result.success();
    }

    @OpLog(module = "物流服务", action = "UPDATE", description = "编辑物流服务")
    @PutMapping
    public Result<Void> update(@RequestBody Logistics entity) {
        logisticsService.update(entity);
        return Result.success();
    }

    @OpLog(module = "物流服务", action = "DELETE", description = "删除物流服务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        logisticsService.delete(id);
        return Result.success();
    }

    @OpLog(module = "物流服务", action = "UPDATE", description = "变更物流服务状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        logisticsService.updateStatus(id, status);
        return Result.success();
    }
}
