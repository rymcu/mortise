package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.VodVideo;
import com.rymcu.mortise.member.mapper.VodVideoMapper;
import com.rymcu.mortise.member.service.VodVideoService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import static com.rymcu.mortise.member.entity.table.VodVideoTableDef.VOD_VIDEO;

/**
 * VOD视频基础服务实现
 *
 * @author ronger
 */
@Service
@Primary
public class VodVideoServiceImpl extends ServiceImpl<VodVideoMapper, VodVideo> implements VodVideoService {

    @Override
    public VodVideo findByAliyunVideoId(String aliyunVideoId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(VOD_VIDEO.ALIYUN_VIDEO_ID.eq(aliyunVideoId));
        return mapper.selectOneByQuery(queryWrapper);
    }
}
