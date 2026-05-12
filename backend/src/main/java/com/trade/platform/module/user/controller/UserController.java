package com.trade.platform.module.user.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.user.dto.PasswordDTO;
import com.trade.platform.module.user.dto.UserQueryDTO;
import com.trade.platform.module.user.entity.User;
import com.trade.platform.module.user.service.UserService;
import com.trade.platform.security.RequireRole;
import com.trade.platform.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @RequireRole({"ADMIN", "GUOHE"})
    @PostMapping("/list")
    public Result<PageResult<User>> list(@RequestBody UserQueryDTO dto) {
        PageResult<User> result = userService.getUserList(dto);
        return Result.success(result);
    }

    @RequireRole({"ADMIN", "GUOHE"})
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    @RequireRole({"ADMIN", "GUOHE"})
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        String newPassword = userService.resetUserPassword(id);
        return Result.success(newPassword);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Long userId = UserContext.getUserId();
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody User user) {
        Long userId = UserContext.getUserId();
        userService.updateUserInfo(userId, user.getRealName(), user.getPhone(), user.getEmail());
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid PasswordDTO dto) {
        Long userId = UserContext.getUserId();
        userService.changePassword(userId, dto);
        return Result.success();
    }
}
