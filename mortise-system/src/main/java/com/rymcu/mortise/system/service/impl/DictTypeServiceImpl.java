package com.rymcu.mortise.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.system.entity.DictType;
import com.rymcu.mortise.system.mapper.DictTypeMapper;
import com.rymcu.mortise.system.model.DictTypeSearch;
import com.rymcu.mortise.system.service.DictTypeService;
import com.rymcu.mortise.system.service.SystemCacheService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.rymcu.mortise.system.entity.table.DictTypeTableDef.DICT_TYPE;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictTypeService {

    @Resource
    private SystemCacheService systemCacheService;

    @Override
    public Page<DictType> findDictTypeList(Page<DictType> page, DictTypeSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(DICT_TYPE.LABEL.eq(search.getQuery(), StringUtils.isNotBlank(search.getQuery())))
                .and(DICT_TYPE.TYPE_CODE.eq(search.getTypeCode(), StringUtils.isNotBlank(search.getTypeCode())))
                .and(DICT_TYPE.STATUS.eq(search.getStatus(), Objects.nonNull(search.getStatus())));
        return mapper.paginate(page, queryWrapper);
    }

    @Override
    public Boolean updateStatus(Long idDictType, Integer status) {
        // 获取原始记录以确定字典类型代码
        DictType originalDictType = mapper.selectOneById(idDictType);
        if (originalDictType == null) {
            return false;
        }

        DictType dictType = UpdateEntity.of(DictType.class, idDictType);
        dictType.setStatus(status);
        boolean result = mapper.update(dictType) > 0;

        // 清除相关缓存 - DictType状态变更影响对应的Dict缓存
        if (result) {
            systemCacheService.removeDictOptions(originalDictType.getTypeCode());
        }

        return result;
    }

    @Override
    public Boolean deleteDictType(Long idDictType) {
        // 获取原始记录以确定字典类型代码
        DictType originalDictType = mapper.selectOneById(idDictType);
        if (originalDictType == null) {
            return false;
        }

        boolean result = mapper.deleteById(idDictType) > 0;

        // 清除相关缓存 - DictType删除时清除对应的Dict缓存
        if (result) {
            systemCacheService.removeDictOptions(originalDictType.getTypeCode());
        }

        return result;
    }

    @Override
    public DictType findById(Long idDictType) {
        return mapper.selectOneById(idDictType);
    }

    @Override
    public Boolean batchDeleteDictTypes(List<Long> idDictTypes) {
        if (idDictTypes == null || idDictTypes.isEmpty()) {
            return false;
        }

        // 获取所有受影响记录的字典类型代码
        List<DictType> affectedDictTypes = mapper.selectListByIds(idDictTypes);
        List<String> affectedTypeCodes = affectedDictTypes.stream()
                .map(DictType::getTypeCode)
                .distinct()
                .toList();

        boolean result = mapper.deleteBatchByIds(idDictTypes) > 0;

        // 清除相关缓存 - DictType批量删除时清除对应的Dict缓存
        if (result && !affectedTypeCodes.isEmpty()) {
            systemCacheService.removeDictOptionsBatch(affectedTypeCodes);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDictType(DictType dictType) {
        mapper.insertSelective(dictType);
        return dictType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDictType(DictType dictType) {
        String newTypeCode = dictType.getTypeCode();
        DictType oldDictType = mapper.selectOneById(dictType.getId());
        if (Objects.isNull(oldDictType)) {
            throw new ServiceException("数据不存在");
        }

        String oldTypeCode = oldDictType.getTypeCode();

        oldDictType.setLabel(dictType.getLabel());
        oldDictType.setTypeCode(dictType.getTypeCode());
        oldDictType.setSortNo(dictType.getSortNo());
        oldDictType.setStatus(dictType.getStatus());
        oldDictType.setUpdatedBy(dictType.getUpdatedBy());
        oldDictType.setUpdatedTime(dictType.getUpdatedTime());

        boolean result = mapper.update(oldDictType) > 0;

        // 清除相关缓存 - DictType变更影响对应的Dict缓存
        if (result) {
            systemCacheService.removeDictOptions(newTypeCode);
            if (!oldTypeCode.equals(newTypeCode)) {
                systemCacheService.removeDictOptions(oldTypeCode);
            }
        }

        return result;
    }
}
