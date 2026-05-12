package com.trade.platform.module.log.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.log.dto.LogQueryDTO;
import com.trade.platform.module.log.entity.OperationLog;
import com.trade.platform.module.log.service.OperationLogService;
import com.trade.platform.security.RequireRole;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/logs")
@RequireRole({"ADMIN", "GUOHE"})
public class OperationLogController {

    @Resource
    private OperationLogService logService;

    @PostMapping("/list")
    public Result<PageResult<OperationLog>> list(@RequestBody LogQueryDTO dto) {
        return Result.success(logService.getList(dto));
    }
}
