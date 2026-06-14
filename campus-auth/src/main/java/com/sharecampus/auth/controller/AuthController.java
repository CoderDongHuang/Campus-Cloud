package com.sharecampus.auth.controller;

import com.sharecampus.auth.service.AuthService;
import com.sharecampus.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 密码登录 */
    @PostMapping("/login")
    public Result<Map<String, String>> loginByPassword(@RequestBody Map<String, String> body) {
        Map<String, String> tokens = authService.loginByPassword(
                body.get("phone"), body.get("password"));
        return Result.success(tokens);
    }

    /** 验证码登录 */
    @PostMapping("/login/sms")
    public Result<Map<String, String>> loginBySms(@RequestBody Map<String, String> body) {
        Map<String, String> tokens = authService.loginBySms(
                body.get("phone"), body.get("code"));
        return Result.success(tokens);
    }

    /** 注册 */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody Map<String, String> body) {
        authService.register(body.get("phone"), body.get("password"), body.get("code"));
        return Result.success();
    }

    /** 发送验证码 */
    @PostMapping("/sms/send")
    public Result<Void> sendSms(@RequestBody Map<String, String> body) {
        authService.sendSmsCode(body.get("phone"));
        return Result.success();
    }

    /** 刷新 Token */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        return Result.success(authService.refreshToken(body.get("refreshToken")));
    }

    /** 登出 */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return Result.success();
    }
}
