package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.dto.DocIntegrityDTO;
import com.gov.doc.engine.dto.DocIntegrityVerifyDTO;
import com.gov.doc.engine.entity.DocIntegrity;
import com.gov.doc.engine.vo.DocIntegrityVO;

import java.util.List;

public interface DocIntegrityService extends IService<DocIntegrity> {

    DocIntegrityVO createRecord(DocIntegrityDTO dto);

    DocIntegrityVO verify(DocIntegrityVerifyDTO dto);

    DocIntegrityVO getByDocId(Long docId);

    List<DocIntegrityVO> getHistoryByDocId(Long docId);

    String computeHash(String content);
}
