package com.trade.platform.module.tenant.dto;

import lombok.Data;

@Data
public class TenantQueryDTO {

    private String name;

    private Integer status;

    private String startDate;

    private String endDate;

    private Long current = 1L;

    private Long size = 20L;
}
