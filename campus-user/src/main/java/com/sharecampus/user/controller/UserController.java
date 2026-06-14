package com.sharecampus.user.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.user.entity.UserAddress;
import com.sharecampus.user.entity.WorkerCertification;
import com.sharecampus.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ===== 地址 =====
    @GetMapping("/address/list")
    public Result<List<UserAddress>> listAddresses() {
        return Result.success(userService.listAddresses());
    }

    @PostMapping("/address")
    public Result<Void> addAddress(@RequestBody UserAddress address) {
        userService.addAddress(address);
        return Result.success();
    }

    @PutMapping("/address/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody UserAddress address) {
        address.setId(id);
        userService.updateAddress(address);
        return Result.success();
    }

    @DeleteMapping("/address/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        userService.deleteAddress(id);
        return Result.success();
    }

    @PutMapping("/address/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        userService.setDefault(id);
        return Result.success();
    }

    // ===== 师傅认证 =====
    @PostMapping("/worker/certify")
    public Result<Void> submitCert(@RequestBody WorkerCertification cert) {
        userService.submitCert(cert);
        return Result.success();
    }

    @GetMapping("/worker/certify/status")
    public Result<WorkerCertification> certStatus() {
        return Result.success(userService.getCertStatus());
    }
}
