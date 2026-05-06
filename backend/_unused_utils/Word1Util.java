package cn.nodesoft.utils;

import java.io.*;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Word1Util {
    /**
     * @param mouldName 模板的名称 比如"basic.ftl"
     * @param dataMap   传入的数据（key=ftl 中的站位的名称同时 要是String ）
     * @param fileName  最后生成的word文件的名称
     * @param response
     * @throws IOException
     */
    public void exportWord(String mouldName, Map<String, Object> dataMap, String fileName, HttpServletResponse response) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setDefaultEncoding("utf-8");
        if (dataMap != null) {
            Iterator it = dataMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                Object value = entry.getValue();
                dataMap.put(key, value == null ? "" : value);//集合
            }
        }
        // 所在的项目中的路径
        configuration.setClassForTemplateLoading(this.getClass(), "/downloadTemplates");
        Template t = null;
        try {
            //test.ftl word模板文件
            t = configuration.getTemplate(mouldName);
            // FileTemplateLoader
        } catch (IOException e) {
            e.printStackTrace();
        }
        String FileName = fileName + ".doc";

        //设置响应头 以下载的方式返回到浏览器
        // response.setHeader("Content-Disposition","attachment;filename=test.txt");
        // response.setHeader("Content-Disposition","attachment;filename=" + FileName);
        response.setHeader("Content-Type", "application/octet-stream");

        response.setContentType("application/x-msdownload");
        response.setContentType("application/msword");
        // response.setContentType("application/msword;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + FileName);
        // response.setHeader("Content-Disposition", "attachment;filename=" + new String(FileName.getBytes("gbk"), "iso-8859-1"));

        Writer out = null;
        try {
            try {
                out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            t.process(dataMap, out);
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}

