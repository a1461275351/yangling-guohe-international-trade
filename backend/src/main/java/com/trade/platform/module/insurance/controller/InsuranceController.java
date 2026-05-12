package com.trade.platform.module.insurance.controller;

import com.trade.platform.common.OpLog;
import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.insurance.dto.InsuranceQueryDTO;
import com.trade.platform.module.insurance.entity.Insurance;
import com.trade.platform.module.insurance.service.InsuranceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/insurances")
public class InsuranceController {

    @Resource
    private InsuranceService insuranceService;

    @PostMapping("/list")
    public Result<PageResult<Insurance>> list(@RequestBody InsuranceQueryDTO dto) {
        return Result.success(insuranceService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Insurance> getById(@PathVariable Long id) {
        return Result.success(insuranceService.getById(id));
    }

    @OpLog(module = "信保服务", action = "CREATE", description = "新增信保服务")
    @PostMapping
    public Result<Void> create(@RequestBody Insurance entity) {
        insuranceService.create(entity);
        return Result.success();
    }

    @OpLog(module = "信保服务", action = "UPDATE", description = "编辑信保服务")
    @PutMapping
    public Result<Void> update(@RequestBody Insurance entity) {
        insuranceService.update(entity);
        return Result.success();
    }

    @OpLog(module = "信保服务", action = "DELETE", description = "删除信保服务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        insuranceService.delete(id);
        return Result.success();
    }
}
