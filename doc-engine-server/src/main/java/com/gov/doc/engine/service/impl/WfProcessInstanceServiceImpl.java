package com.gov.doc.engine.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.WfProcessStartDTO;
import com.gov.doc.engine.entity.*;
import com.gov.doc.engine.enums.WfNodeTypeEnum;
import com.gov.doc.engine.mapper.*;
import com.gov.doc.engine.service.*;
import com.gov.doc.engine.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WfProcessInstanceServiceImpl extends ServiceImpl<WfProcessInstanceMapper, WfProcessInstance> implements WfProcessInstanceService {

    @Autowired
    private WfProcessInstanceMapper processInstanceMapper;

    @Autowired
    private WfTaskMapper taskMapper;

    @Autowired
    private WfProcessNodeMapper processNodeMapper;

    @Autowired
    private WfProcessEdgeMapper processEdgeMapper;

    @Autowired
    private WfApprovalOpinionMapper approvalOpinionMapper;

    @Autowired
    private WfProcessHistoryMapper processHistoryMapper;

    @Autowired
    private WfCountersignMapper countersignMapper;

    @Autowired
    private WfCountersignItemMapper countersignItemMapper;

    @Autowired
    private WfParticipantMapper participantMapper;

    @Autowired
    private WfProcessEngineService processEngineService;

    @Autowired
    private WfCountersignService countersignService;

    @Autowired
    private WfApprovalOpinionService approvalOpinionService;

    @Autowired
    private WfProcessHistoryService processHistoryService;

    @Autowired
    private WfProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private DocDocumentMapper docDocumentMapper;

    @Autowired
    private DocStatusMachineService statusMachineService;

    @Override
    public PageResult<WfProcessInstanceVO> pageList(Integer pageNum, Integer pageSize, String startUserId, String status, String keyword) {
        LambdaQueryWrapper<WfProcessInstance> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(startUserId)) {
            wrapper.eq(WfProcessInstance::getStartUserId, startUserId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(WfProcessInstance::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(WfProcessInstance::getBusinessTitle, keyword)
                    .or().like(WfProcessInstance::getInstanceNo, keyword)
                    .or().like(WfProcessInstance::getProcessName, keyword));
        }
        wrapper.orderByDesc(WfProcessInstance::getCreateTime);

        Page<WfProcessInstance> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<WfProcessInstanceVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public WfProcessInstanceVO getDetail(Long id) {
        WfProcessInstance instance = this.getById(id);
        if (instance == null) {
            return null;
        }
        WfProcessInstanceVO vo = convertToVO(instance);

        List<WfTask> currentTasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getProcessInstanceId, id)
                        .in(WfTask::getStatus, "pending", "claimed")
                        .orderByDesc(WfTask::getCreateTime));
        vo.setCurrentTasks(currentTasks.stream().map(task -> {
            WfTaskVO taskVO = new WfTaskVO();
            BeanUtils.copyProperties(task, taskVO);
            return taskVO;
        }).collect(Collectors.toList()));

        List<WfApprovalOpinionVO> opinions = approvalOpinionService.getByProcessInstanceId(id);
        vo.setOpinions(opinions);

        List<WfProcessHistoryVO> history = processHistoryService.getByProcessInstanceId(id);
        vo.setHistory(history);

        return vo;
    }

    @Override
    public WfProcessInstanceVO getByBusinessKey(String businessKey) {
        WfProcessInstance instance = this.getOne(
                new LambdaQueryWrapper<WfProcessInstance>()
                        .eq(WfProcessInstance::getBusinessKey, businessKey)
                        .orderByDesc(WfProcessInstance::getCreateTime)
                        .last("limit 1"));
        return instance != null ? convertToVO(instance) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startProcess(WfProcessStartDTO startDTO) {
        WfProcessDefinition definition = processDefinitionMapper.selectById(startDTO.getProcessDefId());
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        if (definition.getStatus() != 1) {
            throw new RuntimeException("流程定义未发布");
        }

        WfProcessInstance instance = new WfProcessInstance();
        instance.setInstanceNo(processEngineService.generateInstanceNo());
        instance.setProcessDefId(definition.getId());
        instance.setProcessCode(definition.getProcessCode());
        instance.setProcessName(definition.getProcessName());
        instance.setBusinessKey(startDTO.getBusinessKey());
        instance.setBusinessType(startDTO.getBusinessType());
        instance.setBusinessTitle(startDTO.getBusinessTitle());
        instance.setStatus("running");
        instance.setStartTime(LocalDateTime.now());
        if (startDTO.getFormData() != null && !startDTO.getFormData().isEmpty()) {
            instance.setFormData(JSON.toJSONString(startDTO.getFormData()));
        }
        if (startDTO.getVariables() != null && !startDTO.getVariables().isEmpty()) {
            instance.setVariables(JSON.toJSONString(startDTO.getVariables()));
        }
        instance.setRemark(startDTO.getRemark());
        this.save(instance);

        WfProcessNode startNode = processNodeMapper.selectOne(
                new LambdaQueryWrapper<WfProcessNode>()
                        .eq(WfProcessNode::getProcessDefId, definition.getId())
                        .eq(WfProcessNode::getNodeType, WfNodeTypeEnum.START.getCode()));
        if (startNode == null) {
            throw new RuntimeException("流程定义缺少开始节点");
        }

        Map<String, Object> variables = startDTO.getVariables() != null ? startDTO.getVariables() : new HashMap<>();
        processEngineService.executeNode(instance, startNode, variables);

        updateDocStatusOnProcessStart(instance, startDTO);

        return instance.getId();
    }

    private void updateDocStatusOnProcessStart(WfProcessInstance instance, WfProcessStartDTO startDTO) {
        try {
            String businessKey = startDTO.getBusinessKey();
            if (!StringUtils.hasText(businessKey)) {
                return;
            }

            DocDocument doc = docDocumentMapper.selectOne(
                    new LambdaQueryWrapper<DocDocument>()
                            .eq(DocDocument::getId, Long.parseLong(businessKey))
                            .last("LIMIT 1")
            );

            if (doc == null) {
                doc = docDocumentMapper.selectOne(
                        new LambdaQueryWrapper<DocDocument>()
                                .eq(DocDocument::getProcessInstanceId, instance.getId().toString())
                                .last("LIMIT 1")
                );
            }

            if (doc != null) {
                String targetStatus = DocStatusEnum.REVIEWING.getCode();
                String currentStatus = doc.getStatus();

                if (!targetStatus.equals(currentStatus) && DocStatusEnum.canTransition(currentStatus, targetStatus)) {
                    String operatorId = startDTO.getStartUserId();
                    String operatorName = startDTO.getStartUserName();
                    if (!StringUtils.hasText(operatorId)) {
                        operatorId = instance.getStartUserId();
                        operatorName = instance.getStartUserName();
                    }

                    statusMachineService.transitionWithReason(
                            doc.getId(),
                            targetStatus,
                            "启动流程：" + instance.getProcessName(),
                            operatorId,
                            operatorName,
                            "流程实例ID: " + instance.getId()
                    );

                    if (!StringUtils.hasText(doc.getProcessInstanceId())) {
                        doc.setProcessInstanceId(instance.getId().toString());
                        docDocumentMapper.updateById(doc);
                    }

                    log.info("Document {} status updated to {} on process start", doc.getId(), targetStatus);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update document status on process start", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendProcess(Long id) {
        WfProcessInstance instance = this.getById(id);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        if (!"running".equals(instance.getStatus())) {
            throw new RuntimeException("流程实例状态不允许挂起");
        }
        instance.setStatus("suspended");
        this.updateById(instance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateProcess(Long id) {
        WfProcessInstance instance = this.getById(id);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        if (!"suspended".equals(instance.getStatus())) {
            throw new RuntimeException("流程实例状态不允许激活");
        }
        instance.setStatus("running");
        this.updateById(instance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminateProcess(Long id, String reason) {
        WfProcessInstance instance = this.getById(id);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        if ("completed".equals(instance.getStatus()) || "terminated".equals(instance.getStatus())) {
            throw new RuntimeException("流程实例已结束");
        }

        instance.setStatus("terminated");
        instance.setEndTime(LocalDateTime.now());
        if (instance.getStartTime() != null) {
            long duration = java.time.Duration.between(instance.getStartTime(), instance.getEndTime()).toMillis();
            instance.setDuration(duration);
        }
        this.updateById(instance);

        List<WfTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getProcessInstanceId, id)
                        .in(WfTask::getStatus, "pending", "claimed"));
        for (WfTask task : tasks) {
            task.setStatus("terminated");
            task.setCompleteTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }

        WfApprovalOpinion opinion = new WfApprovalOpinion();
        opinion.setProcessInstanceId(id);
        opinion.setNodeId(instance.getCurrentNodeId());
        opinion.setNodeName(instance.getCurrentNodeName());
        opinion.setBusinessKey(instance.getBusinessKey());
        opinion.setApprovalType("terminate");
        opinion.setApprovalResult("terminated");
        opinion.setApprovalOpinion(reason);
        opinion.setApprovalTime(LocalDateTime.now());
        approvalOpinionService.saveOpinion(opinion);
    }

    @Override
    public WfProcessInstanceVO convertToVO(WfProcessInstance instance) {
        WfProcessInstanceVO vo = new WfProcessInstanceVO();
        BeanUtils.copyProperties(instance, vo);

        vo.setStatusName(getStatusName(instance.getStatus()));
        vo.setDurationText(getDurationText(instance.getDuration()));

        if (StringUtils.hasText(instance.getFormData())) {
            vo.setFormData(JSON.parseObject(instance.getFormData(), Map.class));
        }
        if (StringUtils.hasText(instance.getVariables())) {
            vo.setVariables(JSON.parseObject(instance.getVariables(), Map.class));
        }

        return vo;
    }

    private String getStatusName(String status) {
        switch (status) {
            case "running":
                return "进行中";
            case "suspended":
                return "已挂起";
            case "completed":
                return "已完成";
            case "terminated":
                return "已终止";
            default:
                return "未知";
        }
    }

    private String getDurationText(Long duration) {
        if (duration == null || duration <= 0) {
            return "-";
        }
        long seconds = duration / 1000;
        long days = seconds / (24 * 3600);
        long hours = (seconds % (24 * 3600)) / 3600;
        long minutes = (seconds % 3600) / 60;
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
        return sb.length() > 0 ? sb.toString() : "1分钟内";
    }
}
