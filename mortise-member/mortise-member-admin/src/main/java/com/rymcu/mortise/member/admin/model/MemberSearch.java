package com.rymcu.mortise.member.admin.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Getter;
import lombok.Setter;

/**
 * 会员查询条件
 *
 * @author ronger
 */
@Getter
@Setter
public class MemberSearch extends BaseSearch {

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 会员等级
     */
    private String memberLevel;

}
