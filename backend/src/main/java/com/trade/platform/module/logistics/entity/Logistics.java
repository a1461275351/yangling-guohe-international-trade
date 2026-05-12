package com.trade.platform.module.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_logistics")
public class Logistics extends BaseEntity {

    private Long tenantId;
    private String logisticsNo;
    private Long orderId;
    private Long contractId;
    private String transportMode;
    private String departurePort;
    private String destinationPort;
    private String vesselVoyage;
    private String blNo;
    private String waybillNo;
    private String logisticsProvider;
    private BigDecimal freightAmount;
    private String freightCurrency;
    private BigDecimal insuranceAmount;
    private LocalDate etd;
    private LocalDate eta;
    private LocalDate actualDeparture;
    private LocalDate actualArrival;
    private Integer isColdChain;
    private String temperatureRange;
    private String status;
    private String remark;
}
