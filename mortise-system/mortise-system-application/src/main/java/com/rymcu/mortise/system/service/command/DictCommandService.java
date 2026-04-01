package com.rymcu.mortise.system.service.command;

import com.rymcu.mortise.system.entity.Dict;

import java.util.List;

public interface DictCommandService {

    Boolean updateStatus(Long dictId, Integer status);

    Boolean deleteDict(Long dictId);

    Boolean batchDeleteDictionaries(List<Long> dictIds);

    Long createDict(Dict dict);

    Boolean updateDict(Dict dict);
}
