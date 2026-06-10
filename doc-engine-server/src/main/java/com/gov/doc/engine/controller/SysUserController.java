package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.SysUserQueryDTO;
import com.gov.doc.engine.dto.SysUserSaveDTO;
import com.gov.doc.engine.service.SysUserService;
import com.gov.doc.engine.vo.SysUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/page")
    public Result<PageResult<SysUserVO>> pageList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   SysUserQueryDTO queryDTO) {
        PageResult<SysUserVO> pageResult = sysUserService.pageList(pageNum, pageSize, queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<SysUserVO> getUserDetail(@PathVariable Long id) {
        SysUserVO vo = sysUserService.getUserDetail(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Long> saveUser(@RequestBody @Validated SysUserSaveDTO saveDTO) {
        Long id = sysUserService.saveUser(saveDTO);
        return Result.success(id);
    }

    @PutMapping
    public Result<Void> updateUser(@RequestBody @Validated SysUserSaveDTO saveDTO) {
        sysUserService.updateUser(saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/dept/{deptId}")
    public Result<List<SysUserVO>> listByDept(@PathVariable Long deptId) {
        List<SysUserVO> list = sysUserService.listByDept(deptId);
        return Result.success(list);
    }

    @GetMapping("/post/{postId}")
    public Result<List<SysUserVO>> listByPost(@PathVariable Long postId) {
        List<SysUserVO> list = sysUserService.listByPost(postId);
        return Result.success(list);
    }

    @GetMapping("/unit/{unitId}")
    public Result<List<SysUserVO>> listByUnit(@PathVariable Long unitId) {
        List<SysUserVO> list = sysUserService.listByUnit(unitId);
        return Result.success(list);
    }

    @GetMapping("/search")
    public Result<List<SysUserVO>> searchUsers(@RequestParam String keyword) {
        List<SysUserVO> list = sysUserService.searchUsers(keyword);
        return Result.success(list);
    }
}
