package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocDraftSaveDTO;
import com.gov.doc.engine.entity.DocDraft;
import com.gov.doc.engine.vo.DocDraftVO;

import java.util.List;

public interface DocDraftService extends IService<DocDraft> {

    PageResult<DocDraftVO> pageList(Integer pageNum, Integer pageSize);

    List<DocDraftVO> listByTemplateId(Long templateId);

    DocDraftVO getDetail(Long id);

    DocDraftVO getLatestByTemplateId(Long templateId);

    Long saveDraft(DocDraftSaveDTO saveDTO);

    Long autoSaveDraft(DocDraftSaveDTO saveDTO);

    void deleteDraft(Long id);

    DocDraft convertToEntity(DocDraftSaveDTO saveDTO);

    DocDraftVO convertToVO(DocDraft draft);
}
