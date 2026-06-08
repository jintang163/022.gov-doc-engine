package com.gov.doc.engine.listener;

import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.entity.WfProcessNode;
import com.gov.doc.engine.enums.DocStatusEnum;
import com.gov.doc.engine.enums.WfNodeTypeEnum;
import com.gov.doc.engine.mapper.DocDocumentMapper;
import com.gov.doc.engine.service.DocStatusMachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class DocProcessStatusListener implements DocStatusMachineService.StatusTransitionListener {

    @Autowired
    private DocStatusMachineService statusMachineService;

    @Autowired
    private DocDocumentMapper docDocumentMapper;

    @PostConstruct
    public void init() {
        if (statusMachineService instanceof com.gov.doc.engine.service.impl.DocStatusMachineServiceImpl) {
            ((com.gov.doc.engine.service.impl.DocStatusMachineServiceImpl) statusMachineService)
                    .registerListener(this);
            log.info("DocProcessStatusListener registered successfully");
        }
    }

    @Override
    public void onTransition(Long docId, String fromStatus, String toStatus,
                           String operatorId, String operatorName, String remark) {
        log.info("Document {} status transition: {} -> {}", docId, fromStatus, toStatus);
    }

    public String mapNodeTypeToDocStatus(WfProcessNode node) {
        if (node == null) {
            return null;
        }

        String nodeType = node.getNodeType();
        String nodeName = node.getNodeName();

        if (nodeName != null) {
            if (nodeName.contains("会签") || nodeName.contains("会审")) {
                return DocStatusEnum.COUNTERSIGNING.getCode();
            }
            if (nodeName.contains("签发") || nodeName.contains("签字")) {
                return DocStatusEnum.PENDING_SIGN.getCode();
            }
            if (nodeName.contains("审核") || nodeName.contains("审批") || nodeName.contains("复核")) {
                return DocStatusEnum.REVIEWING.getCode();
            }
        }

        if (WfNodeTypeEnum.COUNTERSIGN.getCode().equals(nodeType)) {
            return DocStatusEnum.COUNTERSIGNING.getCode();
        }

        return DocStatusEnum.REVIEWING.getCode();
    }

    public void updateDocStatusByNode(Long processInstanceId, WfProcessNode currentNode,
                                       String operatorId, String operatorName) {
        if (processInstanceId == null || currentNode == null) {
            return;
        }

        try {
            DocDocument doc = docDocumentMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DocDocument>()
                            .eq(DocDocument::getProcessInstanceId, processInstanceId.toString())
                            .last("LIMIT 1")
            );

            if (doc == null) {
                return;
            }

            String targetStatus = mapNodeTypeToDocStatus(currentNode);
            if (targetStatus == null) {
                return;
            }

            String currentStatus = doc.getStatus();
            if (targetStatus.equals(currentStatus)) {
                return;
            }

            if (DocStatusEnum.canTransition(currentStatus, targetStatus)) {
                statusMachineService.transitionWithReason(
                        doc.getId(),
                        targetStatus,
                        "流程节点流转：" + currentNode.getNodeName(),
                        operatorId,
                        operatorName,
                        "节点ID: " + currentNode.getNodeId()
                );
                log.info("Document {} status updated to {} by process node {}",
                        doc.getId(), targetStatus, currentNode.getNodeId());
            } else {
                log.warn("Cannot transition document {} from {} to {} via process node",
                        doc.getId(), currentStatus, targetStatus);
            }
        } catch (Exception e) {
            log.error("Failed to update document status by process node", e);
        }
    }

    public void updateDocStatusToDraft(Long docId, String operatorId, String operatorName) {
        try {
            DocDocument doc = docDocumentMapper.selectById(docId);
            if (doc != null && doc.getStatus() == null) {
                doc.setStatus(DocStatusEnum.DRAFT.getCode());
                docDocumentMapper.updateById(doc);

                com.gov.doc.engine.entity.DocStatusLog statusLog =
                        new com.gov.doc.engine.entity.DocStatusLog();
                statusLog.setDocId(docId);
                statusLog.setFromStatus(null);
                statusLog.setToStatus(DocStatusEnum.DRAFT.getCode());
                statusLog.setToStatusName(DocStatusEnum.DRAFT.getName());
                statusLog.setTransitionReason("创建公文");
                statusLog.setOperatorId(operatorId);
                statusLog.setOperatorName(operatorName);
                statusLog.setOperationTime(java.time.LocalDateTime.now());

                com.gov.doc.engine.mapper.DocStatusLogMapper statusLogMapper =
                        getStatusLogMapper();
                if (statusLogMapper != null) {
                    statusLogMapper.insert(statusLog);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update document status to draft", e);
        }
    }

    @Autowired(required = false)
    private com.gov.doc.engine.mapper.DocStatusLogMapper statusLogMapper;

    private com.gov.doc.engine.mapper.DocStatusLogMapper getStatusLogMapper() {
        return statusLogMapper;
    }
}
