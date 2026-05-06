package com.trade.platform.module.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_import_ledger")
public class ImportLedger extends BaseEntity {

    private Long tenantId;

    private String ledgerNo;

    private String splitStatus;

    private Integer docCount;

    private Integer goodsCount;

    private String caseNo;

    private String masterBlNo;

    private String subBlNo;

    private Long supplierId;

    private String supplierName;

    private String supervisionMode;

    private String contractNo;

    private Long contractId;

    private String declareCustoms;

    private String entryCustoms;

    private String originCountry;

    private String transitPort;

    private String entryPort;

    private String transportMode;

    private String tradeMode;

    private BigDecimal totalAmount;

    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ieDate;

    private String consignee;

    private Long orderId;

    private String status;

    private String remark;

    private Long createBy;

    @TableField(exist = false)
    private List<ImportLedgerGoods> goodsList;
}
