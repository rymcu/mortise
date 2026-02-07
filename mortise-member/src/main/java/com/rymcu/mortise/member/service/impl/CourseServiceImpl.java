package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.Course;
import com.rymcu.mortise.member.mapper.CourseMapper;
import com.rymcu.mortise.member.service.CourseService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Created on 2025/11/21 15:21.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service.impl
 */
@Service
@Primary
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
}
