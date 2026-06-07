package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocSignatureLogQueryDTO;
import com.gov.doc.engine.service.DocSignatureLogService;
import com.gov.doc.engine.vo.DocSignatureLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doc/signature/log")
public class DocSignatureLogController {

    @Autowired
    private DocSignatureLogService docSignatureLogService;

    @GetMapping("/page")
    public Result<PageResult<DocSignatureLogVO>> page(@Validated DocSignatureLogQueryDTO queryDTO) {
        PageResult<DocSignatureLogVO> pageResult = docSignatureLogService.pageList(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<DocSignatureLogVO> getDetail(@PathVariable Long id) {
        DocSignatureLogVO vo = docSignatureLogService.getDetail(id);
        return Result.success(vo);
    }
}
