package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocArchiveDTO;
import com.gov.doc.engine.dto.DocArchiveQueryDTO;
import com.gov.doc.engine.entity.DocArchive;
import com.gov.doc.engine.vo.DocArchiveVO;

import java.util.List;
import java.util.Map;

public interface DocArchiveService extends IService<DocArchive> {

    PageResult<DocArchiveVO> pageList(DocArchiveQueryDTO queryDTO);

    DocArchiveVO getDetail(Long id);

    DocArchiveVO archiveDocument(DocArchiveDTO archiveDTO);

    void autoArchive(Long docId);

    List<DocArchiveVO> listByYear(Integer archiveYear);

    List<Map<String, Object>> getArchiveStats();

    DocArchiveVO searchByDocNumber(String docNumber);
}
