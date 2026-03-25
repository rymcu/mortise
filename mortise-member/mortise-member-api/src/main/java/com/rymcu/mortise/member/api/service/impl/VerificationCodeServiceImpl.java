package com.rymcu.mortise.member.api.service.impl;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.member.api.service.VerificationCodeService;
import com.rymcu.mortise.notification.entity.NotificationMessage;
import com.rymcu.mortise.notification.enums.NotificationType;
import com.rymcu.mortise.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 * <p>
 * 使用 Redis 存储验证码，默认有效期 5 分钟。
 * Email 验证码通过 {@link NotificationService} 发送（底层由 EmailNotificationSender 完成）。
 *
 * @author ronger
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private final NotificationService notificationService;

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

    /**
     * 验证码邮件模板名称
     */
    private static final String VERIFICATION_CODE_EMAIL_TEMPLATE = "verification-code-email";

    /**
     * 验证码邮件主题
     */
    private static final String VERIFICATION_CODE_EMAIL_SUBJECT = "验证码";

    @Override
    public Boolean sendSmsCode(String phone) {
        // 1. 生成验证码
        String code = generateCode(CODE_LENGTH);

        // 2. 存储到 Redis
        String redisKey = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRATION, TimeUnit.MINUTES);

        // 3. 调用 SMS 服务发送
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("expiration", CODE_EXPIRATION);

        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.SMS)
                .receiver(phone)
                .subject(VERIFICATION_CODE_EMAIL_SUBJECT)
                .content("您的验证码为：" + code + "，有效期 " + CODE_EXPIRATION + " 分钟。请勿泄露给他人。")
                .templateParams(params)
                .async(false)
                .build();

        boolean success = notificationService.send(message);
        if (success) {
            log.info("SMS 验证码发送成功: phone={}", phone);
        } else {
            log.warn("SMS 验证码发送失败（SMS 渠道可能未启用），验证码已存储到 Redis: phone={}", phone);
        }

        return true;
    }

    @Override
    public Boolean sendEmailCode(String email) {
        // 1. 生成验证码
        String code = generateCode(CODE_LENGTH);

        // 2. 存储到 Redis
        String redisKey = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRATION, TimeUnit.MINUTES);

        // 3. 通过 NotificationService 发送邮件
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("expiration", CODE_EXPIRATION);

        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.EMAIL)
                .receiver(email)
                .subject(VERIFICATION_CODE_EMAIL_SUBJECT)
                .template(VERIFICATION_CODE_EMAIL_TEMPLATE)
                .templateParams(params)
                .async(false)
                .build();

        boolean success = notificationService.send(message);
        if (!success) {
            // 发送失败时删除 Redis 中的验证码，避免用户收不到验证码却被要求输入
            redisTemplate.delete(redisKey);
            throw new BusinessException("验证码邮件发送失败，请稍后重试");
        }

        log.info("Email 验证码发送成功: email={}", email);
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
            log.warn("SMS 验证码验证失败: phone={}", phone);
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
            log.warn("Email 验证码验证失败: email={}", email);
        }

        return isValid;
    }

    @Override
    public String generateCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
