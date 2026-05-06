package com.trade.platform.module.order.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {

    private String orderNo;

    private Long contractId;

    private String status;

    private String startDate;

    private String endDate;

    private Long current = 1L;

    private Long size = 20L;
}
