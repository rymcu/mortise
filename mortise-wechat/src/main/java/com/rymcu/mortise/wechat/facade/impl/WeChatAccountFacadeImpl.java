package com.rymcu.mortise.wechat.facade.impl;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.wechat.entity.WeChatAccount;
import com.rymcu.mortise.wechat.entity.WeChatConfig;
import com.rymcu.mortise.wechat.facade.WeChatAccountFacade;
import com.rymcu.mortise.wechat.model.WeChatAccountSearch;
import com.rymcu.mortise.wechat.service.WeChatAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 微信账号管理门面实现
 *
 * @author ronger
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeChatAccountFacadeImpl implements WeChatAccountFacade {

    private final WeChatAccountService accountService;

    @Override
    public PageResult<WeChatAccount> pageAccounts(WeChatAccountSearch search) {
        Page<WeChatAccount> page = new Page<>(search.getPageNum(), search.getPageSize());
        Page<WeChatAccount> result = accountService.pageAccounts(page, search);
        return PageResult.of(
                result.getPageNumber(),
                result.getPageSize(),
                result.getTotalRow(),
                result.getRecords()
        );
    }

    @Override
    public WeChatAccount getAccount(Long id) {
        log.info("获取微信账号详情，id: {}", id);
        return accountService.getAccountById(id);
    }

    @Override
    public Long createAccount(WeChatAccount weChatAccount) {
        log.info("创建微信账号，type: {}, name: {}", weChatAccount.getAccountType(), weChatAccount.getAccountName());

        WeChatAccount account = new WeChatAccount();
        account.setAccountType(weChatAccount.getAccountType());
        account.setAccountName(weChatAccount.getAccountName());
        account.setAppId(weChatAccount.getAppId());
        account.setAppSecret(weChatAccount.getAppSecret());
        account.setIsDefault(weChatAccount.getIsDefault());
        account.setStatus(weChatAccount.getStatus());

        return accountService.createAccount(account);
    }

    @Override
    public boolean updateAccount(Long id, WeChatAccount weChatAccount) {
        log.info("更新微信账号，id: {}", id);

        WeChatAccount account = new WeChatAccount();
        account.setId(id);
        if (weChatAccount.getAccountName() != null) {
            account.setAccountName(weChatAccount.getAccountName());
        }
        if (weChatAccount.getAppId() != null) {
            account.setAppId(weChatAccount.getAppId());
        }
        if (weChatAccount.getAppSecret() != null) {
            account.setAppSecret(weChatAccount.getAppSecret());
        }
        if (weChatAccount.getIsDefault() != null) {
            account.setIsDefault(weChatAccount.getIsDefault());
        }
        if (weChatAccount.getStatus() != null) {
            account.setStatus(weChatAccount.getStatus());
        }

        return accountService.updateAccount(account);
    }

    @Override
    public boolean deleteAccount(Long id) {
        log.info("删除微信账号，id: {}", id);
        return accountService.deleteAccount(id);
    }

    @Override
    public boolean setDefaultAccount(Long id) {
        log.info("设置默认账号，id: {}", id);
        return accountService.setDefaultAccount(id);
    }

    @Override
    public boolean toggleAccount(Long id, boolean enabled) {
        log.info("{}账号，id: {}", enabled ? "启用" : "禁用", id);
        return accountService.toggleAccount(id, enabled);
    }

    @Override
    public List<WeChatConfig> listConfigs(Long accountId) {
        log.info("获取账号配置列表，accountId: {}", accountId);
        return accountService.listConfigs(accountId);
    }

    @Override
    public boolean saveConfig(Long accountId, WeChatConfig request) {
        log.info("保存配置，accountId: {}, key: {}", accountId, request.getConfigKey());
        return accountService.saveConfig(
                accountId,
                request.getConfigKey(),
                request.getConfigValue(),
                request.getIsEncrypted()
        );
    }

    @Override
    public boolean deleteConfig(Long accountId, String configKey) {
        log.info("删除配置，accountId: {}, key: {}", accountId, configKey);
        return accountService.deleteConfig(accountId, configKey);
    }

    @Override
    public void refreshCache() {
        log.info("刷新微信配置缓存");
        accountService.refreshCache();
    }
}
