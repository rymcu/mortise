package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.CourseModule;
import com.rymcu.mortise.member.mapper.CourseModuleMapper;
import com.rymcu.mortise.member.service.CourseModuleService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.rymcu.mortise.member.entity.table.CourseModuleTableDef.COURSE_MODULE;

/**
 * Created on 2025/11/21 15:24.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service.impl
 */
@Service
@Primary
public class CourseModuleServiceImpl extends ServiceImpl<CourseModuleMapper, CourseModule> implements CourseModuleService {

    @Override
    public List<CourseModule> findModulesByCourseId(Long courseId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(COURSE_MODULE.COURSE_ID.eq(courseId))
                .orderBy(COURSE_MODULE.SORT_NO.asc());
        return mapper.selectListByQuery(queryWrapper);
    }

}
