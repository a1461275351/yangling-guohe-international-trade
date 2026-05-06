package cn.nodesoft.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import cn.nodesoft.common.enums.reportEnums.reportNameEnum;
import cn.nodesoft.vo.SheetDTO;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);


    /**
     * excel 导出工具类
     *
     * @param response
     * @param fileName    文件名
     * @param projects    对象集合
     * @param columnNames 导出的excel中的列名
     * @param keys        对应的是对象中的字段名字
     * @throws IOException
     */
    public static void export(HttpServletResponse response, String fileName, List<?> projects, String[] columnNames, String[] keys) throws IOException {

        ExcelWriter bigWriter = cn.hutool.poi.excel.ExcelUtil.getBigWriter();

        for (int i = 0; i < columnNames.length; i++) {
            bigWriter.addHeaderAlias(columnNames[i], keys[i]);
            bigWriter.setColumnWidth(i, 20);
        }
        // 一次性写出内容，使用默认样式，强制输出标题
        bigWriter.write(projects, true);
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName + ".xlsx").getBytes(), "iso-8859-1"));
        ServletOutputStream out = response.getOutputStream();
        bigWriter.flush(out, true);
        // 关闭writer，释放内存
        bigWriter.close();
        //此处记得关闭输出Servlet流
        IoUtil.close(out);
    }

    /**
     *	导出多个 Sheet 页
     * @param response
     * @param sheetList 页数据
     * @param fileName 文件名
     */
    public static void exportExcel(HttpServletResponse response, List<SheetDTO> sheetList, String fileName) {
        ExcelWriter bigWriter = cn.hutool.poi.excel.ExcelUtil.getBigWriter();
        // 重命名第一个Sheet的名称，不然会默认多出一个Sheet1的页
        bigWriter.renameSheet(0, sheetList.get(0).getSheetName());
        for (SheetDTO sheet : sheetList) {
            // 指定要写出的 Sheet 页
            bigWriter.setSheet(sheet.getSheetName());
            Integer[] columnWidth = sheet.getColumnWidth();
            if (columnWidth == null || columnWidth.length != sheet.getFieldAndAlias().size()) {
                // 设置默认宽度 
                for (int i = 0; i < sheet.getFieldAndAlias().size(); i++) {
                    bigWriter.setColumnWidth(i, 25);
                }
            } else {
                // 设置自定义宽度 
                for (int i = 0; i < columnWidth.length; i++) {
                    bigWriter.setColumnWidth(i, columnWidth[i]);
                }
            }
//            //设置背景色
//            CellStyle cellStyle = bigWriter.createCellStyle();
//            cellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
//            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


            // 设置字段和别名
            bigWriter.setHeaderAlias(sheet.getFieldAndAlias());
            // 设置只导出有别名的字段
            bigWriter.setOnlyAlias(true);
            // 设置默认行高
            bigWriter.setDefaultRowHeight(18);
            // 设置冻结行
            bigWriter.setFreezePane(1);
            // 一次性写出内容，使用默认样式，强制输出标题
            bigWriter.write(sheet.getCollection(), true);
            // 设置所有列为自动宽度，不考虑合并单元格
//            bigWriter.autoSizeColumnAll();
        }

        ServletOutputStream out = null;
        try {
            //response为HttpServletResponse对象
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
//            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".xlsx", "iso-8859-1"));
            response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName + ".xlsx").getBytes(), "iso-8859-1"));

            out = response.getOutputStream();
            bigWriter.flush(out, true);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭writer，释放内存
            bigWriter.close();
        }
        //此处记得关闭输出Servlet流
        IoUtil.close(out);
    }


    /**
     * excel导入工具类
     *
     * @param file       文件
     * @param columNames 列对应的字段名
     * @return 返回数据集合
     * @throws OperationException
     * @throws IOException
     */
    /*public static List<Map<String, Object>> leading(MultipartFile file, String[] columNames) throws OperationException, IOException {
        String fileName = file.getOriginalFilename();
        // 上传文件为空
        if (StringUtils.isEmpty(fileName)) {
            throw new OperationException(ReturnCodeEnum.OPERATION_EXCEL_ERROR, "没有导入文件");
        }
        //上传文件大小为1000条数据
        if (file.getSize() > 1024 * 1024 * 10) {
            logger.error("upload | 上传失败: 文件大小超过10M，文件大小为：{}", file.getSize());
            throw new OperationException(ReturnCodeEnum.OPERATION_EXCEL_ERROR, "上传失败: 文件大小不能超过10M!");
        }
        // 上传文件名格式不正确
        if (fileName.lastIndexOf(".") != -1 && !".xlsx".equals(fileName.substring(fileName.lastIndexOf(".")))) {
            throw new OperationException(ReturnCodeEnum.OPERATION_EXCEL_ERROR, "文件名格式不正确, 请使用后缀名为.XLSX的文件");
        }

        //读取数据
        ExcelUtil.read07BySax(file.getInputStream(), 0, createRowHandler());
        //去除excel中的第一行数据
        lineList.remove(0);

        //将数据封装到list<Map>中
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < lineList.size(); i++) {
            if (null != lineList.get(i)) {
                Map<String, Object> hashMap = new HashMap<>();
                for (int j = 0; j < columNames.length; j++) {
                    Object property = lineList.get(i).get(j);
                    hashMap.put(columNames[j], property);
                }
                dataList.add(hashMap);
            } else {
                break;
            }
        }
        return dataList;
    }*/

    /**
     *	通过实现handle方法编写我们要对每行数据的操作方式
     */
    /*private static RowHandler createRowHandler() {
        //清空一下集合中的数据
        lineList.removeAll(lineList);
        return new RowHandler() {
            @Override
            public void handle(int sheetIndex, int rowIndex, List rowlist) {
                //将读取到的每一行数据放入到list集合中
                JSONArray jsonObject = new JSONArray(rowlist);
                lineList.add(jsonObject.toList(Object.class));
            }
        };
    }*/


    /**
     *	通过模板导出excel
     * @param response
     * @param beans 导出所需数据
     * @param fileName 前端路由名
     */
    public static void exportExcel(Map beans, String fileName, HttpServletResponse response) {
        String chinesFileName=reportNameEnum.getValueByKey(fileName);
        // TODO 待优化, 此方式打成jar包可能获取不到模板
        Resource resource = new ClassPathResource("excelmodel/" + fileName + ".xls");
        InputStream resourceInputStream = null;
        try {
            resourceInputStream = resource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (resourceInputStream != null) {
            //transformer转到Excel
            XLSTransformer transformer = new XLSTransformer();

            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                //将数据添加到模版中生成新的文件
//                transformer.transformXLS(file.getPath(), beans, destFile.getAbsolutePath());
                org.apache.poi.ss.usermodel.Workbook workbook = transformer.transformXLS(resourceInputStream,beans);

                // 设置输出流
                ServletOutputStream out = response.getOutputStream();
                // 设置导出格式
                response.setHeader("Content-Disposition", "attachment;filename=" + new String((chinesFileName+DateUtil.dateTimeNow()+".xls").getBytes(),"iso-8859-1"));
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                // 将内容写入输出流并把缓存的内容全部发出去
                workbook.write(out);
                // 关闭流
                resourceInputStream.close();
                out.flush();
                out.close();

            } catch (ParsePropertyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvalidFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //使用完成后关闭流
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (bos != null) {
                        bos.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * excel 导出到本地工具类
     *
     * @param fileName    文件名 （包含路徑）
     * @param projects    对象集合
     * @param columnNames 导出的excel中的列名
     * @param keys        对应的是对象中的字段名字
     * @throws IOException
     */
    public static void export( String fileName, List<?> projects, String[] columnNames, String[] keys) throws IOException {
        ExcelWriter bigWriter = cn.hutool.poi.excel.ExcelUtil.getBigWriter();
        SheetDTO sheet = new SheetDTO();
        sheet.setTitles(columnNames);
        sheet.setProperties(keys);
        sheet.setCollection(projects);
        Integer[] columnWidth = sheet.getColumnWidth();
        if (columnWidth == null || columnWidth.length != sheet.getFieldAndAlias().size()) {
            // 设置默认宽度
            for (int i = 0; i < sheet.getFieldAndAlias().size(); i++) {
                bigWriter.setColumnWidth(i, 25);
            }
        } else {
            // 设置自定义宽度
            for (int i = 0; i < columnWidth.length; i++) {
                bigWriter.setColumnWidth(i, columnWidth[i]);
            }
        }
        //设置表头颜色
//        StyleSet styleSet = bigWriter.getStyleSet();
//        CellStyle headCellStyle = styleSet.getHeadCellStyle();
//        headCellStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN1.getIndex());
//        headCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置字段和别名
        bigWriter.setHeaderAlias(sheet.getFieldAndAlias());
        // 设置只导出有别名的字段
        bigWriter.setOnlyAlias(true);
        // 设置默认行高
        bigWriter.setDefaultRowHeight(18);
        // 设置冻结行
        bigWriter.setFreezePane(1);
        // 指定要写出的 Sheet 页
        bigWriter.setSheet(sheet.getSheetName());
        // 一次性写出内容，使用默认样式，强制输出标题
//        bigWriter.write(projects, true);
        bigWriter.write(sheet.getCollection(), true);
        FileOutputStream outputStream = new FileOutputStream(fileName);
        bigWriter.flush(outputStream, true);
        // 关闭writer，释放内存
        bigWriter.close();
        //此处记得关闭输出Servlet流
        IoUtil.close(outputStream);
    }


    /**
     * xls转换为xlsx
     * @param filePath
     * @return
     */
    public static File xlsToXlsx(String filePath) {
        File outF = null;
        try (InputStream in = new BufferedInputStream(new FileInputStream(filePath))) {
            Workbook wbIn = new HSSFWorkbook(in);
            outF = new File(filePath + "x");
            if (outF.exists())
                outF.delete();

            Workbook wbOut = new XSSFWorkbook();
            int sheetCnt = wbIn.getNumberOfSheets();
            for (int i = 0; i < sheetCnt; i++) {
                Sheet sIn = wbIn.getSheetAt(i);
                Sheet sOut = wbOut.createSheet(sIn.getSheetName());
                Iterator<Row> rowIt = sIn.rowIterator();
                while (rowIt.hasNext()) {
                    Row rowIn = rowIt.next();
                    Row rowOut = sOut.createRow(rowIn.getRowNum());

                    Iterator<Cell> cellIt = rowIn.cellIterator();
                    while (cellIt.hasNext()) {
                        Cell cellIn = cellIt.next();
                        Cell cellOut = rowOut.createCell(
                                cellIn.getColumnIndex(), cellIn.getCellType());

                        switch (cellIn.getCellType()) {
                            case Cell.CELL_TYPE_BLANK:
                                break;

                            case Cell.CELL_TYPE_BOOLEAN:
                                cellOut.setCellValue(cellIn.getBooleanCellValue());
                                break;

                            case Cell.CELL_TYPE_ERROR:
                                cellOut.setCellValue(cellIn.getErrorCellValue());
                                break;

                            case Cell.CELL_TYPE_FORMULA:
                                cellOut.setCellFormula(cellIn.getCellFormula());
                                break;

                            case Cell.CELL_TYPE_NUMERIC:
                                cellOut.setCellValue(cellIn.getNumericCellValue());
                                break;

                            case Cell.CELL_TYPE_STRING:
                                cellOut.setCellValue(cellIn.getStringCellValue());
                                break;
                        }

                        {
                            CellStyle styleIn = cellIn.getCellStyle();
                            CellStyle styleOut = cellOut.getCellStyle();
                            styleOut.setDataFormat(styleIn.getDataFormat());
                        }
                        cellOut.setCellComment(cellIn.getCellComment());

                        // HSSFCellStyle cannot be cast to XSSFCellStyle
//                         cellOut.setCellStyle(cellIn.getCellStyle());
                    }
                }
            }
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outF))) {
                wbOut.write(out);
                // 删除旧版本
                new File(filePath).delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outF;
    }

    /**
     * 使用excel模板导出ecxel到指定目录下
     * @param beans 对象list
     * @param excelmodelName excel模板名称
     * @param path excel文件生成目录
     * @return
     * @throws Exception
     */
    public static String saveFSWAsExcel(Map beans,String excelmodelName,String path)throws Exception {
        File directory = new File("");// 参数为空
//        String courseFile = directory.getCanonicalPath();
        //模板的路径
//        String srcPath = "E:\\gitlab\\spdb-bm\\src\\main\\resources\\excelmodel/issuedcount.xls";
//        //生成文件的路径
//        String to = "d:/ftp/拆分明细.xls";

//        如果不涉及合并单元格，执行以下两句即可
//        XLSTransformer transformer = new XLSTransformer();
//        transformer.transformXLS(srcPath, beans, to);
//        -----------------------------------------------------------
        Resource resource = new ClassPathResource("excelmodel/" + excelmodelName + ".xls");
        InputStream resourceInputStream = null;
        try {
            resourceInputStream = resource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (resourceInputStream != null) {
            XLSTransformer transformer = new XLSTransformer();
            HSSFWorkbook workBook = (HSSFWorkbook) transformer.transformXLS(resourceInputStream, beans);
            OutputStream os = new FileOutputStream(path);
            workBook.write(os);
            resourceInputStream.close();
            os.flush();
            os.close();
        }
        return path;

    }
    public static   Map beanToMap(Object object) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(object));
        }
        return map;
    }


}