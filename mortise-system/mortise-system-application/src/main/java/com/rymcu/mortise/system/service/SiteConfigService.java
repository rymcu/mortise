package com.rymcu.mortise.system.service;

import com.rymcu.mortise.system.model.SiteConfigGroupVO;
import com.rymcu.mortise.system.model.SiteConfigPublicVO;
import com.rymcu.mortise.system.model.SiteConfigSaveRequest;

import java.util.List;

/**
 * 网站配置服务接口
 *
 * @author ronger
 */
public interface SiteConfigService {

    /**
     * 查询所有配置分组（含 Schema + 当前值），供管理端使用。
     *
     * @return 所有配置分组及其字段定义与当前值
     */
    List<SiteConfigGroupVO> listAllGroups();

    /**
     * 查询指定配置分组详情。
     *
     * @param group 分组标识，如 site / seo
     * @return 配置分组视图对象
     */
    SiteConfigGroupVO getGroup(String group);

    /**
     * 保存指定分组的配置，全量覆盖后刷新缓存。
     *
     * @param group   分组标识
     * @param request 配置值
     */
    void saveGroup(String group, SiteConfigSaveRequest request);

    /**
     * 获取公开配置（无需鉴权），聚合所有配置项的值。
     * 供前端启动时加载系统名称、Logo 等基础信息。
     *
     * @return 公开配置视图对象
     */
    SiteConfigPublicVO getPublicConfig();
}
