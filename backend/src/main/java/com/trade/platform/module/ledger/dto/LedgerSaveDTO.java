package com.trade.platform.module.ledger.dto;

import com.trade.platform.module.ledger.entity.ImportLedgerGoods;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class LedgerSaveDTO {

    private Long id;

    private String ledgerNo;

    private String splitStatus;

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

    private LocalDate ieDate;

    private String consignee;

    private Long orderId;

    private String status;

    private String remark;

    private List<ImportLedgerGoods> goodsList;
}
