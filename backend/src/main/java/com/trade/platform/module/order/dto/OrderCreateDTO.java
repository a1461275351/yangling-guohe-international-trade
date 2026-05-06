package com.trade.platform.module.order.dto;

import com.trade.platform.module.order.entity.OrderGoods;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderCreateDTO {

    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    private String tradeTerms;

    private String paymentMethod;

    private String remark;

    private List<OrderGoods> goodsList;
}
