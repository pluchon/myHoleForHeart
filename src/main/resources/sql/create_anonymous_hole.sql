CREATE TABLE `anonymous_hole` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content` text NOT NULL COMMENT '匿名内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='真正匿名树洞表';

-- 初始化测试数据
INSERT INTO `anonymous_hole` (`content`) VALUES 
('有时候，孤独是最好的朋友。'),
('我想去一个没人认识我的地方重新开始。'),
('秘密之所以是秘密，是因为它太沉重了。'),
('今天的晚霞很美，可惜没人分享。'),
('即使在人群中，也感觉像是独自一人。'),
('我希望有人能看穿我的逞强，明白我的脆弱。'),
('有些话只能对自己说，有些路只能一个人走。'),
('在夜深人静的时候，才敢面对真实的自己。'),
('生活总是充满了意外，但我们必须学会接受。'),
('每一个微笑背后，都可能隐藏着不为人知的故事。');