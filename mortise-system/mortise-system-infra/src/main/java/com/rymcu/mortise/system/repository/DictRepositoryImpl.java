package com.rymcu.mortise.system.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.common.model.BaseOption;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Dict;
import com.rymcu.mortise.system.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.system.infra.persistence.PersistenceObjectMapper;
import com.rymcu.mortise.system.infra.persistence.entity.DictPO;
import com.rymcu.mortise.system.mapper.DictMapper;
import com.rymcu.mortise.system.model.DictInfo;
import com.rymcu.mortise.system.model.DictSearch;
import com.rymcu.mortise.system.query.DictQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

import static com.rymcu.mortise.system.infra.persistence.entity.table.DictPOTableDef.DICT_PO;

/**
 * MyBatis-Flex 字典仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class DictRepositoryImpl implements DictRepository, DictQueryService {

    private final DictMapper dictMapper;

    @Override
    public PageResult<Dict> findDictList(PageQuery pageQuery, DictSearch search) {
        Page<Dict> page = dictMapper.paginateAs(FlexPageMapper.toFlexPage(pageQuery), QueryWrapper.create()
                .select()
                .where(DICT_PO.LABEL.eq(search.getQuery(), StringUtils.isNotBlank(search.getQuery())))
                .and(DICT_PO.DICT_TYPE_CODE.eq(search.getDictTypeCode(), StringUtils.isNotBlank(search.getDictTypeCode())))
                .and(DICT_PO.STATUS.eq(search.getStatus(), Objects.nonNull(search.getStatus()))), Dict.class);
        return FlexPageMapper.toPageResult(page);
    }

    @Override
    public Dict findById(Long dictId) {
        return dictMapper.selectOneByQueryAs(QueryWrapper.create().where(DICT_PO.ID.eq(dictId)), Dict.class);
    }

    @Override
    public List<Dict> findByIds(List<Long> dictIds) {
        return dictMapper.selectListByQueryAs(QueryWrapper.create().where(DICT_PO.ID.in(dictIds)), Dict.class);
    }

    @Override
    public String findLabelByTypeCodeAndValue(String dictTypeCode, String value) {
        return dictMapper.selectOneByQueryAs(QueryWrapper.create()
                .select(DICT_PO.LABEL)
                .where(DICT_PO.DICT_TYPE_CODE.eq(dictTypeCode).and(DICT_PO.VALUE.eq(value))), String.class);
    }

    @Override
    public DictInfo findDictInfo(String dictTypeCode, String value) {
        return dictMapper.selectOneByQueryAs(QueryWrapper.create()
                .select(DICT_PO.LABEL, DICT_PO.VALUE, DICT_PO.ICON, DICT_PO.IMAGE, DICT_PO.COLOR)
                .where(DICT_PO.DICT_TYPE_CODE.eq(dictTypeCode).and(DICT_PO.VALUE.eq(value))), DictInfo.class);
    }

    @Override
    public List<BaseOption> findOptions(String dictTypeCode) {
        return dictMapper.selectListByQueryAs(QueryWrapper.create()
                .select(DICT_PO.LABEL, DICT_PO.VALUE)
                .where(DICT_PO.DICT_TYPE_CODE.eq(dictTypeCode)), BaseOption.class);
    }

    @Override
    public List<BaseOption> queryDictOptions(String dictTypeCode) {
        return findOptions(dictTypeCode);
    }

    @Override
    public boolean save(Dict dict) {
        DictPO dictPO = PersistenceObjectMapper.copy(dict, DictPO::new);
        boolean saved = dictMapper.insertSelective(dictPO) > 0;
        if (saved) {
            dict.setId(dictPO.getId());
        }
        return saved;
    }

    @Override
    public boolean saveAll(List<Dict> dicts) {
        if (dicts == null || dicts.isEmpty()) {
            return true;
        }
        return dictMapper.insertBatchSelective(PersistenceObjectMapper.copyList(dicts, DictPO::new)) == dicts.size();
    }

    @Override
    public boolean update(Dict dict) {
        return dictMapper.update(PersistenceObjectMapper.copy(dict, DictPO::new)) > 0;
    }

    @Override
    public boolean updateStatus(Long dictId, Integer status) {
        return UpdateChain.of(DictPO.class)
                .set(DictPO::getStatus, status)
                .where(DictPO::getId).eq(dictId)
                .update();
    }

    @Override
    public boolean deleteById(Long dictId) {
        return dictMapper.deleteById(dictId) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> dictIds) {
        return dictMapper.deleteBatchByIds(dictIds) > 0;
    }
}
