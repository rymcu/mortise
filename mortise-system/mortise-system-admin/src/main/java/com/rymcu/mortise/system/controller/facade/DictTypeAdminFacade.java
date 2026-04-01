package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.controller.request.DictTypeStatusRequest;
import com.rymcu.mortise.system.controller.request.DictTypeUpsertRequest;
import com.rymcu.mortise.system.controller.vo.DictTypeVO;
import com.rymcu.mortise.system.model.DictTypeSearch;

public interface DictTypeAdminFacade {

    PageResult<DictTypeVO> listDictTypes(DictTypeSearch search);

    DictTypeVO getDictTypeById(Long dictTypeId);

    Long createDictType(DictTypeUpsertRequest request, CurrentUser currentUser);

    Boolean updateDictType(Long dictTypeId, DictTypeUpsertRequest request, CurrentUser currentUser);

    Boolean updateDictTypeStatus(Long dictTypeId, DictTypeStatusRequest request);

    Boolean deleteDictType(Long dictTypeId);

    Boolean batchDeleteDictTypes(BatchUpdateInfo batchUpdateInfo);
}
