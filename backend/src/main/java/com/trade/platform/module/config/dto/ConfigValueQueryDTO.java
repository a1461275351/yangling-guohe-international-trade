package com.trade.platform.module.config.dto;

import lombok.Data;

@Data
public class ConfigValueQueryDTO {

    private Long configItemId;

    private Integer status;

    private String keyword;

    private Long current = 1L;

    private Long size = 20L;
}
