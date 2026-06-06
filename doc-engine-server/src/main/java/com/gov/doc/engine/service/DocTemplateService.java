package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocDocumentCreateDTO;
import com.gov.doc.engine.dto.DocTemplateQueryDTO;
import com.gov.doc.engine.dto.DocTemplateSaveDTO;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.entity.DocTemplate;
import com.gov.doc.engine.vo.DocTemplateVO;

import java.util.List;

public interface DocTemplateService extends IService<DocTemplate> {

    PageResult<DocTemplateVO> pageList(DocTemplateQueryDTO queryDTO);

    DocTemplateVO getDetail(Long id);

    Long saveTemplate(DocTemplateSaveDTO saveDTO);

    void updateTemplate(DocTemplateSaveDTO saveDTO);

    void deleteTemplate(Long id);

    Long createNewVersion(Long id);

    void publish(Long id);

    void disable(Long id);

    List<DocTemplateVO> listAvailable();

    DocDocument createDocumentFromTemplate(DocDocumentCreateDTO createDTO);
}
