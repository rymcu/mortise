package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.facade.DashboardAdminFacade;
import com.rymcu.mortise.system.model.DashboardStats;
import com.rymcu.mortise.system.query.MenuQueryService;
import com.rymcu.mortise.system.query.RoleQueryService;
import com.rymcu.mortise.system.query.UserQueryService;
import com.rymcu.mortise.system.service.SystemCacheService;
import org.springframework.stereotype.Component;

@Component
public class DashboardAdminFacadeImpl implements DashboardAdminFacade {

    private final UserQueryService userQueryService;
    private final RoleQueryService roleQueryService;
    private final MenuQueryService menuQueryService;
    private final SystemCacheService systemCacheService;

    public DashboardAdminFacadeImpl(UserQueryService userQueryService,
                                    RoleQueryService roleQueryService,
                                    MenuQueryService menuQueryService,
                                    SystemCacheService systemCacheService) {
        this.userQueryService = userQueryService;
        this.roleQueryService = roleQueryService;
        this.menuQueryService = menuQueryService;
        this.systemCacheService = systemCacheService;
    }

    @Override
    public GlobalResult<DashboardStats> getStats() {
        Long userCount = getOrCacheUserCount();
        Long roleCount = getOrCacheRoleCount();
        Long menuCount = getOrCacheMenuCount();
        Long memberCount = systemCacheService.getMemberCount();
        if (memberCount == null) {
            memberCount = 0L;
        }

        DashboardStats stats = new DashboardStats(userCount, roleCount, menuCount, memberCount);
        return GlobalResult.success(stats);
    }

    private Long getOrCacheUserCount() {
        Long count = systemCacheService.getUserCount();
        if (count == null) {
            count = userQueryService.countEnabled();
            systemCacheService.cacheUserCount(count);
        }
        return count;
    }

    private Long getOrCacheRoleCount() {
        Long count = systemCacheService.getRoleCount();
        if (count == null) {
            count = roleQueryService.countEnabled();
            systemCacheService.cacheRoleCount(count);
        }
        return count;
    }

    private Long getOrCacheMenuCount() {
        Long count = systemCacheService.getMenuCount();
        if (count == null) {
            count = menuQueryService.countEnabled();
            systemCacheService.cacheMenuCount(count);
        }
        return count;
    }
}
