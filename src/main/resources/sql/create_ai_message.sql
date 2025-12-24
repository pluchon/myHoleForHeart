CREATE TABLE `ai_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `ai_type` varchar(50) NOT NULL COMMENT 'AI类型: general, emotional, guardian, etc.',
  `sender` varchar(10) NOT NULL COMMENT '发送者: user, ai',
  `content` text NOT NULL COMMENT '消息内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_ai` (`user_id`, `ai_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI树洞聊天记录表';
