package com.trade.platform.module.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_contract")
public class Contract extends BaseEntity {

    private Long tenantId;

    private String contractNo;

    private String title;

    private String ourCompany;

    private Long partnerId;

    private String partnerType;

    private String partnerName;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate signDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;

    private BigDecimal amount;

    private String currency;

    private String terms;

    private String remark;

    private Long enterpriseId;

    private String contractType;
}
