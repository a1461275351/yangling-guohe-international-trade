package com.trade.platform.module.order.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.order.dto.OrderCreateDTO;
import com.trade.platform.module.order.dto.OrderQueryDTO;
import com.trade.platform.module.order.entity.Order;
import com.trade.platform.module.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/list")
    public Result<PageResult<Order>> list(@RequestBody OrderQueryDTO dto) {
        return Result.success(orderService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    @PostMapping
    public Result<Order> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Order> update(@PathVariable Long id, @Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return Result.success();
    }
}
