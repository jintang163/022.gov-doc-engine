package com.gov.doc.engine.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gov.doc.engine.entity.DocAnalysis;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.entity.DocUrgeLog;
import com.gov.doc.engine.entity.RejectReason;
import com.gov.doc.engine.entity.ReminderLog;
import com.gov.doc.engine.entity.WfApprovalOpinion;
import com.gov.doc.engine.entity.WfProcessHistory;
import com.gov.doc.engine.entity.WfProcessInstance;
import com.gov.doc.engine.mapper.DocAnalysisMapper;
import com.gov.doc.engine.mapper.DocDocumentMapper;
import com.gov.doc.engine.mapper.DocUrgeLogMapper;
import com.gov.doc.engine.mapper.RejectReasonMapper;
import com.gov.doc.engine.mapper.ReminderLogMapper;
import com.gov.doc.engine.mapper.WfApprovalOpinionMapper;
import com.gov.doc.engine.mapper.WfProcessHistoryMapper;
import com.gov.doc.engine.mapper.WfProcessInstanceMapper;
import com.gov.doc.engine.service.DataExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DataExtractionServiceImpl implements DataExtractionService {

    @Autowired
    private WfProcessHistoryMapper wfProcessHistoryMapper;

    @Autowired
    private WfProcessInstanceMapper wfProcessInstanceMapper;

    @Autowired
    private WfApprovalOpinionMapper wfApprovalOpinionMapper;

    @Autowired
    private DocUrgeLogMapper docUrgeLogMapper;

    @Autowired
    private DocDocumentMapper docDocumentMapper;

    @Autowired
    private DocAnalysisMapper docAnalysisMapper;

    @Autowired
    private ReminderLogMapper reminderLogMapper;

    @Autowired
    private RejectReasonMapper rejectReasonMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int extractProcessHistory(LocalDateTime since) {
        log.info("开始抽取流程历史数据，since={}", since);

        LambdaQueryWrapper<WfProcessHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfProcessHistory::getDeleted, 0);
        if (since != null) {
            wrapper.ge(WfProcessHistory::getCreateTime, since);
        }
        wrapper.isNotNull(WfProcessHistory::getOperatorId);
        wrapper.ne(WfProcessHistory::getOperatorId, "");

        List<WfProcessHistory> histories = wfProcessHistoryMapper.selectList(wrapper);
        if (histories.isEmpty()) {
            log.info("无新增流程历史数据需要抽取");
            return 0;
        }

        List<Long> instanceIds = histories.stream()
                .map(WfProcessHistory::getProcessInstanceId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, WfProcessInstance> instanceMap = wfProcessInstanceMapper.selectList(
                new LambdaQueryWrapper<WfProcessInstance>().in(WfProcessInstance::getId, instanceIds)
        ).stream().collect(Collectors.toMap(WfProcessInstance::getId, i -> i, (a, b) -> a));

        List<String> businessKeys = instanceMap.values().stream()
                .map(WfProcessInstance::getBusinessKey)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, DocDocument> docMap = Map.of();
        if (!businessKeys.isEmpty()) {
            docMap = docDocumentMapper.selectList(
                    new LambdaQueryWrapper<DocDocument>().in(DocDocument::getId,
                            businessKeys.stream().map(Long::valueOf).collect(Collectors.toList()))
            ).stream().collect(Collectors.toMap(d -> String.valueOf(d.getId()), d -> d, (a, b) -> a));
        }

        Map<String, DocDocument> finalDocMap = docMap;
        List<Long> existingSourceIds = findExistingSourceIds("camunda_task", histories);

        int count = 0;
        for (WfProcessHistory h : histories) {
            String sourceId = h.getId() != null ? h.getId().toString() : "";
            if (existingSourceIds.contains(h.getId())) continue;

            WfProcessInstance inst = instanceMap.get(h.getProcessInstanceId());
            DocAnalysis analysis = new DocAnalysis();
            analysis.setSourceType("camunda_task");
            analysis.setSourceId(sourceId);

            if (inst != null) {
                analysis.setBusinessKey(inst.getBusinessKey());
                analysis.setProcessCode(inst.getProcessCode());
                analysis.setProcessName(inst.getProcessName());
                if (StrUtil.isNotBlank(inst.getBusinessKey())) {
                    DocDocument doc = finalDocMap.get(inst.getBusinessKey());
                    if (doc != null) {
                        analysis.setDocNumber(doc.getDocNumber());
                        analysis.setDocTitle(doc.getDocTitle());
                        analysis.setDocType(doc.getDocType());
                    }
                }
            }

            analysis.setProcessInstanceId(h.getProcessInstanceId());
            analysis.setNodeId(h.getNodeId());
            analysis.setNodeName(h.getNodeName());
            analysis.setNodeType(h.getNodeType());
            analysis.setOperatorId(h.getOperatorId());
            analysis.setOperatorName(h.getOperatorName());
            analysis.setOperationType(h.getOperationType());
            analysis.setEnterTime(h.getEnterTime());
            analysis.setLeaveTime(h.getLeaveTime());
            analysis.setDuration(h.getDuration());
            analysis.setDurationText(formatDuration(h.getDuration()));
            analysis.setVariables(h.getVariables());
            analysis.setExtractTime(LocalDateTime.now());

            if (h.getLeaveTime() != null && h.getDuration() != null && h.getDuration() > 5 * 24 * 3600) {
                analysis.setIsOverdue(1);
            } else {
                analysis.setIsOverdue(0);
            }

            docAnalysisMapper.insert(analysis);
            count++;
        }

        log.info("流程历史数据抽取完成，新增 {} 条", count);
        return count;
    }

    @Override
    public int extractUrgeLogs(LocalDateTime since) {
        log.info("开始抽取催办记录数据，since={}", since);

        LambdaQueryWrapper<DocUrgeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocUrgeLog::getDeleted, 0);
        if (since != null) {
            wrapper.ge(DocUrgeLog::getCreateTime, since);
        }

        List<DocUrgeLog> urgeLogs = docUrgeLogMapper.selectList(wrapper);
        if (urgeLogs.isEmpty()) {
            log.info("无新增催办记录需要抽取");
            return 0;
        }

        List<Long> existingSourceIds = findExistingReminderSourceIds(urgeLogs);

        int count = 0;
        for (DocUrgeLog ul : urgeLogs) {
            if (existingSourceIds.contains(ul.getId())) continue;

            ReminderLog reminder = new ReminderLog();
            reminder.setSourceType("urge_log");
            reminder.setSourceId(ul.getId() != null ? ul.getId().toString() : "");
            reminder.setIncomingId(ul.getIncomingId());
            reminder.setHandlingId(ul.getHandlingId());
            reminder.setUrgeNo(ul.getUrgeNo());
            reminder.setUrgeType(ul.getUrgeType());
            reminder.setUrgedUserId(ul.getUrgedUserId());
            reminder.setUrgedUserName(ul.getUrgedUserName());
            reminder.setUrgedDeptId(ul.getUrgedDeptId());
            reminder.setUrgedDeptName(ul.getUrgedDeptName());
            reminder.setUrgeContent(ul.getUrgeContent());
            reminder.setUrgeStatus(ul.getStatus());
            reminder.setAcknowledgeTime(ul.getAcknowledgeTime());
            reminder.setExtractTime(LocalDateTime.now());

            enrichReminderWithDocInfo(reminder, ul.getIncomingId());

            reminderLogMapper.insert(reminder);
            count++;
        }

        log.info("催办记录数据抽取完成，新增 {} 条", count);
        return count;
    }

    @Override
    public int extractRejectReasons(LocalDateTime since) {
        log.info("开始抽取退回原因数据，since={}", since);

        LambdaQueryWrapper<WfApprovalOpinion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfApprovalOpinion::getDeleted, 0);
        wrapper.in(WfApprovalOpinion::getApprovalType, "reject", "return");
        if (since != null) {
            wrapper.ge(WfApprovalOpinion::getCreateTime, since);
        }

        List<WfApprovalOpinion> opinions = wfApprovalOpinionMapper.selectList(wrapper);
        if (opinions.isEmpty()) {
            log.info("无新增退回审批意见需要抽取");
            return 0;
        }

        List<Long> existingSourceIds = findExistingRejectSourceIds(opinions);

        int count = 0;
        for (WfApprovalOpinion op : opinions) {
            if (existingSourceIds.contains(op.getId())) continue;

            RejectReason reason = new RejectReason();
            reason.setSourceType("approval_opinion");
            reason.setSourceId(op.getId() != null ? op.getId().toString() : "");
            reason.setBusinessKey(op.getBusinessKey());
            reason.setProcessInstanceId(op.getProcessInstanceId());
            reason.setTaskId(op.getTaskId());
            reason.setNodeId(op.getNodeId());
            reason.setNodeName(op.getNodeName());
            reason.setApprovalType(op.getApprovalType());
            reason.setRejectReason(op.getApprovalOpinion());
            reason.setApproverId(op.getApproverId());
            reason.setApproverName(op.getApproverName());
            reason.setApproverDeptId(op.getApproverDeptId());
            reason.setApproverDeptName(op.getApproverDeptName());
            reason.setTargetNodeId(op.getTargetNodeId());
            reason.setTargetNodeName(op.getTargetNodeName());
            reason.setTargetUserId(op.getTargetUserId());
            reason.setTargetUserName(op.getTargetUserName());
            reason.setApprovalTime(op.getApprovalTime());
            reason.setExtractTime(LocalDateTime.now());

            if (StrUtil.isNotBlank(op.getBusinessKey())) {
                try {
                    DocDocument doc = docDocumentMapper.selectById(Long.valueOf(op.getBusinessKey()));
                    if (doc != null) {
                        reason.setDocNumber(doc.getDocNumber());
                        reason.setDocTitle(doc.getDocTitle());
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            rejectReasonMapper.insert(reason);
            count++;
        }

        log.info("退回原因数据抽取完成，新增 {} 条", count);
        return count;
    }

    @Override
    public void extractAll() {
        LocalDateTime since = getLastExtractTime();
        log.info("开始执行全量数据抽取，最近抽取时间={}", since);

        int processCount = extractProcessHistory(since);
        int urgeCount = extractUrgeLogs(since);
        int rejectCount = extractRejectReasons(since);

        log.info("全量数据抽取完成：流程历史={}, 催办记录={}, 退回原因={}", processCount, urgeCount, rejectCount);
    }

    @Override
    public List<DocAnalysis> queryDocAnalysis(String businessKey, String sourceType, int limit) {
        LambdaQueryWrapper<DocAnalysis> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(businessKey)) {
            wrapper.eq(DocAnalysis::getBusinessKey, businessKey);
        }
        if (StrUtil.isNotBlank(sourceType)) {
            wrapper.eq(DocAnalysis::getSourceType, sourceType);
        }
        wrapper.orderByDesc(DocAnalysis::getExtractTime);
        wrapper.last("LIMIT " + limit);
        return docAnalysisMapper.selectList(wrapper);
    }

    @Override
    public List<ReminderLog> queryReminderLog(Long incomingId, String urgeType, int limit) {
        LambdaQueryWrapper<ReminderLog> wrapper = new LambdaQueryWrapper<>();
        if (incomingId != null) {
            wrapper.eq(ReminderLog::getIncomingId, incomingId);
        }
        if (StrUtil.isNotBlank(urgeType)) {
            wrapper.eq(ReminderLog::getUrgeType, urgeType);
        }
        wrapper.orderByDesc(ReminderLog::getExtractTime);
        wrapper.last("LIMIT " + limit);
        return reminderLogMapper.selectList(wrapper);
    }

    @Override
    public List<RejectReason> queryRejectReason(String businessKey, String approvalType, int limit) {
        LambdaQueryWrapper<RejectReason> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(businessKey)) {
            wrapper.eq(RejectReason::getBusinessKey, businessKey);
        }
        if (StrUtil.isNotBlank(approvalType)) {
            wrapper.eq(RejectReason::getApprovalType, approvalType);
        }
        wrapper.orderByDesc(RejectReason::getExtractTime);
        wrapper.last("LIMIT " + limit);
        return rejectReasonMapper.selectList(wrapper);
    }

    private LocalDateTime getLastExtractTime() {
        try {
            DocAnalysis last = docAnalysisMapper.selectOne(
                    new LambdaQueryWrapper<DocAnalysis>()
                            .isNotNull(DocAnalysis::getExtractTime)
                            .orderByDesc(DocAnalysis::getExtractTime)
                            .last("LIMIT 1"));
            if (last != null && last.getExtractTime() != null) {
                return last.getExtractTime();
            }
        } catch (Exception ignored) {
        }
        return LocalDateTime.now().minusDays(30);
    }

    private List<Long> findExistingSourceIds(String sourceType, List<WfProcessHistory> histories) {
        List<String> ids = histories.stream()
                .map(h -> h.getId() != null ? h.getId().toString() : "")
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        if (ids.isEmpty()) return List.of();
        List<DocAnalysis> existing = docAnalysisMapper.selectList(
                new LambdaQueryWrapper<DocAnalysis>()
                        .eq(DocAnalysis::getSourceType, sourceType)
                        .in(DocAnalysis::getSourceId, ids));
        return existing.stream()
                .map(e -> Long.valueOf(e.getSourceId()))
                .collect(Collectors.toList());
    }

    private List<Long> findExistingReminderSourceIds(List<DocUrgeLog> urgeLogs) {
        List<String> ids = urgeLogs.stream()
                .map(u -> u.getId() != null ? u.getId().toString() : "")
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        if (ids.isEmpty()) return List.of();
        List<ReminderLog> existing = reminderLogMapper.selectList(
                new LambdaQueryWrapper<ReminderLog>()
                        .eq(ReminderLog::getSourceType, "urge_log")
                        .in(ReminderLog::getSourceId, ids));
        return existing.stream()
                .map(e -> Long.valueOf(e.getSourceId()))
                .collect(Collectors.toList());
    }

    private List<Long> findExistingRejectSourceIds(List<WfApprovalOpinion> opinions) {
        List<String> ids = opinions.stream()
                .map(o -> o.getId() != null ? o.getId().toString() : "")
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        if (ids.isEmpty()) return List.of();
        List<RejectReason> existing = rejectReasonMapper.selectList(
                new LambdaQueryWrapper<RejectReason>()
                        .eq(RejectReason::getSourceType, "approval_opinion")
                        .in(RejectReason::getSourceId, ids));
        return existing.stream()
                .map(e -> Long.valueOf(e.getSourceId()))
                .collect(Collectors.toList());
    }

    private void enrichReminderWithDocInfo(ReminderLog reminder, Long incomingId) {
        if (incomingId == null) return;
        try {
            String sql = "SELECT d.doc_number, d.doc_title FROM doc_incoming i " +
                    "LEFT JOIN doc_document d ON i.doc_id = d.id AND d.deleted = 0 " +
                    "WHERE i.id = ? AND i.deleted = 0 LIMIT 1";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, incomingId);
            if (row != null) {
                reminder.setDocNumber(row.get("doc_number") == null ? "" : row.get("doc_number").toString());
                reminder.setDocTitle(row.get("doc_title") == null ? "" : row.get("doc_title").toString());
            }
        } catch (Exception ignored) {
        }
    }

    private String formatDuration(Long seconds) {
        if (seconds == null || seconds <= 0) return "-";
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        if (days > 0) return days + "天" + hours + "小时";
        if (hours > 0) return hours + "小时" + minutes + "分钟";
        return minutes + "分钟";
    }
}
