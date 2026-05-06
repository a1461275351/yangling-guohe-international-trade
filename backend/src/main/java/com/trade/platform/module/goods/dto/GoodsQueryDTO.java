package com.trade.platform.module.goods.dto;

import lombok.Data;

@Data
public class GoodsQueryDTO {

    private String name;

    private String hsCode;

    private String goodsNo;

    private String category;

    private Long current = 1L;

    private Long size = 20L;
}
