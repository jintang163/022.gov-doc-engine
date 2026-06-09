package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocArchiveDTO;
import com.gov.doc.engine.dto.DocArchiveQueryDTO;
import com.gov.doc.engine.service.DocArchiveService;
import com.gov.doc.engine.vo.DocArchiveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doc/archive")
public class DocArchiveController {

    @Autowired
    private DocArchiveService docArchiveService;

    @GetMapping("/page")
    public Result<PageResult<DocArchiveVO>> page(DocArchiveQueryDTO queryDTO) {
        PageResult<DocArchiveVO> pageResult = docArchiveService.pageList(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<DocArchiveVO> getDetail(@PathVariable Long id) {
        DocArchiveVO vo = docArchiveService.getDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/manual")
    public Result<DocArchiveVO> archiveManual(@RequestBody @Validated DocArchiveDTO archiveDTO) {
        DocArchiveVO vo = docArchiveService.archiveDocument(archiveDTO);
        return Result.success("归档成功", vo);
    }

    @PostMapping("/auto/{docId}")
    public Result<Void> archiveAuto(@PathVariable Long docId) {
        docArchiveService.autoArchive(docId);
        return Result.success("自动归档成功", null);
    }

    @GetMapping("/year/{archiveYear}")
    public Result<List<DocArchiveVO>> listByYear(@PathVariable Integer archiveYear) {
        List<DocArchiveVO> list = docArchiveService.listByYear(archiveYear);
        return Result.success(list);
    }

    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> getArchiveStats() {
        List<Map<String, Object>> stats = docArchiveService.getArchiveStats();
        return Result.success(stats);
    }

    @GetMapping("/search/doc-number")
    public Result<DocArchiveVO> searchByDocNumber(@RequestParam String docNumber) {
        DocArchiveVO vo = docArchiveService.searchByDocNumber(docNumber);
        if (vo == null) {
            return Result.fail("未找到对应归档记录");
        }
        return Result.success(vo);
    }
}
