package com.sharecampus.common.mybatis;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.sharecampus.common.security.UserContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.Set;

/**
 * 租户隔离拦截器
 * <p>
 * 自动在每条 SQL 的 WHERE 条件后追加 tenant_id = 当前租户ID，
 * 实现 SaaS 多租户数据隔离。
 */
public class TenantInterceptor implements TenantLineHandler {

    /** 不需要租户隔离的表（全局配置表、跨租户表） */
    private static final Set<String> IGNORE_TABLES = Set.of(
            "sys_config", "sys_dict", "tenant", "tenant_package"
    );

    @Override
    public Expression getTenantId() {
        Long tenantId = UserContext.currentTenantId();
        if (tenantId == null) {
            tenantId = 0L; // 超管无租户时默认 0
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return IGNORE_TABLES.contains(tableName);
    }
}
