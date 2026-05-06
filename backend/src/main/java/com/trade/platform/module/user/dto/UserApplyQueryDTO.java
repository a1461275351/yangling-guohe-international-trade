package com.trade.platform.module.user.dto;

import lombok.Data;

@Data
public class UserApplyQueryDTO {

    private Long tenantId;

    private String status;

    private Long current = 1L;

    private Long size = 20L;

    private String startDate;

    private String endDate;
}
