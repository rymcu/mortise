package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.model.SiteConfigGroupVO;
import com.rymcu.mortise.system.model.SiteConfigPublicVO;
import com.rymcu.mortise.system.model.SiteConfigSaveRequest;

import java.util.List;

public interface SiteConfigAdminFacade {

    GlobalResult<List<SiteConfigGroupVO>> listGroups();

    GlobalResult<SiteConfigGroupVO> getGroup(String group);

    GlobalResult<Void> saveGroup(String group, SiteConfigSaveRequest request);

    GlobalResult<SiteConfigPublicVO> getPublicConfig();
}
