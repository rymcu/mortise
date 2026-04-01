package com.rymcu.mortise.agent.infra.persistence;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;

import java.util.function.Function;

/**
 * MyBatis-Flex 分页适配器。
 */
public final class FlexPageMapper {

    private FlexPageMapper() {
    }

    public static <T> Page<T> toFlexPage(PageQuery query) {
        return new Page<>(query.getPageNumber(), query.getPageSize());
    }

    public static <T> PageResult<T> toPageResult(Page<T> page) {
        return PageResult.of(page.getPageNumber(), page.getPageSize(), page.getTotalRow(), page.getRecords());
    }

    public static <S, T> PageResult<T> toPageResult(Page<S> page, Function<S, T> mapper) {
        return PageResult.of(
                page.getPageNumber(),
                page.getPageSize(),
                page.getTotalRow(),
                page.getRecords().stream().map(mapper).toList()
        );
    }
}
