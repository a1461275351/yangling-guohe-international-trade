package cn.nodesoft.utils;

import cn.nodesoft.common.constant.GeneralConst;
import cn.nodesoft.common.enums.TemplatesEnum;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.exception.ParsePropertyException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.processor.RowListProcessor;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParser;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParserSettings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @ProjectName: spdb-bm
 * @PackageName: cn.nodesoft.utils
 * @ClassName: FileUtil
 * @Description: 文件操作工具
 * @author: chenkang
 * @date: 2022-06-24 11:43
 * @Copyright: 西安捷点科技开发有限责任公司 Copyright (c) 2022
 */
@Slf4j
@Component
public class FileUtil {

    private static Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|（）]");

    private static final String ftp_enable = GlobalUtil.getEnvironmentProperty("ftp.enable");

    private static final String ftp_uploadPath = GlobalUtil.getEnvironmentProperty("ftp.uploadPath");

    /**
     * 将inputStream转化为file
     * @param is
     * @param file 要输出的文件目录
     */
    public static void inputStream2File (InputStream is, File file) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[8192];

            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } finally {
            os.close();
            is.close();
        }
    }

    /**
     * 下载模板文件（只限于excel）
     * @param templatesEnum
     * @param response
     * @throws IOException
     */
    public static void downloadFrTemplate (TemplatesEnum templatesEnum, HttpServletResponse response) throws IOException {
        String en_filename = templatesEnum.getKey();
        String contentType = ContentTypeUtil.getContentTypeByName(en_filename);
        String zh_filename = templatesEnum.getValue();
        Resource resource = new ClassPathResource(GeneralConst.DOWNLOAD_TEMPLATE_PATH + en_filename);
        InputStream resourceInputStream = resource.getInputStream();
        if (resourceInputStream != null) {
            // 在开发工具或war包下可正常下载
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                response.reset();
                //设置响应文本格式
                response.setContentType(contentType + ";charset=utf-8");
                response.setHeader("Content-Disposition",
                        "attachment;filename=" + new String(zh_filename.getBytes(), "iso-8859-1"));
                // TODO 动态解决跨域问题 chenkang
                response.addHeader(  "Access-Control-Allow-Credentials","true");//允许所有来源访同
                response.addHeader(  "Access-Control-Allow-Origin","http://localhost:9000");//允许访问的方式
                //将文件输出到页面
                ServletOutputStream out = response.getOutputStream();
                bis = new BufferedInputStream(resourceInputStream);
                bos = new BufferedOutputStream(out);
                byte[] buff = new byte[2048];
                int bytesRead;
                // 根据读取并写入
                while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                    bos.write(buff, 0, bytesRead);
                }
            } catch (ParsePropertyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvalidFormatException e) {
                // TODO Auto-generated catch block
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
        } else {
            // 项目打包成jar文件后，只能取到文件流，不能转成file对象
            log.error("未获取到目标文件《"+zh_filename+"》，即将重试...");
            ClassPathResource classPathResource = new ClassPathResource(GeneralConst.DOWNLOAD_TEMPLATE_PATH + en_filename);
            InputStream inputStream = classPathResource.getInputStream();
            if (null == inputStream) {
                log.error("仍未获取到目标文件《"+zh_filename+"》，终止");
            }
            response.reset();
            response.setHeader("contentType","application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename="+ new String(zh_filename.getBytes("gbk"), "iso-8859-1"));
            try {
                IOUtils.copy(inputStream, response.getOutputStream());
                response.flushBuffer();
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }


    /**
     * @Title: FileUtil
     * @Description: 压缩多文件
     * @Param: [srcFiles待压缩的文件集合, zipFile]
     * @return: void
     * @throws:
     * @Author: Wanghonglin
     * @Date: 2022/6/27 16:12
     * @version: v1.0
     * Modification History:
     * @Author        @date        @version   @Description
     * Wanghonglin  2022/6/27 16:12    v1.0    压缩多文件
     * -------------------------------------------------------------
     */
    public static void zipFiles(List<File> srcFiles, File zipFile) {
        if (!zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        FileInputStream fileInputStream = null;
        try {
            fileOutputStream = new FileOutputStream(zipFile);
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            ZipEntry zipEntry = null;
            for (int i = 0; i < srcFiles.size(); i++) {
                // 将源文件数组中的当前文件读入FileInputStream流中
                fileInputStream = new FileInputStream(srcFiles.get(i));
                // 实例化ZipEntry对象,源文件数组中的当前文件
                zipEntry = new ZipEntry(srcFiles.get(i).getName());
                zipOutputStream.putNextEntry(zipEntry);
                // 该变量记录每次真正读的字节个数
                int len;
                // 定义每次读取的字节数组
                byte[] buffer = new byte[1024];
                while ((len = fileInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            assert fileInputStream != null;
            fileInputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getFilePath(){
        Calendar date = Calendar.getInstance();
        int month = date.get(Calendar.MONTH)+1;
        int day = date.get(Calendar.DAY_OF_MONTH);
        String m = "";
        String d = "";
        if (month < 10) {
            m = "0" + month;
        } else {
            m = month+"";
        }
        if (day < 10) {
            d = "0" + day;
        } else {
            d = day + "";
        }
        return  File.separator + String.valueOf(date.get(Calendar.YEAR))
                + File.separator + m
                + File.separator + d + File.separator;
    }

    public static String getFilePath(Date sourceDate){
        Calendar date = Calendar.getInstance();
        date.setTime(sourceDate);
        int month = date.get(Calendar.MONTH)+1;
        int day = date.get(Calendar.DAY_OF_MONTH);
        String m = "";
        String d = "";
        if (month < 10) {
            m = "0" + month;
        } else {
            m = month+"";
        }
        if (day < 10) {
            d = "0" + day;
        } else {
            d = day + "";
        }
        return  File.separator + String.valueOf(date.get(Calendar.YEAR))
                + File.separator + m
                + File.separator + d + File.separator;
    }
    public static String getFilePathWin(Date sourceDate){
        Calendar date = Calendar.getInstance();
        date.setTime(sourceDate);
        int month = date.get(Calendar.MONTH)+1;
        int day = date.get(Calendar.DAY_OF_MONTH);
        String m = "";
        String d = "";
        if (month < 10) {
            m = "0" + month;
        } else {
            m = month+"";
        }
        if (day < 10) {
            d = "0" + day;
        } else {
            d = day + "";
        }
        return  "\\"+ String.valueOf(date.get(Calendar.YEAR))
                + "\\" + m
                + "\\" + d + "\\";
    }
    public static String getYMFilePath(){
        Calendar date = Calendar.getInstance();
        int month = date.get(Calendar.MONTH)+1;
        String m = "";
        if (month < 10) {
            m = "0" + month;
        } else {
            m = month+"";
        }
        return  File.separator + String.valueOf(date.get(Calendar.YEAR))
                + File.separator + m
                + File.separator;
    }

    /**
     * 文件上传
     * @param uploadPath
     * @param fName
     * @param inputStream
     * @return
     */
    public static boolean uploadFile(String uploadPath, String fName, InputStream inputStream) {
        File file = new File(uploadPath);
        if(!file.exists()){
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] buffer = new byte[4096];
        try (OutputStream fos = Files.newOutputStream(file.toPath()); InputStream fis = inputStream) {
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    /**
     * 解析csv文件
     * @param file
     * @return
     */
    public static List<String[]> parseCSV(File file){
        //创建一个配置选项，用来提供多种配置选项
        CsvParserSettings parserSettings = new CsvParserSettings();
        //打开解析器的自动检测功能，让它自动检测输入中包含的分隔符
        parserSettings.setLineSeparatorDetectionEnabled(true);
        //创建RowListProcessor对象，用来把每个解析的行存储在列表中
        RowListProcessor rowListProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowListProcessor);  //配置解析器
        //待解析的CSV文件包含标题头，把第一个解析行看作文件中每个列的标题
        parserSettings.setHeaderExtractionEnabled(true);
        parserSettings.setLineSeparatorDetectionEnabled(true);

        //创建CsvParser对象，用于解析文件
        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(file);

        //如果解析中包含标题，用于获取标题
        String[] headers = rowListProcessor.getHeaders();
        //获取行值，并遍历打印
        List<String[]> rows = rowListProcessor.getRows();
        /*for(int i = 0; i < rows.size(); i++){
            System.out.println(Arrays.asList(rows.get(i)));
        }*/
        return rows;
    }

    /**
     * 解析csv文件
     * @param InputStream
     * @return
     */
    public static List<String[]> parseCSV(InputStream inputStream){
        //创建一个配置选项，用来提供多种配置选项
        CsvParserSettings parserSettings = new CsvParserSettings();
        //打开解析器的自动检测功能，让它自动检测输入中包含的分隔符
        parserSettings.setLineSeparatorDetectionEnabled(true);

        //创建RowListProcessor对象，用来把每个解析的行存储在列表中
        RowListProcessor rowListProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowListProcessor);  //配置解析器
        //待解析的CSV文件包含标题头，把第一个解析行看作文件中每个列的标题
        parserSettings.setHeaderExtractionEnabled(true);
        parserSettings.setLineSeparatorDetectionEnabled(true);

        //创建CsvParser对象，用于解析文件
        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(inputStream);

        //如果解析中包含标题，用于获取标题
        String[] headers = rowListProcessor.getHeaders();
        //获取行值，并遍历打印
        List<String[]> rows = rowListProcessor.getRows();
        /*for(int i = 0; i < rows.size(); i++){
            System.out.println(Arrays.asList(rows.get(i)));
        }*/
        return rows;
    }


    /**
     * 删除单个文件
     *
     * @param pathName 删除文件路径名
     * @return
     */
    public static boolean deleteFiles(String pathName) {
        pathName = pathName.replaceAll(Matcher.quoteReplacement(File.separator),"/");
        pathName = pathName.replaceAll("\\\\","/");
        boolean flag = false;
        //根据路径创建文件对象
        File file = new File(pathName);
        //路径是个文件且不为空时删除文件
        if (file.isFile() && file.exists()) {
            flag = file.delete();
        }
        //删除失败时，返回false
        return flag;
    }
}
