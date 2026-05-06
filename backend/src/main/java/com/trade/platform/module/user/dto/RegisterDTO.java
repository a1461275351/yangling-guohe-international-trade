package com.trade.platform.module.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RegisterDTO {

    @NotBlank(message = "租户编码不能为空")
    private String tenantCode;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String realName;

    private String phone;

    private String email;

    @NotBlank(message = "角色不能为空")
    private String role;
}
