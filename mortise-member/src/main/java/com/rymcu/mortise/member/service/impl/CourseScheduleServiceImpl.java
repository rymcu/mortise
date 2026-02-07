package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.CourseSchedule;
import com.rymcu.mortise.member.mapper.CourseScheduleMapper;
import com.rymcu.mortise.member.service.CourseScheduleService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Created on 2025/11/21 15:22.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service.impl
 */
@Service
@Primary
public class CourseScheduleServiceImpl extends ServiceImpl<CourseScheduleMapper, CourseSchedule> implements CourseScheduleService {
}
