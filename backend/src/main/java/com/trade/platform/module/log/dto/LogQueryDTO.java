package com.trade.platform.module.log.dto;

import lombok.Data;

@Data
public class LogQueryDTO {
    private String username;
    private String module;
    private String action;
    private String startDate;
    private String endDate;
    private Long current = 1L;
    private Long size = 20L;
}
