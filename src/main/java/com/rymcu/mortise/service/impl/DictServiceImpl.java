package com.rymcu.mortise.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.core.exception.ServiceException;
import com.rymcu.mortise.entity.Dict;
import com.rymcu.mortise.mapper.DictMapper;
import com.rymcu.mortise.model.BaseOption;
import com.rymcu.mortise.model.DictInfo;
import com.rymcu.mortise.model.DictSearch;
import com.rymcu.mortise.service.CacheService;
import com.rymcu.mortise.service.DictService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.rymcu.mortise.entity.table.DictTableDef.DICT;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Resource
    private CacheService cacheService;

    @Override
    public Page<Dict> findDictList(Page<Dict> page, DictSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .eq("label", search.getQuery(), StringUtils.isNotBlank(search.getQuery()))
                .eq("dict_type_code", search.getDictTypeCode(), StringUtils.isNotBlank(search.getDictTypeCode()))
                .eq("status", search.getStatus(), Objects.nonNull(search.getStatus()));
        return mapper.paginate(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dictData", key = "#dict.dictTypeCode")
    public Boolean saveDict(Dict dict) {
        boolean isUpdate = dict.getId() != null;
        String dictTypeCode = dict.getDictTypeCode();

        if (isUpdate) {
            Dict oldDict = mapper.selectOneById(dict.getId());
            if (oldDict == null) {
                throw new ServiceException("数据不存在");
            }
            // 如果字典类型代码发生变化，需要清除两个类型的缓存
            String oldDictTypeCode = oldDict.getDictTypeCode();

            oldDict.setLabel(dict.getLabel());
            oldDict.setValue(dict.getValue());
            oldDict.setSortNo(dict.getSortNo());
            oldDict.setStatus(dict.getStatus());
            oldDict.setUpdatedBy(dict.getUpdatedBy());
            oldDict.setUpdatedTime(dict.getUpdatedTime());

            boolean result = mapper.update(oldDict) > 0;

            // 清除相关缓存
            if (result) {
                cacheService.removeDictOptions(dictTypeCode);
                if (!oldDictTypeCode.equals(dictTypeCode)) {
                    cacheService.removeDictOptions(oldDictTypeCode);
                }
            }

            return result;
        }

        boolean result = mapper.insertSelective(dict) > 0;

        // 清除相关缓存
        if (result) {
            cacheService.removeDictOptions(dictTypeCode);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long idDict, Integer status) {
        // 获取原始记录以确定字典类型代码
        Dict originalDict = mapper.selectOneById(idDict);
        if (originalDict == null) {
            return false;
        }

        Dict dict = UpdateEntity.of(Dict.class, idDict);
        dict.setStatus(status);
        boolean result = mapper.update(dict) > 0;

        // 清除相关缓存
        if (result) {
            cacheService.removeDictOptions(originalDict.getDictTypeCode());
        }

        return result;
    }

    @Override
    public Boolean deleteDict(Long idDict) {
        // 获取原始记录以确定字典类型代码
        Dict originalDict = mapper.selectOneById(idDict);
        if (originalDict == null) {
            return false;
        }

        boolean result = mapper.deleteById(idDict) > 0;

        // 清除相关缓存
        if (result) {
            cacheService.removeDictOptions(originalDict.getDictTypeCode());
        }

        return result;
    }

    @Override
    public Dict findById(Long idDict) {
        return mapper.selectOneById(idDict);
    }

    @Override
    public String findLabelByTypeCodeAndValue(String dictTypeCode, String value) {
        return mapper.selectOneByQueryAs(QueryWrapper.create()
                .select(DICT.LABEL)
                .where(DICT.DICT_TYPE_CODE.eq(dictTypeCode)
                        .and(DICT.VALUE.eq(value))), String.class);
    }

    @Override
    public DictInfo findDictInfo(String dictTypeCode, String value) {
        return mapper.selectOneByQueryAs(QueryWrapper.create()
                .select(DICT.LABEL, DICT.VALUE, DICT.ICON, DICT.IMAGE, DICT.COLOR)
                .where(DICT.DICT_TYPE_CODE.eq(dictTypeCode)
                        .and(DICT.VALUE.eq(value))), DictInfo.class);
    }

    @Override
    @Cacheable(value = "dictData", key = "#dictTypeCode")
    public List<BaseOption> queryDictOptions(String dictTypeCode) {
        // 先尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<BaseOption> cachedOptions = cacheService.getDictOptions(dictTypeCode, List.class);
        if (cachedOptions != null) {
            return cachedOptions;
        }

        // 缓存未命中，从数据库查询
        List<BaseOption> options = mapper.selectListByQueryAs(QueryWrapper.create()
                .select(DICT.LABEL, DICT.VALUE)
                .where(DICT.DICT_TYPE_CODE.eq(dictTypeCode)), BaseOption.class);

        // 存储到缓存
        if (options != null && !options.isEmpty()) {
            cacheService.storeDictOptions(dictTypeCode, options);
        }

        return options;
    }

    @Override
    public Boolean batchDeleteDictionaries(List<Long> idDictList) {
        if (idDictList == null || idDictList.isEmpty()) {
            return false;
        }

        // 获取所有受影响记录的字典类型代码
        List<Dict> affectedDictionaries = mapper.selectListByIds(idDictList);
        List<String> affectedDictTypeCodes = affectedDictionaries.stream()
                .map(Dict::getDictTypeCode)
                .distinct()
                .toList();

        boolean result = mapper.deleteBatchByIds(idDictList) > 0;

        // 清除相关缓存
        if (result && !affectedDictTypeCodes.isEmpty()) {
            cacheService.removeDictOptionsBatch(affectedDictTypeCodes);
        }

        return result;
    }
}
