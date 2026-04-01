package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.DictType;
import com.rymcu.mortise.system.model.DictTypeSearch;
import com.rymcu.mortise.system.query.DictTypeQueryService;
import com.rymcu.mortise.system.repository.DictTypeRepository;
import com.rymcu.mortise.system.service.command.DictTypeCommandService;
import com.rymcu.mortise.system.service.DictTypeService;
import com.rymcu.mortise.system.service.SystemCacheService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class DictTypeServiceImpl implements DictTypeService, DictTypeCommandService {

    @Resource
    private DictTypeRepository dictTypeRepository;

    @Resource
    private SystemCacheService systemCacheService;
    @Resource
    private DictTypeQueryService dictTypeQueryService;

    @Override
    public PageResult<DictType> findDictTypeList(PageQuery pageQuery, DictTypeSearch search) {
        return dictTypeQueryService.findDictTypeList(pageQuery, search);
    }

    @Override
    public Boolean updateStatus(Long idDictType, Integer status) {
        DictType originalDictType = dictTypeRepository.findById(idDictType);
        if (originalDictType == null) {
            return false;
        }
        boolean result = dictTypeRepository.updateStatus(idDictType, status);
        if (result) {
            systemCacheService.removeDictOptions(originalDictType.getTypeCode());
        }
        return result;
    }

    @Override
    public Boolean deleteDictType(Long idDictType) {
        DictType originalDictType = dictTypeRepository.findById(idDictType);
        if (originalDictType == null) {
            return false;
        }
        boolean result = dictTypeRepository.deleteById(idDictType);
        if (result) {
            systemCacheService.removeDictOptions(originalDictType.getTypeCode());
        }
        return result;
    }

    @Override
    public DictType findById(Long idDictType) {
        return dictTypeRepository.findById(idDictType);
    }

    @Override
    public Boolean batchDeleteDictTypes(List<Long> idDictTypes) {
        if (idDictTypes == null || idDictTypes.isEmpty()) {
            return false;
        }
        List<String> affectedTypeCodes = dictTypeRepository.findByIds(idDictTypes).stream()
                .map(DictType::getTypeCode)
                .distinct()
                .toList();
        boolean result = dictTypeRepository.deleteByIds(idDictTypes);
        if (result && !affectedTypeCodes.isEmpty()) {
            systemCacheService.removeDictOptionsBatch(affectedTypeCodes);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDictType(DictType dictType) {
        dictTypeRepository.save(dictType);
        return dictType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDictType(DictType dictType) {
        String newTypeCode = dictType.getTypeCode();
        DictType oldDictType = dictTypeRepository.findById(dictType.getId());
        if (Objects.isNull(oldDictType)) {
            throw new ServiceException("数据不存在");
        }

        String oldTypeCode = oldDictType.getTypeCode();
        oldDictType.setLabel(dictType.getLabel());
        oldDictType.setTypeCode(dictType.getTypeCode());
        oldDictType.setDescription(dictType.getDescription());
        oldDictType.setSortNo(dictType.getSortNo());
        oldDictType.setStatus(dictType.getStatus());
        oldDictType.setUpdatedBy(dictType.getUpdatedBy());
        oldDictType.setUpdatedTime(dictType.getUpdatedTime());

        boolean result = dictTypeRepository.update(oldDictType);
        if (result) {
            systemCacheService.removeDictOptions(newTypeCode);
            if (!oldTypeCode.equals(newTypeCode)) {
                systemCacheService.removeDictOptions(oldTypeCode);
            }
        }
        return result;
    }
}
