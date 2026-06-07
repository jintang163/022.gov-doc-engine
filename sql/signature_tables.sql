-- =============================================
-- 电子签章模块 - 数据库表
-- =============================================

USE `gov_doc`;

-- =============================================
-- 7. 印章表 (doc_seal)
-- =============================================
DROP TABLE IF EXISTS `doc_seal`;
CREATE TABLE `doc_seal` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `seal_name` VARCHAR(128) NOT NULL COMMENT '印章名称',
    `seal_type` VARCHAR(32) NOT NULL COMMENT '印章类型：UNIT单位章/DEPT部门章/SIGNATURE签名章',
    `seal_code` VARCHAR(64) COMMENT '印章编码',
    `owner_unit_id` VARCHAR(64) COMMENT '所属单位ID',
    `owner_unit_name` VARCHAR(128) COMMENT '所属单位名称',
    `owner_dept_id` VARCHAR(64) COMMENT '所属部门ID',
    `owner_dept_name` VARCHAR(128) COMMENT '所属部门名称',
    `owner_user_id` VARCHAR(64) COMMENT '持有人用户ID（签名章）',
    `owner_user_name` VARCHAR(64) COMMENT '持有人姓名（签名章）',
    `seal_image_path` VARCHAR(512) NOT NULL COMMENT '印章图片路径',
    `seal_image_name` VARCHAR(256) COMMENT '印章图片文件名',
    `seal_width` INT DEFAULT 150 COMMENT '印章宽度（px）',
    `seal_height` INT DEFAULT 150 COMMENT '印章高度（px）',
    `certificate_serial` VARCHAR(128) COMMENT '证书序列号',
    `certificate_subject` VARCHAR(512) COMMENT '证书主题DN',
    `certificate_issuer` VARCHAR(512) COMMENT '证书颁发者DN',
    `certificate_valid_from` DATETIME COMMENT '证书有效期开始',
    `certificate_valid_to` DATETIME COMMENT '证书有效期结束',
    `private_key_path` VARCHAR(512) COMMENT '私钥存储路径（加密存储）',
    `algorithm` VARCHAR(32) DEFAULT 'SM2' COMMENT '加密算法：SM2/RSA',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用1启用2已过期',
    `password` VARCHAR(256) COMMENT '印章密码（加密存储）',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_seal_code` (`seal_code`),
    KEY `idx_seal_type` (`seal_type`),
    KEY `idx_owner_unit` (`owner_unit_id`),
    KEY `idx_owner_dept` (`owner_dept_id`),
    KEY `idx_owner_user` (`owner_user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='印章表';

-- =============================================
-- 8. 印章授权表 (doc_seal_grant)
-- =============================================
DROP TABLE IF EXISTS `doc_seal_grant`;
CREATE TABLE `doc_seal_grant` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `seal_id` BIGINT NOT NULL COMMENT '印章ID',
    `grant_type` VARCHAR(32) NOT NULL COMMENT '授权类型：USER用户/DEPT部门/ROLE角色',
    `grant_target_id` VARCHAR(64) NOT NULL COMMENT '授权目标ID',
    `grant_target_name` VARCHAR(128) NOT NULL COMMENT '授权目标名称',
    `grant_start_time` DATETIME COMMENT '授权开始时间',
    `grant_end_time` DATETIME COMMENT '授权结束时间',
    `sign_limit` INT DEFAULT -1 COMMENT '签章次数限制：-1无限制',
    `sign_count` INT DEFAULT 0 COMMENT '已签章次数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0已撤销1有效2已过期',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_seal_grant` (`seal_id`, `grant_type`, `grant_target_id`),
    KEY `idx_seal_id` (`seal_id`),
    KEY `idx_grant_target` (`grant_type`, `grant_target_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='印章授权表';

-- =============================================
-- 9. 签章记录表 (doc_signature)
-- =============================================
DROP TABLE IF EXISTS `doc_signature`;
CREATE TABLE `doc_signature` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `document_id` BIGINT NOT NULL COMMENT '公文ID',
    `document_title` VARCHAR(256) COMMENT '公文标题（冗余）',
    `document_version` INT DEFAULT 1 COMMENT '文档版本号',
    `seal_id` BIGINT NOT NULL COMMENT '印章ID',
    `seal_name` VARCHAR(128) COMMENT '印章名称（冗余）',
    `seal_type` VARCHAR(32) COMMENT '印章类型（冗余）',
    `signature_type` VARCHAR(32) NOT NULL COMMENT '签章类型：SIGNATURE落款章/RIDING骑缝章',
    `page_number` INT COMMENT '页码（落款章时为具体页码，骑缝章时为0）',
    `total_pages` INT COMMENT '总页数',
    `position_x` INT NOT NULL COMMENT '签章位置X坐标（px）',
    `position_y` INT NOT NULL COMMENT '签章位置Y坐标（px）',
    `seal_width` INT DEFAULT 150 COMMENT '签章显示宽度（px）',
    `seal_height` INT DEFAULT 150 COMMENT '签章显示高度（px）',
    `signature_reason` VARCHAR(256) COMMENT '签章原因',
    `signature_location` VARCHAR(128) COMMENT '签章地点',
    `signed_file_path` VARCHAR(512) NOT NULL COMMENT '签章后文件路径',
    `signed_file_name` VARCHAR(256) COMMENT '签章后文件名',
    `file_hash` VARCHAR(128) NOT NULL COMMENT '文件哈希值（SM3）',
    `signature_value` TEXT NOT NULL COMMENT '签名值（Base64编码）',
    `certificate_serial` VARCHAR(128) COMMENT '证书序列号',
    `sign_time` DATETIME NOT NULL COMMENT '签章时间',
    `signer_id` VARCHAR(64) NOT NULL COMMENT '签章人ID',
    `signer_name` VARCHAR(64) NOT NULL COMMENT '签章人姓名',
    `signer_dept_id` VARCHAR(64) COMMENT '签章人部门ID',
    `signer_dept_name` VARCHAR(128) COMMENT '签章人部门名称',
    `algorithm` VARCHAR(32) DEFAULT 'SM2' COMMENT '签名算法：SM2withSM3',
    `verify_status` TINYINT DEFAULT 1 COMMENT '验证状态：0未验证1验证通过2验证失败3文档已篡改',
    `verify_time` DATETIME COMMENT '最后验证时间',
    `verify_count` INT DEFAULT 0 COMMENT '验证次数',
    `is_valid` TINYINT DEFAULT 1 COMMENT '是否有效：0无效1有效',
    `revoke_reason` VARCHAR(256) COMMENT '撤销原因',
    `revoke_time` DATETIME COMMENT '撤销时间',
    `revoke_by` VARCHAR(64) COMMENT '撤销人',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_seal_id` (`seal_id`),
    KEY `idx_signer_id` (`signer_id`),
    KEY `idx_sign_time` (`sign_time`),
    KEY `idx_verify_status` (`verify_status`),
    KEY `idx_is_valid` (`is_valid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签章记录表';

