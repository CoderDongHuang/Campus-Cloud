package com.sharecampus.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 当前登录用户上下文
 * <p>
 * 由 Gateway 解析 Token 后通过 Header 透传，下游服务通过拦截器解析到 ThreadLocal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    /** 用户 ID */
    private Long userId;
    /** 租户 ID */
    private Long tenantId;
    /** 用户类型：STUDENT / WORKER / ADMIN */
    private String userType;
    /** 角色列表 */
    private List<String> roles;

    // ==================== ThreadLocal 持有 ====================

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    public static void set(UserContext context) { HOLDER.set(context); }

    public static UserContext get() { return HOLDER.get(); }

    public static void clear() { HOLDER.remove(); }

    // ==================== 快捷方法 ====================

    public static Long getUserId() {
        UserContext ctx = get();
        return ctx != null ? ctx.getUserId() : null;
    }

    public static Long getTenantId() {
        UserContext ctx = get();
        return ctx != null ? ctx.getTenantId() : null;
    }
}
