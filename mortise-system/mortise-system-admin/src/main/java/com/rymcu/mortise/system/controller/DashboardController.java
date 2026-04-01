package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.system.controller.facade.DashboardAdminFacade;
import com.rymcu.mortise.system.model.DashboardStats;
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
    private DashboardAdminFacade dashboardAdminFacade;

    @Operation(summary = "获取统计数据", description = "获取仪表盘统计数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/stats")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "获取仪表盘统计数据")
    public GlobalResult<DashboardStats> getStats() {
        return dashboardAdminFacade.getStats();
    }
}
