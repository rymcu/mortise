package com.rymcu.mortise.annotation;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.rymcu.mortise.serializer.DictSerializer;
import com.rymcu.mortise.service.DictService;

/**
 * Created on 2025/4/12 17:36.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.annotation
 */
public class DictAnnotationIntrospector extends JacksonAnnotationIntrospector {
    private final DictService dictService;

    public DictAnnotationIntrospector(DictService dictService) {
        this.dictService = dictService;
    }

    @Override
    public Object findSerializer(Annotated a) {
        DictFormat dictFormat = a.getAnnotation(DictFormat.class);
        if (dictFormat != null) {
            DictSerializer dictSerializer = new DictSerializer(dictService);
            dictSerializer.setDictType(dictFormat.value());
            dictSerializer.setSuffix(dictFormat.suffix());
            dictSerializer.setCover(dictFormat.cover());
            return dictSerializer;
        }
        return super.findSerializer(a);
    }
}
