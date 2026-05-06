package com.trade.platform.module.customs.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.customs.dto.CustomsQueryDTO;
import com.trade.platform.module.customs.dto.ReviewActionDTO;
import com.trade.platform.module.customs.entity.CustomsDeclaration;
import com.trade.platform.module.customs.entity.CustomsReview;
import com.trade.platform.module.customs.service.CustomsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customs")
public class CustomsController {

    @Resource
    private CustomsService customsService;

    @PostMapping("/list")
    public Result<PageResult<CustomsDeclaration>> list(@RequestBody CustomsQueryDTO dto) {
        return Result.success(customsService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.success(customsService.getById(id));
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        return Result.success(customsService.importFromExcel(file));
    }

    // ==================== CRUD ====================

    @PostMapping
    public Result<Void> create(@RequestBody Map<String, Object> body) {
        customsService.create(body);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Map<String, Object> body) {
        customsService.update(body);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customsService.delete(id);
        return Result.success();
    }

    // ==================== Review Workflow ====================

    @PutMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        customsService.submit(id);
        return Result.success();
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody ReviewActionDTO dto) {
        customsService.approve(id, dto.getComment());
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody ReviewActionDTO dto) {
        customsService.reject(id, dto.getComment());
        return Result.success();
    }

    @PutMapping("/{id}/release")
    public Result<Void> release(@PathVariable Long id, @RequestBody ReviewActionDTO dto) {
        customsService.release(id, dto.getComment());
        return Result.success();
    }

    @PutMapping("/{id}/revoke")
    public Result<Void> revoke(@PathVariable Long id) {
        customsService.revoke(id);
        return Result.success();
    }

    @GetMapping("/{id}/reviews")
    public Result<List<CustomsReview>> getReviews(@PathVariable Long id) {
        return Result.success(customsService.getReviews(id));
    }
}
