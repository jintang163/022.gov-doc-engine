package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocSupervisionDTO;
import com.gov.doc.engine.dto.DocSupervisionQueryDTO;
import com.gov.doc.engine.entity.DocSupervision;
import com.gov.doc.engine.vo.DocSupervisionVO;

import java.util.List;

public interface DocSupervisionService extends IService<DocSupervision> {

    PageResult<DocSupervisionVO> pageList(DocSupervisionQueryDTO queryDTO);

    DocSupervisionVO getDetail(Long id);

    List<DocSupervisionVO> identifyTimeoutDocs();

    List<DocSupervisionVO> identifyUrgeOverdueDocs();

    DocSupervisionVO generateSupervision(DocSupervisionDTO dto);

    List<DocSupervisionVO> batchGenerateSupervision(List<DocSupervisionDTO> dtoList);

    DocSupervisionVO pushToLeader(Long id);

    List<DocSupervisionVO> batchPushToLeader(List<Long> ids);

    DocSupervisionVO updateStatus(Long id, String status);

    DocSupervisionVO completeSupervision(Long id);

    PageResult<DocSupervisionVO> getMySupervisions(DocSupervisionQueryDTO queryDTO);
}
