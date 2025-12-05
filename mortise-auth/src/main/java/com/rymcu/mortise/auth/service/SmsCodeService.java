package com.rymcu.mortise.auth.service;

/**
 * 短信验证码服务接口
 * <p>
 * 负责验证码的生成、存储、校验和清理
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/11/12
 */
public interface SmsCodeService {

    /**
     * 生成并发送验证码
     * <p>
     * 生成验证码后，会存储到缓存中，并调用短信服务发送
     * </p>
     *
     * @param mobile 手机号
     * @param userType 用户类型（如 "system", "member"）
     * @return 生成的验证码（仅用于测试环境返回，生产环境应返回null）
     * @throws IllegalArgumentException 手机号格式不正确
     * @throws IllegalStateException 验证码发送过于频繁
     */
    String generateAndSend(String mobile, String userType);

    /**
     * 验证验证码
     * <p>
     * 校验成功后会自动清除验证码，防止重复使用
     * </p>
     *
     * @param mobile 手机号
     * @param code 验证码
     * @param userType 用户类型
     * @return true 验证成功，false 验证失败
     */
    boolean verify(String mobile, String code, String userType);

    /**
     * 清除验证码
     * <p>
     * 用于认证成功或失败后清理缓存
     * </p>
     *
     * @param mobile 手机号
     * @param userType 用户类型
     */
    void clear(String mobile, String userType);

    /**
     * 检查是否可以发送验证码
     * <p>
     * 防止验证码发送过于频繁（例如60秒内只能发送一次）
     * </p>
     *
     * @param mobile 手机号
     * @param userType 用户类型
     * @return true 可以发送，false 发送过于频繁
     */
    boolean canSend(String mobile, String userType);
}
