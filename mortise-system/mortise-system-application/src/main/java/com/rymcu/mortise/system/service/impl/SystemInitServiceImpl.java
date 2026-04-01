package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.system.constant.SystemAuthConstants;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.SiteConfigSaveRequest;
import com.rymcu.mortise.system.model.SystemInitInfo;
import com.rymcu.mortise.system.repository.RoleRepository;
import com.rymcu.mortise.system.repository.UserRepository;
import com.rymcu.mortise.system.repository.UserRoleRepository;
import com.rymcu.mortise.system.service.SiteConfigService;
import com.rymcu.mortise.system.service.SystemInitService;
import com.rymcu.mortise.system.service.command.UserCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统初始化服务实现
 * 角色、字典、菜单和角色菜单授权由 Flyway 脚本负责，
 * 此处仅负责管理员账号、管理员角色绑定和站点基础配置。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemInitServiceImpl implements SystemInitService {

    private final UserCommandService userCommandService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SiteConfigService siteConfigService;

    private volatile int initProgress = 0;

    @Override
    public boolean isSystemInitialized() {
        return userRepository.count() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initializeSystem(SystemInitInfo initInfo) {
        if (isSystemInitialized()) {
            log.warn("系统已经初始化，跳过此次操作。");
            return false;
        }
        try {
            log.info("开始系统初始化...");
            initProgress = 0;

            Long adminUserId = initAdminUser(initInfo);
            initProgress = 40;

            Long adminRoleId = findAdminRole();
            assignRoleToUser(adminUserId, adminRoleId);
            initProgress = 80;

            initSiteConfig(initInfo);
            initProgress = 100;

            log.info("系统初始化完成！");
            return true;
        } catch (Exception e) {
            log.error("系统初始化失败", e);
            initProgress = 0;
            throw new RuntimeException("系统初始化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int getInitializationProgress() {
        return isSystemInitialized() ? 100 : initProgress;
    }

    private Long initAdminUser(SystemInitInfo initInfo) {
        log.info("创建管理员用户...");
        log.info("收到初始化信息: nickname={}, email={}, password长度={}",
                initInfo.getAdminNickname(),
                initInfo.getAdminEmail(),
                initInfo.getAdminPassword() == null ? "null" : initInfo.getAdminPassword().length());

        if (initInfo.getAdminPassword() == null || initInfo.getAdminPassword().isBlank()) {
            throw new IllegalArgumentException("管理员密码不能为空");
        }

        User admin = new User();
        admin.setAccount(userCommandService.nextAccount());
        admin.setPassword(passwordEncoder.encode(initInfo.getAdminPassword()));
        admin.setNickname(initInfo.getAdminNickname());
        admin.setEmail(initInfo.getAdminEmail());
        admin.setCreatedTime(LocalDateTime.now());
        userRepository.save(admin);
        log.info("管理员用户创建完成，ID={}", admin.getId());
        return admin.getId();
    }

    private Long findAdminRole() {
        Role adminRole = roleRepository.findByPermission(SystemAuthConstants.ADMIN_ROLE_PERMISSION);
        if (adminRole == null) {
            throw new IllegalStateException(
                    "未找到 " + SystemAuthConstants.ADMIN_ROLE_PERMISSION + " 角色，请检查 Flyway 种子脚本是否已执行"
            );
        }
        return adminRole.getId();
    }

    private void assignRoleToUser(Long userId, Long roleId) {
        log.info("为管理员分配 {} 角色...", SystemAuthConstants.ADMIN_ROLE_PERMISSION);
        if (!userRoleRepository.replaceRoles(userId, List.of(roleId))) {
            throw new IllegalStateException("管理员角色分配失败");
        }
        log.info("角色分配完成");
    }

    private void initSiteConfig(SystemInitInfo initInfo) {
        Map<String, String> values = new HashMap<>();
        if (StringUtils.hasText(initInfo.getSystemName())) {
            values.put("site.name", initInfo.getSystemName());
        }
        if (StringUtils.hasText(initInfo.getSystemDescription())) {
            values.put("site.description", initInfo.getSystemDescription());
        }
        if (!values.isEmpty()) {
            siteConfigService.saveGroup("site", new SiteConfigSaveRequest(values));
            log.info("网站基本配置写入完成");
        }
    }
}
