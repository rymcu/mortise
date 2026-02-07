package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.member.entity.HomeBanner;
import com.rymcu.mortise.member.mapper.HomeBannerMapper;
import com.rymcu.mortise.member.model.HomeBannerSearch;
import com.rymcu.mortise.member.service.HomeBannerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.rymcu.mortise.member.entity.table.HomeBannerTableDef.HOME_BANNER;

/**
 * 首页轮播图服务实现
 *
 * @author ronger
 */
@Service
@Primary
public class HomeBannerServiceImpl extends ServiceImpl<HomeBannerMapper, HomeBanner> implements HomeBannerService {

    @Override
    public Page<HomeBanner> findBannerList(Page<HomeBanner> page, HomeBannerSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(HOME_BANNER.TITLE.like(search.getQuery(), StringUtils.isNotBlank(search.getQuery())))
                .and(HOME_BANNER.POSITION.eq(search.getPosition(), StringUtils.isNotBlank(search.getPosition())))
                .and(HOME_BANNER.PLATFORM.eq(search.getPlatform(), StringUtils.isNotBlank(search.getPlatform())))
                .and(HOME_BANNER.STATUS.eq(search.getStatus(), Objects.nonNull(search.getStatus())))
                .orderBy(HOME_BANNER.SORT_NO.asc(), HOME_BANNER.CREATED_TIME.desc());
        return mapper.paginate(page, queryWrapper);
    }

    @Override
    public List<HomeBanner> findActiveBanners(String position, String platform) {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(HOME_BANNER.STATUS.eq(1))
                .and(HOME_BANNER.POSITION.eq(position, StringUtils.isNotBlank(position)))
                .and(HOME_BANNER.PLATFORM.in(List.of("all", platform), StringUtils.isNotBlank(platform)))
                .and(HOME_BANNER.START_TIME.isNull().or(HOME_BANNER.START_TIME.le(now)))
                .and(HOME_BANNER.END_TIME.isNull().or(HOME_BANNER.END_TIME.ge(now)))
                .orderBy(HOME_BANNER.SORT_NO.asc());
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incrementClickCount(Long id) {
        HomeBanner banner = getById(id);
        if (banner == null) {
            throw new ServiceException("轮播图不存在");
        }
        HomeBanner updateBanner = UpdateEntity.of(HomeBanner.class, id);
        updateBanner.setClickCount((banner.getClickCount() != null ? banner.getClickCount() : 0) + 1);
        return updateById(updateBanner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incrementViewCount(Long id) {
        HomeBanner banner = getById(id);
        if (banner == null) {
            throw new ServiceException("轮播图不存在");
        }
        HomeBanner updateBanner = UpdateEntity.of(HomeBanner.class, id);
        updateBanner.setViewCount((banner.getViewCount() != null ? banner.getViewCount() : 0) + 1);
        return updateById(updateBanner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enable(Long id) {
        HomeBanner banner = getById(id);
        if (banner == null) {
            throw new ServiceException("轮播图不存在");
        }
        HomeBanner updateBanner = UpdateEntity.of(HomeBanner.class, id);
        updateBanner.setStatus(1);
        updateBanner.setUpdatedTime(LocalDateTime.now());
        return updateById(updateBanner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disable(Long id) {
        HomeBanner banner = getById(id);
        if (banner == null) {
            throw new ServiceException("轮播图不存在");
        }
        HomeBanner updateBanner = UpdateEntity.of(HomeBanner.class, id);
        updateBanner.setStatus(0);
        updateBanner.setUpdatedTime(LocalDateTime.now());
        return updateById(updateBanner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSort(Long id, Integer sortNo) {
        HomeBanner banner = getById(id);
        if (banner == null) {
            throw new ServiceException("轮播图不存在");
        }
        HomeBanner updateBanner = UpdateEntity.of(HomeBanner.class, id);
        updateBanner.setSortNo(sortNo);
        updateBanner.setUpdatedTime(LocalDateTime.now());
        return updateById(updateBanner);
    }
}
