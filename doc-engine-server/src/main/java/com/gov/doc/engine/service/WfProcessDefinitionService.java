package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.WfProcessDefinitionQueryDTO;
import com.gov.doc.engine.dto.WfProcessDefinitionSaveDTO;
import com.gov.doc.engine.entity.WfProcessDefinition;
import com.gov.doc.engine.vo.WfProcessDefinitionVO;
import com.gov.doc.engine.vo.WfProcessGraphVO;

import java.util.List;

public interface WfProcessDefinitionService extends IService<WfProcessDefinition> {

    PageResult<WfProcessDefinitionVO> pageList(Integer pageNum, Integer pageSize, WfProcessDefinitionQueryDTO queryDTO);

    WfProcessDefinitionVO getDetail(Long id);

    WfProcessDefinitionVO getByCode(String processCode);

    Long saveProcessDefinition(WfProcessDefinitionSaveDTO saveDTO);

    void updateProcessDefinition(WfProcessDefinitionSaveDTO saveDTO);

    void publish(Long id);

    void suspend(Long id);

    void deleteProcessDefinition(Long id);

    List<WfProcessDefinitionVO> listAllPublished();

    WfProcessGraphVO getProcessGraph(Long processDefId, Long processInstanceId);

    void deployProcessDefinition(Long id);
}
