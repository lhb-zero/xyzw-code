-- OCR识别结果表
CREATE TABLE IF NOT EXISTS `club_ocr_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '识别ID',
  `filename` varchar(255) NOT NULL COMMENT '图片文件名',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片URL',
  `text` longtext COMMENT '识别出的全部文本',
  `lines` longtext COMMENT '识别出的每一行文本(JSON格式)',
  `processing_time` decimal(10,3) DEFAULT NULL COMMENT '处理耗时（秒）',
  `status` varchar(20) DEFAULT 'processing' COMMENT '识别状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OCR识别结果表';

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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战绩匹配结果表';