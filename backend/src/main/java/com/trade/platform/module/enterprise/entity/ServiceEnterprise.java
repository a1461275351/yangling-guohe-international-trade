package com.trade.platform.module.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_service_enterprise")
public class ServiceEnterprise extends BaseEntity {

    private Long tenantId;
    private String enterpriseName;
    private String creditCode;
    private String region;
    private String industry;
    private String productType;
    private Integer isFirstTrade;
    private String agreementStatus;
    private String agreementNo;
    private LocalDate serviceStartDate;
    private LocalDate serviceEndDate;
    private String serviceScope;
    private String bizContactName;
    private String bizContactPhone;
    private String finContactName;
    private String finContactPhone;
    private String docContactName;
    private String docContactPhone;
    private String riskLevel;
    private Integer annualServiceCount;
    private BigDecimal annualExportAmount;
    private BigDecimal annualImportAmount;
    private Integer status;
    private String remark;
}
