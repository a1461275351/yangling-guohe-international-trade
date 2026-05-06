package com.trade.platform.module.user.dto;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private Long userId;

    private String username;

    private String realName;

    private String role;

    private Long tenantId;

    private String tenantName;

    private String tenantCode;
}
