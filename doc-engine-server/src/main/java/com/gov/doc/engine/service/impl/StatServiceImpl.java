package com.gov.doc.engine.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.dto.EfficiencyRankQueryDTO;
import com.gov.doc.engine.dto.StatQueryDTO;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.entity.StatEfficiency;
import com.gov.doc.engine.enums.EfficiencyRankTypeEnum;
import com.gov.doc.engine.mapper.DocDocumentMapper;
import com.gov.doc.engine.mapper.StatEfficiencyMapper;
import com.gov.doc.engine.mapper.WfProcessDefinitionMapper;
import com.gov.doc.engine.mapper.WfProcessInstanceMapper;
import com.gov.doc.engine.service.StatService;
import com.gov.doc.engine.vo.StatCountersignCycleVO;
import com.gov.doc.engine.vo.StatDeptDraftVO;
import com.gov.doc.engine.vo.StatDocStatusVO;
import com.gov.doc.engine.vo.StatDocTypeVO;
import com.gov.doc.engine.vo.StatEfficiencyVO;
import com.gov.doc.engine.vo.StatNodeDwellVO;
import com.gov.doc.engine.vo.StatOverviewVO;
import com.gov.doc.engine.vo.StatProcessVO;
import com.gov.doc.engine.vo.StatRejectionOverviewVO;
import com.gov.doc.engine.vo.StatRejectionReasonVO;
import com.gov.doc.engine.vo.StatRejectionWordVO;
import com.gov.doc.engine.vo.StatTimelinessTrendVO;
import com.gov.doc.engine.vo.StatTrendVO;
import com.gov.doc.engine.vo.StatUnitVO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private StatEfficiencyMapper statEfficiencyMapper;

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

        long standardSeconds = 1440 * 60;
        String sql = "SELECT ph.node_id, ph.node_name, ph.node_type, " +
                "su.post_id, sp.post_name, " +
                "COUNT(ph.id) as task_count, " +
                "SUM(CASE WHEN ph.duration <= ? THEN 1 ELSE 0 END) as within_count, " +
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

        params.add(0, standardSeconds);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        for (Map<String, Object> row : rows) {
            StatNodeDwellVO vo = new StatNodeDwellVO();
            vo.setNodeId(row.get("node_id") != null ? row.get("node_id").toString() : "");
            vo.setNodeName(row.get("node_name") != null ? row.get("node_name").toString() : "");
            vo.setNodeType(row.get("node_type") != null ? row.get("node_type").toString() : "");
            vo.setPostId(row.get("post_id") != null ? row.get("post_id").toString() : "");
            vo.setPostName(row.get("post_name") != null ? row.get("post_name").toString() : "未分配岗位");
            vo.setTaskCount(row.get("task_count") != null ? ((Number) row.get("task_count")).longValue() : 0L);

            long avgSeconds = row.get("avg_dwell") != null ? ((Number) row.get("avg_dwell")).longValue() : 0L;
            long minSeconds = row.get("min_dwell") != null ? ((Number) row.get("min_dwell")).longValue() : 0L;
            long maxSeconds = row.get("max_dwell") != null ? ((Number) row.get("max_dwell")).longValue() : 0L;
            long withinCount = row.get("within_count") != null ? ((Number) row.get("within_count")).longValue() : 0L;

            vo.setAvgDwellMinutes(avgSeconds / 60);
            vo.setAvgDwellText(formatDuration(avgSeconds * 1000));
            vo.setMinDwellMinutes(minSeconds / 60);
            vo.setMinDwellText(formatDuration(minSeconds * 1000));
            vo.setMaxDwellMinutes(maxSeconds / 60);
            vo.setMaxDwellText(formatDuration(maxSeconds * 1000));

            double withinRate = 0.0;
            if (vo.getTaskCount() > 0) {
                withinRate = Math.round(withinCount * 10000.0 / vo.getTaskCount()) / 100.0;
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

        long standardMinutes = 3 * 24 * 60;
        String sql = "SELECT d.doc_type, " +
                "COUNT(cs.id) as countersign_count, " +
                "SUM(CASE WHEN TIMESTAMPDIFF(MINUTE, cs.start_time, cs.end_time) <= ? THEN 1 ELSE 0 END) as within_count, " +
                "AVG(TIMESTAMPDIFF(MINUTE, cs.start_time, cs.end_time)) as avg_cycle_minutes, " +
                "MIN(TIMESTAMPDIFF(MINUTE, cs.start_time, cs.end_time)) as min_cycle_minutes, " +
                "MAX(TIMESTAMPDIFF(MINUTE, cs.start_time, cs.end_time)) as max_cycle_minutes " +
                "FROM wf_countersign cs " +
                "LEFT JOIN doc_document d ON cs.business_key = CAST(d.id AS CHAR) AND d.deleted = 0 " +
                where.toString() +
                " GROUP BY d.doc_type " +
                " ORDER BY avg_cycle_minutes DESC";

        params.add(0, standardMinutes);

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

            long withinCount = row.get("within_count") != null ? ((Number) row.get("within_count")).longValue() : 0L;
            double withinRate = 0.0;
            if (vo.getCountersignCount() > 0) {
                withinRate = Math.round(withinCount * 10000.0 / vo.getCountersignCount()) / 100.0;
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
        String dwellSql = "SELECT DATE(ph.enter_time) as date, AVG(ph.duration / 60) as avg_dwell_minutes " +
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

    private String buildRejectionWhereClause(StatQueryDTO queryDTO, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE ao.deleted = 0 AND ao.approval_type IN ('reject', 'return') AND ao.approval_opinion IS NOT NULL AND ao.approval_opinion != '' ");

        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            where.append(" AND ao.approval_time >= ? ");
            params.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            where.append(" AND ao.approval_time <= ? ");
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
        return where.toString();
    }

    @Override
    public StatRejectionOverviewVO getRejectionOverview(StatQueryDTO queryDTO) {
        StatRejectionOverviewVO vo = new StatRejectionOverviewVO();
        List<Object> params = new ArrayList<>();
        String where = buildRejectionWhereClause(queryDTO, params);

        String sql = "SELECT " +
                "SUM(CASE WHEN ao.approval_type = 'return' THEN 1 ELSE 0 END) as return_count, " +
                "SUM(CASE WHEN ao.approval_type = 'reject' THEN 1 ELSE 0 END) as reject_count, " +
                "COUNT(ao.id) as total_count " +
                "FROM wf_approval_opinion ao " +
                "LEFT JOIN doc_document d ON ao.business_key = CAST(d.id AS CHAR) AND d.deleted = 0 " +
                where;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        if (!rows.isEmpty()) {
            Map<String, Object> row = rows.get(0);
            vo.setTotalReturnCount(row.get("return_count") != null ? ((Number) row.get("return_count")).longValue() : 0L);
            vo.setTotalRejectionCount(row.get("reject_count") != null ? ((Number) row.get("reject_count")).longValue() : 0L);
            vo.setTotalCount(row.get("total_count") != null ? ((Number) row.get("total_count")).longValue() : 0L);
        }

        long totalTaskCount = 0;
        List<Object> totalParams = new ArrayList<>();
        StringBuilder totalWhere = new StringBuilder(" WHERE ao.deleted = 0 AND ao.approval_opinion IS NOT NULL ");
        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            totalWhere.append(" AND ao.approval_time >= ? ");
            totalParams.add(queryDTO.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            totalWhere.append(" AND ao.approval_time <= ? ");
            totalParams.add(queryDTO.getEndDate() + " 23:59:59");
        }
        String totalSql = "SELECT COUNT(ao.id) as cnt FROM wf_approval_opinion ao " +
                "LEFT JOIN doc_document d ON ao.business_key = CAST(d.id AS CHAR) AND d.deleted = 0 " +
                totalWhere;
        List<Map<String, Object>> totalRows = jdbcTemplate.queryForList(totalSql, totalParams.toArray());
        if (!totalRows.isEmpty() && totalRows.get(0).get("cnt") != null) {
            totalTaskCount = ((Number) totalRows.get(0).get("cnt")).longValue();
        }
        if (totalTaskCount > 0) {
            vo.setReturnRate(Math.round(vo.getTotalCount() * 10000.0 / totalTaskCount) / 100.0);
        } else {
            vo.setReturnRate(0.0);
        }

        return vo;
    }

    private static final List<String> STOP_WORDS = Arrays.asList(
            "的", "了", "和", "是", "在", "我", "有", "就", "不", "人", "都", "一", "一个",
            "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
            "自己", "这", "那", "他", "她", "它", "们", "什么", "怎么", "这个", "那个",
            "请", "把", "被", "给", "让", "对", "与", "及", "或", "但", "而", "如", "因",
            "为", "以", "于", "从", "向", "由", "等", "等等", "啊", "吧", "呢", "吗",
            "哦", "嗯", "哈", "呀", "可以", "已经", "可能", "应该", "需要", "必须",
            "是否", "不是", "不能", "不会", "没有", "只是", "还有", "以及", "但是",
            "如果", "虽然", "所以", "因此", "然后", "之后", "以前", "现在", "同时",
            "问题", "内容", "材料", "情况", "时候", "地方", "工作", "相关", "进行"
    );

    @Override
    public List<StatRejectionWordVO> getRejectionWordStats(StatQueryDTO queryDTO) {
        List<StatRejectionWordVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = buildRejectionWhereClause(queryDTO, params);

        String sql = "SELECT ao.approval_opinion as opinion FROM wf_approval_opinion ao " +
                "LEFT JOIN doc_document d ON ao.business_key = CAST(d.id AS CHAR) AND d.deleted = 0 " +
                where;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        Map<String, Long> wordCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String opinion = row.get("opinion") != null ? row.get("opinion").toString() : "";
            if (StrUtil.isBlank(opinion)) continue;

            String cleaned = opinion.replaceAll("[\\pP\\pS\\s\\d]+", " ");
            List<String> words = new ArrayList<>();

            for (int i = 0; i < cleaned.length() - 1; i++) {
                String bigram = cleaned.substring(i, i + 2).trim();
                if (bigram.length() == 2 && !STOP_WORDS.contains(bigram)
                        && !bigram.matches(".*\\d.*") && !bigram.matches(".*[a-zA-Z].*")) {
                    words.add(bigram);
                }
            }

            for (int i = 0; i < cleaned.length() - 2; i++) {
                String trigram = cleaned.substring(i, i + 3).trim();
                if (trigram.length() == 3 && !STOP_WORDS.contains(trigram)
                        && !trigram.matches(".*\\d.*") && !trigram.matches(".*[a-zA-Z].*")) {
                    words.add(trigram);
                }
            }

            for (int i = 0; i < cleaned.length() - 3; i++) {
                String fourgram = cleaned.substring(i, i + 4).trim();
                if (fourgram.length() == 4 && !STOP_WORDS.contains(fourgram)
                        && !fourgram.matches(".*\\d.*") && !fourgram.matches(".*[a-zA-Z].*")) {
                    words.add(fourgram);
                }
            }

            for (String w : words) {
                wordCountMap.merge(w, 1L, Long::sum);
            }
        }

        long maxCount = wordCountMap.values().stream().max(Long::compareTo).orElse(1L);

        List<Map.Entry<String, Long>> sortedEntries = wordCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(100)
                .collect(Collectors.toList());

        for (Map.Entry<String, Long> entry : sortedEntries) {
            StatRejectionWordVO vo = new StatRejectionWordVO();
            vo.setWord(entry.getKey());
            vo.setCount(entry.getValue());
            vo.setWeight(Math.round(entry.getValue() * 10000.0 / maxCount) / 100.0);
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<StatRejectionReasonVO> getRejectionReasonStats(StatQueryDTO queryDTO) {
        List<StatRejectionReasonVO> result = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = buildRejectionWhereClause(queryDTO, params);

        String sql = "SELECT ao.approver_dept_id as dept_id, ao.approver_dept_name as dept_name, ao.approval_opinion as opinion " +
                "FROM wf_approval_opinion ao " +
                "LEFT JOIN doc_document d ON ao.business_key = CAST(d.id AS CHAR) AND d.deleted = 0 " +
                where;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        Map<String, Long> reasonCountMap = new LinkedHashMap<>();
        Map<String, String> reasonDeptMap = new LinkedHashMap<>();
        Map<String, String> reasonDeptIdMap = new LinkedHashMap<>();

        List<String> reasonKeywords = Arrays.asList(
                "格式不规范", "格式错误", "格式问题", "格式不正确",
                "内容不全", "内容不完整", "内容缺失", "内容不完善",
                "表述不清", "表述不清晰", "描述不清", "描述不准确",
                "政策不符", "不符合政策", "违反规定", "不符合规定", "不符合要求",
                "附件缺失", "缺少附件", "附件不全",
                "签字不全", "缺少签字", "未签字", "未盖章", "缺少盖章",
                "日期错误", "时间不对",
                "文号错误", "字号错误",
                "逻辑错误", "逻辑混乱",
                "文字错误", "错别字", "用词不当",
                "材料不全", "材料不足", "材料不完整",
                "需补正", "需补充", "需修改", "请补充", "请修改", "请完善",
                "退回修改", "退回补正", "不予受理", "驳回申请"
        );

        for (Map<String, Object> row : rows) {
            String opinion = row.get("opinion") != null ? row.get("opinion").toString() : "";
            String deptId = row.get("dept_id") != null ? row.get("dept_id").toString() : "";
            String deptName = row.get("dept_name") != null ? row.get("dept_name").toString() : "";

            if (StrUtil.isBlank(opinion)) continue;

            boolean matched = false;
            for (String keyword : reasonKeywords) {
                if (opinion.contains(keyword)) {
                    reasonCountMap.merge(keyword, 1L, Long::sum);
                    reasonDeptMap.putIfAbsent(keyword, deptName);
                    reasonDeptIdMap.putIfAbsent(keyword, deptId);
                    matched = true;
                }
            }
            if (!matched) {
                String trimmed = opinion.length() > 15 ? opinion.substring(0, 15) + "..." : opinion;
                reasonCountMap.merge(trimmed, 1L, Long::sum);
                reasonDeptMap.putIfAbsent(trimmed, deptName);
                reasonDeptIdMap.putIfAbsent(trimmed, deptId);
            }
        }

        long totalCount = reasonCountMap.values().stream().mapToLong(Long::longValue).sum();

        List<Map.Entry<String, Long>> sortedEntries = reasonCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(50)
                .collect(Collectors.toList());

        for (Map.Entry<String, Long> entry : sortedEntries) {
            StatRejectionReasonVO vo = new StatRejectionReasonVO();
            vo.setReason(entry.getKey());
            vo.setCount(entry.getValue());
            vo.setDeptId(reasonDeptIdMap.getOrDefault(entry.getKey(), ""));
            vo.setDeptName(reasonDeptMap.getOrDefault(entry.getKey(), "未知部门"));
            if (totalCount > 0) {
                vo.setPercentage(Math.round(entry.getValue() * 10000.0 / totalCount) / 100.0);
            } else {
                vo.setPercentage(0.0);
            }
            result.add(vo);
        }

        result.sort(Comparator.comparing(StatRejectionReasonVO::getCount).reversed());
        return result;
    }

    @Override
    public Page<StatEfficiencyVO> getDeptEfficiencyRank(EfficiencyRankQueryDTO queryDTO) {
        String statMonth = resolveStatMonth(queryDTO.getStatMonth());
        if (!hasMonthData(statMonth, EfficiencyRankTypeEnum.DEPARTMENT)) {
            calculateEfficiencyForMonth(statMonth);
        }
        LambdaQueryWrapper<StatEfficiency> wrapper = buildEfficiencyWrapper(statMonth, EfficiencyRankTypeEnum.DEPARTMENT, queryDTO);
        Page<StatEfficiency> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<StatEfficiency> entityPage = statEfficiencyMapper.selectPage(page, wrapper);
        return convertToEfficiencyVOPage(entityPage);
    }

    @Override
    public Page<StatEfficiencyVO> getPersonEfficiencyRank(EfficiencyRankQueryDTO queryDTO) {
        String statMonth = resolveStatMonth(queryDTO.getStatMonth());
        if (!hasMonthData(statMonth, EfficiencyRankTypeEnum.PERSON)) {
            calculateEfficiencyForMonth(statMonth);
        }
        LambdaQueryWrapper<StatEfficiency> wrapper = buildEfficiencyWrapper(statMonth, EfficiencyRankTypeEnum.PERSON, queryDTO);
        Page<StatEfficiency> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<StatEfficiency> entityPage = statEfficiencyMapper.selectPage(page, wrapper);
        return convertToEfficiencyVOPage(entityPage);
    }

    @Override
    public void exportEfficiencyRank(EfficiencyRankQueryDTO queryDTO, String rankType, OutputStream outputStream) {
        EfficiencyRankTypeEnum rankEnum = EfficiencyRankTypeEnum.of(rankType);
        if (rankEnum == null) rankEnum = EfficiencyRankTypeEnum.DEPARTMENT;

        String statMonth = resolveStatMonth(queryDTO.getStatMonth());
        if (!hasMonthData(statMonth, rankEnum)) {
            calculateEfficiencyForMonth(statMonth);
        }
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10000);
        LambdaQueryWrapper<StatEfficiency> wrapper = buildEfficiencyWrapper(statMonth, rankEnum, queryDTO);
        List<StatEfficiency> list = statEfficiencyMapper.selectList(wrapper);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(rankEnum.getDesc() + "效能排行");
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            List<String> headers = new ArrayList<>();
            headers.add("排名");
            headers.add("统计月份");
            if (rankEnum == EfficiencyRankTypeEnum.PERSON) {
                headers.add("姓名");
                headers.add("所属部门");
            } else {
                headers.add("部门名称");
                headers.add("所属单位");
            }
            headers.add("任务总数");
            headers.add("已办结");
            headers.add("超时数");
            headers.add("办结率(%)");
            headers.add("平均时长");
            headers.add("效率得分");
            headers.add("等级");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                headerRow.createCell(i).setCellValue(headers.get(i));
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            for (int i = 0; i < list.size(); i++) {
                StatEfficiency e = list.get(i);
                Row row = sheet.createRow(i + 1);
                int col = 0;
                row.createCell(col++).setCellValue(e.getRankNo() == null ? 0 : e.getRankNo());
                row.createCell(col++).setCellValue(e.getStatMonth());
                if (rankEnum == EfficiencyRankTypeEnum.PERSON) {
                    row.createCell(col++).setCellValue(e.getTargetName() == null ? "" : e.getTargetName());
                    row.createCell(col++).setCellValue(e.getDeptName() == null ? "" : e.getDeptName());
                } else {
                    row.createCell(col++).setCellValue(e.getTargetName() == null ? "" : e.getTargetName());
                    row.createCell(col++).setCellValue(e.getUnitName() == null ? "" : e.getUnitName());
                }
                row.createCell(col++).setCellValue(e.getTotalTask() == null ? 0 : e.getTotalTask());
                row.createCell(col++).setCellValue(e.getCompletedTask() == null ? 0 : e.getCompletedTask());
                row.createCell(col++).setCellValue(e.getOverdueTask() == null ? 0 : e.getOverdueTask());
                row.createCell(col++).setCellValue(e.getCompletionRate() == null ? 0 : e.getCompletionRate());
                row.createCell(col++).setCellValue(e.getAvgDurationText() == null ? "" : e.getAvgDurationText());
                row.createCell(col++).setCellValue(e.getEfficiencyScore() == null ? 0 : e.getEfficiencyScore());
                row.createCell(col++).setCellValue(resolveRankLevel(e.getRankNo()));

                for (int c = 0; c < col; c++) {
                    row.getCell(c).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(width + 1024, 15000));
            }

            wb.write(outputStream);
            outputStream.flush();
        } catch (Exception ex) {
            throw new RuntimeException("导出Excel失败：" + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean calculateEfficiencyForMonth(String statMonth) {
        statMonth = resolveStatMonth(statMonth);
        deleteMonthData(statMonth, EfficiencyRankTypeEnum.DEPARTMENT);
        deleteMonthData(statMonth, EfficiencyRankTypeEnum.PERSON);

        List<StatEfficiency> deptList = aggregateDeptEfficiency(statMonth);
        calcScoreAndRank(deptList);
        for (StatEfficiency e : deptList) statEfficiencyMapper.insert(e);

        List<StatEfficiency> personList = aggregatePersonEfficiency(statMonth);
        calcScoreAndRank(personList);
        for (StatEfficiency e : personList) statEfficiencyMapper.insert(e);

        return true;
    }

    private String resolveStatMonth(String statMonth) {
        if (StrUtil.isBlank(statMonth)) {
            return YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return statMonth.trim();
    }

    private boolean hasMonthData(String statMonth, EfficiencyRankTypeEnum rankType) {
        Long cnt = statEfficiencyMapper.selectCount(new LambdaQueryWrapper<StatEfficiency>()
                .eq(StatEfficiency::getStatMonth, statMonth)
                .eq(StatEfficiency::getRankType, rankType.getCode()));
        return cnt != null && cnt > 0;
    }

    private void deleteMonthData(String statMonth, EfficiencyRankTypeEnum rankType) {
        statEfficiencyMapper.delete(new LambdaQueryWrapper<StatEfficiency>()
                .eq(StatEfficiency::getStatMonth, statMonth)
                .eq(StatEfficiency::getRankType, rankType.getCode()));
    }

    private LambdaQueryWrapper<StatEfficiency> buildEfficiencyWrapper(String statMonth, EfficiencyRankTypeEnum rankType, EfficiencyRankQueryDTO dto) {
        LambdaQueryWrapper<StatEfficiency> w = new LambdaQueryWrapper<>();
        w.eq(StatEfficiency::getStatMonth, statMonth);
        w.eq(StatEfficiency::getRankType, rankType.getCode());
        if (StrUtil.isNotBlank(dto.getUnitCode())) {
            w.eq(StatEfficiency::getUnitCode, dto.getUnitCode());
        }
        if (StrUtil.isNotBlank(dto.getDeptId()) && rankType == EfficiencyRankTypeEnum.PERSON) {
            w.eq(StatEfficiency::getDeptId, dto.getDeptId());
        }
        w.orderByAsc(StatEfficiency::getRankNo);
        return w;
    }

    private Page<StatEfficiencyVO> convertToEfficiencyVOPage(Page<StatEfficiency> entityPage) {
        Page<StatEfficiencyVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<StatEfficiencyVO> vos = entityPage.getRecords().stream().map(e -> {
            StatEfficiencyVO vo = new StatEfficiencyVO();
            vo.setId(e.getId());
            vo.setStatMonth(e.getStatMonth());
            vo.setRankType(e.getRankType());
            vo.setTargetId(e.getTargetId());
            vo.setTargetName(e.getTargetName());
            vo.setDeptId(e.getDeptId());
            vo.setDeptName(e.getDeptName());
            vo.setUnitCode(e.getUnitCode());
            vo.setUnitName(e.getUnitName());
            vo.setTotalTask(e.getTotalTask());
            vo.setCompletedTask(e.getCompletedTask());
            vo.setOverdueTask(e.getOverdueTask());
            vo.setCompletionRate(e.getCompletionRate());
            vo.setAvgDurationMinutes(e.getAvgDurationMinutes());
            vo.setAvgDurationText(e.getAvgDurationText());
            vo.setEfficiencyScore(e.getEfficiencyScore());
            vo.setRankNo(e.getRankNo());
            vo.setRankLevel(resolveRankLevel(e.getRankNo()));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(vos);
        return voPage;
    }

    private String resolveRankLevel(Integer rankNo) {
        if (rankNo == null || rankNo <= 0) return "-";
        if (rankNo <= 1) return "卓越";
        if (rankNo <= 3) return "优秀";
        if (rankNo <= 10) return "良好";
        return "达标";
    }

    private List<StatEfficiency> aggregateDeptEfficiency(String statMonth) {
        YearMonth ym = YearMonth.parse(statMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        String startStr = ym.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
        String endStr = ym.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59";

        String sql = "SELECT " +
                "  d.id as target_id, " +
                "  d.dept_name as target_name, " +
                "  u.unit_code as unit_code, " +
                "  u.unit_name as unit_name, " +
                "  COUNT(h.id) as total_task, " +
                "  SUM(CASE WHEN h.leave_time IS NOT NULL THEN 1 ELSE 0 END) as completed_task, " +
                "  SUM(CASE WHEN h.duration IS NOT NULL AND h.duration > (5 * 24 * 60 * 60) THEN 1 " +
                "      WHEN h.deadline_time IS NOT NULL AND h.leave_time IS NOT NULL AND h.leave_time > h.deadline_time THEN 1 " +
                "      ELSE 0 END) as overdue_task, " +
                "  AVG(CASE WHEN h.duration IS NOT NULL THEN h.duration ELSE NULL END) as avg_duration " +
                "FROM wf_process_history h " +
                "INNER JOIN sys_user su ON h.operator_id = su.user_code AND su.deleted = 0 " +
                "LEFT JOIN sys_dept d ON su.dept_id = d.id AND d.deleted = 0 " +
                "LEFT JOIN sys_unit u ON d.unit_id = u.id AND u.deleted = 0 " +
                "WHERE h.deleted = 0 AND h.node_type = 'userTask' AND h.enter_time BETWEEN ? AND ? " +
                "AND h.operator_id IS NOT NULL AND h.operator_id != '' " +
                "AND su.dept_id IS NOT NULL " +
                "GROUP BY d.id, d.dept_name, u.unit_code, u.unit_name";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, startStr, endStr);
        return rows.stream().map(row -> {
            StatEfficiency e = new StatEfficiency();
            e.setStatMonth(statMonth);
            e.setRankType(EfficiencyRankTypeEnum.DEPARTMENT.getCode());
            e.setTargetId(row.get("target_id") == null ? "" : row.get("target_id").toString());
            e.setTargetName(row.get("target_name") == null ? "未知部门" : row.get("target_name").toString());
            e.setDeptId(e.getTargetId());
            e.setDeptName(e.getTargetName());
            e.setUnitCode(row.get("unit_code") == null ? "" : row.get("unit_code").toString());
            e.setUnitName(row.get("unit_name") == null ? "" : row.get("unit_name").toString());
            e.setTotalTask(row.get("total_task") == null ? 0L : ((Number) row.get("total_task")).longValue());
            e.setCompletedTask(row.get("completed_task") == null ? 0L : ((Number) row.get("completed_task")).longValue());
            e.setOverdueTask(row.get("overdue_task") == null ? 0L : ((Number) row.get("overdue_task")).longValue());
            double completionRate = e.getTotalTask() > 0
                    ? Math.round(e.getCompletedTask() * 10000.0 / e.getTotalTask()) / 100.0
                    : 0.0;
            e.setCompletionRate(completionRate);
            long avgSeconds = row.get("avg_duration") == null ? 0L : ((Number) row.get("avg_duration")).longValue();
            e.setAvgDurationMinutes(avgSeconds / 60);
            e.setAvgDurationText(formatDuration(avgSeconds * 1000L));
            return e;
        }).collect(Collectors.toList());
    }

    private List<StatEfficiency> aggregatePersonEfficiency(String statMonth) {
        YearMonth ym = YearMonth.parse(statMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        String startStr = ym.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
        String endStr = ym.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59";

        String sql = "SELECT " +
                "  su.id as target_id, " +
                "  su.user_name as target_name, " +
                "  su.user_code as user_code, " +
                "  d.id as dept_id, " +
                "  d.dept_name as dept_name, " +
                "  u.unit_code as unit_code, " +
                "  u.unit_name as unit_name, " +
                "  COUNT(h.id) as total_task, " +
                "  SUM(CASE WHEN h.leave_time IS NOT NULL THEN 1 ELSE 0 END) as completed_task, " +
                "  SUM(CASE WHEN h.duration IS NOT NULL AND h.duration > (5 * 24 * 60 * 60) THEN 1 " +
                "      WHEN h.deadline_time IS NOT NULL AND h.leave_time IS NOT NULL AND h.leave_time > h.deadline_time THEN 1 " +
                "      ELSE 0 END) as overdue_task, " +
                "  AVG(CASE WHEN h.duration IS NOT NULL THEN h.duration ELSE NULL END) as avg_duration " +
                "FROM wf_process_history h " +
                "INNER JOIN sys_user su ON h.operator_id = su.user_code AND su.deleted = 0 " +
                "LEFT JOIN sys_dept d ON su.dept_id = d.id AND d.deleted = 0 " +
                "LEFT JOIN sys_unit u ON d.unit_id = u.id AND u.deleted = 0 " +
                "WHERE h.deleted = 0 AND h.node_type = 'userTask' AND h.enter_time BETWEEN ? AND ? " +
                "AND h.operator_id IS NOT NULL AND h.operator_id != '' " +
                "GROUP BY su.id, su.user_name, su.user_code, d.id, d.dept_name, u.unit_code, u.unit_name";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, startStr, endStr);
        return rows.stream().map(row -> {
            StatEfficiency e = new StatEfficiency();
            e.setStatMonth(statMonth);
            e.setRankType(EfficiencyRankTypeEnum.PERSON.getCode());
            String targetId = row.get("target_id") == null ? "" : row.get("target_id").toString();
            String userCode = row.get("user_code") == null ? "" : row.get("user_code").toString();
            e.setTargetId(StrUtil.isNotBlank(targetId) ? targetId : userCode);
            String rawName = row.get("target_name") == null ? null : row.get("target_name").toString();
            if (StrUtil.isBlank(rawName)) rawName = userCode;
            e.setTargetName(rawName);
            e.setDeptId(row.get("dept_id") == null ? "" : row.get("dept_id").toString());
            e.setDeptName(row.get("dept_name") == null ? "未知部门" : row.get("dept_name").toString());
            e.setUnitCode(row.get("unit_code") == null ? "" : row.get("unit_code").toString());
            e.setUnitName(row.get("unit_name") == null ? "" : row.get("unit_name").toString());
            e.setTotalTask(row.get("total_task") == null ? 0L : ((Number) row.get("total_task")).longValue());
            e.setCompletedTask(row.get("completed_task") == null ? 0L : ((Number) row.get("completed_task")).longValue());
            e.setOverdueTask(row.get("overdue_task") == null ? 0L : ((Number) row.get("overdue_task")).longValue());
            double completionRate = e.getTotalTask() > 0
                    ? Math.round(e.getCompletedTask() * 10000.0 / e.getTotalTask()) / 100.0
                    : 0.0;
            e.setCompletionRate(completionRate);
            long avgSeconds = row.get("avg_duration") == null ? 0L : ((Number) row.get("avg_duration")).longValue();
            e.setAvgDurationMinutes(avgSeconds / 60);
            e.setAvgDurationText(formatDuration(avgSeconds * 1000L));
            return e;
        }).collect(Collectors.toList());
    }

    private void calcScoreAndRank(List<StatEfficiency> list) {
        if (list == null || list.isEmpty()) return;

        long maxTotal = list.stream().mapToLong(e -> e.getTotalTask() == null ? 0 : e.getTotalTask()).max().orElse(1L);
        long minAvg = list.stream().mapToLong(e -> e.getAvgDurationMinutes() == null ? 0 : e.getAvgDurationMinutes()).min().orElse(1L);
        if (minAvg <= 0) minAvg = 1L;

        for (StatEfficiency e : list) {
            double totalScore = maxTotal > 0
                    ? ((e.getTotalTask() == null ? 0L : e.getTotalTask()) * 30.0 / maxTotal) : 0;
            double completionScore = (e.getCompletionRate() == null ? 0.0 : e.getCompletionRate()) * 0.4;
            long avgDuration = e.getAvgDurationMinutes() == null ? 0L : e.getAvgDurationMinutes();
            double durationScore = avgDuration > 0
                    ? (minAvg * 30.0 / avgDuration) : 0;
            if (durationScore > 30) durationScore = 30;
            long overdue = e.getOverdueTask() == null ? 0L : e.getOverdueTask();
            double overduePenalty = overdue * 2.0;

            double score = Math.round((totalScore + completionScore + durationScore - overduePenalty) * 100.0) / 100.0;
            if (score < 0) score = 0;
            e.setEfficiencyScore(score);
        }

        list.sort((a, b) -> {
            double sa = a.getEfficiencyScore() == null ? 0 : a.getEfficiencyScore();
            double sb = b.getEfficiencyScore() == null ? 0 : b.getEfficiencyScore();
            return Double.compare(sb, sa);
        });

        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRankNo(i + 1);
        }
    }
}

