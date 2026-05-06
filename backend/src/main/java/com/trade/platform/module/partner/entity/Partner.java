package com.trade.platform.module.partner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_partner")
public class Partner extends BaseEntity {

    private Long tenantId;

    private String type;

    private String name;

    private String creditCode;

    private String address;

    private String province;

    private String city;

    private String district;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private Integer status;
}
