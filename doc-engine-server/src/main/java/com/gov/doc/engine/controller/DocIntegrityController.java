package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.common.annotation.AuditLog;
import com.gov.doc.engine.dto.DocIntegrityDTO;
import com.gov.doc.engine.dto.DocIntegrityVerifyDTO;
import com.gov.doc.engine.service.DocIntegrityService;
import com.gov.doc.engine.vo.DocIntegrityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doc/integrity")
public class DocIntegrityController {

    @Autowired
    private DocIntegrityService integrityService;

    @PostMapping
    @AuditLog(action = "archive", resourceType = "integrity", description = "创建完整性存证")
    public Result<DocIntegrityVO> createRecord(@RequestBody @Validated DocIntegrityDTO dto) {
        DocIntegrityVO vo = integrityService.createRecord(dto);
        return Result.success("存证创建成功", vo);
    }

    @PostMapping("/verify")
    @AuditLog(action = "view", resourceType = "integrity", description = "验证公文完整性")
    public Result<DocIntegrityVO> verify(@RequestBody DocIntegrityVerifyDTO dto) {
        DocIntegrityVO vo = integrityService.verify(dto);
        return Result.success(vo);
    }

    @GetMapping("/doc/{docId}")
    public Result<DocIntegrityVO> getByDocId(@PathVariable Long docId) {
        DocIntegrityVO vo = integrityService.getByDocId(docId);
        return Result.success(vo);
    }

    @GetMapping("/doc/{docId}/history")
    public Result<List<DocIntegrityVO>> getHistoryByDocId(@PathVariable Long docId) {
        List<DocIntegrityVO> list = integrityService.getHistoryByDocId(docId);
        return Result.success(list);
    }

    @PostMapping("/compute-hash")
    public Result<String> computeHash(@RequestBody String content) {
        String hash = integrityService.computeHash(content);
        return Result.success(hash);
    }
}
