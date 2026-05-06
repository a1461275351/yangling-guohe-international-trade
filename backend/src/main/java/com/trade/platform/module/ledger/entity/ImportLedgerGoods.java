package com.trade.platform.module.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("biz_import_ledger_goods")
public class ImportLedgerGoods {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ledgerId;

    private String goodsNo;

    private Long goodsId;

    private String name;

    private String hsCode;

    private String spec;

    private BigDecimal quantity;

    private String unit;

    private BigDecimal price;

    private BigDecimal amount;

    private String originCountry;

    private String currency;

    private BigDecimal assignedQty;
}
