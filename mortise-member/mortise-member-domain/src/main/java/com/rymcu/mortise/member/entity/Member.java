package com.rymcu.mortise.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.rymcu.mortise.persistence.mybatis.handler.JsonbTypeHandler;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 会员实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_member", schema = "mortise")
public class Member implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 用户名
     */
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名必须是4-20位字母、数字或下划线")
    private String username;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 性别：male-男, female-女, other-其他
     */
    private String gender;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * 状态：0-正常, 1-禁用
     */
    private Integer status;

    /**
     * 会员等级
     */
    private String memberLevel;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 注册来源
     */
    private String registerSource;

    /**
     * 推荐人ID
     */
    private Long referrerId;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 邮箱验证时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime emailVerifiedTime;

    /**
     * 手机号验证时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime phoneVerifiedTime;

    /**
     * 扩展资料
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> profile;

    /**
     * 个人偏好设置
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> preferences;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 删除标记：0-未删除, 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer delFlag;
}
