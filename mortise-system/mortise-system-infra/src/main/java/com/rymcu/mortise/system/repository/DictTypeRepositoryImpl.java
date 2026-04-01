package com.rymcu.mortise.system.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.DictType;
import com.rymcu.mortise.system.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.system.infra.persistence.PersistenceObjectMapper;
import com.rymcu.mortise.system.infra.persistence.entity.DictTypePO;
import com.rymcu.mortise.system.mapper.DictTypeMapper;
import com.rymcu.mortise.system.model.DictTypeSearch;
import com.rymcu.mortise.system.query.DictTypeQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

import static com.rymcu.mortise.system.infra.persistence.entity.table.DictTypePOTableDef.DICT_TYPE_PO;

/**
 * MyBatis-Flex 字典类型仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class DictTypeRepositoryImpl implements DictTypeRepository, DictTypeQueryService {

    private final DictTypeMapper dictTypeMapper;

    @Override
    public PageResult<DictType> findDictTypeList(PageQuery pageQuery, DictTypeSearch search) {
        Page<DictType> page = dictTypeMapper.paginateAs(FlexPageMapper.toFlexPage(pageQuery), QueryWrapper.create()
                .select()
                .where(DICT_TYPE_PO.LABEL.eq(search.getQuery(), StringUtils.isNotBlank(search.getQuery())))
                .and(DICT_TYPE_PO.TYPE_CODE.eq(search.getTypeCode(), StringUtils.isNotBlank(search.getTypeCode())))
                .and(DICT_TYPE_PO.STATUS.eq(search.getStatus(), Objects.nonNull(search.getStatus()))), DictType.class);
        return FlexPageMapper.toPageResult(page);
    }

    @Override
    public DictType findById(Long dictTypeId) {
        return dictTypeMapper.selectOneByQueryAs(QueryWrapper.create().where(DICT_TYPE_PO.ID.eq(dictTypeId)), DictType.class);
    }

    @Override
    public List<DictType> findByIds(List<Long> dictTypeIds) {
        return dictTypeMapper.selectListByQueryAs(QueryWrapper.create().where(DICT_TYPE_PO.ID.in(dictTypeIds)), DictType.class);
    }

    @Override
    public boolean save(DictType dictType) {
        DictTypePO dictTypePO = PersistenceObjectMapper.copy(dictType, DictTypePO::new);
        boolean saved = dictTypeMapper.insertSelective(dictTypePO) > 0;
        if (saved) {
            dictType.setId(dictTypePO.getId());
        }
        return saved;
    }

    @Override
    public boolean saveAll(List<DictType> dictTypes) {
        if (dictTypes == null || dictTypes.isEmpty()) {
            return true;
        }
        return dictTypeMapper.insertBatchSelective(PersistenceObjectMapper.copyList(dictTypes, DictTypePO::new)) == dictTypes.size();
    }

    @Override
    public boolean update(DictType dictType) {
        return dictTypeMapper.update(PersistenceObjectMapper.copy(dictType, DictTypePO::new)) > 0;
    }

    @Override
    public boolean updateStatus(Long dictTypeId, Integer status) {
        return UpdateChain.of(DictTypePO.class)
                .set(DictTypePO::getStatus, status)
                .where(DictTypePO::getId).eq(dictTypeId)
                .update();
    }

    @Override
    public boolean deleteById(Long dictTypeId) {
        return dictTypeMapper.deleteById(dictTypeId) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> dictTypeIds) {
        return dictTypeMapper.deleteBatchByIds(dictTypeIds) > 0;
    }
}
