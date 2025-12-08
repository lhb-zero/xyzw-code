-- 智能战绩录入菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('智能战绩录入', '2000', '2', 'battle', 'club/battle/index', 1, 0, 'C', '0', '0', 'club:battle:list', '#', 'admin', sysdate(), '', null, '智能战绩录入菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('上传战绩截图', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'club:battle:upload',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('确认战绩记录', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'club:battle:confirm',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('手动添加战绩', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'club:battle:add',           '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('战绩记录查询', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'club:battle:list',          '#', 'admin', sysdate(), '', null, '');

-- 战绩数据看板菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('战绩数据看板', 2000, 3, 'statistics', 'club/statistics/index', 1, 0, 'C', '0', '0', 'club:statistics:list', '#', 'admin', sysdate(), '', null, '战绩数据看板菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('战绩概览', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'club:statistics:list',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('成员排名', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'club:statistics:ranking',     '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('战绩趋势', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'club:statistics:timeline',     '#', 'admin', sysdate(), '', null, '');

-- 资源日志系统菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('资源日志系统', '2000', '4', 'log', 'club/log/index', 1, 0, 'C', '0', '0', 'club:log:list', '#', 'admin', sysdate(), '', null, '资源日志系统菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('新增日志', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'club:log:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('修改日志', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'club:log:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('删除日志', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'club:log:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('导出日志', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'club:log:export',       '#', 'admin', sysdate(), '', null, '');

-- 字典类型 SQL
insert into sys_dict_type values(100, '俱乐部日志类型', 'club_log_type', '0', 'admin', sysdate(), '', null, '俱乐部日志类型列表');

-- 字典数据 SQL
insert into sys_dict_data values(100, 1,  '个人',    'personal',  'club_log_type', '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '个人日志');
insert into sys_dict_data values(101, 2,  '俱乐部',    'club',  'club_log_type', '',   'success', 'N', '0', 'admin', sysdate(), '', null, '俱乐部日志');
insert into sys_dict_data values(102, 3,  '成员',    'member',  'club_log_type', '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '成员日志');