package com.rymcu.mortise.system.controller.oauth2;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.auth.model.OAuth2ClientConfigSearch;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.controller.facade.OAuth2ClientConfigAdminFacade;
import com.rymcu.mortise.system.controller.request.OAuth2ClientConfigUpsertRequest;
import com.rymcu.mortise.system.controller.vo.OAuth2ClientConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2 客户端配置管理接口
 *
 * @author ronger
 */
@Tag(name = "OAuth2客户端配置", description = "OAuth2 客户端配置管理接口")
@AdminController
@RequestMapping("/oauth2/client-configs")
@RequiredArgsConstructor
public class Oauth2ClientConfigController {

	private final OAuth2ClientConfigAdminFacade oauth2ClientConfigAdminFacade;

	@Operation(summary = "获取客户端配置列表", description = "分页查询 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "查询成功"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@GetMapping
	@PreAuthorize("hasAuthority('system:oauth2-client:list')")
	@ApiLog(recordResponseBody = false, value = "获取OAuth2客户端配置列表")
	public GlobalResult<PageResult<OAuth2ClientConfigVO>> list(@Parameter(description = "查询条件") OAuth2ClientConfigSearch search) {
		return oauth2ClientConfigAdminFacade.list(search);
	}

	@Operation(summary = "获取客户端配置详情", description = "根据ID查询 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "查询成功"),
			@ApiResponse(responseCode = "404", description = "配置不存在"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('system:oauth2-client:query')")
	@ApiLog(recordResponseBody = false, value = "获取OAuth2客户端配置详情")
	public GlobalResult<OAuth2ClientConfigVO> getById(@Parameter(description = "配置ID", required = true) @PathVariable Long id) {
		return oauth2ClientConfigAdminFacade.getById(id);
	}

	@Operation(summary = "创建客户端配置", description = "新增 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "创建成功"),
			@ApiResponse(responseCode = "400", description = "参数错误"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@PostMapping
	@PreAuthorize("hasAuthority('system:oauth2-client:add')")
	@ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "创建OAuth2客户端配置")
	@OperationLog(module = "OAuth2客户端配置", operation = "创建OAuth2客户端配置", recordParams = false, recordResult = false)
	public GlobalResult<Long> createOauth2ClientConfig(
			@Parameter(description = "客户端配置", required = true) @RequestBody OAuth2ClientConfigUpsertRequest request) {
		return oauth2ClientConfigAdminFacade.createOAuth2ClientConfig(request);
	}

	@Operation(summary = "更新客户端配置", description = "修改 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "更新成功"),
			@ApiResponse(responseCode = "400", description = "参数错误"),
			@ApiResponse(responseCode = "404", description = "配置不存在"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('system:oauth2-client:edit')")
	@ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "更新OAuth2客户端配置")
	@OperationLog(module = "OAuth2客户端配置", operation = "更新OAuth2客户端配置", recordParams = false, recordResult = false)
	public GlobalResult<Boolean> updateOauth2ClientConfig(
			@Parameter(description = "配置ID", required = true) @PathVariable Long id,
			@Parameter(description = "客户端配置", required = true) @RequestBody OAuth2ClientConfigUpsertRequest request) {
		return oauth2ClientConfigAdminFacade.updateOAuth2ClientConfig(id, request);
	}

	@Operation(summary = "删除客户端配置", description = "删除 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "删除成功"),
			@ApiResponse(responseCode = "404", description = "配置不存在"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('system:oauth2-client:delete')")
	@ApiLog(recordParams = true, recordRequestBody = false, recordResponseBody = false, value = "删除OAuth2客户端配置")
	@OperationLog(module = "OAuth2客户端配置", operation = "删除OAuth2客户端配置", recordParams = true, recordResult = true)
	public GlobalResult<Boolean> delete(@Parameter(description = "配置ID", required = true) @PathVariable Long id) {
		return oauth2ClientConfigAdminFacade.delete(id);
	}

	@Operation(summary = "批量删除客户端配置", description = "批量删除 OAuth2 客户端配置")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "删除成功"),
			@ApiResponse(responseCode = "400", description = "参数错误"),
			@ApiResponse(responseCode = "403", description = "权限不足")
	})
	@DeleteMapping("/batch")
	@PreAuthorize("hasAuthority('system:oauth2-client:delete')")
	@ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "批量删除OAuth2客户端配置")
	@OperationLog(module = "OAuth2客户端配置", operation = "批量删除OAuth2客户端配置", recordParams = false, recordResult = true)
	public GlobalResult<Boolean> batchDelete(@Parameter(description = "批量删除信息", required = true) @RequestBody BatchUpdateInfo batchUpdateInfo) {
		return oauth2ClientConfigAdminFacade.batchDelete(batchUpdateInfo);
	}
}

