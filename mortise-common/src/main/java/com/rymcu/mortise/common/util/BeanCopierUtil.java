package com.rymcu.mortise.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanCopier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean 属性拷贝工具类
 * 基于 Spring CGLIB BeanCopier 实现高性能对象拷贝
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/8/20
 */
@Slf4j
public class BeanCopierUtil {

    /**
     * BeanCopier 缓存
     * Key: sourceClassName + targetClassName
     */
    private static final Map<String, BeanCopier> BEAN_COPIER_MAP = new ConcurrentHashMap<>();

    /**
     * 将源对象属性拷贝到目标对象
     * (此方法为核心，供内部调用)
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copy(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanCopier beanCopier = getBeanCopier(source.getClass(), target.getClass());
        beanCopier.copy(source, target, null);
    }

    /**
     * 转换单个对象
     * (推荐使用此方法)
     *
     * @param source      源对象
     * @param targetClass 目标对象的Class
     * @param <T>         目标对象的类型
     * @return 转换后的目标对象实例
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T targetInstance = targetClass.getDeclaredConstructor().newInstance();
            copy(source, targetInstance);
            return targetInstance;
        } catch (Exception e) {
            log.error("Failed to convert bean from {} to {}", 
                    source.getClass().getName(), targetClass.getName(), e);
            throw new RuntimeException("Bean conversion failed", e);
        }
    }

    /**
     * 转换列表对象
     *
     * @param sourceList  源对象列表
     * @param targetClass 目标对象的Class
     * @param <S>         源对象的类型
     * @param <T>         目标对象的类型
     * @return 转换后的目标对象列表
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        try {
            for (S sourceItem : sourceList) {
                if (sourceItem != null) {
                    T targetItem = targetClass.getDeclaredConstructor().newInstance();
                    copy(sourceItem, targetItem);
                    targetList.add(targetItem);
                }
            }
        } catch (Exception e) {
            log.error("Failed to convert bean list to class {}", targetClass.getName(), e);
            throw new RuntimeException("Bean list conversion failed", e);
        }
        return targetList;
    }

    /**
     * 获取BeanCopier实例
     * <p>
     * 使用缓存重用BeanCopier，提升性能。
     *
     * @param sourceClass 源类型
     * @param targetClass 目标类型
     * @return BeanCopier实例
     */
    private static BeanCopier getBeanCopier(Class<?> sourceClass, Class<?> targetClass) {
        String classKey = sourceClass.getName() + "#" + targetClass.getName();
        // 使用computeIfAbsent保证线程安全且高效
        return BEAN_COPIER_MAP.computeIfAbsent(classKey, 
                key -> BeanCopier.create(sourceClass, targetClass, false));
    }
}
