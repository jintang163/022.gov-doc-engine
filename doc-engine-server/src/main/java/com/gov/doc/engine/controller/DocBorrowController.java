package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocBorrowApplyDTO;
import com.gov.doc.engine.dto.DocBorrowApproveDTO;
import com.gov.doc.engine.dto.DocBorrowQueryDTO;
import com.gov.doc.engine.service.DocBorrowService;
import com.gov.doc.engine.vo.DocBorrowVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doc/borrow")
public class DocBorrowController {

    @Autowired
    private DocBorrowService docBorrowService;

    @GetMapping("/page")
    public Result<PageResult<DocBorrowVO>> page(DocBorrowQueryDTO queryDTO) {
        PageResult<DocBorrowVO> pageResult = docBorrowService.pageList(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<DocBorrowVO> getDetail(@PathVariable Long id) {
        DocBorrowVO vo = docBorrowService.getDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/apply")
    public Result<DocBorrowVO> apply(@RequestBody @Validated DocBorrowApplyDTO applyDTO) {
        DocBorrowVO vo = docBorrowService.applyBorrow(applyDTO);
        return Result.success("借阅申请已提交", vo);
    }

    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody @Validated DocBorrowApproveDTO approveDTO) {
        docBorrowService.approveBorrow(approveDTO);
        return Result.success("审批完成", null);
    }

    @PostMapping("/return/{borrowId}")
    public Result<Void> returnBorrow(@PathVariable Long borrowId) {
        docBorrowService.returnBorrow(borrowId);
        return Result.success("归还成功", null);
    }

    @GetMapping("/watermark-content/{borrowId}")
    public Result<String> getWatermarkedContent(@PathVariable Long borrowId) {
        String content = docBorrowService.getWatermarkedContent(borrowId);
        return Result.success(content);
    }

    @GetMapping("/my-borrows")
    public Result<PageResult<DocBorrowVO>> getMyBorrows(DocBorrowQueryDTO queryDTO) {
        PageResult<DocBorrowVO> pageResult = docBorrowService.getMyBorrows(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/pending-approvals")
    public Result<PageResult<DocBorrowVO>> getPendingApprovals(DocBorrowQueryDTO queryDTO) {
        PageResult<DocBorrowVO> pageResult = docBorrowService.getPendingApprovals(queryDTO);
        return Result.success(pageResult);
    }
}
