package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.VodVideo;

/**
 * VOD视频基础服务接口
 *
 * @author ronger
 */
public interface VodVideoService extends IService<VodVideo> {

    /**
     * 根据阿里云VideoId查询
     *
     * @param aliyunVideoId 阿里云VideoId
     * @return VOD视频信息
     */
    VodVideo findByAliyunVideoId(String aliyunVideoId);
}
