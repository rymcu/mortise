-- AI 提供商与模型种子数据

-- ========== OpenAI 提供商 ==========
INSERT INTO mortise.mortise_ai_provider (id, name, code, default_model_name, status, sort_no)
SELECT 300000000000000001, 'OpenAI', 'openai', 'gpt-4.1', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_ai_provider WHERE code = 'openai' AND del_flag = 0);

INSERT INTO mortise.mortise_ai_model (id, provider_id, model_name, display_name, status, sort_no)
SELECT v.id, v.provider_id, v.model_name, v.display_name, 1, v.sort_no
FROM (VALUES
    (300000000000001001, 300000000000000001, 'gpt-4.1',      'GPT-4.1',      1),
    (300000000000001002, 300000000000000001, 'gpt-4.1-mini', 'GPT-4.1 Mini', 2),
    (300000000000001003, 300000000000000001, 'gpt-4.1-nano', 'GPT-4.1 Nano', 3),
    (300000000000001004, 300000000000000001, 'gpt-4o',       'GPT-4o',       4),
    (300000000000001005, 300000000000000001, 'gpt-4o-mini',  'GPT-4o Mini',  5)
) AS v(id, provider_id, model_name, display_name, sort_no)
WHERE NOT EXISTS (
    SELECT 1 FROM mortise.mortise_ai_model m
    WHERE m.provider_id = v.provider_id AND m.model_name = v.model_name AND m.del_flag = 0
);

-- ========== Anthropic 提供商 ==========
INSERT INTO mortise.mortise_ai_provider (id, name, code, default_model_name, status, sort_no)
SELECT 300000000000000002, 'Anthropic', 'anthropic', 'claude-sonnet-4-20250514', 1, 2
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_ai_provider WHERE code = 'anthropic' AND del_flag = 0);

INSERT INTO mortise.mortise_ai_model (id, provider_id, model_name, display_name, status, sort_no)
SELECT v.id, v.provider_id, v.model_name, v.display_name, 1, v.sort_no
FROM (VALUES
    (300000000000002001, 300000000000000002, 'claude-sonnet-4-20250514',   'Claude Sonnet 4',         1),
    (300000000000002002, 300000000000000002, 'claude-opus-4-20250514',     'Claude Opus 4',           2),
    (300000000000002003, 300000000000000002, 'claude-3-7-sonnet-20250219', 'Claude 3.7 Sonnet',       3),
    (300000000000002004, 300000000000000002, 'claude-3-5-sonnet-20241022', 'Claude 3.5 Sonnet',       4),
    (300000000000002005, 300000000000000002, 'claude-3-5-haiku-20241022',  'Claude 3.5 Haiku',        5)
) AS v(id, provider_id, model_name, display_name, sort_no)
WHERE NOT EXISTS (
    SELECT 1 FROM mortise.mortise_ai_model m
    WHERE m.provider_id = v.provider_id AND m.model_name = v.model_name AND m.del_flag = 0
);

-- ========== DeepSeek 提供商 ==========
INSERT INTO mortise.mortise_ai_provider (id, name, code, default_model_name, status, sort_no)
SELECT 300000000000000003, 'DeepSeek', 'deepseek', 'deepseek-chat', 1, 3
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_ai_provider WHERE code = 'deepseek' AND del_flag = 0);

INSERT INTO mortise.mortise_ai_model (id, provider_id, model_name, display_name, status, sort_no)
SELECT v.id, v.provider_id, v.model_name, v.display_name, 1, v.sort_no
FROM (VALUES
    (300000000000003001, 300000000000000003, 'deepseek-chat',     'DeepSeek Chat',     1),
    (300000000000003002, 300000000000000003, 'deepseek-reasoner', 'DeepSeek Reasoner', 2)
) AS v(id, provider_id, model_name, display_name, sort_no)
WHERE NOT EXISTS (
    SELECT 1 FROM mortise.mortise_ai_model m
    WHERE m.provider_id = v.provider_id AND m.model_name = v.model_name AND m.del_flag = 0
);

-- ========== 智谱 提供商 ==========
INSERT INTO mortise.mortise_ai_provider (id, name, code, default_model_name, status, sort_no)
SELECT 300000000000000004, '智谱 GLM', 'zhipu', 'glm-4-plus', 1, 4
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_ai_provider WHERE code = 'zhipu' AND del_flag = 0);

INSERT INTO mortise.mortise_ai_model (id, provider_id, model_name, display_name, status, sort_no)
SELECT v.id, v.provider_id, v.model_name, v.display_name, 1, v.sort_no
FROM (VALUES
    (300000000000004001, 300000000000000004, 'glm-4-plus', 'GLM-4 Plus', 1),
    (300000000000004002, 300000000000000004, 'glm-4',      'GLM-4',      2)
) AS v(id, provider_id, model_name, display_name, sort_no)
WHERE NOT EXISTS (
    SELECT 1 FROM mortise.mortise_ai_model m
    WHERE m.provider_id = v.provider_id AND m.model_name = v.model_name AND m.del_flag = 0
);

-- ========== 阿里云通义千问 提供商 ==========
INSERT INTO mortise.mortise_ai_provider (id, name, code, default_model_name, status, sort_no)
SELECT 300000000000000005, '阿里云通义千问', 'dashscope', 'qwen-max', 1, 5
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_ai_provider WHERE code = 'dashscope' AND del_flag = 0);

INSERT INTO mortise.mortise_ai_model (id, provider_id, model_name, display_name, status, sort_no)
SELECT v.id, v.provider_id, v.model_name, v.display_name, 1, v.sort_no
FROM (VALUES
    (300000000000005001, 300000000000000005, 'qwen-max',   'Qwen Max',   1),
    (300000000000005002, 300000000000000005, 'qwen-plus',  'Qwen Plus',  2),
    (300000000000005003, 300000000000000005, 'qwen-turbo', 'Qwen Turbo', 3)
) AS v(id, provider_id, model_name, display_name, sort_no)
WHERE NOT EXISTS (
    SELECT 1 FROM mortise.mortise_ai_model m
    WHERE m.provider_id = v.provider_id AND m.model_name = v.model_name AND m.del_flag = 0
);

-- ========== Ollama 本地模型 提供商 ==========
INSERT INTO mortise.mortise_ai_provider (id, name, code, default_model_name, status, sort_no)
SELECT 300000000000000006, 'Ollama', 'ollama', 'qwen2.5', 1, 6
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_ai_provider WHERE code = 'ollama' AND del_flag = 0);

INSERT INTO mortise.mortise_ai_model (id, provider_id, model_name, display_name, status, sort_no)
SELECT v.id, v.provider_id, v.model_name, v.display_name, 1, v.sort_no
FROM (VALUES
    (300000000000006001, 300000000000000006, 'qwen2.5',     'Qwen 2.5',     1),
    (300000000000006002, 300000000000000006, 'llama3.2',    'Llama 3.2',    2),
    (300000000000006003, 300000000000000006, 'llama3.1',    'Llama 3.1',    3),
    (300000000000006004, 300000000000000006, 'mistral',     'Mistral',      4),
    (300000000000006005, 300000000000000006, 'deepseek-r1', 'DeepSeek R1',  5)
) AS v(id, provider_id, model_name, display_name, sort_no)
WHERE NOT EXISTS (
    SELECT 1 FROM mortise.mortise_ai_model m
    WHERE m.provider_id = v.provider_id AND m.model_name = v.model_name AND m.del_flag = 0
);
