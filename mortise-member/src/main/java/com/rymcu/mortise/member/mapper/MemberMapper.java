package com.rymcu.mortise.member.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员 Mapper
 *
 * @author ronger
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
