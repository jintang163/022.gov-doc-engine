package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocDocumentCreateDTO;
import com.gov.doc.engine.service.DocPreviewService;
import com.gov.doc.engine.service.DocTemplateService;
import com.gov.doc.engine.vo.DocTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/doc/preview")
public class DocPreviewController {

    @Autowired
    private DocPreviewService docPreviewService;

    @Autowired
    private DocTemplateService docTemplateService;

    @GetMapping("/template/html/{templateId}")
    public Result<String> previewTemplateHtml(@PathVariable Long templateId) {
        DocTemplateVO templateVO = docTemplateService.getDetail(templateId);
        if (templateVO == null) {
            return Result.fail("模板不存在");
        }
        String html = docPreviewService.generateTemplatePreviewHtml(templateVO);
        return Result.success(html);
    }

    @PostMapping("/document/html/{templateId}")
    public Result<String> previewDocumentHtml(
            @PathVariable Long templateId,
            @RequestBody DocDocumentCreateDTO documentDTO) {
        DocTemplateVO templateVO = docTemplateService.getDetail(templateId);
        if (templateVO == null) {
            return Result.fail("模板不存在");
        }
        String html = docPreviewService.generateDocumentPreviewHtml(templateVO, documentDTO);
        return Result.success(html);
    }

    @GetMapping(value = "/template/html-view/{templateId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> previewTemplateHtmlView(@PathVariable Long templateId) {
        DocTemplateVO templateVO = docTemplateService.getDetail(templateId);
        if (templateVO == null) {
            return ResponseEntity.notFound().build();
        }
        String html = docPreviewService.generateTemplatePreviewHtml(templateVO);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "html", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(html);
    }

    @PostMapping(value = "/document/html-view/{templateId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> previewDocumentHtmlView(
            @PathVariable Long templateId,
            @RequestBody DocDocumentCreateDTO documentDTO) {
        DocTemplateVO templateVO = docTemplateService.getDetail(templateId);
        if (templateVO == null) {
            return ResponseEntity.notFound().build();
        }
        String html = docPreviewService.generateDocumentPreviewHtml(templateVO, documentDTO);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "html", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(html);
    }

    @GetMapping("/template/pdf/{templateId}")
    public ResponseEntity<byte[]> previewTemplatePdf(@PathVariable Long templateId) {
        DocTemplateVO templateVO = docTemplateService.getDetail(templateId);
        if (templateVO == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] pdfBytes = docPreviewService.generateTemplatePreviewPdf(templateVO);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "template-preview.html");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping("/document/pdf/{templateId}")
    public ResponseEntity<byte[]> previewDocumentPdf(
            @PathVariable Long templateId,
            @RequestBody DocDocumentCreateDTO documentDTO) {
        DocTemplateVO templateVO = docTemplateService.getDetail(templateId);
        if (templateVO == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] pdfBytes = docPreviewService.generateDocumentPreviewPdf(templateVO, documentDTO);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "document-preview.html");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
