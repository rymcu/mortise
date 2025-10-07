package com.rymcu.mortise.auth.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * OAuth2 客户端配置 Mapper
 *
 * @author ronger
 * @since 1.0.0
 */
@Mapper
public interface Oauth2ClientConfigMapper extends BaseMapper<Oauth2ClientConfig> {
}
