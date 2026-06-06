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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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

    @PostMapping("/upload-word/{templateId}")
    public Result<String> uploadWordTemplate(
            @PathVariable Long templateId,
            @RequestParam("file") MultipartFile file) {
        String filePath = docTemplateService.uploadWordTemplate(templateId, file);
        return Result.success("Word模板上传成功", filePath);
    }

    @PostMapping("/extract-variables")
    public Result<List<String>> extractVariables(@RequestParam("file") MultipartFile file) {
        List<String> variables = docTemplateService.extractVariablesFromWord(file);
        return Result.success(variables);
    }

    @PostMapping("/generate-word/{templateId}")
    public ResponseEntity<byte[]> generateWordDocument(
            @PathVariable Long templateId,
            @RequestBody DocDocumentCreateDTO createDTO) {
        byte[] wordBytes = docTemplateService.generateWordDocument(templateId, createDTO);
        
        String fileName = "document_" + System.currentTimeMillis() + ".docx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        return ResponseEntity.ok().headers(headers).body(wordBytes);
    }
}
