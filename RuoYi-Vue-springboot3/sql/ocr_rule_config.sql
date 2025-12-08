-- OCR规则配置表
CREATE TABLE IF NOT EXISTS `ocr_rule_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `keyword` varchar(100) NOT NULL COMMENT '识别关键词',
  `target_field` varchar(50) NOT NULL COMMENT '目标字段名',
  `field_type` varchar(20) DEFAULT 'string' COMMENT '字段类型（int/decimal/string）',
  `is_active` bigint(1) DEFAULT 1 COMMENT '是否启用（0=否,1=是）',
  `sort_order` bigint(20) DEFAULT 0 COMMENT '排序序号',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OCR规则配置';

-- 插入默认规则
INSERT INTO `ocr_rule_config` (`keyword`, `target_field`, `field_type`, `sort_order`) VALUES
('杀敌', 'kills', 'int', 1),
('死亡', 'deaths', 'int', 2),
('刨击', 'digs', 'int', 3),
('刨地', 'digs', 'int', 4),
('复活丹', 'revives', 'int', 5),
('复活', 'revives', 'int', 6);