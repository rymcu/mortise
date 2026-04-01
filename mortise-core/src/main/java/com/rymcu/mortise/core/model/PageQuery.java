package com.rymcu.mortise.core.model;

/**
 * 仓库自有分页查询参数。
 */
public final class PageQuery {

    private final long pageNumber;
    private final long pageSize;

    private PageQuery(long pageNumber, long pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public static PageQuery of(long pageNumber, long pageSize) {
        return new PageQuery(pageNumber, pageSize);
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public long getPageSize() {
        return pageSize;
    }
}
