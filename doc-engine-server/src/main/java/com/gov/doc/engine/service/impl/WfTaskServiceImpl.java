package com.gov.doc.engine.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.WfApprovalDTO;
import com.gov.doc.engine.dto.WfTaskAddSignDTO;
import com.gov.doc.engine.dto.WfTaskDelegateDTO;
import com.gov.doc.engine.dto.WfTaskQueryDTO;
import com.gov.doc.engine.entity.*;
import com.gov.doc.engine.mapper.*;
import com.gov.doc.engine.service.*;
import com.gov.doc.engine.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WfTaskServiceImpl extends ServiceImpl<WfTaskMapper, WfTask> implements WfTaskService {

    @Autowired
    private WfTaskMapper taskMapper;

    @Autowired
    private WfProcessInstanceMapper processInstanceMapper;

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
    private TaskService camundaTaskService;

    @Override
    public PageResult<WfTaskVO> pageTodoList(Integer pageNum, Integer pageSize, WfTaskQueryDTO queryDTO) {
        LambdaQueryWrapper<WfTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WfTask::getStatus, "pending", "claimed");
        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getAssigneeId())) {
                wrapper.eq(WfTask::getAssigneeId, queryDTO.getAssigneeId());
            }
            if (StringUtils.hasText(queryDTO.getTaskType())) {
                wrapper.eq(WfTask::getTaskType, queryDTO.getTaskType());
            }
            if (StringUtils.hasText(queryDTO.getBusinessType())) {
                wrapper.eq(WfTask::getBusinessType, queryDTO.getBusinessType());
            }
            if (queryDTO.getPriority() != null) {
                wrapper.eq(WfTask::getPriority, queryDTO.getPriority());
            }
            if (StringUtils.hasText(queryDTO.getKeyword())) {
                wrapper.and(w -> w.like(WfTask::getBusinessTitle, queryDTO.getKeyword())
                        .or().like(WfTask::getTaskNo, queryDTO.getKeyword()));
            }
        }
        wrapper.orderByDesc(WfTask::getPriority, WfTask::getCreateTime);

        Page<WfTask> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<WfTaskVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public PageResult<WfTaskVO> pageDoneList(Integer pageNum, Integer pageSize, WfTaskQueryDTO queryDTO) {
        LambdaQueryWrapper<WfTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WfTask::getStatus, "completed", "rejected", "terminated");
        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getAssigneeId())) {
                wrapper.eq(WfTask::getAssigneeId, queryDTO.getAssigneeId());
            }
            if (StringUtils.hasText(queryDTO.getTaskType())) {
                wrapper.eq(WfTask::getTaskType, queryDTO.getTaskType());
            }
            if (StringUtils.hasText(queryDTO.getBusinessType())) {
                wrapper.eq(WfTask::getBusinessType, queryDTO.getBusinessType());
            }
            if (StringUtils.hasText(queryDTO.getKeyword())) {
                wrapper.and(w -> w.like(WfTask::getBusinessTitle, queryDTO.getKeyword())
                        .or().like(WfTask::getTaskNo, queryDTO.getKeyword()));
            }
        }
        wrapper.orderByDesc(WfTask::getCompleteTime);

        Page<WfTask> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<WfTaskVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public PageResult<WfTaskVO> pageList(Integer pageNum, Integer pageSize, WfTaskQueryDTO queryDTO) {
        LambdaQueryWrapper<WfTask> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getAssigneeId())) {
                wrapper.eq(WfTask::getAssigneeId, queryDTO.getAssigneeId());
            }
            if (StringUtils.hasText(queryDTO.getStatus())) {
                wrapper.eq(WfTask::getStatus, queryDTO.getStatus());
            }
            if (StringUtils.hasText(queryDTO.getTaskType())) {
                wrapper.eq(WfTask::getTaskType, queryDTO.getTaskType());
            }
            if (StringUtils.hasText(queryDTO.getBusinessType())) {
                wrapper.eq(WfTask::getBusinessType, queryDTO.getBusinessType());
            }
            if (StringUtils.hasText(queryDTO.getBusinessKey())) {
                wrapper.eq(WfTask::getBusinessKey, queryDTO.getBusinessKey());
            }
            if (queryDTO.getProcessInstanceId() != null) {
                wrapper.eq(WfTask::getProcessInstanceId, queryDTO.getProcessInstanceId());
            }
            if (queryDTO.getProcessDefId() != null) {
                wrapper.eq(WfTask::getProcessDefId, queryDTO.getProcessDefId());
            }
            if (StringUtils.hasText(queryDTO.getNodeId())) {
                wrapper.eq(WfTask::getNodeId, queryDTO.getNodeId());
            }
            if (queryDTO.getPriority() != null) {
                wrapper.eq(WfTask::getPriority, queryDTO.getPriority());
            }
            if (StringUtils.hasText(queryDTO.getKeyword())) {
                wrapper.and(w -> w.like(WfTask::getBusinessTitle, queryDTO.getKeyword())
                        .or().like(WfTask::getTaskNo, queryDTO.getKeyword()));
            }
        }
        wrapper.orderByDesc(WfTask::getCreateTime);

        Page<WfTask> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<WfTaskVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public WfTaskVO getDetail(Long id) {
        WfTask task = this.getById(id);
        if (task == null) {
            return null;
        }
        WfTaskVO vo = convertToVO(task);

        WfProcessInstance instance = processInstanceMapper.selectById(task.getProcessInstanceId());
        if (instance != null) {
            WfProcessInstanceVO instanceVO = new WfProcessInstanceVO();
            BeanUtils.copyProperties(instance, instanceVO);
            vo.setProcessInstance(instanceVO);
        }

        if ("countersign".equals(task.getTaskType())) {
            WfCountersign countersign = countersignMapper.selectOne(
                    new LambdaQueryWrapper<WfCountersign>()
                            .eq(WfCountersign::getTaskId, task.getId()));
            if (countersign != null) {
                vo.setCountersign(countersignService.convertToVO(countersign));
            }
        }

        vo.setCanDelegate(canDelegate(task));
        vo.setCanAddSign(canAddSign(task));
        vo.setCanTerminate(canTerminate(task));

        return vo;
    }

    @Override
    public List<WfTaskVO> getByProcessInstanceId(Long processInstanceId) {
        List<WfTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getProcessInstanceId, processInstanceId)
                        .orderByDesc(WfTask::getCreateTime));
        return tasks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimTask(Long taskId) {
        WfTask task = this.getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"pending".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许签收");
        }

        if (StringUtils.hasText(task.getCamundaTaskId())) {
            try {
                camundaTaskService.claim(task.getCamundaTaskId(), task.getAssigneeId());
            } catch (Exception e) {
                log.warn("Camunda claim task error: {}", e.getMessage());
            }
        }

        task.setStatus("claimed");
        task.setClaimTime(LocalDateTime.now());
        this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unclaimTask(Long taskId) {
        WfTask task = this.getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"claimed".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许取消签收");
        }

        if (StringUtils.hasText(task.getCamundaTaskId())) {
            try {
                camundaTaskService.setAssignee(task.getCamundaTaskId(), null);
            } catch (Exception e) {
                log.warn("Camunda unclaim task error: {}", e.getMessage());
            }
        }

        task.setStatus("pending");
        task.setClaimTime(null);
        this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(WfApprovalDTO approvalDTO) {
        WfTask task = this.getById(approvalDTO.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"pending".equals(task.getStatus()) && !"claimed".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许审批");
        }

        WfProcessInstance instance = processInstanceMapper.selectById(task.getProcessInstanceId());
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        if (!"running".equals(instance.getStatus())) {
            throw new RuntimeException("流程实例状态不允许审批");
        }

        WfApprovalOpinion opinion = new WfApprovalOpinion();
        opinion.setProcessInstanceId(task.getProcessInstanceId());
        opinion.setTaskId(task.getId());
        opinion.setNodeId(task.getNodeId());
        opinion.setNodeName(task.getNodeName());
        opinion.setBusinessKey(task.getBusinessKey());
        opinion.setApprovalType(approvalDTO.getApprovalType());
        opinion.setApprovalResult(approvalDTO.getApprovalResult());
        opinion.setApprovalOpinion(approvalDTO.getApprovalOpinion());
        opinion.setOpinionTemplateId(approvalDTO.getOpinionTemplateId());
        opinion.setTargetUserId(approvalDTO.getTargetUserId());
        opinion.setTargetUserName(approvalDTO.getTargetUserName());
        opinion.setTargetNodeId(approvalDTO.getTargetNodeId());
        opinion.setTargetNodeName(approvalDTO.getTargetNodeName());
        opinion.setApprovalTime(LocalDateTime.now());
        if (approvalDTO.getVariables() != null && !approvalDTO.getVariables().isEmpty()) {
            opinion.setVariables(JSON.toJSONString(approvalDTO.getVariables()));
        }
        approvalOpinionService.saveOpinion(opinion);

        task.setStatus("completed");
        task.setCompleteTime(LocalDateTime.now());
        if (approvalDTO.getFormData() != null && !approvalDTO.getFormData().isEmpty()) {
            task.setFormData(JSON.toJSONString(approvalDTO.getFormData()));
            instance.setFormData(JSON.toJSONString(approvalDTO.getFormData()));
        }
        if (approvalDTO.getVariables() != null && !approvalDTO.getVariables().isEmpty()) {
            task.setVariables(JSON.toJSONString(approvalDTO.getVariables()));
            instance.setVariables(JSON.toJSONString(approvalDTO.getVariables()));
        }
        this.updateById(task);
        processInstanceMapper.updateById(instance);

        if (StringUtils.hasText(task.getCamundaTaskId())) {
            try {
                Map<String, Object> camundaVariables = new HashMap<>();
                if (approvalDTO.getVariables() != null) {
                    camundaVariables.putAll(approvalDTO.getVariables());
                }
                camundaVariables.put("approvalType", approvalDTO.getApprovalType());
                camundaVariables.put("approvalResult", approvalDTO.getApprovalResult());
                camundaTaskService.complete(task.getCamundaTaskId(), camundaVariables);
            } catch (Exception e) {
                log.error("Camunda complete task error: {}", e.getMessage(), e);
            }
        }

        String approvalType = approvalDTO.getApprovalType();
        if ("pass".equals(approvalType)) {
            handlePass(task, instance, approvalDTO);
        } else if ("reject".equals(approvalType)) {
            handleReject(task, instance, approvalDTO);
        } else if ("return".equals(approvalType)) {
            handleReturn(task, instance, approvalDTO);
        } else if ("terminate".equals(approvalType)) {
            handleTerminate(task, instance, approvalDTO);
        } else {
            throw new RuntimeException("不支持的审批类型: " + approvalType);
        }
    }

    private void handlePass(WfTask task, WfProcessInstance instance, WfApprovalDTO approvalDTO) {
        if ("countersign".equals(task.getTaskType())) {
            WfCountersign countersign = countersignMapper.selectOne(
                    new LambdaQueryWrapper<WfCountersign>()
                            .eq(WfCountersign::getTaskId, task.getId()));
            if (countersign != null) {
                countersignService.checkAndCompleteCountersign(countersign.getId());
                if ("completed".equals(countersign.getStatus())) {
                    syncInstanceFromCamunda(instance);
                }
            }
        } else {
            syncInstanceFromCamunda(instance);
        }
    }

    private void syncInstanceFromCamunda(WfProcessInstance instance) {
        if (StringUtils.hasText(instance.getCamundaProcessInstanceId())) {
            try {
                List<Task> camundaTasks = processEngineService.findCamundaTasksByProcessInstance(
                        instance.getCamundaProcessInstanceId());
                if (!camundaTasks.isEmpty()) {
                    Task currentTask = camundaTasks.get(0);
                    instance.setCurrentNodeId(currentTask.getTaskDefinitionKey());
                    instance.setCurrentNodeName(currentTask.getName());
                    processInstanceMapper.updateById(instance);
                } else {
                    org.camunda.bpm.engine.runtime.ProcessInstance camundaInstance =
                            processEngineService.findCamundaProcessInstance(instance.getCamundaProcessInstanceId());
                    if (camundaInstance == null) {
                        instance.setStatus("completed");
                        instance.setEndTime(LocalDateTime.now());
                        if (instance.getStartTime() != null) {
                            long duration = java.time.Duration.between(instance.getStartTime(), instance.getEndTime()).toMillis();
                            instance.setDuration(duration);
                        }
                        processInstanceMapper.updateById(instance);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to sync instance from Camunda: {}", e.getMessage());
            }
        }
    }

    private void handleReject(WfTask task, WfProcessInstance instance, WfApprovalDTO approvalDTO) {
        instance.setStatus("rejected");
        instance.setEndTime(LocalDateTime.now());
        if (instance.getStartTime() != null) {
            long duration = java.time.Duration.between(instance.getStartTime(), instance.getEndTime()).toMillis();
            instance.setDuration(duration);
        }
        processInstanceMapper.updateById(instance);

        if (StringUtils.hasText(instance.getCamundaProcessInstanceId())) {
            try {
                new org.camunda.bpm.engine.RuntimeService() {
                };
            } catch (Exception ignored) {}
        }

        List<WfTask> pendingTasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getProcessInstanceId, instance.getId())
                        .in(WfTask::getStatus, "pending", "claimed"));
        for (WfTask pendingTask : pendingTasks) {
            pendingTask.setStatus("rejected");
            pendingTask.setCompleteTime(LocalDateTime.now());
            taskMapper.updateById(pendingTask);
        }
    }

    private void handleReturn(WfTask task, WfProcessInstance instance, WfApprovalDTO approvalDTO) {
        if (!StringUtils.hasText(approvalDTO.getTargetNodeId())) {
            throw new RuntimeException("退回节点不能为空");
        }

        WfProcessNode targetNode = processNodeMapper.selectOne(
                new LambdaQueryWrapper<WfProcessNode>()
                        .eq(WfProcessNode::getProcessDefId, instance.getProcessDefId())
                        .eq(WfProcessNode::getNodeId, approvalDTO.getTargetNodeId()));
        if (targetNode == null) {
            throw new RuntimeException("退回节点不存在");
        }

        List<WfTask> pendingTasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getProcessInstanceId, instance.getId())
                        .in(WfTask::getStatus, "pending", "claimed"));
        for (WfTask pendingTask : pendingTasks) {
            pendingTask.setStatus("returned");
            pendingTask.setCompleteTime(LocalDateTime.now());
            taskMapper.updateById(pendingTask);
        }

        if (StringUtils.hasText(instance.getCamundaProcessInstanceId())) {
            try {
                instance.setCurrentNodeId(targetNode.getNodeId());
                instance.setCurrentNodeName(targetNode.getNodeName());
                processInstanceMapper.updateById(instance);
            } catch (Exception e) {
                log.warn("Failed to sync return in Camunda: {}", e.getMessage());
            }
        }
    }

    private void handleTerminate(WfTask task, WfProcessInstance instance, WfApprovalDTO approvalDTO) {
        instance.setStatus("terminated");
        instance.setEndTime(LocalDateTime.now());
        if (instance.getStartTime() != null) {
            long duration = java.time.Duration.between(instance.getStartTime(), instance.getEndTime()).toMillis();
            instance.setDuration(duration);
        }
        processInstanceMapper.updateById(instance);

        if (StringUtils.hasText(instance.getCamundaProcessInstanceId())) {
            try {
                processEngineService.deleteCamundaProcessInstance(instance.getCamundaProcessInstanceId(), "terminated by user");
            } catch (Exception e) {
                log.warn("Camunda delete process instance error: {}", e.getMessage());
            }
        }

        List<WfTask> pendingTasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getProcessInstanceId, instance.getId())
                        .in(WfTask::getStatus, "pending", "claimed"));
        for (WfTask pendingTask : pendingTasks) {
            pendingTask.setStatus("terminated");
            pendingTask.setCompleteTime(LocalDateTime.now());
            taskMapper.updateById(pendingTask);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(WfTaskDelegateDTO delegateDTO) {
        WfTask task = this.getById(delegateDTO.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"pending".equals(task.getStatus()) && !"claimed".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许转办");
        }

        if (StringUtils.hasText(task.getCamundaTaskId())) {
            try {
                camundaTaskService.delegateTask(task.getCamundaTaskId(), delegateDTO.getTargetUserId());
            } catch (Exception e) {
                log.warn("Camunda delegate task error: {}", e.getMessage());
            }
        }

        task.setDelegatedFromUserId(task.getAssigneeId());
        task.setDelegatedFromUserName(task.getAssigneeName());
        task.setAssigneeId(delegateDTO.getTargetUserId());
        task.setAssigneeName(delegateDTO.getTargetUserName());
        task.setStatus("pending");
        task.setClaimTime(null);
        task.setRemark(StringUtils.hasText(task.getRemark()) ? task.getRemark() + "; " + delegateDTO.getDelegateReason() : delegateDTO.getDelegateReason());
        this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSignTask(WfTaskAddSignDTO addSignDTO) {
        WfTask task = this.getById(addSignDTO.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"pending".equals(task.getStatus()) && !"claimed".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许加签");
        }

        if ("countersign".equals(task.getTaskType())) {
            WfCountersign countersign = countersignMapper.selectOne(
                    new LambdaQueryWrapper<WfCountersign>()
                            .eq(WfCountersign::getTaskId, task.getId()));
            if (countersign != null && addSignDTO.getSignUsers() != null) {
                int maxOrder = 0;
                List<WfCountersignItem> existingItems = countersignItemMapper.selectList(
                        new LambdaQueryWrapper<WfCountersignItem>()
                                .eq(WfCountersignItem::getCountersignId, countersign.getId())
                                .orderByDesc(WfCountersignItem::getSignOrder));
                if (existingItems != null && !existingItems.isEmpty()) {
                    maxOrder = existingItems.get(0).getSignOrder();
                }

                int order = maxOrder + 1;
                for (com.gov.doc.engine.dto.WfParticipantDTO signUser : addSignDTO.getSignUsers()) {
                    WfCountersignItem item = new WfCountersignItem();
                    item.setCountersignId(countersign.getId());
                    item.setProcessInstanceId(task.getProcessInstanceId());
                    item.setTaskId(task.getId());
                    item.setSignUserId(signUser.getParticipantValue());
                    item.setSignUserName(signUser.getParticipantName());
                    item.setSignType(signUser.getParticipantType());
                    item.setSignOrder(order);
                    item.setStatus("pending");
                    item.setSignSequence(order);
                    countersignItemMapper.insert(item);
                    order++;
                }

                countersign.setTotalCount(countersign.getTotalCount() + addSignDTO.getSignUsers().size());
                countersignMapper.updateById(countersign);
            }
        } else {
            if (addSignDTO.getSignUsers() != null && !addSignDTO.getSignUsers().isEmpty()) {
                for (com.gov.doc.engine.dto.WfParticipantDTO signUser : addSignDTO.getSignUsers()) {
                    WfTask newTask = new WfTask();
                    BeanUtils.copyProperties(task, newTask);
                    newTask.setId(null);
                    newTask.setTaskNo(processEngineService.generateTaskNo());
                    newTask.setAssigneeId(signUser.getParticipantValue());
                    newTask.setAssigneeName(signUser.getParticipantName());
                    newTask.setAssigneeType(signUser.getParticipantType());
                    newTask.setStatus("pending");
                    newTask.setClaimTime(null);
                    newTask.setCompleteTime(null);
                    newTask.setCamundaTaskId(null);
                    newTask.setRemark(StringUtils.hasText(addSignDTO.getAddSignReason()) ? addSignDTO.getAddSignReason() : task.getRemark());
                    taskMapper.insert(newTask);
                }
            }
        }
    }

    @Override
    public WfTaskCountVO getTaskCount(String userId) {
        WfTaskCountVO countVO = new WfTaskCountVO();

        Integer todoCount = Math.toIntExact(taskMapper.selectCount(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getAssigneeId, userId)
                        .in(WfTask::getStatus, "pending", "claimed")));
        countVO.setTodoCount(todoCount);

        Integer doneCount = Math.toIntExact(taskMapper.selectCount(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getAssigneeId, userId)
                        .in(WfTask::getStatus, "completed", "rejected", "terminated")));
        countVO.setDoneCount(doneCount);

        Integer countersignCount = Math.toIntExact(taskMapper.selectCount(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getAssigneeId, userId)
                        .eq(WfTask::getTaskType, "countersign")
                        .in(WfTask::getStatus, "pending", "claimed")));
        countVO.setCountersignCount(countersignCount);

        Integer urgentCount = Math.toIntExact(taskMapper.selectCount(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getAssigneeId, userId)
                        .in(WfTask::getStatus, "pending", "claimed")
                        .ge(WfTask::getPriority, 2)));
        countVO.setUrgentCount(urgentCount);

        countVO.setTotalCount(todoCount + doneCount);

        return countVO;
    }

    @Override
    public WfTaskVO convertToVO(WfTask task) {
        WfTaskVO vo = new WfTaskVO();
        BeanUtils.copyProperties(task, vo);

        vo.setTaskTypeName(getTaskTypeName(task.getTaskType()));
        vo.setStatusName(getStatusName(task.getStatus()));
        vo.setAssigneeTypeName(getAssigneeTypeName(task.getAssigneeType()));
        vo.setPriorityName(getPriorityName(task.getPriority()));

        if (StringUtils.hasText(task.getFormData())) {
            vo.setFormData(JSON.parseObject(task.getFormData(), Map.class));
        }
        if (StringUtils.hasText(task.getVariables())) {
            vo.setVariables(JSON.parseObject(task.getVariables(), Map.class));
        }

        return vo;
    }

    @Override
    public List<WfTaskVO> findTodoTasks(String assigneeId, Long processInstanceId) {
        List<WfTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getAssigneeId, assigneeId)
                        .eq(WfTask::getProcessInstanceId, processInstanceId)
                        .in(WfTask::getStatus, "pending", "claimed")
                        .orderByDesc(WfTask::getCreateTime));
        return tasks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private boolean canDelegate(WfTask task) {
        return "pending".equals(task.getStatus()) || "claimed".equals(task.getStatus());
    }

    private boolean canAddSign(WfTask task) {
        return "pending".equals(task.getStatus()) || "claimed".equals(task.getStatus());
    }

    private boolean canTerminate(WfTask task) {
        return "pending".equals(task.getStatus()) || "claimed".equals(task.getStatus());
    }

    private String getTaskTypeName(String taskType) {
        if ("userTask".equals(taskType)) {
            return "用户任务";
        } else if ("countersign".equals(taskType)) {
            return "会签任务";
        }
        return taskType;
    }

    private String getStatusName(String status) {
        switch (status) {
            case "pending":
                return "待办理";
            case "claimed":
                return "已签收";
            case "completed":
                return "已完成";
            case "rejected":
                return "已驳回";
            case "returned":
                return "已退回";
            case "terminated":
                return "已终止";
            case "delegated":
                return "已转办";
            default:
                return status;
        }
    }

    private String getAssigneeTypeName(String assigneeType) {
        if ("user".equals(assigneeType)) {
            return "用户";
        } else if ("role".equals(assigneeType)) {
            return "角色";
        } else if ("dept".equals(assigneeType)) {
            return "部门";
        }
        return assigneeType;
    }

    private String getPriorityName(Integer priority) {
        if (priority == null) {
            return "普通";
        }
        switch (priority) {
            case 0:
                return "低";
            case 1:
                return "普通";
            case 2:
                return "高";
            case 3:
                return "紧急";
            default:
                return "普通";
        }
    }
}
