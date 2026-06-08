package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.enums.DocStatusEnum;
import com.gov.doc.engine.service.DocStatusMachineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Api(tags = "公文状态机")
@RestController
@RequestMapping("/api/doc/status")
public class DocStatusMachineController {

    @Autowired
    private DocStatusMachineService statusMachineService;

    @ApiOperation("获取所有公文状态")
    @GetMapping("/all")
    public Result<List<Map<String, String>>> getAllStatus() {
        List<Map<String, String>> list = java.util.Arrays.stream(DocStatusEnum.values())
                .map(e -> {
                    Map<String, String> map = new java.util.HashMap<>();
                    map.put("code", e.getCode());
                    map.put("name", e.getName());
                    return map;
                })
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @ApiOperation("获取当前公文状态")
    @GetMapping("/current/{docId}")
    public Result<String> getCurrentStatus(@PathVariable Long docId) {
        return Result.success(statusMachineService.getCurrentStatus(docId));
    }

    @ApiOperation("获取允许的状态跳转")
    @GetMapping("/allowed-transitions/{docId}")
    public Result<Set<String>> getAllowedTransitions(@PathVariable Long docId) {
        String currentStatus = statusMachineService.getCurrentStatus(docId);
        return Result.success(DocStatusEnum.getAllowedTransitions(currentStatus));
    }

    @ApiOperation("检查是否可以跳转到目标状态")
    @GetMapping("/can-transition")
    public Result<Boolean> canTransition(
            @RequestParam Long docId,
            @RequestParam String toStatus) {
        String operatorId = "current_user";
        String operatorName = "当前用户";
        return Result.success(statusMachineService.canTransition(docId, toStatus, operatorId, operatorName));
    }

    @ApiOperation("手动流转状态")
    @PostMapping("/transition")
    public Result<Void> transition(
            @RequestParam Long docId,
            @RequestParam String toStatus,
            @RequestParam(required = false) String transitionReason,
            @RequestParam(required = false) String remark) {
        String operatorId = "current_user";
        String operatorName = "当前用户";
        statusMachineService.transitionWithReason(
                docId, toStatus, transitionReason, operatorId, operatorName, remark);
        return Result.success();
    }

    @ApiOperation("废止公文")
    @PostMapping("/abolish/{docId}")
    public Result<Void> abolish(@PathVariable Long docId,
                             @RequestParam(required = false) String reason,
                             @RequestParam(required = false) String remark) {
        String operatorId = "current_user";
        String operatorName = "当前用户";
        statusMachineService.transitionWithReason(
                docId, DocStatusEnum.ABOLISHED.getCode(), reason, operatorId, operatorName, remark);
        return Result.success();
    }

    @ApiOperation("归档公文")
    @PostMapping("/archive/{docId}")
    public Result<Void> archive(@PathVariable Long docId,
                             @RequestParam(required = false) String remark) {
        String operatorId = "current_user";
        String operatorName = "当前用户";
        statusMachineService.transitionWithReason(
                docId, DocStatusEnum.ARCHIVED.getCode(), "手动归档", operatorId, operatorName, remark);
        return Result.success();
    }
}
