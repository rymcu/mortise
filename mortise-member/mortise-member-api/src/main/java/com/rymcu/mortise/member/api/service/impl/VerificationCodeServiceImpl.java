package com.rymcu.mortise.member.api.service.impl;

import com.rymcu.mortise.member.api.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 * <p>
 * 使用 Redis 存储验证码，默认有效期 5 分钟
 *
 * @author ronger
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 6;

    /**
     * 验证码有效期（分钟）
     */
    private static final int CODE_EXPIRATION = 5;

    /**
     * SMS 验证码 Redis Key 前缀
     */
    private static final String SMS_CODE_PREFIX = "api:sms:code:";

    /**
     * Email 验证码 Redis Key 前缀
     */
    private static final String EMAIL_CODE_PREFIX = "api:email:code:";

    @Override
    public Boolean sendSmsCode(String phone) {
        // 1. 生成验证码
        String code = generateCode(CODE_LENGTH);

        // 2. 存储到 Redis
        String redisKey = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRATION, TimeUnit.MINUTES);

        // 3. 调用 SMS 服务发送
        // TODO: 集成阿里云/腾讯云 SMS 服务
        log.info("发送 SMS 验证码: phone={}, code={} (有效期: {} 分钟)", phone, code, CODE_EXPIRATION);

        // 临时方案：打印到日志（生产环境应删除）
        log.warn("【开发模式】SMS 验证码: {} -> {}", phone, code);

        return true;
    }

    @Override
    public Boolean sendEmailCode(String email) {
        // 1. 生成验证码
        String code = generateCode(CODE_LENGTH);

        // 2. 存储到 Redis
        String redisKey = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRATION, TimeUnit.MINUTES);

        // 3. 调用 Email 服务发送
        // TODO: 使用 mortise-notification 模块发送邮件
        log.info("发送 Email 验证码: email={}, code={} (有效期: {} 分钟)", email, code, CODE_EXPIRATION);

        // 临时方案：打印到日志（生产环境应删除）
        log.warn("【开发模式】Email 验证码: {} -> {}", email, code);

        return true;
    }

    @Override
    public Boolean verifySmsCode(String phone, String code) {
        String redisKey = SMS_CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            log.warn("SMS 验证码不存在或已过期: phone={}", phone);
            return false;
        }

        boolean isValid = storedCode.equals(code);
        if (isValid) {
            // 验证成功后删除验证码（防止重复使用）
            redisTemplate.delete(redisKey);
            log.info("SMS 验证码验证成功: phone={}", phone);
        } else {
            log.warn("SMS 验证码验证失败: phone={}, expected={}, actual={}", phone, storedCode, code);
        }

        return isValid;
    }

    @Override
    public Boolean verifyEmailCode(String email, String code) {
        String redisKey = EMAIL_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            log.warn("Email 验证码不存在或已过期: email={}", email);
            return false;
        }

        boolean isValid = storedCode.equals(code);
        if (isValid) {
            // 验证成功后删除验证码（防止重复使用）
            redisTemplate.delete(redisKey);
            log.info("Email 验证码验证成功: email={}", email);
        } else {
            log.warn("Email 验证码验证失败: email={}, expected={}, actual={}", email, storedCode, code);
        }

        return isValid;
    }

    @Override
    public String generateCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
