package com.gov.doc.engine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database initialization...");

        try {
            initDocStatusLogTable();
            initDocDistributionTable();
            initDocDistributionReceiveColumns();
            initCountersignItemColumns();
            initDocDocumentIndexes();
            initDocArchiveTable();
            initDocBorrowTable();
            initDocIncomingTable();
            initDocHandlingTable();
            initDocPermissionTable();
            initDocAuditLogTable();
            initDocIntegrityTable();
            initSysUnitTable();
            initSysDeptTable();
            initSysPostTable();
            initSysUserTable();
            initDeletedColumns();
            initDocSupervisionTable();
            initDocUrgeLogTable();
            initStatEfficiencyTable();
            initDocAnalysisTable();
            initReminderLogTable();
            initRejectReasonTable();
            initRecommendTestData();

            log.info("Database initialization completed successfully");
        } catch (Exception e) {
            log.error("Database initialization failed", e);
        }
    }

    private boolean tableExists(String tableName) throws Exception {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        }
    }

    private boolean columnExists(String tableName, String columnName) throws Exception {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
                return rs.next();
            }
        }
    }

    private void executeSqlFromResource(String resourcePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        String[] statements = sql.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                try {
                    jdbcTemplate.execute(trimmed);
                    log.debug("Executed SQL: {}", trimmed.substring(0, Math.min(50, trimmed.length())));
                } catch (Exception e) {
                    if (trimmed.toUpperCase().contains("ALTER TABLE") && 
                        (e.getMessage().contains("Duplicate column name") || 
                         e.getMessage().contains("already exists") ||
                         e.getMessage().contains("Duplicate key name"))) {
                        log.debug("Column/Index already exists, skipping: {}", trimmed.substring(0, Math.min(50, trimmed.length())));
                    } else if (trimmed.toUpperCase().contains("CREATE TABLE") && 
                               e.getMessage().contains("already exists")) {
                        log.debug("Table already exists, skipping: {}", trimmed.substring(0, Math.min(50, trimmed.length())));
                    } else {
                        log.warn("SQL execution warning: {}", e.getMessage());
                    }
                }
            }
        }
    }

    private void initDocStatusLogTable() throws Exception {
        if (!tableExists("doc_status_log")) {
            log.info("Creating doc_status_log table...");
            executeSqlFromResource("db/changelog/006_add_doc_status_and_distribution.sql");
        } else {
            log.info("Table doc_status_log already exists, skipping creation");
        }
    }

    private void initDocDistributionTable() throws Exception {
        if (!tableExists("doc_distribution")) {
            log.info("Creating doc_distribution table...");
            executeSqlFromResource("db/changelog/006_add_doc_status_and_distribution.sql");
        } else {
            log.info("Table doc_distribution already exists, skipping creation");
        }
    }

    private void initDocDistributionReceiveColumns() throws Exception {
        if (tableExists("doc_distribution") && !columnExists("doc_distribution", "receive_ip")) {
            log.info("Adding receive_ip/receive_ua columns to doc_distribution...");
            executeSqlFromResource("db/changelog/009_add_distribution_receive_columns.sql");
        }
    }

    private void initCountersignItemColumns() throws Exception {
        String[] columns = {"participant_type", "participant_value", "participant_name"};
        for (String column : columns) {
            if (!columnExists("wf_countersign_item", column)) {
                log.info("Adding column {} to wf_countersign_item...", column);
                executeSqlFromResource("db/changelog/006_add_doc_status_and_distribution.sql");
                break;
            }
        }
    }

    private void initDocDocumentIndexes() throws Exception {
        try {
            jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'doc_document' AND index_name = 'idx_process_instance_id'",
                Integer.class
            );
        } catch (Exception e) {
            log.info("Adding indexes to doc_document...");
            executeSqlFromResource("db/changelog/006_add_doc_status_and_distribution.sql");
        }
    }

    private void initDocArchiveTable() throws Exception {
        if (!tableExists("doc_archive")) {
            log.info("Creating doc_archive table...");
            executeSqlFromResource("db/changelog/007_add_archive_and_borrow.sql");
        } else {
            log.info("Table doc_archive already exists, skipping creation");
        }
    }

    private void initDocBorrowTable() throws Exception {
        if (!tableExists("doc_borrow")) {
            log.info("Creating doc_borrow table...");
            executeSqlFromResource("db/changelog/007_add_archive_and_borrow.sql");
        } else {
            log.info("Table doc_borrow already exists, skipping creation");
        }
    }

    private void initDocIncomingTable() throws Exception {
        if (!tableExists("doc_incoming")) {
            log.info("Creating doc_incoming table...");
            executeSqlFromResource("db/changelog/008_add_incoming_and_handling.sql");
        } else {
            log.info("Table doc_incoming already exists, skipping creation");
        }
    }

    private void initDocHandlingTable() throws Exception {
        if (!tableExists("doc_handling")) {
            log.info("Creating doc_handling table...");
            executeSqlFromResource("db/changelog/008_add_incoming_and_handling.sql");
        } else {
            log.info("Table doc_handling already exists, skipping creation");
        }
    }

    private void initDocPermissionTable() throws Exception {
        if (!tableExists("doc_permission")) {
            log.info("Creating doc_permission table...");
            executeSqlFromResource("db/changelog/010_add_security_tables.sql");
        } else {
            log.info("Table doc_permission already exists, skipping creation");
        }
    }

    private void initDocAuditLogTable() throws Exception {
        if (!tableExists("doc_audit_log")) {
            log.info("Creating doc_audit_log table...");
            executeSqlFromResource("db/changelog/010_add_security_tables.sql");
        } else {
            log.info("Table doc_audit_log already exists, skipping creation");
        }
    }

    private void initDocIntegrityTable() throws Exception {
        if (!tableExists("doc_integrity")) {
            log.info("Creating doc_integrity table...");
            executeSqlFromResource("db/changelog/010_add_security_tables.sql");
        } else {
            log.info("Table doc_integrity already exists, skipping creation");
        }
    }

    private void initSysUnitTable() throws Exception {
        if (!tableExists("sys_unit")) {
            log.info("Creating sys_unit table...");
            executeSqlFromResource("db/changelog/011_add_org_tables.sql");
        } else {
            log.info("Table sys_unit already exists, skipping creation");
        }
    }

    private void initSysDeptTable() throws Exception {
        if (!tableExists("sys_dept")) {
            log.info("Creating sys_dept table...");
            executeSqlFromResource("db/changelog/011_add_org_tables.sql");
        } else {
            log.info("Table sys_dept already exists, skipping creation");
        }
    }

    private void initSysPostTable() throws Exception {
        if (!tableExists("sys_post")) {
            log.info("Creating sys_post table...");
            executeSqlFromResource("db/changelog/011_add_org_tables.sql");
        } else {
            log.info("Table sys_post already exists, skipping creation");
        }
    }

    private void initSysUserTable() throws Exception {
        if (!tableExists("sys_user")) {
            log.info("Creating sys_user table...");
            executeSqlFromResource("db/changelog/011_add_org_tables.sql");
        } else {
            log.info("Table sys_user already exists, skipping creation");
        }
    }

    private void initDeletedColumns() throws Exception {
        log.info("Fixing deleted columns for existing tables...");
        executeSqlFromResource("db/changelog/012_fix_deleted_columns.sql");
    }

    private void initDocSupervisionTable() throws Exception {
        if (!tableExists("doc_supervision")) {
            log.info("Creating doc_supervision table...");
            executeSqlFromResource("db/changelog/013_add_supervision_tables.sql");
        } else {
            log.info("Table doc_supervision already exists, skipping creation");
        }
    }

    private void initDocUrgeLogTable() throws Exception {
        if (!tableExists("doc_urge_log")) {
            log.info("Creating doc_urge_log table...");
            executeSqlFromResource("db/changelog/013_add_supervision_tables.sql");
        } else {
            log.info("Table doc_urge_log already exists, skipping creation");
        }
    }

    private void initStatEfficiencyTable() throws Exception {
        if (!tableExists("stat_efficiency")) {
            log.info("Creating stat_efficiency table...");
            executeSqlFromResource("db/changelog/014_add_efficiency_table.sql");
        } else {
            log.info("Table stat_efficiency already exists, skipping creation");
        }
    }

    private void initDocAnalysisTable() throws Exception {
        if (!tableExists("doc_analysis")) {
            log.info("Creating doc_analysis table...");
            executeSqlFromResource("db/changelog/015_add_analysis_tables.sql");
        } else {
            log.info("Table doc_analysis already exists, skipping creation");
        }
    }

    private void initReminderLogTable() throws Exception {
        if (!tableExists("reminder_log")) {
            log.info("Creating reminder_log table...");
            executeSqlFromResource("db/changelog/015_add_analysis_tables.sql");
        } else {
            log.info("Table reminder_log already exists, skipping creation");
        }
    }

    private void initRejectReasonTable() throws Exception {
        if (!tableExists("reject_reason")) {
            log.info("Creating reject_reason table...");
            executeSqlFromResource("db/changelog/015_add_analysis_tables.sql");
        } else {
            log.info("Table reject_reason already exists, skipping creation");
        }
    }

    private void initRecommendTestData() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM doc_incoming WHERE id BETWEEN 2001 AND 2011", Integer.class);
            if (count != null && count > 0) {
                log.info("Recommend test data already exists, skipping");
                return;
            }
            log.info("Inserting recommend test data...");

            jdbcTemplate.update("INSERT IGNORE INTO sys_dept (id, unit_id, dept_code, dept_name, parent_id, dept_type, leader, phone, email, sort_order, status, create_by, create_time, update_by, update_time, deleted) VALUES (1001, 1, 'DEPT01', '综合办公室', 0, 'admin', '张三', '010-12345001', 'office@gov.cn', 1, 1, 'system', NOW(), 'system', NOW(), 0)");
            jdbcTemplate.update("INSERT IGNORE INTO sys_dept (id, unit_id, dept_code, dept_name, parent_id, dept_type, leader, phone, email, sort_order, status, create_by, create_time, update_by, update_time, deleted) VALUES (1002, 1, 'DEPT02', '财政审计科', 0, 'business', '李四', '010-12345002', 'finance@gov.cn', 2, 1, 'system', NOW(), 'system', NOW(), 0)");
            jdbcTemplate.update("INSERT IGNORE INTO sys_dept (id, unit_id, dept_code, dept_name, parent_id, dept_type, leader, phone, email, sort_order, status, create_by, create_time, update_by, update_time, deleted) VALUES (1003, 1, 'DEPT03', '政策法规科', 0, 'business', '王五', '010-12345003', 'policy@gov.cn', 3, 1, 'system', NOW(), 'system', NOW(), 0)");
            jdbcTemplate.update("INSERT IGNORE INTO sys_dept (id, unit_id, dept_code, dept_name, parent_id, dept_type, leader, phone, email, sort_order, status, create_by, create_time, update_by, update_time, deleted) VALUES (1004, 1, 'DEPT04', '人事教育科', 0, 'business', '赵六', '010-12345004', 'hr@gov.cn', 4, 1, 'system', NOW(), 'system', NOW(), 0)");
            jdbcTemplate.update("INSERT IGNORE INTO sys_dept (id, unit_id, dept_code, dept_name, parent_id, dept_type, leader, phone, email, sort_order, status, create_by, create_time, update_by, update_time, deleted) VALUES (1005, 1, 'DEPT05', '信息技术科', 0, 'business', '钱七', '010-12345005', 'it@gov.cn', 5, 1, 'system', NOW(), 'system', NOW(), 0)");

            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2001, 'IC20250101001', 'manual', '国务院', '国发〔2025〕1号', '关于加强财政预算管理工作的通知', '通知', '普通', '普通', '2025-01-10', 'mail', 5, 8, '财政,预算,管理', 'completed', '1', '登记员A', '1001', '综合办公室', '1', '2025-01-10 09:00:00', '1', '2025-01-10 09:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2002, 'IC20250102001', 'manual', '财政部', '财预〔2025〕15号', '关于开展年度财政审计检查的通知', '通知', '普通', '加急', '2025-01-15', 'courier', 3, 12, '财政,审计,检查', 'completed', '1', '登记员A', '1001', '综合办公室', '1', '2025-01-15 10:30:00', '1', '2025-01-15 10:30:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2003, 'IC20250201001', 'manual', '省政府', '赣府发〔2025〕8号', '关于加强财政资金监管的通知', '通知', '秘密', '普通', '2025-02-05', 'email', 2, 6, '财政,资金,监管', 'completed', '1', '登记员A', '1001', '综合办公室', '1', '2025-02-05 14:00:00', '1', '2025-02-05 14:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2004, 'IC20250301001', 'manual', '国务院', '国发〔2025〕12号', '关于深化预算管理制度改革的批复', '批复', '普通', '普通', '2025-03-01', 'mail', 1, 4, '预算,管理,改革', 'completed', '1', '登记员A', '1001', '综合办公室', '1', '2025-03-01 09:00:00', '1', '2025-03-01 09:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2005, 'IC20250315001', 'manual', '审计署', '审办发〔2025〕22号', '关于开展财政专项审计的通知', '通知', '普通', '普通', '2025-03-15', 'courier', 4, 15, '财政,审计,专项', 'completed', '1', '登记员A', '1001', '综合办公室', '1', '2025-03-15 11:00:00', '1', '2025-03-15 11:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2006, 'IC20250401001', 'manual', '省政府', '赣府函〔2025〕3号', '关于调整财政管理体制的函', '函', '普通', '加急', '2025-04-01', 'email', 2, 3, '财政,体制,调整', 'completed', '1', '登记员A', '1001', '综合办公室', '1', '2025-04-01 08:30:00', '1', '2025-04-01 08:30:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2007, 'IC20250415001', 'manual', '财政部', '财办〔2025〕30号', '关于加强预算执行管理的通知', '通知', '普通', '普通', '2025-04-15', 'mail', 3, 7, '预算,执行,管理', 'completed', '1', '登记员A', '1001', '综合办公室', '1', '2025-04-15 09:30:00', '1', '2025-04-15 09:30:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2008, 'IC20250501001', 'manual', '省政府', '赣府发〔2025〕18号', '关于做好财政决算工作的通知', '通知', '普通', '普通', '2025-05-01', 'courier', 6, 10, '财政,决算', 'completed', '1', '登记员A', '1001', '综合办公室', '1', '2025-05-01 10:00:00', '1', '2025-05-01 10:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2009, 'IC20250515001', 'manual', '审计署', '审发〔2025〕11号', '关于开展经济责任审计的请示', '请示', '秘密', '普通', '2025-05-15', 'mail', 2, 8, '经济责任,审计', 'completed', '1', '登记员A', '1001', '综合办公室', '1', '2025-05-15 14:00:00', '1', '2025-05-15 14:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2010, 'IC20250601001', 'manual', '省政府', '赣府发〔2025〕25号', '关于推进财政信息化建设的通知', '通知', '普通', '加急', '2025-06-01', 'email', 3, 5, '财政,信息化,建设', 'completed', '1', '登记员A', '1001', '综合办公室', '1', '2025-06-01 09:00:00', '1', '2025-06-01 09:00:00', 0)");

            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3001, 2001, 'HD20250110001', 'draft_opinion', '拟请财政审计科办理', '1', '办公室主任', '1001', '综合办公室', NULL, NULL, NULL, NULL, '2025-02-10', '2025-01-10 10:00:00', NULL, NULL, 'completed', '1', '2025-01-10 10:00:00', '1', '2025-01-10 10:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3002, 2001, 'HD20250110002', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1002', '财政审计科', '10', '李审计', '2025-02-10', '2025-01-10 10:30:00', '已按要求完成预算审核', '2025-02-05 16:00:00', 'completed', '1', '2025-01-10 10:30:00', '1', '2025-02-05 16:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3003, 2002, 'HD20250115001', 'draft_opinion', '拟请财政审计科牵头检查', '1', '办公室主任', '1001', '综合办公室', NULL, NULL, NULL, NULL, '2025-02-28', '2025-01-15 11:00:00', NULL, NULL, 'completed', '1', '2025-01-15 11:00:00', '1', '2025-01-15 11:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3004, 2002, 'HD20250115002', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1002', '财政审计科', '10', '李审计', '2025-02-28', '2025-01-15 11:30:00', '审计检查已完成', '2025-02-20 15:00:00', 'completed', '1', '2025-01-15 11:30:00', '1', '2025-02-20 15:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3005, 2003, 'HD20250205001', 'draft_opinion', '建议财政审计科与政策法规科会办', '1', '办公室主任', '1001', '综合办公室', NULL, NULL, NULL, NULL, '2025-03-05', '2025-02-05 15:00:00', NULL, NULL, 'completed', '1', '2025-02-05 15:00:00', '1', '2025-02-05 15:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3006, 2003, 'HD20250205002', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1002', '财政审计科', '10', '李审计', '2025-03-05', '2025-02-05 15:30:00', '资金监管方案已拟定', '2025-03-01 10:00:00', 'completed', '1', '2025-02-05 15:30:00', '1', '2025-03-01 10:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3007, 2003, 'HD20250205003', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1003', '政策法规科', '20', '王法规', '2025-03-05', '2025-02-05 15:30:00', '法规合规性审查已完成', '2025-03-02 14:00:00', 'completed', '1', '2025-02-05 15:30:00', '1', '2025-03-02 14:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3008, 2004, 'HD20250301001', 'draft_opinion', '拟请政策法规科研究', '1', '办公室主任', '1001', '综合办公室', NULL, NULL, NULL, NULL, '2025-04-01', '2025-03-01 10:00:00', NULL, NULL, 'completed', '1', '2025-03-01 10:00:00', '1', '2025-03-01 10:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3009, 2004, 'HD20250301002', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1003', '政策法规科', '20', '王法规', '2025-04-01', '2025-03-01 10:30:00', '改革方案已研究完成', '2025-03-25 16:00:00', 'completed', '1', '2025-03-01 10:30:00', '1', '2025-03-25 16:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3010, 2005, 'HD20250315001', 'draft_opinion', '建议财政审计科办理专项审计', '1', '办公室主任', '1001', '综合办公室', NULL, NULL, NULL, NULL, '2025-04-15', '2025-03-15 12:00:00', NULL, NULL, 'completed', '1', '2025-03-15 12:00:00', '1', '2025-03-15 12:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3011, 2005, 'HD20250315002', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1002', '财政审计科', '10', '李审计', '2025-04-15', '2025-03-15 12:30:00', '专项审计报告已提交', '2025-04-10 17:00:00', 'completed', '1', '2025-03-15 12:30:00', '1', '2025-04-10 17:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3012, 2006, 'HD20250401001', 'draft_opinion', '拟请政策法规科研究调整方案', '1', '办公室主任', '1001', '综合办公室', NULL, NULL, NULL, NULL, '2025-05-01', '2025-04-01 09:00:00', NULL, NULL, 'completed', '1', '2025-04-01 09:00:00', '1', '2025-04-01 09:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3013, 2006, 'HD20250401002', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1003', '政策法规科', '20', '王法规', '2025-05-01', '2025-04-01 09:30:00', '体制调整方案已拟定', '2025-04-25 11:00:00', 'completed', '1', '2025-04-01 09:30:00', '1', '2025-04-25 11:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3014, 2007, 'HD20250415001', 'draft_opinion', '拟请财政审计科办理', '1', '办公室主任', '1001', '综合办公室', NULL, NULL, NULL, NULL, '2025-05-15', '2025-04-15 10:00:00', NULL, NULL, 'completed', '1', '2025-04-15 10:00:00', '1', '2025-04-15 10:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3015, 2007, 'HD20250415002', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1002', '财政审计科', '10', '李审计', '2025-05-15', '2025-04-15 10:30:00', '预算执行情况已汇总', '2025-05-10 15:00:00', 'completed', '1', '2025-04-15 10:30:00', '1', '2025-05-10 15:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3016, 2008, 'HD20250501001', 'draft_opinion', '拟请财政审计科办理决算', '1', '办公室主任', '1001', '综合办公室', NULL, NULL, NULL, NULL, '2025-06-01', '2025-05-01 11:00:00', NULL, NULL, 'completed', '1', '2025-05-01 11:00:00', '1', '2025-05-01 11:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3017, 2008, 'HD20250501002', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1002', '财政审计科', '10', '李审计', '2025-06-01', '2025-05-01 11:30:00', '决算已完成', '2025-05-28 16:00:00', 'completed', '1', '2025-05-01 11:30:00', '1', '2025-05-28 16:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3018, 2009, 'HD20250515001', 'draft_opinion', '拟请财政审计科办理', '1', '办公室主任', '1001', '综合办公室', NULL, NULL, NULL, NULL, '2025-06-15', '2025-05-15 15:00:00', NULL, NULL, 'completed', '1', '2025-05-15 15:00:00', '1', '2025-05-15 15:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3019, 2009, 'HD20250515002', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1002', '财政审计科', '10', '李审计', '2025-06-15', '2025-05-15 15:30:00', NULL, NULL, 'pending', '1', '2025-05-15 15:30:00', '1', '2025-05-15 15:30:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3020, 2010, 'HD20250601001', 'draft_opinion', '拟请信息技术科办理', '1', '办公室主任', '1001', '综合办公室', NULL, NULL, NULL, NULL, '2025-07-01', '2025-06-01 10:00:00', NULL, NULL, 'completed', '1', '2025-06-01 10:00:00', '1', '2025-06-01 10:00:00', 0)");
            jdbcTemplate.update("INSERT INTO doc_handling (id, incoming_id, handling_no, handling_type, opinion, handler_id, handler_name, handler_dept_id, handler_dept_name, target_dept_id, target_dept_name, target_user_id, target_user_name, deadline, handling_time, feedback_content, feedback_time, status, create_by, create_time, update_by, update_time, deleted) VALUES (3021, 2010, 'HD20250601002', 'assign', NULL, '1', '办公室主任', '1001', '综合办公室', '1005', '信息技术科', '30', '钱工程师', '2025-07-01', '2025-06-01 10:30:00', NULL, NULL, 'pending', '1', '2025-06-01 10:30:00', '1', '2025-06-01 10:30:00', 0)");

            jdbcTemplate.update("INSERT INTO doc_incoming (id, incoming_no, source, source_unit, source_doc_number, doc_title, doc_type, security_level, urgency_level, received_date, received_method, copies, pages, keyword, status, registrant_id, registrant_name, registrant_dept_id, registrant_dept_name, create_by, create_time, update_by, update_time, deleted) VALUES (2011, 'IC20260610001', 'manual', '财政部', '财预〔2026〕20号', '关于加强财政预算绩效管理的通知', '通知', '普通', '普通', '2026-06-10', 'mail', 3, 9, '财政,预算,绩效', 'registered', '1', '登记员A', '1001', '综合办公室', '1', '2026-06-10 09:00:00', '1', '2026-06-10 09:00:00', 0)");

            log.info("Recommend test data inserted successfully");
        } catch (Exception e) {
            log.warn("Failed to insert recommend test data: {}", e.getMessage());
        }
    }
}
