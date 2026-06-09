-- =============================================
-- 分发表增加签收IP/UA字段
-- =============================================

ALTER TABLE `doc_distribution`
ADD COLUMN IF NOT EXISTS `receive_ip` VARCHAR(64) DEFAULT NULL COMMENT '签收IP' AFTER `receive_time`,
ADD COLUMN IF NOT EXISTS `receive_ua` VARCHAR(256) DEFAULT NULL COMMENT '签收客户端信息' AFTER `receive_ip`;
