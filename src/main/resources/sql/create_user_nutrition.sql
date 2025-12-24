CREATE TABLE IF NOT EXISTS `user_nutrition` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `seed_count` int(11) DEFAULT '0' COMMENT '种子数量',
  `last_check_in_date` date DEFAULT NULL COMMENT '上次签到日期',
  `continuous_days` int(11) DEFAULT '0' COMMENT '连续签到天数',
  `total_days` int(11) DEFAULT '0' COMMENT '总签到天数',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户养料/签到表';
