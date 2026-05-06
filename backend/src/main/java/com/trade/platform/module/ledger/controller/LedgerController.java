package com.trade.platform.module.ledger.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.customs.entity.CustomsDeclaration;
import com.trade.platform.module.ledger.dto.LedgerQueryDTO;
import com.trade.platform.module.ledger.dto.LedgerSaveDTO;
import com.trade.platform.module.ledger.entity.ImportLedger;
import com.trade.platform.module.ledger.service.LedgerService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    @Resource
    private LedgerService ledgerService;

    @PostMapping("/list")
    public Result<PageResult<ImportLedger>> list(@RequestBody LedgerQueryDTO dto) {
        return Result.success(ledgerService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.success(ledgerService.getById(id));
    }

    @PostMapping
    public Result<ImportLedger> create(@RequestBody LedgerSaveDTO dto) {
        return Result.success(ledgerService.create(dto));
    }

    @PutMapping
    public Result<ImportLedger> update(@RequestBody LedgerSaveDTO dto) {
        return Result.success(ledgerService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ledgerService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/copy")
    public Result<ImportLedger> copy(@PathVariable Long id) {
        return Result.success(ledgerService.copy(id));
    }

    @PostMapping("/generate-from-contract/{contractId}")
    public Result<ImportLedger> generateFromContract(@PathVariable Long contractId) {
        return Result.success(ledgerService.generateFromContract(contractId));
    }

    @PostMapping("/generate-from-order/{orderId}")
    public Result<ImportLedger> generateFromOrder(@PathVariable Long orderId) {
        return Result.success(ledgerService.generateFromOrder(orderId));
    }

    @PostMapping("/import")
    public Result<List<ImportLedger>> importFromExcel(@RequestParam("file") MultipartFile file) {
        return Result.success(ledgerService.importFromExcel(file));
    }

    @PostMapping("/{id}/generate-declaration")
    public Result<CustomsDeclaration> generateDeclaration(@PathVariable Long id) {
        return Result.success(ledgerService.generateDeclaration(id));
    }

    @PostMapping("/{id}/files")
    public Result<Void> associateFiles(@PathVariable Long id, @RequestBody List<Long> fileIds) {
        ledgerService.associateFiles(id, fileIds);
        return Result.success();
    }

    @GetMapping("/{id}/declarations")
    public Result<List<CustomsDeclaration>> getRelatedDeclarations(@PathVariable Long id) {
        return Result.success(ledgerService.getRelatedDeclarations(id));
    }

    // ==================== 单据生成 ====================

    @Resource
    private com.trade.platform.module.ledger.service.DocumentGenerateService documentGenerateService;

    /**
     * 生成箱单 PackingList
     */
    @GetMapping("/{id}/doc/packing-list")
    public void generatePackingList(@PathVariable Long id, javax.servlet.http.HttpServletResponse response) throws Exception {
        documentGenerateService.generatePackingList(id, response);
    }

    /**
     * 生成发票 Invoice
     */
    @GetMapping("/{id}/doc/invoice")
    public void generateInvoice(@PathVariable Long id, javax.servlet.http.HttpServletResponse response) throws Exception {
        documentGenerateService.generateInvoice(id, response);
    }

    /**
     * 生成合同 Contract
     */
    @GetMapping("/{id}/doc/contract")
    public void generateContract(@PathVariable Long id, javax.servlet.http.HttpServletResponse response) throws Exception {
        documentGenerateService.generateContract(id, response);
    }
}
