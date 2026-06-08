package com.gov.doc.engine.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.entity.WfApprovalOpinion;
import com.gov.doc.engine.entity.WfProcessHistory;
import com.gov.doc.engine.enums.WfNodeTypeEnum;
import com.gov.doc.engine.enums.WfOperationTypeEnum;
import com.gov.doc.engine.mapper.WfApprovalOpinionMapper;
import com.gov.doc.engine.mapper.WfProcessHistoryMapper;
import com.gov.doc.engine.service.WfProcessHistoryService;
import com.gov.doc.engine.vo.WfApprovalOpinionVO;
import com.gov.doc.engine.vo.WfProcessHistoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WfProcessHistoryServiceImpl extends ServiceImpl<WfProcessHistoryMapper, WfProcessHistory> implements WfProcessHistoryService {

    @Autowired
    private WfProcessHistoryMapper processHistoryMapper;

    @Autowired
    private WfApprovalOpinionMapper approvalOpinionMapper;

    @Override
    public List<WfProcessHistoryVO> getByProcessInstanceId(Long processInstanceId) {
        List<WfProcessHistory> list = processHistoryMapper.selectList(
                new LambdaQueryWrapper<WfProcessHistory>()
                        .eq(WfProcessHistory::getProcessInstanceId, processInstanceId)
                        .orderByAsc(WfProcessHistory::getEnterTime));
        return list.stream()
                .sorted(Comparator.comparing(WfProcessHistory::getEnterTime))
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordHistory(WfProcessHistory history) {
        history.setEnterTime(LocalDateTime.now());
        history.setStatus("active");
        this.save(history);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeHistory(Long processInstanceId, String nodeId, String operationType) {
        WfProcessHistory history = getActiveHistory(processInstanceId, nodeId);
        if (history == null) {
            return;
        }

        history.setLeaveTime(LocalDateTime.now());
        if (history.getEnterTime() != null && history.getLeaveTime() != null) {
            history.setDuration(Duration.between(history.getEnterTime(), history.getLeaveTime()).getSeconds());
        }
        history.setOperationType(operationType);
        history.setStatus("history");
        this.updateById(history);
    }

    @Override
    public WfProcessHistory getActiveHistory(Long processInstanceId, String nodeId) {
        return processHistoryMapper.selectOne(
                new LambdaQueryWrapper<WfProcessHistory>()
                        .eq(WfProcessHistory::getProcessInstanceId, processInstanceId)
                        .eq(WfProcessHistory::getNodeId, nodeId)
                        .eq(WfProcessHistory::getStatus, "active")
                        .orderByDesc(WfProcessHistory::getEnterTime)
                        .last("LIMIT 1"));
    }

    @Override
    public WfProcessHistory getCompletedHistory(Long processInstanceId, String nodeId) {
        return processHistoryMapper.selectOne(
                new LambdaQueryWrapper<WfProcessHistory>()
                        .eq(WfProcessHistory::getProcessInstanceId, processInstanceId)
                        .eq(WfProcessHistory::getNodeId, nodeId)
                        .eq(WfProcessHistory::getStatus, "history")
                        .orderByDesc(WfProcessHistory::getEnterTime)
                        .last("LIMIT 1"));
    }

    @Override
    public WfProcessHistoryVO convertToVO(WfProcessHistory history) {
        WfProcessHistoryVO vo = new WfProcessHistoryVO();
        BeanUtils.copyProperties(history, vo);

        vo.setNodeTypeName(WfNodeTypeEnum.getNameByCode(history.getNodeType()));
        if (StringUtils.hasText(history.getOperationType())) {
            vo.setOperationTypeName(WfOperationTypeEnum.getNameByCode(history.getOperationType()));
        }
        vo.setStatusName(getHistoryStatusName(history.getStatus()));
        if (history.getDuration() != null) {
            vo.setDurationText(formatDuration(history.getDuration()));
        }
        if (StringUtils.hasText(history.getVariables())) {
            vo.setVariables(JSON.parseObject(history.getVariables(), Map.class));
        }

        if (history.getTaskId() != null) {
            WfApprovalOpinion opinion = approvalOpinionMapper.selectOne(
                    new LambdaQueryWrapper<WfApprovalOpinion>()
                            .eq(WfApprovalOpinion::getTaskId, history.getTaskId())
                            .orderByDesc(WfApprovalOpinion::getApprovalTime)
                            .last("LIMIT 1"));
            if (opinion != null) {
                WfApprovalOpinionVO opinionVO = new WfApprovalOpinionVO();
                BeanUtils.copyProperties(opinion, opinionVO);
                vo.setOpinion(opinionVO);
            }
        }

        return vo;
    }

    private String getHistoryStatusName(String status) {
        switch (status) {
            case "active":
                return "进行中";
            case "history":
                return "已完成";
            default:
                return status;
        }
    }

    private String formatDuration(Long seconds) {
        if (seconds == null || seconds <= 0) {
            return "0秒";
        }
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

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
        if (secs > 0 || sb.length() == 0) {
            sb.append(secs).append("秒");
        }
        return sb.toString();
    }
}
