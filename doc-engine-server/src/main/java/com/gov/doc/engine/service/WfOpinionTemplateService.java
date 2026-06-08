package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.dto.WfOpinionTemplateQueryDTO;
import com.gov.doc.engine.dto.WfOpinionTemplateSaveDTO;
import com.gov.doc.engine.entity.WfOpinionTemplate;
import com.gov.doc.engine.vo.WfOpinionTemplateVO;

import java.util.List;

public interface WfOpinionTemplateService extends IService<WfOpinionTemplate> {

    List<WfOpinionTemplateVO> list(WfOpinionTemplateQueryDTO queryDTO);

    WfOpinionTemplateVO getDetail(Long id);

    Long saveTemplate(WfOpinionTemplateSaveDTO saveDTO);

    void updateTemplate(WfOpinionTemplateSaveDTO saveDTO);

    void deleteTemplate(Long id);

    WfOpinionTemplateVO convertToVO(WfOpinionTemplate template);
}
