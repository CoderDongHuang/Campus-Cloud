package com.sharecampus.auth.service;

import com.sharecampus.common.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auth 核心业务逻辑单元测试
 */
class AuthServiceTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtTokenProvider jwtProvider = new JwtTokenProvider();

    @Test
    void passwordEncodeAndMatch() {
        String raw = "test123456";
        String encoded = encoder.encode(raw);
        assertNotEquals(raw, encoded);
        assertTrue(encoder.matches(raw, encoded));
        assertFalse(encoder.matches("wrong", encoded));
    }

    @Test
    void jwtCreateAndValidate() {
        String token = jwtProvider.createAccessToken(1L, 0L, "STUDENT", List.of("STUDENT"));
        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void jwtInvalidToken() {
        assertFalse(jwtProvider.validateToken("invalid.token.here"));
        assertFalse(jwtProvider.validateToken(""));
        assertFalse(jwtProvider.validateToken(null));
    }

    @Test
    void jwtParseUserContext() {
        String token = jwtProvider.createAccessToken(2073005678401105922L, 0L, "STUDENT", List.of("STUDENT"));
        var ctx = jwtProvider.parseToken(token);
        assertEquals(2073005678401105922L, ctx.getUserId());
        assertEquals(0L, ctx.getTenantId());
        assertEquals("STUDENT", ctx.getUserType());
    }

    @Test
    void jwtRefreshToken() {
        String refresh = jwtProvider.createRefreshToken(1L, 0L);
        assertNotNull(refresh);
        assertTrue(jwtProvider.validateToken(refresh));
    }
}
