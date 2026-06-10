package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.SysPostQueryDTO;
import com.gov.doc.engine.dto.SysPostSaveDTO;
import com.gov.doc.engine.service.SysPostService;
import com.gov.doc.engine.vo.SysPostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/post")
public class SysPostController {

    @Autowired
    private SysPostService sysPostService;

    @GetMapping("/page")
    public Result<PageResult<SysPostVO>> pageList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   SysPostQueryDTO queryDTO) {
        PageResult<SysPostVO> pageResult = sysPostService.pageList(pageNum, pageSize, queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<SysPostVO> getPostDetail(@PathVariable Long id) {
        SysPostVO vo = sysPostService.getPostDetail(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Long> savePost(@RequestBody @Validated SysPostSaveDTO saveDTO) {
        Long id = sysPostService.savePost(saveDTO);
        return Result.success(id);
    }

    @PutMapping
    public Result<Void> updatePost(@RequestBody @Validated SysPostSaveDTO saveDTO) {
        sysPostService.updatePost(saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        sysPostService.deletePost(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<SysPostVO>> listAll(@RequestParam Long unitId) {
        List<SysPostVO> list = sysPostService.listAll(unitId);
        return Result.success(list);
    }
}
