package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocBorrowApplyDTO;
import com.gov.doc.engine.dto.DocBorrowApproveDTO;
import com.gov.doc.engine.dto.DocBorrowQueryDTO;
import com.gov.doc.engine.entity.DocBorrow;
import com.gov.doc.engine.vo.DocBorrowVO;

public interface DocBorrowService extends IService<DocBorrow> {

    PageResult<DocBorrowVO> pageList(DocBorrowQueryDTO queryDTO);

    DocBorrowVO getDetail(Long id);

    DocBorrowVO applyBorrow(DocBorrowApplyDTO applyDTO);

    void approveBorrow(DocBorrowApproveDTO approveDTO);

    void returnBorrow(Long borrowId);

    void incrementViewCount(Long borrowId);

    String getWatermarkedContent(Long borrowId);

    PageResult<DocBorrowVO> getMyBorrows(DocBorrowQueryDTO queryDTO);

    PageResult<DocBorrowVO> getPendingApprovals(DocBorrowQueryDTO queryDTO);
}
