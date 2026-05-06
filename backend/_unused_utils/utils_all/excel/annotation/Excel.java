package cn.nodesoft.utils.excel.annotation;

import cn.nodesoft.utils.excel.enums.ExcelType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 小懒虫
 * @date 2018/12/14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Excel {
    // 字段标题名称或文件名称
    public String value();
    // excel操作类型ExcelType
    public ExcelType type() default ExcelType.ALL;
    // 关联操作实体对象字段名称，用于获取关联数据（只支持导出操作）
    public String joinField() default "";
}
