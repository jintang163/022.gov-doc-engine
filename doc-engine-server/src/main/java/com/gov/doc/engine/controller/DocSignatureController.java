package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocSignatureSignDTO;
import com.gov.doc.engine.dto.DocSignatureVerifyDTO;
import com.gov.doc.engine.service.DocSignatureService;
import com.gov.doc.engine.vo.DocSignatureVO;
import com.gov.doc.engine.vo.DocSignatureVerifyResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/doc/signature")
public class DocSignatureController {

    @Autowired
    private DocSignatureService docSignatureService;

    @PostMapping("/sign")
    public Result<DocSignatureVO> signDocument(@Valid @RequestBody DocSignatureSignDTO signDTO) {
        DocSignatureVO vo = docSignatureService.signDocument(signDTO);
        return Result.success("签章成功", vo);
    }

    @PostMapping("/verify")
    public Result<DocSignatureVerifyResultVO> verifySignature(@Valid @RequestBody DocSignatureVerifyDTO verifyDTO) {
        DocSignatureVerifyResultVO result = docSignatureService.verifySignature(verifyDTO);
        return Result.success(result);
    }

    @GetMapping("/verify/{signatureId}")
    public Result<DocSignatureVerifyResultVO> verifySignatureById(@PathVariable Long signatureId) {
        DocSignatureVerifyResultVO result = docSignatureService.verifySignatureById(signatureId);
        return Result.success(result);
    }

    @GetMapping("/verify/document/{documentId}")
    public Result<List<DocSignatureVerifyResultVO>> verifyDocumentSignatures(@PathVariable Long documentId) {
        List<DocSignatureVerifyResultVO> results = docSignatureService.verifyDocumentSignatures(documentId);
        return Result.success(results);
    }

    @GetMapping("/page")
    public Result<PageResult<DocSignatureVO>> pageList(
            @RequestParam(required = false) Long documentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<DocSignatureVO> page = docSignatureService.pageList(documentId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<DocSignatureVO> getDetail(@PathVariable Long id) {
        DocSignatureVO vo = docSignatureService.getDetail(id);
        if (vo == null) {
            return Result.fail(404, "签章记录不存在");
        }
        return Result.success(vo);
    }

    @GetMapping("/document/{documentId}")
    public Result<List<DocSignatureVO>> getDocumentSignatures(@PathVariable Long documentId) {
        List<DocSignatureVO> list = docSignatureService.getDocumentSignatures(documentId);
        return Result.success(list);
    }

    @PostMapping("/revoke/{signatureId}")
    public Result<Boolean> revokeSignature(
            @PathVariable Long signatureId,
            @RequestParam(required = false) String reason) {
        boolean result = docSignatureService.revokeSignature(signatureId, reason);
        if (result) {
            return Result.success("撤销成功", true);
        }
        return Result.fail("撤销失败");
    }

    @GetMapping("/download/{signatureId}")
    public void downloadSignedDocument(@PathVariable Long signatureId, HttpServletResponse response) throws IOException {
        DocSignatureVO vo = docSignatureService.getDetail(signatureId);
        if (vo == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = new File(vo.getSignedFilePath());
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/pdf");
        String fileName = URLEncoder.encode(vo.getSignedFileName(), StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        response.setContentLengthLong(file.length());

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = bis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        }
    }

    @GetMapping("/preview/{signatureId}")
    public void previewSignedDocument(@PathVariable Long signatureId, HttpServletResponse response) throws IOException {
        DocSignatureVO vo = docSignatureService.getDetail(signatureId);
        if (vo == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = new File(vo.getSignedFilePath());
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=" + vo.getSignedFileName());
        response.setContentLengthLong(file.length());

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = bis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        }
    }
}
