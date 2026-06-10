package com.gov.doc.engine.listener;

import com.gov.doc.engine.entity.WfProcessHistory;
import com.gov.doc.engine.entity.WfProcessInstance;
import com.gov.doc.engine.entity.WfTask;
import com.gov.doc.engine.mapper.WfProcessHistoryMapper;
import com.gov.doc.engine.mapper.WfProcessInstanceMapper;
import com.gov.doc.engine.mapper.WfTaskMapper;
import com.gov.doc.engine.service.WfProcessHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class CamundaProcessListener implements TaskListener {

    @Autowired
    private WfProcessInstanceMapper processInstanceMapper;

    @Autowired
    private WfTaskMapper taskMapper;

    @Autowired
    private WfProcessHistoryService processHistoryService;

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        try {
            if ("create".equals(eventName)) {
                onTaskCreate(delegateTask);
            } else if ("complete".equals(eventName)) {
                onTaskComplete(delegateTask);
            }
        } catch (Exception e) {
            log.error("Camunda process listener error: {}", e.getMessage(), e);
        }
    }

    private void onTaskCreate(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        WfProcessInstance instance = findLocalInstance(processInstanceId);
        if (instance == null) {
            return;
        }

        WfTask task = new WfTask();
        task.setTaskNo("T" + System.currentTimeMillis() + String.format("%06d", (int) (Math.random() * 1000000)));
        task.setProcessInstanceId(instance.getId());
        task.setProcessDefId(instance.getProcessDefId());
        task.setNodeId(delegateTask.getTaskDefinitionKey());
        task.setNodeName(delegateTask.getName());
        task.setTaskType("userTask");
        task.setBusinessKey(instance.getBusinessKey());
        task.setBusinessType(instance.getBusinessType());
        task.setBusinessTitle(instance.getBusinessTitle());
        task.setStatus("pending");
        if (delegateTask.getAssignee() != null) {
            task.setAssigneeId(delegateTask.getAssignee());
            task.setAssigneeType("user");
        }
        taskMapper.insert(task);

        WfProcessHistory history = new WfProcessHistory();
        history.setProcessInstanceId(instance.getId());
        history.setNodeId(delegateTask.getTaskDefinitionKey());
        history.setNodeName(delegateTask.getName());
        history.setNodeType("userTask");
        history.setOperatorId(delegateTask.getAssignee());
        history.setOperationType("enter");
        history.setEnterTime(LocalDateTime.now());
        history.setStatus("active");
        history.setTaskId(task.getId());
        processHistoryService.recordHistory(history);

        instance.setCurrentNodeId(delegateTask.getTaskDefinitionKey());
        instance.setCurrentNodeName(delegateTask.getName());
        processInstanceMapper.updateById(instance);
    }

    private void onTaskComplete(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        WfProcessInstance instance = findLocalInstance(processInstanceId);
        if (instance == null) {
            return;
        }

        processHistoryService.completeHistory(instance.getId(), delegateTask.getTaskDefinitionKey(), "leave");
    }

    private WfProcessInstance findLocalInstance(String camundaProcessInstanceId) {
        return processInstanceMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfProcessInstance>()
                        .eq(WfProcessInstance::getCamundaProcessInstanceId, camundaProcessInstanceId)
                        .last("LIMIT 1"));
    }
}
