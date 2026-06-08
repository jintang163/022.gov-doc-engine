package com.gov.doc.engine.service;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocDistributionCreateDTO;
import com.gov.doc.engine.entity.DocDistribution;

import java.util.List;

public interface DocDistributionService {

    PageResult<DocDistribution> pageList(Integer pageNum, Integer pageSize, Long docId, String status);

    DocDistribution getDetail(Long id);

    DocDistribution distribute(DocDistributionCreateDTO dto, String operatorId, String operatorName);

    void confirmReceive(Long id, String receiverId, String receiverName, String remark);

    List<DocDistribution> getByDocId(Long docId);

    void markPrinted(Long id, String operatorId, String operatorName);
}
