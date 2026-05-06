package com.trade.platform.module.partner.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.partner.dto.PartnerQueryDTO;
import com.trade.platform.module.partner.entity.Partner;
import com.trade.platform.module.partner.service.PartnerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    @Resource
    private PartnerService partnerService;

    @PostMapping("/list")
    public Result<PageResult<Partner>> getList(@RequestBody PartnerQueryDTO dto) {
        return Result.success(partnerService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Partner> getById(@PathVariable Long id) {
        return Result.success(partnerService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Partner partner) {
        partnerService.create(partner);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Partner partner) {
        partnerService.update(partner);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        partnerService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        partnerService.updateStatus(id, status);
        return Result.success();
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/batch-status")
    public Result<Void> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        List<Long> ids = (List<Long>) body.get("ids");
        Integer status = (Integer) body.get("status");
        partnerService.batchUpdateStatus(ids, status);
        return Result.success();
    }

    @GetMapping("/active")
    public Result<List<Partner>> getActiveByType(@RequestParam String type) {
        return Result.success(partnerService.getActiveByType(type));
    }
}
