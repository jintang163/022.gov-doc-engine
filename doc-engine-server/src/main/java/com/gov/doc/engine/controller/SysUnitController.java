package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.SysUnitSaveDTO;
import com.gov.doc.engine.service.SysUnitService;
import com.gov.doc.engine.vo.SysUnitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/unit")
public class SysUnitController {

    @Autowired
    private SysUnitService sysUnitService;

    @GetMapping("/tree")
    public Result<List<SysUnitVO>> getUnitTree() {
        List<SysUnitVO> tree = sysUnitService.getUnitTree();
        return Result.success(tree);
    }

    @GetMapping("/{id}")
    public Result<SysUnitVO> getUnitDetail(@PathVariable Long id) {
        SysUnitVO vo = sysUnitService.getUnitDetail(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Long> saveUnit(@RequestBody @Validated SysUnitSaveDTO saveDTO) {
        Long id = sysUnitService.saveUnit(saveDTO);
        return Result.success(id);
    }

    @PutMapping
    public Result<Void> updateUnit(@RequestBody @Validated SysUnitSaveDTO saveDTO) {
        sysUnitService.updateUnit(saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUnit(@PathVariable Long id) {
        sysUnitService.deleteUnit(id);
        return Result.success();
    }

    @GetMapping("/children/{parentId}")
    public Result<List<SysUnitVO>> getChildren(@PathVariable Long parentId) {
        List<SysUnitVO> children = sysUnitService.getChildren(parentId);
        return Result.success(children);
    }
}
