-- 战绩匹配结果表
CREATE TABLE IF NOT EXISTS `club_battle_match_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '匹配结果ID',
  `ocr_result_id` bigint(20) NOT NULL COMMENT 'OCR识别结果ID',
  `nickname` varchar(100) DEFAULT NULL COMMENT '昵称',
  `member_id` bigint(20) DEFAULT NULL COMMENT '成员ID',
  `kills` int(11) DEFAULT 0 COMMENT '击杀数',
  `deaths` int(11) DEFAULT 0 COMMENT '死亡数',
  `digs` int(11) DEFAULT 0 COMMENT '刨地数',
  `revives` int(11) DEFAULT 0 COMMENT '复活数',
  `kd_ratio` decimal(10,2) DEFAULT 0.00 COMMENT 'KD比例',
  `battle_date` varchar(20) DEFAULT NULL COMMENT '战斗日期',
  `confirmed` tinyint(1) DEFAULT 0 COMMENT '是否已确认（0未确认，1已确认）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ocr_result_id` (`ocr_result_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_confirmed` (`confirmed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战绩匹配结果表';