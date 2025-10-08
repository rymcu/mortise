package com.rymcu.mortise.auth.controller;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import com.rymcu.mortise.auth.model.OAuth2ClientConfigSearch;
import com.rymcu.mortise.auth.service.Oauth2ClientConfigService;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.result.GlobalResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2 客户端配置管理接口
 *
 * @author ronger
 */
@Tag(name = "OAuth2客户端配置", description = "OAuth2 客户端配置管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/oauth2/client-configs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class Oauth2ClientConfigController {

	private final Oauth2ClientConfigService oauth2ClientConfigService;

	@Operation(summary = "获取客户端配置列表", description = "分页查询 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "查询成功"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@GetMapping
	public GlobalResult<Page<Oauth2ClientConfig>> list(@Parameter(description = "查询条件") OAuth2ClientConfigSearch search) {
		Page<Oauth2ClientConfig> page = new Page<>(search.getPageNum(), search.getPageSize());
		return GlobalResult.success(oauth2ClientConfigService.findOauth2ClientConfigs(page, search));
	}

	@Operation(summary = "获取客户端配置详情", description = "根据ID查询 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "查询成功"),
			@ApiResponse(responseCode = "404", description = "配置不存在"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@GetMapping("/{id}")
	public GlobalResult<Oauth2ClientConfig> getById(@Parameter(description = "配置ID", required = true) @PathVariable Long id) {
		return GlobalResult.success(oauth2ClientConfigService.getById(String.valueOf(id)));
	}

	@Operation(summary = "创建客户端配置", description = "新增 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "创建成功"),
			@ApiResponse(responseCode = "400", description = "参数错误"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@PostMapping
	public GlobalResult<Boolean> createOauth2ClientConfig(@Parameter(description = "客户端配置", required = true) @RequestBody Oauth2ClientConfig config) {
		return GlobalResult.success(oauth2ClientConfigService.createOauth2ClientConfig(config));
	}

	@Operation(summary = "更新客户端配置", description = "修改 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "更新成功"),
			@ApiResponse(responseCode = "400", description = "参数错误"),
			@ApiResponse(responseCode = "404", description = "配置不存在"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@PutMapping("/{id}")
	public GlobalResult<Boolean> updateOauth2ClientConfig(@Parameter(description = "配置ID", required = true) @PathVariable Long id,
									   @Parameter(description = "客户端配置", required = true) @RequestBody Oauth2ClientConfig config) {
        log.info("更新微信账号，id: {}", id);
        config.setId(id);
		return GlobalResult.success(oauth2ClientConfigService.updateOauth2ClientConfig(config));
	}

	@Operation(summary = "删除客户端配置", description = "删除 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "删除成功"),
			@ApiResponse(responseCode = "404", description = "配置不存在"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@DeleteMapping("/{id}")
	public GlobalResult<Boolean> delete(@Parameter(description = "配置ID", required = true) @PathVariable Long id) {
		return GlobalResult.success(oauth2ClientConfigService.deleteById(id));
	}

	@Operation(summary = "批量删除客户端配置", description = "批量删除 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "删除成功"),
			@ApiResponse(responseCode = "400", description = "参数错误"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@DeleteMapping("/batch")
	public GlobalResult<Boolean> batchDelete(@Parameter(description = "批量删除信息", required = true) @RequestBody BatchUpdateInfo batchUpdateInfo) {
		return GlobalResult.success(oauth2ClientConfigService.batchDeleteOAuth2ClientConfig(batchUpdateInfo.getIds()));
	}
}
