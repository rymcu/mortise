-- 系统字典类型与字典条目种子数据
-- 字典类型 ID 规则: 10000000000003xxx
-- 字典条目 ID 规则: 10000000000004xxx
-- 幂等：使用 WHERE NOT EXISTS 避免重复插入

-- ========== 字典类型 ==========

INSERT INTO mortise.mortise_dict_type (id, label, type_code, description, sort_no, status, del_flag, created_time, updated_time)
SELECT 10000000000003001, '状态', 'Status', '通用状态字典', 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict_type WHERE type_code = 'Status');

INSERT INTO mortise.mortise_dict_type (id, label, type_code, description, sort_no, status, del_flag, created_time, updated_time)
SELECT 10000000000003002, '删除标记', 'DelFlag', '删除标记字典', 2, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict_type WHERE type_code = 'DelFlag');

INSERT INTO mortise.mortise_dict_type (id, label, type_code, description, sort_no, status, del_flag, created_time, updated_time)
SELECT 10000000000003003, '默认标记', 'DefaultFlag', '默认标记字典', 3, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict_type WHERE type_code = 'DefaultFlag');

INSERT INTO mortise.mortise_dict_type (id, label, type_code, description, sort_no, status, del_flag, created_time, updated_time)
SELECT 10000000000003004, '菜单类型', 'MenuType', '菜单类型字典', 4, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict_type WHERE type_code = 'MenuType');

INSERT INTO mortise.mortise_dict_type (id, label, type_code, description, sort_no, status, del_flag, created_time, updated_time)
SELECT 10000000000003005, '性别', 'Sex', '用户性别字典', 5, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict_type WHERE type_code = 'Sex');

INSERT INTO mortise.mortise_dict_type (id, label, type_code, description, sort_no, status, del_flag, created_time, updated_time)
SELECT 10000000000003006, '是否', 'YesNo', '通用是否字典', 6, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict_type WHERE type_code = 'YesNo');

INSERT INTO mortise.mortise_dict_type (id, label, type_code, description, sort_no, status, del_flag, created_time, updated_time)
SELECT 10000000000003007, '通知类型', 'NoticeType', '通知类型字典', 7, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict_type WHERE type_code = 'NoticeType');

INSERT INTO mortise.mortise_dict_type (id, label, type_code, description, sort_no, status, del_flag, created_time, updated_time)
SELECT 10000000000003008, '操作类型', 'OperationType', '操作日志类型字典', 8, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict_type WHERE type_code = 'OperationType');

INSERT INTO mortise.mortise_dict_type (id, label, type_code, description, sort_no, status, del_flag, created_time, updated_time)
SELECT 10000000000003009, '登录状态', 'LoginStatus', '登录状态字典', 9, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict_type WHERE type_code = 'LoginStatus');

INSERT INTO mortise.mortise_dict_type (id, label, type_code, description, sort_no, status, del_flag, created_time, updated_time)
SELECT 10000000000003010, '可见性', 'Visibility', '内容可见性字典', 10, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict_type WHERE type_code = 'Visibility');

-- ========== 字典条目：Status ==========

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004001, 'Status', '正常', '0', 1, 'i-lucide-square-check', 'success', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'Status' AND value = '0');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004002, 'Status', '禁用', '1', 2, 'i-lucide-square-x', 'error', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'Status' AND value = '1');

-- ========== 字典条目：DelFlag ==========

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004003, 'DelFlag', '未删除', '0', 1, 'i-lucide-square-check', 'success', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'DelFlag' AND value = '0');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004004, 'DelFlag', '已删除', '1', 2, 'i-lucide-square-x', 'error', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'DelFlag' AND value = '1');

-- ========== 字典条目：DefaultFlag ==========

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004005, 'DefaultFlag', '非默认', '0', 1, 'i-lucide-square-x', 'info', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'DefaultFlag' AND value = '0');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004006, 'DefaultFlag', '默认', '1', 2, 'i-lucide-square-check', 'success', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'DefaultFlag' AND value = '1');

-- ========== 字典条目：MenuType ==========

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004007, 'MenuType', '目录', '0', 1, 'i-lucide-folder', 'info', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'MenuType' AND value = '0');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004008, 'MenuType', '菜单', '1', 2, 'i-lucide-menu', 'primary', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'MenuType' AND value = '1');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004009, 'MenuType', '按钮', '2', 3, 'i-lucide-mouse-pointer-click', 'warning', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'MenuType' AND value = '2');

-- ========== 字典条目：Sex ==========

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004010, 'Sex', '未知', '0', 1, 'i-lucide-help-circle', 'info', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'Sex' AND value = '0');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004011, 'Sex', '男', '1', 2, 'i-lucide-user', 'primary', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'Sex' AND value = '1');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004012, 'Sex', '女', '2', 3, 'i-lucide-user', 'error', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'Sex' AND value = '2');

-- ========== 字典条目：YesNo ==========

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004013, 'YesNo', '否', '0', 1, 'i-lucide-x', 'error', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'YesNo' AND value = '0');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004014, 'YesNo', '是', '1', 2, 'i-lucide-check', 'success', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'YesNo' AND value = '1');

-- ========== 字典条目：NoticeType ==========

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004015, 'NoticeType', '系统通知', '0', 1, 'i-lucide-megaphone', 'warning', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'NoticeType' AND value = '0');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004016, 'NoticeType', '个人消息', '1', 2, 'i-lucide-mail', 'primary', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'NoticeType' AND value = '1');

-- ========== 字典条目：OperationType ==========

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004017, 'OperationType', '查询', '0', 1, 'i-lucide-search', 'info', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'OperationType' AND value = '0');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004018, 'OperationType', '新增', '1', 2, 'i-lucide-plus', 'success', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'OperationType' AND value = '1');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004019, 'OperationType', '修改', '2', 3, 'i-lucide-pencil', 'primary', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'OperationType' AND value = '2');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004020, 'OperationType', '删除', '3', 4, 'i-lucide-trash-2', 'error', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'OperationType' AND value = '3');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004021, 'OperationType', '导入', '4', 5, 'i-lucide-upload', 'warning', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'OperationType' AND value = '4');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004022, 'OperationType', '导出', '5', 6, 'i-lucide-download', 'warning', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'OperationType' AND value = '5');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004023, 'OperationType', '授权', '6', 7, 'i-lucide-shield-check', 'success', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'OperationType' AND value = '6');

-- ========== 字典条目：LoginStatus ==========

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004024, 'LoginStatus', '成功', '0', 1, 'i-lucide-log-in', 'success', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'LoginStatus' AND value = '0');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004025, 'LoginStatus', '失败', '1', 2, 'i-lucide-log-out', 'error', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'LoginStatus' AND value = '1');

-- ========== 字典条目：Visibility ==========

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004026, 'Visibility', '公开', '0', 1, 'i-lucide-eye', 'success', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'Visibility' AND value = '0');

INSERT INTO mortise.mortise_dict (id, dict_type_code, label, value, sort_no, icon, color, status, del_flag, created_time, updated_time)
SELECT 10000000000004027, 'Visibility', '私有', '1', 2, 'i-lucide-eye-off', 'warning', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM mortise.mortise_dict WHERE dict_type_code = 'Visibility' AND value = '1');
