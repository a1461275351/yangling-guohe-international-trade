package com.trade.platform.module.file.dto;

import lombok.Data;

@Data
public class FileQueryDTO {

    private String fileName;

    private String businessType;

    private String startDate;

    private String endDate;

    private Long current = 1L;

    private Long size = 20L;
}
