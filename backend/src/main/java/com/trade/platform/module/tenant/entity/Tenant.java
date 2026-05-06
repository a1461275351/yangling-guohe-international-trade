package com.trade.platform.module.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
public class Tenant extends BaseEntity {

    private String tenantCode;

    private String name;

    private String creditCode;

    private Integer status;

    private String contactPerson;

    private String contactPhone;

    private String address;
}
