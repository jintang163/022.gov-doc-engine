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
}
