package com.rymcu.mortise.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.core.exception.ServiceException;
import com.rymcu.mortise.entity.DictType;
import com.rymcu.mortise.mapper.DictTypeMapper;
import com.rymcu.mortise.model.DictTypeSearch;
import com.rymcu.mortise.service.DictTypeService;
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
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictTypeService {

    @Override
    public Page<DictType> findDictTypeList(Page<DictType> page, DictTypeSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .eq("label", search.getQuery(), StringUtils.isNotBlank(search.getQuery()))
                .eq("type_code", search.getTypeCode(), StringUtils.isNotBlank(search.getTypeCode()))
                .eq("status", search.getStatus(), Objects.nonNull(search.getStatus()));;
        return mapper.paginate(page, queryWrapper);
    }

    @Override
    public Boolean saveDictType(DictType dictType) {
        boolean isUpdate = dictType.getId() != null;
        if (isUpdate) {
            DictType oldDictType = mapper.selectOneById(dictType.getId());
            if (oldDictType == null) {
                throw new ServiceException("数据不存在");
            }
            oldDictType.setLabel(dictType.getLabel());
            oldDictType.setTypeCode(dictType.getTypeCode());
            oldDictType.setSortNo(dictType.getSortNo());
            oldDictType.setStatus(dictType.getStatus());
            oldDictType.setUpdatedBy(dictType.getUpdatedBy());
            oldDictType.setUpdatedTime(dictType.getUpdatedTime());
            return mapper.update(oldDictType) > 0;
        }
        return mapper.insertSelective(dictType) > 0;
    }

    @Override
    public Boolean updateStatus(Long idDictType, Integer status) {
        DictType dictType = UpdateEntity.of(DictType.class, idDictType);
        dictType.setStatus(status);
        return mapper.update(dictType) > 0;
    }

    @Override
    public Boolean updateDelFlag(Long idDictType, Integer delFlag) {
        return mapper.deleteById(idDictType) > 0;
    }

    @Override
    public DictType findById(Long idDictType) {
        return mapper.selectOneById(idDictType);
    }

    @Override
    public Boolean batchUpdateDelFlag(List<Long> idDictTypes, Integer delFlag) {
        return mapper.deleteBatchByIds(idDictTypes) > 0;
    }
}
