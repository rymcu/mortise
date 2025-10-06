package com.rymcu.mortise.wechat.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.wechat.entity.WeChatAccount;
import com.rymcu.mortise.wechat.entity.WeChatConfig;
import com.rymcu.mortise.wechat.model.WeChatAccountSearch;

import java.util.List;

/**
 * 微信账号服务接口
 *
 * @author ronger
 * @since 1.0.0
 */
public interface WeChatAccountService extends IService<WeChatAccount> {

    Page<WeChatAccount> pageAccounts(Page<WeChatAccount> page, WeChatAccountSearch search);

    List<WeChatAccount> listAccounts(String accountType);

    WeChatAccount getAccountById(Long accountId);

    WeChatAccount getAccountByAppId(String appId);

    WeChatAccount getDefaultAccount(String accountType);

    Long createAccount(WeChatAccount account);

    boolean updateAccount(WeChatAccount account);

    boolean deleteAccount(Long accountId);

    boolean setDefaultAccount(Long accountId);

    boolean toggleAccount(Long accountId, boolean enabled);

    List<WeChatConfig> listConfigs(Long accountId);

    boolean saveConfig(Long accountId, String configKey, String configValue, Integer isEncrypted);

    boolean batchSaveConfigs(Long accountId, List<WeChatConfig> configs);

    boolean deleteConfig(Long accountId, String configKey);

    void refreshCache();
}
