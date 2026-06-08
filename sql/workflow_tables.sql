-- =============================================
-- 会签与审批流程模块 - 数据库表
-- =============================================

USE `gov_doc`;

-- =============================================
-- 1. 流程定义表 (wf_process_definition)
-- =============================================
DROP TABLE IF EXISTS `wf_process_definition`;
CREATE TABLE `wf_process_definition` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `process_code` VARCHAR(64) NOT NULL COMMENT '流程编码',
    `process_name` VARCHAR(128) NOT NULL COMMENT '流程名称',
    `process_type` VARCHAR(32) NOT NULL COMMENT '流程类型：approval审批/sign会签',
    `process_category` VARCHAR(32) COMMENT '流程分类：公文审批/财务审批/人事审批等',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `is_current_version` TINYINT NOT NULL DEFAULT 1 COMMENT '是否当前版本：0否1是',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0草稿1已发布2已停用',
    `description` VARCHAR(512) COMMENT '流程说明',
    `bpmn_xml` LONGTEXT COMMENT 'BPMN XML定义',
    `process_graph` LONGTEXT COMMENT '流程图JSON定义（节点和连线）',
    `form_key` VARCHAR(64) COMMENT '关联表单标识',
    `unit_code` VARCHAR(64) COMMENT '适用单位编码',
    `unit_name` VARCHAR(128) COMMENT '适用单位名称',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_process_code_version` (`process_code`, `version`),
    KEY `idx_process_type` (`process_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程定义表';

