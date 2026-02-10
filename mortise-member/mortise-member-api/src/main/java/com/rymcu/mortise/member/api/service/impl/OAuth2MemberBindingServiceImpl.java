package com.rymcu.mortise.member.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import com.rymcu.mortise.auth.util.OAuth2ProviderUtils;
import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.member.api.model.OAuth2LoginResponse;
import com.rymcu.mortise.member.api.service.ApiMemberService;
import com.rymcu.mortise.member.api.service.OAuth2MemberBindingService;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.entity.MemberOAuth2Binding;
import com.rymcu.mortise.member.service.MemberOAuth2BindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 会员绑定和登录服务实现
 *
 * 与 mortise-system 中的 OAuth2 实现协同工作，但针对 API 端（客户端）的需求进行了优化
 *
 * @author ronger
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2MemberBindingServiceImpl implements OAuth2MemberBindingService {

    private final ApiMemberService memberService;
    private final MemberOAuth2BindingService memberOAuth2BindingService;
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;
    private final AuthCacheService authCacheService;

    @Override
    @Transactional
    public OAuth2LoginResponse bindOrLoginWithOAuth2User(OAuth2User oauth2User, String providerType, String registrationId) {
        log.debug("处理 OAuth2 用户登录或绑定: providerType={}, registrationId={}", providerType, registrationId);

        if (oauth2User == null) {
            log.error("OAuth2User 为 null");
            throw new IllegalArgumentException("OAuth2User cannot be null");
        }

        // 从 OAuth2User 中提取标准信息
        String openid = extractOpenid(oauth2User, providerType);
        String unionid = extractUnionid(oauth2User, providerType);
        String nickname = extractNickname(oauth2User, providerType);
        String avatarUrl = extractAvatarUrl(oauth2User, providerType);

        log.debug("提取的 OAuth2 用户信息: openid={}, unionid={}, nickname={}", openid, unionid, nickname);

        // 1. 尝试用 unionid 查找会员（优先使用 unionid，因为跨不同的微信应用）
        if (unionid != null) {
            OAuth2LoginResponse response = findMemberByUnionid(unionid, providerType);
            if (response != null) {
                log.info("找到已绑定的会员: unionid={}, memberId={}", unionid, response.memberId());
                return response;
            }
        }

        // 2. 尝试用 openid 查找会员
        if (openid != null) {
            OAuth2LoginResponse response = findMemberByOpenid(openid, providerType);
            if (response != null) {
                log.info("找到已绑定的会员: openid={}, memberId={}", openid, response.memberId());
                return response;
            }
        }

        // 3. 如果没找到，创建新会员并绑定
        log.info("未找到已绑定的会员，创建新会员: openid={}, unionid={}", openid, unionid);
        return createMemberAndBind(oauth2User, providerType, registrationId);
    }

    @Override
    public OAuth2LoginResponse findMemberByOpenid(String openid, String providerType) {
        log.debug("根据 openid 查找会员: openid={}, providerType={}", openid, providerType);

        // 从数据库查询会员 OAuth2 关联表
        MemberOAuth2Binding binding = memberOAuth2BindingService.findByProviderAndOpenId(providerType, openid);
        if (binding == null) {
            log.debug("未找到 openid 对应的绑定关系: openid={}, providerType={}", openid, providerType);
            return null;
        }

        // 获取会员信息
        Member member = memberService.getMemberById(binding.getMemberId());
        if (member == null) {
            log.warn("绑定关系存在但会员不存在: memberId={}", binding.getMemberId());
            return null;
        }

        return buildLoginResponse(member, binding.getOpenId(), binding.getUnionId(),
                binding.getNickname(), binding.getAvatar(), false, true);
    }

    @Override
    public OAuth2LoginResponse findMemberByUnionid(String unionid, String providerType) {
        log.debug("根据 unionid 查找会员: unionid={}, providerType={}", unionid, providerType);

        // 从数据库查询会员 OAuth2 关联表
        MemberOAuth2Binding binding = memberOAuth2BindingService.findByProviderAndUnionId(providerType, unionid);
        if (binding == null) {
            log.debug("未找到 unionid 对应的绑定关系: unionid={}, providerType={}", unionid, providerType);
            return null;
        }

        // 获取会员信息
        Member member = memberService.getMemberById(binding.getMemberId());
        if (member == null) {
            log.warn("绑定关系存在但会员不存在: memberId={}", binding.getMemberId());
            return null;
        }

        return buildLoginResponse(member, binding.getOpenId(), binding.getUnionId(),
                binding.getNickname(), binding.getAvatar(), false, true);
    }

    @Override
    @Transactional
    public OAuth2LoginResponse createMemberAndBind(OAuth2User oauth2User, String providerType, String registrationId) {
        log.info("创建新会员并绑定 OAuth2 账号: providerType={}", providerType);

        String nickname = extractNickname(oauth2User, providerType);
        String openid = extractOpenid(oauth2User, providerType);
        String unionid = extractUnionid(oauth2User, providerType);
        String avatarUrl = extractAvatarUrl(oauth2User, providerType);

        // 创建新会员
        Member member = new Member();
        member.setNickname(nickname != null ? nickname : "WeChat_User_" + openid);
        member.setAvatarUrl(avatarUrl);
        // 其他字段可根据需要设置

        Long memberId = memberService.createMemberFromOAuth2(member);
        log.info("新会员创建成功: memberId={}", memberId);
        member.setId(memberId);

        // 绑定 OAuth2 账号
        MemberOAuth2Binding binding = createBindingFromOAuth2User(memberId, oauth2User, providerType);
        memberOAuth2BindingService.createBinding(binding);
        log.info("OAuth2 绑定创建成功: memberId={}, openid={}", memberId, openid);

        // 生成登录响应
        return buildLoginResponse(member, openid, unionid, nickname, avatarUrl, true, true);
    }

    @Override
    @Transactional
    public OAuth2LoginResponse bindMemberToOAuth2(Long memberId, OAuth2User oauth2User, String providerType, String registrationId) {
        log.info("为会员绑定 OAuth2 账号: memberId={}, providerType={}", memberId, providerType);

        Member member = memberService.getMemberById(memberId);
        if (member == null) {
            log.error("会员不存在: memberId={}", memberId);
            throw new IllegalArgumentException("Member not found: " + memberId);
        }

        String openid = extractOpenid(oauth2User, providerType);
        String unionid = extractUnionid(oauth2User, providerType);
        String nickname = extractNickname(oauth2User, providerType);
        String avatarUrl = extractAvatarUrl(oauth2User, providerType);

        // 检查是否已存在绑定关系
        MemberOAuth2Binding existingBinding = memberOAuth2BindingService.findByMemberIdAndProvider(memberId, providerType);
        if (existingBinding != null) {
            log.warn("会员已绑定该 OAuth2 账号: memberId={}, provider={}", memberId, providerType);
            // 更新现有绑定
            updateBindingFromOAuth2User(existingBinding, oauth2User, providerType);
            memberOAuth2BindingService.updateBinding(existingBinding);
        } else {
            // 创建新绑定
            MemberOAuth2Binding binding = createBindingFromOAuth2User(memberId, oauth2User, providerType);
            memberOAuth2BindingService.createBinding(binding);
        }

        log.info("OAuth2 账号绑定成功: memberId={}, openid={}", memberId, openid);

        return buildLoginResponse(member, openid, unionid, nickname, avatarUrl, false, true);
    }

    @Override
    @Transactional
    public Boolean updateMemberFromOAuth2User(Long memberId, OAuth2User oauth2User) {
        log.debug("更新会员的 OAuth2 用户信息: memberId={}", memberId);

        Member member = memberService.getMemberById(memberId);
        if (member == null) {
            log.error("会员不存在: memberId={}", memberId);
            return false;
        }

        String nickname = extractNickname(oauth2User, OAuth2ProviderUtils.PROVIDER_WECHAT);
        String avatarUrl = extractAvatarUrl(oauth2User, OAuth2ProviderUtils.PROVIDER_WECHAT);

        if (nickname != null) {
            member.setNickname(nickname);
        }
        if (avatarUrl != null) {
            member.setAvatarUrl(avatarUrl);
        }

        return memberService.updateMember(member);
    }

    /**
     * 从 OAuth2User 中提取 openid
     */
    private String extractOpenid(OAuth2User oauth2User, String providerType) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        if (OAuth2ProviderUtils.isWeChatProviderType(providerType)) {
            // 微信返回的属性中 openid 在顶级
            return (String) attributes.get("openid");
        }

        return null;
    }

    /**
     * 从 OAuth2User 中提取 unionid
     */
    private String extractUnionid(OAuth2User oauth2User, String providerType) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        if (OAuth2ProviderUtils.isWeChatProviderType(providerType)) {
            // 微信返回的属性中 unionid 在顶级
            return (String) attributes.get("unionid");
        }

        return null;
    }

    /**
     * 从 OAuth2User 中提取昵称
     */
    private String extractNickname(OAuth2User oauth2User, String providerType) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        if (OAuth2ProviderUtils.isWeChatProviderType(providerType)) {
            return (String) attributes.get("nickname");
        }

        // 从 name 或 login 字段获取
        String name = oauth2User.getName();
        if (name != null) {
            return name;
        }

        return (String) attributes.get("name");
    }

    /**
     * 从 OAuth2User 中提取头像 URL
     */
    private String extractAvatarUrl(OAuth2User oauth2User, String providerType) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        if (OAuth2ProviderUtils.isWeChatProviderType(providerType)) {
            return (String) attributes.get("headimgurl");
        }

        return (String) attributes.get("avatar_url");
    }

    /**
     * 从 OAuth2User 中提取性别
     */
    private String extractGender(OAuth2User oauth2User, String providerType) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        if (OAuth2ProviderUtils.isWeChatProviderType(providerType)) {
            Object sex = attributes.get("sex");
            if (sex != null) {
                int sexValue = sex instanceof Number ? ((Number) sex).intValue() : Integer.parseInt(sex.toString());
                return switch (sexValue) {
                    case 1 -> "male";
                    case 2 -> "female";
                    default -> "other";
                };
            }
        }
        return null;
    }

    /**
     * 从 OAuth2User 中提取地理位置
     */
    private String extractCountry(OAuth2User oauth2User, String providerType) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        return (String) attributes.get("country");
    }

    private String extractProvince(OAuth2User oauth2User, String providerType) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        return (String) attributes.get("province");
    }

    private String extractCity(OAuth2User oauth2User, String providerType) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        return (String) attributes.get("city");
    }

    /**
     * 从 OAuth2User 创建绑定实体
     */
    private MemberOAuth2Binding createBindingFromOAuth2User(Long memberId, OAuth2User oauth2User, String providerType) {
        MemberOAuth2Binding binding = new MemberOAuth2Binding();
        binding.setMemberId(memberId);
        binding.setProvider(providerType);
        binding.setOpenId(extractOpenid(oauth2User, providerType));
        binding.setUnionId(extractUnionid(oauth2User, providerType));
        binding.setNickname(extractNickname(oauth2User, providerType));
        binding.setAvatar(extractAvatarUrl(oauth2User, providerType));
        binding.setGender(extractGender(oauth2User, providerType));
        binding.setCountry(extractCountry(oauth2User, providerType));
        binding.setProvince(extractProvince(oauth2User, providerType));
        binding.setCity(extractCity(oauth2User, providerType));
        binding.setStatus(0);
        binding.setDelFlag(0);

        // 序列化原始用户数据
        try {
            binding.setRawData(objectMapper.writeValueAsString(oauth2User.getAttributes()));
        } catch (JsonProcessingException e) {
            log.warn("序列化 OAuth2 用户属性失败", e);
        }

        return binding;
    }

    /**
     * 更新绑定实体从 OAuth2User
     */
    private void updateBindingFromOAuth2User(MemberOAuth2Binding binding, OAuth2User oauth2User, String providerType) {
        binding.setNickname(extractNickname(oauth2User, providerType));
        binding.setAvatar(extractAvatarUrl(oauth2User, providerType));
        binding.setGender(extractGender(oauth2User, providerType));
        binding.setCountry(extractCountry(oauth2User, providerType));
        binding.setProvince(extractProvince(oauth2User, providerType));
        binding.setCity(extractCity(oauth2User, providerType));

        // 如果有新的 unionId，则更新
        String unionId = extractUnionid(oauth2User, providerType);
        if (unionId != null && !unionId.equals(binding.getUnionId())) {
            binding.setUnionId(unionId);
        }

        // 序列化原始用户数据
        try {
            binding.setRawData(objectMapper.writeValueAsString(oauth2User.getAttributes()));
        } catch (JsonProcessingException e) {
            log.warn("序列化 OAuth2 用户属性失败", e);
        }
    }

    /**
     * 更新绑定的 Token 信息
     */
    public void updateBindingTokens(Long memberId, String providerType,
                                    OAuth2AccessToken accessToken, String refreshToken) {
        MemberOAuth2Binding binding = memberOAuth2BindingService.findByMemberIdAndProvider(memberId, providerType);
        if (binding == null) {
            log.warn("未找到绑定关系: memberId={}, provider={}", memberId, providerType);
            return;
        }

        binding.setAccessToken(accessToken.getTokenValue());
        binding.setRefreshToken(refreshToken);

        if (accessToken.getExpiresAt() != null) {
            binding.setExpiresAt(LocalDateTime.ofInstant(accessToken.getExpiresAt(), ZoneId.systemDefault()));
        }

        memberOAuth2BindingService.updateBinding(binding);
        log.debug("更新绑定 Token 成功: memberId={}, provider={}", memberId, providerType);
    }

    /**
     * 构建登录响应对象
     */
    private OAuth2LoginResponse buildLoginResponse(Member member, String openid, String unionid,
                                                   String nickname, String avatarUrl,
                                                   Boolean isNewUser, Boolean isBound) {
        log.debug("构建登录响应: memberId={}, isNewUser={}, isBound={}", member.getId(), isNewUser, isBound);

        // 生成 JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", member.getId());
        claims.put("type", "member");
        claims.put("loginType", "oauth2");
        claims.put("openid", openid);
        if (unionid != null) {
            claims.put("unionid", unionid);
        }

        String username = member.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = member.getEmail() != null ? member.getEmail() : member.getPhone();
        }

        String jwtToken = jwtTokenUtil.generateToken(username, claims);

        // 生成 Refresh Token 并存储
        String refreshToken = Utils.genKey();
        authCacheService.storeMemberRefreshToken(refreshToken, member.getId());

        return new OAuth2LoginResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname() != null ? member.getNickname() : nickname,
                member.getAvatarUrl() != null ? member.getAvatarUrl() : avatarUrl,
                jwtToken,
                refreshToken,
                jwtTokenUtil.getTokenPrefix().trim(),
                1800000L,
                AuthCacheConstant.MEMBER_REFRESH_TOKEN_EXPIRE_HOURS * 60 * 60 * 1000,
                openid,
                unionid,
                nickname,
                avatarUrl,
                isNewUser,
                isBound
        );
    }
}
