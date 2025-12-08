-- 更新club_ocr_result表，添加关键字段
ALTER TABLE `club_ocr_result` 
ADD COLUMN `confidence_score` decimal(5,2) DEFAULT '0.00' COMMENT 'OCR识别置信度' AFTER `status`,
ADD COLUMN `is_confirmed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已确认' AFTER `confidence_score`,
ADD COLUMN `confirmed_record_id` int DEFAULT NULL COMMENT '关联的战绩记录ID' AFTER `is_confirmed`,
ADD COLUMN `ocr_engine_version` varchar(50) DEFAULT NULL COMMENT 'OCR引擎版本' AFTER `confirmed_record_id`,
ADD COLUMN `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数' AFTER `ocr_engine_version`;

-- 添加索引
ALTER TABLE `club_ocr_result` 
ADD INDEX `idx_is_confirmed` (`is_confirmed`),
ADD INDEX `idx_confirmed_record` (`confirmed_record_id`);