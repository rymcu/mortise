package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.controller.facade.MenuAdminFacade;
import com.rymcu.mortise.system.controller.request.MenuStatusRequest;
import com.rymcu.mortise.system.controller.request.MenuUpsertRequest;
import com.rymcu.mortise.system.controller.vo.MenuVO;
import com.rymcu.mortise.system.model.MenuSearch;
import com.rymcu.mortise.system.model.MenuTreeInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 * 提供菜单的 CRUD 操作
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/8/10
 */
@Tag(name = "菜单管理", description = "菜单管理相关接口")
@AdminController
@RequestMapping("/menus")
public class MenuController {
    @Resource
    private MenuAdminFacade menuAdminFacade;

    @Operation(summary = "获取菜单列表", description = "查询菜单信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('system:menu:list')")
    @ApiLog("查询菜单列表")
    public GlobalResult<PageResult<MenuVO>> listMenu(@Parameter(description = "菜单查询条件") @Valid MenuSearch search) {
        return GlobalResult.success(menuAdminFacade.listMenus(search));
    }

    @Operation(summary = "获取菜单详情", description = "根据ID获取菜单详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "菜单不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:query')")
    @ApiLog("获取菜单详情")
    public GlobalResult<MenuVO> getMenuById(@Parameter(description = "菜单ID", required = true) @PathVariable("id") Long idMenu) {
        return GlobalResult.success(menuAdminFacade.getMenuById(idMenu));
    }

    @Operation(summary = "创建菜单", description = "新增菜单数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    @ApiLog("创建菜单")
    @OperationLog(module = "菜单管理", operation = "创建菜单", recordParams = true, recordResult = true)
    public GlobalResult<Long> createMenu(@Parameter(description = "菜单信息", required = true) @Valid @RequestBody MenuUpsertRequest request) {
        return GlobalResult.success(menuAdminFacade.createMenu(request));
    }

    @Operation(summary = "更新菜单", description = "修改菜单数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "菜单不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @ApiLog("更新菜单")
    @OperationLog(module = "菜单管理", operation = "更新菜单", recordParams = true)
    public GlobalResult<Boolean> updateMenu(@Parameter(description = "菜单ID", required = true) @PathVariable("id") Long idMenu,
                                           @Parameter(description = "菜单信息", required = true) @Valid @RequestBody MenuUpsertRequest request) {
        return GlobalResult.success(menuAdminFacade.updateMenu(idMenu, request));
    }

    @Operation(summary = "更新菜单状态", description = "启用/禁用菜单")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "菜单不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @ApiLog("更新菜单状态")
    @OperationLog(module = "菜单管理", operation = "更新菜单状态", recordParams = true)
    public GlobalResult<Boolean> updateMenuStatus(@Parameter(description = "菜单ID", required = true) @PathVariable("id") Long idMenu,
                                                  @Parameter(description = "菜单状态信息", required = true) @Valid @RequestBody MenuStatusRequest request) {
        return GlobalResult.success(menuAdminFacade.updateMenuStatus(idMenu, request));
    }

    @Operation(summary = "删除菜单", description = "软删除菜单数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "菜单不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    @ApiLog("删除菜单")
    @OperationLog(module = "菜单管理", operation = "删除菜单")
    public GlobalResult<Boolean> deleteMenu(@Parameter(description = "菜单ID", required = true) @PathVariable("id") Long idMenu) {
        return GlobalResult.success(menuAdminFacade.deleteMenu(idMenu));
    }

    @Operation(summary = "获取菜单树", description = "查询菜单树形结构")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    @ApiLog("获取菜单树")
    public GlobalResult<List<MenuTreeInfo>> getMenuTree(@Parameter(description = "菜单查询条件") @Valid MenuSearch search) {
        return GlobalResult.success(menuAdminFacade.getMenuTree(search));
    }

    @Operation(summary = "批量删除菜单", description = "批量软删除菜单数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    @ApiLog("批量删除菜单")
    @OperationLog(module = "菜单管理", operation = "批量删除菜单", recordParams = true)
    public GlobalResult<Boolean> batchDeleteMenus(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(menuAdminFacade.batchDeleteMenus(batchUpdateInfo));
    }
}

