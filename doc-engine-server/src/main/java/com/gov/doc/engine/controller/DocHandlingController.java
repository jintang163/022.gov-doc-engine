package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocHandlingDTO;
import com.gov.doc.engine.dto.DocHandlingFeedbackDTO;
import com.gov.doc.engine.dto.DocHandlingQueryDTO;
import com.gov.doc.engine.dto.DocSignReceiptDTO;
import com.gov.doc.engine.service.DocHandlingService;
import com.gov.doc.engine.vo.DocHandlingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doc/handling")
public class DocHandlingController {

    @Autowired
    private DocHandlingService docHandlingService;

    @GetMapping("/page")
    public Result<PageResult<DocHandlingVO>> page(DocHandlingQueryDTO queryDTO) {
        PageResult<DocHandlingVO> pageResult = docHandlingService.pageList(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<DocHandlingVO> getDetail(@PathVariable Long id) {
        DocHandlingVO vo = docHandlingService.getDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/draft-opinion")
    public Result<DocHandlingVO> draftOpinion(@RequestBody @Validated DocHandlingDTO dto) {
        DocHandlingVO vo = docHandlingService.draftOpinion(dto);
        return Result.success("拟办意见提交成功", vo);
    }

    @PostMapping("/assign")
    public Result<DocHandlingVO> assignHandling(@RequestBody @Validated DocHandlingDTO dto) {
        DocHandlingVO vo = docHandlingService.assignHandling(dto);
        return Result.success("转承办成功", vo);
    }

    @PostMapping("/feedback")
    public Result<DocHandlingVO> submitFeedback(@RequestBody @Validated DocHandlingFeedbackDTO dto) {
        DocHandlingVO vo = docHandlingService.submitFeedback(dto);
        return Result.success("反馈提交成功", vo);
    }

    @PostMapping("/sign-receipt")
    public Result<DocHandlingVO> signReceipt(@RequestBody @Validated DocSignReceiptDTO dto) {
        DocHandlingVO vo = docHandlingService.signReceipt(dto);
        return Result.success("签收确认成功", vo);
    }

    @GetMapping("/my-handlings")
    public Result<PageResult<DocHandlingVO>> getMyHandlings(DocHandlingQueryDTO queryDTO) {
        PageResult<DocHandlingVO> pageResult = docHandlingService.getMyHandlings(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/pending-sign-receipts")
    public Result<PageResult<DocHandlingVO>> getPendingSignReceipts(DocHandlingQueryDTO queryDTO) {
        PageResult<DocHandlingVO> pageResult = docHandlingService.getPendingSignReceipts(queryDTO);
        return Result.success(pageResult);
    }
}
