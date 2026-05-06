package com.trade.platform.module.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_file")
public class BizFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String fileName;

    private String originalName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private String businessType;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
}
