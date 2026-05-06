package com.trade.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private String role;

    @TableField("tenant_id")
    private Long tenantId;

    private Integer status;
}
