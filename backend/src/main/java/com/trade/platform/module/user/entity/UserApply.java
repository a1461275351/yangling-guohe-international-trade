package com.trade.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_apply")
public class UserApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private String role;

    private Long tenantId;

    /**
     * PENDING, APPROVED, REJECTED
     */
    private String status;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
