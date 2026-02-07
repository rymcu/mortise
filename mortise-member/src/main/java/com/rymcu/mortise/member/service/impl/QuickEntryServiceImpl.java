package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.member.entity.QuickEntry;
import com.rymcu.mortise.member.mapper.QuickEntryMapper;
import com.rymcu.mortise.member.model.QuickEntrySearch;
import com.rymcu.mortise.member.service.QuickEntryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.rymcu.mortise.member.entity.table.QuickEntryTableDef.QUICK_ENTRY;

/**
 * 金刚区快捷入口服务实现
 *
 * @author ronger
 */
@Service
@Primary
public class QuickEntryServiceImpl extends ServiceImpl<QuickEntryMapper, QuickEntry> implements QuickEntryService {

    @Override
    public Page<QuickEntry> findEntryList(Page<QuickEntry> page, QuickEntrySearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(QUICK_ENTRY.NAME.like(search.getQuery(), StringUtils.isNotBlank(search.getQuery())))
                .and(QUICK_ENTRY.POSITION.eq(search.getPosition(), StringUtils.isNotBlank(search.getPosition())))
                .and(QUICK_ENTRY.PLATFORM.eq(search.getPlatform(), StringUtils.isNotBlank(search.getPlatform())))
                .and(QUICK_ENTRY.STATUS.eq(search.getStatus(), Objects.nonNull(search.getStatus())))
                .and(QUICK_ENTRY.GROUP_NAME.eq(search.getGroupName(), StringUtils.isNotBlank(search.getGroupName())))
                .and(QUICK_ENTRY.ROW_INDEX.eq(search.getRowIndex(), Objects.nonNull(search.getRowIndex())))
                .orderBy(QUICK_ENTRY.ROW_INDEX.asc(), QUICK_ENTRY.SORT_NO.asc(), QUICK_ENTRY.CREATED_TIME.desc());
        return mapper.paginate(page, queryWrapper);
    }

    @Override
    public List<QuickEntry> findActiveEntries(String position, String platform) {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(QUICK_ENTRY.STATUS.eq(1))
                .and(QUICK_ENTRY.POSITION.eq(position, StringUtils.isNotBlank(position)))
                .and(QUICK_ENTRY.PLATFORM.in(List.of("all", platform), StringUtils.isNotBlank(platform)))
                .and(QUICK_ENTRY.START_TIME.isNull().or(QUICK_ENTRY.START_TIME.le(now)))
                .and(QUICK_ENTRY.END_TIME.isNull().or(QUICK_ENTRY.END_TIME.ge(now)))
                .orderBy(QUICK_ENTRY.ROW_INDEX.asc(), QUICK_ENTRY.SORT_NO.asc());
        return list(queryWrapper);
    }

    @Override
    public Map<Integer, List<QuickEntry>> findActiveEntriesGroupByRow(String position, String platform) {
        List<QuickEntry> entries = findActiveEntries(position, platform);
        return entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getRowIndex() != null ? entry.getRowIndex() : 1,
                        Collectors.toList()
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incrementClickCount(Long id) {
        QuickEntry entry = getById(id);
        if (entry == null) {
            throw new ServiceException("快捷入口不存在");
        }
        QuickEntry updateEntry = UpdateEntity.of(QuickEntry.class, id);
        updateEntry.setClickCount((entry.getClickCount() != null ? entry.getClickCount() : 0) + 1);
        return updateById(updateEntry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enable(Long id) {
        QuickEntry entry = getById(id);
        if (entry == null) {
            throw new ServiceException("快捷入口不存在");
        }
        QuickEntry updateEntry = UpdateEntity.of(QuickEntry.class, id);
        updateEntry.setStatus(1);
        updateEntry.setUpdatedTime(LocalDateTime.now());
        return updateById(updateEntry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disable(Long id) {
        QuickEntry entry = getById(id);
        if (entry == null) {
            throw new ServiceException("快捷入口不存在");
        }
        QuickEntry updateEntry = UpdateEntity.of(QuickEntry.class, id);
        updateEntry.setStatus(0);
        updateEntry.setUpdatedTime(LocalDateTime.now());
        return updateById(updateEntry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSort(Long id, Integer sortNo) {
        QuickEntry entry = getById(id);
        if (entry == null) {
            throw new ServiceException("快捷入口不存在");
        }
        QuickEntry updateEntry = UpdateEntity.of(QuickEntry.class, id);
        updateEntry.setSortNo(sortNo);
        updateEntry.setUpdatedTime(LocalDateTime.now());
        return updateById(updateEntry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRowIndex(Long id, Integer rowIndex) {
        QuickEntry entry = getById(id);
        if (entry == null) {
            throw new ServiceException("快捷入口不存在");
        }
        if (rowIndex == null || rowIndex < 1) {
            throw new ServiceException("行索引必须大于0");
        }
        QuickEntry updateEntry = UpdateEntity.of(QuickEntry.class, id);
        updateEntry.setRowIndex(rowIndex);
        updateEntry.setUpdatedTime(LocalDateTime.now());
        return updateById(updateEntry);
    }
}
