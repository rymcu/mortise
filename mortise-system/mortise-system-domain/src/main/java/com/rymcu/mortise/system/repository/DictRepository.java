package com.rymcu.mortise.system.repository;

import com.rymcu.mortise.common.model.BaseOption;
import com.rymcu.mortise.system.entity.Dict;
import com.rymcu.mortise.system.model.DictInfo;

import java.util.List;

/**
 * 字典仓储端口。
 */
public interface DictRepository {

    Dict findById(Long dictId);

    List<Dict> findByIds(List<Long> dictIds);

    String findLabelByTypeCodeAndValue(String dictTypeCode, String value);

    DictInfo findDictInfo(String dictTypeCode, String value);

    List<BaseOption> findOptions(String dictTypeCode);

    boolean save(Dict dict);

    boolean saveAll(List<Dict> dicts);

    boolean update(Dict dict);

    boolean updateStatus(Long dictId, Integer status);

    boolean deleteById(Long dictId);

    boolean deleteByIds(List<Long> dictIds);
}
