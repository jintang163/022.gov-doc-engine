package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocIncomingDTO;
import com.gov.doc.engine.dto.DocIncomingQueryDTO;
import com.gov.doc.engine.service.DocIncomingService;
import com.gov.doc.engine.vo.DocIncomingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doc/incoming")
public class DocIncomingController {

    @Autowired
    private DocIncomingService docIncomingService;

    @GetMapping("/page")
    public Result<PageResult<DocIncomingVO>> page(DocIncomingQueryDTO queryDTO) {
        PageResult<DocIncomingVO> pageResult = docIncomingService.pageList(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<DocIncomingVO> getDetail(@PathVariable Long id) {
        DocIncomingVO vo = docIncomingService.getDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/register")
    public Result<DocIncomingVO> register(@RequestBody @Validated DocIncomingDTO dto) {
        DocIncomingVO vo = docIncomingService.register(dto);
        return Result.success("收文登记成功", vo);
    }

    @PostMapping("/api/register")
    public Result<DocIncomingVO> registerFromApi(@RequestBody @Validated DocIncomingDTO dto) {
        DocIncomingVO vo = docIncomingService.registerFromApi(dto);
        return Result.success("接口接入登记成功", vo);
    }
}
