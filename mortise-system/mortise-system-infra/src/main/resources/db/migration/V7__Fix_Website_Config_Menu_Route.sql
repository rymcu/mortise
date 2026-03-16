-- 修正网站配置菜单路由，兼容现有前端页面路径
UPDATE mortise.mortise_menu
SET href = '/systems/site-config',
    updated_time = CURRENT_TIMESTAMP
WHERE permission = 'system:website-config'
  AND href = '/systems/website-config';
