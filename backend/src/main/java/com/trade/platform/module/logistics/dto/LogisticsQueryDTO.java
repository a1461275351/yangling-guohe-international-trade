package com.trade.platform.module.logistics.dto;

import lombok.Data;

@Data
public class LogisticsQueryDTO {
    private String logisticsNo;
    private String transportMode;
    private String status;
    private Long orderId;
    private Long current = 1L;
    private Long size = 20L;
}
