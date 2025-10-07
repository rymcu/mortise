package com.rymcu.mortise.auth.controller;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import com.rymcu.mortise.auth.model.OAuth2ClientConfigSearch;
import com.rymcu.mortise.auth.service.Oauth2ClientConfigService;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.result.GlobalResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2 客户端配置管理接口
 *
 * @author ronger
 */
@RestController
@RequestMapping("/api/v1/admin/oauth2/client-configs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class Oauth2ClientConfigController {

	private final Oauth2ClientConfigService oauth2ClientConfigService;

	@GetMapping
	public GlobalResult<Page<Oauth2ClientConfig>> list(OAuth2ClientConfigSearch search) {
		// 只返回启用的配置
        Page<Oauth2ClientConfig> page = new Page<>(search.getPageNum(), search.getPageSize());
		return GlobalResult.success(oauth2ClientConfigService.findOauth2ClientConfigs(page, search));
	}

	@GetMapping("/{id}")
	public GlobalResult<Oauth2ClientConfig> getById(@PathVariable Long id) {
		// 这里只做简单演示，实际可加异常处理
		return GlobalResult.success(oauth2ClientConfigService.getById(String.valueOf(id)));
	}

	@PostMapping
	public GlobalResult<Boolean> create(@RequestBody Oauth2ClientConfig config) {
		return GlobalResult.success(oauth2ClientConfigService.saveOauth2ClientConfig(config));
	}

	@PutMapping("/{id}")
	public GlobalResult<Boolean> update(@PathVariable Long id, @RequestBody Oauth2ClientConfig config) {
		config.setId(id);
		return GlobalResult.success(oauth2ClientConfigService.saveOauth2ClientConfig(config));
	}

	@DeleteMapping("/{id}")
	public GlobalResult<Boolean> delete(@PathVariable Long id) {
		return GlobalResult.success(oauth2ClientConfigService.deleteById(id));
	}

	@DeleteMapping("/batch")
	public GlobalResult<Boolean> batchDelete(@RequestBody BatchUpdateInfo batchUpdateInfo) {
		return GlobalResult.success(oauth2ClientConfigService.batchDeleteOAuth2ClientConfig(batchUpdateInfo.getIds()));
	}
}
