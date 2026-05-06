package com.trade.platform.module.customs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.customs.dto.CustomsQueryDTO;
import com.trade.platform.module.customs.entity.CustomsDeclaration;
import com.trade.platform.module.customs.entity.CustomsGoods;
import com.trade.platform.module.customs.entity.CustomsReview;
import com.trade.platform.module.customs.mapper.CustomsDeclarationMapper;
import com.trade.platform.module.customs.mapper.CustomsGoodsMapper;
import com.trade.platform.module.customs.mapper.CustomsReviewMapper;
import com.trade.platform.security.UserContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomsService {

    @Resource
    private CustomsDeclarationMapper declarationMapper;

    @Resource
    private CustomsGoodsMapper customsGoodsMapper;

    @Resource
    private CustomsReviewMapper customsReviewMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PageResult<CustomsDeclaration> getList(CustomsQueryDTO dto) {
        LambdaQueryWrapper<CustomsDeclaration> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getDeclarationNo())) {
            wrapper.like(CustomsDeclaration::getDeclarationNo, dto.getDeclarationNo());
        }
        if (StringUtils.hasText(dto.getIeType())) {
            wrapper.eq(CustomsDeclaration::getIeType, dto.getIeType());
        }
        if (StringUtils.hasText(dto.getStartDate())) {
            wrapper.ge(CustomsDeclaration::getIeDate, LocalDate.parse(dto.getStartDate()));
        }
        if (StringUtils.hasText(dto.getEndDate())) {
            wrapper.le(CustomsDeclaration::getIeDate, LocalDate.parse(dto.getEndDate()));
        }
        if (StringUtils.hasText(dto.getTransportMode())) {
            wrapper.eq(CustomsDeclaration::getTransportMode, dto.getTransportMode());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(CustomsDeclaration::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(CustomsDeclaration::getCreateTime);

        Page<CustomsDeclaration> page = new Page<>(dto.getCurrent(), dto.getSize());
        declarationMapper.selectPage(page, wrapper);

        PageResult<CustomsDeclaration> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    public Map<String, Object> getById(Long id) {
        CustomsDeclaration declaration = declarationMapper.selectById(id);
        if (declaration == null) {
            throw new BusinessException("报关单不存在");
        }
        LambdaQueryWrapper<CustomsGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomsGoods::getDeclarationId, id);
        List<CustomsGoods> goodsList = customsGoodsMapper.selectList(wrapper);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("declaration", declaration);
        resultMap.put("goods", goodsList);
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public void importDeclarations(List<CustomsDeclaration> declarations, List<List<CustomsGoods>> goodsList) {
        Long tenantId = UserContext.getTenantId();
        for (int i = 0; i < declarations.size(); i++) {
            CustomsDeclaration declaration = declarations.get(i);
            declaration.setTenantId(tenantId);
            declaration.setCreateTime(LocalDateTime.now());
            declarationMapper.insert(declaration);

            if (goodsList != null && i < goodsList.size()) {
                List<CustomsGoods> goods = goodsList.get(i);
                if (goods != null) {
                    for (CustomsGoods item : goods) {
                        item.setDeclarationId(declaration.getId());
                        customsGoodsMapper.insert(item);
                    }
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromExcel(MultipartFile file) {
        int success = 0, fail = 0;
        List<String> errors = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Long tenantId = UserContext.getTenantId();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    CustomsDeclaration decl = new CustomsDeclaration();
                    decl.setTenantId(tenantId);
                    decl.setDeclarationNo(getCellStringValue(row, 0));
                    decl.setIeType(getCellStringValue(row, 1));
                    String dateStr = getCellStringValue(row, 2);
                    if (dateStr != null && !dateStr.isEmpty()) {
                        decl.setIeDate(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    }
                    decl.setTransportMode(getCellStringValue(row, 3));
                    decl.setTradeMode(getCellStringValue(row, 4));
                    decl.setCustomsCode(getCellStringValue(row, 5));
                    decl.setConsignee(getCellStringValue(row, 6));
                    decl.setConsigner(getCellStringValue(row, 7));
                    decl.setStatus(getCellStringValue(row, 8));
                    String amountStr = getCellStringValue(row, 9);
                    if (amountStr != null && !amountStr.isEmpty()) {
                        decl.setTotalAmount(new BigDecimal(amountStr));
                    }
                    decl.setCurrency(getCellStringValue(row, 10));
                    decl.setCreateTime(LocalDateTime.now());

                    if (decl.getDeclarationNo() != null) {
                        Long count = declarationMapper.selectCount(
                                new LambdaQueryWrapper<CustomsDeclaration>()
                                        .eq(CustomsDeclaration::getDeclarationNo, decl.getDeclarationNo())
                                        .eq(CustomsDeclaration::getTenantId, tenantId));
                        if (count > 0) {
                            fail++;
                            errors.add("第" + (i + 1) + "行: 报关单号已存在");
                            continue;
                        }
                    }

                    declarationMapper.insert(decl);
                    success++;
                } catch (Exception e) {
                    fail++;
                    errors.add("第" + (i + 1) + "行: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new BusinessException("Excel文件解析失败: " + e.getMessage());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        result.put("errors", errors);
        return result;
    }

    private String getCellStringValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    // ==================== CRUD ====================

    @Transactional(rollbackFor = Exception.class)
    public void create(Map<String, Object> body) {
        CustomsDeclaration declaration = objectMapper.convertValue(body.get("declaration"), CustomsDeclaration.class);
        if (declaration == null) {
            throw new BusinessException("报关单数据不能为空");
        }
        declaration.setTenantId(UserContext.getTenantId());
        declaration.setReviewStatus("DRAFT");
        declaration.setDeleted(0);
        declaration.setCreateTime(LocalDateTime.now());
        declarationMapper.insert(declaration);

        Object goodsObj = body.get("goods");
        if (goodsObj != null) {
            List<?> rawList = objectMapper.convertValue(goodsObj, List.class);
            for (Object item : rawList) {
                CustomsGoods goods = objectMapper.convertValue(item, CustomsGoods.class);
                goods.setDeclarationId(declaration.getId());
                customsGoodsMapper.insert(goods);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Map<String, Object> body) {
        CustomsDeclaration declaration = objectMapper.convertValue(body.get("declaration"), CustomsDeclaration.class);
        if (declaration == null || declaration.getId() == null) {
            throw new BusinessException("报关单数据不能为空");
        }
        CustomsDeclaration existing = declarationMapper.selectById(declaration.getId());
        if (existing == null) {
            throw new BusinessException("报关单不存在");
        }
        String reviewStatus = existing.getReviewStatus();
        if (!"DRAFT".equals(reviewStatus) && !"REJECTED".equals(reviewStatus)) {
            throw new BusinessException("只有草稿或已驳回状态的报关单才能编辑");
        }
        declarationMapper.updateById(declaration);

        // 删除原有商品，重新插入
        customsGoodsMapper.delete(new LambdaQueryWrapper<CustomsGoods>()
                .eq(CustomsGoods::getDeclarationId, declaration.getId()));

        Object goodsObj = body.get("goods");
        if (goodsObj != null) {
            List<?> rawList = objectMapper.convertValue(goodsObj, List.class);
            for (Object item : rawList) {
                CustomsGoods goods = objectMapper.convertValue(item, CustomsGoods.class);
                goods.setId(null);
                goods.setDeclarationId(declaration.getId());
                customsGoodsMapper.insert(goods);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CustomsDeclaration declaration = declarationMapper.selectById(id);
        if (declaration == null) {
            throw new BusinessException("报关单不存在");
        }
        if (!"DRAFT".equals(declaration.getReviewStatus())) {
            throw new BusinessException("只有草稿状态的报关单才能删除");
        }
        declarationMapper.deleteById(id);
    }

    // ==================== Review Workflow ====================

    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        CustomsDeclaration declaration = declarationMapper.selectById(id);
        if (declaration == null) {
            throw new BusinessException("报关单不存在");
        }
        if (!"DRAFT".equals(declaration.getReviewStatus())) {
            throw new BusinessException("只有草稿状态的报关单才能提交审核");
        }
        String fromStatus = declaration.getReviewStatus();
        declaration.setReviewStatus("SUBMITTED");
        declaration.setSubmitTime(LocalDateTime.now());
        declaration.setSubmitBy(UserContext.getUserId());
        declarationMapper.updateById(declaration);

        saveReviewRecord(id, "SUBMIT", fromStatus, "SUBMITTED", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String comment) {
        CustomsDeclaration declaration = declarationMapper.selectById(id);
        if (declaration == null) {
            throw new BusinessException("报关单不存在");
        }
        String reviewStatus = declaration.getReviewStatus();
        if (!"SUBMITTED".equals(reviewStatus) && !"REVIEWING".equals(reviewStatus)) {
            throw new BusinessException("当前状态不允许审批通过");
        }
        declaration.setReviewStatus("APPROVED");
        declaration.setReviewTime(LocalDateTime.now());
        declaration.setReviewBy(UserContext.getUserId());
        declarationMapper.updateById(declaration);

        saveReviewRecord(id, "APPROVE", reviewStatus, "APPROVED", comment);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String comment) {
        CustomsDeclaration declaration = declarationMapper.selectById(id);
        if (declaration == null) {
            throw new BusinessException("报关单不存在");
        }
        String reviewStatus = declaration.getReviewStatus();
        if (!"SUBMITTED".equals(reviewStatus) && !"REVIEWING".equals(reviewStatus)) {
            throw new BusinessException("当前状态不允许驳回");
        }
        declaration.setReviewStatus("REJECTED");
        declaration.setReviewTime(LocalDateTime.now());
        declaration.setReviewBy(UserContext.getUserId());
        declarationMapper.updateById(declaration);

        saveReviewRecord(id, "REJECT", reviewStatus, "REJECTED", comment);
    }

    @Transactional(rollbackFor = Exception.class)
    public void release(Long id, String comment) {
        CustomsDeclaration declaration = declarationMapper.selectById(id);
        if (declaration == null) {
            throw new BusinessException("报关单不存在");
        }
        if (!"APPROVED".equals(declaration.getReviewStatus())) {
            throw new BusinessException("只有审批通过的报关单才能放行");
        }
        String fromStatus = declaration.getReviewStatus();
        declaration.setReviewStatus("RELEASED");
        declaration.setReviewTime(LocalDateTime.now());
        declaration.setReviewBy(UserContext.getUserId());
        declarationMapper.updateById(declaration);

        saveReviewRecord(id, "RELEASE", fromStatus, "RELEASED", comment);
    }

    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long id) {
        CustomsDeclaration declaration = declarationMapper.selectById(id);
        if (declaration == null) {
            throw new BusinessException("报关单不存在");
        }
        if (!"SUBMITTED".equals(declaration.getReviewStatus())) {
            throw new BusinessException("只有已提交状态的报关单才能撤回");
        }
        String fromStatus = declaration.getReviewStatus();
        declaration.setReviewStatus("DRAFT");
        declaration.setSubmitTime(null);
        declaration.setSubmitBy(null);
        declarationMapper.updateById(declaration);

        saveReviewRecord(id, "REVOKE", fromStatus, "DRAFT", null);
    }

    public List<CustomsReview> getReviews(Long id) {
        LambdaQueryWrapper<CustomsReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomsReview::getDeclarationId, id);
        wrapper.orderByDesc(CustomsReview::getCreateTime);
        return customsReviewMapper.selectList(wrapper);
    }

    private void saveReviewRecord(Long declarationId, String action, String fromStatus, String toStatus, String comment) {
        CustomsReview review = new CustomsReview();
        review.setDeclarationId(declarationId);
        review.setAction(action);
        review.setFromStatus(fromStatus);
        review.setToStatus(toStatus);
        review.setOperatorId(UserContext.getUserId());
        review.setOperatorName(UserContext.getUsername());
        review.setComment(comment);
        review.setCreateTime(LocalDateTime.now());
        customsReviewMapper.insert(review);
    }
}
