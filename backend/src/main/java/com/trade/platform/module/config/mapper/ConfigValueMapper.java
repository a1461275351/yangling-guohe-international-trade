package com.trade.platform.module.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trade.platform.module.config.entity.ConfigValue;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConfigValueMapper extends BaseMapper<ConfigValue> {
}
