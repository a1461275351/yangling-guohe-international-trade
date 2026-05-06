package com.trade.platform.module.ledger.dto;

import lombok.Data;

@Data
public class LedgerQueryDTO {

    private String ledgerNo;

    private String supplierName;

    private String status;

    private String contractNo;

    private String startDate;

    private String endDate;

    private Integer current = 1;

    private Integer size = 20;
}
