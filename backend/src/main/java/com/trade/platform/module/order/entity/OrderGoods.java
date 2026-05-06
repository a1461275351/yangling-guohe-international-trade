package com.trade.platform.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("biz_order_goods")
public class OrderGoods {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long goodsId;

    private String goodsName;

    private String goodsNo;

    private String hsCode;

    private BigDecimal quantity;

    private String unit;

    private BigDecimal price;

    private BigDecimal amount;
}
