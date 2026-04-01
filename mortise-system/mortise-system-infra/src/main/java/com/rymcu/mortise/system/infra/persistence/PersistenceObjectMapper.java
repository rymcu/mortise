package com.rymcu.mortise.system.infra.persistence;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class PersistenceObjectMapper {

    private PersistenceObjectMapper() {
    }

    public static <T> T copy(Object source, Supplier<T> targetSupplier) {
        if (source == null) {
            return null;
        }
        T target = targetSupplier.get();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T> List<T> copyList(List<?> sources, Supplier<T> targetSupplier) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        return sources.stream()
                .filter(Objects::nonNull)
                .map(source -> copy(source, targetSupplier))
                .toList();
    }
}
