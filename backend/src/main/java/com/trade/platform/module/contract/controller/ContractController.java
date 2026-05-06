package com.trade.platform.module.contract.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.contract.dto.ContractQueryDTO;
import com.trade.platform.module.contract.entity.Contract;
import com.trade.platform.module.contract.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @PostMapping("/list")
    public Result<PageResult<Contract>> list(@RequestBody ContractQueryDTO dto) {
        return Result.success(contractService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Contract> getById(@PathVariable Long id) {
        return Result.success(contractService.getById(id));
    }

    @PostMapping
    public Result<Contract> create(@RequestBody Contract contract) {
        return Result.success(contractService.create(contract));
    }

    @PutMapping
    public Result<Contract> update(@RequestBody Contract contract) {
        return Result.success(contractService.update(contract));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        contractService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return Result.success();
    }

    @GetMapping("/expiring")
    public Result<List<Contract>> getExpiringContracts(@RequestParam(defaultValue = "30") int days) {
        return Result.success(contractService.getExpiringContracts(days));
    }

    @GetMapping("/expiring/stats")
    public Result<Map<String, Object>> getExpiringStats() {
        return Result.success(contractService.getExpiringStats());
    }
}
