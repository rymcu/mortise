package com.rymcu.mortise.wechat.facade;

import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.wechat.entity.WeChatAccount;
import com.rymcu.mortise.wechat.entity.WeChatConfig;
import com.rymcu.mortise.wechat.model.WeChatAccountSearch;

import java.util.List;

/**
 * 微信账号管理门面
 *
 * @author ronger
 */
public interface WeChatAccountFacade {

    PageResult<WeChatAccount> pageAccounts(WeChatAccountSearch search);

    WeChatAccount getAccount(Long id);

    Long createAccount(WeChatAccount weChatAccount);

    boolean updateAccount(Long id, WeChatAccount weChatAccount);

    boolean deleteAccount(Long id);

    boolean setDefaultAccount(Long id);

    boolean toggleAccount(Long id, boolean enabled);

    List<WeChatConfig> listConfigs(Long accountId);

    boolean saveConfig(Long accountId, WeChatConfig request);

    boolean deleteConfig(Long accountId, String configKey);

    void refreshCache();
}
