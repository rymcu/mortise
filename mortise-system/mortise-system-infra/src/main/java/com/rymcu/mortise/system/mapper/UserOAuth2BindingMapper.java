package com.rymcu.mortise.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.system.infra.persistence.entity.UserOAuth2BindingPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 OAuth2 绑定 Mapper
 *
 * @author ronger
 * @since 1.0.0
 */
@Mapper
public interface UserOAuth2BindingMapper extends BaseMapper<UserOAuth2BindingPO> {
}
