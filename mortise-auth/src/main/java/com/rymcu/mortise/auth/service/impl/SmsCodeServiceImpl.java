package com.rymcu.mortise.auth.service.impl;

import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.auth.service.SmsCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 短信验证码服务实现
 * <p>
 * 基于 Spring Cache 实现验证码的生成、存储和校验
 * </p>
 * <p>
 * 安全特性：
 * <ol>
 *   <li>验证码随机生成，使用 SecureRandom 提高安全性</li>
 *   <li>验证码自动过期（默认5分钟）</li>
 *   <li>验证成功后自动清除，防止重复使用</li>
 *   <li>发送频率限制（默认60秒内只能发送一次）</li>
 *   <li>多用户类型隔离（system、member等）</li>
 * </ol>
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/11/12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeServiceImpl implements SmsCodeService {

    /**
     * 手机号正则表达式（中国大陆）
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 6;

    /**
     * 安全随机数生成器
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 缓存管理器
     */
    private final CacheManager cacheManager;

    /**
     * 生成并发送验证码
     *
     * @param mobile 手机号
     * @param userType 用户类型
     * @return 生成的验证码（生产环境应返回null）
     */
    @Override
    public String generateAndSend(String mobile, String userType) {
        // 1. 参数校验
        validateMobile(mobile);
        validateUserType(userType);

        // 2. 检查发送频率限制
        if (!canSend(mobile, userType)) {
            log.warn("验证码发送过于频繁: mobile={}, userType={}", mobile, userType);
            throw new IllegalStateException("验证码发送过于频繁，请稍后再试");
        }

        // 3. 生成验证码
        String code = generateCode();
        log.debug("生成验证码: mobile={}, userType={}, code={}", mobile, userType, code);

        // 4. 存储验证码到缓存
        String cacheKey = buildCodeCacheKey(mobile, userType);
        Cache cache = getCodeCache();
        if (cache != null) {
            cache.put(cacheKey, code);
            log.debug("验证码已存储到缓存: key={}", cacheKey);
        } else {
            log.error("验证码缓存不可用");
            throw new IllegalStateException("验证码服务暂时不可用");
        }

        // 5. 设置发送限制标记
        String limitKey = buildLimitCacheKey(mobile, userType);
        Cache limitCache = getLimitCache();
        if (limitCache != null) {
            limitCache.put(limitKey, System.currentTimeMillis());
        }

        // 6. 发送短信（此处仅模拟，实际应调用短信服务）
        sendSms(mobile, code);

        // 7. 生产环境应返回null，测试环境可返回验证码
        return isTestEnvironment() ? code : null;
    }

    /**
     * 验证验证码
     *
     * @param mobile 手机号
     * @param code 验证码
     * @param userType 用户类型
     * @return true 验证成功，false 验证失败
     */
    @Override
    public boolean verify(String mobile, String code, String userType) {
        // 1. 参数校验
        validateMobile(mobile);
        validateUserType(userType);

        if (StringUtils.isBlank(code)) {
            log.warn("验证码为空: mobile={}", mobile);
            return false;
        }

        // 2. 从缓存中获取验证码
        String cacheKey = buildCodeCacheKey(mobile, userType);
        Cache cache = getCodeCache();
        if (cache == null) {
            log.error("验证码缓存不可用");
            return false;
        }

        Cache.ValueWrapper wrapper = cache.get(cacheKey);
        if (wrapper == null || wrapper.get() == null) {
            log.warn("验证码不存在或已过期: mobile={}, userType={}", mobile, userType);
            return false;
        }

        String cachedCode = (String) wrapper.get();

        // 3. 比对验证码（忽略大小写）
        boolean matched = code.equalsIgnoreCase(cachedCode);

        if (matched) {
            log.debug("验证码校验成功: mobile={}, userType={}", mobile, userType);
            // 验证成功后自动清除验证码
            clear(mobile, userType);
        } else {
            log.warn("验证码不匹配: mobile={}, userType={}, expected={}, actual={}",
                    mobile, userType, cachedCode, code);
        }

        return matched;
    }

    /**
     * 清除验证码
     *
     * @param mobile 手机号
     * @param userType 用户类型
     */
    @Override
    public void clear(String mobile, String userType) {
        String cacheKey = buildCodeCacheKey(mobile, userType);
        Cache cache = getCodeCache();
        if (cache != null) {
            cache.evict(cacheKey);
            log.debug("验证码已清除: key={}", cacheKey);
        }
    }

    /**
     * 检查是否可以发送验证码
     *
     * @param mobile 手机号
     * @param userType 用户类型
     * @return true 可以发送，false 发送过于频繁
     */
    @Override
    public boolean canSend(String mobile, String userType) {
        String limitKey = buildLimitCacheKey(mobile, userType);
        Cache limitCache = getLimitCache();

        if (limitCache == null) {
            return true;
        }

        Cache.ValueWrapper wrapper = limitCache.get(limitKey);
        if (wrapper == null || wrapper.get() == null) {
            return true;
        }

        Object value = wrapper.get();
        if (!(value instanceof Long)) {
            return true;
        }

        Long lastSendTime = (Long) value;
        long elapsed = System.currentTimeMillis() - lastSendTime;
        long limitMillis = TimeUnit.SECONDS.toMillis(AuthCacheConstant.SMS_CODE_SEND_LIMIT_SECONDS);

        return elapsed >= limitMillis;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成随机验证码
     *
     * @return 6位数字验证码
     */
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送短信
     * <p>
     * TODO: 集成实际的短信服务提供商（阿里云、腾讯云等）
     * </p>
     *
     * @param mobile 手机号
     * @param code 验证码
     */
    private void sendSms(String mobile, String code) {
        // 模拟发送短信
        log.info("【模拟】发送短信验证码: mobile={}, code={}", mobile, code);
        
        // 实际实现示例：
        // try {
        //     smsClient.sendCode(mobile, code);
        //     log.info("短信发送成功: mobile={}", mobile);
        // } catch (Exception e) {
        //     log.error("短信发送失败: mobile={}", mobile, e);
        //     throw new IllegalStateException("短信发送失败，请稍后重试");
        // }
    }

    /**
     * 构建验证码缓存Key
     *
     * @param mobile 手机号
     * @param userType 用户类型
     * @return 缓存key，格式: {userType}:{mobile}
     */
    private String buildCodeCacheKey(String mobile, String userType) {
        return userType + ":" + mobile;
    }

    /**
     * 构建发送限制缓存Key
     *
     * @param mobile 手机号
     * @param userType 用户类型
     * @return 缓存key
     */
    private String buildLimitCacheKey(String mobile, String userType) {
        return userType + ":" + mobile;
    }

    /**
     * 获取验证码缓存
     *
     * @return Cache 对象
     */
    private Cache getCodeCache() {
        return cacheManager.getCache(AuthCacheConstant.SMS_CODE_CACHE);
    }

    /**
     * 获取发送限制缓存
     *
     * @return Cache 对象
     */
    private Cache getLimitCache() {
        return cacheManager.getCache(AuthCacheConstant.SMS_CODE_SEND_LIMIT_CACHE);
    }

    /**
     * 校验手机号格式
     *
     * @param mobile 手机号
     * @throws IllegalArgumentException 格式不正确时抛出
     */
    private void validateMobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
    }

    /**
     * 校验用户类型
     *
     * @param userType 用户类型
     * @throws IllegalArgumentException 用户类型为空时抛出
     */
    private void validateUserType(String userType) {
        if (StringUtils.isBlank(userType)) {
            throw new IllegalArgumentException("用户类型不能为空");
        }
    }

    /**
     * 判断是否为测试环境
     * <p>
     * 可以通过 Spring Profile 或配置文件判断
     * </p>
     *
     * @return true 测试环境，false 生产环境
     */
    private boolean isTestEnvironment() {
        // TODO: 通过配置判断环境
        // return environment.acceptsProfiles(Profiles.of("dev", "test"));
        return true; // 临时返回true，便于测试
    }
}
