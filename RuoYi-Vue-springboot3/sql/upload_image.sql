-- 图片上传管理表
CREATE TABLE IF NOT EXISTS `upload_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `access_path` varchar(255) DEFAULT NULL COMMENT '访问路径',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `storage_path` varchar(500) DEFAULT NULL COMMENT '服务器存储路径',
  `file_size` bigint(20) DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` varchar(20) DEFAULT NULL COMMENT '文件类型（jpg/png/jpeg）',
  `image_width` bigint(20) DEFAULT NULL COMMENT '图片宽度（px）',
  `image_height` bigint(20) DEFAULT NULL COMMENT '图片高度（px）',
  `upload_type` varchar(20) DEFAULT 'other' COMMENT '上传类型（battle/resource/other）',
  `upload_status` varchar(20) DEFAULT 'uploaded' COMMENT '上传状态（uploaded=已上传,processing=处理中,processed=已处理,deleted=已删除）',
  `md5_hash` varchar(32) DEFAULT NULL COMMENT '文件MD5值',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片上传管理';