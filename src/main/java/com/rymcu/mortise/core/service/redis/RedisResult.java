package com.rymcu.mortise.core.service.redis;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * redis 中取得的结果
 * Created by liwei on 2017/2/7.
 */
@Setter
@Getter
public class RedisResult<T> {

    /**
     * redis中是否存在
     */
    private boolean exist = false;

    /**
     * redis中取得的数据
     */
    private T result;

    /**
     * redis中取得的List数据
     */
    private List<T> listResult;
    /**
     * redis中的key是否存在。true:表示redis中存在Key,但对应的值为空值标记
     */
    private boolean keyExists = false;
    /**
     * redis中key 对应在对象值
     */
    private T resultObj;


}
