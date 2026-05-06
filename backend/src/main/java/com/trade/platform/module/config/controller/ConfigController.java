package com.trade.platform.module.config.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.security.RequireRole;
import com.trade.platform.common.Result;
import com.trade.platform.module.config.dto.ConfigItemQueryDTO;
import com.trade.platform.module.config.dto.ConfigValueQueryDTO;
import com.trade.platform.module.config.entity.ConfigItem;
import com.trade.platform.module.config.entity.ConfigValue;
import com.trade.platform.module.config.service.ConfigService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequireRole("ADMIN")
public class ConfigController {

    @Resource
    private ConfigService configService;

    @PostMapping("/items/list")
    public Result<PageResult<ConfigItem>> getItemList(@RequestBody ConfigItemQueryDTO dto) {
        return Result.success(configService.getItemList(dto));
    }

    @PostMapping("/items")
    public Result<Void> createItem(@RequestBody ConfigItem item) {
        configService.createItem(item);
        return Result.success();
    }

    @PutMapping("/items")
    public Result<Void> updateItem(@RequestBody ConfigItem item) {
        configService.updateItem(item);
        return Result.success();
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        configService.deleteItem(id);
        return Result.success();
    }

    @PutMapping("/items/{id}/status")
    public Result<Void> updateItemStatus(@PathVariable Long id, @RequestParam Integer status) {
        configService.updateItemStatus(id, status);
        return Result.success();
    }

    @PostMapping("/values/list")
    public Result<PageResult<ConfigValue>> getValueList(@RequestBody ConfigValueQueryDTO dto) {
        return Result.success(configService.getValueList(dto));
    }

    @PostMapping("/values")
    public Result<Void> createValue(@RequestBody ConfigValue value) {
        configService.createValue(value);
        return Result.success();
    }

    @PutMapping("/values")
    public Result<Void> updateValue(@RequestBody ConfigValue value) {
        configService.updateValue(value);
        return Result.success();
    }

    @DeleteMapping("/values/{id}")
    public Result<Void> deleteValue(@PathVariable Long id) {
        configService.deleteValue(id);
        return Result.success();
    }

    @PutMapping("/values/{id}/status")
    public Result<Void> updateValueStatus(@PathVariable Long id, @RequestParam Integer status) {
        configService.updateValueStatus(id, status);
        return Result.success();
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/values/batch-status")
    public Result<Void> batchUpdateValueStatus(@RequestBody Map<String, Object> body) {
        List<Long> ids = (List<Long>) body.get("ids");
        Integer status = (Integer) body.get("status");
        configService.batchUpdateValueStatus(ids, status);
        return Result.success();
    }

    @GetMapping("/values/by-item-code")
    public Result<List<ConfigValue>> getActiveValuesByItemCode(@RequestParam String code) {
        return Result.success(configService.getActiveValuesByItemCode(code));
    }
}
