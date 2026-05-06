package com.trade.platform.module.config.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config_value")
public class ConfigValue extends BaseEntity {

    private Long configItemId;

    private String code;

    private String name;

    private Integer status;
}
