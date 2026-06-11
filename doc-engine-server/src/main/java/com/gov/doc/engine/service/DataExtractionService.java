package com.gov.doc.engine.service;

import com.gov.doc.engine.entity.DocAnalysis;
import com.gov.doc.engine.entity.RejectReason;
import com.gov.doc.engine.entity.ReminderLog;

import java.time.LocalDateTime;
import java.util.List;

public interface DataExtractionService {

    int extractProcessHistory(LocalDateTime since);

    int extractUrgeLogs(LocalDateTime since);

    int extractRejectReasons(LocalDateTime since);

    void extractAll();

    List<DocAnalysis> queryDocAnalysis(String businessKey, String sourceType, int limit);

    List<ReminderLog> queryReminderLog(Long incomingId, String urgeType, int limit);

    List<RejectReason> queryRejectReason(String businessKey, String approvalType, int limit);
}
