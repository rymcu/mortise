package com.rymcu.mortise.voice.infra.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.entity.VoiceJob;
import com.rymcu.mortise.voice.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.voice.infra.persistence.entity.VoiceJobPO;
import com.rymcu.mortise.voice.mapper.VoiceJobMapper;
import com.rymcu.mortise.voice.model.VoiceJobSearchCriteria;
import com.rymcu.mortise.voice.repository.VoiceJobRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static com.rymcu.mortise.voice.infra.persistence.entity.table.VoiceJobPOTableDef.VOICE_JOB_PO;

/**
 * 语音任务仓储实现。
 */
@Repository
public class VoiceJobRepositoryImpl implements VoiceJobRepository {

    private final VoiceJobMapper voiceJobMapper;

    public VoiceJobRepositoryImpl(VoiceJobMapper voiceJobMapper) {
        this.voiceJobMapper = voiceJobMapper;
    }

    @Override
    public PageResult<VoiceJob> findJobs(PageQuery pageQuery, VoiceJobSearchCriteria criteria) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(VOICE_JOB_PO.CREATED_TIME.desc(), VOICE_JOB_PO.ID.desc());
        if (criteria != null) {
            if (StringUtils.hasText(criteria.status())) {
                queryWrapper.and(VOICE_JOB_PO.STATUS.eq(criteria.status()));
            }
            if (StringUtils.hasText(criteria.jobType())) {
                queryWrapper.and(VOICE_JOB_PO.JOB_TYPE.eq(criteria.jobType()));
            }
            if (criteria.profileId() != null) {
                queryWrapper.and(VOICE_JOB_PO.PROFILE_ID.eq(criteria.profileId()));
            }
            if (criteria.userId() != null) {
                queryWrapper.and(VOICE_JOB_PO.USER_ID.eq(criteria.userId()));
            }
            if (StringUtils.hasText(criteria.query())) {
                QueryCondition keywordCondition = VOICE_JOB_PO.SOURCE_MODULE.like(criteria.query())
                        .or(VOICE_JOB_PO.RESULT_SUMMARY.like(criteria.query()))
                        .or(VOICE_JOB_PO.ERROR_MESSAGE.like(criteria.query()))
                        .or(VOICE_JOB_PO.STATUS.like(criteria.query()))
                        .or(VOICE_JOB_PO.JOB_TYPE.like(criteria.query()));
                Long queryId = tryParseId(criteria.query());
                if (queryId != null) {
                    keywordCondition = VOICE_JOB_PO.ID.eq(queryId).or(keywordCondition);
                }
                queryWrapper.and(keywordCondition);
            }
        }
        Page<VoiceJobPO> page = voiceJobMapper.paginate(FlexPageMapper.toFlexPage(pageQuery), queryWrapper);
        return FlexPageMapper.toPageResult(page, this::toDomain);
    }

    @Override
    public Optional<VoiceJob> findById(Long id) {
        return Optional.ofNullable(voiceJobMapper.selectOneById(id)).map(this::toDomain);
    }

    @Override
    public boolean save(VoiceJob job) {
        VoiceJobPO jobPO = toPersistence(job);
        boolean saved = voiceJobMapper.insertSelective(jobPO) > 0;
        if (saved) {
            job.setId(jobPO.getId());
        }
        return saved;
    }

    @Override
    public boolean update(VoiceJob job) {
        return voiceJobMapper.update(toPersistence(job)) > 0;
    }

    private VoiceJobPO toPersistence(VoiceJob job) {
        VoiceJobPO jobPO = new VoiceJobPO();
        jobPO.setId(job.getId());
        jobPO.setJobType(job.getJobType());
        jobPO.setStatus(job.getStatus());
        jobPO.setProfileId(job.getProfileId());
        jobPO.setUserId(job.getUserId());
        jobPO.setSourceModule(job.getSourceModule());
        jobPO.setDurationMillis(job.getDurationMillis());
        jobPO.setResultSummary(job.getResultSummary());
        jobPO.setErrorMessage(job.getErrorMessage());
        jobPO.setDelFlag(job.getDelFlag());
        return jobPO;
    }

    private VoiceJob toDomain(VoiceJobPO po) {
        VoiceJob job = new VoiceJob();
        job.setId(po.getId());
        job.setJobType(po.getJobType());
        job.setStatus(po.getStatus());
        job.setProfileId(po.getProfileId());
        job.setUserId(po.getUserId());
        job.setSourceModule(po.getSourceModule());
        job.setDurationMillis(po.getDurationMillis());
        job.setResultSummary(po.getResultSummary());
        job.setErrorMessage(po.getErrorMessage());
        job.setDelFlag(po.getDelFlag());
        job.setCreatedTime(po.getCreatedTime());
        job.setUpdatedTime(po.getUpdatedTime());
        return job;
    }

    private Long tryParseId(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.strip());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}