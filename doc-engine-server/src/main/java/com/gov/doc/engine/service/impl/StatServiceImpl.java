package com.gov.doc.engine.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.dto.StatQueryDTO;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.mapper.DocDocumentMapper;
import com.gov.doc.engine.mapper.WfProcessDefinitionMapper;
import com.gov.doc.engine.mapper.WfProcessInstanceMapper;
import com.gov.doc.engine.service.StatService;
import com.gov.doc.engine.vo.StatCountersignCycleVO;
import com.gov.doc.engine.vo.StatDeptDraftVO;
import com.gov.doc.engine.vo.StatDocStatusVO;
import com.gov.doc.engine.vo.StatDocTypeVO;
import com.gov.doc.engine.vo.StatNodeDwellVO;
import com.gov.doc.engine.vo.StatOverviewVO;
import com.gov.doc.engine.vo.StatProcessVO;
import com.gov.doc.engine.vo.StatTimelinessTrendVO;
import com.gov.doc.engine.vo.StatTrendVO;
import com.gov.doc.engine.vo.StatUnitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class StatServiceImpl extends ServiceImpl<DocDocumentMapper, DocDocument> implements StatService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DocDocumentMapper docDocumentMapper;

    @Autowired
    private WfProcessInstanceMapper wfProcessInstanceMapper;

    @Autowired
    private WfProcessDefinitionMapper wfProcessDefinitionMapper;

    private static final Map<String, String> DOC_TYPE_NAME_MAP = new HashMap<>();
    private static final Map<String, String> DOC_STATUS_NAME_MAP = new HashMap<>();
    private static final Map<String, String> PROCESS_STATUS_NAME_MAP = new HashMap<>();

    static {
        DOC_TYPE_NAME_MAP.put("上行文", "上行文");
        DOC_TYPE_NAME_MAP.put("下行文", "下行文");
        DOC_TYPE_NAME_MAP.put("平行文", "平行文");
        DOC_TYPE_NAME_MAP.put("内部文", "内部文");

        DOC_STATUS_NAME_MAP.put("draft", "起草");
        DOC_STATUS_NAME_MAP.put("reviewing", "审核中");
        DOC_STATUS_NAME_MAP.put("countersigning", "会签中");
        DOC_STATUS_NAME_MAP.put("pending_sign", "待签发");
        DOC_STATUS_NAME_MAP.put("signed", "已签发");
        DOC_STATUS_NAME_MAP.put("distributing", "分发中");
        DOC_STATUS_NAME_MAP.put("archived", "归档");
        DOC_STATUS_NAME_MAP.put("abolished", "废止");

        PROCESS_STATUS_NAME_MAP.put("running", "运行中");
        PROCESS_STATUS_NAME_MAP.put("completed", "已完成");
        PROCESS_STATUS_NAME_MAP.put("suspended", "已挂起");
        PROCESS_STATUS_NAME_MAP.put("terminated", "已终止");
    }

    private String buildDocWhereClause(StatQueryDTO queryDTO, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE deleted = 0 ");
        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            where.append(" AND create_time >= ? ");
            params.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            where.append(" AND create_time <= ? ");
            params.add(queryDTO.getEndDate() + " 23:59:59");
        }
        if (StrUtil.isNotBlank(queryDTO.getUnitCode())) {
            where.append(" AND unit_code = ? ");
            params.add(queryDTO.getUnitCode());
        }
        if (StrUtil.isNotBlank(queryDTO.getDocType())) {
            where.append(" AND doc_type = ? ");
            params.add(queryDTO.getDocType());
        }
        return where.toString();
    }

    private String buildProcessWhereClause(StatQueryDTO queryDTO, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE pi.deleted = 0 AND pd.deleted = 0 ");
        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            where.append(" AND pi.create_time >= ? ");
            params.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            where.append(" AND pi.create_time <= ? ");
            params.add(queryDTO.getEndDate() + " 23:59:59");
        }
        if (StrUtil.isNotBlank(queryDTO.getUnitCode())) {
            where.append(" AND pd.unit_code = ? ");
            params.add(queryDTO.getUnitCode());
        }
        if (StrUtil.isNotBlank(queryDTO.getProcessType())) {
            where.append(" AND pd.process_type = ? ");
            params.add(queryDTO.getProcessType());
        }
        return where.toString();
    }

    private String formatDuration(Long milliseconds) {
        if (milliseconds == null || milliseconds <= 0) {
            return "0分钟";
        }
        long days = milliseconds / (24 * 60 * 60 * 1000);
        long hours = (milliseconds % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (milliseconds % (60 * 60 * 1000)) / (60 * 1000);
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        if (sb.length() == 0) {
            sb.append("0分钟");
        }
        return sb.toString();
    }

    @Override
    public StatOverviewVO getOverview(StatQueryDTO queryDTO) {
        StatOverviewVO vo = new StatOverviewVO();
        List<Object> params = new ArrayList<>();
        String where = buildDocWhereClause(queryDTO, params);

        String statusSql = "SELECT status, COUNT(*) as cnt FROM doc_document " + where + " GROUP BY status";
        List<Map<String, Object>> statusRows = jdbcTemplate.queryForList(statusSql, params.toArray());

        long totalCount = 0;
        long draftCount = 0;
        long reviewingCount = 0;
        long signedCount = 0;
        long archivedCount = 0;
        long abolishedCount = 0;

        for (Map<String, Object> row : statusRows) {
            String status = (String) row.get("status");
            long cnt = ((Number) row.get("cnt")).longValue();
            totalCount += cnt;
            if ("draft".equals(status)) draftCount = cnt;
            else if ("reviewing".equals(status) || "countersigning".equals(status) || "pending_sign".equals(status)) reviewingCount += cnt;
            else if ("signed".equals(status)) signedCount = cnt;
            else if ("archived".equals(status)) archivedCount = cnt;
            else if ("abolished".equals(status)) abolishedCount = cnt;
        }

        vo.setTotalDocCount(totalCount);
        vo.setDraftCount(draftCount);
        vo.setReviewingCount(reviewingCount);
        vo.setSignedCount(signedCount);
        vo.setArchivedCount(archivedCount);
        vo.setAbolishedCount(abolishedCount);

        if (totalCount > 0) {
            vo.setCompletionRate(Math.round((archivedCount + signedCount) * 10000.0 / totalCount) / 100.0);
        } else {
            vo.setCompletionRate(0.0);
        }

        List<Object> durationParams = new ArrayList<>();
        StringBuilder durationWhere = new StringBuilder(" WHERE pi.deleted = 0 AND pi.status = 'completed' AND pi.duration IS NOT NULL ");
        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            durationWhere.append(" AND pi.end_time >= ? ");
            durationParams.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            durationWhere.append(" AND pi.end_time <= ? ");
            durationParams.add(queryDTO.getEndDate() + " 23:59:59");
        }
        String avgDurationSql = "SELECT AVG(pi.duration) as avg_duration FROM wf_process_instance pi " + durationWhere;
        List<Map<String, Object>> durationRows = jdbcTemplate.queryForList(avgDurationSql, durationParams.toArray());
        if (!durationRows.isEmpty() && durationRows.get(0).get("avg_duration") != null) {
            double avgDurationMs = ((Number) durationRows.get(0).get("avg_duration")).doubleValue();
            long avgMinutes = (long) (avgDurationMs / (60 * 1000));
            vo.setAvgDurationMinutes(avgMinutes);
            vo.setAvgDurationText(formatDuration((long) avgDurationMs));
        } else {
            vo.setAvgDurationMinutes(0L);
            vo.setAvgDurationText("0分钟");
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate monthStart = today.withDayOfMonth(1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Object> todayParams = new ArrayList<>(params);
        String todaySql = "SELECT COUNT(*) as cnt FROM doc_document WHERE deleted = 0 AND DATE(create_time) = ?";
        todayParams.clear();
        todayParams.add(today.format(fmt));
        List<Map<String, Object>> todayRows = jdbcTemplate.queryForList(todaySql, todayParams.toArray());
        vo.setTodayCount(todayRows.isEmpty() ? 0L : ((Number) todayRows.get(0).get("cnt")).longValue());

        List<Object> weekParams = new ArrayList<>();
        String weekSql = "SELECT COUNT(*) as cnt FROM doc_document WHERE deleted = 0 AND DATE(create_time) >= ? AND DATE(create_time) <= ?";
        weekParams.add(weekStart.format(fmt));
        weekParams.add(today.format(fmt));
        List<Map<String, Object>> weekRows = jdbcTemplate.queryForList(weekSql, weekParams.toArray());
        vo.setWeekCount(weekRows.isEmpty() ? 0L : ((Number) weekRows.get(0).get("cnt")).longValue());

        List<Object> monthParams = new ArrayList<>();
        String monthSql = "SELECT COUNT(*) as cnt FROM doc_document WHERE deleted = 0 AND DATE(create_time) >= ? AND DATE(create_time) <= ?";
        monthParams.add(monthStart.format(fmt));
        monthParams.add(today.format(fmt));
        List<Map<String, Object>> monthRows = jdbcTemplate.queryForList(monthSql, monthParams.toArray());
        vo.setMonthCount(monthRows.isEmpty() ? 0L : ((Number) monthRows.get(0).get("cnt")).longValue());

        return vo;
    }

    @Override
    public List<StatDocTypeVO> getDocTypeStats(StatQueryDTO queryDTO) {
        List<StatDocTypeVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = buildDocWhereClause(queryDTO, params);

        String sql = "SELECT doc_type, COUNT(*) as cnt FROM doc_document " + where + " GROUP BY doc_type ORDER BY cnt DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        long total = 0;
        for (Map<String, Object> row : rows) {
            total += ((Number) row.get("cnt")).longValue();
        }

        for (Map<String, Object> row : rows) {
            StatDocTypeVO vo = new StatDocTypeVO();
            String docType = (String) row.get("doc_type");
            long cnt = ((Number) row.get("cnt")).longValue();
            vo.setDocType(docType);
            vo.setDocTypeName(DOC_TYPE_NAME_MAP.getOrDefault(docType, docType));
            vo.setCount(cnt);
            if (total > 0) {
                vo.setPercentage(Math.round(cnt * 10000.0 / total) / 100.0);
            } else {
                vo.setPercentage(0.0);
            }
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<StatDocStatusVO> getDocStatusStats(StatQueryDTO queryDTO) {
        List<StatDocStatusVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = buildDocWhereClause(queryDTO, params);

        String sql = "SELECT status, COUNT(*) as cnt FROM doc_document " + where + " GROUP BY status ORDER BY cnt DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        long total = 0;
        for (Map<String, Object> row : rows) {
            total += ((Number) row.get("cnt")).longValue();
        }

        for (Map<String, Object> row : rows) {
            StatDocStatusVO vo = new StatDocStatusVO();
            String status = (String) row.get("status");
            long cnt = ((Number) row.get("cnt")).longValue();
            vo.setStatus(status);
            vo.setStatusName(DOC_STATUS_NAME_MAP.getOrDefault(status, status));
            vo.setCount(cnt);
            if (total > 0) {
                vo.setPercentage(Math.round(cnt * 10000.0 / total) / 100.0);
            } else {
                vo.setPercentage(0.0);
            }
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<StatProcessVO> getProcessStats(StatQueryDTO queryDTO) {
        List<StatProcessVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = buildProcessWhereClause(queryDTO, params);

        String sql = "SELECT pd.process_name, " +
                "COUNT(pi.id) as total_count, " +
                "SUM(CASE WHEN pi.status = 'completed' THEN 1 ELSE 0 END) as completed_count, " +
                "SUM(CASE WHEN pi.status = 'running' THEN 1 ELSE 0 END) as running_count, " +
                "SUM(CASE WHEN pi.status = 'terminated' THEN 1 ELSE 0 END) as terminated_count, " +
                "AVG(CASE WHEN pi.status = 'completed' AND pi.duration IS NOT NULL THEN pi.duration ELSE NULL END) as avg_duration " +
                "FROM wf_process_instance pi " +
                "JOIN wf_process_definition pd ON pi.process_def_id = pd.id " +
                where +
                " GROUP BY pd.process_name ORDER BY total_count DESC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        for (Map<String, Object> row : rows) {
            StatProcessVO vo = new StatProcessVO();
            vo.setProcessName((String) row.get("process_name"));
            vo.setTotalCount(((Number) row.get("total_count")).longValue());
            vo.setCompletedCount(((Number) row.get("completed_count")).longValue());
            vo.setRunningCount(((Number) row.get("running_count")).longValue());
            vo.setTerminatedCount(((Number) row.get("terminated_count")).longValue());

            if (vo.getTotalCount() > 0) {
                vo.setCompletionRate(Math.round(vo.getCompletedCount() * 10000.0 / vo.getTotalCount()) / 100.0);
            } else {
                vo.setCompletionRate(0.0);
            }

            if (row.get("avg_duration") != null) {
                double avgDurationMs = ((Number) row.get("avg_duration")).doubleValue();
                vo.setAvgDurationMinutes((long) (avgDurationMs / (60 * 1000)));
                vo.setAvgDurationText(formatDuration((long) avgDurationMs));
            } else {
                vo.setAvgDurationMinutes(0L);
                vo.setAvgDurationText("0分钟");
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<StatTrendVO> getTrendStats(StatQueryDTO queryDTO) {
        List<StatTrendVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = buildDocWhereClause(queryDTO, params);

        String sql = "SELECT DATE(create_time) as date, COUNT(*) as doc_count " +
                "FROM doc_document " + where +
                " GROUP BY DATE(create_time) ORDER BY date ASC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        for (Map<String, Object> row : rows) {
            StatTrendVO vo = new StatTrendVO();
            vo.setDate(row.get("date").toString());
            vo.setDocCount(((Number) row.get("doc_count")).longValue());
            vo.setCompletedCount(0L);
            vo.setAvgDurationMinutes(0L);
            result.add(vo);
        }

        if (!result.isEmpty()) {
            String firstDate = result.get(0).getDate();
            String lastDate = result.get(result.size() - 1).getDate();

            List<Object> completedParams = new ArrayList<>();
            String completedSql = "SELECT DATE(end_time) as date, COUNT(*) as completed_count " +
                    "FROM wf_process_instance WHERE deleted = 0 AND status = 'completed' AND end_time IS NOT NULL " +
                    "AND DATE(end_time) >= ? AND DATE(end_time) <= ? " +
                    "GROUP BY DATE(end_time)";
            completedParams.add(firstDate);
            completedParams.add(lastDate);
            List<Map<String, Object>> completedRows = jdbcTemplate.queryForList(completedSql, completedParams.toArray());

            Map<String, Long> completedMap = new HashMap<>();
            for (Map<String, Object> row : completedRows) {
                completedMap.put(row.get("date").toString(), ((Number) row.get("completed_count")).longValue());
            }

            List<Object> durationParams = new ArrayList<>();
            String durationSql = "SELECT DATE(end_time) as date, AVG(duration) as avg_duration " +
                    "FROM wf_process_instance WHERE deleted = 0 AND status = 'completed' AND duration IS NOT NULL AND end_time IS NOT NULL " +
                    "AND DATE(end_time) >= ? AND DATE(end_time) <= ? " +
                    "GROUP BY DATE(end_time)";
            durationParams.add(firstDate);
            durationParams.add(lastDate);
            List<Map<String, Object>> durationRows = jdbcTemplate.queryForList(durationSql, durationParams.toArray());

            Map<String, Long> durationMap = new HashMap<>();
            for (Map<String, Object> row : durationRows) {
                if (row.get("avg_duration") != null) {
                    double avgMs = ((Number) row.get("avg_duration")).doubleValue();
                    durationMap.put(row.get("date").toString(), (long) (avgMs / (60 * 1000)));
                }
            }

            for (StatTrendVO vo : result) {
                vo.setCompletedCount(completedMap.getOrDefault(vo.getDate(), 0L));
                vo.setAvgDurationMinutes(durationMap.getOrDefault(vo.getDate(), 0L));
            }
        }

        return result;
    }

    @Override
    public List<StatUnitVO> getUnitStats(StatQueryDTO queryDTO) {
        List<StatUnitVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = buildDocWhereClause(queryDTO, params);

        String sql = "SELECT unit_code, unit_name, COUNT(*) as doc_count, " +
                "SUM(CASE WHEN status IN ('archived', 'signed') THEN 1 ELSE 0 END) as completed_count " +
                "FROM doc_document " + where +
                " GROUP BY unit_code, unit_name ORDER BY doc_count DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        for (Map<String, Object> row : rows) {
            StatUnitVO vo = new StatUnitVO();
            vo.setUnitCode((String) row.get("unit_code"));
            vo.setUnitName((String) row.get("unit_name"));
            vo.setDocCount(((Number) row.get("doc_count")).longValue());
            vo.setCompletedCount(((Number) row.get("completed_count")).longValue());

            if (vo.getDocCount() > 0) {
                vo.setCompletionRate(Math.round(vo.getCompletedCount() * 10000.0 / vo.getDocCount()) / 100.0);
            } else {
                vo.setCompletionRate(0.0);
            }

            List<Object> unitDurationParams = new ArrayList<>();
            StringBuilder unitDurationWhere = new StringBuilder(" WHERE pi.deleted = 0 AND pi.status = 'completed' AND pi.duration IS NOT NULL ");
            unitDurationWhere.append(" AND pi.start_dept_id IN (SELECT id FROM sys_dept WHERE unit_code = ?) ");
            unitDurationParams.add(vo.getUnitCode());
            if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
                unitDurationWhere.append(" AND pi.end_time >= ? ");
                unitDurationParams.add(queryDTO.getStartDate() + " 00:00:00");
            }
            if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
                unitDurationWhere.append(" AND pi.end_time <= ? ");
                unitDurationParams.add(queryDTO.getEndDate() + " 23:59:59");
            }
            String unitDurationSql = "SELECT AVG(pi.duration) as avg_duration FROM wf_process_instance pi " + unitDurationWhere;
            List<Map<String, Object>> unitDurationRows = jdbcTemplate.queryForList(unitDurationSql, unitDurationParams.toArray());
            if (!unitDurationRows.isEmpty() && unitDurationRows.get(0).get("avg_duration") != null) {
                double avgMs = ((Number) unitDurationRows.get(0).get("avg_duration")).doubleValue();
                vo.setAvgDurationMinutes((long) (avgMs / (60 * 1000)));
            } else {
                vo.setAvgDurationMinutes(0L);
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<StatDeptDraftVO> getDeptDraftStats(StatQueryDTO queryDTO) {
        List<StatDeptDraftVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE d.deleted = 0 AND d.status != 'draft' ");

        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            where.append(" AND d.create_time >= ? ");
            params.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            where.append(" AND d.create_time <= ? ");
            params.add(queryDTO.getEndDate() + " 23:59:59");
        }
        if (StrUtil.isNotBlank(queryDTO.getUnitCode())) {
            where.append(" AND d.unit_code = ? ");
            params.add(queryDTO.getUnitCode());
        }
        if (StrUtil.isNotBlank(queryDTO.getDocType())) {
            where.append(" AND d.doc_type = ? ");
            params.add(queryDTO.getDocType());
        }

        String sql = "SELECT d.creator_dept_id as dept_id, d.creator_dept_name as dept_name, " +
                "COUNT(d.id) as doc_count, " +
                "AVG(TIMESTAMPDIFF(MINUTE, d.create_time, h.first_enter_time)) as avg_draft_minutes, " +
                "MIN(TIMESTAMPDIFF(MINUTE, d.create_time, h.first_enter_time)) as min_draft_minutes, " +
                "MAX(TIMESTAMPDIFF(MINUTE, d.create_time, h.first_enter_time)) as max_draft_minutes " +
                "FROM doc_document d " +
                "LEFT JOIN ( " +
                "  SELECT pi.business_key, MIN(ph.enter_time) as first_enter_time " +
                "  FROM wf_process_history ph " +
                "  JOIN wf_process_instance pi ON ph.process_instance_id = pi.id AND pi.deleted = 0 " +
                "  WHERE ph.deleted = 0 AND ph.node_type = 'userTask' AND ph.enter_time IS NOT NULL " +
                "  GROUP BY pi.business_key " +
                ") h ON d.id = CAST(h.business_key AS UNSIGNED) " +
                where.toString() +
                " GROUP BY d.creator_dept_id, d.creator_dept_name " +
                " ORDER BY avg_draft_minutes DESC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        for (Map<String, Object> row : rows) {
            StatDeptDraftVO vo = new StatDeptDraftVO();
            vo.setDeptId(row.get("dept_id") != null ? row.get("dept_id").toString() : "");
            vo.setDeptName(row.get("dept_name") != null ? row.get("dept_name").toString() : "未知部门");
            vo.setDocCount(row.get("doc_count") != null ? ((Number) row.get("doc_count")).longValue() : 0L);
            vo.setAvgDraftMinutes(row.get("avg_draft_minutes") != null ? ((Number) row.get("avg_draft_minutes")).longValue() : 0L);
            vo.setAvgDraftText(formatDuration(vo.getAvgDraftMinutes() * 60 * 1000));
            vo.setMinDraftMinutes(row.get("min_draft_minutes") != null ? ((Number) row.get("min_draft_minutes")).longValue() : 0L);
            vo.setMinDraftText(formatDuration(vo.getMinDraftMinutes() * 60 * 1000));
            vo.setMaxDraftMinutes(row.get("max_draft_minutes") != null ? ((Number) row.get("max_draft_minutes")).longValue() : 0L);
            vo.setMaxDraftText(formatDuration(vo.getMaxDraftMinutes() * 60 * 1000));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<StatNodeDwellVO> getNodeDwellStats(StatQueryDTO queryDTO) {
        List<StatNodeDwellVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE ph.deleted = 0 AND ph.duration IS NOT NULL AND ph.duration > 0 ");

        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            where.append(" AND ph.enter_time >= ? ");
            params.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            where.append(" AND ph.leave_time <= ? ");
            params.add(queryDTO.getEndDate() + " 23:59:59");
        }
        if (StrUtil.isNotBlank(queryDTO.getProcessType())) {
            where.append(" AND pi.process_def_id IN (SELECT id FROM wf_process_definition WHERE process_type = ? AND deleted = 0) ");
            params.add(queryDTO.getProcessType());
        }

        String sql = "SELECT ph.node_id, ph.node_name, ph.node_type, " +
                "su.post_id, sp.post_name, " +
                "COUNT(ph.id) as task_count, " +
                "AVG(ph.duration) as avg_dwell, " +
                "MIN(ph.duration) as min_dwell, " +
                "MAX(ph.duration) as max_dwell " +
                "FROM wf_process_history ph " +
                "JOIN wf_process_instance pi ON ph.process_instance_id = pi.id AND pi.deleted = 0 " +
                "LEFT JOIN sys_user su ON ph.operator_id = su.user_code AND su.deleted = 0 " +
                "LEFT JOIN sys_post sp ON su.post_id = sp.id AND sp.deleted = 0 " +
                where.toString() +
                " GROUP BY ph.node_id, ph.node_name, ph.node_type, su.post_id, sp.post_name " +
                " ORDER BY avg_dwell DESC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        for (Map<String, Object> row : rows) {
            StatNodeDwellVO vo = new StatNodeDwellVO();
            vo.setNodeId(row.get("node_id") != null ? row.get("node_id").toString() : "");
            vo.setNodeName(row.get("node_name") != null ? row.get("node_name").toString() : "");
            vo.setNodeType(row.get("node_type") != null ? row.get("node_type").toString() : "");
            vo.setPostId(row.get("post_id") != null ? row.get("post_id").toString() : "");
            vo.setPostName(row.get("post_name") != null ? row.get("post_name").toString() : "未分配岗位");
            vo.setTaskCount(row.get("task_count") != null ? ((Number) row.get("task_count")).longValue() : 0L);

            long avgMs = row.get("avg_dwell") != null ? ((Number) row.get("avg_dwell")).longValue() : 0L;
            long minMs = row.get("min_dwell") != null ? ((Number) row.get("min_dwell")).longValue() : 0L;
            long maxMs = row.get("max_dwell") != null ? ((Number) row.get("max_dwell")).longValue() : 0L;

            vo.setAvgDwellMinutes(avgMs / (60 * 1000));
            vo.setAvgDwellText(formatDuration(avgMs));
            vo.setMinDwellMinutes(minMs / (60 * 1000));
            vo.setMinDwellText(formatDuration(minMs));
            vo.setMaxDwellMinutes(maxMs / (60 * 1000));
            vo.setMaxDwellText(formatDuration(maxMs));

            double withinRate = 0.0;
            long standardMinutes = 1440;
            if (vo.getAvgDwellMinutes() > 0 && vo.getAvgDwellMinutes() <= standardMinutes) {
                withinRate = Math.round(vo.getAvgDwellMinutes() * 10000.0 / standardMinutes) / 100.0;
            } else if (vo.getAvgDwellMinutes() > standardMinutes) {
                withinRate = 0.0;
            }
            vo.setWithinRate(withinRate);

            result.add(vo);
        }
        return result;
    }

    @Override
    public List<StatCountersignCycleVO> getCountersignCycleStats(StatQueryDTO queryDTO) {
        List<StatCountersignCycleVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE cs.deleted = 0 AND cs.status = 'completed' AND cs.start_time IS NOT NULL AND cs.end_time IS NOT NULL ");

        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            where.append(" AND cs.start_time >= ? ");
            params.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            where.append(" AND cs.end_time <= ? ");
            params.add(queryDTO.getEndDate() + " 23:59:59");
        }
        if (StrUtil.isNotBlank(queryDTO.getUnitCode())) {
            where.append(" AND d.unit_code = ? ");
            params.add(queryDTO.getUnitCode());
        }
        if (StrUtil.isNotBlank(queryDTO.getDocType())) {
            where.append(" AND d.doc_type = ? ");
            params.add(queryDTO.getDocType());
        }

        String sql = "SELECT d.doc_type, " +
                "COUNT(cs.id) as countersign_count, " +
                "AVG(TIMESTAMPDIFF(MINUTE, cs.start_time, cs.end_time)) as avg_cycle_minutes, " +
                "MIN(TIMESTAMPDIFF(MINUTE, cs.start_time, cs.end_time)) as min_cycle_minutes, " +
                "MAX(TIMESTAMPDIFF(MINUTE, cs.start_time, cs.end_time)) as max_cycle_minutes " +
                "FROM wf_countersign cs " +
                "LEFT JOIN doc_document d ON cs.business_key = CAST(d.id AS CHAR) AND d.deleted = 0 " +
                where.toString() +
                " GROUP BY d.doc_type " +
                " ORDER BY avg_cycle_minutes DESC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        for (Map<String, Object> row : rows) {
            StatCountersignCycleVO vo = new StatCountersignCycleVO();
            String docType = row.get("doc_type") != null ? row.get("doc_type").toString() : "未知";
            vo.setDocType(docType);
            vo.setDocTypeName(DOC_TYPE_NAME_MAP.getOrDefault(docType, docType));
            vo.setCountersignCount(row.get("countersign_count") != null ? ((Number) row.get("countersign_count")).longValue() : 0L);
            vo.setAvgCycleMinutes(row.get("avg_cycle_minutes") != null ? ((Number) row.get("avg_cycle_minutes")).longValue() : 0L);
            vo.setAvgCycleText(formatDuration(vo.getAvgCycleMinutes() * 60 * 1000));
            vo.setMinCycleMinutes(row.get("min_cycle_minutes") != null ? ((Number) row.get("min_cycle_minutes")).longValue() : 0L);
            vo.setMinCycleText(formatDuration(vo.getMinCycleMinutes() * 60 * 1000));
            vo.setMaxCycleMinutes(row.get("max_cycle_minutes") != null ? ((Number) row.get("max_cycle_minutes")).longValue() : 0L);
            vo.setMaxCycleText(formatDuration(vo.getMaxCycleMinutes() * 60 * 1000));

            double withinRate = 0.0;
            long standardMinutes = 3 * 24 * 60;
            if (vo.getAvgCycleMinutes() > 0 && vo.getAvgCycleMinutes() <= standardMinutes) {
                withinRate = Math.round((standardMinutes - vo.getAvgCycleMinutes()) * 10000.0 / standardMinutes) / 100.0;
            }
            vo.setWithinRate(withinRate);

            result.add(vo);
        }
        return result;
    }

    @Override
    public List<StatTimelinessTrendVO> getTimelinessTrend(StatQueryDTO queryDTO) {
        List<StatTimelinessTrendVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE d.deleted = 0 ");

        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            where.append(" AND d.create_time >= ? ");
            params.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            where.append(" AND d.create_time <= ? ");
            params.add(queryDTO.getEndDate() + " 23:59:59");
        }
        if (StrUtil.isNotBlank(queryDTO.getUnitCode())) {
            where.append(" AND d.unit_code = ? ");
            params.add(queryDTO.getUnitCode());
        }
        if (StrUtil.isNotBlank(queryDTO.getDocType())) {
            where.append(" AND d.doc_type = ? ");
            params.add(queryDTO.getDocType());
        }

        String sql = "SELECT DATE(d.create_time) as date, " +
                "AVG(TIMESTAMPDIFF(MINUTE, d.create_time, h.first_enter_time)) as avg_draft_minutes, " +
                "SUM(CASE WHEN d.status IN ('archived', 'signed') THEN 1 ELSE 0 END) * 100.0 / COUNT(d.id) as completion_rate " +
                "FROM doc_document d " +
                "LEFT JOIN ( " +
                "  SELECT pi.business_key, MIN(ph.enter_time) as first_enter_time " +
                "  FROM wf_process_history ph " +
                "  JOIN wf_process_instance pi ON ph.process_instance_id = pi.id AND pi.deleted = 0 " +
                "  WHERE ph.deleted = 0 AND ph.node_type = 'userTask' AND ph.enter_time IS NOT NULL " +
                "  GROUP BY pi.business_key " +
                ") h ON d.id = CAST(h.business_key AS UNSIGNED) " +
                where.toString() +
                " GROUP BY DATE(d.create_time) " +
                " ORDER BY date ASC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        List<Object> dwellParams = new ArrayList<>();
        StringBuilder dwellWhere = new StringBuilder(" WHERE ph.deleted = 0 AND ph.duration IS NOT NULL AND ph.duration > 0 ");
        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            dwellWhere.append(" AND ph.enter_time >= ? ");
            dwellParams.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            dwellWhere.append(" AND ph.leave_time <= ? ");
            dwellParams.add(queryDTO.getEndDate() + " 23:59:59");
        }
        String dwellSql = "SELECT DATE(ph.enter_time) as date, AVG(ph.duration / (60 * 1000)) as avg_dwell_minutes " +
                "FROM wf_process_history ph " +
                dwellWhere.toString() +
                " GROUP BY DATE(ph.enter_time)";
        List<Map<String, Object>> dwellRows = jdbcTemplate.queryForList(dwellSql, dwellParams.toArray());
        Map<String, Long> dwellMap = new HashMap<>();
        for (Map<String, Object> row : dwellRows) {
            if (row.get("avg_dwell_minutes") != null) {
                dwellMap.put(row.get("date").toString(), ((Number) row.get("avg_dwell_minutes")).longValue());
            }
        }

        List<Object> csParams = new ArrayList<>();
        StringBuilder csWhere = new StringBuilder(" WHERE cs.deleted = 0 AND cs.status = 'completed' AND cs.start_time IS NOT NULL AND cs.end_time IS NOT NULL ");
        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            csWhere.append(" AND cs.start_time >= ? ");
            csParams.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            csWhere.append(" AND cs.end_time <= ? ");
            csParams.add(queryDTO.getEndDate() + " 23:59:59");
        }
        String csSql = "SELECT DATE(cs.start_time) as date, AVG(TIMESTAMPDIFF(MINUTE, cs.start_time, cs.end_time)) as avg_cs_minutes " +
                "FROM wf_countersign cs " +
                csWhere.toString() +
                " GROUP BY DATE(cs.start_time)";
        List<Map<String, Object>> csRows = jdbcTemplate.queryForList(csSql, csParams.toArray());
        Map<String, Long> csMap = new HashMap<>();
        for (Map<String, Object> row : csRows) {
            if (row.get("avg_cs_minutes") != null) {
                csMap.put(row.get("date").toString(), ((Number) row.get("avg_cs_minutes")).longValue());
            }
        }

        for (Map<String, Object> row : rows) {
            StatTimelinessTrendVO vo = new StatTimelinessTrendVO();
            vo.setDate(row.get("date").toString());
            vo.setAvgDraftMinutes(row.get("avg_draft_minutes") != null ? ((Number) row.get("avg_draft_minutes")).longValue() : 0L);
            vo.setAvgDwellMinutes(dwellMap.getOrDefault(vo.getDate(), 0L));
            vo.setAvgCountersignMinutes(csMap.getOrDefault(vo.getDate(), 0L));
            vo.setCompletionRate(row.get("completion_rate") != null ? Math.round(((Number) row.get("completion_rate")).doubleValue() * 100) / 100.0 : 0.0);
            result.add(vo);
        }

        return result;
    }
}
