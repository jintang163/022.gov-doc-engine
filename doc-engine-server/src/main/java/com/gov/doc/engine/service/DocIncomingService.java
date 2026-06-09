package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocIncomingDTO;
import com.gov.doc.engine.dto.DocIncomingQueryDTO;
import com.gov.doc.engine.entity.DocIncoming;
import com.gov.doc.engine.vo.DocIncomingVO;

public interface DocIncomingService extends IService<DocIncoming> {

    PageResult<DocIncomingVO> pageList(DocIncomingQueryDTO queryDTO);

    DocIncomingVO getDetail(Long id);

    DocIncomingVO register(DocIncomingDTO dto);

    DocIncomingVO registerFromApi(DocIncomingDTO dto);
}
