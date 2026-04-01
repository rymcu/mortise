package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.controller.assembler.DictTypeAdminAssembler;
import com.rymcu.mortise.system.controller.facade.DictTypeAdminFacade;
import com.rymcu.mortise.system.controller.request.DictTypeStatusRequest;
import com.rymcu.mortise.system.controller.request.DictTypeUpsertRequest;
import com.rymcu.mortise.system.controller.vo.DictTypeVO;
import com.rymcu.mortise.system.entity.DictType;
import com.rymcu.mortise.system.model.DictTypeSearch;
import com.rymcu.mortise.system.query.DictTypeQueryService;
import com.rymcu.mortise.system.service.command.DictTypeCommandService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DictTypeAdminFacadeImpl implements DictTypeAdminFacade {

    private final DictTypeCommandService dictTypeCommandService;
    private final DictTypeQueryService dictTypeQueryService;

    public DictTypeAdminFacadeImpl(DictTypeCommandService dictTypeCommandService, DictTypeQueryService dictTypeQueryService) {
        this.dictTypeCommandService = dictTypeCommandService;
        this.dictTypeQueryService = dictTypeQueryService;
    }

    @Override
    public PageResult<DictTypeVO> listDictTypes(DictTypeSearch search) {
        PageResult<DictType> result = dictTypeQueryService.findDictTypeList(PageQuery.of(search.getPageNum(), search.getPageSize()), search);
        return result.map(DictTypeAdminAssembler::toDictTypeVO);
    }

    @Override
    public DictTypeVO getDictTypeById(Long dictTypeId) {
        return DictTypeAdminAssembler.toDictTypeVO(dictTypeQueryService.findById(dictTypeId));
    }

    @Override
    public Long createDictType(DictTypeUpsertRequest request, CurrentUser currentUser) {
        DictType dictType = DictTypeAdminAssembler.toDictType(request);
        dictType.setCreatedBy(currentUser.getUserId());
        return dictTypeCommandService.createDictType(dictType);
    }

    @Override
    public Boolean updateDictType(Long dictTypeId, DictTypeUpsertRequest request, CurrentUser currentUser) {
        DictType dictType = DictTypeAdminAssembler.toDictType(request);
        dictType.setId(dictTypeId);
        dictType.setUpdatedBy(currentUser.getUserId());
        dictType.setUpdatedTime(LocalDateTime.now());
        return dictTypeCommandService.updateDictType(dictType);
    }

    @Override
    public Boolean updateDictTypeStatus(Long dictTypeId, DictTypeStatusRequest request) {
        return dictTypeCommandService.updateStatus(dictTypeId, request.getStatus());
    }

    @Override
    public Boolean deleteDictType(Long dictTypeId) {
        return dictTypeCommandService.deleteDictType(dictTypeId);
    }

    @Override
    public Boolean batchDeleteDictTypes(BatchUpdateInfo batchUpdateInfo) {
        return dictTypeCommandService.batchDeleteDictTypes(batchUpdateInfo.getIds());
    }
}
