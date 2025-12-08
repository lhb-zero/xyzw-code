-- 创建OCR暂存表，用于存储待确认的战绩记录
CREATE TABLE IF NOT EXISTS `club_ocr_pending` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ocr_result_id` bigint(20) NOT NULL COMMENT '关联OCR结果ID',
  `member_id` bigint(20) NOT NULL COMMENT '盐场成员ID',
  `member_name` varchar(50) NOT NULL COMMENT '成员名称',
  `kills` int(11) NOT NULL DEFAULT '0' COMMENT '击杀数',
  `deaths` int(11) NOT NULL DEFAULT '0' COMMENT '死亡数',
  `kd_ratio` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'KD比率',
  `digging` int(11) NOT NULL DEFAULT '0' COMMENT '刨地数',
  `revives` int(11) NOT NULL DEFAULT '0' COMMENT '复活数',
  `record_date` date NOT NULL COMMENT '战绩日期',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_ocr_result_id` (`ocr_result_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OCR识别暂存表';