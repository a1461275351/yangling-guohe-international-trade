package com.trade.platform.module.settlement.controller;

import com.trade.platform.common.OpLog;
import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.settlement.dto.SettlementQueryDTO;
import com.trade.platform.module.settlement.entity.Settlement;
import com.trade.platform.module.settlement.service.SettlementService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    @Resource
    private SettlementService settlementService;

    @PostMapping("/list")
    public Result<PageResult<Settlement>> list(@RequestBody SettlementQueryDTO dto) {
        return Result.success(settlementService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Settlement> getById(@PathVariable Long id) {
        return Result.success(settlementService.getById(id));
    }

    @OpLog(module = "结算收汇", action = "CREATE", description = "新增结算收汇")
    @PostMapping
    public Result<Void> create(@RequestBody Settlement entity) {
        settlementService.create(entity);
        return Result.success();
    }

    @OpLog(module = "结算收汇", action = "UPDATE", description = "编辑结算收汇")
    @PutMapping
    public Result<Void> update(@RequestBody Settlement entity) {
        settlementService.update(entity);
        return Result.success();
    }

    @OpLog(module = "结算收汇", action = "DELETE", description = "删除结算收汇")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        settlementService.delete(id);
        return Result.success();
    }
}
