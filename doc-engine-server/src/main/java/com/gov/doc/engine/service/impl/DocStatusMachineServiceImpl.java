package com.gov.doc.engine.service.impl;

import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.entity.DocStatusLog;
import com.gov.doc.engine.enums.DocStatusEnum;
import com.gov.doc.engine.mapper.DocDocumentMapper;
import com.gov.doc.engine.mapper.DocStatusLogMapper;
import com.gov.doc.engine.service.DocStatusMachineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class DocStatusMachineServiceImpl implements DocStatusMachineService {

    @Autowired
    private DocDocumentMapper docDocumentMapper;

    @Autowired
    private DocStatusLogMapper docStatusLogMapper;

    private final List<StatusTransitionListener> listeners = new ArrayList<>();

    public void registerListener(StatusTransitionListener listener) {
        listeners.add(listener);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int normalizeStatusForExistingDocs(String operatorId, String operatorName) {
        LambdaQueryWrapper<DocDocument> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .isNull(DocDocument::getStatus)
                .or()
                .eq(DocDocument::getStatus, "")
                .or()
                .eq(DocDocument::getStatus, "0"));
        List<DocDocument> docs = docDocumentMapper.selectList(queryWrapper);

        int count = 0;
        for (DocDocument doc : docs) {
            try {
                doc.setStatus(DocStatusEnum.DRAFT.getCode());
                doc.setUpdateBy(operatorId);
                doc.setUpdateTime(LocalDateTime.now());
                docDocumentMapper.updateById(doc);

                DocStatusLog statusLog = new DocStatusLog();
                statusLog.setDocId(doc.getId());
                statusLog.setFromStatus(doc.getStatus());
                statusLog.setToStatus(DocStatusEnum.DRAFT.getCode());
                statusLog.setToStatusName(DocStatusEnum.DRAFT.getName());
                statusLog.setTransitionReason("存量数据标准化");
                statusLog.setOperatorId(operatorId);
                statusLog.setOperatorName(operatorName);
                statusLog.setOperationTime(LocalDateTime.now());
                statusLog.setRemark("系统自动将状态统一为起草");
                docStatusLogMapper.insert(statusLog);

                count++;
            } catch (Exception e) {
                log.error("Failed to normalize status for doc: {}", doc.getId(), e);
            }
        }

        log.info("Normalized status for {} documents", count);
        return count;
    }

    @Override
    public boolean canTransition(Long docId, String toStatus, String operatorId, String operatorName) {
        if (!DocStatusEnum.isValidStatus(toStatus)) {
            log.warn("Invalid target status: {}", toStatus);
            return false;
        }

        DocDocument doc = docDocumentMapper.selectById(docId);
        if (doc == null) {
            log.warn("Document not found: {}", docId);
            return false;
        }

        String currentStatus = doc.getStatus();
        if (!DocStatusEnum.canTransition(currentStatus, toStatus)) {
            log.warn("Illegal status transition: {} -> {} for document: {}",
                    currentStatus, toStatus, docId);
            return false;
        }

        return true;
    }

    @Override
    public String getCurrentStatus(Long docId) {
        DocDocument doc = docDocumentMapper.selectById(docId);
        return doc != null ? doc.getStatus() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transition(Long docId, String toStatus, String operatorId, String operatorName, String remark) {
        transitionWithReason(docId, toStatus, null, operatorId, operatorName, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transitionWithReason(Long docId, String toStatus, String transitionReason,
                                 String operatorId, String operatorName, String remark) {
        if (!StringUtils.hasText(toStatus)) {
            throw new IllegalArgumentException("目标状态不能为空");
        }

        if (!DocStatusEnum.isValidStatus(toStatus)) {
            throw new IllegalArgumentException("无效的状态: " + toStatus);
        }

        DocDocument doc = docDocumentMapper.selectById(docId);
        if (doc == null) {
            throw new IllegalArgumentException("公文不存在: " + docId);
        }

        String fromStatus = doc.getStatus();

        if (toStatus.equals(fromStatus)) {
            log.info("Document {} already in status {}, skip transition", docId, toStatus);
            return;
        }

        if (!DocStatusEnum.canTransition(fromStatus, toStatus)) {
            throw new IllegalStateException(
                    String.format("非法状态跳转: %s(%s) -> %s(%s)",
                            fromStatus, DocStatusEnum.getNameByCode(fromStatus),
                            toStatus, DocStatusEnum.getNameByCode(toStatus))
            );
        }

        doc.setStatus(toStatus);
        doc.setUpdateBy(operatorId);
        doc.setUpdateTime(LocalDateTime.now());
        docDocumentMapper.updateById(doc);

        DocStatusLog statusLog = new DocStatusLog();
        statusLog.setDocId(docId);
        statusLog.setFromStatus(fromStatus);
        statusLog.setToStatus(toStatus);
        statusLog.setFromStatusName(DocStatusEnum.getNameByCode(fromStatus));
        statusLog.setToStatusName(DocStatusEnum.getNameByCode(toStatus));
        statusLog.setTransitionReason(transitionReason);
        statusLog.setOperatorId(operatorId);
        statusLog.setOperatorName(operatorName);
        statusLog.setOperationTime(LocalDateTime.now());
        statusLog.setRemark(remark);
        docStatusLogMapper.insert(statusLog);

        log.info("Document {} status transition: {} -> {}, operator: {}",
                docId, fromStatus, toStatus, operatorName);

        for (StatusTransitionListener listener : listeners) {
            try {
                listener.onTransition(docId, fromStatus, toStatus, operatorId, operatorName, remark);
            } catch (Exception e) {
                log.error("Status transition listener error", e);
            }
        }
    }
}
