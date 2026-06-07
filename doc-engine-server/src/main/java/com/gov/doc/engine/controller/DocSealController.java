package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocSealCreateDTO;
import com.gov.doc.engine.dto.DocSealGrantDTO;
import com.gov.doc.engine.dto.DocSealQueryDTO;
import com.gov.doc.engine.dto.DocSealUpdateDTO;
import com.gov.doc.engine.service.DocSealService;
import com.gov.doc.engine.vo.DocSealGrantVO;
import com.gov.doc.engine.vo.DocSealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/doc/seal")
public class DocSealController {

    @Autowired
    private DocSealService docSealService;

    @GetMapping("/page")
    public Result<PageResult<DocSealVO>> page(@Validated DocSealQueryDTO queryDTO) {
        PageResult<DocSealVO> pageResult = docSealService.pageList(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<DocSealVO> getDetail(@PathVariable Long id) {
        DocSealVO vo = docSealService.getDetail(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Long> save(@RequestBody @Validated DocSealCreateDTO createDTO) {
        Long id = docSealService.createSeal(createDTO);
        return Result.success("创建成功", id);
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Validated DocSealUpdateDTO updateDTO) {
        docSealService.updateSeal(updateDTO);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        docSealService.deleteSeal(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/upload-image/{sealId}")
    public Result<String> uploadSealImage(
            @PathVariable Long sealId,
            @RequestParam("file") MultipartFile file) {
        String filePath = docSealService.uploadSealImage(sealId, file);
        return Result.success("印章图片上传成功", filePath);
    }

    @GetMapping("/image/{sealId}")
    public ResponseEntity<byte[]> getSealImage(@PathVariable Long sealId) {
        byte[] imageBytes = docSealService.getSealImage(sealId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return ResponseEntity.ok().headers(headers).body(imageBytes);
    }

    @PostMapping("/grant")
    public Result<Void> grant(@RequestBody @Validated DocSealGrantDTO grantDTO) {
        docSealService.grantSeal(grantDTO);
        return Result.success("授权成功", null);
    }

    @PostMapping("/revoke/{grantId}")
    public Result<Void> revoke(@PathVariable Long grantId) {
        docSealService.revokeGrant(grantId);
        return Result.success("撤销授权成功", null);
    }

    @GetMapping("/grants/{sealId}")
    public Result<List<DocSealGrantVO>> getGrants(@PathVariable Long sealId) {
        List<DocSealGrantVO> grants = docSealService.getSealGrants(sealId);
        return Result.success(grants);
    }

    @GetMapping("/available")
    public Result<List<DocSealVO>> listAvailable() {
        List<DocSealVO> list = docSealService.listAvailableSeals();
        return Result.success(list);
    }
}
