package com.trade.platform.module.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_import_ledger_file")
public class ImportLedgerFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ledgerId;

    private Long fileId;

    private String fileType;

    private LocalDateTime createTime;
}
