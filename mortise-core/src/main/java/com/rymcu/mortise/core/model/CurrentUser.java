package com.rymcu.mortise.core.model;

/**
 * 当前登录用户接口
 * <p>
 * 此接口定义了获取当前登录用户基本信息的标准方法。
 * 各模块可以有自己的实现，避免模块之间的直接依赖。
 * </p>
 * 
 * <p><b>设计原则：</b></p>
 * <ul>
 *   <li>依赖倒置原则（DIP）：业务模块依赖接口而不是具体实现</li>
 *   <li>接口隔离原则（ISP）：只定义必要的方法</li>
 *   <li>开闭原则（OCP）：对扩展开放，对修改关闭</li>
 * </ul>
 * 
 * <p><b>使用场景：</b></p>
 * <ul>
 *   <li>自动填充审计字段（createdBy, updatedBy）</li>
 *   <li>权限验证</li>
 *   <li>业务日志记录</li>
 *   <li>数据权限过滤</li>
 * </ul>
 * 
 * <p><b>实现示例：</b></p>
 * <pre>
 * public class UserDetailInfo implements UserDetails, CurrentUser {
 *     private User user;
 *     
 *     &#64;Override
 *     public Long getUserId() {
 *         return user != null ? user.getId() : null;
 *     }
 *     
 *     &#64;Override
 *     public String getUsername() {
 *         return user != null ? user.getAccount() : null;
 *     }
 *     
 *     // ... 其他方法实现
 * }
 * </pre>
 * 
 * <p><b>使用示例：</b></p>
 * <pre>
 * Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 * if (authentication.getPrincipal() instanceof CurrentUser currentUser) {
 *     Long userId = currentUser.getUserId();
 *     String username = currentUser.getUsername();
 * }
 * </pre>
 *
 * @author AI Assistant
 * @since 0.0.1
 * @see org.springframework.security.core.Authentication
 */
public interface CurrentUser {

    /**
     * 获取用户ID
     * <p>
     * 用户的唯一标识符，通常用于：
     * <ul>
     *   <li>数据库关联查询</li>
     *   <li>审计字段填充（createdBy, updatedBy）</li>
     *   <li>权限验证</li>
     *   <li>业务逻辑处理</li>
     * </ul>
     * </p>
     *
     * @return 用户ID，如果用户未登录或用户信息不完整则返回 null
     */
    Long getUserId();

    /**
     * 获取用户账号（登录名）
     * <p>
     * 用户的登录账号，通常用于：
     * <ul>
     *   <li>显示当前登录用户</li>
     *   <li>操作日志记录</li>
     *   <li>安全审计</li>
     * </ul>
     * </p>
     *
     * @return 用户账号，如果用户未登录或用户信息不完整则返回 null
     */
    String getUsername();

    /**
     * 获取用户昵称（显示名称）
     * <p>
     * 用户的显示名称，通常用于：
     * <ul>
     *   <li>UI 界面显示</li>
     *   <li>用户友好的提示信息</li>
     *   <li>通知消息</li>
     * </ul>
     * </p>
     *
     * @return 用户昵称，如果用户未登录或用户信息不完整则返回 null
     */
    String getNickname();

    /**
     * 判断用户是否已认证
     * <p>
     * 判断当前用户是否已通过身份验证，通常用于：
     * <ul>
     *   <li>权限检查</li>
     *   <li>安全控制</li>
     *   <li>业务流程判断</li>
     * </ul>
     * </p>
     * 
     * <p><b>注意：</b></p>
     * <ul>
     *   <li>已认证不等于有权限，需要额外的权限检查</li>
     *   <li>匿名用户也可能返回 true（取决于具体实现）</li>
     * </ul>
     *
     * @return true 表示用户已认证，false 表示未认证
     */
    boolean isAuthenticated();

    /**
     * 获取用户邮箱
     * <p>
     * 用户的电子邮箱地址，通常用于：
     * <ul>
     *   <li>发送通知邮件</li>
     *   <li>密码重置</li>
     *   <li>账号验证</li>
     * </ul>
     * </p>
     * 
     * <p><b>默认实现：</b>返回 null，子类可以覆盖此方法提供实际值</p>
     *
     * @return 用户邮箱，如果未设置则返回 null
     */
    default String getEmail() {
        return null;
    }

    /**
     * 获取用户手机号
     * <p>
     * 用户的手机号码，通常用于：
     * <ul>
     *   <li>发送短信通知</li>
     *   <li>双因素认证</li>
     *   <li>账号验证</li>
     * </ul>
     * </p>
     * 
     * <p><b>默认实现：</b>返回 null，子类可以覆盖此方法提供实际值</p>
     *
     * @return 用户手机号，如果未设置则返回 null
     */
    default String getPhone() {
        return null;
    }

    /**
     * 获取用户头像URL
     * <p>
     * 用户的头像图片地址，通常用于：
     * <ul>
     *   <li>UI 界面显示</li>
     *   <li>用户信息展示</li>
     *   <li>评论、消息等场景</li>
     * </ul>
     * </p>
     * 
     * <p><b>默认实现：</b>返回 null，子类可以覆盖此方法提供实际值</p>
     *
     * @return 用户头像URL，如果未设置则返回 null
     */
    default String getAvatarUrl() {
        return null;
    }
}