-- =============================================
-- 10. 签章日志表 (doc_signature_log)
-- =============================================
DROP TABLE IF EXISTS `doc_signature_log`;
CREATE TABLE `doc_signature_log` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `operation_type` VARCHAR(32) NOT NULL COMMENT '操作类型：UPLOAD上传/ASSIGN分配/REVOKE撤销/SIGN签章/VERIFY验章',
    `seal_id` BIGINT COMMENT '印章ID',
    `seal_name` VARCHAR(128) COMMENT '印章名称',
    `document_id` BIGINT COMMENT '公文ID',
    `document_title` VARCHAR(256) COMMENT '公文标题',
    `signature_id` BIGINT COMMENT '签章记录ID',
    `grant_id` BIGINT COMMENT '授权ID',
    `operation_detail` TEXT COMMENT '操作详情JSON',
    `ip_address` VARCHAR(64) COMMENT '操作IP地址',
    `user_agent` VARCHAR(512) COMMENT '客户端信息',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态：0失败1成功',
    `error_message` VARCHAR(512) COMMENT '错误信息',
    `operate_time` DATETIME NOT NULL COMMENT '操作时间',
    `operator_id` VARCHAR(64) NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) NOT NULL COMMENT '操作人姓名',
    `operator_dept_id` VARCHAR(64) COMMENT '操作人部门ID',
    `operator_dept_name` VARCHAR(128) COMMENT '操作人部门名称',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_seal_id` (`seal_id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_operate_time` (`operate_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签章日志表';

-- =============================================
-- 初始化印章测试数据
-- =============================================

-- 插入单位章
INSERT INTO `doc_seal` (`id`, `seal_name`, `seal_type`, `seal_code`, `owner_unit_id`, `owner_unit_name`, `seal_image_path`, `algorithm`, `status`, `create_by`, `create_time`)
VALUES (5001, 'XX省人民政府办公厅公章', 'UNIT', 'SEAL-UNIT-001', 'UNIT001', 'XX省人民政府办公厅', '/seals/unit_gov_office.png', 'SM2', 1, 'admin', NOW());

-- 插入部门章
INSERT INTO `doc_seal` (`id`, `seal_name`, `seal_type`, `seal_code`, `owner_unit_id`, `owner_unit_name`, `owner_dept_id`, `owner_dept_name`, `seal_image_path`, `algorithm`, `status`, `create_by`, `create_time`)
VALUES (5002, 'XX省人民政府办公厅-秘书处章', 'DEPT', 'SEAL-DEPT-001', 'UNIT001', 'XX省人民政府办公厅', 'DEPT001', '秘书处', '/seals/dept_secretariat.png', 'SM2', 1, 'admin', NOW());

-- 插入签名章
INSERT INTO `doc_seal` (`id`, `seal_name`, `seal_type`, `seal_code`, `owner_unit_id`, `owner_unit_name`, `owner_user_id`, `owner_user_name`, `seal_image_path`, `algorithm`, `status`, `create_by`, `create_time`)
VALUES (5003, '张三-签名章', 'SIGNATURE', 'SEAL-SIG-001', 'UNIT001', 'XX省人民政府办公厅', 'USER001', '张三', '/seals/sign_zhangsan.png', 'SM2', 1, 'admin', NOW());

-- 插入印章授权
INSERT INTO `doc_seal_grant` (`id`, `seal_id`, `grant_type`, `grant_target_id`, `grant_target_name`, `sign_limit`, `status`, `create_by`, `create_time`)
VALUES (6001, 5001, 'USER', 'USER001', '张三', -1, 1, 'admin', NOW());

INSERT INTO `doc_seal_grant` (`id`, `seal_id`, `grant_type`, `grant_target_id`, `grant_target_name`, `sign_limit`, `status`, `create_by`, `create_time`)
VALUES (6002, 5002, 'DEPT', 'DEPT001', '秘书处', 100, 1, 'admin', NOW());

INSERT INTO `doc_seal_grant` (`id`, `seal_id`, `grant_type`, `grant_target_id`, `grant_target_name`, `sign_limit`, `status`, `create_by`, `create_time`)
VALUES (6003, 5003, 'USER', 'USER001', '张三', -1, 1, 'admin', NOW());
