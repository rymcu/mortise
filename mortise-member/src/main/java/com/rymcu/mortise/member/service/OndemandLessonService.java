package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.OndemandLesson;

import java.util.List;

/**
 * Created on 2025/11/21 15:20.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service
 */
public interface OndemandLessonService extends IService<OndemandLesson> {

    /**
     * 获取模块的所有课时
     *
     * @param moduleId 模块ID
     * @return 课时列表
     */
    List<OndemandLesson> findLessonsByModuleId(Long moduleId);

}
