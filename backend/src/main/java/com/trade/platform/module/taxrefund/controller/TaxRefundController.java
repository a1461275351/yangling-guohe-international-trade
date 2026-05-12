package com.trade.platform.module.taxrefund.controller;

import com.trade.platform.common.OpLog;
import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.taxrefund.dto.TaxRefundQueryDTO;
import com.trade.platform.module.taxrefund.entity.TaxRefund;
import com.trade.platform.module.taxrefund.service.TaxRefundService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/tax-refunds")
public class TaxRefundController {

    @Resource
    private TaxRefundService taxRefundService;

    @PostMapping("/list")
    public Result<PageResult<TaxRefund>> list(@RequestBody TaxRefundQueryDTO dto) {
        return Result.success(taxRefundService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<TaxRefund> getById(@PathVariable Long id) {
        return Result.success(taxRefundService.getById(id));
    }

    @OpLog(module = "退税业务", action = "CREATE", description = "新增退税业务")
    @PostMapping
    public Result<Void> create(@RequestBody TaxRefund entity) {
        taxRefundService.create(entity);
        return Result.success();
    }

    @OpLog(module = "退税业务", action = "UPDATE", description = "编辑退税业务")
    @PutMapping
    public Result<Void> update(@RequestBody TaxRefund entity) {
        taxRefundService.update(entity);
        return Result.success();
    }

    @OpLog(module = "退税业务", action = "DELETE", description = "删除退税业务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taxRefundService.delete(id);
        return Result.success();
    }

    @OpLog(module = "退税业务", action = "UPDATE", description = "变更退税业务状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        taxRefundService.updateStatus(id, status);
        return Result.success();
    }
}
