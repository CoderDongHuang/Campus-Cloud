package com.sharecampus.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.common.core.exception.BizException;
import com.sharecampus.common.core.exception.ErrorCode;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.tenant.entity.Tenant;
import com.sharecampus.tenant.mapper.TenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantMapper tenantMapper;

    /** 学校注册 */
    public void register(Tenant tenant) {
        tenant.setStatus(0); // 待审核
        tenantMapper.insert(tenant);
        log.info("租户注册: name={}", tenant.getName());
    }

    /** 获取当前租户信息 */
    public Tenant current() {
        Long tenantId = UserContext.getTenantId();
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) throw new BizException(ErrorCode.TENANT_NOT_FOUND);
        return tenant;
    }

    /** 审核通过 */
    public void audit(Long id, boolean approved) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) throw new BizException(ErrorCode.TENANT_NOT_FOUND);
        tenant.setStatus(approved ? 1 : 2);
        if (approved) tenant.setExpireTime(LocalDate.now().plusYears(1));
        tenantMapper.updateById(tenant);
        log.info("租户审核: id={}, status={}", id, tenant.getStatus());
    }

    /** 租户列表（超管） */
    public List<Tenant> listAll() {
        return tenantMapper.selectList(new LambdaQueryWrapper<Tenant>().orderByDesc(Tenant::getCreateTime));
    }

    /** 变更租户状态 */
    public void updateStatus(Long id, Integer status) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) throw new BizException(ErrorCode.TENANT_NOT_FOUND);
        tenant.setStatus(status);
        tenantMapper.updateById(tenant);
    }
}
