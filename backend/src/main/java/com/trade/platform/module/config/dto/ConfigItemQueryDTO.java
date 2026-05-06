package com.trade.platform.module.config.dto;

import lombok.Data;

@Data
public class ConfigItemQueryDTO {

    private String code;

    private String name;

    private Integer status;

    private Long current = 1L;

    private Long size = 20L;
}
