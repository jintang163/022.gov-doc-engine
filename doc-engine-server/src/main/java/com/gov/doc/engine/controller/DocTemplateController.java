package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocDocumentCreateDTO;
import com.gov.doc.engine.dto.DocTemplateQueryDTO;
import com.gov.doc.engine.dto.DocTemplateSaveDTO;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.service.DocTemplateService;
import com.gov.doc.engine.vo.DocTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doc/template")
public class DocTemplateController {

    @Autowired
    private DocTemplateService docTemplateService;

    @GetMapping("/page")
    public Result<PageResult<DocTemplateVO>> page(@Validated DocTemplateQueryDTO queryDTO) {
        PageResult<DocTemplateVO> pageResult = docTemplateService.pageList(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<DocTemplateVO> getDetail(@PathVariable Long id) {
        DocTemplateVO vo = docTemplateService.getDetail(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Long> save(@RequestBody @Validated DocTemplateSaveDTO saveDTO) {
        Long id = docTemplateService.saveTemplate(saveDTO);
        return Result.success("创建成功", id);
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Validated DocTemplateSaveDTO saveDTO) {
        docTemplateService.updateTemplate(saveDTO);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        docTemplateService.deleteTemplate(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/version/{id}")
    public Result<Long> createNewVersion(@PathVariable Long id) {
        Long newVersionId = docTemplateService.createNewVersion(id);
        return Result.success("创建新版本成功", newVersionId);
    }

    @PostMapping("/publish/{id}")
    public Result<Void> publish(@PathVariable Long id) {
        docTemplateService.publish(id);
        return Result.success("发布成功", null);
    }

    @PostMapping("/disable/{id}")
    public Result<Void> disable(@PathVariable Long id) {
        docTemplateService.disable(id);
        return Result.success("停用成功", null);
    }

    @GetMapping("/available")
    public Result<List<DocTemplateVO>> listAvailable() {
        List<DocTemplateVO> list = docTemplateService.listAvailable();
        return Result.success(list);
    }

    @PostMapping("/document/create")
    public Result<DocDocument> createDocument(@RequestBody @Validated DocDocumentCreateDTO createDTO) {
        DocDocument document = docTemplateService.createDocumentFromTemplate(createDTO);
        return Result.success("公文创建成功", document);
    }
}
