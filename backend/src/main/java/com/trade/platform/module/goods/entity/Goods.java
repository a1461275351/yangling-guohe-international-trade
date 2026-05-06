package com.trade.platform.module.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_goods")
public class Goods extends BaseEntity {

    private Long tenantId;

    private String goodsNo;

    private String name;

    private String hsCode;

    private String spec;

    private String model;

    private String unit;

    private BigDecimal price;

    private String currency;

    private String imageUrl;

    private String category;
}
