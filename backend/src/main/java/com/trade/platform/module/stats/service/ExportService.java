package com.trade.platform.module.stats.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trade.platform.module.contract.entity.Contract;
import com.trade.platform.module.contract.mapper.ContractMapper;
import com.trade.platform.module.customs.entity.CustomsDeclaration;
import com.trade.platform.module.customs.mapper.CustomsDeclarationMapper;
import com.trade.platform.module.enterprise.entity.ServiceEnterprise;
import com.trade.platform.module.enterprise.mapper.ServiceEnterpriseMapper;
import com.trade.platform.module.logistics.mapper.LogisticsMapper;
import com.trade.platform.module.taxrefund.mapper.TaxRefundMapper;
import com.trade.platform.module.settlement.mapper.SettlementMapper;
import com.trade.platform.module.insurance.mapper.InsuranceMapper;
import com.trade.platform.module.financing.mapper.FinancingMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    @Resource
    private ServiceEnterpriseMapper enterpriseMapper;
    @Resource
    private ContractMapper contractMapper;
    @Resource
    private CustomsDeclarationMapper declarationMapper;
    @Resource
    private LogisticsMapper logisticsMapper;
    @Resource
    private TaxRefundMapper taxRefundMapper;
    @Resource
    private SettlementMapper settlementMapper;
    @Resource
    private InsuranceMapper insuranceMapper;
    @Resource
    private FinancingMapper financingMapper;

    public void exportEnterpriseList(HttpServletResponse response) throws IOException {
        List<ServiceEnterprise> list = enterpriseMapper.selectList(
                new LambdaQueryWrapper<ServiceEnterprise>().eq(ServiceEnterprise::getStatus, 1)
                        .orderByAsc(ServiceEnterprise::getCreateTime));

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("服务企业清单");
        CellStyle headerStyle = createHeaderStyle(wb);

        String[] headers = {"序号", "企业名称", "统一社会信用代码", "所在区域", "行业", "主营产品",
                "协议状态", "协议编号", "服务起始日", "服务截止日", "服务范围",
                "年度出口额(万元)", "年度进口额(万元)", "风险等级"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < list.size(); i++) {
            ServiceEnterprise e = list.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(str(e.getEnterpriseName()));
            row.createCell(2).setCellValue(str(e.getCreditCode()));
            row.createCell(3).setCellValue(str(e.getRegion()));
            row.createCell(4).setCellValue(str(e.getIndustry()));
            row.createCell(5).setCellValue(str(e.getProductType()));
            row.createCell(6).setCellValue("SIGNED".equals(e.getAgreementStatus()) ? "已签署" : "未签署");
            row.createCell(7).setCellValue(str(e.getAgreementNo()));
            row.createCell(8).setCellValue(e.getServiceStartDate() != null ? e.getServiceStartDate().format(fmt) : "");
            row.createCell(9).setCellValue(e.getServiceEndDate() != null ? e.getServiceEndDate().format(fmt) : "");
            row.createCell(10).setCellValue(str(e.getServiceScope()));
            row.createCell(11).setCellValue(dec(e.getAnnualExportAmount()));
            row.createCell(12).setCellValue(dec(e.getAnnualImportAmount()));
            row.createCell(13).setCellValue(str(e.getRiskLevel()));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        writeResponse(response, wb, "服务企业清单");
    }

    public void exportTradeStats(HttpServletResponse response) throws IOException {
        List<ServiceEnterprise> enterprises = enterpriseMapper.selectList(
                new LambdaQueryWrapper<ServiceEnterprise>().eq(ServiceEnterprise::getStatus, 1));

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("进出口统计");
        CellStyle headerStyle = createHeaderStyle(wb);

        String[] headers = {"企业名称", "统一社会信用代码", "年度出口额(万元)", "年度进口额(万元)", "进出口总额(万元)"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        BigDecimal totalExport = BigDecimal.ZERO;
        BigDecimal totalImport = BigDecimal.ZERO;

        for (int i = 0; i < enterprises.size(); i++) {
            ServiceEnterprise e = enterprises.get(i);
            BigDecimal exp = e.getAnnualExportAmount() != null ? e.getAnnualExportAmount() : BigDecimal.ZERO;
            BigDecimal imp = e.getAnnualImportAmount() != null ? e.getAnnualImportAmount() : BigDecimal.ZERO;
            totalExport = totalExport.add(exp);
            totalImport = totalImport.add(imp);

            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(str(e.getEnterpriseName()));
            row.createCell(1).setCellValue(str(e.getCreditCode()));
            row.createCell(2).setCellValue(dec(e.getAnnualExportAmount()));
            row.createCell(3).setCellValue(dec(e.getAnnualImportAmount()));
            row.createCell(4).setCellValue(dec(exp.add(imp)));
        }

        Row totalRow = sheet.createRow(enterprises.size() + 1);
        totalRow.createCell(0).setCellValue("合计");
        totalRow.createCell(2).setCellValue(dec(totalExport));
        totalRow.createCell(3).setCellValue(dec(totalImport));
        totalRow.createCell(4).setCellValue(dec(totalExport.add(totalImport)));

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        writeResponse(response, wb, "进出口统计");
    }

    public void exportServiceSummary(HttpServletResponse response) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("综合服务概览");
        CellStyle headerStyle = createHeaderStyle(wb);

        String[] headers = {"服务类型", "数据量", "状态"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        long customsCount = declarationMapper.selectCount(null);
        long logisticsCount = logisticsMapper.selectCount(null);
        long taxRefundCount = taxRefundMapper.selectCount(null);
        long settlementCount = settlementMapper.selectCount(null);
        long insuranceCount = insuranceMapper.selectCount(null);
        long financingCount = financingMapper.selectCount(null);

        Object[][] data = {
                {"通关服务(报关单)", customsCount, customsCount > 0 ? "已覆盖" : "待完善"},
                {"物流服务", logisticsCount, logisticsCount > 0 ? "已覆盖" : "待完善"},
                {"退税业务", taxRefundCount, taxRefundCount > 0 ? "已覆盖" : "待完善"},
                {"结算收汇", settlementCount, settlementCount > 0 ? "已覆盖" : "待完善"},
                {"信保服务", insuranceCount, insuranceCount > 0 ? "已覆盖" : "待完善"},
                {"融资协助", financingCount, financingCount > 0 ? "已覆盖" : "待完善"},
        };

        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue((String) data[i][0]);
            row.createCell(1).setCellValue(((Long) data[i][1]).doubleValue());
            row.createCell(2).setCellValue((String) data[i][2]);
        }

        int coveredCount = 0;
        for (Object[] d : data) {
            if ("已覆盖".equals(d[2])) coveredCount++;
        }
        Row summaryRow = sheet.createRow(data.length + 2);
        summaryRow.createCell(0).setCellValue("服务覆盖率");
        summaryRow.createCell(1).setCellValue(coveredCount + "/6");

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        writeResponse(response, wb, "综合服务概览");
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void writeResponse(HttpServletResponse response, Workbook wb, String filename) throws IOException {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fullName = filename + "_" + dateStr + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fullName, StandardCharsets.UTF_8));
        wb.write(response.getOutputStream());
        wb.close();
    }

    private String str(String val) {
        return val != null ? val : "";
    }

    private double dec(BigDecimal val) {
        return val != null ? val.doubleValue() : 0;
    }
}
