package com.rymcu.mortise.system.controller.facade.impl;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import com.rymcu.mortise.auth.model.OAuth2ClientConfigSearch;
import com.rymcu.mortise.auth.service.Oauth2ClientConfigService;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.assembler.OAuth2ClientConfigAdminAssembler;
import com.rymcu.mortise.system.controller.facade.OAuth2ClientConfigAdminFacade;
import com.rymcu.mortise.system.controller.request.OAuth2ClientConfigUpsertRequest;
import com.rymcu.mortise.system.controller.vo.OAuth2ClientConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2ClientConfigAdminFacadeImpl implements OAuth2ClientConfigAdminFacade {

    private final Oauth2ClientConfigService oauth2ClientConfigService;

    public OAuth2ClientConfigAdminFacadeImpl(Oauth2ClientConfigService oauth2ClientConfigService) {
        this.oauth2ClientConfigService = oauth2ClientConfigService;
    }

    @Override
    public GlobalResult<Page<OAuth2ClientConfigVO>> list(OAuth2ClientConfigSearch search) {
        Page<Oauth2ClientConfig> page = new Page<>(search.getPageNum(), search.getPageSize());
        Page<Oauth2ClientConfig> result = oauth2ClientConfigService.findOauth2ClientConfigs(page, search);
        Page<OAuth2ClientConfigVO> voPage = new Page<>(result.getPageNumber(), result.getPageSize());
        voPage.setTotalRow(result.getTotalRow());
        voPage.setRecords(result.getRecords().stream()
                .map(OAuth2ClientConfigAdminAssembler::toVO)
                .toList());
        return GlobalResult.success(voPage);
    }

    @Override
    public GlobalResult<OAuth2ClientConfigVO> getById(Long id) {
        return GlobalResult.success(OAuth2ClientConfigAdminAssembler.toVO(oauth2ClientConfigService.getById(id)));
    }

    @Override
    public GlobalResult<Long> createOAuth2ClientConfig(OAuth2ClientConfigUpsertRequest request) {
        return GlobalResult.success(
                oauth2ClientConfigService.createOauth2ClientConfig(OAuth2ClientConfigAdminAssembler.toEntity(request))
        );
    }

    @Override
    public GlobalResult<Boolean> updateOAuth2ClientConfig(Long id, OAuth2ClientConfigUpsertRequest request) {
        log.info("更新微信账号，id: {}", id);
        Oauth2ClientConfig config = OAuth2ClientConfigAdminAssembler.toEntity(request);
        config.setId(id);
        return GlobalResult.success(oauth2ClientConfigService.updateOauth2ClientConfig(config));
    }

    @Override
    public GlobalResult<Boolean> delete(Long id) {
        return GlobalResult.success(oauth2ClientConfigService.deleteById(id));
    }

    @Override
    public GlobalResult<Boolean> batchDelete(BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(oauth2ClientConfigService.batchDeleteOAuth2ClientConfig(batchUpdateInfo.getIds()));
    }
}
