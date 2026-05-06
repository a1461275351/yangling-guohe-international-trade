package com.trade.platform.module.goods.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.goods.dto.GoodsQueryDTO;
import com.trade.platform.module.goods.entity.Goods;
import com.trade.platform.module.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping("/list")
    public Result<PageResult<Goods>> list(@RequestBody GoodsQueryDTO dto) {
        return Result.success(goodsService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Goods> getById(@PathVariable Long id) {
        return Result.success(goodsService.getById(id));
    }

    @PostMapping
    public Result<Goods> create(@RequestBody Goods goods) {
        return Result.success(goodsService.create(goods));
    }

    @PutMapping
    public Result<Goods> update(@RequestBody Goods goods) {
        return Result.success(goodsService.update(goods));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        goodsService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        goodsService.batchDelete(ids);
        return Result.success();
    }

    @GetMapping("/selection")
    public Result<List<Goods>> getGoodsForSelection() {
        return Result.success(goodsService.getGoodsForSelection());
    }
}
