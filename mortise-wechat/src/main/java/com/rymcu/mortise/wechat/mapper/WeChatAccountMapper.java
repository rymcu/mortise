package com.rymcu.mortise.wechat.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.wechat.entity.WeChatAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信账号 Mapper
 *
 * @author ronger
 * @since 1.0.0
 */
@Mapper
public interface WeChatAccountMapper extends BaseMapper<WeChatAccount> {
}
