package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.dto.DocUrgeDTO;
import com.gov.doc.engine.entity.DocUrgeLog;
import com.gov.doc.engine.vo.DocUrgeLogVO;

import java.util.List;

public interface DocUrgeLogService extends IService<DocUrgeLog> {

    DocUrgeLogVO createUrge(DocUrgeDTO dto);

    DocUrgeLogVO acknowledge(Long id);

    int countByIncomingId(Long incomingId);

    List<DocUrgeLogVO> listByIncomingId(Long incomingId);
}
