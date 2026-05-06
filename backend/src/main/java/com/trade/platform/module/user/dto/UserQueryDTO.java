package com.trade.platform.module.user.dto;

import lombok.Data;

@Data
public class UserQueryDTO {

    private Long tenantId;

    private Integer status;

    private String keyword;

    private Long current = 1L;

    private Long size = 20L;
}
