-- 创建批量日期设置记录表，用于记录批量设置的日期信息
CREATE TABLE IF NOT EXISTS `batch_date_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `setting_date` date NOT NULL COMMENT '设置的日期',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批量日期设置记录表';

-- 插入一条默认记录，使用当前日期
INSERT INTO `batch_date_setting` (`setting_date`, `create_by`, `remark`) 
VALUES (CURRENT_DATE, 'system', '默认批量设置日期');