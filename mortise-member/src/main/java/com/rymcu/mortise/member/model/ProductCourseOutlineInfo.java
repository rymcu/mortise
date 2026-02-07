package com.rymcu.mortise.member.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 产品课程大纲 DTO
 * 用于展示产品页面的课程目录信息
 *
 * @author ronger
 */
@Data
public class ProductCourseOutlineInfo implements Serializable {

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品标题
     */
    private String productTitle;

    /**
     * 关联的课程列表
     */
    private List<CourseOutlineInfo> courses;

}
