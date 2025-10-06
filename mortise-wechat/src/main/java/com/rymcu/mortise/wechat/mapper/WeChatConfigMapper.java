package com.rymcu.mortise.wechat.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.wechat.entity.WeChatConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 微信配置 Mapper
 *
 * @author ronger
 * @since 1.0.0
 */
@Mapper
public interface WeChatConfigMapper extends BaseMapper<WeChatConfig> {
}
