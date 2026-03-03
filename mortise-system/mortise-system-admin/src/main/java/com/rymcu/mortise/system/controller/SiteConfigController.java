package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.model.SiteConfigGroupVO;
import com.rymcu.mortise.system.model.SiteConfigPublicVO;
import com.rymcu.mortise.system.model.SiteConfigSaveRequest;
import com.rymcu.mortise.system.service.SiteConfigService;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 网站配置管理控制器
 * <p>
 * 提供网站基本信息（Logo、名称、Favicon）、SEO 等配置的查询与保存接口。
 *
 * @author ronger
 */
@Tag(name = "网站配置", description = "网站信息、SEO 等全局配置管理接口")
@AdminController
@RequestMapping("/system/site-config")
public class SiteConfigController {

    @Resource
    private SiteConfigService siteConfigService;

    @Operation(summary = "查询所有配置分组", description = "返回所有配置分组（含 Schema 字段定义 + 当前值）")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiLog("查询网站配置分组列表")
    public GlobalResult<List<SiteConfigGroupVO>> listGroups() {
        return GlobalResult.success(siteConfigService.listAllGroups());
    }

    @Operation(summary = "查询指定配置分组", description = "返回指定分组的配置详情")
    @GetMapping("/{group}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiLog("查询网站配置分组详情")
    public GlobalResult<SiteConfigGroupVO> getGroup(
            @Parameter(description = "分组标识，如 site / seo", required = true)
            @PathVariable String group) {
        return GlobalResult.success(siteConfigService.getGroup(group));
    }

    @Operation(summary = "保存配置分组", description = "全量覆盖保存指定分组的配置，保存后立即刷新缓存")
    @PutMapping("/{group}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiLog("保存网站配置分组")
    @OperationLog(module = "网站配置", operation = "保存配置分组", recordParams = true)
    public GlobalResult<Void> saveGroup(
            @Parameter(description = "分组标识，如 site / seo", required = true)
            @PathVariable String group,
            @Parameter(description = "配置内容", required = true)
            @Valid @RequestBody SiteConfigSaveRequest request) {
        siteConfigService.saveGroup(group, request);
        return GlobalResult.success();
    }

    @Operation(summary = "获取公开配置", description = "无需鉴权，供前端启动时加载系统名称、Logo 等基础信息")
    @GetMapping("/public")
    @ApiLog("获取网站公开配置")
    public GlobalResult<SiteConfigPublicVO> getPublicConfig() {
        return GlobalResult.success(siteConfigService.getPublicConfig());
    }
}
