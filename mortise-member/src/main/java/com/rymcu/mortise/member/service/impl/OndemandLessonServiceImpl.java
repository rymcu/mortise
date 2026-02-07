package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.OndemandLesson;
import com.rymcu.mortise.member.mapper.OndemandLessonMapper;
import com.rymcu.mortise.member.service.OndemandLessonService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.rymcu.mortise.member.entity.table.OndemandLessonTableDef.ONDEMAND_LESSON;

/**
 * Created on 2025/11/21 15:23.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service.impl
 */
@Service
@Primary
public class OndemandLessonServiceImpl extends ServiceImpl<OndemandLessonMapper, OndemandLesson> implements OndemandLessonService {

    @Override
    public List<OndemandLesson> findLessonsByModuleId(Long moduleId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ONDEMAND_LESSON.MODULE_ID.eq(moduleId))
                .orderBy(ONDEMAND_LESSON.SORT_NO.asc());
        return mapper.selectListByQuery(queryWrapper);
    }

}
