package com.rymcu.mortise.system.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.entity.UserRole;
import com.rymcu.mortise.system.mapper.RoleMapper;
import com.rymcu.mortise.system.mapper.UserMapper;
import com.rymcu.mortise.system.mapper.UserRoleMapper;
import com.rymcu.mortise.system.model.SiteConfigSaveRequest;
import com.rymcu.mortise.system.model.SystemInitInfo;
import com.rymcu.mortise.system.service.SiteConfigService;
import com.rymcu.mortise.system.service.SystemInitService;
import com.rymcu.mortise.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.rymcu.mortise.system.entity.table.RoleTableDef.ROLE;
import static com.rymcu.mortise.system.entity.table.UserTableDef.USER;

/**
 * 系统初始化服务实现
 * <p>
 * 角色、字典、菜单及角色-菜单授权均由 Flyway 脚本（V8/V9/V10）在应用启动时自动完成。
 * 本服务仅负责：
 * <ol>
 *   <li>创建管理员账号（密码由用户在安装向导中填写）</li>
 *   <li>将 ADMIN 角色分配给管理员</li>
 *   <li>将网站名称与描述写入系统配置</li>
 * </ol>
 *
 * @author ronger
 * @since 2025-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemInitServiceImpl implements SystemInitService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final SiteConfigService siteConfigService;

    private volatile int initProgress = 0;

    @Override
    public boolean isSystemInitialized() {
        long userCount = userMapper.selectCountByQuery(QueryWrapper.create().from(USER));
        return userCount > 0;
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

            // 1. 创建管理员用户 (40%)
            Long adminUserId = initAdminUser(initInfo);
            initProgress = 40;

            // 2. 分配 ADMIN 角色给管理员 (80%)
            Long adminRoleId = findAdminRole();
            assignRoleToUser(adminUserId, adminRoleId);
            initProgress = 80;

            // 3. 写入网站基本配置 (100%)
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
        admin.setAccount(userService.nextAccount());
        admin.setPassword(passwordEncoder.encode(initInfo.getAdminPassword()));
        admin.setNickname(initInfo.getAdminNickname());
        admin.setEmail(initInfo.getAdminEmail());
        admin.setCreatedTime(LocalDateTime.now());
        userMapper.insertSelective(admin);
        log.info("管理员用户创建完成，ID={}", admin.getId());
        return admin.getId();
    }

    /**
     * 从数据库中查找 ADMIN 角色（由 Flyway V8 种子脚本创建）。
     */
    private Long findAdminRole() {
        Role adminRole = roleMapper.selectOneByQuery(
                QueryWrapper.create().from(ROLE).where(ROLE.PERMISSION.eq("ADMIN"))
        );
        if (adminRole == null) {
            throw new IllegalStateException("未找到 ADMIN 角色，请检查 Flyway 脚本 V8__Seed_System_Roles.sql 是否已执行");
        }
        return adminRole.getId();
    }

    private void assignRoleToUser(Long userId, Long roleId) {
        log.info("为管理员分配 ADMIN 角色...");
        UserRole userRole = new UserRole();
        userRole.setIdMortiseUser(userId);
        userRole.setIdMortiseRole(roleId);
        userRoleMapper.insertSelective(userRole);
        log.info("角色分配完成");
    }

    /**
     * 将安装向导填写的网站名称和描述写入 site 配置分组。
     * 未填写的字段保持 Flyway 默认值不变。
     */
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
