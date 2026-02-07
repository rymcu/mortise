package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.CourseModule;

import java.util.List;

/**
 * Created on 2025/11/21 15:19.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service
 */
public interface CourseModuleService extends IService<CourseModule> {

    /**
     * 获取课程的所有模块
     *
     * @param courseId 课程ID
     * @return 模块列表
     */
    List<CourseModule> findModulesByCourseId(Long courseId);

}
