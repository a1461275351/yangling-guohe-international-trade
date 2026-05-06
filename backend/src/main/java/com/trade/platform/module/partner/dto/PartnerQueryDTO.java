package com.trade.platform.module.partner.dto;

import lombok.Data;

@Data
public class PartnerQueryDTO {

    private String type;

    private String name;

    private Integer status;

    private Long current = 1L;

    private Long size = 20L;
}
