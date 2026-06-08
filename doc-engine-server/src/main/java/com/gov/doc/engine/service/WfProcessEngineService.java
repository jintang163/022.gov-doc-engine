package com.gov.doc.engine.service;

import com.gov.doc.engine.entity.WfProcessInstance;
import com.gov.doc.engine.entity.WfProcessNode;

import java.util.Map;

public interface WfProcessEngineService {

    void executeNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables);

    void leaveNode(WfProcessInstance instance, WfProcessNode currentNode, Map<String, Object> variables);

    void enterNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables);

    void executeStartNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables);

    void executeEndNode(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables);

    void executeUserTask(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables);

    void executeCountersign(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables);

    void executeParallelGateway(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables);

    void executeExclusiveGateway(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables);

    void executeInclusiveGateway(WfProcessInstance instance, WfProcessNode node, Map<String, Object> variables);

    WfProcessNode getNextNode(WfProcessInstance instance, WfProcessNode currentNode, Map<String, Object> variables);

    boolean evaluateCondition(String conditionExpression, Map<String, Object> variables);

    String generateTaskNo();

    String generateInstanceNo();

    String generateCountersignNo();
}
