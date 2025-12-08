-- 俱乐部详情菜单
INSERT INTO `sys_menu` (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES ('俱乐部详情', 0, 8, 'club', NULL, '', 1, 0, 'M', '0', '0', '', 'people', 'admin', NOW(), '', NULL, '俱乐部详情目录');

-- 获取刚刚插入的菜单ID，用于后续插入子菜单
SET @parent_menu_id = LAST_INSERT_ID();

-- 俱乐部详情页面
INSERT INTO `sys_menu` (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES ('详情统计', @parent_menu_id, 1, 'detail', 'club/detail/index', '', 1, 0, 'C', '0', '0', 'club:detail:list', 'chart', 'admin', NOW(), '', NULL, '俱乐部详情菜单');

-- 获取详情统计菜单ID，用于后续插入权限
SET @detail_menu_id = LAST_INSERT_ID();

-- 俱乐部详情权限
INSERT INTO `sys_menu` (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES 
('详情查询', @detail_menu_id, 1, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:query', '#', 'admin', NOW(), '', NULL, ''),
('详情列表', @detail_menu_id, 2, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:list', '#', 'admin', NOW(), '', NULL, ''),
('详情新增', @detail_menu_id, 3, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:add', '#', 'admin', NOW(), '', NULL, ''),
('详情修改', @detail_menu_id, 4, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:edit', '#', 'admin', NOW(), '', NULL, ''),
('详情删除', @detail_menu_id, 5, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:remove', '#', 'admin', NOW(), '', NULL, ''),
('详情导出', @detail_menu_id, 6, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:export', '#', 'admin', NOW(), '', NULL, ''),
('团队概览', @detail_menu_id, 7, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:overview', '#', 'admin', NOW(), '', NULL, ''),
('团别分布', @detail_menu_id, 8, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:teamDistribution', '#', 'admin', NOW(), '', NULL, ''),
('阵容分布', @detail_menu_id, 9, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:lineupDistribution', '#', 'admin', NOW(), '', NULL, ''),
('战力对比', @detail_menu_id, 10, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:powerComparison', '#', 'admin', NOW(), '', NULL, ''),
('红淬炼对比', @detail_menu_id, 11, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:redRefineComparison', '#', 'admin', NOW(), '', NULL, ''),
('战力分布', @detail_menu_id, 12, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:powerDistribution', '#', 'admin', NOW(), '', NULL, ''),
('红淬炼分布', @detail_menu_id, 13, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:redRefineDistribution', '#', 'admin', NOW(), '', NULL, ''),
('战力红淬炼散点', @detail_menu_id, 14, '', '', '', 1, 0, 'F', '0', '0', 'club:detail:powerRedRefineScatter', '#', 'admin', NOW(), '', NULL, '');