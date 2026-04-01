package com.rymcu.mortise.system.controller.assembler;

import com.rymcu.mortise.system.controller.request.DictTypeUpsertRequest;
import com.rymcu.mortise.system.controller.vo.DictTypeVO;
import com.rymcu.mortise.system.entity.DictType;

import java.util.List;

/**
 * 管理端字典类型视图装配器。
 */
public final class DictTypeAdminAssembler {

    private DictTypeAdminAssembler() {
    }

    public static List<DictTypeVO> toDictTypeVOs(List<DictType> dictTypes) {
        return dictTypes.stream()
                .map(DictTypeAdminAssembler::toDictTypeVO)
                .toList();
    }

    public static DictTypeVO toDictTypeVO(DictType dictType) {
        DictTypeVO dictTypeVO = new DictTypeVO();
        dictTypeVO.setId(dictType.getId());
        dictTypeVO.setLabel(dictType.getLabel());
        dictTypeVO.setTypeCode(dictType.getTypeCode());
        dictTypeVO.setDescription(dictType.getDescription());
        dictTypeVO.setSortNo(dictType.getSortNo());
        dictTypeVO.setStatus(dictType.getStatus());
        dictTypeVO.setDelFlag(dictType.getDelFlag());
        dictTypeVO.setCreatedBy(dictType.getCreatedBy());
        dictTypeVO.setCreatedTime(dictType.getCreatedTime());
        dictTypeVO.setUpdatedBy(dictType.getUpdatedBy());
        dictTypeVO.setUpdatedTime(dictType.getUpdatedTime());
        return dictTypeVO;
    }

    public static DictType toDictType(DictTypeUpsertRequest request) {
        DictType dictType = new DictType();
        dictType.setLabel(request.getLabel());
        dictType.setTypeCode(request.getTypeCode());
        dictType.setDescription(request.getDescription());
        dictType.setSortNo(request.getSortNo());
        dictType.setStatus(request.getStatus());
        return dictType;
    }
}
