package com.rymcu.mortise.member.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报名查询参数
 *
 * @author ronger
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnrollmentSearch extends BaseSearch {

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 报名类型
     */
    private String enrollmentType;
}
