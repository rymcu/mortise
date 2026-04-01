package com.rymcu.mortise.system.controller.assembler;

import com.rymcu.mortise.system.controller.request.DictUpsertRequest;
import com.rymcu.mortise.system.controller.vo.DictVO;
import com.rymcu.mortise.system.entity.Dict;

import java.util.List;

/**
 * 管理端字典视图装配器。
 */
public final class DictAdminAssembler {

    private DictAdminAssembler() {
    }

    public static List<DictVO> toDictVOs(List<Dict> dictionaries) {
        return dictionaries.stream()
                .map(DictAdminAssembler::toDictVO)
                .toList();
    }

    public static DictVO toDictVO(Dict dict) {
        DictVO dictVO = new DictVO();
        dictVO.setId(dict.getId());
        dictVO.setDictTypeCode(dict.getDictTypeCode());
        dictVO.setLabel(dict.getLabel());
        dictVO.setValue(dict.getValue());
        dictVO.setSortNo(dict.getSortNo());
        dictVO.setStatus(dict.getStatus());
        dictVO.setDelFlag(dict.getDelFlag());
        dictVO.setCreatedBy(dict.getCreatedBy());
        dictVO.setCreatedTime(dict.getCreatedTime());
        dictVO.setUpdatedBy(dict.getUpdatedBy());
        dictVO.setUpdatedTime(dict.getUpdatedTime());
        dictVO.setIcon(dict.getIcon());
        dictVO.setImage(dict.getImage());
        dictVO.setColor(dict.getColor());
        return dictVO;
    }

    public static Dict toDict(DictUpsertRequest request) {
        Dict dict = new Dict();
        dict.setDictTypeCode(request.getDictTypeCode());
        dict.setLabel(request.getLabel());
        dict.setValue(request.getValue());
        dict.setSortNo(request.getSortNo());
        dict.setStatus(request.getStatus());
        dict.setIcon(request.getIcon());
        dict.setImage(request.getImage());
        dict.setColor(request.getColor());
        return dict;
    }
}
