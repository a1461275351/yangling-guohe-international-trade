package com.trade.platform.module.config.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config_item")
public class ConfigItem extends BaseEntity {

    private String code;

    private String name;

    private Integer status;
}
