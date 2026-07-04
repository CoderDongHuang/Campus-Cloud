package com.sharecampus.user.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.user.entity.UserAddress;
import com.sharecampus.user.entity.WorkerCertification;
import com.sharecampus.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final HttpServletRequest request;

    /** 从 Gateway 注入的 Header 获取用户ID */
    private Long userId() {
        String uid = request.getHeader("X-User-Id");
        return uid != null && !uid.isEmpty() && !"null".equals(uid) ? Long.valueOf(uid) : null;
    }

    @GetMapping("/address/list")
    public Result<List<UserAddress>> listAddresses() {
        return Result.success(userService.listAddresses(userId()));
    }

    @PostMapping("/address")
    public Result<Void> addAddress(@RequestBody UserAddress address) {
        userService.addAddress(address, userId());
        return Result.success();
    }

    @PutMapping("/address/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody UserAddress address) {
        address.setId(id);
        userService.updateAddress(address, userId());
        return Result.success();
    }

    @DeleteMapping("/address/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) { userService.deleteAddress(id); return Result.success(); }

    @PutMapping("/address/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        userService.setDefault(id, userId());
        return Result.success();
    }

    @PostMapping("/worker/certify")
    public Result<Void> submitCert(@RequestBody WorkerCertification cert) {
        userService.submitCert(cert, userId());
        return Result.success();
    }

    @GetMapping("/worker/certify/status")
    public Result<WorkerCertification> certStatus() {
        return Result.success(userService.getCertStatus(userId()));
    }
}
