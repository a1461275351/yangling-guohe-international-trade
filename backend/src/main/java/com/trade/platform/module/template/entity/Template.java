package com.trade.platform.module.template.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_template")
public class Template extends BaseEntity {

    private String type;

    private String name;

    private String description;

    private String htmlContent;

    private String filePath;

    private String fileName;

    private Long fileSize;

    private String pdfPath;
}
