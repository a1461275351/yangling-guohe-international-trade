package com.trade.platform.module.ledger.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.contract.entity.Contract;
import com.trade.platform.module.contract.mapper.ContractMapper;
import com.trade.platform.module.customs.entity.CustomsDeclaration;
import com.trade.platform.module.customs.entity.CustomsGoods;
import com.trade.platform.module.customs.mapper.CustomsDeclarationMapper;
import com.trade.platform.module.customs.mapper.CustomsGoodsMapper;
import com.trade.platform.module.ledger.dto.LedgerQueryDTO;
import com.trade.platform.module.ledger.dto.LedgerSaveDTO;
import com.trade.platform.module.ledger.entity.ImportLedger;
import com.trade.platform.module.ledger.entity.ImportLedgerFile;
import com.trade.platform.module.ledger.entity.ImportLedgerGoods;
import com.trade.platform.module.ledger.mapper.ImportLedgerFileMapper;
import com.trade.platform.module.ledger.mapper.ImportLedgerGoodsMapper;
import com.trade.platform.module.ledger.mapper.ImportLedgerMapper;
import com.trade.platform.module.order.entity.Order;
import com.trade.platform.module.order.entity.OrderGoods;
import com.trade.platform.module.order.mapper.OrderGoodsMapper;
import com.trade.platform.module.order.mapper.OrderMapper;
import com.trade.platform.security.UserContext;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class LedgerService {

    @Resource
    private ImportLedgerMapper ledgerMapper;

    @Resource
    private ImportLedgerGoodsMapper ledgerGoodsMapper;

    @Resource
    private ImportLedgerFileMapper ledgerFileMapper;

    @Resource
    private ContractMapper ledgerContractMapper;

    @Resource
    private OrderMapper ledgerOrderMapper;

    @Resource
    private OrderGoodsMapper ledgerOrderGoodsMapper;

    @Resource
    private CustomsDeclarationMapper ledgerDeclMapper;

    @Resource
    private CustomsGoodsMapper ledgerCustGoodsMapper;

    /**
     * Paginated list with filters
     */
    public PageResult<ImportLedger> getList(LedgerQueryDTO dto) {
        LambdaQueryWrapper<ImportLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImportLedger::getTenantId, UserContext.getTenantId());
        if (StringUtils.hasText(dto.getLedgerNo())) {
            wrapper.like(ImportLedger::getLedgerNo, dto.getLedgerNo());
        }
        if (StringUtils.hasText(dto.getSupplierName())) {
            wrapper.like(ImportLedger::getSupplierName, dto.getSupplierName());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(ImportLedger::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getContractNo())) {
            wrapper.like(ImportLedger::getContractNo, dto.getContractNo());
        }
        if (StringUtils.hasText(dto.getStartDate()) && StringUtils.hasText(dto.getEndDate())) {
            wrapper.between(ImportLedger::getCreateTime,
                    LocalDate.parse(dto.getStartDate()).atStartOfDay(),
                    LocalDate.parse(dto.getEndDate()).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(ImportLedger::getCreateTime);

        Page<ImportLedger> page = new Page<>(dto.getCurrent(), dto.getSize());
        ledgerMapper.selectPage(page, wrapper);
        return PageResult.from(page);
    }

    /**
     * Get ledger detail with goods and files
     */
    public Map<String, Object> getById(Long id) {
        ImportLedger ledger = ledgerMapper.selectById(id);
        if (ledger == null) {
            throw new BusinessException("台账不存在");
        }
        List<ImportLedgerGoods> goodsList = ledgerGoodsMapper.selectList(
                new LambdaQueryWrapper<ImportLedgerGoods>().eq(ImportLedgerGoods::getLedgerId, id));
        List<ImportLedgerFile> fileList = ledgerFileMapper.selectList(
                new LambdaQueryWrapper<ImportLedgerFile>().eq(ImportLedgerFile::getLedgerId, id));

        ledger.setGoodsList(goodsList);

        Map<String, Object> result = new HashMap<>();
        result.put("ledger", ledger);
        result.put("goodsList", goodsList);
        result.put("fileList", fileList);
        return result;
    }

    /**
     * Create ledger with goods
     */
    @Transactional
    public ImportLedger create(LedgerSaveDTO dto) {
        ImportLedger ledger = buildLedgerFromDTO(dto);
        ledger.setTenantId(UserContext.getTenantId());
        ledger.setCreateBy(UserContext.getUserId());
        ledger.setLedgerNo(generateLedgerNo());
        ledger.setStatus("DRAFT");
        if (ledger.getSplitStatus() == null) {
            ledger.setSplitStatus("UNSPLIT");
        }
        if (ledger.getCurrency() == null) {
            ledger.setCurrency("USD");
        }

        calculateAndSetTotals(ledger, dto.getGoodsList());

        ledgerMapper.insert(ledger);

        saveGoodsList(ledger.getId(), dto.getGoodsList());

        return ledger;
    }

    /**
     * Update ledger (DRAFT only), delete old goods and insert new
     */
    @Transactional
    public ImportLedger update(LedgerSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("台账ID不能为空");
        }
        ImportLedger existing = ledgerMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("台账不存在");
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new BusinessException("只有草稿状态的台账才能修改");
        }

        ImportLedger ledger = buildLedgerFromDTO(dto);
        ledger.setId(dto.getId());
        calculateAndSetTotals(ledger, dto.getGoodsList());

        ledgerMapper.updateById(ledger);

        // Delete old goods and insert new
        ledgerGoodsMapper.delete(new LambdaQueryWrapper<ImportLedgerGoods>()
                .eq(ImportLedgerGoods::getLedgerId, dto.getId()));
        saveGoodsList(dto.getId(), dto.getGoodsList());

        return ledger;
    }

    /**
     * Delete ledger (DRAFT only)
     */
    @Transactional
    public void delete(Long id) {
        ImportLedger existing = ledgerMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("台账不存在");
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new BusinessException("只有草稿状态的台账才能删除");
        }
        ledgerGoodsMapper.delete(new LambdaQueryWrapper<ImportLedgerGoods>()
                .eq(ImportLedgerGoods::getLedgerId, id));
        ledgerFileMapper.delete(new LambdaQueryWrapper<ImportLedgerFile>()
                .eq(ImportLedgerFile::getLedgerId, id));
        ledgerMapper.deleteById(id);
    }

    /**
     * Deep copy ledger with new ledgerNo
     */
    @Transactional
    public ImportLedger copy(Long id) {
        ImportLedger source = ledgerMapper.selectById(id);
        if (source == null) {
            throw new BusinessException("台账不存在");
        }

        ImportLedger copy = new ImportLedger();
        copy.setTenantId(source.getTenantId());
        copy.setLedgerNo(generateLedgerNo());
        copy.setSplitStatus(source.getSplitStatus());
        copy.setCaseNo(source.getCaseNo());
        copy.setMasterBlNo(source.getMasterBlNo());
        copy.setSubBlNo(source.getSubBlNo());
        copy.setSupplierId(source.getSupplierId());
        copy.setSupplierName(source.getSupplierName());
        copy.setSupervisionMode(source.getSupervisionMode());
        copy.setContractNo(source.getContractNo());
        copy.setContractId(source.getContractId());
        copy.setDeclareCustoms(source.getDeclareCustoms());
        copy.setEntryCustoms(source.getEntryCustoms());
        copy.setOriginCountry(source.getOriginCountry());
        copy.setTransitPort(source.getTransitPort());
        copy.setEntryPort(source.getEntryPort());
        copy.setTransportMode(source.getTransportMode());
        copy.setTradeMode(source.getTradeMode());
        copy.setTotalAmount(source.getTotalAmount());
        copy.setCurrency(source.getCurrency());
        copy.setIeDate(source.getIeDate());
        copy.setConsignee(source.getConsignee());
        copy.setOrderId(source.getOrderId());
        copy.setStatus("DRAFT");
        copy.setRemark(source.getRemark());
        copy.setCreateBy(UserContext.getUserId());
        copy.setDocCount(source.getDocCount());
        copy.setGoodsCount(source.getGoodsCount());

        ledgerMapper.insert(copy);

        // Copy goods
        List<ImportLedgerGoods> goodsList = ledgerGoodsMapper.selectList(
                new LambdaQueryWrapper<ImportLedgerGoods>().eq(ImportLedgerGoods::getLedgerId, id));
        for (ImportLedgerGoods goods : goodsList) {
            goods.setId(null);
            goods.setLedgerId(copy.getId());
            goods.setAssignedQty(BigDecimal.ZERO);
            ledgerGoodsMapper.insert(goods);
        }

        return copy;
    }

    /**
     * Generate ledger from contract
     */
    @Transactional
    public ImportLedger generateFromContract(Long contractId) {
        Contract contract = ledgerContractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        ImportLedger ledger = new ImportLedger();
        ledger.setTenantId(UserContext.getTenantId());
        ledger.setCreateBy(UserContext.getUserId());
        ledger.setLedgerNo(generateLedgerNo());
        ledger.setContractId(contractId);
        ledger.setContractNo(contract.getContractNo());
        ledger.setSupplierId(contract.getPartnerId());
        ledger.setSupplierName(contract.getPartnerName());
        ledger.setTotalAmount(contract.getAmount());
        ledger.setCurrency(contract.getCurrency() != null ? contract.getCurrency() : "USD");
        ledger.setStatus("DRAFT");
        ledger.setSplitStatus("UNSPLIT");
        ledger.setGoodsCount(0);
        ledger.setDocCount(0);

        ledgerMapper.insert(ledger);
        return ledger;
    }

    /**
     * Generate ledger from order (with goods)
     */
    @Transactional
    public ImportLedger generateFromOrder(Long orderId) {
        Order order = ledgerOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        ImportLedger ledger = new ImportLedger();
        ledger.setTenantId(UserContext.getTenantId());
        ledger.setCreateBy(UserContext.getUserId());
        ledger.setLedgerNo(generateLedgerNo());
        ledger.setOrderId(orderId);
        ledger.setContractId(order.getContractId());
        ledger.setTotalAmount(order.getTotalAmount());
        ledger.setCurrency(order.getCurrency() != null ? order.getCurrency() : "USD");
        ledger.setStatus("DRAFT");
        ledger.setSplitStatus("UNSPLIT");

        // Copy order goods to ledger goods
        List<OrderGoods> orderGoodsList = ledgerOrderGoodsMapper.selectList(
                new LambdaQueryWrapper<OrderGoods>().eq(OrderGoods::getOrderId, orderId));

        ledger.setGoodsCount(orderGoodsList.size());
        ledger.setDocCount(0);

        ledgerMapper.insert(ledger);

        int seq = 1;
        for (OrderGoods og : orderGoodsList) {
            ImportLedgerGoods lg = new ImportLedgerGoods();
            lg.setLedgerId(ledger.getId());
            lg.setGoodsNo(String.valueOf(seq++));
            lg.setGoodsId(og.getGoodsId());
            lg.setName(og.getGoodsName());
            lg.setHsCode(og.getHsCode());
            lg.setQuantity(og.getQuantity());
            lg.setUnit(og.getUnit());
            lg.setPrice(og.getPrice());
            lg.setAmount(og.getAmount());
            lg.setAssignedQty(BigDecimal.ZERO);
            ledgerGoodsMapper.insert(lg);
        }

        return ledger;
    }

    /**
     * Import ledgers from Excel
     */
    @Transactional
    public List<ImportLedger> importFromExcel(MultipartFile file) {
        List<ImportLedger> result = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("Excel文件为空");
            }

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                ImportLedger ledger = new ImportLedger();
                ledger.setTenantId(UserContext.getTenantId());
                ledger.setCreateBy(UserContext.getUserId());
                ledger.setLedgerNo(generateLedgerNo());
                ledger.setStatus("DRAFT");
                ledger.setSplitStatus("UNSPLIT");
                ledger.setCurrency("USD");
                ledger.setGoodsCount(0);
                ledger.setDocCount(0);

                ledger.setCaseNo(getCellStringValue(row, 0));
                ledger.setMasterBlNo(getCellStringValue(row, 1));
                ledger.setSubBlNo(getCellStringValue(row, 2));
                ledger.setSupplierName(getCellStringValue(row, 3));
                ledger.setContractNo(getCellStringValue(row, 4));
                ledger.setOriginCountry(getCellStringValue(row, 5));
                ledger.setTransportMode(getCellStringValue(row, 6));
                ledger.setTradeMode(getCellStringValue(row, 7));
                ledger.setConsignee(getCellStringValue(row, 8));
                ledger.setRemark(getCellStringValue(row, 9));

                String amountStr = getCellStringValue(row, 10);
                if (StringUtils.hasText(amountStr)) {
                    try {
                        ledger.setTotalAmount(new BigDecimal(amountStr));
                    } catch (NumberFormatException ignored) {
                    }
                }

                ledgerMapper.insert(ledger);
                result.add(ledger);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Excel解析失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * Generate customs declaration from ledger
     */
    @Transactional
    public CustomsDeclaration generateDeclaration(Long ledgerId) {
        ImportLedger ledger = ledgerMapper.selectById(ledgerId);
        if (ledger == null) {
            throw new BusinessException("台账不存在");
        }

        CustomsDeclaration declaration = new CustomsDeclaration();
        declaration.setTenantId(ledger.getTenantId());
        declaration.setIeType("I");
        declaration.setIeDate(ledger.getIeDate());
        declaration.setTransportMode(ledger.getTransportMode());
        declaration.setTradeMode(ledger.getTradeMode());
        declaration.setCustomsCode(ledger.getDeclareCustoms());
        declaration.setConsignee(ledger.getConsignee());
        declaration.setTotalAmount(ledger.getTotalAmount());
        declaration.setCurrency(ledger.getCurrency());
        declaration.setStatus("DRAFT");
        declaration.setCreateTime(LocalDateTime.now());

        ledgerDeclMapper.insert(declaration);

        // Copy ledger goods to customs goods
        List<ImportLedgerGoods> goodsList = ledgerGoodsMapper.selectList(
                new LambdaQueryWrapper<ImportLedgerGoods>().eq(ImportLedgerGoods::getLedgerId, ledgerId));
        for (ImportLedgerGoods lg : goodsList) {
            CustomsGoods cg = new CustomsGoods();
            cg.setDeclarationId(declaration.getId());
            cg.setGoodsNo(lg.getGoodsNo());
            cg.setName(lg.getName());
            cg.setHsCode(lg.getHsCode());
            cg.setSpec(lg.getSpec());
            cg.setQuantity(lg.getQuantity());
            cg.setUnit(lg.getUnit());
            cg.setPrice(lg.getPrice());
            cg.setAmount(lg.getAmount());
            cg.setOriginCountry(lg.getOriginCountry());
            ledgerCustGoodsMapper.insert(cg);
        }

        return declaration;
    }

    /**
     * Associate files to ledger
     */
    @Transactional
    public void associateFiles(Long ledgerId, List<Long> fileIds) {
        ImportLedger ledger = ledgerMapper.selectById(ledgerId);
        if (ledger == null) {
            throw new BusinessException("台账不存在");
        }

        for (Long fileId : fileIds) {
            // Check if already associated
            long count = ledgerFileMapper.selectCount(new LambdaQueryWrapper<ImportLedgerFile>()
                    .eq(ImportLedgerFile::getLedgerId, ledgerId)
                    .eq(ImportLedgerFile::getFileId, fileId));
            if (count == 0) {
                ImportLedgerFile lf = new ImportLedgerFile();
                lf.setLedgerId(ledgerId);
                lf.setFileId(fileId);
                lf.setCreateTime(LocalDateTime.now());
                ledgerFileMapper.insert(lf);
            }
        }

        // Update doc count
        long docCount = ledgerFileMapper.selectCount(
                new LambdaQueryWrapper<ImportLedgerFile>().eq(ImportLedgerFile::getLedgerId, ledgerId));
        ImportLedger update = new ImportLedger();
        update.setId(ledgerId);
        update.setDocCount((int) docCount);
        ledgerMapper.updateById(update);
    }

    /**
     * Get declarations related to a ledger (by matching consignee and ieDate)
     */
    public List<CustomsDeclaration> getRelatedDeclarations(Long ledgerId) {
        ImportLedger ledger = ledgerMapper.selectById(ledgerId);
        if (ledger == null) {
            throw new BusinessException("台账不存在");
        }
        LambdaQueryWrapper<CustomsDeclaration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomsDeclaration::getTenantId, ledger.getTenantId());
        wrapper.eq(CustomsDeclaration::getConsignee, ledger.getConsignee());
        if (ledger.getIeDate() != null) {
            wrapper.eq(CustomsDeclaration::getIeDate, ledger.getIeDate());
        }
        wrapper.orderByDesc(CustomsDeclaration::getCreateTime);
        return ledgerDeclMapper.selectList(wrapper);
    }

    // ===================== Private helpers =====================

    private String generateLedgerNo() {
        String prefix = "TZ";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = ledgerMapper.selectCount(new LambdaQueryWrapper<ImportLedger>()
                .likeRight(ImportLedger::getLedgerNo, prefix + date));
        return prefix + date + String.format("%04d", count + 1);
    }

    private ImportLedger buildLedgerFromDTO(LedgerSaveDTO dto) {
        ImportLedger ledger = new ImportLedger();
        ledger.setSplitStatus(dto.getSplitStatus());
        ledger.setCaseNo(dto.getCaseNo());
        ledger.setMasterBlNo(dto.getMasterBlNo());
        ledger.setSubBlNo(dto.getSubBlNo());
        ledger.setSupplierId(dto.getSupplierId());
        ledger.setSupplierName(dto.getSupplierName());
        ledger.setSupervisionMode(dto.getSupervisionMode());
        ledger.setContractNo(dto.getContractNo());
        ledger.setContractId(dto.getContractId());
        ledger.setDeclareCustoms(dto.getDeclareCustoms());
        ledger.setEntryCustoms(dto.getEntryCustoms());
        ledger.setOriginCountry(dto.getOriginCountry());
        ledger.setTransitPort(dto.getTransitPort());
        ledger.setEntryPort(dto.getEntryPort());
        ledger.setTransportMode(dto.getTransportMode());
        ledger.setTradeMode(dto.getTradeMode());
        ledger.setCurrency(dto.getCurrency());
        ledger.setIeDate(dto.getIeDate());
        ledger.setConsignee(dto.getConsignee());
        ledger.setOrderId(dto.getOrderId());
        ledger.setRemark(dto.getRemark());
        return ledger;
    }

    private void calculateAndSetTotals(ImportLedger ledger, List<ImportLedgerGoods> goodsList) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int goodsCount = 0;
        if (goodsList != null) {
            goodsCount = goodsList.size();
            for (ImportLedgerGoods goods : goodsList) {
                if (goods.getAmount() == null && goods.getPrice() != null && goods.getQuantity() != null) {
                    goods.setAmount(goods.getPrice().multiply(goods.getQuantity()));
                }
                if (goods.getAmount() != null) {
                    totalAmount = totalAmount.add(goods.getAmount());
                }
            }
        }
        ledger.setTotalAmount(totalAmount);
        ledger.setGoodsCount(goodsCount);
        if (ledger.getDocCount() == null) {
            ledger.setDocCount(0);
        }
    }

    private void saveGoodsList(Long ledgerId, List<ImportLedgerGoods> goodsList) {
        if (goodsList == null) return;
        int seq = 1;
        for (ImportLedgerGoods goods : goodsList) {
            goods.setId(null);
            goods.setLedgerId(ledgerId);
            if (!StringUtils.hasText(goods.getGoodsNo())) {
                goods.setGoodsNo(String.valueOf(seq));
            }
            if (goods.getAssignedQty() == null) {
                goods.setAssignedQty(BigDecimal.ZERO);
            }
            ledgerGoodsMapper.insert(goods);
            seq++;
        }
    }

    private String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue();
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
