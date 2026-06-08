package com.gov.doc.engine.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.entity.*;
import com.gov.doc.engine.enums.WfNodeTypeEnum;
import com.gov.doc.engine.mapper.*;
import com.gov.doc.engine.listener.DocProcessStatusListener;
import com.gov.doc.engine.service.WfCountersignService;
import com.gov.doc.engine.service.WfParticipantResolverService;
import com.gov.doc.engine.service.WfProcessEngineService;
import com.gov.doc.engine.service.WfProcessHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WfProcessEngineServiceImpl extends ServiceImpl<WfProcessInstanceMapper, WfProcessInstance> implements WfProcessEngineService {

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
    private WfCountersignService countersignService;

    @Autowired
    private WfProcessHistoryService processHistoryService;

    @Autowired
    private WfParticipantResolverService participantResolverService;

    @Autowired(required = false)
    private DocProcessStatusListener docProcessStatusListener;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
        if (node == null) {
            throw new RuntimeException("流程节点不存在");
        }
        String nodeType = node.getNodeType();
        if (WfNodeTypeEnum.START.getCode().equals(nodeType)) {
            executeStartNode(instance, node, variables);
        } else if (WfNodeTypeEnum.END.getCode().equals(nodeType)) {
            executeEndNode(instance, node, variables);
        } else if (WfNodeTypeEnum.USER_TASK.getCode().equals(nodeType)) {
            executeUserTask(instance, node, variables);
        } else if (WfNodeTypeEnum.COUNTERSIGN.getCode().equals(nodeType)) {
            executeCountersign(instance, node, variables);
        } else if (WfNodeTypeEnum.PARALLEL_GATEWAY.getCode().equals(nodeType)) {
            executeParallelGateway(instance, node, variables);
        } else if (WfNodeTypeEnum.EXCLUSIVE_GATEWAY.getCode().equals(nodeType)) {
            executeExclusiveGateway(instance, node, variables);
        } else if (WfNodeTypeEnum.INCLUSIVE_GATEWAY.getCode().equals(nodeType)) {
            executeInclusiveGateway(instance, node, variables);
        } else {
            throw new RuntimeException("不支持的节点类型: " + nodeType);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enterNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
        WfProcessHistory history = new WfProcessHistory();
        history.setProcessInstanceId(instance.getId());
        history.setNodeId(node.getNodeId());
        history.setNodeName(node.getNodeName());
        history.setNodeType(node.getNodeType());
        history.setOperatorId(instance.getStartUserId());
        history.setOperatorName(instance.getStartUserName());
        history.setOperationType("enter");
        history.setEnterTime(LocalDateTime.now());
        history.setStatus("active");
        if (variables != null && !variables.isEmpty()) {
            history.setVariables(JSON.toJSONString(variables));
        }
        processHistoryService.recordHistory(history);

        instance.setCurrentNodeId(node.getNodeId());
        instance.setCurrentNodeName(node.getNodeName());
        processInstanceMapper.updateById(instance);

        if (docProcessStatusListener != null) {
            String operatorId = history.getOperatorId();
            String operatorName = history.getOperatorName();
            docProcessStatusListener.updateDocStatusByNode(
                    instance.getId(), node, operatorId, operatorName);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveNode(WfProcessInstance instance, WfProcessNode currentNode, Map<String, Object> variables) {
        processHistoryService.completeHistory(instance.getId(), currentNode.getNodeId(), "leave");

        if (WfNodeTypeEnum.PARALLEL_GATEWAY.getCode().equals(currentNode.getNodeType())) {
            return;
        }

        WfProcessNode nextNode = getNextNode(instance, currentNode, variables);
        if (nextNode != null) {
            executeNode(instance, nextNode, variables);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeStartNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
        enterNode(instance, node, variables);
        leaveNode(instance, node, variables);
        WfProcessNode nextNode = getNextNode(instance, node, variables);
        if (nextNode != null) {
            executeNode(instance, nextNode, variables);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeEndNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
        enterNode(instance, node, variables);
        leaveNode(instance, node, variables);

        instance.setStatus("completed");
        instance.setEndTime(LocalDateTime.now());
        if (instance.getStartTime() != null) {
            long duration = java.time.Duration.between(instance.getStartTime(), instance.getEndTime()).toMillis();
            instance.setDuration(duration);
        }
        processInstanceMapper.updateById(instance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeUserTask(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
        enterNode(instance, node, variables);

        WfTask task = new WfTask();
        task.setTaskNo(generateTaskNo());
        task.setProcessInstanceId(instance.getId());
        task.setProcessDefId(instance.getProcessDefId());
        task.setNodeId(node.getNodeId());
        task.setNodeName(node.getNodeName());
        task.setTaskType("userTask");
        task.setBusinessKey(instance.getBusinessKey());
        task.setBusinessType(instance.getBusinessType());
        task.setBusinessTitle(instance.getBusinessTitle());
        task.setStatus("pending");
        task.setFormData(instance.getFormData());
        if (variables != null && !variables.isEmpty()) {
            task.setVariables(JSON.toJSONString(variables));
        }

        List<WfParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<WfParticipant>()
                        .eq(WfParticipant::getProcessDefId, instance.getProcessDefId())
                        .eq(WfParticipant::getNodeId, node.getNodeId())
                        .orderByAsc(WfParticipant::getSortOrder));

        if (participants != null && !participants.isEmpty()) {
            WfParticipantResolverService.ResolvedParticipant resolved = 
                    participantResolverService.resolveParticipant(participants.get(0), variables);
            if (resolved != null) {
                task.setAssigneeType(resolved.getParticipantType());
                task.setAssigneeId(resolved.getUserId());
                task.setAssigneeName(resolved.getUserName());
            }
        }

        if (StringUtils.hasText(node.getNodeConfig())) {
            Map<String, Object> nodeConfig = JSON.parseObject(node.getNodeConfig(), Map.class);
            if (nodeConfig != null && nodeConfig.get("priority") != null) {
                task.setPriority(Integer.parseInt(nodeConfig.get("priority").toString()));
            }
            if (nodeConfig != null && nodeConfig.get("dueDays") != null) {
                int dueDays = Integer.parseInt(nodeConfig.get("dueDays").toString());
                task.setDueTime(LocalDateTime.now().plusDays(dueDays));
            }
        }

        taskMapper.insert(task);

        WfProcessHistory history = processHistoryService.getActiveHistory(instance.getId(), node.getNodeId());
        if (history != null) {
            history.setTaskId(task.getId());
            processHistoryMapper.updateById(history);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeCountersign(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
        enterNode(instance, node, variables);

        WfTask task = new WfTask();
        task.setTaskNo(generateTaskNo());
        task.setProcessInstanceId(instance.getId());
        task.setProcessDefId(instance.getProcessDefId());
        task.setNodeId(node.getNodeId());
        task.setNodeName(node.getNodeName());
        task.setTaskType("countersign");
        task.setBusinessKey(instance.getBusinessKey());
        task.setBusinessType(instance.getBusinessType());
        task.setBusinessTitle(instance.getBusinessTitle());
        task.setStatus("pending");
        task.setFormData(instance.getFormData());
        if (variables != null && !variables.isEmpty()) {
            task.setVariables(JSON.toJSONString(variables));
        }
        taskMapper.insert(task);

        WfCountersign countersign = new WfCountersign();
        countersign.setCountersignNo(generateCountersignNo());
        countersign.setProcessInstanceId(instance.getId());
        countersign.setProcessDefId(instance.getProcessDefId());
        countersign.setNodeId(node.getNodeId());
        countersign.setNodeName(node.getNodeName());
        countersign.setTaskId(task.getId());
        countersign.setBusinessKey(instance.getBusinessKey());
        countersign.setBusinessType(instance.getBusinessType());
        countersign.setBusinessTitle(instance.getBusinessTitle());
        countersign.setStatus("active");
        countersign.setStartTime(LocalDateTime.now());

        if (StringUtils.hasText(node.getNodeConfig())) {
            Map<String, Object> nodeConfig = JSON.parseObject(node.getNodeConfig(), Map.class);
            if (nodeConfig != null) {
                if (nodeConfig.get("countersignType") != null) {
                    countersign.setCountersignType(nodeConfig.get("countersignType").toString());
                }
                if (nodeConfig.get("voteType") != null) {
                    countersign.setVoteType(nodeConfig.get("voteType").toString());
                }
                if (nodeConfig.get("passPercentage") != null) {
                    countersign.setPassPercentage(Integer.parseInt(nodeConfig.get("passPercentage").toString()));
                }
                if (nodeConfig.get("signOrder") != null) {
                    countersign.setSignOrder(nodeConfig.get("signOrder").toString());
                }
            }
        }

        List<WfParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<WfParticipant>()
                        .eq(WfParticipant::getProcessDefId, instance.getProcessDefId())
                        .eq(WfParticipant::getNodeId, node.getNodeId())
                        .orderByAsc(WfParticipant::getSortOrder));

        countersign.setTotalCount(participants != null ? participants.size() : 0);
        countersign.setSignedCount(0);
        countersign.setPassedCount(0);
        countersign.setRejectedCount(0);
        countersign.setAbstainedCount(0);
        countersignMapper.insert(countersign);

        if (participants != null && !participants.isEmpty()) {
            List<WfParticipantResolverService.ResolvedParticipant> resolvedList = 
                    participantResolverService.resolveParticipants(participants, variables);
            countersign.setTotalCount(resolvedList.size());
            int order = 1;
            for (WfParticipantResolverService.ResolvedParticipant resolved : resolvedList) {
                WfCountersignItem item = new WfCountersignItem();
                item.setCountersignId(countersign.getId());
                item.setProcessInstanceId(instance.getId());
                item.setTaskId(task.getId());
                item.setSignUserId(resolved.getUserId());
                item.setSignUserName(resolved.getUserName());
                item.setSignType(resolved.getParticipantType());
                item.setSignOrder(order);
                item.setStatus("pending");
                item.setSignSequence(order);
                item.setParticipantType(resolved.getParticipantType());
                item.setParticipantValue(resolved.getParticipantValue());
                item.setParticipantName(resolved.getParticipantName());
                countersignItemMapper.insert(item);
                order++;
            }
        }

        countersignService.startCountersign(countersign);

        WfProcessHistory history = processHistoryService.getActiveHistory(instance.getId(), node.getNodeId());
        if (history != null) {
            history.setTaskId(task.getId());
            processHistoryMapper.updateById(history);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeParallelGateway(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
        List<WfProcessEdge> incomingEdges = processEdgeMapper.selectList(
                new LambdaQueryWrapper<WfProcessEdge>()
                        .eq(WfProcessEdge::getProcessDefId, instance.getProcessDefId())
                        .eq(WfProcessEdge::getTargetNodeId, node.getNodeId()));

        List<WfProcessEdge> outgoingEdges = processEdgeMapper.selectList(
                new LambdaQueryWrapper<WfProcessEdge>()
                        .eq(WfProcessEdge::getProcessDefId, instance.getProcessDefId())
                        .eq(WfProcessEdge::getSourceNodeId, node.getNodeId())
                        .orderByAsc(WfProcessEdge::getSortOrder));

        if (incomingEdges.size() <= 1) {
            enterNode(instance, node, variables);
            processHistoryService.completeHistory(instance.getId(), node.getNodeId(), "leave");

            for (WfProcessEdge edge : outgoingEdges) {
                WfProcessNode targetNode = processNodeMapper.selectOne(
                        new LambdaQueryWrapper<WfProcessNode>()
                                .eq(WfProcessNode::getProcessDefId, instance.getProcessDefId())
                                .eq(WfProcessNode::getNodeId, edge.getTargetNodeId()));
                if (targetNode != null) {
                    executeNode(instance, targetNode, variables);
                }
            }
        } else {
            enterNode(instance, node, variables);

            boolean allBranchesCompleted = checkAllParallelBranchesCompleted(instance, node, incomingEdges);
            if (allBranchesCompleted) {
                processHistoryService.completeHistory(instance.getId(), node.getNodeId(), "leave");

                WfProcessNode nextNode = getNextNode(instance, node, variables);
                if (nextNode != null) {
                    executeNode(instance, nextNode, variables);
                }
            }
        }
    }

    private boolean checkAllParallelBranchesCompleted(WfProcessInstance instance, WfProcessNode joinNode, List<WfProcessEdge> incomingEdges) {
        for (WfProcessEdge edge : incomingEdges) {
            WfProcessHistory history = processHistoryService.getCompletedHistory(
                    instance.getId(), edge.getSourceNodeId());
            if (history == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeExclusiveGateway(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
        enterNode(instance, node, variables);
        leaveNode(instance, node, variables);

        List<WfProcessEdge> outgoingEdges = processEdgeMapper.selectList(
                new LambdaQueryWrapper<WfProcessEdge>()
                        .eq(WfProcessEdge::getProcessDefId, instance.getProcessDefId())
                        .eq(WfProcessEdge::getSourceNodeId, node.getNodeId())
                        .orderByAsc(WfProcessEdge::getSortOrder));

        WfProcessNode nextNode = null;
        for (WfProcessEdge edge : outgoingEdges) {
            if (!StringUtils.hasText(edge.getConditionExpression()) || evaluateCondition(edge.getConditionExpression(), variables)) {
                nextNode = processNodeMapper.selectOne(
                        new LambdaQueryWrapper<WfProcessNode>()
                                .eq(WfProcessNode::getProcessDefId, instance.getProcessDefId())
                                .eq(WfProcessNode::getNodeId, edge.getTargetNodeId()));
                break;
            }
        }

        if (nextNode == null && !outgoingEdges.isEmpty()) {
            WfProcessEdge defaultEdge = outgoingEdges.get(outgoingEdges.size() - 1);
            nextNode = processNodeMapper.selectOne(
                    new LambdaQueryWrapper<WfProcessNode>()
                            .eq(WfProcessNode::getProcessDefId, instance.getProcessDefId())
                            .eq(WfProcessNode::getNodeId, defaultEdge.getTargetNodeId()));
        }

        if (nextNode != null) {
            executeNode(instance, nextNode, variables);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeInclusiveGateway(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
        enterNode(instance, node, variables);
        leaveNode(instance, node, variables);

        List<WfProcessEdge> outgoingEdges = processEdgeMapper.selectList(
                new LambdaQueryWrapper<WfProcessEdge>()
                        .eq(WfProcessEdge::getProcessDefId, instance.getProcessDefId())
                        .eq(WfProcessEdge::getSourceNodeId, node.getNodeId())
                        .orderByAsc(WfProcessEdge::getSortOrder));

        for (WfProcessEdge edge : outgoingEdges) {
            if (!StringUtils.hasText(edge.getConditionExpression()) || evaluateCondition(edge.getConditionExpression(), variables)) {
                WfProcessNode targetNode = processNodeMapper.selectOne(
                        new LambdaQueryWrapper<WfProcessNode>()
                                .eq(WfProcessNode::getProcessDefId, instance.getProcessDefId())
                                .eq(WfProcessNode::getNodeId, edge.getTargetNodeId()));
                if (targetNode != null) {
                    executeNode(instance, targetNode, variables);
                }
            }
        }
    }

    @Override
    public WfProcessNode getNextNode(WfProcessInstance instance, WfProcessNode currentNode, Map<String, Object> variables) {
        List<WfProcessEdge> outgoingEdges = processEdgeMapper.selectList(
                new LambdaQueryWrapper<WfProcessEdge>()
                        .eq(WfProcessEdge::getProcessDefId, instance.getProcessDefId())
                        .eq(WfProcessEdge::getSourceNodeId, currentNode.getNodeId())
                        .orderByAsc(WfProcessEdge::getSortOrder));

        if (outgoingEdges == null || outgoingEdges.isEmpty()) {
            return null;
        }

        for (WfProcessEdge edge : outgoingEdges) {
            if (!StringUtils.hasText(edge.getConditionExpression()) || evaluateCondition(edge.getConditionExpression(), variables)) {
                return processNodeMapper.selectOne(
                        new LambdaQueryWrapper<WfProcessNode>()
                                .eq(WfProcessNode::getProcessDefId, instance.getProcessDefId())
                                .eq(WfProcessNode::getNodeId, edge.getTargetNodeId()));
            }
        }

        WfProcessEdge defaultEdge = outgoingEdges.get(outgoingEdges.size() - 1);
        return processNodeMapper.selectOne(
                new LambdaQueryWrapper<WfProcessNode>()
                        .eq(WfProcessNode::getProcessDefId, instance.getProcessDefId())
                        .eq(WfProcessNode::getNodeId, defaultEdge.getTargetNodeId()));
    }

    @Override
    public boolean evaluateCondition(String conditionExpression, Map<String, Object> variables) {
        if (!StringUtils.hasText(conditionExpression)) {
            return true;
        }
        if (variables == null || variables.isEmpty()) {
            return false;
        }

        String expr = conditionExpression.trim();
        try {
            if (expr.contains("==")) {
                String[] parts = expr.split("==");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim().replace("'", "").replace("\"", "");
                    Object varValue = variables.get(key);
                    return varValue != null && varValue.toString().equals(value);
                }
            } else if (expr.contains("!=")) {
                String[] parts = expr.split("!=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim().replace("'", "").replace("\"", "");
                    Object varValue = variables.get(key);
                    return varValue == null || !varValue.toString().equals(value);
                }
            } else if (expr.contains(">=")) {
                String[] parts = expr.split(">=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    double value = Double.parseDouble(parts[1].trim());
                    Object varValue = variables.get(key);
                    return varValue != null && Double.parseDouble(varValue.toString()) >= value;
                }
            } else if (expr.contains("<=")) {
                String[] parts = expr.split("<=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    double value = Double.parseDouble(parts[1].trim());
                    Object varValue = variables.get(key);
                    return varValue != null && Double.parseDouble(varValue.toString()) <= value;
                }
            } else if (expr.contains(">")) {
                String[] parts = expr.split(">");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    double value = Double.parseDouble(parts[1].trim());
                    Object varValue = variables.get(key);
                    return varValue != null && Double.parseDouble(varValue.toString()) > value;
                }
            } else if (expr.contains("<")) {
                String[] parts = expr.split("<");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    double value = Double.parseDouble(parts[1].trim());
                    Object varValue = variables.get(key);
                    return varValue != null && Double.parseDouble(varValue.toString()) < value;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    @Override
    public String generateTaskNo() {
        return "T" + LocalDateTime.now().format(FORMATTER) + String.format("%06d", RANDOM.nextInt(1000000));
    }

    @Override
    public String generateInstanceNo() {
        return "I" + LocalDateTime.now().format(FORMATTER) + String.format("%06d", RANDOM.nextInt(1000000));
    }

    @Override
    public String generateCountersignNo() {
        return "C" + LocalDateTime.now().format(FORMATTER) + String.format("%06d", RANDOM.nextInt(1000000));
    }
}
