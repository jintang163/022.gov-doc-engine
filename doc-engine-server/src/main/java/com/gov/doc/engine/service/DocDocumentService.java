package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.*;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.vo.DocPlaceholderResultVO;
import com.gov.doc.engine.vo.DocValidationResultVO;

import java.util.Map;

public interface DocDocumentService extends IService<DocDocument> {

    PageResult<DocDocument> pageList(Integer pageNum, Integer pageSize, String keyword);

    DocDocument getDetail(Long id);

    void updateDocument(DocDocumentUpdateDTO updateDTO);

    DocValidationResultVO validateDocument(DocDocumentValidationDTO validationDTO);

    DocPlaceholderResultVO replacePlaceholders(DocPlaceholderReplaceDTO replaceDTO);

    Map<String, String> getPresetPlaceholders();

    String formatFileSize(Long fileSize);
}
