package com.rymcu.mortise.system.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.system.entity.*;
import com.rymcu.mortise.system.mapper.*;
import com.rymcu.mortise.system.model.SystemInitInfo;
import com.rymcu.mortise.system.service.SystemInitService;
import com.rymcu.mortise.system.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rymcu.mortise.system.entity.table.UserTableDef.USER;

/**
 * 系统初始化服务实现 (重构版)
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
    private final MenuMapper menuMapper;
    private final DictTypeMapper dictTypeMapper;
    private final DictMapper dictMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper; // Spring Boot 自动配置
    private final ResourceLoader resourceLoader; // 用于加载资源文件

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
            log.info("开始从JSON文件初始化系统...");
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
            // 向上抛出运行时异常，触发事务回滚
            throw new RuntimeException("系统初始化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int getInitializationProgress() {
        return isSystemInitialized() ? 100 : initProgress;
    }

    /**
     * 从JSON文件加载数据并反序列化
     * @param path classpath路径
     * @param typeReference 目标类型
     * @return T 类型的对象列表
     */
    private <T> T loadDataFromJson(String path, TypeReference<T> typeReference) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + path);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, typeReference);
        }
    }

    /**
     * 初始化字典类型和字典数据
     */
    private void initDictionaries() throws IOException {
        log.info("从 dictionaries.json 初始化字典...");
        List<DictTypeJson> dictTypeJsons = loadDataFromJson("init-data/dictionaries.json", new TypeReference<>() {});

        List<DictType> dictTypes = new ArrayList<>();
        List<Dict> dicts = new ArrayList<>();

        for (DictTypeJson typeJson : dictTypeJsons) {
            DictType dictType = new DictType();
            dictType.setLabel(typeJson.getLabel());
            dictType.setTypeCode(typeJson.getTypeCode());
            dictType.setDescription(typeJson.getDescription());
            dictType.setSortNo(typeJson.getSortNo());
            dictType.setCreatedTime(LocalDateTime.now());
            dictType.setUpdatedTime(LocalDateTime.now());
            dictTypes.add(dictType);

            if (typeJson.getDicts() != null) {
                for (DictJson dictJson : typeJson.getDicts()) {
                    Dict dict = new Dict();
                    dict.setDictTypeCode(typeJson.getTypeCode());
                    dict.setLabel(dictJson.getLabel());
                    dict.setValue(dictJson.getValue());
                    dict.setSortNo(dictJson.getSortNo());
                    dict.setIcon(dictJson.getIcon());
                    dict.setColor(dictJson.getColor());
                    dict.setCreatedTime(LocalDateTime.now());
                    dict.setUpdatedTime(LocalDateTime.now());
                    dicts.add(dict);
                }
            }
        }

        if (!dictTypes.isEmpty()) {
            dictTypeMapper.insertBatchSelective(dictTypes);
        }
        if (!dicts.isEmpty()) {
            dictMapper.insertBatchSelective(dicts);
        }
        log.info("字典初始化完成，共创建 {} 个字典类型，{} 条字典数据", dictTypes.size(), dicts.size());
    }

    /**
     * 初始化角色
     * @return 超级管理员角色ID
     */
    private Long initRoles() throws IOException {
        log.info("从 roles.json 初始化角色...");
        List<Role> roles = loadDataFromJson("init-data/roles.json", new TypeReference<>() {});

        roles.forEach(role -> {
            role.setCreatedTime(LocalDateTime.now());
            role.setUpdatedTime(LocalDateTime.now());
        });

        roleMapper.insertBatchSelective(roles);

        Role adminRole = roles.stream()
                .filter(role -> "ADMIN".equals(role.getPermission()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未在 roles.json 中找到 ADMIN 角色"));

        log.info("角色初始化完成");
        return adminRole.getId();
    }

    /**
     * 初始化菜单, 并为管理员角色授权
     * @param adminRoleId 管理员角色ID
     */
    private void initMenus(Long adminRoleId) throws IOException {
        log.info("从 menus.json 初始化菜单...");
        List<MenuJson> menuJsons = loadDataFromJson("init-data/menus.json", new TypeReference<>() {});

        // 使用 permission 作为唯一标识符来处理父子关系
        Map<String, Menu> permissionToMenuMap = menuJsons.stream()
                .map(this::convertMenuJsonToEntity)
                .collect(Collectors.toMap(Menu::getPermission, Function.identity()));

        // 设置父ID
        permissionToMenuMap.values().forEach(menu -> {
            String parentPermission = menuJsons.stream()
                    .filter(json -> menu.getPermission().equals(json.getPermission()))
                    .findFirst().orElse(new MenuJson()).getParentPermission();

            if (StringUtils.hasText(parentPermission)) {
                Menu parentMenu = permissionToMenuMap.get(parentPermission);
                if (parentMenu != null) {
                    // 关键：在批量插入前，MyBatis-Flex不会填充ID，所以我们先不设置parentID
                } else {
                    log.warn("未找到父菜单: {}", parentPermission);
                }
            }
        });

        // 按层级插入以确保父级先有ID
        // 1. 插入顶层菜单 (parentId is null)
        List<Menu> topLevelMenus = permissionToMenuMap.values().stream()
                .filter(menu -> {
                    String parentPermission = menuJsons.stream().filter(json -> menu.getPermission().equals(json.getPermission())).findFirst().get().getParentPermission();
                    return !StringUtils.hasText(parentPermission);
                })
                .collect(Collectors.toList());
        menuMapper.insertBatchSelective(topLevelMenus);
        // 更新 map，让实体包含数据库生成的 ID
        topLevelMenus.forEach(menu -> permissionToMenuMap.put(menu.getPermission(), menu));

        // 2. 插入二级及以下菜单
        List<Menu> otherMenus = new ArrayList<>(permissionToMenuMap.values());
        otherMenus.removeAll(topLevelMenus);

        // 简单的循环处理多级，可以优化为更复杂的图遍历算法，但对于几层菜单足够了
        int maxLevels = 10; // 防止死循环
        while (!otherMenus.isEmpty() && maxLevels-- > 0) {
            List<Menu> menusToInsert = new ArrayList<>();
            otherMenus.removeIf(menu -> {
                String parentPermission = menuJsons.stream().filter(json -> menu.getPermission().equals(json.getPermission())).findFirst().get().getParentPermission();
                Menu parentMenu = permissionToMenuMap.get(parentPermission);
                if (parentMenu != null && parentMenu.getId() != null) {
                    menu.setParentId(parentMenu.getId());
                    menusToInsert.add(menu);
                    return true; // 从待处理列表中移除
                }
                return false; // 保留以待下次循环
            });
            if (!menusToInsert.isEmpty()) {
                menuMapper.insertBatchSelective(menusToInsert);
                menusToInsert.forEach(menu -> permissionToMenuMap.put(menu.getPermission(), menu));
            } else if (!otherMenus.isEmpty()) {
                // 如果有剩余菜单但本次未插入任何内容，说明有父级不存在，中断
                log.error("存在无法找到父菜单的菜单项，初始化中断。");
                throw new IllegalStateException("菜单数据层级关系错误");
            }
        }


        // 给管理员角色分配所有非按钮菜单 (或所有菜单，根据业务决定)
        List<Menu> allMenus = new ArrayList<>(permissionToMenuMap.values());
        List<RoleMenu> roleMenus = allMenus.stream()
                // .filter(menu -> !Objects.equals(menu.getMenuType(), MenuType.BUTTON.ordinal())) // 可选：只分配目录和菜单
                .map(menu -> {
                    RoleMenu rm = new RoleMenu();
                    rm.setIdMortiseRole(adminRoleId);
                    rm.setIdMortiseMenu(menu.getId());
                    return rm;
                })
                .collect(Collectors.toList());

        roleMenuMapper.insertBatchSelective(roleMenus);
        log.info("菜单初始化完成，并为管理员分配了 {} 个菜单权限", roleMenus.size());
    }

    // ... initAdminUser 和 assignRoleToUser 方法保持不变 ...
    private Long initAdminUser(SystemInitInfo initInfo) {
        log.info("初始化管理员用户...");
        User admin = new User();
        admin.setAccount(userService.nextAccount());
        admin.setPassword(passwordEncoder.encode(initInfo.getAdminPassword()));
        admin.setNickname(initInfo.getAdminNickname());
        admin.setEmail(initInfo.getAdminEmail());
        admin.setCreatedTime(LocalDateTime.now());
        userMapper.insertSelective(admin);
        log.info("管理员用户初始化完成");
        return admin.getId();
    }

    private void assignRoleToUser(Long userId, Long roleId) {
        log.info("分配角色给用户...");
        UserRole userRole = new UserRole();
        userRole.setIdMortiseUser(userId);
        userRole.setIdMortiseRole(roleId);
        userRoleMapper.insertSelective(userRole);
        log.info("角色分配完成");
    }


    // ========== DTOs for JSON Deserialization ==========
    // 使用静态内部类来避免创建额外的文件

    @Setter
    @Getter
    private static class DictTypeJson {
        // Getters and Setters
        private String label;
        private String typeCode;
        private String description;
        private Integer sortNo;
        private List<DictJson> dicts;

    }

    @Setter
    @Getter
    private static class DictJson {
        // Getters and Setters
        private String label;
        private String value;
        private Integer sortNo;
        private String icon;
        private String color;

    }

    @Setter
    @Getter
    private static class MenuJson {
        // Getters and Setters
        private String label;
        private String permission;
        private String icon;
        private String href;
        private Integer menuType;
        private Integer sortNo;
        private String parentPermission;

    }

    private Menu convertMenuJsonToEntity(MenuJson json) {
        Menu menu = new Menu();
        menu.setLabel(json.getLabel());
        menu.setPermission(json.getPermission());
        menu.setIcon(json.getIcon());
        menu.setHref(json.getHref());
        menu.setMenuType(json.getMenuType());
        menu.setSortNo(json.getSortNo());
        // ParentId will be set later after parents are inserted
        menu.setCreatedTime(LocalDateTime.now());
        menu.setUpdatedTime(LocalDateTime.now());
        return menu;
    }
}
