-- =============================================
-- 修复deleted字段：为已有表添加逻辑删除列
-- =============================================

ALTER TABLE `wf_process_definition` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `wf_process_node` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `wf_process_edge` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `wf_process_instance` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `wf_task` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `wf_process_history` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `wf_participant` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `wf_approval_opinion` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `wf_opinion_template` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `wf_countersign` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `wf_countersign_item` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `doc_document` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `doc_template` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `doc_template_header` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `doc_seal` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `doc_signature_record` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `doc_status_log` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `doc_distribution` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';
ALTER TABLE `doc_audit_log` ADD COLUMN IF NOT EXISTS `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是';

ALTER TABLE `wf_process_instance` ADD COLUMN IF NOT EXISTS `camunda_process_instance_id` varchar(64) DEFAULT NULL COMMENT 'Camunda流程实例ID';
ALTER TABLE `wf_process_instance` ADD INDEX IF NOT EXISTS `idx_camunda_process_instance_id` (`camunda_process_instance_id`);

ALTER TABLE `wf_process_definition` ADD COLUMN IF NOT EXISTS `camunda_deployment_id` varchar(64) DEFAULT NULL;
ALTER TABLE `wf_process_definition` ADD COLUMN IF NOT EXISTS `camunda_process_def_id` varchar(64) DEFAULT NULL;

ALTER TABLE `wf_task` ADD COLUMN IF NOT EXISTS `camunda_task_id` varchar(64) DEFAULT NULL;
ALTER TABLE `wf_task` ADD INDEX IF NOT EXISTS `idx_camunda_task_id` (`camunda_task_id`);
