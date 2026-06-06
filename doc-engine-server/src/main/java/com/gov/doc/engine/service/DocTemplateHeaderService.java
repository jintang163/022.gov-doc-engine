package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.dto.DocTemplateHeaderDTO;
import com.gov.doc.engine.entity.DocTemplateHeader;

import java.util.List;

public interface DocTemplateHeaderService extends IService<DocTemplateHeader> {

    List<DocTemplateHeaderDTO> listAll();

    DocTemplateHeaderDTO getDetail(Long id);

    Long saveHeader(DocTemplateHeaderDTO dto);

    void updateHeader(DocTemplateHeaderDTO dto);

    void deleteHeader(Long id);
}
