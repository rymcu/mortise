package com.rymcu.mortise.wechat.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.wechat.service.DynamicWeChatServiceManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.menu.WxMpMenu;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 微信公众号自定义菜单管理控制器。
 *
 * @author ronger
 * @since 1.0.0
 */
@Tag(name = "微信公众号菜单管理", description = "微信公众号自定义菜单维护接口")
@Slf4j
@AdminController
@RequestMapping("/wechat/accounts/{accountId}/menu")
@RequiredArgsConstructor
public class WeChatMenuController {

    /**
     * 微信“菜单不存在”错误码（46003）。
     */
    private static final int MENU_NOT_FOUND_ERROR_CODE = 46003;

    private final DynamicWeChatServiceManager dynamicWeChatServiceManager;

    @Operation(summary = "获取公众号菜单", description = "读取指定微信账号当前已发布的自定义菜单")
    @GetMapping
    @ApiLog("获取公众号菜单")
    @PreAuthorize("hasAuthority('wechat:account:query')")
    public GlobalResult<WxMpMenu> getMenu(@PathVariable @Parameter(description = "账号ID") Long accountId) {
        try {
            log.info("读取微信公众号菜单，accountId: {}", accountId);
            WxMpMenu menu = dynamicWeChatServiceManager.getServiceByAccountId(accountId).getMenuService().menuGet();
            return GlobalResult.success(menu);
        } catch (WxErrorException e) {
            if (e.getError() != null && e.getError().getErrorCode() == MENU_NOT_FOUND_ERROR_CODE) {
                log.info("微信公众号尚未创建菜单，accountId: {}", accountId);
                return GlobalResult.success(new WxMpMenu());
            }
            log.warn("读取微信公众号菜单失败，accountId: {}, error: {}", accountId, e.getMessage(), e);
            return GlobalResult.error("读取微信公众号菜单失败: " + e.getMessage());
        }
    }

    @Operation(summary = "发布公众号菜单", description = "发布指定微信账号的自定义菜单")
    @PutMapping
    @ApiLog("发布公众号菜单")
    @OperationLog(module = "微信公众号菜单管理", operation = "发布公众号菜单", recordParams = true, recordResult = true)
    @PreAuthorize("hasAuthority('wechat:account:edit')")
    public GlobalResult<String> updateMenu(@PathVariable @Parameter(description = "账号ID") Long accountId,
                                           @RequestBody WxMenu menu) {
        try {
            log.info("发布微信公众号菜单，accountId: {}", accountId);
            String menuId = dynamicWeChatServiceManager.getServiceByAccountId(accountId).getMenuService().menuCreate(menu);
            return GlobalResult.success(menuId);
        } catch (WxErrorException e) {
            log.warn("发布微信公众号菜单失败，accountId: {}, error: {}", accountId, e.getMessage(), e);
            return GlobalResult.error("发布微信公众号菜单失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除公众号菜单", description = "删除指定微信账号当前已发布的自定义菜单")
    @DeleteMapping
    @ApiLog("删除公众号菜单")
    @OperationLog(module = "微信公众号菜单管理", operation = "删除公众号菜单")
    @PreAuthorize("hasAuthority('wechat:account:delete')")
    public GlobalResult<Boolean> deleteMenu(@PathVariable @Parameter(description = "账号ID") Long accountId) {
        try {
            log.info("删除微信公众号菜单，accountId: {}", accountId);
            dynamicWeChatServiceManager.getServiceByAccountId(accountId).getMenuService().menuDelete();
            return GlobalResult.success(true);
        } catch (WxErrorException e) {
            log.warn("删除微信公众号菜单失败，accountId: {}, error: {}", accountId, e.getMessage(), e);
            return GlobalResult.error("删除微信公众号菜单失败: " + e.getMessage());
        }
    }
}
