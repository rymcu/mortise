package com.rymcu.mortise.agent.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.agent.infra.persistence.entity.AiProviderPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 提供商 Mapper
 *
 * @author ronger
 */
@Mapper
public interface AiProviderMapper extends BaseMapper<AiProviderPO> {
}
