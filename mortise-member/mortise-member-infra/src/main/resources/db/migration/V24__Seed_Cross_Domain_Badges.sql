INSERT INTO mortise.mortise_badge
    (id, code, name, icon, description, condition_type, condition_value, created_time, updated_time, del_flag)
VALUES
    (900101, 'product_reference_contributor', '产品联动作者', 'i-lucide-package-check', '内容被产品模块引用 3 次。', 'product_reference_count', '3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (900102, 'curriculum_citation_contributor', '教材贡献者', 'i-lucide-graduation-cap', '内容被教材或课程引用 1 次。', 'curriculum_citation_count', '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (900103, 'steady_checkin_creator', '连续签到创作者', 'i-lucide-calendar-check-2', '连续签到 7 天。', 'checkin_streak_days', '7', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;
