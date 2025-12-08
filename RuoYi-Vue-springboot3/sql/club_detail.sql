-- ----------------------------
-- 俱乐部详情表
-- ----------------------------
DROP TABLE IF EXISTS `club_detail`;
CREATE TABLE `club_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `team_group` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '团别（1团、2团、3团等）',
  `member_count` int DEFAULT '0' COMMENT '团队成员数量',
  `total_power` bigint DEFAULT '0' COMMENT '总战力值',
  `avg_power` bigint DEFAULT '0' COMMENT '平均战力值',
  `total_red_refine` bigint DEFAULT '0' COMMENT '总红淬炼数量',
  `avg_red_refine` bigint DEFAULT '0' COMMENT '平均红淬炼数量',
  `power_stat_time` datetime DEFAULT NULL COMMENT '战力统计时间',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `team_group` (`team_group`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='俱乐部详情统计表';