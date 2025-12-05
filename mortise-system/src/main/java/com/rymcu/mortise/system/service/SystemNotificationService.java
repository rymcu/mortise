package com.rymcu.mortise.system.service;

import java.util.Map;

/**
 * 系统通知服务接口
 * 业务层封装 - 封装基础设施层的 NotificationService
 *
 * 设计原则：
 * - 业务模块不直接调用 NotificationService
 * - 通过 SystemNotificationService 提供业务语义化的通知操作
 * - 内部使用 NotificationService 实现
 *
 * @author ronger
 */
public interface SystemNotificationService {

    /**
     * 发送注册欢迎邮件
     *
     * @param email 邮箱
     * @param username 用户名
     * @return 是否发送成功
     */
    boolean sendWelcomeEmail(String email, String username);

    /**
     * 发送验证码邮件
     *
     * @param email 邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    boolean sendVerificationCodeEmail(String email, String code);

    /**
     * 发送密码重置邮件
     *
     * @param email 邮箱
     * @param resetToken 重置令牌
     * @return 是否发送成功
     */
    boolean sendPasswordResetEmail(String email, String resetToken);

    /**
     * 发送账号激活邮件
     *
     * @param email 邮箱
     * @param activationToken 激活令牌
     * @return 是否发送成功
     */
    boolean sendAccountActivationEmail(String email, String activationToken);

    /**
     * 发送系统通知邮件
     *
     * @param email 邮箱
     * @param subject 主题
     * @param content 内容
     * @return 是否发送成功
     */
    boolean sendSystemNotificationEmail(String email, String subject, String content);

    /**
     * 使用模板发送邮件
     *
     * @param email 邮箱
     * @param subject 主题
     * @param templateName 模板名称
     * @param templateParams 模板参数
     * @return 是否发送成功
     */
    boolean sendTemplateEmail(String email, String subject, String templateName, Map<String, Object> templateParams);

    /**
     * 异步发送邮件通知
     *
     * @param email 邮箱
     * @param subject 主题
     * @param content 内容
     */
    void sendEmailAsync(String email, String subject, String content);

    /**
     * 发送站内消息
     *
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @return 是否发送成功
     */
    boolean sendSystemMessage(Long userId, String title, String content);
}
