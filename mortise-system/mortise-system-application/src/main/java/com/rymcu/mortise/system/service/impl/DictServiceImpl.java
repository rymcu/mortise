package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.common.model.BaseOption;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.constant.SystemCacheConstant;
import com.rymcu.mortise.system.entity.Dict;
import com.rymcu.mortise.system.model.DictInfo;
import com.rymcu.mortise.system.model.DictSearch;
import com.rymcu.mortise.system.query.DictQueryService;
import com.rymcu.mortise.system.repository.DictRepository;
import com.rymcu.mortise.system.service.command.DictCommandService;
import com.rymcu.mortise.system.service.DictService;
import com.rymcu.mortise.system.service.SystemCacheService;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class DictServiceImpl implements DictService, DictCommandService {

    @Resource
    private DictRepository dictRepository;

    @Resource
    private SystemCacheService systemCacheService;
    @Resource
    private DictQueryService dictQueryService;

    @Override
    public PageResult<Dict> findDictList(PageQuery pageQuery, DictSearch search) {
        return dictQueryService.findDictList(pageQuery, search);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long idDict, Integer status) {
        Dict originalDict = dictRepository.findById(idDict);
        if (originalDict == null) {
            return false;
        }
        boolean result = dictRepository.updateStatus(idDict, status);
        if (result) {
            systemCacheService.removeDictOptions(originalDict.getDictTypeCode());
        }
        return result;
    }

    @Override
    public Boolean deleteDict(Long idDict) {
        Dict originalDict = dictRepository.findById(idDict);
        if (originalDict == null) {
            return false;
        }
        boolean result = dictRepository.deleteById(idDict);
        if (result) {
            systemCacheService.removeDictOptions(originalDict.getDictTypeCode());
        }
        return result;
    }

    @Override
    public Dict findById(Long idDict) {
        return dictRepository.findById(idDict);
    }

    @Override
    public String findLabelByTypeCodeAndValue(String dictTypeCode, String value) {
        return dictRepository.findLabelByTypeCodeAndValue(dictTypeCode, value);
    }

    @Override
    public DictInfo findDictInfo(String dictTypeCode, String value) {
        return dictRepository.findDictInfo(dictTypeCode, value);
    }

    @Override
    @Cacheable(value = SystemCacheConstant.DICT_DATA_CACHE, key = "#dictTypeCode")
    public List<BaseOption> queryDictOptions(String dictTypeCode) {
        @SuppressWarnings("unchecked")
        List<BaseOption> cachedOptions = systemCacheService.getDictOptions(dictTypeCode, List.class);
        if (cachedOptions != null) {
            return cachedOptions;
        }
        List<BaseOption> options = dictRepository.findOptions(dictTypeCode);
        if (options != null && !options.isEmpty()) {
            systemCacheService.storeDictOptions(dictTypeCode, options);
        }
        return options;
    }

    @Override
    public Boolean batchDeleteDictionaries(List<Long> idDictList) {
        if (idDictList == null || idDictList.isEmpty()) {
            return false;
        }
        List<String> affectedDictTypeCodes = dictRepository.findByIds(idDictList).stream()
                .map(Dict::getDictTypeCode)
                .distinct()
                .toList();
        boolean result = dictRepository.deleteByIds(idDictList);
        if (result && !affectedDictTypeCodes.isEmpty()) {
            systemCacheService.removeDictOptionsBatch(affectedDictTypeCodes);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = SystemCacheConstant.DICT_DATA_CACHE, key = "#dict.dictTypeCode")
    public Long createDict(Dict dict) {
        String dictTypeCode = dict.getDictTypeCode();
        boolean result = dictRepository.save(dict);
        if (result) {
            systemCacheService.removeDictOptions(dictTypeCode);
        }
        return dict.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = SystemCacheConstant.DICT_DATA_CACHE, key = "#dict.dictTypeCode")
    public Boolean updateDict(Dict dict) {
        String dictTypeCode = dict.getDictTypeCode();
        Dict oldDict = dictRepository.findById(dict.getId());
        if (oldDict == null) {
            throw new ServiceException("数据不存在");
        }
        String oldDictTypeCode = oldDict.getDictTypeCode();

        oldDict.setDictTypeCode(dict.getDictTypeCode());
        oldDict.setLabel(dict.getLabel());
        oldDict.setValue(dict.getValue());
        oldDict.setSortNo(dict.getSortNo());
        oldDict.setStatus(dict.getStatus());
        oldDict.setUpdatedBy(dict.getUpdatedBy());
        oldDict.setUpdatedTime(dict.getUpdatedTime());
        oldDict.setIcon(dict.getIcon());
        oldDict.setImage(dict.getImage());
        oldDict.setColor(dict.getColor());

        boolean result = dictRepository.update(oldDict);
        if (result) {
            systemCacheService.removeDictOptions(dictTypeCode);
            if (!oldDictTypeCode.equals(dictTypeCode)) {
                systemCacheService.removeDictOptions(oldDictTypeCode);
            }
        }
        return result;
    }
}
