package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocSignatureLogQueryDTO;
import com.gov.doc.engine.entity.DocSignatureLog;
import com.gov.doc.engine.vo.DocSignatureLogVO;

public interface DocSignatureLogService extends IService<DocSignatureLog> {

    void logOperation(String operationType, Long sealId, String sealName, Long documentId, String documentTitle,
                      Long signatureId, Long grantId, String operationDetail, Integer status, String errorMessage, String remark);

    PageResult<DocSignatureLogVO> pageList(DocSignatureLogQueryDTO queryDTO);

    DocSignatureLogVO getDetail(Long id);
}
