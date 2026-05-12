package com.trade.platform.module.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trade.platform.module.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
