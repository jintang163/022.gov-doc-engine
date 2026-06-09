package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocHandlingDTO;
import com.gov.doc.engine.dto.DocHandlingFeedbackDTO;
import com.gov.doc.engine.dto.DocHandlingQueryDTO;
import com.gov.doc.engine.entity.DocHandling;
import com.gov.doc.engine.vo.DocHandlingVO;

public interface DocHandlingService extends IService<DocHandling> {

    PageResult<DocHandlingVO> pageList(DocHandlingQueryDTO queryDTO);

    DocHandlingVO getDetail(Long id);

    DocHandlingVO draftOpinion(DocHandlingDTO dto);

    DocHandlingVO assignHandling(DocHandlingDTO dto);

    DocHandlingVO submitFeedback(DocHandlingFeedbackDTO dto);

    PageResult<DocHandlingVO> getMyHandlings(DocHandlingQueryDTO queryDTO);
}
