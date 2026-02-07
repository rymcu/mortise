package com.rymcu.mortise.member.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.HomeBanner;
import com.rymcu.mortise.member.model.HomeBannerSearch;

import java.util.List;

/**
 * 首页轮播图服务接口
 *
 * @author ronger
 */
public interface HomeBannerService extends IService<HomeBanner> {

    /**
     * 分页查询轮播图
     *
     * @param page   分页对象
     * @param search 查询条件
     * @return 分页数据
     */
    Page<HomeBanner> findBannerList(Page<HomeBanner> page, HomeBannerSearch search);

    /**
     * 获取有效的轮播图列表（前端展示用）
     *
     * @param position 展示位置
     * @param platform 平台
     * @return 轮播图列表
     */
    List<HomeBanner> findActiveBanners(String position, String platform);

    /**
     * 增加点击次数
     *
     * @param id 轮播图ID
     * @return 是否成功
     */
    Boolean incrementClickCount(Long id);

    /**
     * 增加曝光次数
     *
     * @param id 轮播图ID
     * @return 是否成功
     */
    Boolean incrementViewCount(Long id);

    /**
     * 启用轮播图
     *
     * @param id 轮播图ID
     * @return 是否成功
     */
    Boolean enable(Long id);

    /**
     * 禁用轮播图
     *
     * @param id 轮播图ID
     * @return 是否成功
     */
    Boolean disable(Long id);

    /**
     * 更新排序
     *
     * @param id     轮播图ID
     * @param sortNo 新的排序号
     * @return 是否成功
     */
    Boolean updateSort(Long id, Integer sortNo);
}
