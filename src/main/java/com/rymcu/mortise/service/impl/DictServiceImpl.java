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
import com.rymcu.mortise.service.DictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

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
    public Boolean saveDict(Dict dict) {
        boolean isUpdate = dict.getId() != null;
        if (isUpdate) {
            Dict oldDict = mapper.selectOneById(dict.getId());
            if (oldDict == null) {
                throw new ServiceException("数据不存在");
            }
            oldDict.setLabel(dict.getLabel());
            oldDict.setValue(dict.getValue());
            oldDict.setSortNo(dict.getSortNo());
            oldDict.setStatus(dict.getStatus());
            oldDict.setUpdatedBy(dict.getUpdatedBy());
            oldDict.setUpdatedTime(dict.getUpdatedTime());
            return mapper.update(oldDict) > 0;
        }
        return mapper.insert(dict) > 0;
    }

    @Override
    public Boolean updateStatus(Long idDict, Integer status) {
        Dict dict = UpdateEntity.of(Dict.class, idDict);
        dict.setStatus(status);
        return mapper.update(dict) > 0;
    }

    @Override
    public Boolean updateDelFlag(Long idDict, Integer delFlag) {
        return mapper.deleteById(idDict) > 0;
    }

    @Override
    public Dict findById(Long idDict) {
        return mapper.selectOneById(idDict);
    }

    @Override
    public String findLabelByTypeCodeAndValue(String dictTypeCode, String value) {
        Dict dict = mapper.selectByTypeCodeAndValue(dictTypeCode, value);
        if (Objects.isNull(dict) || StringUtils.isBlank(dict.getLabel())) {
            return null;
        }
        return dict.getLabel();
    }

    @Override
    public DictInfo findDictInfo(String dictTypeCode, String value) {
        return mapper.selectDictInfo(dictTypeCode, value);
    }

    @Override
    public List<BaseOption> queryDictOptions(String dictTypeCode) {
        return mapper.selectDictOptions(dictTypeCode);
    }

    @Override
    public Boolean batchUpdateDelFlag(List<Long> idDictList, Integer delFlag) {
        return mapper.deleteBatchByIds(idDictList) > 0;
    }
}
