package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.common.model.BaseOption;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.controller.assembler.DictAdminAssembler;
import com.rymcu.mortise.system.controller.facade.DictAdminFacade;
import com.rymcu.mortise.system.controller.request.DictStatusRequest;
import com.rymcu.mortise.system.controller.request.DictUpsertRequest;
import com.rymcu.mortise.system.controller.vo.DictVO;
import com.rymcu.mortise.system.entity.Dict;
import com.rymcu.mortise.system.model.DictSearch;
import com.rymcu.mortise.system.query.DictQueryService;
import com.rymcu.mortise.system.service.command.DictCommandService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DictAdminFacadeImpl implements DictAdminFacade {

    private final DictCommandService dictCommandService;
    private final DictQueryService dictQueryService;

    public DictAdminFacadeImpl(DictCommandService dictCommandService, DictQueryService dictQueryService) {
        this.dictCommandService = dictCommandService;
        this.dictQueryService = dictQueryService;
    }

    @Override
    public PageResult<DictVO> listDicts(DictSearch search) {
        PageResult<Dict> result = dictQueryService.findDictList(PageQuery.of(search.getPageNum(), search.getPageSize()), search);
        return result.map(DictAdminAssembler::toDictVO);
    }

    @Override
    public DictVO getDictById(Long dictId) {
        return DictAdminAssembler.toDictVO(dictQueryService.findById(dictId));
    }

    @Override
    public Long createDict(DictUpsertRequest request, CurrentUser currentUser) {
        Dict dict = DictAdminAssembler.toDict(request);
        dict.setCreatedBy(currentUser.getUserId());
        return dictCommandService.createDict(dict);
    }

    @Override
    public Boolean updateDict(Long dictId, DictUpsertRequest request, CurrentUser currentUser) {
        Dict dict = DictAdminAssembler.toDict(request);
        dict.setId(dictId);
        dict.setUpdatedBy(currentUser.getUserId());
        dict.setUpdatedTime(LocalDateTime.now());
        return dictCommandService.updateDict(dict);
    }

    @Override
    public Boolean updateDictStatus(Long dictId, DictStatusRequest request) {
        return dictCommandService.updateStatus(dictId, request.getStatus());
    }

    @Override
    public Boolean deleteDict(Long dictId) {
        return dictCommandService.deleteDict(dictId);
    }

    @Override
    public List<BaseOption> getDictOptions(String dictTypeCode) {
        return dictQueryService.queryDictOptions(dictTypeCode);
    }

    @Override
    public Boolean batchDeleteDictionaries(BatchUpdateInfo batchUpdateInfo) {
        return dictCommandService.batchDeleteDictionaries(batchUpdateInfo.getIds());
    }
}
