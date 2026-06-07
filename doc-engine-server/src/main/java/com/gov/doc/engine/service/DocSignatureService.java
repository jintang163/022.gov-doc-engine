package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocSignatureSignDTO;
import com.gov.doc.engine.dto.DocSignatureVerifyDTO;
import com.gov.doc.engine.entity.DocSignature;
import com.gov.doc.engine.vo.DocSignatureVO;
import com.gov.doc.engine.vo.DocSignatureVerifyResultVO;

import java.util.List;

public interface DocSignatureService extends IService<DocSignature> {

    DocSignatureVO signDocument(DocSignatureSignDTO signDTO);

    DocSignatureVerifyResultVO verifySignature(DocSignatureVerifyDTO verifyDTO);

    DocSignatureVerifyResultVO verifySignatureById(Long signatureId);

    List<DocSignatureVerifyResultVO> verifyDocumentSignatures(Long documentId);

    PageResult<DocSignatureVO> pageList(Long documentId, Integer pageNum, Integer pageSize);

    DocSignatureVO getDetail(Long id);

    List<DocSignatureVO> getDocumentSignatures(Long documentId);

    boolean revokeSignature(Long signatureId, String reason);
}
