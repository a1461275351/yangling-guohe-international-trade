package cn.nodesoft.utils;

import cn.hutool.core.io.IoUtil;
import cn.nodesoft.common.constant.GeneralConst;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.codec.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class PdfUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * @param map
     * @param response HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
     * @throws IOException
     */
    public void generatePDF(Map<String, String> map, String templateFileStr, String exportFilename, HttpServletResponse response, Boolean isPreview) throws IOException {
        String fileName = encodeChineseDownloadFileName(exportFilename);
        // OutputStream out;
        if (!isPreview) {
            // 设置下载头
            response.setHeader("Content-Disposition", "attachment;filename=" + new String((exportFilename + DateUtil.dateTimeNow() + ".pdf").getBytes(), "utf-8"));
            // response.setContentType("application/pdf;charset=utf-8");
            // 消除控制台报错
            response.setContentType("application/json;charset=utf-8");
        }

        // 模板路径
        URL templatePath = getClass().getClassLoader().getResource(templateFileStr);

        // 生成的新文件路径(下载本地)
        // String newPDFPath = "E:\\work\\postmandownloadfile\\test1-data.pdf";
        PdfReader reader;
        // FileOutputStream out;

        PdfStamper stamper;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            // 输出流
            // out = new FileOutputStream(newPDFPath);
            // 读取pdf模板
            reader = new PdfReader(templatePath);
            stamper = new PdfStamper(reader, bos);
            AcroFields form = stamper.getAcroFields();
            // 给表单添加中文字体 这里采用系统字体。不设置的话，中文可能无法显示
            String filepath1 = GlobalUtil.getEnvironmentProperty("fontfile.stpath");
            String filepath2 = GlobalUtil.getEnvironmentProperty("fontfile.tbpath");
            BaseFont baseFont1 = BaseFont.createFont(filepath1, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            BaseFont baseFont = BaseFont.createFont(filepath2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            ArrayList<BaseFont> fonts = new ArrayList<>();
            fonts.add(baseFont);
            fonts.add(baseFont1);
            form.setSubstitutionFonts(fonts);

            //遍历map装入数据
            for (Map.Entry<String, String> entry : map.entrySet()) {
                // Rectangle position = form.getFieldPositions(fileName).get(0).position;
                form.setField(entry.getKey(), entry.getValue());
                // form.setFieldProperty();
                //若当前文本域无法满足
                //自动换行后的高度
                if (checkAddrLength(entry.getValue(), entry.getKey(), form)) {
                    /*获取当前文本框的尺寸，返回的数据依次为左上右下（0,1,2,3）*/
                    PdfArray rect0 = form.getFieldItem(entry.getKey()).getValue(0).getAsArray(PdfName.RECT);
                    rect0.set(1, new PdfNumber(rect0.getAsNumber(1).intValue() - 16));
                    //手动换行
                    // Phrase phrase = new Phrase();
                    // phrase.add(Chunk.NEWLINE);
                    //设置高度(无效)
                    // Paragraph paragraph = new Paragraph(entry.getKey());
                    // PdfPCell pdfPCell = new PdfPCell(paragraph);
                    // pdfPCell.setFixedHeight(1000.11F);
                }
                System.out.println("插入PDF数据---->  key= " + entry.getKey() + " and value= " + entry.getValue());
            }

            // 如果为false那么生成的PDF文件还能编辑，一定要设为true
            stamper.setFormFlattening(true);
            stamper.close();

            // if (isPreview) {
            // 文件写到本地
            // String filePath = "D:\\data\\" + UUID.randomUUID() + ".pdf";
            // File testFile = new File(filePath);
            // testFile.createNewFile();
            // FileOutputStream fileOutputStream = new FileOutputStream(testFile);
            // IoUtil.copy(new ByteArrayInputStream(bos.toByteArray()), fileOutputStream);

            // 文件返回前端
            // response.reset();
            // response.setContentType("application/pdf");
            // response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("测试中文.pdf", "UTF-8"));//①tips
            // ServletOutputStream outputStream = response.getOutputStream();
            // IoUtil.copy(new FileInputStream(filePath), outputStream);


            // response.reset();
            // response.setContentType("application/pdf");
            // response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("测试中文.pdf", "UTF-8"));//①tips
            // ServletOutputStream outputStream = response.getOutputStream();
            // IoUtil.copy(new ByteArrayInputStream(bos.toByteArray()), outputStream);
            // }
            ServletOutputStream outputStream = response.getOutputStream();
            IoUtil.copy(new ByteArrayInputStream(bos.toByteArray()), outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("PDF制作错误-错误信息:" + e.getMessage(), e);
        }
    }

    /**
     * 以 Stream 的方式获取资源(jar包形式获取资源)
     * @return
     */
    private InputStream getTemplateToStream(String fontName) {
        // 获取模板资源
        Resource resource = new ClassPathResource("font/"+fontName);
        InputStream simfang = null;

        try {
            simfang = resource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return simfang;
    }

    /**
     * 使用iTextAsian.jar中的字体，FontFactory方式
     */
    public void test3() throws DocumentException, IOException {
        Font font = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED,10f, Font.NORMAL, BaseColor.BLACK);
        // createPdf(font);
    }

    /**
     * 超出长度处理
     *
     * @param addr
     * @param fileName
     * @param form
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    public boolean checkAddrLength(String addr, String fileName, AcroFields form) throws IOException, DocumentException {
        float fontSize = 14;
        boolean flag = false;
        // BaseFont baseFont = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        String filepath1 = GlobalUtil.getEnvironmentProperty("fontfile.stpath");
        BaseFont baseFont = BaseFont.createFont(filepath1, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        float textWidth = baseFont.getWidthPoint(addr, fontSize);
        Rectangle position = form.getFieldPositions(fileName).get(0).position;
        float textBoxWidth = position.getWidth();
        if (textWidth > textBoxWidth) {
            flag = true;
        }
        return flag;
    }

    public boolean checkHeight(String addr, String fileName, AcroFields form) throws IOException, DocumentException {
        float fontSize = 14;
        boolean flag = false;
        // BaseFont baseFont = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        String filepath1 = GlobalUtil.getEnvironmentProperty("fontfile.stpath");
        BaseFont baseFont = BaseFont.createFont(filepath1, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        // float textWidth = baseFont.getWidthPoint(addr, fontSize);
        float textWidth = baseFont.getWidthPoint(addr, fontSize);
        Rectangle position = form.getFieldPositions(fileName).get(0).position;
        // float textBoxWidth = position.getWidth();
        float textBoxHeight = position.getHeight();
        if (textWidth > textBoxHeight) {
            flag = true;
        }
        return flag;
    }

    // private void fillPdfTemplateData(File file, Map<String, String> params, String targetFileUrl) throws IOException {
    //     PdfDocument pdf = new PdfDocument(new PdfReader(file), new PdfWriter(targetFileUrl));
    //     PdfAcroForm form = PdfAcroForm.getAcroForm(pdf, true);//第二个参数，如果不显示值设置为true
    //     Map<String, PdfFormField> fields = form.getFormFields();
    //     String path = this.getClass().getResource("/").getPath();
    //     path = path.substring(0, path.indexOf("classes") - 1) + "/classes/font/simsun.ttc,1";
    //
    //     //使用指定字体如--微软雅黑
    //     // PdfFont font = PdfFontFactory.createFont(path, PdfEncodings.IDENTITY_H, BaseFont.EMBEDDED);
    //     //PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H", false);
    //
    //     //遍历值域，填充对应值
    //     Iterator<String> it = fields.keySet().iterator();
    //     while (it.hasNext()) {
    //         String name = it.next().toString();
    //         logger.info("pdf中标签参数名称:"+name);
    //         logger.info(name+"对应的值:"+params.get(name));
    //
    //         //设置字体，设置值，设置字体大小为自动
    //         fields.get(name).setFont(font).setValue(params.get(name) == null ? "" : params.get(name)).setFontSizeAutoScale();
    //         //fields.get(name).setFont(font).setValue(params.get(name));
    //     }
    //     form.flattenFields();
    //     pdf.close();
    // }

    /**
     * 编码格式转换
     *
     * @param pFileName 文件名称
     * @return String
     * @throws UnsupportedEncodingException
     */
    private String encodeChineseDownloadFileName(String pFileName)
            throws UnsupportedEncodingException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String filename;
        String agent = request.getHeader("USER-AGENT");
        if (null != agent) {
            if (-1 != agent.indexOf("Firefox")) {
                filename = "=?UTF-8?B?" + (new String(Base64.decode(String.valueOf(pFileName.getBytes(StandardCharsets.UTF_8))))) + "?=";
            } else if (-1 != agent.indexOf("Chrome")) {
                filename = new String(pFileName.getBytes(), StandardCharsets.ISO_8859_1);
            } else {//IE7+
                filename = URLEncoder.encode(pFileName, StandardCharsets.UTF_8.name());
            }
        } else {
            filename = pFileName;
        }
        return filename;
    }

    public static void returnPdfStream(HttpServletResponse response, String pathName) throws Exception {
        response.setContentType("application/pdf");

        File file = new File(pathName);
        if (file.exists()) {
            FileInputStream in = new FileInputStream(file);
            OutputStream out = response.getOutputStream();
            byte[] b = new byte[1024 * 1204];
            int n;
            while ((n = in.read(b)) != -1) {
                out.write(b, 0, n);
            }
            out.flush();
            in.close();
            out.close();
        }
    }

    /**
     * 将PDF转换成base64编码
     * 1.使用BufferedInputStream和FileInputStream从File指定的文件中读取内容；
     * 2.然后建立写入到ByteArrayOutputStream底层输出流对象的缓冲输出流BufferedOutputStream
     * 3.底层输出流转换成字节数组，然后由BASE64Encoder的对象对流进行编码
     * <p>
     * //将PDF格式文件转成base64编码
     * String base64String = getPDFBinary("C:\\Users\\Pluto\\Desktop\\1.1命题与联结词（第2课时）.pdf");
     * System.out.println(base64String);
     */
    public static String getPDFBinary(String filePath) {
        FileInputStream fin = null;
        BufferedInputStream bin = null;
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bout = null;
        try {
            //建立读取文件的文件输出流
            fin = new FileInputStream(new File(filePath));
            //在文件输出流上安装节点流（更大效率读取）
            bin = new BufferedInputStream(fin);
            // 创建一个新的 byte 数组输出流，它具有指定大小的缓冲区容量
            baos = new ByteArrayOutputStream();
            //创建一个新的缓冲输出流，以将数据写入指定的底层输出流
            bout = new BufferedOutputStream(baos);
            byte[] buffer = new byte[1024];
            int len = bin.read(buffer);
            while (len != -1) {
                bout.write(buffer, 0, len);
                len = bin.read(buffer);
            }
            //刷新此输出流并强制写出所有缓冲的输出字节，必须这行代码，否则有可能有问题
            bout.flush();
            byte[] bytes = baos.toByteArray();
            //sun公司的API
            BASE64Encoder encoder = new BASE64Encoder();
            return encoder.encodeBuffer(bytes).trim();
            //apache公司的API
            //return Base64.encodeBase64String(bytes);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fin.close();
                bin.close();
                //关闭 ByteArrayOutputStream 无效。此类中的方法在关闭此流后仍可被调用，而不会产生任何 IOException
                //baos.close();
                bout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
