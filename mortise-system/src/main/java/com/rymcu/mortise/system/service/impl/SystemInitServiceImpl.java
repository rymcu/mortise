package com.rymcu.mortise.system.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.common.enumerate.DefaultFlag;
import com.rymcu.mortise.common.enumerate.MenuType;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.system.entity.*;
import com.rymcu.mortise.system.mapper.*;
import com.rymcu.mortise.system.model.SystemInitInfo;
import com.rymcu.mortise.system.service.SystemInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.rymcu.mortise.system.entity.table.UserTableDef.USER;

/**
 * 系统初始化服务实现
 *
 * @author ronger
 * @since 2025-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemInitServiceImpl implements SystemInitService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final DictTypeMapper dictTypeMapper;
    private final DictMapper dictMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final PasswordEncoder passwordEncoder;

    private volatile int initProgress = 0;

    @Override
    public boolean isSystemInitialized() {
        // 检查是否存在管理员用户
        long userCount = userMapper.selectCountByQuery(QueryWrapper.create().where(USER.STATUS.eq(Status.ENABLED.ordinal())));
        return userCount > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initializeSystem(SystemInitInfo initInfo) {
        try {
            log.info("开始初始化系统...");
            initProgress = 0;

            // 1. 初始化字典类型和字典数据 (20%)
            initDictionaries();
            initProgress = 20;

            // 2. 初始化角色 (40%)
            Long adminRoleId = initRoles();
            initProgress = 40;

            // 3. 初始化菜单 (60%)
            initMenus(adminRoleId);
            initProgress = 60;

            // 4. 初始化管理员用户 (80%)
            Long adminUserId = initAdminUser(initInfo);
            initProgress = 80;

            // 5. 分配角色给管理员 (100%)
            assignRoleToUser(adminUserId, adminRoleId);
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
        return isSystemInitialized() ? 100: initProgress;
    }

    /**
     * 初始化字典类型和字典数据
     */
    private void initDictionaries() {
        log.info("初始化字典类型和字典数据...");

        // 批量创建字典类型
        List<DictType> dictTypes = new ArrayList<>(3);
        dictTypes.add(createDictType("状态", "Status", "通用状态字典", 1));
        dictTypes.add(createDictType("删除标记", "DelFlag", "删除标记字典", 2));
        dictTypes.add(createDictType("默认标记", "DefaultFlag", "默认标记字典", 3));
        dictTypes.add(createDictType("菜单类型", "MenuType", "菜单类型字典", 4));

        dictTypeMapper.insertBatchSelective(dictTypes);

        // 批量创建字典数据
        List<Dict> dictList = new ArrayList<>(9);

        // 状态字典数据
        dictList.add(createDict("Status", "正常", "0", 1, "i-lucide-square-check", "success"));
        dictList.add(createDict("Status", "禁用", "1", 2, "i-lucide-square-x", "error"));

        // 删除标记字典数据
        dictList.add(createDict("DelFlag", "未删除", "0", 1, "i-lucide-square-check", "success"));
        dictList.add(createDict("DelFlag", "已删除", "1", 2, "i-lucide-square-x", "error"));

        // 默认标记字典数据
        dictList.add(createDict("DefaultFlag", "非默认", "0", 1, "i-lucide-square-x", "info"));
        dictList.add(createDict("DefaultFlag", "默认", "1", 2, "i-lucide-square-check", "success"));

        // 菜单类型字典数据
        dictList.add(createDict("MenuType", "目录", "0", 1, "i-lucide-folder", "info"));
        dictList.add(createDict("MenuType", "菜单", "1", 2, "i-lucide-menu", "primary"));
        dictList.add(createDict("MenuType", "按钮", "2", 3, "i-lucide-mouse-pointer-click", "warning"));

        dictMapper.insertBatchSelective(dictList);

        log.info("字典初始化完成，共创建 {} 个字典类型，{} 条字典数据", dictTypes.size(), dictList.size());
    }

    /**
     * 初始化角色
     *
     * @return 管理员角色ID
     */
    private Long initRoles() {
        log.info("初始化角色...");
        List<Role> roleList = new ArrayList<>(2);
        // 创建超级管理员角色
        Role adminRole = new Role();
        adminRole.setLabel("超级管理员");
        adminRole.setPermission("ADMIN");
        adminRole.setCreatedTime(LocalDateTime.now());
        adminRole.setUpdatedTime(LocalDateTime.now());
        roleList.add(adminRole);

        // 创建普通用户角色（默认角色）
        Role userRole = new Role();
        userRole.setLabel("普通用户");
        userRole.setPermission("USER");
        userRole.setIsDefault(DefaultFlag.YES.ordinal());
        userRole.setCreatedTime(LocalDateTime.now());
        userRole.setUpdatedTime(LocalDateTime.now());
        roleList.add(userRole);

        roleMapper.insertBatchSelective(roleList);

        log.info("角色初始化完成");
        return adminRole.getId();
    }

    /**
     * 初始化菜单
     *
     * @param adminRoleId 管理员角色ID
     */
    private void initMenus(Long adminRoleId) {
        log.info("初始化菜单...");

        // 用于存储所有菜单，后续批量插入
        List<Menu> allMenus = new ArrayList<>();

        List<Menu> firstLevelMenus = new ArrayList<>(4);
        // 第一层：创建一级菜单（目录）
        Menu homeMenu = createMenu("首页", "home", "i-lucide-house", "/", MenuType.MENU.ordinal(), 1, 0L);
        Menu inboxMenu = createMenu("收件箱", "inbox", "i-lucide-inbox", "/inbox", MenuType.MENU.ordinal(), 2, 0L);
        Menu systemMenu = createMenu("系统", "system", "i-lucide-settings-2", "/systems", MenuType.FOLDER.ordinal(), 10, 0L);
        Menu settingMenu = createMenu("设置", "settings", "i-lucide-settings", "/settings", MenuType.FOLDER.ordinal(), 1000, 0L);

        firstLevelMenus.add(homeMenu);
        firstLevelMenus.add(inboxMenu);
        firstLevelMenus.add(systemMenu);
        firstLevelMenus.add(settingMenu);

        menuMapper.insertBatchSelective(firstLevelMenus);

        // 第二层：创建二级菜单（功能模块）
        List<Menu> secondLevelMenus = new ArrayList<>(7);
        Menu userMenu = createMenu("用户", "system:user", "i-lucide-users", "/systems/users", MenuType.MENU.ordinal(), 1, systemMenu.getId());
        Menu roleMenu = createMenu("角色", "system:role", "i-lucide-shield-user", "/systems/roles", MenuType.MENU.ordinal(), 2, systemMenu.getId());
        Menu menuMenu = createMenu("菜单", "system:menu", "i-lucide-menu", "/systems/menus", MenuType.MENU.ordinal(), 3, systemMenu.getId());
        Menu dictMenu = createMenu("字典", "system:dict", "i-lucide-book", "/systems/dictionaries", MenuType.MENU.ordinal(), 4, systemMenu.getId());
        Menu profileMenu = createMenu("资料", "settings:profile", "i-lucide-user", "/settings", MenuType.MENU.ordinal(), 1, settingMenu.getId());
        Menu notificationMenu = createMenu("消息", "settings:notifications", "i-lucide-bell-ring", "/settings/notifications", MenuType.MENU.ordinal(), 2, settingMenu.getId());
        Menu securityMenu = createMenu("安全", "settings:security", "i-lucide-shield", "/settings/security", MenuType.MENU.ordinal(), 3, settingMenu.getId());

        secondLevelMenus.add(userMenu);
        secondLevelMenus.add(roleMenu);
        secondLevelMenus.add(menuMenu);
        secondLevelMenus.add(dictMenu);
        secondLevelMenus.add(profileMenu);
        secondLevelMenus.add(notificationMenu);
        secondLevelMenus.add(securityMenu);

        menuMapper.insertBatchSelective(secondLevelMenus);

        // 第三层：创建按钮（操作权限）
        List<Menu> buttonMenus = new ArrayList<>(16);

        // 用户管理按钮
        buttonMenus.add(createMenu("查询用户", "system:user:list", null, null, MenuType.BUTTON.ordinal(), 1, userMenu.getId()));
        buttonMenus.add(createMenu("新增用户", "system:user:add", null, null, MenuType.BUTTON.ordinal(), 2, userMenu.getId()));
        buttonMenus.add(createMenu("编辑用户", "system:user:edit", null, null, MenuType.BUTTON.ordinal(), 3, userMenu.getId()));
        buttonMenus.add(createMenu("删除用户", "system:user:delete", null, null, MenuType.BUTTON.ordinal(), 4, userMenu.getId()));

        // 角色管理按钮
        buttonMenus.add(createMenu("查询角色", "system:role:list", null, null, MenuType.BUTTON.ordinal(), 1, roleMenu.getId()));
        buttonMenus.add(createMenu("新增角色", "system:role:add", null, null, MenuType.BUTTON.ordinal(), 2, roleMenu.getId()));
        buttonMenus.add(createMenu("编辑角色", "system:role:edit", null, null, MenuType.BUTTON.ordinal(), 3, roleMenu.getId()));
        buttonMenus.add(createMenu("删除角色", "system:role:delete", null, null, MenuType.BUTTON.ordinal(), 4, roleMenu.getId()));

        // 菜单管理按钮
        buttonMenus.add(createMenu("查询菜单", "system:menu:list", null, null, MenuType.BUTTON.ordinal(), 1, menuMenu.getId()));
        buttonMenus.add(createMenu("新增菜单", "system:menu:add", null, null, MenuType.BUTTON.ordinal(), 2, menuMenu.getId()));
        buttonMenus.add(createMenu("编辑菜单", "system:menu:edit", null, null, MenuType.BUTTON.ordinal(), 3, menuMenu.getId()));
        buttonMenus.add(createMenu("删除菜单", "system:menu:delete", null, null, MenuType.BUTTON.ordinal(), 4, menuMenu.getId()));

        // 字典管理按钮
        buttonMenus.add(createMenu("查询字典", "system:dict:list", null, null, MenuType.BUTTON.ordinal(), 1, dictMenu.getId()));
        buttonMenus.add(createMenu("新增字典", "system:dict:add", null, null, MenuType.BUTTON.ordinal(), 2, dictMenu.getId()));
        buttonMenus.add(createMenu("编辑字典", "system:dict:edit", null, null, MenuType.BUTTON.ordinal(), 3, dictMenu.getId()));
        buttonMenus.add(createMenu("删除字典", "system:dict:delete", null, null, MenuType.BUTTON.ordinal(), 4, dictMenu.getId()));

        menuMapper.insertBatchSelective(buttonMenus);

        // 收集所有菜单ID用于权限分配
        allMenus.add(systemMenu);
        allMenus.addAll(secondLevelMenus);
        allMenus.addAll(buttonMenus);

        // 批量分配菜单权限给管理员角色
        List<RoleMenu> roleMenuRelations = new ArrayList<>(allMenus.size());
        for (Menu menu : allMenus) {
            RoleMenu roleMenuRelation = new RoleMenu();
            roleMenuRelation.setIdMortiseRole(adminRoleId);
            roleMenuRelation.setIdMortiseMenu(menu.getId());
            roleMenuRelations.add(roleMenuRelation);
        }

        roleMenuMapper.insertBatchSelective(roleMenuRelations);
        log.info("分配菜单权限给管理员角色，共 {} 个菜单", allMenus.size());

        log.info("菜单初始化完成");
    }

    /**
     * 初始化管理员用户
     *
     * @param initInfo 初始化信息
     * @return 管理员用户ID
     */
    private Long initAdminUser(SystemInitInfo initInfo) {
        log.info("初始化管理员用户...");

        User admin = new User();
        admin.setAccount(initInfo.getAdminAccount());
        admin.setPassword(passwordEncoder.encode(initInfo.getAdminPassword()));
        admin.setNickname(initInfo.getAdminNickname());
        admin.setEmail(initInfo.getAdminEmail());
        admin.setCreatedTime(LocalDateTime.now());

        userMapper.insertSelective(admin);

        log.info("管理员用户初始化完成");
        return admin.getId();
    }

    /**
     * 分配角色给用户
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    private void assignRoleToUser(Long userId, Long roleId) {
        log.info("分配角色给用户...");

        UserRole userRole = new UserRole();
        userRole.setIdMortiseUser(userId);
        userRole.setIdMortiseRole(roleId);
        userRoleMapper.insertSelective(userRole);

        log.info("角色分配完成");
    }

    // ========== 辅助方法 ==========

    private DictType createDictType(String label, String typeCode, String description, int sortNo) {
        DictType dictType = new DictType();
        // ID 交由 MyBatis-Flex 在单条 insert 时自动生成（由实体上的 @Id 注解控制）
        dictType.setLabel(label);
        dictType.setTypeCode(typeCode);
        dictType.setDescription(description);
        dictType.setSortNo(sortNo);
        dictType.setCreatedTime(LocalDateTime.now());
        dictType.setUpdatedTime(LocalDateTime.now());
        return dictType;
    }

    private Dict createDict(String typeCode, String label, String value, int sortNo, String icon, String color) {
        Dict dict = new Dict();
        // ID 交由 MyBatis-Flex 在单条 insert 时自动生成（由实体上的 @Id 注解控制）
        dict.setDictTypeCode(typeCode);
        dict.setLabel(label);
        dict.setValue(value);
        dict.setSortNo(sortNo);
        dict.setIcon(icon);
        dict.setColor(color);
        dict.setCreatedTime(LocalDateTime.now());
        dict.setUpdatedTime(LocalDateTime.now());
        return dict;
    }

    private Menu createMenu(String label, String permission, String icon, String href, int menuType, int sortNo, Long parentId) {
        Menu menu = new Menu();
        // ID 交由 MyBatis-Flex 在单条 insert 时自动生成（由实体上的 @Id 注解控制）
        menu.setLabel(label);
        menu.setPermission(permission);
        menu.setIcon(icon);
        menu.setHref(href);
        menu.setMenuType(menuType);
        menu.setSortNo(sortNo);
        menu.setParentId(parentId);
        menu.setCreatedTime(LocalDateTime.now());
        menu.setUpdatedTime(LocalDateTime.now());
        return menu;
    }
}
