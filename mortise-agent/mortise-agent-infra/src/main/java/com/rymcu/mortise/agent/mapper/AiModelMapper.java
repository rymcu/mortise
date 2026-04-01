package com.rymcu.mortise.agent.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.agent.infra.persistence.entity.AiModelPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型 Mapper
 *
 * @author ronger
 */
@Mapper
public interface AiModelMapper extends BaseMapper<AiModelPO> {
}
