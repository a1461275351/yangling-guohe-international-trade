package cn.nodesoft.utils;

import cn.nodesoft.biz.sysConfigs.bean.ReportConfig;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName: spdb-bm
 * @PackageName: cn.nodesoft.utils
 * @ClassName: ReportUtil
 * @Description: 视图名解析工具类
 * @author: chenkang
 * @date: 2022-06-12 16:01
 * @Copyright: 西安捷点科技开发有限责任公司 Copyright (c) 2022
 */
public class ReportUtil {

    /**
     * 重要 重要 重要 ：在数据库中新建视图时统一以"v_"作为视图名称前缀（防止视图名和表名冲突）
     */
    public static final String VIEW_PREFIX = "v_";
    public static final String TABLE_PREFIX = "t_";


    /**
     * 循环原数据，这里需要解决的问题有两个
     * 1、从数据库视图中获取数据时，字段值如果为空，则mbs不返回该字段，导致前端接收数据时，某些行的某些字段缺失，会导致表格渲染出现异常
     * TODO 无法解决：2、直接返回key为中文的数据，这样在导出数据时，则可以直接将中文表头导出（在新map字段名称出现重复时覆盖，此问题无法解决，导出时考虑使用字段一对一翻译）
     */
    public static List<Map<String,Object>> checkReportKey(List<Map<String,Object>> sourceList, List<ReportConfig> configs){
        if (sourceList.size() == 0) {
            return sourceList;
        }
        for (Map<String,Object> map : sourceList) {
            // 从报表配置功能获取到的表头配置项集合
            for (ReportConfig config : configs) {
                // 判断原集合中是否存在配置的列
                if (!map.containsKey(config.getColumn().trim())) {
                    // 如果原集合没有目标列，则直接取配置的列的中文key和默认值
                    map.put(config.getColumn().trim(),config.getDefaultVal());
                }
            }
        }
        return sourceList;
    }


    public static List<ReportConfig> getConfigList(Enum[] enums){
        List<ReportConfig> configList = new ArrayList<>();
        for (Enum enumItem : enums) {
            ReportConfig reportConfig = new ReportConfig();
            BeanUtils.copyProperties(enumItem,reportConfig);
            configList.add(reportConfig);
        }
        return configList;
    }





















    /**
     * 将驼峰转为下划线
     * @param camelCaseVname
     * @return
     */
    public static String toUnderlineCase(String camelCaseVname) {
        if (camelCaseVname == null) {
            return null;
        }
        // 将驼峰字符串转换成数组
        char[] charArray = camelCaseVname.toCharArray();
        StringBuffer buffer = new StringBuffer();
        //处理字符串
        for (int i = 0, l = charArray.length; i < l; i++) {
            if (charArray[i] >= 65 && charArray[i] <= 90) {
                buffer.append("_").append(charArray[i] += 32);
            } else {
                buffer.append(charArray[i]);
            }
        }
        String sourceVname = buffer.toString();
        if (sourceVname.startsWith(VIEW_PREFIX)) {
            return sourceVname;
        } else {
            return VIEW_PREFIX+sourceVname;
        }
    }

    /**
     * 将驼峰转为下划线
     * @param camelCaseVname
     * @return
     */
    public static String toUnderlineCaseTable(String camelCaseVname) {
        if (camelCaseVname == null) {
            return null;
        }
        // 将驼峰字符串转换成数组
        char[] charArray = camelCaseVname.toCharArray();
        StringBuffer buffer = new StringBuffer();
        //处理字符串
        for (int i = 0, l = charArray.length; i < l; i++) {
            if (charArray[i] >= 65 && charArray[i] <= 90) {
                buffer.append("_").append(charArray[i] += 32);
            } else {
                buffer.append(charArray[i]);
            }
        }
        String sourceVname = buffer.toString();
        if (sourceVname.startsWith(TABLE_PREFIX)) {
            return sourceVname;
        } else {
            return TABLE_PREFIX+sourceVname;
        }
    }


    /**
     * 将下划线转为驼峰
     * @param underlineVname
     * @return
     */
    public static String toCamelCase(String underlineVname){
        if (underlineVname == null) {
            return null;
        }
        // 分成数组
        char[] charArray = underlineVname.toCharArray();
        // 判断上次循环的字符是否是"_"
        boolean underlineBefore = false;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, l = charArray.length; i < l; i++) {
            // 判断当前字符是否是"_",如果跳出本次循环
            if (charArray[i] == 95) {
                underlineBefore = true;
            } else if (underlineBefore) {
                // 如果为true，代表上次的字符是"_",当前字符需要转成大写
                buffer.append(charArray[i] -= 32);
                underlineBefore = false;
            } else {
                // 不是"_"后的字符就直接追加
                buffer.append(charArray[i]);
            }
        }
        return buffer.toString();
    }

    public static void main(String[] args) {
        System.out.println(toUnderlineCase("aaa_bbb_ccc"));
    }
}
