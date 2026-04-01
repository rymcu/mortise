package com.rymcu.mortise.cache.service;

/**
 * 跨节点缓存版本服务。
 */
public interface CacheVersionService {

    /**
     * 获取指定命名空间的当前版本。
     */
    long currentVersion(String namespace);

    /**
     * 递增指定命名空间的版本号。
     */
    long bumpVersion(String namespace);
}
