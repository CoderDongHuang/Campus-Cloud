package com.sharecampus.tenant.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.tenant.entity.Tenant;
import com.sharecampus.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody Tenant tenant) { tenantService.register(tenant); return Result.success(); }

    @GetMapping("/current")
    public Result<Tenant> current(@RequestHeader("X-Tenant-Id") Long tenantId) { return Result.success(tenantService.current(tenantId)); }

    @GetMapping("/admin/list")
    public Result<List<Tenant>> listAll() { return Result.success(tenantService.listAll()); }

    @GetMapping("/packages")
    public Result<java.util.List<com.sharecampus.tenant.entity.TenantPackage>> listPackages() {
        return Result.success(tenantService.listPackages());
    }

    @GetMapping("/usage")
    public Result<Map<String, Object>> usage(@RequestHeader("X-Tenant-Id") Long tenantId) {
        return Result.success(tenantService.getUsage(tenantId));
    }

    @PutMapping("/admin/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Boolean> body) { tenantService.audit(id, body.getOrDefault("approved", true)); return Result.success(); }

    @PutMapping("/admin/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) { tenantService.updateStatus(id, body.get("status")); return Result.success(); }
}
