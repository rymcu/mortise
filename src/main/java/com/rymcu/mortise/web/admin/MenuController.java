package com.rymcu.mortise.web.admin;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.entity.Menu;
import com.rymcu.mortise.model.BatchUpdateInfo;
import com.rymcu.mortise.model.Link;
import com.rymcu.mortise.model.MenuSearch;
import com.rymcu.mortise.service.MenuService;
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
@RestController
@RequestMapping("/api/v1/admin/menus")
@PreAuthorize("hasRole('admin')")
public class MenuController {
    @Resource
    private MenuService menuService;

    @Operation(summary = "获取菜单列表", description = "查询菜单信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    public GlobalResult<List<Link>> listMenu(@Parameter(description = "菜单查询条件") @Valid MenuSearch search) {
        List<Link> list = menuService.findMenus(search);
        return GlobalResult.success(list);
    }

    @Operation(summary = "获取子菜单列表", description = "分页查询子菜单信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/children")
    public GlobalResult<Page<Link>> listChildrenMenu(@Parameter(description = "菜单查询条件") @Valid MenuSearch search) {
        Page<Link> page = new Page<>(search.getPageNum(), search.getPageSize());
        List<Link> list = menuService.findChildrenMenus(page, search);
        page.setRecords(list);
        return GlobalResult.success(page);
    }

    @Operation(summary = "获取菜单详情", description = "根据ID获取菜单详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "菜单不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    public GlobalResult<Menu> getMenuById(@Parameter(description = "菜单ID", required = true) @PathVariable("id") Long idMenu) {
        return GlobalResult.success(menuService.findById(idMenu));
    }

    @Operation(summary = "创建菜单", description = "新增菜单数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    public GlobalResult<Boolean> createMenu(@Parameter(description = "菜单信息", required = true) @Valid @RequestBody Menu menu) {
        return GlobalResult.success(menuService.saveMenu(menu));
    }

    @Operation(summary = "更新菜单", description = "修改菜单数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "菜单不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    public GlobalResult<Boolean> updateMenu(@Parameter(description = "菜单ID", required = true) @PathVariable("id") Long idMenu,
                                           @Parameter(description = "菜单信息", required = true) @Valid @RequestBody Menu menu) {
        menu.setId(idMenu);
        return GlobalResult.success(menuService.saveMenu(menu));
    }

    @Operation(summary = "更新菜单状态", description = "启用/禁用菜单")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "菜单不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    public GlobalResult<Boolean> updateMenuStatus(@Parameter(description = "菜单ID", required = true) @PathVariable("id") Long idMenu,
                                                  @Parameter(description = "菜单状态信息", required = true) @Valid @RequestBody Menu menu) {
        return GlobalResult.success(menuService.updateStatus(idMenu, menu.getStatus()));
    }

    @Operation(summary = "删除菜单", description = "软删除菜单数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "菜单不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    public GlobalResult<Boolean> deleteMenu(@Parameter(description = "菜单ID", required = true) @PathVariable("id") Long idMenu) {
        return GlobalResult.success(menuService.deleteMenu(idMenu));
    }

    @Operation(summary = "获取菜单树", description = "查询菜单树形结构")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/tree")
    public GlobalResult<List<Link>> getMenuTree(@Parameter(description = "菜单查询条件") @Valid MenuSearch search) {
        List<Link> menus = menuService.findMenuTree(search);
        return GlobalResult.success(menus);
    }

    @Operation(summary = "批量删除菜单", description = "批量软删除菜单数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/batch")
    public GlobalResult<Boolean> batchDeleteMenus(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(menuService.batchDeleteMenus(batchUpdateInfo.getIds()));
    }
}
