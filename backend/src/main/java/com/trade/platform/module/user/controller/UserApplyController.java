package com.trade.platform.module.user.controller;

import com.trade.platform.security.RequireRole;

import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.user.dto.UserApplyQueryDTO;
import com.trade.platform.module.user.entity.UserApply;
import com.trade.platform.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user-applies")
@RequireRole("ADMIN")
public class UserApplyController {

    @Autowired
    private UserService userService;

    @PostMapping("/list")
    public Result<PageResult<UserApply>> list(@RequestBody UserApplyQueryDTO dto) {
        PageResult<UserApply> result = userService.getApplyList(dto);
        return Result.success(result);
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        userService.approveApply(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        userService.rejectApply(id, reason);
        return Result.success();
    }

    @PutMapping("/batch-approve")
    public Result<Void> batchApprove(@RequestBody List<Long> ids) {
        userService.batchApprove(ids);
        return Result.success();
    }
}
