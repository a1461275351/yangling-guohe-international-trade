package com.trade.platform.module.user.controller;

import com.trade.platform.common.OpLog;
import com.trade.platform.common.Result;
import com.trade.platform.module.user.dto.LoginDTO;
import com.trade.platform.module.user.dto.LoginVO;
import com.trade.platform.module.user.dto.RegisterDTO;
import com.trade.platform.module.user.dto.ResetPasswordDTO;
import com.trade.platform.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @OpLog(module = "认证", action = "LOGIN", description = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return Result.success(vo);
    }

    @OpLog(module = "认证", action = "REGISTER", description = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        userService.resetPasswordRequest(dto);
        return Result.success();
    }
}
