package com.rymcu.mortise.system.service.command;

import com.rymcu.mortise.system.entity.DictType;

import java.util.List;

public interface DictTypeCommandService {

    Boolean updateStatus(Long dictTypeId, Integer status);

    Boolean deleteDictType(Long dictTypeId);

    Boolean batchDeleteDictTypes(List<Long> dictTypeIds);

    Long createDictType(DictType dictType);

    Boolean updateDictType(DictType dictType);
}
