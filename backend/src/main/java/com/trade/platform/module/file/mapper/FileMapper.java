package com.trade.platform.module.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trade.platform.module.file.entity.BizFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<BizFile> {
}
