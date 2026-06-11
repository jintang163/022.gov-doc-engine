package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocHandlingDTO;
import com.gov.doc.engine.dto.DocHandlingFeedbackDTO;
import com.gov.doc.engine.dto.DocHandlingQueryDTO;
import com.gov.doc.engine.service.DocHandlingService;
import com.gov.doc.engine.service.DocUrgeLogService;
import com.gov.doc.engine.vo.DocHandlingVO;
import com.gov.doc.engine.vo.DocUrgeLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doc/handling")
public class DocHandlingController {

    @Autowired
    private DocHandlingService docHandlingService;

    @Autowired
    private DocUrgeLogService docUrgeLogService;

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

    @GetMapping("/my-handlings")
    public Result<PageResult<DocHandlingVO>> getMyHandlings(DocHandlingQueryDTO queryDTO) {
        PageResult<DocHandlingVO> pageResult = docHandlingService.getMyHandlings(queryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/{id}/urge")
    public Result<DocUrgeLogVO> urgeHandling(@PathVariable Long id, @RequestParam(required = false) String urgeContent) {
        DocUrgeLogVO vo = docHandlingService.urgeHandling(id, urgeContent);
        return Result.success("催办发送成功", vo);
    }

    @GetMapping("/{id}/urge-logs")
    public Result<List<DocUrgeLogVO>> listUrgeLogs(@PathVariable Long id) {
        DocHandlingVO handling = docHandlingService.getDetail(id);
        List<DocUrgeLogVO> list = docUrgeLogService.listByIncomingId(handling.getIncomingId());
        return Result.success(list);
    }
}