-- =============================================
-- 2. 流程节点定义表 (wf_process_node)
-- =============================================
DROP TABLE IF EXISTS `wf_process_node`;
CREATE TABLE `wf_process_node` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `process_def_id` BIGINT NOT NULL COMMENT '流程定义ID',
    `node_id` VARCHAR(64) NOT NULL COMMENT '节点标识',
    `node_name` VARCHAR(128) NOT NULL COMMENT '节点名称',
    `node_type` VARCHAR(32) NOT NULL COMMENT '节点类型：start开始/end结束/userTask用户任务/parallelGateway并行网关/exclusiveGateway排他网关/inclusiveGateway包容网关/countersign会签',
    `node_config` LONGTEXT COMMENT '节点配置JSON（审批人、会签规则等）',
    `form_properties` TEXT COMMENT '表单属性JSON',
    `x` INT COMMENT '节点X坐标',
    `y` INT COMMENT '节点Y坐标',
    `width` INT COMMENT '节点宽度',
    `height` INT COMMENT '节点高度',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_process_node` (`process_def_id`, `node_id`),
    KEY `idx_process_def_id` (`process_def_id`),
    KEY `idx_node_type` (`node_type`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程节点定义表';

-- =============================================
-- 3. 流程连线定义表 (wf_process_edge)
-- =============================================
DROP TABLE IF EXISTS `wf_process_edge`;
CREATE TABLE `wf_process_edge` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `process_def_id` BIGINT NOT NULL COMMENT '流程定义ID',
    `edge_id` VARCHAR(64) NOT NULL COMMENT '连线标识',
    `edge_name` VARCHAR(128) COMMENT '连线名称',
    `source_node_id` VARCHAR(64) NOT NULL COMMENT '源节点标识',
    `target_node_id` VARCHAR(64) NOT NULL COMMENT '目标节点标识',
    `condition_expression` VARCHAR(512) COMMENT '条件表达式',
    `condition_label` VARCHAR(128) COMMENT '条件标签',
    `edge_points` TEXT COMMENT '连线坐标点JSON',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_process_edge` (`process_def_id`, `edge_id`),
    KEY `idx_process_def_id` (`process_def_id`),
    KEY `idx_source_node` (`source_node_id`),
    KEY `idx_target_node` (`target_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程连线定义表';

-- =============================================
-- 4. 流程实例表 (wf_process_instance)
-- =============================================
DROP TABLE IF EXISTS `wf_process_instance`;
CREATE TABLE `wf_process_instance` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `instance_no` VARCHAR(64) NOT NULL COMMENT '流程实例编号',
    `process_def_id` BIGINT NOT NULL COMMENT '流程定义ID',
    `process_code` VARCHAR(64) NOT NULL COMMENT '流程编码',
    `process_name` VARCHAR(128) NOT NULL COMMENT '流程名称',
    `business_key` VARCHAR(64) COMMENT '业务主键（如公文ID）',
    `business_type` VARCHAR(32) COMMENT '业务类型',
    `business_title` VARCHAR(256) COMMENT '业务标题',
    `status` VARCHAR(32) NOT NULL DEFAULT 'running' COMMENT '状态：running运行中/completed已完成/suspended已挂起/terminated已终止',
    `start_user_id` VARCHAR(64) COMMENT '发起人ID',
    `start_user_name` VARCHAR(128) COMMENT '发起人姓名',
    `start_dept_id` VARCHAR(64) COMMENT '发起部门ID',
    `start_dept_name` VARCHAR(128) COMMENT '发起部门名称',
    `start_time` DATETIME COMMENT '发起时间',
    `end_time` DATETIME COMMENT '结束时间',
    `duration` BIGINT COMMENT '耗时（毫秒）',
    `current_node_id` VARCHAR(64) COMMENT '当前节点标识',
    `current_node_name` VARCHAR(128) COMMENT '当前节点名称',
    `form_data` LONGTEXT COMMENT '表单数据JSON',
    `variables` LONGTEXT COMMENT '流程变量JSON',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_instance_no` (`instance_no`),
    KEY `idx_process_def_id` (`process_def_id`),
    KEY `idx_business_key` (`business_key`),
    KEY `idx_start_user_id` (`start_user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程实例表';

-- =============================================
-- 5. 任务表 (wf_task)
-- =============================================
DROP TABLE IF EXISTS `wf_task`;
CREATE TABLE `wf_task` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `task_no` VARCHAR(64) NOT NULL COMMENT '任务编号',
    `process_instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
    `process_def_id` BIGINT NOT NULL COMMENT '流程定义ID',
    `node_id` VARCHAR(64) NOT NULL COMMENT '节点标识',
    `node_name` VARCHAR(128) NOT NULL COMMENT '节点名称',
    `task_type` VARCHAR(32) NOT NULL DEFAULT 'userTask' COMMENT '任务类型：userTask用户任务/countersign会签任务',
    `business_key` VARCHAR(64) COMMENT '业务主键',
    `business_type` VARCHAR(32) COMMENT '业务类型',
    `business_title` VARCHAR(256) COMMENT '业务标题',
    `status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态：pending待处理/processing处理中/completed已完成/delegated已转办/canceled已取消',
    `assignee_type` VARCHAR(32) COMMENT '处理人类型：user用户/post岗位/dept部门/role角色',
    `assignee_id` VARCHAR(64) COMMENT '处理人ID',
    `assignee_name` VARCHAR(128) COMMENT '处理人姓名',
    `delegated_from_user_id` VARCHAR(64) COMMENT '转办来源用户ID',
    `delegated_from_user_name` VARCHAR(128) COMMENT '转办来源用户姓名',
    `claim_time` DATETIME COMMENT '签收时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `due_time` DATETIME COMMENT '到期时间',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级：0普通1高2紧急',
    `form_data` LONGTEXT COMMENT '表单数据JSON',
    `variables` LONGTEXT COMMENT '任务变量JSON',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_no` (`task_no`),
    KEY `idx_process_instance_id` (`process_instance_id`),
    KEY `idx_process_def_id` (`process_def_id`),
    KEY `idx_node_id` (`node_id`),
    KEY `idx_assignee_id` (`assignee_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_business_key` (`business_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- =============================================
-- 6. 会签表 (wf_countersign)
-- =============================================
DROP TABLE IF EXISTS `wf_countersign`;
CREATE TABLE `wf_countersign` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `countersign_no` VARCHAR(64) NOT NULL COMMENT '会签编号',
    `process_instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
    `process_def_id` BIGINT NOT NULL COMMENT '流程定义ID',
    `node_id` VARCHAR(64) NOT NULL COMMENT '节点标识',
    `node_name` VARCHAR(128) NOT NULL COMMENT '节点名称',
    `task_id` BIGINT COMMENT '主任务ID',
    `business_key` VARCHAR(64) COMMENT '业务主键',
    `business_type` VARCHAR(32) COMMENT '业务类型',
    `business_title` VARCHAR(256) COMMENT '业务标题',
    `countersign_type` VARCHAR(32) NOT NULL DEFAULT 'parallel' COMMENT '会签类型：parallel并行/sequential顺序',
    `vote_type` VARCHAR(32) NOT NULL DEFAULT 'all_pass' COMMENT '投票规则：one_pass一票通过/all_pass全部通过/one_reject一票否决/percentage百分比通过',
    `pass_percentage` INT COMMENT '通过百分比（vote_type=percentage时有效）',
    `sign_order` VARCHAR(512) COMMENT '顺序会签时的签署顺序JSON',
    `status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态：pending待开始/signing会签中/completed已完成/rejected已否决',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '应签人数',
    `signed_count` INT NOT NULL DEFAULT 0 COMMENT '已签人数',
    `passed_count` INT NOT NULL DEFAULT 0 COMMENT '同意人数',
    `rejected_count` INT NOT NULL DEFAULT 0 COMMENT '反对人数',
    `abstained_count` INT NOT NULL DEFAULT 0 COMMENT '弃权人数',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `variables` LONGTEXT COMMENT '会签变量JSON',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_countersign_no` (`countersign_no`),
    KEY `idx_process_instance_id` (`process_instance_id`),
    KEY `idx_process_def_id` (`process_def_id`),
    KEY `idx_node_id` (`node_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会签表';

-- =============================================
-- 7. 会签签批表 (wf_countersign_item)
-- =============================================
DROP TABLE IF EXISTS `wf_countersign_item`;
CREATE TABLE `wf_countersign_item` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `countersign_id` BIGINT NOT NULL COMMENT '会签ID',
    `process_instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
    `task_id` BIGINT COMMENT '任务ID',
    `sign_user_id` VARCHAR(64) NOT NULL COMMENT '签署人ID',
    `sign_user_name` VARCHAR(128) NOT NULL COMMENT '签署人姓名',
    `sign_user_dept_id` VARCHAR(64) COMMENT '签署人部门ID',
    `sign_user_dept_name` VARCHAR(128) COMMENT '签署人部门名称',
    `sign_type` VARCHAR(32) COMMENT '签署人类型：user用户/post岗位/dept部门',
    `sign_order` INT COMMENT '签署顺序（顺序会签时有效）',
    `status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态：pending待签/signed已签/rejected已拒绝',
    `sign_result` VARCHAR(32) COMMENT '签署结果：agree同意/reject反对/abstain弃权',
    `sign_opinion` TEXT COMMENT '签署意见',
    `sign_time` DATETIME COMMENT '签署时间',
    `duration` BIGINT COMMENT '耗时（毫秒）',
    `sign_sequence` INT COMMENT '实际签署序号',
    `variables` LONGTEXT COMMENT '签署变量JSON',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    KEY `idx_countersign_id` (`countersign_id`),
    KEY `idx_process_instance_id` (`process_instance_id`),
    KEY `idx_sign_user_id` (`sign_user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_sign_order` (`sign_order`),
    KEY `idx_sign_time` (`sign_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会签签批表';

-- =============================================
-- 8. 审批意见表 (wf_approval_opinion)
-- =============================================
DROP TABLE IF EXISTS `wf_approval_opinion`;
CREATE TABLE `wf_approval_opinion` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `process_instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
    `task_id` BIGINT COMMENT '任务ID',
    `node_id` VARCHAR(64) COMMENT '节点标识',
    `node_name` VARCHAR(128) COMMENT '节点名称',
    `business_key` VARCHAR(64) COMMENT '业务主键',
    `approval_type` VARCHAR(32) NOT NULL COMMENT '审批类型：pass通过/reject驳回/return退回/terminate终止/delegate转办/addSign加签/countersign会签',
    `approval_result` VARCHAR(32) COMMENT '审批结果（会签时用）：agree同意/reject反对/abstain弃权',
    `approval_opinion` TEXT COMMENT '审批意见',
    `opinion_template_id` BIGINT COMMENT '常用语模板ID',
    `attachments` TEXT COMMENT '附件信息JSON',
    `approver_id` VARCHAR(64) NOT NULL COMMENT '审批人ID',
    `approver_name` VARCHAR(128) NOT NULL COMMENT '审批人姓名',
    `approver_dept_id` VARCHAR(64) COMMENT '审批人部门ID',
    `approver_dept_name` VARCHAR(128) COMMENT '审批人部门名称',
    `target_user_id` VARCHAR(64) COMMENT '目标用户ID（转办/加签时）',
    `target_user_name` VARCHAR(128) COMMENT '目标用户姓名（转办/加签时）',
    `target_node_id` VARCHAR(64) COMMENT '目标节点ID（驳回/退回时）',
    `target_node_name` VARCHAR(128) COMMENT '目标节点名称（驳回/退回时）',
    `approval_time` DATETIME COMMENT '审批时间',
    `variables` LONGTEXT COMMENT '扩展变量JSON',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    KEY `idx_process_instance_id` (`process_instance_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_business_key` (`business_key`),
    KEY `idx_approver_id` (`approver_id`),
    KEY `idx_approval_type` (`approval_type`),
    KEY `idx_approval_time` (`approval_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批意见表';

-- =============================================
-- 9. 流程历史轨迹表 (wf_process_history)
-- =============================================
DROP TABLE IF EXISTS `wf_process_history`;
CREATE TABLE `wf_process_history` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `process_instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
    `node_id` VARCHAR(64) NOT NULL COMMENT '节点标识',
    `node_name` VARCHAR(128) NOT NULL COMMENT '节点名称',
    `node_type` VARCHAR(32) NOT NULL COMMENT '节点类型',
    `task_id` BIGINT COMMENT '任务ID',
    `operator_id` VARCHAR(64) COMMENT '操作人ID',
    `operator_name` VARCHAR(128) COMMENT '操作人姓名',
    `operation_type` VARCHAR(32) NOT NULL COMMENT '操作类型：arrive到达/complete完成/delegate转办/addSign加签/terminate终止',
    `enter_time` DATETIME COMMENT '进入时间',
    `leave_time` DATETIME COMMENT '离开时间',
    `duration` BIGINT COMMENT '耗时（毫秒）',
    `status` VARCHAR(32) COMMENT '状态：active当前/history历史',
    `variables` LONGTEXT COMMENT '变量JSON',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    KEY `idx_process_instance_id` (`process_instance_id`),
    KEY `idx_node_id` (`node_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_enter_time` (`enter_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程历史轨迹表';

-- =============================================
-- 10. 审批常用语表 (wf_opinion_template)
-- =============================================
DROP TABLE IF EXISTS `wf_opinion_template`;
CREATE TABLE `wf_opinion_template` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_type` VARCHAR(32) NOT NULL COMMENT '模板类型：pass同意/reject驳回/general通用',
    `template_content` VARCHAR(512) NOT NULL COMMENT '模板内容',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `is_preset` TINYINT NOT NULL DEFAULT 0 COMMENT '是否预置：0否1是',
    `user_id` VARCHAR(64) COMMENT '所属用户ID（个人常用语）',
    `user_name` VARCHAR(128) COMMENT '所属用户姓名',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用1启用',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批常用语表';

-- =============================================
-- 11. 流程参与者表 (wf_participant)
-- =============================================
DROP TABLE IF EXISTS `wf_participant`;
CREATE TABLE `wf_participant` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `process_def_id` BIGINT COMMENT '流程定义ID',
    `node_id` VARCHAR(64) COMMENT '节点标识',
    `participant_type` VARCHAR(32) NOT NULL COMMENT '参与者类型：user用户/post岗位/dept部门/role角色/expression表达式',
    `participant_value` VARCHAR(512) NOT NULL COMMENT '参与者值（ID或表达式）',
    `participant_name` VARCHAR(128) COMMENT '参与者名称',
    `assignment_type` VARCHAR(32) NOT NULL DEFAULT 'all' COMMENT '分配方式：all全部/any任意/candidate候选人',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    KEY `idx_process_def_id` (`process_def_id`),
    KEY `idx_node_id` (`node_id`),
    KEY `idx_participant_type` (`participant_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程参与者表';

-- =============================================
-- 初始化数据 - 审批常用语
-- =============================================
INSERT INTO `wf_opinion_template` (`id`, `template_type`, `template_content`, `sort_order`, `is_preset`, `status`) VALUES
(1, 'pass', '同意', 1, 1, 1),
(2, 'pass', '同意，请按此执行', 2, 1, 1),
(3, 'pass', '同意，转相关部门阅处', 3, 1, 1),
(4, 'pass', '同意，请尽快落实', 4, 1, 1),
(5, 'pass', '原则同意', 5, 1, 1),
(6, 'reject', '驳回，请修改后重新提交', 1, 1, 1),
(7, 'reject', '驳回，理由如下：', 2, 1, 1),
(8, 'reject', '不同意，原因：', 3, 1, 1),
(9, 'general', '请相关部门领导审阅', 1, 1, 1),
(10, 'general', '请领导批示', 2, 1, 1),
(11, 'general', '已阅', 3, 1, 1),
(12, 'general', '情况属实', 4, 1, 1);

-- =============================================
-- 初始化数据 - 示例流程定义
-- =============================================
INSERT INTO `wf_process_definition` (`id`, `process_code`, `process_name`, `process_type`, `process_category`, `version`, `is_current_version`, `status`, `description`, `unit_name`)
VALUES
(1, 'WF_DOC_APPROVAL_001', '公文标准审批流程', 'approval', '公文审批', 1, 1, 1, '适用于一般公文的审批流转', 'XX省人民政府办公厅');

-- 初始化示例流程节点
INSERT INTO `wf_process_node` (`id`, `process_def_id`, `node_id`, `node_name`, `node_type`, `x`, `y`, `width`, `height`, `sort_order`)
VALUES
(1, 1, 'start', '开始', 'start', 100, 200, 50, 50, 1),
(2, 1, 'dept_audit', '部门审核', 'userTask', 250, 200, 120, 60, 2),
(3, 1, 'office_countersign', '办公室会签', 'countersign', 450, 200, 120, 60, 3),
(4, 1, 'leader_approval', '领导审批', 'userTask', 650, 200, 120, 60, 4),
(5, 1, 'end', '结束', 'end', 850, 200, 50, 50, 5);

-- 初始化示例流程连线
INSERT INTO `wf_process_edge` (`id`, `process_def_id`, `edge_id`, `edge_name`, `source_node_id`, `target_node_id`, `sort_order`)
VALUES
(1, 1, 'flow1', '', 'start', 'dept_audit', 1),
(2, 1, 'flow2', '', 'dept_audit', 'office_countersign', 2),
(3, 1, 'flow3', '', 'office_countersign', 'leader_approval', 3),
(4, 1, 'flow4', '', 'leader_approval', 'end', 4);
