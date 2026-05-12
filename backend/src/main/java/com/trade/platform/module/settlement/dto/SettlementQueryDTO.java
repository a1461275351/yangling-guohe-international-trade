package com.trade.platform.module.settlement.dto;

import lombok.Data;

@Data
public class SettlementQueryDTO {
    private String settlementNo;
    private String settlementType;
    private Long enterpriseId;
    private String status;
    private Long current = 1L;
    private Long size = 20L;
}
