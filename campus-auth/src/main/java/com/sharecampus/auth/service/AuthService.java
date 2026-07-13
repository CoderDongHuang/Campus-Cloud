package com.sharecampus.auth.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.auth.entity.SysUser;
import com.sharecampus.auth.mapper.SysUserMapper;
import com.sharecampus.common.core.exception.BizException;
import com.sharecampus.common.core.exception.ErrorCode;
import com.sharecampus.common.security.JwtTokenProvider;
import com.sharecampus.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==================== 登录 ====================

    /** 密码登录 */
    public Map<String, String> loginByPassword(String phone, String password) {
        // 1. 查用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        // 2. 验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(ErrorCode.PASSWORD_ERROR);
        }
        // 3. 验状态
        if (user.getStatus() == 0) {
            throw new BizException(ErrorCode.USER_STATUS_DISABLED);
        }
        // 4. 生成 Token
        return buildTokens(user);
    }

    /** 验证码登录 */
    public Map<String, String> loginBySms(String phone, String code) {
        // 1. 验验证码
        String cachedCode = redisTemplate.opsForValue().get("sms:code:" + phone);
        if (StrUtil.isEmpty(cachedCode) || !cachedCode.equals(code)) {
            throw new BizException(ErrorCode.SMS_CODE_ERROR);
        }
        redisTemplate.delete("sms:code:" + phone); // 用后即删

        // 2. 查用户，不存在则自动注册
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (user == null) {
            user = new SysUser();
            user.setPhone(phone);
            user.setNickname("用户" + phone.substring(7));
            user.setUserType("STUDENT");
            user.setStatus(1);
            userMapper.insert(user);
        }
        // 3. 生成 Token
        return buildTokens(user);
    }

    // ==================== 注册 ====================

    public void register(String phone, String password, String code) {
        // 验证码校验已由前端图形验证码完成，后端不再校验SMS
        // 检查手机号是否已注册
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (count > 0) {
            throw new BizException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        // 3. 创建用户
        SysUser user = new SysUser();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname("用户" + phone.substring(7));
        user.setUserType("STUDENT");
        user.setStatus(1);
        userMapper.insert(user);

        redisTemplate.delete("sms:code:" + phone);
        log.info("用户注册成功: phone={}", phone);
    }

    // ==================== Token ====================

    /** 刷新 Token */
    public Map<String, String> refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        UserContext context = jwtTokenProvider.parseToken(refreshToken);
        return Map.of(
                "accessToken", jwtTokenProvider.createAccessToken(
                        context.getUserId(), context.getTenantId(),
                        context.getUserType(), context.getRoles()),
                "refreshToken", jwtTokenProvider.createRefreshToken(
                        context.getUserId(), context.getTenantId())
        );
    }

    /** 登出（Token 加入 Redis 黑名单） */
    public void logout(String token) {
        try {
            String jti = jwtTokenProvider.getTokenId(token);
            java.util.Date expiration = jwtTokenProvider.getExpiration(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set("token:blacklist:" + jti,
                        "1", Duration.ofMillis(ttl));
            }
        } catch (Exception e) {
            log.warn("登出 Token 处理失败: {}", e.getMessage());
        }
    }

    // ==================== 验证码 ====================

    public void sendSmsCode(String phone) {
        // 限流：60 秒内不重复发
        String rateKey = "sms:rate:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey))) {
            throw new BizException(ErrorCode.RATE_LIMIT);
        }
        String code = RandomUtil.randomNumbers(6);
        redisTemplate.opsForValue().set("sms:code:" + phone, code, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(rateKey, "1", 60, TimeUnit.SECONDS);
        log.info("验证码已发送: phone={}, code={}", phone, code); // 开发环境打印，生产接短信服务
    }

    // ==================== 内部方法 ====================

    private Map<String, String> buildTokens(SysUser user) {
        List<String> roles = Collections.singletonList(user.getUserType());
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getTenantId(), user.getUserType(), roles);
        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getId(), user.getTenantId());

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        Map<String, String> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("userId", String.valueOf(user.getId()));
        result.put("nickname", user.getNickname());
        result.put("userType", user.getUserType());
        return result;
    }
}
