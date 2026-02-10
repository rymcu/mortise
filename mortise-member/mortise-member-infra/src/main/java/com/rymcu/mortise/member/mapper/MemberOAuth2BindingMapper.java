package com.rymcu.mortise.member.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.member.entity.MemberOAuth2Binding;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员 OAuth2 绑定 Mapper
 *
 * @author ronger
 * @since 1.0.0
 */
@Mapper
public interface MemberOAuth2BindingMapper extends BaseMapper<MemberOAuth2Binding> {
}
