package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.system.model.DashboardStats;
import com.rymcu.mortise.system.service.MenuService;
import com.rymcu.mortise.system.service.RoleService;
import com.rymcu.mortise.system.service.SystemCacheService;
import com.rymcu.mortise.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 仪表盘控制器
 * 提供仪表盘统计数据
 *
 * @author ronger
 */
@Tag(name = "仪表盘", description = "仪表盘统计相关接口")
@AdminController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Resource
    private MenuService menuService;

    @Resource
    private SystemCacheService systemCacheService;

    @Operation(summary = "获取统计数据", description = "获取仪表盘统计数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/stats")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "获取仪表盘统计数据")
    public GlobalResult<DashboardStats> getStats() {
        // 从缓存读取，缓存不存在时从数据库读取并更新缓存
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

    /**
     * 获取用户数，缓存不存在时从数据库读取并缓存
     */
    private Long getOrCacheUserCount() {
        Long count = systemCacheService.getUserCount();
        if (count == null) {
            count = userService.count();
            systemCacheService.cacheUserCount(count);
        }
        return count;
    }

    /**
     * 获取角色数，缓存不存在时从数据库读取并缓存
     */
    private Long getOrCacheRoleCount() {
        Long count = systemCacheService.getRoleCount();
        if (count == null) {
            count = roleService.count();
            systemCacheService.cacheRoleCount(count);
        }
        return count;
    }

    /**
     * 获取菜单数，缓存不存在时从数据库读取并缓存
     */
    private Long getOrCacheMenuCount() {
        Long count = systemCacheService.getMenuCount();
        if (count == null) {
            count = menuService.count();
            systemCacheService.cacheMenuCount(count);
        }
        return count;
    }
}
