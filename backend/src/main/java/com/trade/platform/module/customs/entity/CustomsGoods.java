package com.trade.platform.module.customs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("biz_customs_goods")
public class CustomsGoods {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long declarationId;

    private String goodsNo;

    private String name;

    private String hsCode;

    private String spec;

    private BigDecimal quantity;

    private String unit;

    private BigDecimal price;

    private BigDecimal amount;

    private String originCountry;
}
