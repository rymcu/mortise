package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.common.model.BaseOption;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.controller.request.DictStatusRequest;
import com.rymcu.mortise.system.controller.request.DictUpsertRequest;
import com.rymcu.mortise.system.controller.vo.DictVO;
import com.rymcu.mortise.system.model.DictSearch;

import java.util.List;

public interface DictAdminFacade {

    PageResult<DictVO> listDicts(DictSearch search);

    DictVO getDictById(Long dictId);

    Long createDict(DictUpsertRequest request, CurrentUser currentUser);

    Boolean updateDict(Long dictId, DictUpsertRequest request, CurrentUser currentUser);

    Boolean updateDictStatus(Long dictId, DictStatusRequest request);

    Boolean deleteDict(Long dictId);

    List<BaseOption> getDictOptions(String dictTypeCode);

    Boolean batchDeleteDictionaries(BatchUpdateInfo batchUpdateInfo);
}
