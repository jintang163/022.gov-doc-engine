package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.SysDeptSaveDTO;
import com.gov.doc.engine.service.SysDeptService;
import com.gov.doc.engine.vo.SysDeptVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/dept")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;

    @GetMapping("/tree")
    public Result<List<SysDeptVO>> getDeptTree(@RequestParam Long unitId) {
        List<SysDeptVO> tree = sysDeptService.getDeptTree(unitId);
        return Result.success(tree);
    }

    @GetMapping("/{id}")
    public Result<SysDeptVO> getDeptDetail(@PathVariable Long id) {
        SysDeptVO vo = sysDeptService.getDeptDetail(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Long> saveDept(@RequestBody @Validated SysDeptSaveDTO saveDTO) {
        Long id = sysDeptService.saveDept(saveDTO);
        return Result.success(id);
    }

    @PutMapping
    public Result<Void> updateDept(@RequestBody @Validated SysDeptSaveDTO saveDTO) {
        sysDeptService.updateDept(saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDept(@PathVariable Long id) {
        sysDeptService.deleteDept(id);
        return Result.success();
    }

    @GetMapping("/children")
    public Result<List<SysDeptVO>> getChildren(@RequestParam Long unitId, @RequestParam Long parentId) {
        List<SysDeptVO> children = sysDeptService.getChildren(unitId, parentId);
        return Result.success(children);
    }
}
