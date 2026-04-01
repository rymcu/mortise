package com.rymcu.mortise.core.model;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 仓库自有分页结果。
 */
public final class PageResult<T> {

    private final List<T> records;
    private final long pageNumber;
    private final long pageSize;
    private final long totalPage;
    private final long totalRow;
    private final Long maxPageSize;
    private final Boolean optimizeCountQuery;
    private final Boolean hasNext;
    private final Boolean hasPrevious;
    private final Boolean hasRecords;
    private final Long offset;

    private PageResult(long pageNumber, long pageSize, long totalRow, List<T> records) {
        this.records = records == null ? List.of() : List.copyOf(records);
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalRow = totalRow;
        this.totalPage = calculateTotalPage(pageSize, totalRow);
        this.maxPageSize = null;
        this.optimizeCountQuery = null;
        this.hasNext = totalPage > 0 && pageNumber < totalPage;
        this.hasPrevious = totalPage > 0 && pageNumber > 1;
        this.hasRecords = !this.records.isEmpty();
        this.offset = pageSize > 0 ? Math.max(pageNumber - 1, 0) * pageSize : 0L;
    }

    public static <T> PageResult<T> of(PageQuery pageQuery, long totalRow, List<T> records) {
        return new PageResult<>(pageQuery.getPageNumber(), pageQuery.getPageSize(), totalRow, records);
    }

    public static <T> PageResult<T> of(long pageNumber, long pageSize, long totalRow, List<T> records) {
        return new PageResult<>(pageNumber, pageSize, totalRow, records);
    }

    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        List<R> mappedRecords = records.stream().map(mapper).<R>map(value -> value).toList();
        return PageResult.of(pageNumber, pageSize, totalRow, mappedRecords);
    }

    public List<T> getRecords() {
        return records;
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public long getPageSize() {
        return pageSize;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public long getTotalRow() {
        return totalRow;
    }

    public Long getMaxPageSize() {
        return maxPageSize;
    }

    public Boolean getOptimizeCountQuery() {
        return optimizeCountQuery;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public Boolean getHasRecords() {
        return hasRecords;
    }

    public Long getOffset() {
        return offset;
    }

    private static long calculateTotalPage(long pageSize, long totalRow) {
        if (pageSize <= 0 || totalRow <= 0) {
            return 0L;
        }
        return (totalRow + pageSize - 1) / pageSize;
    }
}
