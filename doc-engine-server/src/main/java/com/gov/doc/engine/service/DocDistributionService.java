package com.gov.doc.engine.service;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocDistributionCreateDTO;
import com.gov.doc.engine.entity.DocDistribution;

import java.util.List;
import java.util.Map;

public interface DocDistributionService {

    PageResult<DocDistribution> pageList(Integer pageNum, Integer pageSize, Long docId, String status);
    DocDistribution getDetail(Long id);
    List<DocDistribution> distribute(DocDistributionCreateDTO dto, String operatorId, String operatorName);
    void confirmReceive(Long id, String receiverId, String receiverName, String remark, String receiveIp, String receiveUa);
    List<DocDistribution> getByDocId(Long docId);
    Map<String, List<DocDistribution>> getByDocIdGrouped(Long docId);
    void markPrinted(Long id, String operatorId, String operatorName);
}
