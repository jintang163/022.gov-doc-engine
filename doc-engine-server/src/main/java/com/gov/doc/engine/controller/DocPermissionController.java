package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.common.annotation.AuditLog;
import com.gov.doc.engine.common.annotation.RequirePermission;
import com.gov.doc.engine.dto.DocPermissionDTO;
import com.gov.doc.engine.service.DocPermissionService;
import com.gov.doc.engine.vo.DocPermissionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doc/permission")
public class DocPermissionController {

    @Autowired
    private DocPermissionService permissionService;

    @GetMapping("/page")
    public Result<PageResult<DocPermissionVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String permissionType,
            @RequestParam(required = false) String permissionCode) {
        PageResult<DocPermissionVO> pageResult = permissionService.pageList(pageNum, pageSize, permissionType, permissionCode);
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    public Result<List<DocPermissionVO>> listAll() {
        List<DocPermissionVO> list = permissionService.listAll();
        return Result.success(list);
    }

    @GetMapping("/code/{code}")
    public Result<DocPermissionVO> getByCode(@PathVariable String code) {
        DocPermissionVO vo = permissionService.getByCode(code);
        return Result.success(vo);
    }

    @PostMapping
    @RequirePermission("doc:modify")
    @AuditLog(action = "modify", resourceType = "permission", description = "创建权限配置")
    public Result<DocPermissionVO> create(@RequestBody @Validated DocPermissionDTO dto) {
        DocPermissionVO vo = permissionService.create(dto);
        return Result.success("创建成功", vo);
    }

    @PutMapping("/{id}")
    @RequirePermission("doc:modify")
    @AuditLog(action = "modify", resourceType = "permission", description = "修改权限配置")
    public Result<DocPermissionVO> update(@PathVariable Long id, @RequestBody @Validated DocPermissionDTO dto) {
        DocPermissionVO vo = permissionService.update(id, dto);
        return Result.success("修改成功", vo);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("doc:delete")
    @AuditLog(action = "delete", resourceType = "permission", description = "删除权限配置")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/check")
    public Result<Boolean> checkPermission(@RequestParam String permissionCode) {
        boolean hasPermission = permissionService.checkPermission(permissionCode);
        return Result.success(hasPermission);
    }

    @GetMapping("/check-security-level")
    public Result<Boolean> checkSecurityLevel(
            @RequestParam String userId,
            @RequestParam String securityLevel) {
        boolean canAccess = permissionService.checkSecurityLevelAccess(userId, securityLevel);
        return Result.success(canAccess);
    }
}
