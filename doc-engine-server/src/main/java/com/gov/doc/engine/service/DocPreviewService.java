package com.gov.doc.engine.service;

import com.gov.doc.engine.dto.DocDocumentCreateDTO;
import com.gov.doc.engine.dto.DocTemplateHeaderDTO;
import com.gov.doc.engine.vo.DocTemplateVO;

public interface DocPreviewService {

    String generateTemplatePreviewHtml(DocTemplateVO templateVO);

    String generateDocumentPreviewHtml(DocTemplateVO templateVO, DocDocumentCreateDTO documentDTO);

    byte[] generateTemplatePreviewPdf(DocTemplateVO templateVO);

    byte[] generateDocumentPreviewPdf(DocTemplateVO templateVO, DocDocumentCreateDTO documentDTO);
}
