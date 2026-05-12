package com.trade.platform.module.customs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_customs_declaration")
public class CustomsDeclaration {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String declarationNo;

    private String ieType;

    private LocalDate ieDate;

    private String transportMode;

    private String tradeMode;

    private String customsCode;

    private String consignee;

    private String consigner;

    private String status;

    private BigDecimal totalAmount;

    private String currency;

    private String remark;

    private Long contractId;

    private Long orderId;

    private Long enterpriseId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long ledgerId;

    /** 拆单序号 */
    private Integer splitSeq;

    /** 审核状态: DRAFT/SUBMITTED/REVIEWING/APPROVED/REJECTED/RELEASED */
    private String reviewStatus;

    private LocalDateTime submitTime;

    private Long submitBy;

    private LocalDateTime reviewTime;

    private Long reviewBy;

    @TableLogic
    private Integer deleted;
}
