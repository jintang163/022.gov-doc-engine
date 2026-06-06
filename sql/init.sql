-- =============================================
-- 政务协同办公公文引擎 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- =============================================

CREATE DATABASE IF NOT EXISTS `gov_doc` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `gov_doc`;

-- =============================================
-- 1. 公文模板表 (doc_template)
-- =============================================
DROP TABLE IF EXISTS `doc_template`;
CREATE TABLE `doc_template` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_code` VARCHAR(64) NOT NULL COMMENT '模板编码',
    `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(32) NOT NULL COMMENT '模板类型：上行文/下行文/平行文/内部文',
    `template_category` VARCHAR(32) COMMENT '模板分类：请示/报告/通知/批复/函等',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `is_current_version` TINYINT NOT NULL DEFAULT 1 COMMENT '是否当前版本：0否1是',
    `parent_template_id` BIGINT COMMENT '父模板ID（版本追溯）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0草稿1已发布2已停用',
    `description` VARCHAR(512) COMMENT '模板说明',
    `unit_code` VARCHAR(64) COMMENT '适用单位编码',
    `unit_name` VARCHAR(128) COMMENT '适用单位名称',
    `security_level` VARCHAR(32) COMMENT '保密等级：普通/秘密/机密/绝密',
    `urgency_level` VARCHAR(32) COMMENT '紧急程度：普通/加急/特急',
    `permission_roles` VARCHAR(512) COMMENT '可用角色ID列表，逗号分隔',
    `permission_users` VARCHAR(512) COMMENT '可用用户ID列表，逗号分隔',
    `permission_depts` VARCHAR(512) COMMENT '可用部门ID列表，逗号分隔',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code_version` (`template_code`, `version`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公文模板表';

-- =============================================
-- 2. 公文红头样式表 (doc_template_header)
-- =============================================
DROP TABLE IF EXISTS `doc_template_header`;
CREATE TABLE `doc_template_header` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `header_type` VARCHAR(32) NOT NULL DEFAULT 'standard' COMMENT '红头类型：standard标准/custom自定义',
    `unit_name` VARCHAR(128) NOT NULL COMMENT '发文单位名称',
    `unit_name_font` VARCHAR(32) NOT NULL DEFAULT '宋体' COMMENT '单位名称字体',
    `unit_name_font_size` INT NOT NULL DEFAULT 54 COMMENT '单位名称字号（px）',
    `unit_name_font_color` VARCHAR(16) NOT NULL DEFAULT '#C60000' COMMENT '单位名称颜色',
    `unit_name_font_bold` TINYINT NOT NULL DEFAULT 1 COMMENT '是否加粗：0否1是',
    `unit_name_text_align` VARCHAR(16) NOT NULL DEFAULT 'center' COMMENT '对齐方式：left/center/right',
    `unit_name_margin_top` INT NOT NULL DEFAULT 30 COMMENT '上边距（px）',
    `unit_name_margin_bottom` INT NOT NULL DEFAULT 10 COMMENT '下边距（px）',
    `show_document_number` TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示发文字号：0否1是',
    `document_number_prefix` VARCHAR(64) COMMENT '发文字号前缀（如：XX发）',
    `document_number_year` VARCHAR(16) COMMENT '发文字号年份（如：〔2024〕）',
    `document_number_font` VARCHAR(32) NOT NULL DEFAULT '仿宋' COMMENT '发文字号字体',
    `document_number_font_size` INT NOT NULL DEFAULT 16 COMMENT '发文字号字号（px）',
    `document_number_font_color` VARCHAR(16) NOT NULL DEFAULT '#000000' COMMENT '发文字号颜色',
    `document_number_text_align` VARCHAR(16) NOT NULL DEFAULT 'right' COMMENT '发文字号对齐方式',
    `document_number_margin_top` INT NOT NULL DEFAULT 0 COMMENT '发文字号上边距',
    `document_number_margin_bottom` INT NOT NULL DEFAULT 10 COMMENT '发文字号下边距',
    `show_red_line` TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示红色分隔线：0否1是',
    `red_line_width` INT NOT NULL DEFAULT 100 COMMENT '分隔线宽度（%）',
    `red_line_height` INT NOT NULL DEFAULT 2 COMMENT '分隔线高度（px）',
    `red_line_color` VARCHAR(16) NOT NULL DEFAULT '#C60000' COMMENT '分隔线颜色',
    `red_line_margin_top` INT NOT NULL DEFAULT 10 COMMENT '分隔线上边距',
    `red_line_margin_bottom` INT NOT NULL DEFAULT 20 COMMENT '分隔线下边距',
    `show_star` TINYINT NOT NULL DEFAULT 0 COMMENT '是否显示五角星：0否1是',
    `star_size` INT NOT NULL DEFAULT 30 COMMENT '五角星大小（px）',
    `star_color` VARCHAR(16) NOT NULL DEFAULT '#C60000' COMMENT '五角星颜色',
    `page_width` INT NOT NULL DEFAULT 794 COMMENT '页面宽度（px，A4=794）',
    `page_height` INT NOT NULL DEFAULT 1123 COMMENT '页面高度（px，A4=1123）',
    `page_margin_top` INT NOT NULL DEFAULT 80 COMMENT '页面上边距（px）',
    `page_margin_bottom` INT NOT NULL DEFAULT 80 COMMENT '页面下边距（px）',
    `page_margin_left` INT NOT NULL DEFAULT 80 COMMENT '页面左边距（px）',
    `page_margin_right` INT NOT NULL DEFAULT 80 COMMENT '页面右边距（px）',
    `custom_css` TEXT COMMENT '自定义CSS样式',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_id` (`template_id`),
    KEY `idx_header_type` (`header_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公文红头样式表';

-- =============================================
-- 3. 公文模板字段表 (doc_template_field)
-- =============================================
DROP TABLE IF EXISTS `doc_template_field`;
CREATE TABLE `doc_template_field` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `field_key` VARCHAR(64) NOT NULL COMMENT '字段键名',
    `field_name` VARCHAR(64) NOT NULL COMMENT '字段显示名称',
    `field_type` VARCHAR(32) NOT NULL COMMENT '字段类型：input/textarea/select/date/datetime/radio/checkbox/upload/editor',
    `field_group` VARCHAR(32) NOT NULL DEFAULT 'main' COMMENT '字段分组：main主体/header首部/footer尾部/attachment附件',
    `is_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填：0否1是',
    `is_preset` TINYINT NOT NULL DEFAULT 0 COMMENT '是否预置字段：0否1是',
    `default_value` VARCHAR(512) COMMENT '默认值',
    `placeholder` VARCHAR(128) COMMENT '占位提示',
    `field_options` TEXT COMMENT '选项值JSON（适用于select/radio/checkbox）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `validation_rule` VARCHAR(256) COMMENT '校验规则（JSON格式）',
    `font_family` VARCHAR(32) COMMENT '字体',
    `font_size` INT COMMENT '字号（px）',
    `font_color` VARCHAR(16) COMMENT '字体颜色',
    `font_bold` TINYINT COMMENT '是否加粗：0否1是',
    `text_align` VARCHAR(16) COMMENT '对齐方式：left/center/right',
    `margin_top` INT COMMENT '上边距（px）',
    `margin_bottom` INT COMMENT '下边距（px）',
    `margin_left` INT COMMENT '左边距（px）',
    `margin_right` INT COMMENT '右边距（px）',
    `line_height` DECIMAL(4,2) COMMENT '行高',
    `text_indent` INT COMMENT '首行缩进（字符）',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_field_key` (`template_id`, `field_key`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_field_group` (`field_group`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公文模板字段表';

-- =============================================
-- 4. 公文表 (doc_document)
-- =============================================
DROP TABLE IF EXISTS `doc_document`;
CREATE TABLE `doc_document` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `doc_title` VARCHAR(256) NOT NULL COMMENT '公文标题',
    `doc_number` VARCHAR(128) COMMENT '发文字号',
    `doc_type` VARCHAR(32) COMMENT '公文类型',
    `security_level` VARCHAR(32) COMMENT '保密等级',
    `urgency_level` VARCHAR(32) COMMENT '紧急程度',
    `main_send_dept` VARCHAR(512) COMMENT '主送机关',
    `copy_send_dept` VARCHAR(512) COMMENT '抄送机关',
    `signer` VARCHAR(64) COMMENT '签发人',
    `sign_date` DATE COMMENT '签发日期',
    `written_date` DATE COMMENT '成文日期',
    `doc_content` LONGTEXT COMMENT '公文正文内容（HTML）',
    `attachment_info` TEXT COMMENT '附件信息JSON',
    `field_data` TEXT COMMENT '自定义字段数据JSON',
    `status` VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '状态：draft草稿/pending待审/reviewing审批中/approved已通过/rejected已驳回/archived已归档',
    `current_node` VARCHAR(64) COMMENT '当前审批节点',
    `process_instance_id` VARCHAR(64) COMMENT '流程实例ID',
    `creator_dept_id` VARCHAR(64) COMMENT '创建部门ID',
    `creator_dept_name` VARCHAR(128) COMMENT '创建部门名称',
    `unit_code` VARCHAR(64) COMMENT '所属单位编码',
    `unit_name` VARCHAR(128) COMMENT '所属单位名称',
    `remark` VARCHAR(256) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_doc_number` (`doc_number`),
    KEY `idx_status` (`status`),
    KEY `idx_create_by` (`create_by`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公文表';

-- =============================================
-- 初始化数据 - 预置公文模板
-- =============================================

-- 插入下行文通知模板
INSERT INTO `doc_template` (`id`, `template_code`, `template_name`, `template_type`, `template_category`, `version`, `is_current_version`, `status`, `description`, `unit_name`)
VALUES (1001, 'TPL_DOWN_NOTICE_001', '下行文-通知模板', '下行文', '通知', 1, 1, 1, '适用于向下级机关发布指示、布置工作、传达有关事项等', 'XX省人民政府办公厅');

-- 插入上行文请示模板
INSERT INTO `doc_template` (`id`, `template_code`, `template_name`, `template_type`, `template_category`, `version`, `is_current_version`, `status`, `description`, `unit_name`)
VALUES (1002, 'TPL_UP_REQUEST_001', '上行文-请示模板', '上行文', '请示', 1, 1, 1, '适用于向上级机关请求指示、批准事项', 'XX省人民政府办公厅');

-- 插入上行文报告模板
INSERT INTO `doc_template` (`id`, `template_code`, `template_name`, `template_type`, `template_category`, `version`, `is_current_version`, `status`, `description`, `unit_name`)
VALUES (1003, 'TPL_UP_REPORT_001', '上行文-报告模板', '上行文', '报告', 1, 1, 1, '适用于向上级机关汇报工作、反映情况、答复询问', 'XX省人民政府办公厅');

-- 插入平行文函模板
INSERT INTO `doc_template` (`id`, `template_code`, `template_name`, `template_type`, `template_category`, `version`, `is_current_version`, `status`, `description`, `unit_name`)
VALUES (1004, 'TPL_PARALLEL_LETTER_001', '平行文-函模板', '平行文', '函', 1, 1, 1, '适用于不相隶属机关之间商洽工作、询问和答复问题', 'XX省人民政府办公厅');

-- =============================================
-- 初始化红头样式数据
-- =============================================

-- 下行文通知红头样式
INSERT INTO `doc_template_header` (`id`, `template_id`, `unit_name`, `unit_name_font_size`, `unit_name_font_color`, `document_number_prefix`, `document_number_year`)
VALUES (2001, 1001, 'XX省人民政府办公厅', 54, '#C60000', 'XX政办发', '〔2024〕');

-- 上行文请示红头样式
INSERT INTO `doc_template_header` (`id`, `template_id`, `unit_name`, `unit_name_font_size`, `unit_name_font_color`, `document_number_prefix`, `document_number_year`)
VALUES (2002, 1002, 'XX省人民政府办公厅', 54, '#C60000', 'XX政办请', '〔2024〕');

-- 上行文报告红头样式
INSERT INTO `doc_template_header` (`id`, `template_id`, `unit_name`, `unit_name_font_size`, `unit_name_font_color`, `document_number_prefix`, `document_number_year`)
VALUES (2003, 1003, 'XX省人民政府办公厅', 54, '#C60000', 'XX政办报', '〔2024〕');

-- 平行文函红头样式
INSERT INTO `doc_template_header` (`id`, `template_id`, `unit_name`, `unit_name_font_size`, `unit_name_font_color`, `document_number_prefix`, `document_number_year`)
VALUES (2004, 1004, 'XX省人民政府办公厅', 54, '#C60000', 'XX政办函', '〔2024〕');

-- =============================================
-- 初始化模板字段数据
-- =============================================

-- 预置字段列表
-- 1. 发文字号 2. 签发人 3. 标题 4. 主送机关 5. 正文 6. 附件 7. 成文日期 8. 抄送机关

-- 通知模板字段
INSERT INTO `doc_template_field` (`id`, `template_id`, `field_key`, `field_name`, `field_type`, `field_group`, `is_required`, `is_preset`, `sort_order`, `font_family`, `font_size`, `font_bold`, `text_align`, `line_height`, `text_indent`)
VALUES
(3001, 1001, 'docNumber', '发文字号', 'input', 'header', 1, 1, 1, '仿宋', 16, 0, 'right', 1.50, 0),
(3002, 1001, 'title', '标题', 'input', 'main', 1, 1, 2, '黑体', 22, 1, 'center', 1.50, 0),
(3003, 1001, 'mainSendDept', '主送机关', 'input', 'main', 1, 1, 3, '仿宋', 16, 0, 'left', 1.50, 0),
(3004, 1001, 'content', '正文', 'editor', 'main', 1, 1, 4, '仿宋', 16, 0, 'left', 1.50, 2),
(3005, 1001, 'attachment', '附件', 'textarea', 'main', 0, 1, 5, '仿宋', 16, 0, 'left', 1.50, 2),
(3006, 1001, 'writtenDate', '成文日期', 'date', 'footer', 1, 1, 6, '仿宋', 16, 0, 'right', 1.50, 0),
(3007, 1001, 'copySendDept', '抄送机关', 'input', 'footer', 0, 1, 7, '仿宋', 14, 0, 'left', 1.50, 0);

-- 请示模板字段（含签发人）
INSERT INTO `doc_template_field` (`id`, `template_id`, `field_key`, `field_name`, `field_type`, `field_group`, `is_required`, `is_preset`, `sort_order`, `font_family`, `font_size`, `font_bold`, `text_align`, `line_height`, `text_indent`)
VALUES
(3101, 1002, 'docNumber', '发文字号', 'input', 'header', 1, 1, 1, '仿宋', 16, 0, 'right', 1.50, 0),
(3102, 1002, 'signer', '签发人', 'input', 'header', 1, 1, 2, '仿宋', 16, 0, 'right', 1.50, 0),
(3103, 1002, 'title', '标题', 'input', 'main', 1, 1, 3, '黑体', 22, 1, 'center', 1.50, 0),
(3104, 1002, 'mainSendDept', '主送机关', 'input', 'main', 1, 1, 4, '仿宋', 16, 0, 'left', 1.50, 0),
(3105, 1002, 'content', '正文', 'editor', 'main', 1, 1, 5, '仿宋', 16, 0, 'left', 1.50, 2),
(3106, 1002, 'writtenDate', '成文日期', 'date', 'footer', 1, 1, 6, '仿宋', 16, 0, 'right', 1.50, 0);

-- 报告模板字段
INSERT INTO `doc_template_field` (`id`, `template_id`, `field_key`, `field_name`, `field_type`, `field_group`, `is_required`, `is_preset`, `sort_order`, `font_family`, `font_size`, `font_bold`, `text_align`, `line_height`, `text_indent`)
VALUES
(3201, 1003, 'docNumber', '发文字号', 'input', 'header', 1, 1, 1, '仿宋', 16, 0, 'right', 1.50, 0),
(3202, 1003, 'signer', '签发人', 'input', 'header', 0, 1, 2, '仿宋', 16, 0, 'right', 1.50, 0),
(3203, 1003, 'title', '标题', 'input', 'main', 1, 1, 3, '黑体', 22, 1, 'center', 1.50, 0),
(3204, 1003, 'mainSendDept', '主送机关', 'input', 'main', 1, 1, 4, '仿宋', 16, 0, 'left', 1.50, 0),
(3205, 1003, 'content', '正文', 'editor', 'main', 1, 1, 5, '仿宋', 16, 0, 'left', 1.50, 2),
(3206, 1003, 'attachment', '附件', 'textarea', 'main', 0, 1, 6, '仿宋', 16, 0, 'left', 1.50, 2),
(3207, 1003, 'writtenDate', '成文日期', 'date', 'footer', 1, 1, 7, '仿宋', 16, 0, 'right', 1.50, 0);

-- 函模板字段
INSERT INTO `doc_template_field` (`id`, `template_id`, `field_key`, `field_name`, `field_type`, `field_group`, `is_required`, `is_preset`, `sort_order`, `font_family`, `font_size`, `font_bold`, `text_align`, `line_height`, `text_indent`)
VALUES
(3301, 1004, 'docNumber', '发文字号', 'input', 'header', 1, 1, 1, '仿宋', 16, 0, 'right', 1.50, 0),
(3302, 1004, 'title', '标题', 'input', 'main', 1, 1, 2, '黑体', 22, 1, 'center', 1.50, 0),
(3303, 1004, 'mainSendDept', '主送机关', 'input', 'main', 1, 1, 3, '仿宋', 16, 0, 'left', 1.50, 0),
(3304, 1004, 'content', '正文', 'editor', 'main', 1, 1, 4, '仿宋', 16, 0, 'left', 1.50, 2),
(3305, 1004, 'writtenDate', '成文日期', 'date', 'footer', 1, 1, 5, '仿宋', 16, 0, 'right', 1.50, 0);
