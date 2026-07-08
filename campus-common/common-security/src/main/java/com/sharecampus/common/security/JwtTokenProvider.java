package com.sharecampus.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JWT Token 工具类
 * <p>
 * 负责生成 AccessToken（2h）和 RefreshToken（7d），以及 Token 解析和校验
 */
@Slf4j
@org.springframework.stereotype.Component
public class JwtTokenProvider {

    private static final String DEFAULT_SECRET = "ShareCampusCloud2025SecretKeyForJWT!!!!";
    public static final long ACCESS_TOKEN_EXPIRE_MS = 2 * 60 * 60 * 1000L;
    public static final long REFRESH_TOKEN_EXPIRE_MS = 7 * 24 * 60 * 60 * 1000L;

    @org.springframework.beans.factory.annotation.Value("${campus.jwt.secret:" + DEFAULT_SECRET + "}")
    private String secret;

    private SecretKey secretKey;

    /** 无参构造器（用于单元测试或非Spring环境） */
    public JwtTokenProvider() {
        this.secret = DEFAULT_SECRET;
        initSecretKey();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        if (secret == null) secret = DEFAULT_SECRET;
        initSecretKey();
        log.info("JWT密钥已加载(来源: {})", secret.equals(DEFAULT_SECRET) ? "默认值" : "Nacos配置");
    }

    private void initSecretKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // ==================== 生成 Token ====================

    /** 生成 AccessToken */
    public String createAccessToken(Long userId, Long tenantId, String userType, List<String> roles) {
        return buildToken(userId, tenantId, userType, roles, ACCESS_TOKEN_EXPIRE_MS);
    }

    /** 生成 RefreshToken */
    public String createRefreshToken(Long userId, Long tenantId) {
        return buildToken(userId, tenantId, null, null, REFRESH_TOKEN_EXPIRE_MS);
    }

    private String buildToken(Long userId, Long tenantId, String userType,
                              List<String> roles, long expireMs) {
        Date now = new Date();
        Date expiration = new Date(System.currentTimeMillis() + expireMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())                  // jti: 唯一ID，用于黑名单
                .subject(String.valueOf(userId))                   // sub: 用户ID
                .claim("tid", tenantId)                            // 租户ID
                .claim("typ", userType != null ? userType : "")    // 用户类型
                .claim("rol", roles != null ? String.join(",", roles) : "") // 角色
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // ==================== 解析 Token ====================

    /** 解析 Token 并返回 UserContext */
    public UserContext parseToken(String token) {
        Claims claims = parseClaims(token);
        log.info("JWT claims: sub={}, tid={}, typ={}, rol={}",
                claims.getSubject(), claims.get("tid"), claims.get("typ"), claims.get("rol"));
        UserContext ctx = new UserContext();
        ctx.setUserId(Long.valueOf(claims.getSubject()));
        ctx.setTenantId(claims.get("tid", Long.class));
        ctx.setUserType(claims.get("typ", String.class));
        ctx.setRoles(Arrays.asList(claims.get("rol", String.class).split(",")));
        return ctx;
    }

    /** 获取 Token 的唯一 ID（jti），用于黑名单 */
    public String getTokenId(String token) {
        return parseClaims(token).getId();
    }

    /** 获取 Token 过期时间 */
    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    // ==================== 校验 Token ====================

    /** 验证 Token 是否有效（不抛异常即有效） */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token 校验失败: {}", e.getMessage());
            return false;
        }
    }

    // ==================== 内部方法 ====================

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
