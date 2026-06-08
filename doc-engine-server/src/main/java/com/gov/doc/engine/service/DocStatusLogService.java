package com.gov.doc.engine.service;

import com.gov.doc.engine.entity.DocStatusLog;

import java.util.List;

public interface DocStatusLogService {

    List<DocStatusLog> getByDocId(Long docId);

    DocStatusLog getLatestStatus(Long docId);
}
