package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocTemplateHeaderDTO;
import com.gov.doc.engine.service.DocTemplateHeaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doc/template-header")
public class DocTemplateHeaderController {

    @Autowired
    private DocTemplateHeaderService docTemplateHeaderService;

    @GetMapping("/list")
    public Result<List<DocTemplateHeaderDTO>> list() {
        List<DocTemplateHeaderDTO> list = docTemplateHeaderService.listAll();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<DocTemplateHeaderDTO> getDetail(@PathVariable Long id) {
        DocTemplateHeaderDTO dto = docTemplateHeaderService.getDetail(id);
        return Result.success(dto);
    }

    @PostMapping
    public Result<Long> save(@RequestBody @Validated DocTemplateHeaderDTO dto) {
        Long id = docTemplateHeaderService.saveHeader(dto);
        return Result.success("创建成功", id);
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Validated DocTemplateHeaderDTO dto) {
        docTemplateHeaderService.updateHeader(dto);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        docTemplateHeaderService.deleteHeader(id);
        return Result.success("删除成功", null);
    }
}
