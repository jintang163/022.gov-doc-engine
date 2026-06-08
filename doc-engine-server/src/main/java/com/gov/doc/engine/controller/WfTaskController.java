package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.WfApprovalDTO;
import com.gov.doc.engine.dto.WfTaskAddSignDTO;
import com.gov.doc.engine.dto.WfTaskDelegateDTO;
import com.gov.doc.engine.dto.WfTaskQueryDTO;
import com.gov.doc.engine.service.WfTaskService;
import com.gov.doc.engine.vo.WfTaskCountVO;
import com.gov.doc.engine.vo.WfTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wf/task")
public class WfTaskController {

    @Autowired
    private WfTaskService wfTaskService;

    @GetMapping("/todo/page")
    public Result<PageResult<WfTaskVO>> pageTodo(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ModelAttribute WfTaskQueryDTO queryDTO) {
        PageResult<WfTaskVO> pageResult = wfTaskService.pageTodoList(pageNum, pageSize, queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/done/page")
    public Result<PageResult<WfTaskVO>> pageDone(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ModelAttribute WfTaskQueryDTO queryDTO) {
        PageResult<WfTaskVO> pageResult = wfTaskService.pageDoneList(pageNum, pageSize, queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/page")
    public Result<PageResult<WfTaskVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ModelAttribute WfTaskQueryDTO queryDTO) {
        PageResult<WfTaskVO> pageResult = wfTaskService.pageList(pageNum, pageSize, queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<WfTaskVO> getDetail(@PathVariable Long id) {
        WfTaskVO vo = wfTaskService.getDetail(id);
        return Result.success(vo);
    }

    @GetMapping("/process/{processInstanceId}")
    public Result<List<WfTaskVO>> getByProcessInstanceId(@PathVariable Long processInstanceId) {
        List<WfTaskVO> list = wfTaskService.getByProcessInstanceId(processInstanceId);
        return Result.success(list);
    }

    @PostMapping("/claim/{id}")
    public Result<Void> claim(@PathVariable Long id) {
        wfTaskService.claimTask(id);
        return Result.success("签收成功", null);
    }

    @PostMapping("/unclaim/{id}")
    public Result<Void> unclaim(@PathVariable Long id) {
        wfTaskService.unclaimTask(id);
        return Result.success("取消签收成功", null);
    }

    @PostMapping("/complete")
    public Result<Void> complete(@RequestBody @Validated WfApprovalDTO approvalDTO) {
        wfTaskService.completeTask(approvalDTO);
        return Result.success("审批成功", null);
    }

    @PostMapping("/delegate")
    public Result<Void> delegate(@RequestBody @Validated WfTaskDelegateDTO delegateDTO) {
        wfTaskService.delegateTask(delegateDTO);
        return Result.success("转办成功", null);
    }

    @PostMapping("/add-sign")
    public Result<Void> addSign(@RequestBody @Validated WfTaskAddSignDTO addSignDTO) {
        wfTaskService.addSignTask(addSignDTO);
        return Result.success("加签成功", null);
    }

    @GetMapping("/count")
    public Result<WfTaskCountVO> getTaskCount(@RequestParam(required = false) String userId) {
        WfTaskCountVO vo = wfTaskService.getTaskCount(userId);
        return Result.success(vo);
    }
}
