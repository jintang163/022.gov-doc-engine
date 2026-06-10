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
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class WfProcessEngineServiceImpl extends ServiceImpl<WfProcessInstanceMapper, WfProcessInstance> implements WfProcessEngineService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private org.camunda.bpm.engine.TaskService camundaTaskService;

    @Autowired
    private HistoryService historyService;

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
    public void executeNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
    }

    @Override
    public void leaveNode(WfProcessInstance instance, WfProcessNode currentNode, Map<String, Object> variables) {
    }

    @Override
    public void enterNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
    }

    @Override
    public void executeStartNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
    }

    @Override
    public void executeEndNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
    }

    @Override
    public void executeUserTask(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
    }

    @Override
    public void executeCountersign(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
    }

    @Override
    public void executeParallelGateway(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
    }

    @Override
    public void executeExclusiveGateway(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
    }

    @Override
    public void executeInclusiveGateway(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables) {
    }

    @Override
    public WfProcessNode getNextNode(WfProcessInstance instance, WfProcessNode currentNode, Map<String, Object> variables) {
        return null;
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
            } else if (expr.contains(">") && !expr.contains(">=")) {
                String[] parts = expr.split(">");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    double value = Double.parseDouble(parts[1].trim());
                    Object varValue = variables.get(key);
                    return varValue != null && Double.parseDouble(varValue.toString()) > value;
                }
            } else if (expr.contains("<") && !expr.contains("<=")) {
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

    public ProcessInstance startCamundaProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
    }

    public void deployProcessDefinition(String resourceName, String bpmnXml) {
        repositoryService.createDeployment()
                .addString(resourceName, bpmnXml)
                .deploy();
    }

    public void completeCamundaTask(String camundaTaskId, Map<String, Object> variables) {
        camundaTaskService.complete(camundaTaskId, variables);
    }

    public void claimCamundaTask(String camundaTaskId, String userId) {
        camundaTaskService.claim(camundaTaskId, userId);
    }

    public void unclaimCamundaTask(String camundaTaskId) {
        camundaTaskService.setAssignee(camundaTaskId, null);
    }

    public void delegateCamundaTask(String camundaTaskId, String userId) {
        camundaTaskService.delegateTask(camundaTaskId, userId);
    }

    public ProcessInstance findCamundaProcessInstance(String camundaProcessInstanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(camundaProcessInstanceId)
                .singleResult();
    }

    public Task findCamundaTask(String camundaTaskId) {
        return camundaTaskService.createTaskQuery()
                .taskId(camundaTaskId)
                .singleResult();
    }

    public List<Task> findCamundaTasksByProcessInstance(String camundaProcessInstanceId) {
        return camundaTaskService.createTaskQuery()
                .processInstanceId(camundaProcessInstanceId)
                .list();
    }

    public void suspendCamundaProcessInstance(String camundaProcessInstanceId) {
        runtimeService.suspendProcessInstanceById(camundaProcessInstanceId);
    }

    public void activateCamundaProcessInstance(String camundaProcessInstanceId) {
        runtimeService.activateProcessInstanceById(camundaProcessInstanceId);
    }

    public void deleteCamundaProcessInstance(String camundaProcessInstanceId, String reason) {
        runtimeService.deleteProcessInstance(camundaProcessInstanceId, reason);
    }

    public ProcessDefinition findCamundaProcessDefinition(String processDefinitionKey) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
    }

    public List<ProcessDefinition> listCamundaProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .list();
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
