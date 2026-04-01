package com.rymcu.mortise.system.repository;

import com.rymcu.mortise.system.entity.DictType;

import java.util.List;

/**
 * 字典类型仓储端口。
 */
public interface DictTypeRepository {

    DictType findById(Long dictTypeId);

    List<DictType> findByIds(List<Long> dictTypeIds);

    boolean save(DictType dictType);

    boolean saveAll(List<DictType> dictTypes);

    boolean update(DictType dictType);

    boolean updateStatus(Long dictTypeId, Integer status);

    boolean deleteById(Long dictTypeId);

    boolean deleteByIds(List<Long> dictTypeIds);
}
