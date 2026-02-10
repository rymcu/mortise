package com.rymcu.mortise.member.api.service;

/**
 * 验证码服务
 * <p>
 * 支持 SMS 和 Email 验证码的发送、存储和验证
 *
 * @author ronger
 */
public interface VerificationCodeService {

    /**
     * 发送 SMS 验证码
     *
     * @param phone 手机号
     * @return 是否成功
     */
    Boolean sendSmsCode(String phone);

    /**
     * 发送 Email 验证码
     *
     * @param email 邮箱
     * @return 是否成功
     */
    Boolean sendEmailCode(String email);

    /**
     * 验证 SMS 验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否验证成功
     */
    Boolean verifySmsCode(String phone, String code);

    /**
     * 验证 Email 验证码
     *
     * @param email 邮箱
     * @param code  验证码
     * @return 是否验证成功
     */
    Boolean verifyEmailCode(String email, String code);

    /**
     * 生成验证码
     *
     * @param length 长度
     * @return 验证码
     */
    String generateCode(int length);
}
