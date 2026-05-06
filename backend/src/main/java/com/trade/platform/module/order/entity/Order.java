package com.trade.platform.module.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_order")
public class Order extends BaseEntity {

    private Long tenantId;

    private String orderNo;

    private Long contractId;

    private String tradeTerms;

    private String paymentMethod;

    private String status;

    private BigDecimal totalAmount;

    private String currency;

    private String remark;

    @TableField(exist = false)
    private List<OrderGoods> goodsList;

    @TableField(exist = false)
    private String contractNo;

    @TableField(exist = false)
    private String contractTitle;
}
