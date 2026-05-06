package com.trade.platform.module.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ResetPasswordDTO {

    @NotBlank(message = "租户编码不能为空")
    private String tenantCode;

    @NotBlank(message = "用户名不能为空")
    private String username;
}
