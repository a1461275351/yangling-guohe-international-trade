package com.trade.platform.module.financing.controller;

import com.trade.platform.common.OpLog;
import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.financing.dto.FinancingQueryDTO;
import com.trade.platform.module.financing.entity.Financing;
import com.trade.platform.module.financing.service.FinancingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/financings")
public class FinancingController {

    @Resource
    private FinancingService financingService;

    @PostMapping("/list")
    public Result<PageResult<Financing>> list(@RequestBody FinancingQueryDTO dto) {
        return Result.success(financingService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Financing> getById(@PathVariable Long id) {
        return Result.success(financingService.getById(id));
    }

    @OpLog(module = "融资协助", action = "CREATE", description = "新增融资协助")
    @PostMapping
    public Result<Void> create(@RequestBody Financing entity) {
        financingService.create(entity);
        return Result.success();
    }

    @OpLog(module = "融资协助", action = "UPDATE", description = "编辑融资协助")
    @PutMapping
    public Result<Void> update(@RequestBody Financing entity) {
        financingService.update(entity);
        return Result.success();
    }

    @OpLog(module = "融资协助", action = "DELETE", description = "删除融资协助")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        financingService.delete(id);
        return Result.success();
    }
}
