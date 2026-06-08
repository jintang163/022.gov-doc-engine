package com.gov.doc.engine.service;

public interface DocStatusMachineService {

    int normalizeStatusForExistingDocs(String operatorId, String operatorName);

    boolean canTransition(Long docId, String toStatus, String operatorId, String operatorName);

    String getCurrentStatus(Long docId);

    void transition(Long docId, String toStatus, String operatorId, String operatorName, String remark);

    void transitionWithReason(Long docId, String toStatus, String transitionReason,
                          String operatorId, String operatorName, String remark);

    interface StatusTransitionListener {
        void onTransition(Long docId, String fromStatus, String toStatus,
                           String operatorId, String operatorName, String remark);
    }
}
