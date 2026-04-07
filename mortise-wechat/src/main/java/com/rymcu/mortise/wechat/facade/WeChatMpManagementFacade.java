package com.rymcu.mortise.wechat.facade;

import java.util.Map;

/**
 * 微信公众号动态管理门面
 *
 * @author ronger
 */
public interface WeChatMpManagementFacade {

    Map<String, Object> reloadAll();

    Map<String, Object> reloadAccount(Long accountId);

    Map<String, Object> removeAccount(Long accountId);

    Map<String, Object> getStatus();

    Map<String, Object> testAccount(Long accountId);
}
