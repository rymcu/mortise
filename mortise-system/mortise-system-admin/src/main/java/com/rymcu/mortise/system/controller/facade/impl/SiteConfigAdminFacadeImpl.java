package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.facade.SiteConfigAdminFacade;
import com.rymcu.mortise.system.model.SiteConfigGroupVO;
import com.rymcu.mortise.system.model.SiteConfigPublicVO;
import com.rymcu.mortise.system.model.SiteConfigSaveRequest;
import com.rymcu.mortise.system.service.SiteConfigService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SiteConfigAdminFacadeImpl implements SiteConfigAdminFacade {

    private final SiteConfigService siteConfigService;

    public SiteConfigAdminFacadeImpl(SiteConfigService siteConfigService) {
        this.siteConfigService = siteConfigService;
    }

    @Override
    public GlobalResult<List<SiteConfigGroupVO>> listGroups() {
        return GlobalResult.success(siteConfigService.listAllGroups());
    }

    @Override
    public GlobalResult<SiteConfigGroupVO> getGroup(String group) {
        return GlobalResult.success(siteConfigService.getGroup(group));
    }

    @Override
    public GlobalResult<Void> saveGroup(String group, SiteConfigSaveRequest request) {
        siteConfigService.saveGroup(group, request);
        return GlobalResult.success();
    }

    @Override
    public GlobalResult<SiteConfigPublicVO> getPublicConfig() {
        return GlobalResult.success(siteConfigService.getPublicConfig());
    }
}
