package com.rymcu.mortise.agent.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.agent.entity.AiProvider;
import com.rymcu.mortise.agent.mapper.AiProviderMapper;
import com.rymcu.mortise.agent.service.AiProviderService;
import org.springframework.stereotype.Service;

/**
 * AI 提供商服务实现
 *
 * @author ronger
 */
@Service
public class AiProviderServiceImpl extends ServiceImpl<AiProviderMapper, AiProvider> implements AiProviderService {
}
