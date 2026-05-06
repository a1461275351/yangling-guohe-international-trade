package com.trade.platform.module.template.dto;

import lombok.Data;

@Data
public class TemplateQueryDTO {

    private String type;

    private String keyword;

    private Long current = 1L;

    private Long size = 20L;
}
