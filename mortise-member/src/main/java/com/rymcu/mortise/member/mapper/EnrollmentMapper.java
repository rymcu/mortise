package com.rymcu.mortise.member.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.member.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学员报名 Mapper
 *
 * @author ronger
 */
@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {
}
