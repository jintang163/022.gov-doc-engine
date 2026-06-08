-- 公文状态日志表
CREATE TABLE IF NOT EXISTS `doc_status_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `doc_id` bigint NOT NULL COMMENT '公文ID',
  `from_status` varchar(50) DEFAULT NULL COMMENT '原状态',
  `from_status_name` varchar(50) DEFAULT NULL COMMENT '原状态名称',
  `to_status` varchar(50) NOT NULL COMMENT '目标状态',
  `to_status_name` varchar(50) NOT NULL COMMENT '目标状态名称',
  `transition_reason` varchar(500) DEFAULT NULL COMMENT '流转原因',
  `operator_id` varchar(64) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) DEFAULT NULL COMMENT '操作人姓名',
  `operation_time` datetime DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_doc_id` (`doc_id`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公文状态流转日志';

-- 公文分发表
CREATE TABLE IF NOT EXISTS `doc_distribution` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `doc_id` bigint NOT NULL COMMENT '公文ID',
  `distribution_no` varchar(64) NOT NULL COMMENT '分发编号',
  `distribution_type` varchar(32) NOT NULL COMMENT '分发类型：electronic电子传输 print打印 both电子+打印',
  `distribution_type_name` varchar(50) NOT NULL COMMENT '分发类型名称',
  `main_send_units` text COMMENT '主送单位（JSON数组）',
  `copy_send_units` text COMMENT '抄送单位（JSON数组）',
  `print_count` varchar(32) DEFAULT NULL COMMENT '打印份数',
  `deliverer_id` varchar(64) DEFAULT NULL COMMENT '分发人ID',
  `deliverer_name` varchar(100) DEFAULT NULL COMMENT '分发人姓名',
  `distribute_time` datetime DEFAULT NULL COMMENT '分发时间',
  `receiver_id` varchar(64) DEFAULT NULL COMMENT '接收人ID',
  `receiver_name` varchar(100) DEFAULT NULL COMMENT '接收人姓名',
  `receive_time` datetime DEFAULT NULL COMMENT '接收时间',
  `status` varchar(32) NOT NULL COMMENT '状态：distributed已分发 received已接收 printed已打印',
  `status_name` varchar(50) NOT NULL COMMENT '状态名称',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_distribution_no` (`distribution_no`),
  KEY `idx_doc_id` (`doc_id`),
  KEY `idx_status` (`status`),
  KEY `idx_distribute_time` (`distribute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公文分发记录';

-- 给会签项表添加参与者字段
ALTER TABLE `wf_countersign_item`
ADD COLUMN IF NOT EXISTS `participant_type` varchar(32) DEFAULT NULL COMMENT '参与者类型' AFTER `sign_type`,
ADD COLUMN IF NOT EXISTS `participant_value` varchar(64) DEFAULT NULL COMMENT '参与者值' AFTER `participant_type`,
ADD COLUMN IF NOT EXISTS `participant_name` varchar(100) DEFAULT NULL COMMENT '参与者名称' AFTER `participant_value`;

-- 给公文表添加索引
ALTER TABLE `doc_document`
ADD INDEX IF NOT EXISTS `idx_process_instance_id` (`process_instance_id`),
ADD INDEX IF NOT EXISTS `idx_status` (`status`);
