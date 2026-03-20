package com.rymcu.mortise.member.api.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.cache.service.CacheService;
import com.rymcu.mortise.common.enumerate.DelFlag;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.member.api.service.ApiMemberService;
import com.rymcu.mortise.member.api.service.VerificationCodeService;
import com.rymcu.mortise.member.constant.OAuth2UserAttributeKeys;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.enumerate.MemberLevel;
import com.rymcu.mortise.member.service.impl.MemberServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.rymcu.mortise.member.entity.table.MemberTableDef.MEMBER;

/**
 * 客户端会员服务实现
 * <p>
 * 继承 member 模块的基础实现，扩展客户端特定的业务逻辑
 *
 * @author ronger
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiMemberServiceImpl extends MemberServiceImpl implements ApiMemberService {

    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;
    private final CacheService cacheService;

    private static final String DASHBOARD_MEMBER_COUNT = "dashboard:member:count";
    private static final long CACHE_EXPIRE_HOURS = 1;

    @Override
    public Member findByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        return getOne(QueryWrapper.create()
                .where(MEMBER.USERNAME.eq(username))
                .and(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
    }

    @Override
    public Member findByEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        return getOne(QueryWrapper.create()
                .where(MEMBER.EMAIL.eq(email))
                .and(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
    }

    @Override
    public Member findByPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return null;
        }
        return getOne(QueryWrapper.create()
                .where(MEMBER.PHONE.eq(phone))
                .and(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(Member member, String password) {
        // 验证用户名、邮箱、手机号是否已存在
        if (StringUtils.isNotBlank(member.getUsername()) && findByUsername(member.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (StringUtils.isNotBlank(member.getEmail()) && findByEmail(member.getEmail()) != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        if (StringUtils.isNotBlank(member.getPhone()) && findByPhone(member.getPhone()) != null) {
            throw new IllegalArgumentException("手机号已被注册");
        }

        // 加密密码
        member.setPasswordHash(passwordEncoder.encode(password));
        member.setCreatedTime(LocalDateTime.now());
        member.setUpdatedTime(LocalDateTime.now());
        member.setStatus(Status.ENABLED.getCode());
        member.setMemberLevel(MemberLevel.DEFAULT_LEVEL_CODE);
        member.setPoints(0);
        member.setDelFlag(DelFlag.NORMAL.ordinal());

        // 使用继承自 ServiceImpl 的 save 方法
        save(member);
        // 更新会员数缓存
        updateMemberCountCache();
        return member.getId();
    }

    @Override
    public Member login(String account, String password) {
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("账号或密码不能为空");
        }

        // 尝试通过用户名、邮箱或手机号查找
        Member member = findByUsername(account);
        if (member == null) {
            member = findByEmail(account);
        }
        if (member == null) {
            member = findByPhone(account);
        }

        if (member == null) {
            throw new IllegalArgumentException("账号不存在");
        }

        if (member.getStatus() != Status.ENABLED.getCode()) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, member.getPasswordHash())) {
            throw new IllegalArgumentException("密码错误");
        }

        // 更新最后登录时间
        updateLastLoginTime(member.getId());

        // 清除密码哈希再返回
        member.setPasswordHash(null);
        return member;
    }

    @Override
    public Member loginByPhone(String phone, String code) {
        if (StringUtils.isBlank(phone)) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (StringUtils.isBlank(code)) {
            throw new IllegalArgumentException("验证码不能为空");
        }

        // 验证验证码
        if (!verificationCodeService.verifySmsCode(phone, code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        // 查找会员
        Member member = findByPhone(phone);
        if (member == null) {
            throw new IllegalArgumentException("手机号未注册");
        }

        if (member.getStatus() != Status.ENABLED.getCode()) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        // 更新最后登录时间
        updateLastLoginTime(member.getId());

        // 清除密码哈希再返回
        member.setPasswordHash(null);
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMember(Member member) {
        member.setUpdatedTime(LocalDateTime.now());
        return updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePassword(Long memberId, String oldPassword, String newPassword) {
        Member member = getById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        if (StringUtils.isNotBlank(member.getPasswordHash())) {
            if (!passwordEncoder.matches(oldPassword, member.getPasswordHash())) {
                throw new IllegalArgumentException("旧密码错误");
            }
        }

        member.setPasswordHash(passwordEncoder.encode(newPassword));
        member.setUpdatedTime(LocalDateTime.now());
        return updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(String account, String newPassword, String verificationCode) {
        // 注意：验证码校验已在 Controller 层完成，此处直接执行密码重置
        Member member = findByUsername(account);
        if (member == null) {
            member = findByEmail(account);
        }
        if (member == null) {
            member = findByPhone(account);
        }

        if (member == null) {
            throw new IllegalArgumentException("账号不存在");
        }

        member.setPasswordHash(passwordEncoder.encode(newPassword));
        member.setUpdatedTime(LocalDateTime.now());
        return updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean verifyEmail(Long memberId, String code) {
        Member member = getById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        String email = member.getEmail();
        if (StringUtils.isBlank(email)) {
            throw new IllegalArgumentException("该会员未绑定邮箱");
        }

        if (!verificationCodeService.verifyEmailCode(email, code)) {
            throw new IllegalArgumentException("邮箱验证码错误或已过期");
        }

        member.setEmailVerifiedTime(LocalDateTime.now());
        member.setUpdatedTime(LocalDateTime.now());
        return updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean verifyPhone(Long memberId, String code) {
        Member member = getById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        String phone = member.getPhone();
        if (StringUtils.isBlank(phone)) {
            throw new IllegalArgumentException("该会员未绑定手机号");
        }

        if (!verificationCodeService.verifySmsCode(phone, code)) {
            throw new IllegalArgumentException("手机验证码错误或已过期");
        }

        member.setPhoneVerifiedTime(LocalDateTime.now());
        member.setUpdatedTime(LocalDateTime.now());
        return updateById(member);
    }

    @Override
    public void updateLastLoginTime(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        member.setLastLoginTime(LocalDateTime.now());
        member.setUpdatedTime(LocalDateTime.now());
        updateById(member);
    }

    @Override
    @Deprecated
    public Page<Member> findMemberList(Page<Member> page, Integer status, String memberLevel, String keyword) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()));

        if (status != null) {
            queryWrapper.and(MEMBER.STATUS.eq(status));
        }

        if (StringUtils.isNotBlank(memberLevel)) {
            queryWrapper.and(MEMBER.MEMBER_LEVEL.eq(memberLevel));
        }

        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(MEMBER.USERNAME.like(keyword)
                    .or(MEMBER.EMAIL.like(keyword))
                    .or(MEMBER.PHONE.like(keyword))
                    .or(MEMBER.NICKNAME.like(keyword)));
        }

        queryWrapper.orderBy(MEMBER.CREATED_TIME.desc());

        return mapper.paginate(page, queryWrapper);
    }

    @Override
    @Transactional
    public Long createMemberFromOAuth2(Member member) {
        log.debug("从 OAuth2 用户信息创建会员: nickname={}", member.getNickname());

        // 确保有用户名，如果没有则生成一个
        if (StringUtils.isBlank(member.getUsername())) {
            String generatedUsername = generateUniqueUsername();
            member.setUsername(generatedUsername);
            log.debug("生成用户名: {}", generatedUsername);
        }

        // 设置默认状态
        if (member.getStatus() == null) {
            member.setStatus(Status.ENABLED.getCode());
        }

        // 设置其他默认值
        member.setDelFlag(DelFlag.NORMAL.ordinal());
        member.setCreatedTime(LocalDateTime.now());

        // 保存会员
        save(member);
        // 更新会员数缓存
        updateMemberCountCache();

        log.info("从 OAuth2 用户创建会员成功: memberId={}, username={}", member.getId(), member.getUsername());
        return member.getId();
    }

    @Override
    public Member getMemberById(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return getById(memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUsername(Long memberId, String newUsername) {
        // 参数校验
        if (memberId == null) {
            throw new IllegalArgumentException("会员ID 不能为空");
        }
        if (StringUtils.isBlank(newUsername)) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        // 格式校验
        if (!newUsername.matches("^[a-zA-Z0-9_]{8,20}$")) {
            throw new IllegalArgumentException("用户名必须是8-20位字母、数字或下划线");
        }

        // 检查会员是否存在
        Member member = getById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        // 检查会员状态
        if (member.getStatus() != Status.ENABLED.getCode()) {
            throw new IllegalArgumentException("账号已被禁用，无法修改用户名");
        }

        // 检查是否和当前用户名相同
        if (newUsername.equals(member.getUsername())) {
            throw new IllegalArgumentException("新用户名与当前用户名相同");
        }

        // 检查新用户名是否已被占用
        Member existingMember = findByUsername(newUsername);
        if (existingMember != null) {
            throw new IllegalArgumentException("用户名已被占用");
        }

        // 更新用户名
        String oldUsername = member.getUsername();
        member.setUsername(newUsername);
        member.setUpdatedTime(LocalDateTime.now());
        boolean result = updateById(member);

        if (result) {
            log.info("用户名修改成功: memberId={}, oldUsername={}, newUsername={}",
                     memberId, oldUsername, newUsername);
        } else {
            log.warn("用户名修改失败: memberId={}, newUsername={}", memberId, newUsername);
        }

        return result;
    }

    /**
     * 生成唯一的用户名
     * <p>
     * 格式：user_${timestamp}_${randomCode}
     */
    private String generateUniqueUsername() {
        String prefix = OAuth2UserAttributeKeys.OAUTH2_USERNAME_PREFIX;
        String suffix = System.currentTimeMillis() + "_" +
                       (int) (Math.random() * 10000);
        String username = prefix + suffix;

        // 检查用户名是否已存在
        while (findByUsername(username) != null) {
            suffix = System.currentTimeMillis() + "_" +
                    (int) (Math.random() * 10000);
            username = prefix + suffix;
        }

        return username;
    }

    /**
     * 更新会员数缓存
     */
    private void updateMemberCountCache() {
        try {
            long count = count();
            cacheService.set(DASHBOARD_MEMBER_COUNT, "value", count, Duration.ofHours(CACHE_EXPIRE_HOURS));
            log.debug("更新会员数缓存: count={}", count);
        } catch (Exception e) {
            log.warn("更新会员数缓存失败: {}", e.getMessage());
        }
    }
}
