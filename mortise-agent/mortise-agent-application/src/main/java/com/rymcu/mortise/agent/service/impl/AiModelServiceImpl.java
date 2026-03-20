package com.rymcu.mortise.agent.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.agent.entity.AiModel;
import com.rymcu.mortise.agent.mapper.AiModelMapper;
import com.rymcu.mortise.agent.service.AiModelService;
import org.springframework.stereotype.Service;

/**
 * AI 模型服务实现
 *
 * @author ronger
 */
@Service
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel> implements AiModelService {
}
