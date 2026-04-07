package com.rymcu.mortise.system.controller.facade;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.auth.model.OAuth2ClientConfigSearch;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.request.OAuth2ClientConfigUpsertRequest;
import com.rymcu.mortise.system.controller.vo.OAuth2ClientConfigVO;

public interface OAuth2ClientConfigAdminFacade {

    GlobalResult<PageResult<OAuth2ClientConfigVO>> list(OAuth2ClientConfigSearch search);

    GlobalResult<OAuth2ClientConfigVO> getById(Long id);

    GlobalResult<Long> createOAuth2ClientConfig(OAuth2ClientConfigUpsertRequest request);

    GlobalResult<Boolean> updateOAuth2ClientConfig(Long id, OAuth2ClientConfigUpsertRequest request);

    GlobalResult<Boolean> delete(Long id);

    GlobalResult<Boolean> batchDelete(BatchUpdateInfo batchUpdateInfo);
}
