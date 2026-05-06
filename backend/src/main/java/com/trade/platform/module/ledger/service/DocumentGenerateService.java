package com.trade.platform.module.ledger.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trade.platform.common.BusinessException;
import com.trade.platform.module.ledger.entity.ImportLedger;
import com.trade.platform.module.ledger.entity.ImportLedgerGoods;
import com.trade.platform.module.ledger.mapper.ImportLedgerGoodsMapper;
import com.trade.platform.module.ledger.mapper.ImportLedgerMapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 单据生成服务 - 严格按照公司模板的14列双栏布局
 * 布局：左侧C0-C9(收发货人信息)  右侧C10-C13(编号/日期/重量等)
 */
@Service
public class DocumentGenerateService {

    @Resource
    private ImportLedgerMapper ledgerMapper;
    @Resource
    private ImportLedgerGoodsMapper ledgerGoodsMapper;

    // ==================== 样式 ====================

    private CellStyle titleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 18); f.setFontName("Times New Roman");
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        return s;
    }

    private CellStyle labelStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 10); f.setFontName("SimSun");
        s.setFont(f);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        return s;
    }

    private CellStyle valueStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 10); f.setFontName("SimSun");
        s.setFont(f);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        return s;
    }

    private CellStyle italicStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setItalic(true); f.setFontHeightInPoints((short) 10); f.setFontName("SimSun");
        s.setFont(f);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 10); f.setFontName("SimSun");
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        s.setBorderTop(BorderStyle.THIN); s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN); s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private CellStyle dataStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 10); f.setFontName("SimSun");
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        s.setBorderTop(BorderStyle.THIN); s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN); s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private CellStyle dataRightStyle(Workbook wb) {
        CellStyle s = dataStyle(wb);
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    // ==================== 列宽设置 ====================

    private void setColumnWidths(Sheet sheet) {
        // 14列，总宽度约 16000px
        sheet.setColumnWidth(0, 1600);   // 序号
        sheet.setColumnWidth(1, 3000);   // 采购订单
        sheet.setColumnWidth(2, 3200);   // HS编码
        sheet.setColumnWidth(3, 2800);   // 料号
        sheet.setColumnWidth(4, 2500);   // 品名左半
        sheet.setColumnWidth(5, 1500);   // 品名右半
        sheet.setColumnWidth(6, 3200);   // 规格型号
        sheet.setColumnWidth(7, 3200);   // 英文名称
        sheet.setColumnWidth(8, 3000);   // 原产国
        sheet.setColumnWidth(9, 2800);   // 数量
        sheet.setColumnWidth(10, 3500);  // 单位 / 右侧标签列
        sheet.setColumnWidth(11, 3000);  // 净重
        sheet.setColumnWidth(12, 2200);  // 件数
        sheet.setColumnWidth(13, 3200);  // 毛重
    }

    // ==================== 通用头部（14列双栏） ====================

    /**
     * 写入通用头部（Consignee + Shipper + 右侧信息）
     * 返回下一个可用的行号
     */
    private int writeHeader(Sheet sheet, Workbook wb, ImportLedger ledger, String titleCn, String titleEn, boolean isPackingList) {
        CellStyle ts = titleStyle(wb);
        CellStyle ls = labelStyle(wb);
        CellStyle vs = valueStyle(wb);

        int r = 0;

        // === Row 0-1: 标题 ===
        Row rTitle = sheet.createRow(r++);
        rTitle.setHeightInPoints(40);
        Cell cTitle = rTitle.createCell(0);
        cTitle.setCellValue(titleCn + "\n" + titleEn);
        cTitle.setCellStyle(ts);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));

        // === Row 1: Consignee 开始 + No ===
        Row r1 = sheet.createRow(r++);
        r1.setHeightInPoints(20);
        setCell(r1, 0, "Consignee: ", ls);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 9));
        setCell(r1, 10, "No:", ls);
        setCell(r1, 11, safe(ledger.getLedgerNo()), vs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));

        // === Row 2: Consignee名称 + Date ===
        Row r2 = sheet.createRow(r++);
        r2.setHeightInPoints(20);
        setCell(r2, 0, safe(ledger.getConsignee()), vs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 9));
        setCell(r2, 10, "Date:", ls);
        setCell(r2, 11, ledger.getIeDate() != null ? ledger.getIeDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) : "", vs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));

        // === Row 3-4: 空行（consignee地址续行） ===
        sheet.createRow(r++).setHeightInPoints(16);
        sheet.createRow(r++).setHeightInPoints(16);

        // === Row 5: Tel/Contact/Fax + Delivery term ===
        Row r5 = sheet.createRow(r++);
        r5.setHeightInPoints(20);
        setCell(r5, 0, "Tel:", ls);
        setCell(r5, 3, "Contact:", ls);
        setCell(r5, 5, "Fax:", ls);
        setCell(r5, 10, "Delivery term:", ls);
        setCell(r5, 11, safe(ledger.getTradeMode()), vs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));

        // === Row 6: + No. of Packages ===
        Row r6 = sheet.createRow(r++);
        r6.setHeightInPoints(20);
        setCell(r6, 10, "No. of Packages:", ls);
        setCell(r6, 11, "", vs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));

        if (isPackingList) {
            // === Row 7: Kind of packages ===
            Row r7 = sheet.createRow(r++);
            r7.setHeightInPoints(20);
            setCell(r7, 10, "Kind of packages:", ls);
            setCell(r7, 11, "", vs);
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));

            // === Row 8: Container List ===
            Row r8 = sheet.createRow(r++);
            r8.setHeightInPoints(20);
            setCell(r8, 10, "Container List:", ls);
            setCell(r8, 11, safe(ledger.getCaseNo()), vs);
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));
        }

        // === Shipper 开始 ===
        Row rShipper = sheet.createRow(r++);
        rShipper.setHeightInPoints(20);
        setCell(rShipper, 0, "Shipper : ", ls);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 9));

        // === 发货人名称 + Gross Weight ===
        Row rShipperName = sheet.createRow(r++);
        rShipperName.setHeightInPoints(20);
        setCell(rShipperName, 0, safe(ledger.getSupplierName()), vs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 9));
        setCell(rShipperName, 10, "Gross Weight (KG):", ls);
        setCell(rShipperName, 11, "", vs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));

        // === Net Weight ===
        Row rNet = sheet.createRow(r++);
        rNet.setHeightInPoints(20);
        setCell(rNet, 10, "Net Weight (KG):", ls);
        setCell(rNet, 11, "", vs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));

        // === Tel/Contact/Fax + Measurement ===
        Row rTel2 = sheet.createRow(r++);
        rTel2.setHeightInPoints(20);
        setCell(rTel2, 0, "Tel:", ls);
        setCell(rTel2, 3, "Contact:", ls);
        setCell(rTel2, 5, "Fax:", ls);
        setCell(rTel2, 10, "Measurement(CBM):", ls);
        setCell(rTel2, 11, "", vs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));

        return r;
    }

    // ==================== 表头 ====================

    private int writePackingTableHeader(Sheet sheet, Workbook wb, int r) {
        CellStyle hs = headerStyle(wb);
        Row hr = sheet.createRow(r++);
        hr.setHeightInPoints(36);
        String[] headers = {
            "序号\nItem", "采购订单\nPO NO", "商品编码\nHS CODE", "商品料号\nPart NO",
            "商品名称\nDescription", "",
            "规格型号\nSpecification", "英文名称\nEnglish Description", "原产国\nCountry of Origin",
            "数量\nQuantity", "单位\nUnit", "净重\nnet weight (KG)", "件数\nPackages", "毛重\nGross Weight (KG)"
        };
        for (int i = 0; i < headers.length; i++) setCell(hr, i, headers[i], hs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 4, 5));
        return r;
    }

    private int writeInvoiceTableHeader(Sheet sheet, Workbook wb, int r) {
        CellStyle hs = headerStyle(wb);
        Row hr = sheet.createRow(r++);
        hr.setHeightInPoints(36);
        String[] headers = {
            "序号\nItem", "采购订单\nPO NO", "商品编码\nHS CODE", "商品料号\nPart NO",
            "商品名称\nDescription", "",
            "规格型号\nSpecification", "英文名称\nEnglish Description", "原产国\nCountry of Origin",
            "数量/单位\nQuantity/Unit", "单价/币制\nCurrency/Unit price", "金额/总计\nCurrency/Amount", "", ""
        };
        for (int i = 0; i < headers.length; i++) setCell(hr, i, headers[i], hs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 4, 5));
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));
        return r;
    }

    // ==================== 箱单 PackingList ====================

    public void generatePackingList(Long ledgerId, HttpServletResponse response) throws IOException {
        ImportLedger ledger = getLedger(ledgerId);
        List<ImportLedgerGoods> goods = getGoods(ledgerId);

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("PackingList");
        setColumnWidths(sheet);

        int r = writeHeader(sheet, wb, ledger, "箱单", "Packing list", true);
        r = writePackingTableHeader(sheet, wb, r);

        CellStyle ds = dataStyle(wb);
        CellStyle dr = dataRightStyle(wb);
        CellStyle hs = headerStyle(wb);
        CellStyle ls = labelStyle(wb);

        BigDecimal totalQty = BigDecimal.ZERO;
        for (int i = 0; i < goods.size(); i++) {
            ImportLedgerGoods g = goods.get(i);
            Row row = sheet.createRow(r++);
            row.setHeightInPoints(24);
            setCell(row, 0, String.valueOf(i + 1), ds);
            setCell(row, 1, "", ds);
            setCell(row, 2, safe(g.getHsCode()), ds);
            setCell(row, 3, "", ds);
            setCell(row, 4, safe(g.getName()), ds);
            setCell(row, 5, "", ds);
            setCell(row, 6, safe(g.getSpec()), ds);
            setCell(row, 7, "", ds);
            setCell(row, 8, safe(g.getOriginCountry()), ds);
            setCell(row, 9, numStr(g.getQuantity()), dr);
            setCell(row, 10, safe(g.getUnit()), ds);
            setCell(row, 11, "", ds);
            setCell(row, 12, "", ds);
            setCell(row, 13, "", ds);
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 4, 5));
            if (g.getQuantity() != null) totalQty = totalQty.add(g.getQuantity());
        }

        // Total 行
        Row tr = sheet.createRow(r++);
        tr.setHeightInPoints(24);
        setCell(tr, 0, "", ds); setCell(tr, 1, "", ds); setCell(tr, 2, "", ds); setCell(tr, 3, "", ds);
        setCell(tr, 4, "", ds); setCell(tr, 5, "", ds); setCell(tr, 6, "", ds); setCell(tr, 7, "", ds);
        setCell(tr, 8, "Total ", hs);
        setCell(tr, 9, numStr(totalQty), hs);
        setCell(tr, 10, "", ds);
        setCell(tr, 11, "0", hs);
        setCell(tr, 12, "", ds);
        setCell(tr, 13, "", ds);

        // 空行 + 签章
        sheet.createRow(r++);
        Row signRow = sheet.createRow(r);
        signRow.setHeightInPoints(40);
        setCell(signRow, 1, "买方\nTHE BUYERS:", ls);
        setCell(signRow, 11, "卖方\nTHE SELLERS：", ls);

        writeResponse(wb, response, "PackingList_" + ledger.getLedgerNo() + ".xls");
    }

    // ==================== 发票 Invoice ====================

    public void generateInvoice(Long ledgerId, HttpServletResponse response) throws IOException {
        ImportLedger ledger = getLedger(ledgerId);
        List<ImportLedgerGoods> goods = getGoods(ledgerId);

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Invoice");
        setColumnWidths(sheet);

        int r = writeHeader(sheet, wb, ledger, "发票", "Invoice", false);

        // 条款文字
        CellStyle is = italicStyle(wb);
        CellStyle ls = labelStyle(wb);
        Row c1 = sheet.createRow(r++);
        c1.setHeightInPoints(20);
        setCell(c1, 0, "兹经买卖双方同意成交下列商品订立条款如下：", ls);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 13));
        Row c2 = sheet.createRow(r++);
        c2.setHeightInPoints(20);
        setCell(c2, 0, "The undersigned Sellers and Buyers have agreed to close the following transaction according to the term and conditions stipulated below.", is);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 13));

        r = writeInvoiceTableHeader(sheet, wb, r);

        CellStyle ds = dataStyle(wb);
        CellStyle dr = dataRightStyle(wb);
        CellStyle hs = headerStyle(wb);

        String cur = safe(ledger.getCurrency()); if (cur.isEmpty()) cur = "USD";
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < goods.size(); i++) {
            ImportLedgerGoods g = goods.get(i);
            Row row = sheet.createRow(r++);
            row.setHeightInPoints(24);
            setCell(row, 0, String.valueOf(i + 1), ds);
            setCell(row, 1, "", ds);
            setCell(row, 2, safe(g.getHsCode()), ds);
            setCell(row, 3, "", ds);
            setCell(row, 4, safe(g.getName()), ds);
            setCell(row, 5, "", ds);
            setCell(row, 6, safe(g.getSpec()), ds);
            setCell(row, 7, "", ds);
            setCell(row, 8, safe(g.getOriginCountry()), ds);
            setCell(row, 9, numStr(g.getQuantity()) + safe(g.getUnit()), dr);
            setCell(row, 10, g.getPrice() != null ? cur + g.getPrice().toPlainString() : "", dr);
            setCell(row, 11, g.getAmount() != null ? cur + numStr(g.getAmount()) : "", dr);
            setCell(row, 12, "", ds);
            setCell(row, 13, "", ds);
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 4, 5));
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));
            if (g.getAmount() != null) totalAmount = totalAmount.add(g.getAmount());
        }

        // Total
        Row tr = sheet.createRow(r++);
        tr.setHeightInPoints(24);
        for (int i = 0; i < 14; i++) setCell(tr, i, "", ds);
        setCell(tr, 10, "Total", hs);
        setCell(tr, 11, cur + numStr(totalAmount), hs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));

        // 签章
        sheet.createRow(r++);
        Row signRow = sheet.createRow(r);
        signRow.setHeightInPoints(40);
        setCell(signRow, 1, "卖方\nTHE SELLERS：", ls);
        setCell(signRow, 10, "买方\nTHE BUYERS:", ls);

        writeResponse(wb, response, "Invoice_" + ledger.getLedgerNo() + ".xls");
    }

    // ==================== 合同 Contract ====================

    public void generateContract(Long ledgerId, HttpServletResponse response) throws IOException {
        ImportLedger ledger = getLedger(ledgerId);
        List<ImportLedgerGoods> goods = getGoods(ledgerId);

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Contract");
        setColumnWidths(sheet);

        int r = writeHeader(sheet, wb, ledger, "合同", "Contract", false);

        CellStyle is = italicStyle(wb);
        CellStyle ls = labelStyle(wb);
        Row c1 = sheet.createRow(r++);
        c1.setHeightInPoints(20);
        setCell(c1, 0, "兹经买卖双方同意成交下列商品订立条款如下：", ls);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 13));
        Row c2 = sheet.createRow(r++);
        c2.setHeightInPoints(20);
        setCell(c2, 0, "The undersigned Sellers and Buyers have agreed to close the following transaction according to the term and conditions stipulated below.", is);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 13));

        r = writeInvoiceTableHeader(sheet, wb, r);

        CellStyle ds = dataStyle(wb);
        CellStyle dr = dataRightStyle(wb);
        CellStyle hs = headerStyle(wb);

        String cur = safe(ledger.getCurrency()); if (cur.isEmpty()) cur = "USD";
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < goods.size(); i++) {
            ImportLedgerGoods g = goods.get(i);
            Row row = sheet.createRow(r++);
            row.setHeightInPoints(24);
            setCell(row, 0, String.valueOf(i + 1), ds);
            setCell(row, 1, "", ds);
            setCell(row, 2, safe(g.getHsCode()), ds);
            setCell(row, 3, "", ds);
            setCell(row, 4, safe(g.getName()), ds);
            setCell(row, 5, "", ds);
            setCell(row, 6, safe(g.getSpec()), ds);
            setCell(row, 7, "", ds);
            setCell(row, 8, safe(g.getOriginCountry()), ds);
            setCell(row, 9, numStr(g.getQuantity()) + safe(g.getUnit()), dr);
            setCell(row, 10, g.getPrice() != null ? cur + g.getPrice().toPlainString() : "", dr);
            setCell(row, 11, g.getAmount() != null ? cur + numStr(g.getAmount()) : "", dr);
            setCell(row, 12, "", ds);
            setCell(row, 13, "", ds);
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 4, 5));
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));
            if (g.getAmount() != null) totalAmount = totalAmount.add(g.getAmount());
        }

        Row tr = sheet.createRow(r++);
        tr.setHeightInPoints(24);
        for (int i = 0; i < 14; i++) setCell(tr, i, "", ds);
        setCell(tr, 10, "Total", hs);
        setCell(tr, 11, cur + numStr(totalAmount), hs);
        sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 11, 13));

        sheet.createRow(r++);
        Row signRow = sheet.createRow(r);
        signRow.setHeightInPoints(40);
        setCell(signRow, 1, "卖方\nTHE SELLERS：", ls);
        setCell(signRow, 10, "买方\nTHE BUYERS:", ls);

        writeResponse(wb, response, "Contract_" + ledger.getLedgerNo() + ".xls");
    }

    // ==================== 工具方法 ====================

    private ImportLedger getLedger(Long id) {
        ImportLedger l = ledgerMapper.selectById(id);
        if (l == null) throw new BusinessException("台账不存在");
        return l;
    }

    private List<ImportLedgerGoods> getGoods(Long ledgerId) {
        return ledgerGoodsMapper.selectList(new LambdaQueryWrapper<ImportLedgerGoods>().eq(ImportLedgerGoods::getLedgerId, ledgerId));
    }

    private String safe(String s) { return s != null ? s : ""; }

    private String numStr(BigDecimal n) {
        if (n == null) return "";
        return n.stripTrailingZeros().toPlainString();
    }

    private void setCell(Row row, int col, String val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(val);
        c.setCellStyle(style);
    }

    private void writeResponse(Workbook wb, HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        wb.write(response.getOutputStream());
        wb.close();
    }
}
