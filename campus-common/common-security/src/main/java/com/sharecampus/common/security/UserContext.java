package com.sharecampus.common.security;

import java.util.List;

public class UserContext {

    private Long userId;
    private Long tenantId;
    private String userType;
    private List<String> roles;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    // ==================== ThreadLocal ====================
    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();
    public static void set(UserContext context) { HOLDER.set(context); }
    public static UserContext get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }

    // ==================== 快捷静态方法 ====================
    public static Long currentUserId() {
        UserContext ctx = get();
        return ctx != null ? ctx.getUserId() : null;
    }
    public static Long currentTenantId() {
        UserContext ctx = get();
        return ctx != null ? ctx.getTenantId() : null;
    }
}
