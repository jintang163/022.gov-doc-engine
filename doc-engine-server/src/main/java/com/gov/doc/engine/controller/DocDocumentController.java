package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.*;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.service.DocAttachmentService;
import com.gov.doc.engine.service.DocDocumentService;
import com.gov.doc.engine.service.DocDraftService;
import com.gov.doc.engine.vo.DocAttachmentVO;
import com.gov.doc.engine.vo.DocDraftVO;
import com.gov.doc.engine.vo.DocPlaceholderResultVO;
import com.gov.doc.engine.vo.DocValidationResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doc/document")
public class DocDocumentController {

    @Autowired
    private DocDocumentService docDocumentService;

    @Autowired
    private DocAttachmentService docAttachmentService;

    @Autowired
    private DocDraftService docDraftService;

    @GetMapping("/page")
    public Result<PageResult<DocDocument>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String keyword) {
        PageResult<DocDocument> pageResult = docDocumentService.pageList(pageNum, pageSize, keyword);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<DocDocument> getDetail(@PathVariable Long id) {
        DocDocument document = docDocumentService.getDetail(id);
        return Result.success(document);
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Validated DocDocumentUpdateDTO updateDTO) {
        docDocumentService.updateDocument(updateDTO);
        return Result.success("更新成功", null);
    }

    @GetMapping("/attachment/list/{documentId}")
    public Result<List<DocAttachmentVO>> listAttachments(@PathVariable Long documentId) {
        List<DocAttachmentVO> list = docAttachmentService.listByDocumentId(documentId);
        return Result.success(list);
    }

    @GetMapping("/attachment/{id}")
    public Result<DocAttachmentVO> getAttachmentDetail(@PathVariable Long id) {
        DocAttachmentVO vo = docAttachmentService.getDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/attachment")
    public Result<Long> saveAttachment(@RequestBody @Validated DocAttachmentDTO attachmentDTO) {
        Long id = docAttachmentService.saveAttachment(attachmentDTO);
        return Result.success("保存成功", id);
    }

    @PutMapping("/attachment")
    public Result<Void> updateAttachment(@RequestBody @Validated DocAttachmentDTO attachmentDTO) {
        docAttachmentService.updateAttachment(attachmentDTO);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/attachment/{id}")
    public Result<Void> deleteAttachment(@PathVariable Long id) {
        docAttachmentService.deleteAttachment(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/attachment/upload/{documentId}")
    public Result<DocAttachmentVO> uploadAttachment(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file,
            String attachmentName) {
        DocAttachmentVO vo = docAttachmentService.uploadAttachment(documentId, file, attachmentName);
        return Result.success("上传成功", vo);
    }

    @GetMapping("/attachment/download/{id}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id) {
        byte[] fileBytes = docAttachmentService.downloadAttachment(id);
        DocAttachmentVO attachment = docAttachmentService.getDetail(id);

        String fileName = attachment.getAttachmentName() + "." + attachment.getFileExt();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(determineContentType(attachment.getFileType())));
        headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        return ResponseEntity.ok().headers(headers).body(fileBytes);
    }

    @GetMapping("/attachment/preview/{id}")
    public ResponseEntity<?> previewAttachment(@PathVariable Long id) {
        String previewUrl = docAttachmentService.getPreviewUrl(id);
        if (previewUrl != null && !previewUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(previewUrl))
                    .build();
        }
        byte[] fileBytes = docAttachmentService.downloadAttachment(id);
        DocAttachmentVO attachment = docAttachmentService.getDetail(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(determineContentType(attachment.getFileType())));
        return ResponseEntity.ok().headers(headers).body(fileBytes);
    }

    @GetMapping("/draft/page")
    public Result<PageResult<DocDraftVO>> pageDrafts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<DocDraftVO> pageResult = docDraftService.pageList(pageNum, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/draft/list/{templateId}")
    public Result<List<DocDraftVO>> listDraftsByTemplateId(@PathVariable Long templateId) {
        List<DocDraftVO> list = docDraftService.listByTemplateId(templateId);
        return Result.success(list);
    }

    @GetMapping("/draft/{id}")
    public Result<DocDraftVO> getDraftDetail(@PathVariable Long id) {
        DocDraftVO vo = docDraftService.getDetail(id);
        return Result.success(vo);
    }

    @GetMapping("/draft/latest/{templateId}")
    public Result<DocDraftVO> getLatestDraft(@PathVariable Long templateId) {
        DocDraftVO vo = docDraftService.getLatestByTemplateId(templateId);
        return Result.success(vo);
    }

    @PostMapping("/draft")
    public Result<Long> saveDraft(@RequestBody @Validated DocDraftSaveDTO saveDTO) {
        Long id = docDraftService.saveDraft(saveDTO);
        return Result.success("保存成功", id);
    }

    @PostMapping("/draft/auto")
    public Result<Long> autoSaveDraft(@RequestBody @Validated DocDraftSaveDTO saveDTO) {
        Long id = docDraftService.autoSaveDraft(saveDTO);
        return Result.success("自动保存成功", id);
    }

    @DeleteMapping("/draft/{id}")
    public Result<Void> deleteDraft(@PathVariable Long id) {
        docDraftService.deleteDraft(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/placeholder/replace")
    public Result<DocPlaceholderResultVO> replacePlaceholders(@RequestBody @Validated DocPlaceholderReplaceDTO replaceDTO) {
        DocPlaceholderResultVO vo = docDocumentService.replacePlaceholders(replaceDTO);
        return Result.success(vo);
    }

    @GetMapping("/placeholder/preset")
    public Result<Map<String, String>> getPresetPlaceholders() {
        Map<String, String> placeholders = docDocumentService.getPresetPlaceholders();
        return Result.success(placeholders);
    }

    @PostMapping("/validate")
    public Result<DocValidationResultVO> validate(@RequestBody @Validated DocDocumentValidationDTO validationDTO) {
        DocValidationResultVO vo = docDocumentService.validateDocument(validationDTO);
        return Result.success(vo);
    }

    @PostMapping("/image/upload")
    public Map<String, Object> uploadEditorImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "0") Long documentId) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            if (documentId == null || documentId <= 0) {
                documentId = 0L;
            }
            DocAttachmentVO vo = docAttachmentService.uploadAttachment(documentId, file, file.getOriginalFilename());
            String imageUrl = "/api/doc/document/attachment/preview/" + vo.getId();
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("url", imageUrl);
            data.put("alt", vo.getAttachmentName());
            data.put("href", imageUrl);
            result.put("errno", 0);
            result.put("data", data);
        } catch (Exception e) {
            result.put("errno", 1);
            result.put("message", "图片上传失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/attachment/upload-editor")
    public Map<String, Object> uploadEditorAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "0") Long documentId) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            if (documentId == null || documentId <= 0) {
                documentId = 0L;
            }
            DocAttachmentVO vo = docAttachmentService.uploadAttachment(documentId, file, file.getOriginalFilename());
            String fileUrl = "/api/doc/document/attachment/download/" + vo.getId();
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("url", fileUrl);
            data.put("name", vo.getAttachmentName());
            result.put("errno", 0);
            result.put("data", data);
        } catch (Exception e) {
            result.put("errno", 1);
            result.put("message", "附件上传失败: " + e.getMessage());
        }
        return result;
    }

    private String determineContentType(String fileType) {
        if (fileType == null || fileType.isEmpty()) {
            return "application/octet-stream";
        }
        switch (fileType.toLowerCase()) {
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "txt":
                return "text/plain";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            default:
                return "application/octet-stream";
        }
    }
}
